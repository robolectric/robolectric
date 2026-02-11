package org.robolectric.internal;

import static java.util.Arrays.asList;
import static java.util.Arrays.stream;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.WeakHashMap;
import javax.annotation.Nonnull;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.internal.runners.statements.FailOnTimeout;
import org.junit.rules.RunRules;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
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
import org.robolectric.util.ReflectionHelpers;
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
  private final HashSet<Class<?>> invokedBeforeClasses = new HashSet<>();

  private final HashMap<Class<?>, HelperTestRunner> helperRunners = new HashMap<>();
  private final WeakHashMap<Sandbox, LinkageError> firstLinkageErrors = new WeakHashMap<>();

  private static final boolean USE_LEGACY_SANDBOX_FLOW =
      Boolean.getBoolean("robolectric.useLegacySandboxFlow");

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

  /**
   * An interface used by {@link #mapChildrenInSandbox(List, SandboxChildMapper)} to map a list of
   * {@link FrameworkMethod}s within a specific {@link Sandbox}.
   */
  protected interface SandboxChildMapper {
    List<FrameworkMethod> map(Sandbox sandbox, List<FrameworkMethod> methods);
  }

  /**
   * Provides a mechanism for test runners to perform test discovery or other operations within the
   * context of each test's {@link Sandbox}. This is particularly useful for runners that need
   * access to the Android environment during test discovery, such as parameterizing runners that
   * invoke methods on the test class to generate test cases.
   *
   * <p>The method groups the given {@code children} by their associated {@link Sandbox}, and then
   * applies the provided {@link SandboxChildMapper} to each group within the context of its
   * respective sandbox.
   *
   * @param children The list of {@link FrameworkMethod}s to be mapped.
   * @param mapper The {@link SandboxChildMapper} to apply to each group of methods.
   * @return An {@link ImmutableList} containing the results of the mapping.
   */
  protected ImmutableList<FrameworkMethod> mapChildrenInSandbox(
      List<FrameworkMethod> children, SandboxChildMapper mapper) {
    Map<Sandbox, List<FrameworkMethod>> methodsBySandbox = new LinkedHashMap<>();
    for (FrameworkMethod method : children) {
      if (!isIgnored(method)) {
        methodsBySandbox
            .computeIfAbsent(getSandbox(method), unused -> new ArrayList<>())
            .add(method);
      }
    }

    ImmutableList.Builder<FrameworkMethod> result = ImmutableList.builder();
    for (Map.Entry<Sandbox, List<FrameworkMethod>> entry : methodsBySandbox.entrySet()) {
      List<FrameworkMethod> methods = entry.getValue();
      Sandbox sandbox = ensureSandboxIsAlive(entry.getKey(), methods.get(0));
      configureSandbox(sandbox, methods.get(0));
      sandbox.runOnMainThreadWithClassLoader(
          () -> {
            result.addAll(
                (List<FrameworkMethod>)
                    runInSandbox(
                        sandbox,
                        methods.get(0),
                        /* invokeBeforeClass= */ false,
                        unused -> mapper.map(sandbox, methods)));
          });
    }
    return result.build();
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
    if (USE_LEGACY_SANDBOX_FLOW) {
      return legacyClassBlock(notifier);
    }
    return sandboxGroupingClassBlock(notifier);
  }

  private Statement legacyClassBlock(RunNotifier notifier) {
    Statement statement = childrenInvoker(notifier);
    statement = withAfterClassesInSandbox(statement);
    if (hasClassRules(getTestClass().getJavaClass())) {
      statement = withClassRulesInSandbox(statement);
    }
    return statement;
  }

  private Statement sandboxGroupingClassBlock(RunNotifier notifier) {
    List<FrameworkMethod> children =
        ReflectionHelpers.callInstanceMethod(this, "getFilteredChildren");
    // Use a linked hashmap as a slight improvement to run tests in the order of getChildren.
    Map<Sandbox, List<FrameworkMethod>> methodsBySandbox = new LinkedHashMap<>();
    for (FrameworkMethod method : children) {
      Description description = describeChild(method);
      if (!isIgnored(method)) {
        try {
          Sandbox sandbox = getSandbox(method);
          methodsBySandbox.computeIfAbsent(sandbox, k -> new ArrayList<>()).add(method);
        } catch (IllegalArgumentException e) {
          notifier.fireTestStarted(description);
          notifier.fireTestFailure(new Failure(description, e));
          notifier.fireTestFinished(description);
        }
      } else {
        // send ignored tests to the notifier listeners
        notifier.fireTestIgnored(description);
      }
    }
    return new Statement() {
      @Override
      public void evaluate() throws Throwable {
        // generating nested statement for all the tests in each sandboxes
        for (Map.Entry<Sandbox, List<FrameworkMethod>> entry : methodsBySandbox.entrySet()) {
          FrameworkMethod firstMethod = entry.getValue().get(0);
          Sandbox sandbox = ensureSandboxIsAlive(entry.getKey(), firstMethod);

          Statement statement = childrenInvoker(entry.getValue(), notifier);

          Class<?> bootstrappedTestClass = sandbox.bootstrappedClass(getTestClass().getJavaClass());

          HelperTestRunner helperTestRunner = getCachedHelperTestRunner(bootstrappedTestClass);
          statement = helperTestRunner.withBeforeClasses(statement);
          statement = helperTestRunner.withAfterClasses(statement);

          statement = withClassRules(statement, bootstrappedTestClass);

          // Use the first method to setup a sandbox and invoke everything in that sandbox
          Statement statementsOfTestGroup = inSandboxThread(sandbox, firstMethod, statement);
          statementsOfTestGroup.evaluate();
        }
      }
    };
  }

  private static boolean hasClassRules(Class<?> testClass) {
    for (Field field : testClass.getDeclaredFields()) {
      if (Modifier.isStatic(field.getModifiers()) && field.isAnnotationPresent(ClassRule.class)) {
        return true;
      }
    }
    return false;
  }

  private Statement withAfterClassesInSandbox(Statement base) {
    return new Statement() {
      @Override
      public void evaluate() throws Throwable {
        try {
          base.evaluate();
          for (Map.Entry<Class<?>, Sandbox> entry : loadedTestClasses.entrySet()) {
            Sandbox sandbox = entry.getValue();
            sandbox.runOnMainThreadWithClassLoader(
                () -> {
                  try {
                    invokeAfterClass(entry.getKey());
                  } catch (Throwable throwable) {
                    throw Util.sneakyThrow(throwable);
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

  private Statement withClassRules(Statement statement, Class<?> bootstrappedTestClass) {
    HelperTestRunner helperTestRunner = getCachedHelperTestRunner(bootstrappedTestClass);
    return new Statement() {
      @Override
      public void evaluate() throws Throwable {
        List<TestRule> classRules = helperTestRunner.classRules();
        // Create a RunRules instance with the classRules to retain the default ordering of
        // rules.
        if (!classRules.isEmpty()) {
          RunRules runRules = new RunRules(statement, classRules, getDescription());
          runRules.evaluate();
        } else {
          statement.evaluate();
        }
      }
    };
  }

  private Statement withClassRulesInSandbox(Statement statement) {
    for (FrameworkMethod frameworkMethod : getChildren()) {
      Sandbox sandbox = getSandbox(frameworkMethod);
      Class<?> bootstrappedTestClass = sandbox.bootstrappedClass(getTestClass().getJavaClass());

      if (!loadedTestClasses.containsKey(bootstrappedTestClass)) {
        loadedTestClasses.put(bootstrappedTestClass, sandbox);

        // Configure sandbox *BEFORE* setting the ClassLoader. This is necessary because
        // creating the ShadowMap loads all ShadowProviders via ServiceLoader and this is
        // not available once we install the Robolectric class loader.
        configureSandbox(sandbox, frameworkMethod);

        HelperTestRunner helperTestRunner = getCachedHelperTestRunner(bootstrappedTestClass);
        List<TestRule> classRules = helperTestRunner.classRules();
        for (TestRule classRule : classRules) {
          statement = applyRuleInSandbox(classRule, sandbox, statement, getDescription());
        }
      }
    }
    return statement;
  }

  private Statement applyRuleInSandbox(
      TestRule rule, Sandbox sandbox, Statement base, Description description) {
    return new Statement() {
      @Override
      public void evaluate() throws Throwable {
        ClassLoader priorContextClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(sandbox.getRobolectricClassLoader());
        try {
          rule.apply(base, description).evaluate();
        } catch (Throwable throwable) {
          throw Util.sneakyThrow(throwable);
        } finally {
          Thread.currentThread().setContextClassLoader(priorContextClassLoader);
        }
      }
    };
  }

  private void invokeBeforeClass(final Class<?> clazz) throws Throwable {
    if (!invokedBeforeClasses.contains(clazz)) {
      invokedBeforeClasses.add(clazz);

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

  // As the sandbox for all methods are the same, we can just use the first method to get the
  // extra shadows
  private Statement inSandboxThread(Sandbox sandbox, FrameworkMethod firstMethod, Statement base) {
    return new Statement() {
      @Override
      public void evaluate() throws Throwable {
        // Configure shadows *BEFORE* setting the ClassLoader. This is necessary because
        // creating the ShadowMap loads all ShadowProviders via ServiceLoader and this is
        // not available once we install the Robolectric class loader.
        configureSandbox(sandbox, firstMethod);

        sandbox.runOnMainThreadWithClassLoader(
            () -> {
              try {
                base.evaluate();
              } catch (Throwable throwable) {
                throw Util.sneakyThrow(throwable);
              }
            });
      }
    };
  }

  private Statement childrenInvoker(List<FrameworkMethod> children, RunNotifier notifier) {
    return new Statement() {
      @Override
      public void evaluate() throws Throwable {
        for (FrameworkMethod method : children) {
          runChild(method, notifier);
        }
      }
    };
  }

  protected void afterClass() {}

  @Nonnull
  protected Sandbox getSandbox(FrameworkMethod method) {
    InstrumentationConfiguration instrumentationConfiguration = createClassLoaderConfig(method);
    return new Sandbox(instrumentationConfiguration, new UrlResourceProvider(), classInstrumentor);
  }

  /** Returns a sandbox for the given method. If the sandbox is shutdown, returns a new sandbox. */
  // TODO: Evicting sandboxes in these use cases is inefficient seeing as we are retaining the
  //  sandboxes in the methodsBySandox map, seems like it would be better to first group by key
  //  rather than creating the sandbox?
  private Sandbox ensureSandboxIsAlive(Sandbox sandbox, FrameworkMethod method) {
    if (sandbox.isShutdown()) {
      return getSandbox(method);
    }
    return sandbox;
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
    InstrumentationConfiguration.Builder builder = InstrumentationConfiguration.newBuilder();

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
        perfStatsCollector.setEnabled(!perfStatsReporters.isEmpty());

        Event initialization = perfStatsCollector.startEvent("initialization");

        final Sandbox sandbox = getSandbox(method);

        // Configure sandbox *BEFORE* setting the ClassLoader. This is necessary because
        // creating the ShadowMap loads all ShadowProviders via ServiceLoader and this is
        // not available once we install the Robolectric class loader.
        configureSandbox(sandbox, method);

        if (USE_LEGACY_SANDBOX_FLOW) {
          final PerfStatsCollector finalPerfStatsCollector = perfStatsCollector;
          final Event finalInitialization = initialization;
          sandbox.runOnMainThread(
              () ->
                  executeInSandbox(sandbox, method, finalPerfStatsCollector, finalInitialization));
        } else {
          executeInSandbox(sandbox, method, perfStatsCollector, initialization);
        }
      }
    };
  }

  private void executeInSandbox(
      final Sandbox sandbox,
      final FrameworkMethod method,
      PerfStatsCollector perfStatsCollector,
      Event initialization) {
    ClassLoader priorContextClassLoader = null;
    if (USE_LEGACY_SANDBOX_FLOW) {
      priorContextClassLoader = Thread.currentThread().getContextClassLoader();
      Thread.currentThread().setContextClassLoader(sandbox.getRobolectricClassLoader());
    }

    Class<?> bootstrappedTestClass = sandbox.bootstrappedClass(getTestClass().getJavaClass());
    HelperTestRunner helperTestRunner = getCachedHelperTestRunner(bootstrappedTestClass);
    helperTestRunner.frameworkMethod = method;

    try {
      runInSandbox(
          sandbox,
          method,
          /* invokeBeforeClass= */ USE_LEGACY_SANDBOX_FLOW,
          bootstrappedMethod -> {
            initialization.finished();

            Statement statement =
                helperTestRunner.methodBlock(new FrameworkMethod(bootstrappedMethod));
            try {
              statement.evaluate();
            } catch (Throwable t) {
              throw Util.sneakyThrow(t);
            }
            return null;
          });
    } finally {
      if (USE_LEGACY_SANDBOX_FLOW) {
        Thread.currentThread().setContextClassLoader(priorContextClassLoader);
      }
      reportPerfStats(perfStatsCollector);
      perfStatsCollector.reset();
    }
  }

  private interface SandboxCallable<T> {
    T call(Method bootstrappedMethod) throws Throwable;
  }

  private <T> T runInSandbox(
      Sandbox sandbox,
      FrameworkMethod method,
      boolean invokeBeforeClass,
      SandboxCallable<T> callable) {
    Class<?> bootstrappedTestClass = sandbox.bootstrappedClass(getTestClass().getJavaClass());
    // The method class may be different than the test class if the method annotated @Test
    // is declared on a superclass of the test.
    Method bootstrappedMethod = getBootstrappedMethod(sandbox, method);

    T result = null;
    Queue<Throwable> thrown = new ArrayDeque<>();
    try {
      if (invokeBeforeClass) {
        // Only invoke @BeforeClass once per class
        invokeBeforeClass(bootstrappedTestClass);

        // When there is no class rules in the test class, loadedTestClasses should be updated here.
        loadedTestClasses.putIfAbsent(bootstrappedTestClass, sandbox);
      }
      beforeTest(sandbox, method, bootstrappedMethod);

      result = callable.call(bootstrappedMethod);
    } catch (Throwable throwable) {
      thrown.add(throwable);
    }

    try {
      afterTest(method, bootstrappedMethod);
    } catch (Throwable throwable) {
      thrown.add(throwable);
    }

    try {
      finallyAfterTest(method);
    } catch (Throwable throwable) {
      thrown.add(throwable);
    }

    Throwable first = thrown.poll();
    if (first != null) {
      if (first instanceof LinkageError) {
        // Potentially upgrade the LinkageError with a potentially more complete
        // descriptive exception.
        first = handleLinkageError(first, sandbox);
      }
      while (!thrown.isEmpty()) {
        first.addSuppressed(thrown.remove());
      }
      throw Util.sneakyThrow(first);
    }
    return result;
  }

  /**
   * Retrieves the sandboxed version of a test method.
   *
   * <p>When running tests in a sandbox, the classes are loaded by a special class loader. This
   * method takes a {@link FrameworkMethod} from the original class loader and returns the
   * corresponding {@link Method} instance loaded within the provided {@link Sandbox}.
   *
   * @param sandbox The {@link Sandbox} in which the method should be loaded.
   * @param method The JUnit {@link FrameworkMethod} from the original class loader.
   * @return The {@link Method} instance representing the same method within the sandbox's class
   *     loader.
   * @throws RuntimeException if the method cannot be found in the bootstrapped class.
   */
  protected static Method getBootstrappedMethod(Sandbox sandbox, FrameworkMethod method) {
    Class<?> bootstrappedMethodClass =
        sandbox.bootstrappedClass(method.getMethod().getDeclaringClass());
    try {
      Class<?>[] parameterTypes =
          stream(method.getMethod().getParameterTypes())
              .map(type -> type.isPrimitive() ? type : sandbox.bootstrappedClass(type))
              .toArray(Class[]::new);
      return bootstrappedMethodClass.getMethod(method.getMethod().getName(), parameterTypes);
    } catch (NoSuchMethodException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * If an exception occurs when a class is being loaded (e.g. an exception during static
   * initialization), the initial LinkageError is complete and informative. However, in subsequent
   * tests, if the same class is attempted to be loaded, the JVM throws an error that is a truncated
   * and incomplete NoClassDefError. This logic attempts to cache initial LinkageErrors and replace
   * incomplete NoClassDefError with the original and more descriptive LinkageErrors.
   */
  private Throwable handleLinkageError(Throwable throwable, Sandbox sandbox) {
    if (!firstLinkageErrors.containsKey(sandbox)) {
      firstLinkageErrors.put(sandbox, (LinkageError) throwable);
      return throwable;
    }

    if (throwable instanceof NoClassDefFoundError
        && firstLinkageErrors.containsKey(sandbox)
        && linkageErrorsMatch((NoClassDefFoundError) throwable, firstLinkageErrors.get(sandbox))) {
      return firstLinkageErrors.get(sandbox);
    }

    return throwable;
  }

  private boolean linkageErrorsMatch(NoClassDefFoundError error, LinkageError first) {
    if (error.getStackTrace().length == 0 || first.getStackTrace().length == 0) {
      return false;
    }
    StackTraceElement firstElement = error.getStackTrace()[0];
    for (StackTraceElement element : first.getStackTrace()) {
      if (Objects.equals(firstElement, element)) {
        return true;
      }
    }
    return false;
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

  /**
   * The goal of {@link HelperTestRunner} is to have {@link BlockJUnit4ClassRunner} instances that
   * encapsulate bootstrapped test classes. When SandboxTestRunner is initially constructed, the
   * test class is from the main app class loader. This test class can't be used to run tests
   * because it can't load Android classes.
   *
   * <p>Because we need load a separate test class per sandbox, we need to create a new Runner that
   * encapsulates the bootstrapped test class. This is primarily used to invoke the {@link
   * ParentRunner#methodBlock(FrameworkMethod)}, which uses reflection to invoke before/after
   * methods and test rules.
   */
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

    // for visibility from SandboxTestRunner.methodBlock()
    @Override
    public List<TestRule> classRules() {
      return super.classRules();
    }

    @Override
    protected Statement withAfterClasses(Statement statement) {
      return super.withAfterClasses(statement);
    }

    @Override
    protected Statement withBeforeClasses(Statement statement) {
      return super.withBeforeClasses(statement);
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
    return shadowClasses.toArray(new Class[0]);
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
