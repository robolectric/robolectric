package org.robolectric;


import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import java.lang.reflect.Method;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.Nonnull;
import org.junit.AssumptionViolatedException;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
import org.robolectric.android.RobolectricManager;
import org.robolectric.android.SandboxConfigurer;
import org.robolectric.annotation.Config;
import org.robolectric.internal.AndroidSandbox;
import org.robolectric.internal.BuckManifestFactory;
import org.robolectric.internal.DefaultManifestFactory;
import org.robolectric.internal.ManifestFactory;
import org.robolectric.internal.ManifestIdentifier;
import org.robolectric.internal.MavenManifestFactory;
import org.robolectric.internal.ResourcesMode;
import org.robolectric.internal.SandboxTestRunner;
import org.robolectric.internal.TestEnvironment;
import org.robolectric.internal.bytecode.ClassInstrumentor;
import org.robolectric.internal.bytecode.Sandbox;
import org.robolectric.internal.bytecode.SandboxClassLoader;
import org.robolectric.junit.SandboxConfigurerFromConfig;
import org.robolectric.manifest.AndroidManifest;
import org.robolectric.pluginapi.Sdk;
import org.robolectric.pluginapi.SdkPicker;
import org.robolectric.pluginapi.config.Configuration;
import org.robolectric.pluginapi.config.ConfigurationStrategy;
import org.robolectric.pluginapi.perf.Metadata;
import org.robolectric.pluginapi.perf.Metric;
import org.robolectric.pluginapi.perf.PerfStatsReporter;
import org.robolectric.plugins.ConfigurationImpl;
import org.robolectric.util.PerfStatsCollector;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.inject.Injector;

/**
 * Loads and runs a test in a {@link SandboxClassLoader} in order to provide a simulation of the
 * Android runtime environment.
 */
@SuppressWarnings("NewApi")
public class RobolectricTestRunner extends SandboxTestRunner {

  public static final String CONFIG_PROPERTIES = "robolectric.properties";
  private static final Injector DEFAULT_INJECTOR = defaultInjector().build();

  static {
    new SecureRandom(); // this starts up the Poller SunPKCS11-Darwin thread early, outside of any Robolectric classloader
  }

  protected static Injector.Builder defaultInjector() {
    return new Injector.Builder()
        .bindDefault(ClassInstrumentor.class, ClassInstrumentor.pickInstrumentorClass())
        .bind(Properties.class, System.getProperties());
  }

  private final SdkPicker sdkPicker;
  private RobolectricManager robolectricManager;
  private final List<PerfStatsReporter> perfStatsReporters;
  private final ConfigurationStrategy configurationStrategy;

  private final ResModeStrategy resModeStrategy = getResModeStrategy();
  private boolean alwaysIncludeVariantMarkersInName =
      Boolean.parseBoolean(
          System.getProperty("robolectric.alwaysIncludeVariantMarkersInTestName", "false"));

  /**
   * Creates a runner to run {@code testClass}. Use the {@link Config} annotation to configure.
   *
   * @param testClass the test class to be run
   * @throws InitializationError if junit says so
   */
  public RobolectricTestRunner(final Class<?> testClass) throws InitializationError {
    this(testClass, DEFAULT_INJECTOR);
  }

  protected RobolectricTestRunner(final Class<?> testClass, Injector injector)
      throws InitializationError {
    super(testClass);

    this.sdkPicker = injector.getInstance(SdkPicker.class);
    this.robolectricManager = injector.getInstance(RobolectricManager.class);
    this.configurationStrategy = injector.getInstance(ConfigurationStrategy.class);
    this.perfStatsReporters = Arrays.asList(injector.getInstance(PerfStatsReporter[].class));
  }

  @Override
  @Nonnull
  protected AndroidSandbox getSandbox(FrameworkMethod method) {
    RobolectricFrameworkMethod roboMethod = (RobolectricFrameworkMethod) method;
    return robolectricManager.getSandbox(roboMethod.configuration);
  }

  @Override
  protected void configureSandbox(Sandbox sandbox, FrameworkMethod method) {
    RobolectricFrameworkMethod roboMethod = (RobolectricFrameworkMethod) method;
    robolectricManager.configure(sandbox, roboMethod.configuration);
  }

  /**
   * An instance of the returned class will be created for each test invocation.
   *
   * Custom TestRunner subclasses may wish to override this method to provide alternate configuration.
   *
   * @return a class which implements {@link TestLifecycle}. This implementation returns a {@link DefaultTestLifecycle}.
   */
  @Nonnull
  protected Class<? extends TestLifecycle> getTestLifecycleClass() {
    return DefaultTestLifecycle.class;
  }

  enum ResModeStrategy {
    legacy,
    binary,
    best,
    both;

    static final ResModeStrategy DEFAULT = best;

    private static ResModeStrategy getFromProperties() {
      String resourcesMode = System.getProperty("robolectric.resourcesMode");
      return resourcesMode == null ? DEFAULT : valueOf(resourcesMode);
    }

    boolean includeLegacy(AndroidManifest appManifest) {
      return appManifest.supportsLegacyResourcesMode()
          &&
          (this == legacy
              || (this == best && !appManifest.supportsBinaryResourcesMode())
              || this == both);
    }

    boolean includeBinary(AndroidManifest appManifest) {
      return appManifest.supportsBinaryResourcesMode()
          && (this == binary || this == best || this == both);
    }
  }

  @Override
  protected List<FrameworkMethod> getChildren() {
    List<FrameworkMethod> children = new ArrayList<>();
    for (FrameworkMethod frameworkMethod : super.getChildren()) {
      try {
        Configuration configuration =
            configurationStrategy.getConfig(getTestClass().getJavaClass(), frameworkMethod.getMethod());

        AndroidManifest appManifest = getAppManifest(configuration);

        List<Sdk> sdksToRun = sdkPicker.selectSdks(configuration, appManifest);
        RobolectricFrameworkMethod last = null;
        for (Sdk sdk : sdksToRun) {
          if (resModeStrategy.includeLegacy(appManifest)) {
            children.add(
                last = wrapFrameworkMethod(frameworkMethod, configuration, appManifest, sdk, ResourcesMode.LEGACY));
          }
          if (resModeStrategy.includeBinary(appManifest)) {
            children.add(
                last = wrapFrameworkMethod(frameworkMethod, configuration, appManifest, sdk, ResourcesMode.BINARY));
          }
        }
        if (last != null) {
          last.dontIncludeVariantMarkersInTestName();
        }
      } catch (IllegalArgumentException e) {
        throw new IllegalArgumentException("failed to configure " +
            getTestClass().getName() + "." + frameworkMethod.getMethod().getName() +
            ": " + e.getMessage(), e);
      }
    }
    return children;
  }

  private RobolectricFrameworkMethod wrapFrameworkMethod(
      FrameworkMethod frameworkMethod, Configuration configuration, AndroidManifest appManifest, Sdk sdk,
      ResourcesMode resourcesMode
  ) {
    Config config = configuration.get(Config.class);

    ConfigurationImpl thisConfig = new ConfigurationImpl(configuration)
        .put(AndroidManifest.class, appManifest)
        .put(Sdk.class, sdk)
        .put(ResourcesMode.class, resourcesMode)
        .put(SandboxConfigurer.class, getSandboxConfigurer(config));

    return new RobolectricFrameworkMethod(
        frameworkMethod.getMethod(), sdk, thisConfig, resourcesMode, resModeStrategy,
        alwaysIncludeVariantMarkersInName);
  }

  @VisibleForTesting
  SandboxConfigurer getSandboxConfigurer(Config config) {
    return new SandboxConfigurerFromConfig(config);
  }

  @Override
  protected Statement methodBlock(FrameworkMethod method) {
    Statement superBlock = super.methodBlock(method);

    return new Statement() {
      @Override
      public void evaluate() throws Throwable {
        PerfStatsCollector perfStatsCollector = PerfStatsCollector.getInstance();
        perfStatsCollector.reset();
        perfStatsCollector.setEnabled(!perfStatsReporters.isEmpty());

        try {
          superBlock.evaluate();
        } finally {
          reportPerfStats(perfStatsCollector);
          perfStatsCollector.reset();
        }
      }
    };
  }

  @Override
  protected void beforeTest(Sandbox sandbox, FrameworkMethod method, Method bootstrappedMethod) throws Throwable {
    AndroidSandbox androidSandbox = (AndroidSandbox) sandbox;
    RobolectricFrameworkMethod roboMethod = (RobolectricFrameworkMethod) method;

    PerfStatsCollector perfStatsCollector = PerfStatsCollector.getInstance();
    Sdk sdk = roboMethod.getSdk();
    perfStatsCollector.putMetadata(
        AndroidMetadata.class,
        new AndroidMetadata(
            ImmutableMap.of("ro.build.version.sdk", "" + sdk.getApiLevel()),
            roboMethod.resourcesMode.name()));

    System.out.println(
        "[Robolectric] " + roboMethod.getDeclaringClass().getName() + "."
            + roboMethod.getMethod().getName() + ": sdk=" + sdk.getApiLevel()
            + "; resources=" + roboMethod.resourcesMode);

    if (roboMethod.resourcesMode == ResourcesMode.LEGACY) {
      System.out.println(
          "[Robolectric] NOTICE: legacy resources mode is deprecated; see http://robolectric.org/migrating/#migrating-to-40");
    }

    roboMethod.prepareContext(androidSandbox.getTestEnvironment());
    Class<TestLifecycle> cl = androidSandbox.bootstrappedClass(getTestLifecycleClass());
    roboMethod.testLifecycle = ReflectionHelpers.newInstance(cl);

    androidSandbox.getTestEnvironment()
        .setUpApplicationState(roboMethod.configuration, getTestName(bootstrappedMethod));

    roboMethod.testLifecycle.beforeTest(bootstrappedMethod);
  }

  @Override
  protected void afterTest(Sandbox sandbox, FrameworkMethod method, Method bootstrappedMethod) {
    AndroidSandbox androidSandbox = (AndroidSandbox) sandbox;
    RobolectricFrameworkMethod roboMethod = (RobolectricFrameworkMethod) method;
    try {
      androidSandbox.getTestEnvironment().tearDownApplication();
    } finally {
      roboMethod.testLifecycle.afterTest(bootstrappedMethod);
    }
  }

  @Override
  protected void finallyAfterTest(FrameworkMethod method) {
    RobolectricFrameworkMethod roboMethod = (RobolectricFrameworkMethod) method;

    // If the test was interrupted, it will interfere with new AbstractInterruptibleChannels in
    // subsequent tests, e.g. created by Files.newInputStream(), so clear it and warn.
    if (Thread.interrupted()) {
      System.out.println("WARNING: Test thread was interrupted! " + method.toString());
    }

    try {
      // reset static state afterward too, so statics don't defeat GC?
      PerfStatsCollector.getInstance()
          .measure(
              "reset Android state (after test)",
              () -> roboMethod.getTestEnvironment().resetState());
    } finally {
      roboMethod.testLifecycle = null;
      roboMethod.clearContext();
    }
  }

  /**
   * Create a file system safe directory path name for the current test.
   */
  private String getTestName(Method method) {
    return method.getClass().getSimpleName()
        + "_"
        + method.getName().replaceAll("[^a-zA-Z0-9.-]", "_");
  }

  @Override protected SandboxTestRunner.HelperTestRunner getHelperTestRunner(Class bootstrappedTestClass) {
    try {
      return new HelperTestRunner(bootstrappedTestClass);
    } catch (InitializationError initializationError) {
      throw new RuntimeException(initializationError);
    }
  }

  /**
   * Detects which build system is in use and returns the appropriate ManifestFactory implementation.
   *
   * Custom TestRunner subclasses may wish to override this method to provide alternate configuration.
   *
   * @param config Specification of the SDK version, manifest file, package name, etc.
   */
  protected ManifestFactory getManifestFactory(Config config) {
    Properties buildSystemApiProperties = getBuildSystemApiProperties();
    if (buildSystemApiProperties != null) {
      return new DefaultManifestFactory(buildSystemApiProperties);
    }

    if (BuckManifestFactory.isBuck()) {
      return new BuckManifestFactory();
    } else {
      return new MavenManifestFactory();
    }
  }

  protected Properties getBuildSystemApiProperties() {
    return DefaultManifestFactory.loadBuildSystemApiProperties();
  }

  private AndroidManifest getAppManifest(Configuration configuration) {
    Config config = configuration.get(Config.class);
    ManifestFactory manifestFactory = getManifestFactory(config);
    ManifestIdentifier identifier = manifestFactory.identify(config);
    return robolectricManager.cachedCreateAppManifest(identifier);
  }

  @Override
  protected void afterClass() {
  }

  @Override
  public Object createTest() throws Exception {
    throw new UnsupportedOperationException("this should always be invoked on the HelperTestRunner!");
  }

  @VisibleForTesting
  ResModeStrategy getResModeStrategy() {
    return ResModeStrategy.getFromProperties();
  }

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

  public static class HelperTestRunner extends SandboxTestRunner.HelperTestRunner {
    public HelperTestRunner(Class bootstrappedTestClass) throws InitializationError {
      super(bootstrappedTestClass);
    }

    @Override protected Object createTest() throws Exception {
      Object test = super.createTest();
      RobolectricFrameworkMethod roboMethod = (RobolectricFrameworkMethod) this.frameworkMethod;
      roboMethod.testLifecycle.prepareTest(test);
      return test;
    }

    @Override
    protected Statement methodBlock(FrameworkMethod method) {
      RobolectricFrameworkMethod roboMethod = (RobolectricFrameworkMethod) this.frameworkMethod;
      Statement baseStatement = super.methodBlock(method);
      return new Statement() {
        @Override
        public void evaluate() throws Throwable {
          try {
            baseStatement.evaluate();
          } catch (AssumptionViolatedException e) {
            throw e;
          } catch (Throwable t) {
            roboMethod.getTestEnvironment().checkStateAfterTestFailure(t);
            throw t;
          }
        }
      };
    }
  }

  /**
   * Fields in this class must be serializable using [XStream](https://x-stream.github.io/).
   */
  public static class RobolectricFrameworkMethod extends FrameworkMethod {

    private static final AtomicInteger NEXT_ID = new AtomicInteger();
    private static final Map<Integer, TestExecutionContext> CONTEXT = new HashMap<>();

    private final int id;

    @Nonnull private final Configuration configuration;
    @Nonnull private final ResourcesMode resourcesMode;
    @Nonnull private final ResModeStrategy defaultResModeStrategy;
    private final boolean alwaysIncludeVariantMarkersInName;

    private boolean includeVariantMarkersInTestName = true;
    TestLifecycle<?> testLifecycle;

    RobolectricFrameworkMethod(
        @Nonnull Method method,
        @Nonnull Sdk sdk,
        @Nonnull Configuration configuration,
        @Nonnull ResourcesMode resourcesMode,
        @Nonnull ResModeStrategy defaultResModeStrategy,
        boolean alwaysIncludeVariantMarkersInName) {
      super(method);

      this.configuration = configuration;
      this.resourcesMode = resourcesMode;
      this.defaultResModeStrategy = defaultResModeStrategy;
      this.alwaysIncludeVariantMarkersInName = alwaysIncludeVariantMarkersInName;

      // external storage for things that can't go through a serialization cycle e.g. for PowerMock.
      this.id = NEXT_ID.getAndIncrement();
      CONTEXT.put(id, new TestExecutionContext(sdk));
    }

    @Override
    public String getName() {
      // IDE focused test runs rely on preservation of the test name; we'll use the
      // latest supported SDK for focused test runs
      StringBuilder buf = new StringBuilder(super.getName());

      if (includeVariantMarkersInTestName || alwaysIncludeVariantMarkersInName) {
        buf.append("[").append(getSdk().getApiLevel()).append("]");

        if (defaultResModeStrategy == ResModeStrategy.both) {
          buf.append("[").append(resourcesMode.name()).append("]");
        }
      }

      return buf.toString();
    }

    void dontIncludeVariantMarkersInTestName() {
      includeVariantMarkersInTestName = false;
    }

    @Nonnull
    public Sdk getSdk() {
      return getContext().sdk;
    }

    void prepareContext(TestEnvironment testEnvironment) {
      TestExecutionContext context = getContext();
      context.testEnvironment = testEnvironment;
    }

    TestEnvironment getTestEnvironment() {
      TestExecutionContext context = getContext();
      return context == null ? null : context.testEnvironment;
    }

    public boolean isLegacy() {
      return resourcesMode == ResourcesMode.LEGACY;
    }

    private TestExecutionContext getContext() {
      return CONTEXT.get(id);
    }

    private void clearContext() {
      CONTEXT.remove(id);
    }

    @Override
    protected void finalize() throws Throwable {
      super.finalize();
      clearContext();
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      if (!super.equals(o)) return false;

      RobolectricFrameworkMethod that = (RobolectricFrameworkMethod) o;

      return getSdk().equals(that.getSdk()) && resourcesMode == that.resourcesMode;
    }

    @Override
    public int hashCode() {
      int result = super.hashCode();
      result = 31 * result + getSdk().hashCode();
      result = 31 * result + resourcesMode.ordinal();
      return result;
    }

    @Override
    public String toString() {
      return getName();
    }

    @Nonnull
    public Configuration getConfiguration() {
      return configuration;
    }

    private static class TestExecutionContext {

      private final Sdk sdk;
      private TestEnvironment testEnvironment;

      TestExecutionContext(Sdk sdk) {
        this.sdk = sdk;
      }
    }
  }

}
