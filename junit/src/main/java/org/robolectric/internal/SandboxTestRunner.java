package org.robolectric.internal;

import static java.util.Arrays.asList;
import static java.util.Arrays.stream;

import com.google.common.base.Splitter;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.internal.runners.statements.FailOnTimeout;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
import org.junit.runners.model.TestClass;
import org.robolectric.internal.bytecode.ClassHandler;
import org.robolectric.internal.bytecode.ClassHandlerBuilder;
import org.robolectric.internal.bytecode.ClassInstrumentor;
import org.robolectric.internal.bytecode.InstrumentationConfiguration;
import org.robolectric.internal.bytecode.Interceptor;
import org.robolectric.internal.bytecode.Interceptors;
import org.robolectric.internal.bytecode.Sandbox;
import org.robolectric.internal.bytecode.SandboxConfig;
import org.robolectric.internal.bytecode.ShadowInfo;
import org.robolectric.internal.bytecode.ShadowMap;
import org.robolectric.internal.bytecode.ShadowProviders;
import org.robolectric.internal.bytecode.UrlResourceProvider;
import org.robolectric.pluginapi.perf.Metadata;
import org.robolectric.pluginapi.perf.Metric;
import org.robolectric.pluginapi.perf.PerfStatsReporter;
import org.robolectric.sandbox.ShadowMatcher;
import org.robolectric.util.PerfStatsCollector;
import org.robolectric.util.PerfStatsCollector.Event;
import org.robolectric.util.Util;
import org.robolectric.util.inject.Injector;

/**
 * Sandbox test runner that runs each test in a sandboxed class loader environment. Typically this
 * runner should not be directly accessed, use {@link org.robolectric.RobolectricTestRunner}
 * instead.
 */
@SuppressWarnings({"NewApi", "AndroidJdkLibsChecker"})
public class SandboxTestRunner extends BlockJUnit4ClassRunner {

  private static final Injector DEFAULT_INJECTOR = defaultInjector().build();

  protected static Injector.Builder defaultInjector() {
    return new Injector.Builder();
  }

  private final ClassInstrumentor classInstrumentor;
  private final Interceptors interceptors;
  private final ShadowProviders shadowProviders;
  protected final ClassHandlerBuilder classHandlerBuilder;

  private final List<PerfStatsReporter> perfStatsReporters;
  private final HashMap<Class<?>, Sandbox> loadedTestClasses = new HashMap<>();
  private final HashMap<Class<?>, HelperTestRunner> helperRunners = new HashMap<>();

  public SandboxTestRunner(Class<?> klass) throws InitializationError {
    this(klass, DEFAULT_INJECTOR);
  }

  public SandboxTestRunner(Class<?> klass, Injector injector) throws InitializationError {
    super(klass);

    classInstrumentor = injector.getInstance(ClassInstrumentor.class);
    interceptors = new Interceptors(findInterceptors());
    shadowProviders = injector.getInstance(ShadowProviders.class);
    classHandlerBuilder = injector.getInstance(ClassHandlerBuilder.class);
    perfStatsReporters = Arrays.asList(injector.getInstance(PerfStatsReporter[].class));
  }

  @Nonnull
  protected Collection<Interceptor> findInterceptors() {
    return Collections.emptyList();
  }

  @Nonnull
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
          for (Map.Entry<Class<?>, Sandbox> entry : loadedTestClasses.entrySet()) {
            Sandbox sandbox = entry.getValue();
            sandbox.runOnMainThread(
                () -> {
                  ClassLoader priorContextClassLoader =
                      Thread.currentThread().getContextClassLoader();
                  Thread.currentThread().setContextClassLoader(sandbox.getRobolectricClassLoader());
                  try {
                    invokeAfterClass(entry.getKey());
                  } catch (Throwable throwable) {
                    throw Util.sneakyThrow(throwable);
                  } finally {
                    Thread.currentThread().setContextClassLoader(priorContextClassLoader);
                  }
                });
          }
        } finally {
          afterClass();
          loadedTestClasses.clear();
        }
      }
    };
  }

  private void invokeBeforeClass(final Class<?> clazz, final Sandbox sandbox) throws Throwable {
    if (!loadedTestClasses.containsKey(clazz)) {
      loadedTestClasses.put(clazz, sandbox);

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

  protected void afterClass() {}

  @Nonnull
  protected Sandbox getSandbox(FrameworkMethod method) {
    InstrumentationConfiguration instrumentationConfiguration = createClassLoaderConfig(method);
    return new Sandbox(instrumentationConfiguration, new UrlResourceProvider(), classInstrumentor);
  }

  /**
   * Create an {@link InstrumentationConfiguration} suitable for the provided {@link
   * FrameworkMethod}.
   *
   * <p>Custom TestRunner subclasses may wish to override this method to provide alternate
   * configuration.
   *
   * @param method the test method that's about to run
   * @return an {@link InstrumentationConfiguration}
   */
  @Nonnull
  protected InstrumentationConfiguration createClassLoaderConfig(FrameworkMethod method) {
    InstrumentationConfiguration.Builder builder =
        InstrumentationConfiguration.newBuilder()
            .doNotAcquirePackage("java.")
            .doNotAcquirePackage("jdk.internal.")
            .doNotAcquirePackage("sun.")
            .doNotAcquirePackage("org.robolectric.annotation.")
            .doNotAcquirePackage("org.robolectric.internal.")
            .doNotAcquirePackage("org.robolectric.pluginapi.")
            .doNotAcquirePackage("org.robolectric.util.")
            .doNotAcquirePackage("org.junit");

    String customPackages = System.getProperty("org.robolectric.packagesToNotAcquire", "");
    for (String pkg : Splitter.on(',').split(customPackages)) {
      if (!pkg.isEmpty()) {
        builder.doNotAcquirePackage(pkg);
      }
    }

    String customClassesRegex =
        System.getProperty("org.robolectric.classesToNotInstrumentRegex", "");
    if (!customClassesRegex.isEmpty()) {
      builder.setDoNotInstrumentClassRegex(customClassesRegex);
    }

    for (Class<?> shadowClass : getExtraShadows(method)) {
      ShadowInfo shadowInfo = ShadowMap.obtainShadowInfo(shadowClass);
      builder.addInstrumentedClass(shadowInfo.shadowedClassName);
    }

    addInstrumentedPackages(method, builder);

    return builder.build();
  }

  private void addInstrumentedPackages(
      FrameworkMethod method, InstrumentationConfiguration.Builder builder) {
    SandboxConfig classConfig = getTestClass().getJavaClass().getAnnotation(SandboxConfig.class);
    if (classConfig != null) {
      for (String pkgName : classConfig.instrumentedPackages()) {
        builder.addInstrumentedPackage(pkgName);
      }
    }

    SandboxConfig methodConfig = method.getAnnotation(SandboxConfig.class);
    if (methodConfig != null) {
      for (String pkgName : methodConfig.instrumentedPackages()) {
        builder.addInstrumentedPackage(pkgName);
      }
    }
  }

  protected void configureSandbox(Sandbox sandbox, FrameworkMethod method) {
    ShadowMap.Builder builder = shadowProviders.getBaseShadowMap().newBuilder();

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

  @Override
  @SuppressWarnings("CatchAndPrintStackTrace")
  protected Statement methodBlock(final FrameworkMethod method) {
    return new Statement() {
      @Override
      public void evaluate() throws Throwable {
        PerfStatsCollector perfStatsCollector = PerfStatsCollector.getInstance();
        perfStatsCollector.reset();
        perfStatsCollector.setEnabled(!perfStatsReporters.isEmpty());

        Event initialization = perfStatsCollector.startEvent("initialization");

        final Sandbox sandbox = getSandbox(method);

        // Configure sandbox *BEFORE* setting the ClassLoader. This is necessary because
        // creating the ShadowMap loads all ShadowProviders via ServiceLoader and this is
        // not available once we install the Robolectric class loader.
        configureSandbox(sandbox, method);

        sandbox.runOnMainThread(
            () -> {
              ClassLoader priorContextClassLoader = Thread.currentThread().getContextClassLoader();
              Thread.currentThread().setContextClassLoader(sandbox.getRobolectricClassLoader());

              Class<?> bootstrappedTestClass =
                  sandbox.bootstrappedClass(getTestClass().getJavaClass());
              HelperTestRunner helperTestRunner = getCachedHelperTestRunner(bootstrappedTestClass);
              helperTestRunner.frameworkMethod = method;

              final Method bootstrappedMethod;
              try {
                Class<?>[] parameterTypes =
                    stream(method.getMethod().getParameterTypes())
                        .map(type -> type.isPrimitive() ? type : sandbox.bootstrappedClass(type))
                        .toArray(Class[]::new);
                bootstrappedMethod =
                    bootstrappedTestClass.getMethod(method.getMethod().getName(), parameterTypes);
              } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
              }

              try {
                // Only invoke @BeforeClass once per class
                invokeBeforeClass(bootstrappedTestClass, sandbox);

                beforeTest(sandbox, method, bootstrappedMethod);

                initialization.finished();

                Statement statement =
                    helperTestRunner.methodBlock(new FrameworkMethod(bootstrappedMethod));

                // todo: this try/finally probably isn't right -- should mimic RunAfters? [xw]
                try {
                  statement.evaluate();
                } finally {
                  afterTest(method, bootstrappedMethod);
                }
              } catch (Throwable throwable) {
                throw Util.sneakyThrow(throwable);
              } finally {
                Thread.currentThread().setContextClassLoader(priorContextClassLoader);
                try {
                  finallyAfterTest(method);
                } catch (Exception e) {
                  e.printStackTrace();
                }
              }
            });

        reportPerfStats(perfStatsCollector);
        perfStatsCollector.reset();
      }
    };
  }

  @SuppressWarnings("CatchAndPrintStackTrace")
  private void reportPerfStats(PerfStatsCollector perfStatsCollector) {
    if (perfStatsReporters.isEmpty()) {
      return;
    }

    Metadata metadata = perfStatsCollector.getMetadata();
    Collection<Metric> metrics = perfStatsCollector.getMetrics();

    for (PerfStatsReporter perfStatsReporter : perfStatsReporters) {
      try {
        perfStatsReporter.report(metadata, metrics);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  protected void beforeTest(Sandbox sandbox, FrameworkMethod method, Method bootstrappedMethod)
      throws Throwable {}

  protected void afterTest(FrameworkMethod method, Method bootstrappedMethod) {}

  protected void finallyAfterTest(FrameworkMethod method) {}

  protected HelperTestRunner getHelperTestRunner(Class<?> bootstrappedTestClass)
      throws InitializationError {
    return new HelperTestRunner(bootstrappedTestClass);
  }

  private HelperTestRunner getCachedHelperTestRunner(Class<?> bootstrappedTestClass) {
    return helperRunners.computeIfAbsent(
        bootstrappedTestClass,
        klass -> {
          try {
            return getHelperTestRunner(klass);
          } catch (InitializationError e) {
            throw new RuntimeException(e);
          }
        });
  }

  protected static class HelperTestRunner extends BlockJUnit4ClassRunner {
    public FrameworkMethod frameworkMethod;

    public HelperTestRunner(Class<?> klass) throws InitializationError {
      super(klass);
    }

    // for visibility from SandboxTestRunner.methodBlock()
    @Override
    protected Statement methodBlock(FrameworkMethod method) {
      return super.methodBlock(method);
    }

    /**
     * For tests with a timeout, we need to wrap the test method execution (but not {@code @Before}s
     * or {@code @After}s in a {@link TimeLimitedStatement}. JUnit's built-in {@link FailOnTimeout}
     * statement causes the test method (but not {@code @Before}s or {@code @After}s) to be run on a
     * short-lived thread. This is inadequate for our purposes; we want to guarantee that every
     * entry point to test code is run from the same thread.
     */
    @Override
    protected Statement methodInvoker(FrameworkMethod method, Object test) {
      Statement delegate = super.methodInvoker(method, test);
      long timeout = getTimeout(method.getAnnotation(Test.class));

      if (timeout == 0) {
        return delegate;
      } else {
        return new TimeLimitedStatement(timeout, delegate);
      }
    }

    /**
     * Disables JUnit's normal timeout mode strategy.
     *
     * @see #methodInvoker(FrameworkMethod, Object)
     * @see TimeLimitedStatement
     */
    @Override
    protected Statement withPotentialTimeout(FrameworkMethod method, Object test, Statement next) {
      return next;
    }

    private long getTimeout(Test annotation) {
      if (annotation == null) {
        return 0;
      }
      return annotation.timeout();
    }

    @Override
    protected String testName(FrameworkMethod method) {
      return frameworkMethod.getName();
    }
  }

  @Nonnull
  protected Class<?>[] getExtraShadows(FrameworkMethod method) {
    List<Class<?>> shadowClasses = new ArrayList<>();
    addShadows(shadowClasses, getTestClass().getJavaClass().getAnnotation(SandboxConfig.class));
    addShadows(shadowClasses, method.getAnnotation(SandboxConfig.class));
    return shadowClasses.toArray(new Class[shadowClasses.size()]);
  }

  private void addShadows(List<Class<?>> shadowClasses, SandboxConfig annotation) {
    if (annotation != null) {
      shadowClasses.addAll(asList(annotation.shadows()));
    }
  }

  @Nonnull
  protected ClassHandler createClassHandler(ShadowMap shadowMap, Sandbox sandbox) {
    return classHandlerBuilder.build(shadowMap, ShadowMatcher.MATCH_ALL, interceptors);
  }

  /**
   * Disables JUnit's normal timeout mode strategy.
   *
   * @see #methodInvoker(FrameworkMethod, Object)
   * @see TimeLimitedStatement
   */
  protected Statement withPotentialTimeout(FrameworkMethod method, Object test, Statement next) {
    return next;
  }
}
