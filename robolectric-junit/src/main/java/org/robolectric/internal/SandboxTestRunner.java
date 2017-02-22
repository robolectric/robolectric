package org.robolectric.internal;

import org.jetbrains.annotations.NotNull;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.internal.AssumptionViolatedException;
import org.junit.internal.runners.model.EachTestNotifier;
import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
import org.junit.runners.model.TestClass;
import org.robolectric.internal.bytecode.ClassHandler;
import org.robolectric.internal.bytecode.InstrumentationConfiguration;
import org.robolectric.internal.bytecode.Interceptor;
import org.robolectric.internal.bytecode.Interceptors;
import org.robolectric.internal.bytecode.Sandbox;
import org.robolectric.internal.bytecode.SandboxClassLoader;
import org.robolectric.internal.bytecode.SandboxConfig;
import org.robolectric.internal.bytecode.ShadowMap;
import org.robolectric.internal.bytecode.ShadowWrangler;

import java.lang.reflect.Method;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static java.util.Arrays.asList;

public class SandboxTestRunner extends BlockJUnit4ClassRunner {

  private final Interceptors interceptors;
  private final HashSet<Class<?>> loadedTestClasses = new HashSet<>();

  public SandboxTestRunner(Class<?> klass) throws InitializationError {
    super(klass);
    interceptors = new Interceptors(findInterceptors());
  }

  @NotNull
  protected Collection<Interceptor> findInterceptors() {
    return Collections.emptyList();
  }

  @NotNull
  protected Interceptors getInterceptors() {
    return interceptors;
  }

  @Override
  protected Statement classBlock(RunNotifier notifier) {
    final Statement statement = childrenInvoker(notifier);
    return new Statement() {
      @Override
      public void evaluate() throws Throwable {
        try {
          statement.evaluate();
          for (Class<?> testClass : loadedTestClasses) {
            invokeAfterClass(testClass);
          }
        } finally {
          afterClass();
          loadedTestClasses.clear();
        }
      }
    };
  }

  private void invokeBeforeClass(final Class clazz) throws Throwable {
    if (!loadedTestClasses.contains(clazz)) {
      loadedTestClasses.add(clazz);

      final TestClass testClass = new TestClass(clazz);
      final List<FrameworkMethod> befores = testClass.getAnnotatedMethods(BeforeClass.class);
      for (FrameworkMethod before : befores) {
        before.invokeExplosively(null);
      }
    }
  }

  private static void invokeAfterClass(final Class<?> clazz) throws Throwable {
    final TestClass testClass = new TestClass(clazz);
    final List<FrameworkMethod> afters = testClass.getAnnotatedMethods(AfterClass.class);
    for (FrameworkMethod after : afters) {
      after.invokeExplosively(null);
    }
  }

  protected void afterClass() {
  }

  @Override
  protected void runChild(FrameworkMethod method, RunNotifier notifier) {
    Description description = describeChild(method);
    EachTestNotifier eachNotifier = new EachTestNotifier(notifier, description);

    if (shouldIgnore(method)) {
      eachNotifier.fireTestIgnored();
    } else {
      eachNotifier.fireTestStarted();

      try {
        methodBlock(method).evaluate();
      } catch (AssumptionViolatedException e) {
        eachNotifier.addFailedAssumption(e);
      } catch (Throwable e) {
        eachNotifier.addFailure(e);
      } finally {
        eachNotifier.fireTestFinished();
      }
    }
  }

  @NotNull
  protected Sandbox getSandbox(FrameworkMethod method) {
    InstrumentationConfiguration instrumentationConfiguration = createClassLoaderConfig(method);
    URLClassLoader systemClassLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
    ClassLoader sandboxClassLoader = new SandboxClassLoader(systemClassLoader, instrumentationConfiguration);
    Sandbox sandbox = new Sandbox(sandboxClassLoader);
    configureShadows(method, sandbox);
    return sandbox;
  }

  /**
   * Create an {@link InstrumentationConfiguration} suitable for the provided {@link FrameworkMethod}.
   *
   * Custom TestRunner subclasses may wish to override this method to provide alternate configuration.
   *
   * @param method the test method that's about to run
   * @return an {@link InstrumentationConfiguration}
   */
  @NotNull
  protected InstrumentationConfiguration createClassLoaderConfig(FrameworkMethod method) {
    InstrumentationConfiguration.Builder builder = InstrumentationConfiguration.newBuilder()
        .doNotAcquirePackage("java.")
        .doNotAcquirePackage("sun.")
        .doNotAcquirePackage("org.robolectric.annotation.")
        .doNotAcquirePackage("org.robolectric.internal.")
        .doNotAcquirePackage("org.robolectric.util.")
        .doNotAcquirePackage("org.junit.");

    for (Class<?> shadowClass : getExtraShadows(method)) {
      ShadowMap.ShadowInfo shadowInfo = ShadowMap.getShadowInfo(shadowClass);
      builder.addInstrumentedClass(shadowInfo.getShadowedClassName());
    }

    return builder.build();
  }

  protected void configureShadows(FrameworkMethod method, Sandbox sandbox) {
    ShadowMap.Builder builder = createShadowMap().newBuilder();

    // Configure shadows *BEFORE* setting the ClassLoader. This is necessary because
    // creating the ShadowMap loads all ShadowProviders via ServiceLoader and this is
    // not available once we install the Robolectric class loader.
    Class<?>[] shadows = getExtraShadows(method);
    if (shadows.length > 0) {
      builder.addShadowClasses(shadows);
    }
    ShadowMap shadowMap = builder.build();
    sandbox.replaceShadowMap(shadowMap);

    sandbox.configure(createClassHandler(shadowMap, sandbox), getInterceptors());
  }

  protected Statement methodBlock(final FrameworkMethod method) {
    return new Statement() {
      @Override
      public void evaluate() throws Throwable {
        Sandbox sandbox = getSandbox(method);

        // Configure shadows *BEFORE* setting the ClassLoader. This is necessary because
        // creating the ShadowMap loads all ShadowProviders via ServiceLoader and this is
        // not available once we install the Robolectric class loader.
        configureShadows(method, sandbox);

        final ClassLoader priorContextClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(sandbox.getRobolectricClassLoader());

        //noinspection unchecked
        Class bootstrappedTestClass = sandbox.bootstrappedClass(getTestClass().getJavaClass());
        HelperTestRunner helperTestRunner = getHelperTestRunner(bootstrappedTestClass);
        helperTestRunner.frameworkMethod = method;

        final Method bootstrappedMethod;
        try {
          //noinspection unchecked
          bootstrappedMethod = bootstrappedTestClass.getMethod(method.getMethod().getName());
        } catch (NoSuchMethodException e) {
          throw new RuntimeException(e);
        }

        try {
          // Only invoke @BeforeClass once per class
          invokeBeforeClass(bootstrappedTestClass);

          beforeTest(sandbox, method, bootstrappedMethod);

          final Statement statement = helperTestRunner.methodBlock(new FrameworkMethod(bootstrappedMethod));

          // todo: this try/finally probably isn't right -- should mimic RunAfters? [xw]
          try {
            statement.evaluate();
          } finally {
            afterTest(method, bootstrappedMethod);
          }
        } finally {
          Thread.currentThread().setContextClassLoader(priorContextClassLoader);
          finallyAfterTest(method);
        }
      }
    };
  }

  protected void beforeTest(Sandbox sandbox, FrameworkMethod method, Method bootstrappedMethod) throws Throwable {
  }

  protected void afterTest(FrameworkMethod method, Method bootstrappedMethod) {
  }

  protected void finallyAfterTest(FrameworkMethod method) {
  }

  protected HelperTestRunner getHelperTestRunner(Class bootstrappedTestClass) {
    try {
      return new HelperTestRunner(bootstrappedTestClass);
    } catch (InitializationError initializationError) {
      throw new RuntimeException(initializationError);
    }
  }

  protected static class HelperTestRunner extends BlockJUnit4ClassRunner {
    public FrameworkMethod frameworkMethod;

    public HelperTestRunner(Class<?> klass) throws InitializationError {
      super(klass);
    }

    // cuz accessibility
    @Override
    protected Statement methodBlock(FrameworkMethod method) {
      return super.methodBlock(method);
    }
  }

  @NotNull
  protected Class<?>[] getExtraShadows(FrameworkMethod method) {
    List<Class<?>> shadowClasses = new ArrayList<>();
    addShadows(shadowClasses, getTestClass().getAnnotation(SandboxConfig.class));
    addShadows(shadowClasses, method.getAnnotation(SandboxConfig.class));
    return shadowClasses.toArray(new Class[shadowClasses.size()]);
  }

  private void addShadows(List<Class<?>> shadowClasses, SandboxConfig annotation) {
    if (annotation != null) {
      shadowClasses.addAll(asList(annotation.shadows()));
    }
  }

  protected ShadowMap createShadowMap() {
    return ShadowMap.EMPTY;
  }

  @NotNull
  protected ClassHandler createClassHandler(ShadowMap shadowMap, Sandbox sandbox) {
    return new ShadowWrangler(shadowMap, 0, interceptors);
  }

  protected boolean shouldIgnore(FrameworkMethod method) {
    return method.getAnnotation(Ignore.class) != null;
  }
}