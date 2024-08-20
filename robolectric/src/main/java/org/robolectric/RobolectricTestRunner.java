package org.robolectric;

import com.google.auto.service.AutoService;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import javax.annotation.Nonnull;
import javax.annotation.Priority;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
import org.robolectric.android.AndroidSdkShadowMatcher;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.GraphicsMode;
import org.robolectric.annotation.LooperMode;
import org.robolectric.annotation.LooperMode.Mode;
import org.robolectric.annotation.ResourcesMode;
import org.robolectric.annotation.SQLiteMode;
import org.robolectric.config.AndroidConfigurer;
import org.robolectric.interceptors.AndroidInterceptors;
import org.robolectric.internal.AndroidSandbox;
import org.robolectric.internal.DefaultManifestFactory;
import org.robolectric.internal.ManifestFactory;
import org.robolectric.internal.ManifestIdentifier;
import org.robolectric.internal.MavenManifestFactory;
import org.robolectric.internal.SandboxManager;
import org.robolectric.internal.SandboxTestRunner;
import org.robolectric.internal.TestEnvironment;
import org.robolectric.internal.bytecode.ClassHandler;
import org.robolectric.internal.bytecode.InstrumentationConfiguration;
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
import org.robolectric.util.Logger;
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
  private static final int MAX_DATA_DIR_NAME_LENGTH = 120;
  private static final Injector DEFAULT_INJECTOR = defaultInjector().build();
  private static final Map<ManifestIdentifier, AndroidManifest> appManifestsCache = new HashMap<>();

  static {
    // This starts up the Poller SunPKCS11-Darwin thread early, outside of any Robolectric
    // classloader.
    new SecureRandom();
    // Fixes an issue using AWT-backed graphics shadows when using X11 forwarding.
    System.setProperty("java.awt.headless", "true");
    // Fixes a performance regression in caused by the addition of RSA modulus
    // validation introduced in Bouncy Castle 1.71.
    // https://github.com/bcgit/bc-java/issues/1144
    System.setProperty("org.bouncycastle.rsa.max_mr_tests", "0");
  }

  protected static Injector.Builder defaultInjector() {
    return SandboxTestRunner.defaultInjector().bind(Properties.class, System.getProperties());
  }

  private final SandboxManager sandboxManager;
  private final SdkPicker sdkPicker;
  private final ConfigurationStrategy configurationStrategy;
  private final AndroidConfigurer androidConfigurer;

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
    this.androidConfigurer = injector.getInstance(AndroidConfigurer.class);
  }

  /**
   * Create a {@link ClassHandler} appropriate for the given arguments.
   *
   * <p>Robolectric may chose to cache the returned instance, keyed by {@code shadowMap} and {@code
   * sandbox}.
   *
   * <p>Custom TestRunner subclasses may wish to override this method to provide alternate
   * configuration.
   *
   * @param shadowMap the {@link ShadowMap} in effect for this test
   * @param sandbox the {@link Sdk} in effect for this test
   * @return an appropriate {@link ShadowWrangler}.
   * @since 2.3
   */
  @Override
  @Nonnull
  protected ClassHandler createClassHandler(ShadowMap shadowMap, Sandbox sandbox) {
    int apiLevel = ((AndroidSandbox) sandbox).getSdk().getApiLevel();
    AndroidSdkShadowMatcher shadowMatcher = new AndroidSdkShadowMatcher(apiLevel);
    return classHandlerBuilder.build(shadowMap, shadowMatcher, getInterceptors());
  }

  @Override
  @Nonnull // todo
  protected Collection<Interceptor> findInterceptors() {
    return AndroidInterceptors.all();
  }

  /**
   * Create an {@link InstrumentationConfiguration} suitable for the provided {@link
   * FrameworkMethod}.
   *
   * <p>Adds configuration for Android using {@link AndroidConfigurer}.
   *
   * <p>Custom TestRunner subclasses may wish to override this method to provide additional
   * configuration.
   *
   * @param method the test method that's about to run
   * @return an {@link InstrumentationConfiguration}
   */
  @Override
  @Nonnull
  protected InstrumentationConfiguration createClassLoaderConfig(final FrameworkMethod method) {
    Configuration configuration = ((RobolectricFrameworkMethod) method).getConfiguration();
    Config config = configuration.get(Config.class);

    InstrumentationConfiguration.Builder builder =
        new InstrumentationConfiguration.Builder(super.createClassLoaderConfig(method));
    androidConfigurer.configure(builder, getInterceptors());
    androidConfigurer.withConfig(builder, config);
    return builder.build();
  }

  /**
   * An instance of the returned class will be created for each test invocation.
   *
   * <p>Custom TestRunner subclasses may wish to override this method to provide alternate
   * configuration.
   *
   * @return a class which implements {@link TestLifecycle}. This implementation returns a {@link
   *     DefaultTestLifecycle}.
   */
  @Nonnull
  protected Class<? extends TestLifecycle> getTestLifecycleClass() {
    return DefaultTestLifecycle.class;
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
          children.add(
              last =
                  new RobolectricFrameworkMethod(
                      frameworkMethod.getMethod(),
                      appManifest,
                      sdk,
                      configuration,
                      alwaysIncludeVariantMarkersInName));
        }
        if (last != null) {
          last.dontIncludeVariantMarkersInTestName();
        }
      } catch (IllegalArgumentException e) {
        throw new IllegalArgumentException(
            "failed to configure "
                + getTestClass().getName()
                + "."
                + frameworkMethod.getMethod().getName()
                + ": "
                + e.getMessage(),
            e);
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
    ResourcesMode.Mode resourcesMode =
        roboMethod.configuration == null
            ? ResourcesMode.Mode.BINARY
            : roboMethod.configuration.get(ResourcesMode.Mode.class);
    ;

    LooperMode.Mode looperMode =
        roboMethod.configuration == null
            ? Mode.LEGACY
            : roboMethod.configuration.get(LooperMode.Mode.class);

    SQLiteMode.Mode sqliteMode =
        roboMethod.configuration == null
            ? SQLiteMode.Mode.LEGACY
            : roboMethod.configuration.get(SQLiteMode.Mode.class);

    GraphicsMode.Mode graphicsMode =
        roboMethod.configuration == null
            ? GraphicsMode.Mode.LEGACY
            : roboMethod.configuration.get(GraphicsMode.Mode.class);

    sdk.verifySupportedSdk(method.getDeclaringClass().getName());
    return sandboxManager.getAndroidSandbox(
        classLoaderConfig, sdk, resourcesMode, looperMode, sqliteMode, graphicsMode);
  }

  @Override
  protected void beforeTest(Sandbox sandbox, FrameworkMethod method, Method bootstrappedMethod)
      throws Throwable {
    AndroidSandbox androidSandbox = (AndroidSandbox) sandbox;
    RobolectricFrameworkMethod roboMethod = (RobolectricFrameworkMethod) method;

    PerfStatsCollector perfStatsCollector = PerfStatsCollector.getInstance();
    Sdk sdk = roboMethod.getSdk();
    perfStatsCollector.putMetadata(
        AndroidMetadata.class,
        new AndroidMetadata(ImmutableMap.of("ro.build.version.sdk", "" + sdk.getApiLevel())));

    Logger.lifecycle(
        roboMethod.getDeclaringClass().getName()
            + "."
            + roboMethod.getMethod().getName()
            + ": sdk="
            + sdk.getApiLevel());

    roboMethod.setStuff(androidSandbox, androidSandbox.getTestEnvironment());
    Class<TestLifecycle> cl = androidSandbox.bootstrappedClass(getTestLifecycleClass());
    roboMethod.testLifecycle = ReflectionHelpers.newInstance(cl);

    AndroidManifest appManifest = roboMethod.getAppManifest();

    String tmpDirName = getTempDirName(bootstrappedMethod);
    roboMethod
        .getTestEnvironment()
        .setUpApplicationState(tmpDirName, roboMethod.getConfiguration(), appManifest);

    roboMethod.testLifecycle.beforeTest(bootstrappedMethod);
  }

  /** Returns a filesystem-safe directory path name for the current test. */
  private String getTempDirName(Method method) {
    // Cap the size to 120 to avoid unnecessarily long directory names.
    String directoryName =
        (method.getDeclaringClass().getSimpleName() + "_" + method.getName())
            .replaceAll("[^a-zA-Z0-9.-]", "_");
    if (directoryName.length() > MAX_DATA_DIR_NAME_LENGTH) {
      directoryName = directoryName.substring(0, MAX_DATA_DIR_NAME_LENGTH);
    }
    return directoryName;
  }

  @Override
  protected void afterTest(FrameworkMethod method, Method bootstrappedMethod) {
    RobolectricFrameworkMethod roboMethod = (RobolectricFrameworkMethod) method;
    try {
      roboMethod.getTestEnvironment().tearDownApplication();
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
      Logger.warn("Test thread was interrupted! " + method.toString());
    }

    try {
      // reset static state afterward too, so statics don't defeat GC?
      PerfStatsCollector.getInstance()
          .measure(
              "reset Android state (after test)",
              () -> roboMethod.getTestEnvironment().resetState());
    } finally {
      roboMethod.testLifecycle = null;
    }
  }

  @Override
  protected SandboxTestRunner.HelperTestRunner getHelperTestRunner(Class<?> bootstrappedTestClass)
      throws InitializationError {
    return new HelperTestRunner(bootstrappedTestClass);
  }

  /**
   * Detects which build system is in use and returns the appropriate ManifestFactory
   * implementation.
   *
   * <p>Custom TestRunner subclasses may wish to override this method to provide alternate
   * configuration.
   *
   * @param config Specification of the SDK version, manifest file, package name, etc.
   */
  protected ManifestFactory getManifestFactory(Config config) {
    Properties buildSystemApiProperties = getBuildSystemApiProperties();
    if (buildSystemApiProperties != null) {
      return new DefaultManifestFactory(buildSystemApiProperties);
    }

    return new MavenManifestFactory();
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
   *
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

    return new AndroidManifest(
        manifestIdentifier.getManifestFile(),
        manifestIdentifier.getResDir(),
        manifestIdentifier.getAssetDir(),
        libraryManifests,
        manifestIdentifier.getPackageName(),
        manifestIdentifier.getApkFile());
  }

  /** Calculate the configuration for a given test method. */
  private Configuration getConfiguration(Method method) {
    return configurationStrategy.getConfig(getTestClass().getJavaClass(), method);
  }

  /**
   * Provides the base Robolectric configuration {@link Config} used for all tests.
   *
   * <p>Configuration provided for specific packages, test classes, and test method configurations
   * will override values provided here.
   *
   * <p>Custom TestRunner subclasses may wish to override this method to provide alternate
   * configuration. Consider using a {@link Config.Builder}.
   *
   * <p>The default implementation has appropriate values for most use cases.
   *
   * @return global {@link Config} object
   * @deprecated Provide a service implementation of {@link GlobalConfigProvider} instead. This
   *     method will be removed in Robolectric 4.3.
   * @since 3.1.3
   * @see <a href="http://robolectric.org/migrating/#migrating-to-40">Migration Notes</a> for more
   *     details.
   */
  @Deprecated
  @SuppressWarnings("InlineMeSuggester")
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

  @Override
  @Nonnull
  protected Class<?>[] getExtraShadows(FrameworkMethod frameworkMethod) {
    ArrayList<Class<?>> extraShadows = new ArrayList<>();
    RobolectricFrameworkMethod roboFrameworkMethod = (RobolectricFrameworkMethod) frameworkMethod;
    Config config = roboFrameworkMethod.getConfiguration().get(Config.class);
    Collections.addAll(extraShadows, config.shadows());
    return extraShadows.toArray(new Class<?>[] {});
  }

  @Override
  protected void afterClass() {}

  @Override
  public Object createTest() throws Exception {
    throw new UnsupportedOperationException(
        "this should always be invoked on the HelperTestRunner!");
  }

  public static class HelperTestRunner extends SandboxTestRunner.HelperTestRunner {
    public HelperTestRunner(Class bootstrappedTestClass) throws InitializationError {
      super(bootstrappedTestClass);
    }

    @Override
    protected Object createTest() throws Exception {
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
          } catch (org.junit.internal.AssumptionViolatedException e) {
            // catch JUnit's internal AssumptionViolatedException that is the ancestor of all
            // AssumptionViolatedExceptions, including Truth's ThrowableAssumptionViolatedException.
            throw e;
          } catch (Throwable t) {
            roboMethod.getTestEnvironment().checkStateAfterTestFailure(t);
            throw t;
          }
        }
      };
    }
  }

  /** A {@link FrameworkMethod} subclass that contains data required to run Robolectric tests. */
  public static class RobolectricFrameworkMethod extends FrameworkMethod {

    private final int apiLevel;
    @Nonnull private final AndroidManifest appManifest;
    @Nonnull private final Configuration configuration;
    @Nonnull private final Sdk sdk;

    private final boolean alwaysIncludeVariantMarkersInName;

    private boolean includeVariantMarkersInTestName = true;
    TestLifecycle<?> testLifecycle;
    Sandbox sandbox;
    TestEnvironment testEnvironment;

    protected RobolectricFrameworkMethod(RobolectricFrameworkMethod other) {
      this(
          other.getMethod(),
          other.appManifest,
          other.getSdk(),
          other.configuration,
          other.alwaysIncludeVariantMarkersInName);

      includeVariantMarkersInTestName = other.includeVariantMarkersInTestName;
      testLifecycle = other.testLifecycle;
    }

    RobolectricFrameworkMethod(
        @Nonnull Method method,
        @Nonnull AndroidManifest appManifest,
        @Nonnull Sdk sdk,
        @Nonnull Configuration configuration,
        boolean alwaysIncludeVariantMarkersInName) {
      super(method);

      this.apiLevel = sdk.getApiLevel();
      this.appManifest = appManifest;
      this.configuration = configuration;
      this.alwaysIncludeVariantMarkersInName = alwaysIncludeVariantMarkersInName;
      this.sdk = sdk;
    }

    @Override
    public String getName() {
      // IDE focused test runs rely on preservation of the test name; we'll use the
      // latest supported SDK for focused test runs
      StringBuilder buf = new StringBuilder(super.getName());

      if (includeVariantMarkersInTestName || alwaysIncludeVariantMarkersInName) {
        buf.append("[").append(getSdk().getApiLevel()).append("]");
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
      return sdk;
    }

    void setStuff(Sandbox sandbox, TestEnvironment testEnvironment) {
      this.sandbox = sandbox;
      this.testEnvironment = testEnvironment;
    }

    Sandbox getSandbox() {
      return sandbox;
    }

    TestEnvironment getTestEnvironment() {
      return testEnvironment;
    }

    public boolean isLegacy() {
      return false;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof RobolectricFrameworkMethod)) return false;
      if (!super.equals(o)) return false;

      RobolectricFrameworkMethod that = (RobolectricFrameworkMethod) o;

      return apiLevel == that.apiLevel;
    }

    @Override
    public int hashCode() {
      int result = super.hashCode();
      result = 31 * result + apiLevel;
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
  }
}
