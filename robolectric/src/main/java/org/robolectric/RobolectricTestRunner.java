package org.robolectric;


import android.os.Build;
import com.google.auto.service.AutoService;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.ServiceLoader;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.Nonnull;
import javax.annotation.Priority;
import org.junit.AssumptionViolatedException;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.robolectric.android.AndroidInterceptors;
import org.robolectric.annotation.Config;
import org.robolectric.internal.AndroidConfigurer;
import org.robolectric.internal.AndroidSandbox;
import org.robolectric.internal.BuckManifestFactory;
import org.robolectric.internal.DefaultManifestFactory;
import org.robolectric.internal.Environment;
import org.robolectric.internal.ManifestFactory;
import org.robolectric.internal.ManifestIdentifier;
import org.robolectric.internal.MavenManifestFactory;
import org.robolectric.internal.ResourcesMode;
import org.robolectric.internal.SandboxManager;
import org.robolectric.internal.SandboxTestRunner;
import org.robolectric.internal.ShadowProvider;
import org.robolectric.internal.bytecode.ClassHandler;
import org.robolectric.internal.bytecode.InstrumentationConfiguration;
import org.robolectric.internal.bytecode.InstrumentationConfiguration.Builder;
import org.robolectric.internal.bytecode.Interceptor;
import org.robolectric.internal.bytecode.Sandbox;
import org.robolectric.internal.bytecode.SandboxClassLoader;
import org.robolectric.internal.bytecode.ShadowMap;
import org.robolectric.internal.bytecode.ShadowWrangler;
import org.robolectric.manifest.AndroidManifest;
import org.robolectric.pluginapi.Sdk;
import org.robolectric.pluginapi.SdkPicker;
import org.robolectric.pluginapi.config.ConfigurationStrategy;
import org.robolectric.pluginapi.config.ConfigurationStrategy.Configuration;
import org.robolectric.pluginapi.config.GlobalConfigProvider;
import org.robolectric.plugins.HierarchicalConfigurationStrategy.ConfigurationImpl;
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
  private static final Map<ManifestIdentifier, AndroidManifest> appManifestsCache = new HashMap<>();

  static {
    new SecureRandom(); // this starts up the Poller SunPKCS11-Darwin thread early, outside of any Robolectric classloader
  }

  protected static Injector.Builder defaultInjector() {
    return SandboxTestRunner.defaultInjector()
        .bind(Properties.class, System.getProperties());
  }

  private final SandboxManager sandboxManager;
  private final SdkPicker sdkPicker;
  private final ConfigurationStrategy configurationStrategy;

  private ServiceLoader<ShadowProvider> providers;
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
    super(testClass, injector);

    if (DeprecatedTestRunnerDefaultConfigProvider.globalConfig == null) {
      DeprecatedTestRunnerDefaultConfigProvider.globalConfig = buildGlobalConfig();
    }

    this.sandboxManager = injector.getInstance(SandboxManager.class);
    this.sdkPicker = injector.getInstance(SdkPicker.class);
    this.configurationStrategy = injector.getInstance(ConfigurationStrategy.class);
  }

  /**
   * Create a {@link ClassHandler} appropriate for the given arguments.
   *
   * Robolectric may chose to cache the returned instance, keyed by <tt>shadowMap</tt> and <tt>sdk</tt>.
   *
   * Custom TestRunner subclasses may wish to override this method to provide alternate configuration.
   *
   * @param shadowMap the {@link ShadowMap} in effect for this test
   * @param sandbox the {@link Sdk} in effect for this test
   * @return an appropriate {@link ClassHandler}. This implementation returns a {@link ShadowWrangler}.
   * @since 2.3
   */
  @Override
  @Nonnull
  protected ClassHandler createClassHandler(ShadowMap shadowMap, Sandbox sandbox) {
    return new ShadowWrangler(shadowMap, ((AndroidSandbox) sandbox).getSdk().getApiLevel(), getInterceptors());
  }

  @Override
  @Nonnull // todo
  protected Collection<Interceptor> findInterceptors() {
    return AndroidInterceptors.all();
  }

  /**
   * Create an {@link InstrumentationConfiguration} suitable for the provided
   * {@link FrameworkMethod}.
   *
   * Adds configuration for Android using {@link AndroidConfigurer}.
   *
   * Custom TestRunner subclasses may wish to override this method to provide additional
   * configuration.
   *
   * @param method the test method that's about to run
   * @return an {@link InstrumentationConfiguration}
   */
  @Override @Nonnull
  protected InstrumentationConfiguration createClassLoaderConfig(final FrameworkMethod method) {
    Configuration configuration = ((RobolectricFrameworkMethod) method).getConfiguration();
    Config config = configuration.get(Config.class);

    Builder builder = new Builder(super.createClassLoaderConfig(method));
    AndroidConfigurer.configure(builder, getInterceptors());
    AndroidConfigurer.withConfig(builder, config);
    return builder.build();
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
        Configuration configuration = getConfiguration(frameworkMethod.getMethod());

        AndroidManifest appManifest = getAppManifest(configuration);

        List<Sdk> sdksToRun = sdkPicker.selectSdks(configuration, appManifest);
        RobolectricFrameworkMethod last = null;
        for (Sdk sdk : sdksToRun) {
          if (resModeStrategy.includeLegacy(appManifest)) {
            children.add(
                last =
                    new RobolectricFrameworkMethod(
                        frameworkMethod.getMethod(),
                        appManifest,
                        sdk,
                        configuration,
                        ResourcesMode.LEGACY,
                        resModeStrategy,
                        alwaysIncludeVariantMarkersInName));
          }
          if (resModeStrategy.includeBinary(appManifest)) {
            children.add(
                last =
                    new RobolectricFrameworkMethod(
                        frameworkMethod.getMethod(),
                        appManifest,
                        sdk,
                        configuration,
                        ResourcesMode.BINARY,
                        resModeStrategy,
                        alwaysIncludeVariantMarkersInName));
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

  @Override
  @Nonnull
  protected AndroidSandbox getSandbox(FrameworkMethod method) {
    RobolectricFrameworkMethod roboMethod = (RobolectricFrameworkMethod) method;
    Sdk sdk = roboMethod.getSdk();

    InstrumentationConfiguration classLoaderConfig = createClassLoaderConfig(method);
    ResourcesMode resourcesMode = roboMethod.getResourcesMode();

    if (resourcesMode == ResourcesMode.LEGACY && sdk.getApiLevel() > Build.VERSION_CODES.P) {
      throw new AssumptionViolatedException("Robolectric doesn't support legacy mode after P");
    }

    if (sdk.isKnown() && !sdk.isSupported()) {
      throw new AssumptionViolatedException(
          "Failed to create a Robolectric sandbox: " + sdk.getUnsupportedMessage());
    } else {
      return sandboxManager.getAndroidSandbox(classLoaderConfig, sdk, resourcesMode);
    }
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

    roboMethod.setStuff(androidSandbox, androidSandbox.getEnvironment());
    Class<TestLifecycle> cl = androidSandbox.bootstrappedClass(getTestLifecycleClass());
    roboMethod.testLifecycle = ReflectionHelpers.newInstance(cl);

    providers = ServiceLoader.load(ShadowProvider.class, androidSandbox.getRobolectricClassLoader());

    AndroidManifest appManifest = roboMethod.getAppManifest();

    roboMethod.getEnvironment().setUpApplicationState(
        bootstrappedMethod,
        roboMethod.getConfiguration(), appManifest
    );

    roboMethod.testLifecycle.beforeTest(bootstrappedMethod);
  }

  @Override
  protected void afterTest(FrameworkMethod method, Method bootstrappedMethod) {
    RobolectricFrameworkMethod roboMethod = (RobolectricFrameworkMethod) method;
    try {
      roboMethod.getEnvironment().tearDownApplication();
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
          .measure("reset Android state (after test)", () -> {
            // TODO: roboMethod.sandbox.resetState(); instead
            if (providers != null) {
              for (ShadowProvider provider : providers) {
                provider.reset();
              }
            }
          });
    } finally {
      roboMethod.testLifecycle = null;
      roboMethod.clearContext();
    }
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
    return staticGetBuildSystemApiProperties();
  }

  protected static Properties staticGetBuildSystemApiProperties() {
    try (InputStream resourceAsStream =
        RobolectricTestRunner.class.getResourceAsStream(
            "/com/android/tools/test_config.properties")) {
      if (resourceAsStream == null) {
        return null;
      }

      Properties properties = new Properties();
      properties.load(resourceAsStream);
      return properties;
    } catch (IOException e) {
      return null;
    }
  }

  private AndroidManifest getAppManifest(Configuration configuration) {
    Config config = configuration.get(Config.class);
    ManifestFactory manifestFactory = getManifestFactory(config);
    ManifestIdentifier identifier = manifestFactory.identify(config);

    return cachedCreateAppManifest(identifier);
  }

  private AndroidManifest cachedCreateAppManifest(ManifestIdentifier identifier) {
    synchronized (appManifestsCache) {
      AndroidManifest appManifest;
      appManifest = appManifestsCache.get(identifier);
      if (appManifest == null) {
        appManifest = createAndroidManifest(identifier);
        appManifestsCache.put(identifier, appManifest);
      }

      return appManifest;
    }
  }

  /**
   * Internal use only.
   * @deprecated Do not use.
   */
  @Deprecated
  @VisibleForTesting
  public static AndroidManifest createAndroidManifest(ManifestIdentifier manifestIdentifier) {
    List<ManifestIdentifier> libraries = manifestIdentifier.getLibraries();

    List<AndroidManifest> libraryManifests = new ArrayList<>();
    for (ManifestIdentifier library : libraries) {
      libraryManifests.add(createAndroidManifest(library));
    }

    return new AndroidManifest(manifestIdentifier.getManifestFile(), manifestIdentifier.getResDir(),
        manifestIdentifier.getAssetDir(), libraryManifests, manifestIdentifier.getPackageName(),
        manifestIdentifier.getApkFile());
  }


  /**
   * Compute the effective Robolectric configuration for a given test method.
   *
   * Configuration information is collected from package-level <tt>robolectric.properties</tt> files
   * and {@link Config} annotations on test classes, superclasses, and methods.
   *
   * Custom TestRunner subclasses may wish to override this method to provide alternate configuration.
   *
   * @param method the test method
   * @return the effective Robolectric configuration for the given test method
   * @deprecated Provide an implementation of {@link javax.inject.Provider<Config>} instead. See
   *     [Migration Notes](http://robolectric.org/migrating/#migrating-to-40) for details. This
   *     method will be removed in Robolectric 4.3.
   * @since 2.0
   */
  @Deprecated
  public Config getConfig(Method method) {
    throw new UnsupportedOperationException();
  }

  /**
   * Calculate the configuration for a given test method.
   *
   * Temporarily visible for migration.
   * @deprecated Going away before 4.2. DO NOT SHIP.
   */
  @Deprecated
  protected Configuration getConfiguration(Method method) {
    Configuration configuration =
        configurationStrategy.getConfig(getTestClass().getJavaClass(), method);

    // in case #getConfig(Method) has been overridden...
    try {
      Config config = getConfig(method);
      ((ConfigurationImpl) configuration).put(Config.class, config);
    } catch (UnsupportedOperationException e) {
      // no problem
    }

    return configuration;
  }

  /**
   * Provides the base Robolectric configuration {@link Config} used for all tests.
   *
   * Configuration provided for specific packages, test classes, and test method
   * configurations will override values provided here.
   *
   * Custom TestRunner subclasses may wish to override this method to provide
   * alternate configuration. Consider using a {@link Config.Builder}.
   *
   * The default implementation has appropriate values for most use cases.
   *
   * @return global {@link Config} object
   * @deprecated Provide a service implementation of {@link GlobalConfigProvider} instead. See
   *     [Migration Notes](http://robolectric.org/migrating/#migrating-to-40) for details. This
   *     method will be removed in Robolectric 4.3.
   * @since 3.1.3
   */
  @Deprecated
  protected Config buildGlobalConfig() {
    return new Config.Builder().build();
  }

  @AutoService(GlobalConfigProvider.class)
  @Priority(Integer.MIN_VALUE)
  @Deprecated
  public static class DeprecatedTestRunnerDefaultConfigProvider implements GlobalConfigProvider {
    static Config globalConfig;

    @Override
    public Config get() {
      return globalConfig;
    }
  }

  @Override @Nonnull
  protected Class<?>[] getExtraShadows(FrameworkMethod frameworkMethod) {
    Config config = ((RobolectricFrameworkMethod) frameworkMethod).getConfiguration().get(Config.class);
    return config.shadows();
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
  }

  /**
   * Fields in this class must be serializable using [XStream](https://x-stream.github.io/).
   */
  static final class RobolectricFrameworkMethod extends FrameworkMethod {

    private static final AtomicInteger NEXT_ID = new AtomicInteger();
    private static final Map<Integer, TestExecutionContext> CONTEXT = new HashMap<>();
    
    private final int id;

    @Nonnull private final AndroidManifest appManifest;
    @Nonnull private final Configuration configuration;
    @Nonnull private final ResourcesMode resourcesMode;
    @Nonnull private final ResModeStrategy defaultResModeStrategy;
    private final boolean alwaysIncludeVariantMarkersInName;

    private boolean includeVariantMarkersInTestName = true;
    TestLifecycle testLifecycle;

    RobolectricFrameworkMethod(
        @Nonnull Method method,
        @Nonnull AndroidManifest appManifest,
        @Nonnull Sdk sdk,
        @Nonnull Configuration configuration,
        @Nonnull ResourcesMode resourcesMode,
        @Nonnull ResModeStrategy defaultResModeStrategy,
        boolean alwaysIncludeVariantMarkersInName) {
      super(method);

      this.appManifest = appManifest;
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
      //   latest supported SDK for focused test runs
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
    AndroidManifest getAppManifest() {
      return appManifest;
    }

    @Nonnull
    public Sdk getSdk() {
      return getContext().sdk;
    }

    void setStuff(Sandbox sandbox, Environment environment) {
      TestExecutionContext context = getContext();
      context.sandbox = sandbox;
      context.environment = environment;
    }

    Sandbox getSandbox() {
      return getContext().sandbox;
    }

    Environment getEnvironment() {
      TestExecutionContext context = getContext();
      return context == null ? null : context.environment;
    }

    public boolean isLegacy() {
      return resourcesMode == ResourcesMode.LEGACY;
    }

    public ResourcesMode getResourcesMode() {
      return resourcesMode;
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
      private Sandbox sandbox;
      private Environment environment;

      TestExecutionContext(Sdk sdk) {
        this.sdk = sdk;
      }
    }
  }

}
