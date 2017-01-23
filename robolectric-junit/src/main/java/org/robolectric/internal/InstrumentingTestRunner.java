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
import org.robolectric.internal.bytecode.InstrumentingClassLoader;
import org.robolectric.internal.bytecode.Interceptors;
import org.robolectric.internal.bytecode.RoboConfig;
import org.robolectric.internal.bytecode.RobolectricInternals;
import org.robolectric.internal.bytecode.Sandbox;
import org.robolectric.internal.bytecode.ShadowInvalidator;
import org.robolectric.internal.bytecode.ShadowMap;
import org.robolectric.internal.bytecode.ShadowWrangler;
import org.robolectric.util.ReflectionHelpers;

import java.lang.reflect.Method;
import java.net.URL;
import java.util.HashSet;
import java.util.List;

public class InstrumentingTestRunner extends BlockJUnit4ClassRunner {

  private final Interceptors interceptors;
  private final HashSet<Class<?>> loadedTestClasses = new HashSet<>();

  public InstrumentingTestRunner(Class<?> klass) throws InitializationError {
    super(klass);
    interceptors = new Interceptors();
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
    InstrumentationConfiguration instrumentationConfiguration =
        InstrumentationConfiguration.newBuilder()
            .build();
    final InstrumentingClassLoader instrumentingClassLoader =
        new InstrumentingClassLoader(instrumentationConfiguration, new URL[0]);
    Sandbox sandbox = new Sandbox(instrumentingClassLoader);
    configureShadows(method, sandbox);
    return sandbox;
  }

  protected Statement methodBlock(final FrameworkMethod method) {
    return new Statement() {
      @Override
      public void evaluate() throws Throwable {
        Sandbox sandbox = getSandbox(method);

        final ClassLoader priorContextClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(sandbox.getRobolectricClassLoader());

        Class bootstrappedTestClass = sandbox.bootstrappedClass(getTestClass().getJavaClass());
        HelperTestRunner helperTestRunner = getHelperTestRunner(bootstrappedTestClass);

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
          finallyAfterTest();
        }
      }
    };
  }

  protected void beforeTest(Sandbox sandbox, FrameworkMethod method, Method bootstrappedMethod) throws Throwable {
  }

  protected void afterTest(FrameworkMethod method, Method bootstrappedMethod) {
  }

  protected void finallyAfterTest() {
  }

  protected HelperTestRunner getHelperTestRunner(Class bootstrappedTestClass) {
    try {
      return new HelperTestRunner(bootstrappedTestClass);
    } catch (InitializationError initializationError) {
      throw new RuntimeException(initializationError);
    }
  }

  protected static class HelperTestRunner extends BlockJUnit4ClassRunner {
    public HelperTestRunner(Class<?> klass) throws InitializationError {
      super(klass);
    }

    // cuz accessibility
    @Override
    protected Statement methodBlock(FrameworkMethod method) {
      return super.methodBlock(method);
    }
  }

  private void configureShadows(FrameworkMethod method, Sandbox sandbox) {
    ShadowMap.Builder builder = ShadowMap.EMPTY.newBuilder();

    // Configure shadows *BEFORE* setting the ClassLoader. This is necessary because
    // creating the ShadowMap loads all ShadowProviders via ServiceLoader and this is
    // not available once we install the Robolectric class loader.
    RoboConfig roboConfig = method.getAnnotation(RoboConfig.class);
    if (roboConfig != null) {
      builder.addShadowClasses(roboConfig.shadows());
    }
    ShadowMap shadowMap = builder.build();
    sandbox.replaceShadowMap(shadowMap);

    ClassHandler classHandler = createClassHandler(shadowMap);
    injectEnvironment(sandbox.getRobolectricClassLoader(), classHandler, sandbox.getShadowInvalidator());
  }

  private ClassHandler createClassHandler(ShadowMap shadowMap) {
    return new ShadowWrangler(shadowMap, 0, interceptors);
  }

  public static void injectEnvironment(ClassLoader robolectricClassLoader,
                                       ClassHandler classHandler, ShadowInvalidator invalidator) {
    String className = RobolectricInternals.class.getName();
    Class<?> robolectricInternalsClass = ReflectionHelpers.loadClass(robolectricClassLoader, className);
    ReflectionHelpers.setStaticField(robolectricInternalsClass, "classHandler", classHandler);
    ReflectionHelpers.setStaticField(robolectricInternalsClass, "shadowInvalidator", invalidator);
  }

  protected boolean shouldIgnore(FrameworkMethod method) {
    return method.getAnnotation(Ignore.class) != null;
  }

}
