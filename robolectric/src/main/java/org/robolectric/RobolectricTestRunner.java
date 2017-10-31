package org.robolectric;

import android.app.Application;
import android.os.Build;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import javax.annotation.Nonnull;
import org.junit.Ignore;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
import org.robolectric.android.AndroidInterceptors;
import org.robolectric.android.internal.ParallelUniverse;
import org.robolectric.annotation.Config;
import org.robolectric.internal.AndroidConfigurer;
import org.robolectric.internal.BuckManifestFactory;
import org.robolectric.internal.DefaultManifestFactory;
import org.robolectric.internal.GradleManifestFactory;
import org.robolectric.internal.ManifestFactory;
import org.robolectric.internal.ManifestIdentifier;
import org.robolectric.internal.MavenManifestFactory;
import org.robolectric.internal.ParallelUniverseInterface;
import org.robolectric.internal.SandboxFactory;
import org.robolectric.internal.SandboxTestRunner;
import org.robolectric.internal.SdkConfig;
import org.robolectric.internal.SdkEnvironment;
import org.robolectric.internal.bytecode.ClassHandler;
import org.robolectric.internal.bytecode.InstrumentationConfiguration;
import org.robolectric.internal.bytecode.InstrumentationConfiguration.Builder;
import org.robolectric.internal.bytecode.Interceptor;
import org.robolectric.internal.bytecode.Sandbox;
import org.robolectric.internal.bytecode.SandboxClassLoader;
import org.robolectric.internal.bytecode.ShadowMap;
import org.robolectric.internal.bytecode.ShadowWrangler;
import org.robolectric.internal.dependency.CachedDependencyResolver;
import org.robolectric.internal.dependency.DependencyResolver;
import org.robolectric.internal.dependency.LocalDependencyResolver;
import org.robolectric.internal.dependency.PropertiesDependencyResolver;
import org.robolectric.manifest.AndroidManifest;
import org.robolectric.res.Fs;
import org.robolectric.res.FsFile;
import org.robolectric.res.PackageResourceTable;
import org.robolectric.res.ResourceMerger;
import org.robolectric.res.ResourcePath;
import org.robolectric.res.ResourceTable;
import org.robolectric.res.ResourceTableFactory;
import org.robolectric.res.RoutingResourceTable;
import org.robolectric.util.Logger;
import org.robolectric.util.ReflectionHelpers;

/**
 * Installs a {@link SandboxClassLoader} and {@link ResourceTable} in order to
 * provide a simulation of the Android runtime environment.
 */
public class RobolectricTestRunner extends SandboxTestRunner {

  public static final String CONFIG_PROPERTIES = "robolectric.properties";
  
  private static final Map<AndroidManifest, PackageResourceTable> appResourceTableCache = new HashMap<>();
  private static final Map<ManifestIdentifier, AndroidManifest> appManifestsCache = new HashMap<>();
  private static PackageResourceTable compiletimeSdkResourceTable;

  private final SdkPicker sdkPicker;
  private final ConfigMerger configMerger;

  private transient DependencyResolver dependencyResolver;

  static {
    new SecureRandom(); // this starts up the Poller SunPKCS11-Darwin thread early, outside of any Robolectric classloader
  }

  /**
   * Creates a runner to run {@code testClass}. Looks in your working directory for your AndroidManifest.xml file
   * and res directory by default. Use the {@link Config} annotation to configure.
   *
   * @param testClass the test class to be run
   * @throws InitializationError if junit says so
   */
  public RobolectricTestRunner(final Class<?> testClass) throws InitializationError {
    super(testClass);
    this.configMerger = createConfigMerger();
    this.sdkPicker = createSdkPicker();
  }

  protected DependencyResolver getJarResolver() {
    if (dependencyResolver == null) {
      if (Boolean.getBoolean("robolectric.offline")) {
        String propPath = System.getProperty("robolectric-deps.properties");
        if (propPath != null) {
          try {
            dependencyResolver = new PropertiesDependencyResolver(
                Fs.newFile(propPath),
                null);
          } catch (IOException e) {
            throw new RuntimeException("couldn't read dependencies" , e);
          }
        } else {
          String dependencyDir = System.getProperty("robolectric.dependency.dir", ".");
          dependencyResolver = new LocalDependencyResolver(new File(dependencyDir));
        }
      } else {
        File cacheDir = new File(new File(System.getProperty("java.io.tmpdir")), "robolectric");

        Class<?> mavenDependencyResolverClass = ReflectionHelpers.loadClass(RobolectricTestRunner.class.getClassLoader(),
            "org.robolectric.internal.dependency.MavenDependencyResolver");
        DependencyResolver dependencyResolver = (DependencyResolver) ReflectionHelpers.callConstructor(mavenDependencyResolverClass);
        if (cacheDir.exists() || cacheDir.mkdir()) {
          Logger.info("Dependency cache location: %s", cacheDir.getAbsolutePath());
          this.dependencyResolver = new CachedDependencyResolver(dependencyResolver, cacheDir, 60 * 60 * 24 * 1000);
        } else {
          this.dependencyResolver = dependencyResolver;
        }
      }

      URL buildPathPropertiesUrl = getClass().getClassLoader().getResource("robolectric-deps.properties");
      if (buildPathPropertiesUrl != null) {
        Logger.info("Using Robolectric classes from %s", buildPathPropertiesUrl.getPath());

        FsFile propertiesFile = Fs.fileFromPath(buildPathPropertiesUrl.getFile());
        try {
          dependencyResolver = new PropertiesDependencyResolver(propertiesFile, dependencyResolver);
        } catch (IOException e) {
          throw new RuntimeException("couldn't read " + buildPathPropertiesUrl, e);
        }
      }
    }

    return dependencyResolver;
  }

  /**
   * Create a {@link ClassHandler} appropriate for the given arguments.
   *
   * Robolectric may chose to cache the returned instance, keyed by <tt>shadowMap</tt> and <tt>sdkConfig</tt>.
   *
   * Custom TestRunner subclasses may wish to override this method to provide alternate configuration.
   *
   * @param shadowMap the {@link ShadowMap} in effect for this test
   * @param sandbox the {@link SdkConfig} in effect for this test
   * @return an appropriate {@link ClassHandler}. This implementation returns a {@link ShadowWrangler}.
   * @since 2.3
   */
  @Override
  @Nonnull
  protected ClassHandler createClassHandler(ShadowMap shadowMap, Sandbox sandbox) {
    return new ShadowWrangler(shadowMap, ((SdkEnvironment) sandbox).getSdkConfig().getApiLevel(), getInterceptors());
  }

  /**
   * Create a {@link ConfigMerger} for calculating the {@link Config} tests.
   *
   * Custom TestRunner subclasses may wish to override this method to provide alternate configuration.
   *
   * @return an {@link ConfigMerger}.
   * @since 3.2
   */
  @Nonnull
  private ConfigMerger createConfigMerger() {
    return new ConfigMerger();
  }

  /**
   * Create a {@link SdkPicker} for determining which SDKs will be tested.
   *
   * Custom TestRunner subclasses may wish to override this method to provide alternate configuration.
   *
   * @return an {@link SdkPicker}.
   * @since 3.2
   */
  @Nonnull
  protected SdkPicker createSdkPicker() {
    return new SdkPicker();
  }

  @Override
  @Nonnull // todo
  protected Collection<Interceptor> findInterceptors() {
    return AndroidInterceptors.all();
  }

  /**
   * Create an {@link InstrumentationConfiguration} suitable for the provided {@link Config}.
   *
   * Custom TestRunner subclasses may wish to override this method to provide alternate configuration.
   *
   * @param config the merged configuration for the test that's about to run -- todo
   * @return an {@link InstrumentationConfiguration}
   * @deprecated Override {@link #createClassLoaderConfig(FrameworkMethod)} instead
   */
  @Deprecated
  @Nonnull
  public InstrumentationConfiguration createClassLoaderConfig(Config config) {
    FrameworkMethod method = ((MethodPassThrough) config).method;
    Builder builder = new InstrumentationConfiguration.Builder(super.createClassLoaderConfig(method));
    AndroidConfigurer.configure(builder, getInterceptors());
    AndroidConfigurer.withConfig(builder, config);
    return builder.build();
  }

  /**
   * {@inheritDoc}
   */
  @Override @Nonnull
  protected InstrumentationConfiguration createClassLoaderConfig(final FrameworkMethod method) {
    return createClassLoaderConfig(new Config.Builder(((RobolectricFrameworkMethod) method).config) {
      @Override
      public Config.Implementation build() {
        return new MethodPassThrough(method, sdk, minSdk, maxSdk, manifest, qualifiers, packageName, abiSplit, resourceDir, assetDir, buildDir, shadows, instrumentedPackages, application, libraries, constants);
      }
    }.build());
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

  @Override
  protected List<FrameworkMethod> getChildren() {
    List<FrameworkMethod> children = new ArrayList<>();
    for (FrameworkMethod frameworkMethod : super.getChildren()) {
      try {
        Config config = getConfig(frameworkMethod.getMethod());
        AndroidManifest appManifest = getAppManifest(config);
        List<SdkConfig> sdksToRun = sdkPicker.selectSdks(config, appManifest);
        RobolectricFrameworkMethod last = null;
        for (SdkConfig sdkConfig : sdksToRun) {
          last = new RobolectricFrameworkMethod(frameworkMethod.getMethod(), appManifest, sdkConfig, config);
          children.add(last);
        }
        if (last != null) {
          last.dontIncludeApiLevelInName();
        }
      } catch (IllegalArgumentException e) {
        throw new IllegalArgumentException("failed to configure " +
            getTestClass().getName() + "." + frameworkMethod.getMethod().getName() +
            ": " + e.getMessage(), e);
      }
    }
    return children;
  }

  /**
   * Returns the ResourceProvider for the compile time SDK.
   */
  @Nonnull
  private static PackageResourceTable getCompiletimeSdkResourceTable() {
    if (compiletimeSdkResourceTable == null) {
      ResourceTableFactory resourceTableFactory = new ResourceTableFactory();
      compiletimeSdkResourceTable = resourceTableFactory.newFrameworkResourceTable(new ResourcePath(android.R.class, null, null));
    }
    return compiletimeSdkResourceTable;
  }

  /**
   * @deprecated Override {@link #shouldIgnore(FrameworkMethod)} instead.
   */
  @Deprecated
  protected boolean shouldIgnore(FrameworkMethod method, Config config) {
    return method.getAnnotation(Ignore.class) != null;
  }

  @Override protected boolean shouldIgnore(FrameworkMethod method) {
    return shouldIgnore(method, ((RobolectricFrameworkMethod) method).config);
  }

  @Override
  @Nonnull
  protected SdkEnvironment getSandbox(FrameworkMethod method) {
    RobolectricFrameworkMethod roboMethod = (RobolectricFrameworkMethod) method;
    SdkConfig sdkConfig = roboMethod.sdkConfig;
    return SandboxFactory.INSTANCE.getSdkEnvironment(
        createClassLoaderConfig(method), getJarResolver(), sdkConfig);
  }

  @Override
  protected void beforeTest(Sandbox sandbox, FrameworkMethod method, Method bootstrappedMethod) throws Throwable {
    SdkEnvironment sdkEnvironment = (SdkEnvironment) sandbox;
    RobolectricFrameworkMethod roboMethod = (RobolectricFrameworkMethod) method;

    roboMethod.parallelUniverseInterface = getHooksInterface(sdkEnvironment);
    Class<TestLifecycle> cl = sdkEnvironment.bootstrappedClass(getTestLifecycleClass());
    roboMethod.testLifecycle = ReflectionHelpers.newInstance(cl);

    final Config config = roboMethod.config;
    final AndroidManifest appManifest = roboMethod.getAppManifest();

    roboMethod.parallelUniverseInterface.setSdkConfig((sdkEnvironment).getSdkConfig());
    roboMethod.parallelUniverseInterface.resetStaticState(config);

    SdkConfig sdkConfig = roboMethod.sdkConfig;
    Class<?> androidBuildVersionClass = (sdkEnvironment).bootstrappedClass(Build.VERSION.class);
    ReflectionHelpers.setStaticField(androidBuildVersionClass, "SDK_INT", sdkConfig.getApiLevel());
    ReflectionHelpers.setStaticField(androidBuildVersionClass, "RELEASE", sdkConfig.getAndroidVersion());
    ReflectionHelpers.setStaticField(androidBuildVersionClass, "CODENAME", sdkConfig.getAndroidCodeName());

    PackageResourceTable systemResourceTable = sdkEnvironment.getSystemResourceTable(getJarResolver());
    PackageResourceTable appResourceTable = getAppResourceTable(appManifest);

    roboMethod.parallelUniverseInterface.setUpApplicationState(bootstrappedMethod, roboMethod.testLifecycle, appManifest, config, new RoutingResourceTable(getCompiletimeSdkResourceTable(), appResourceTable), new RoutingResourceTable(systemResourceTable, appResourceTable), new RoutingResourceTable(systemResourceTable));
    roboMethod.testLifecycle.beforeTest(bootstrappedMethod);
  }

  @Override
  protected void afterTest(FrameworkMethod method, Method bootstrappedMethod) {
    RobolectricFrameworkMethod roboMethod = (RobolectricFrameworkMethod) method;

    try {
      roboMethod.parallelUniverseInterface.tearDownApplication();
    } finally {
      try {
        internalAfterTest(method, bootstrappedMethod);
      } finally {
        Config config = ((RobolectricFrameworkMethod) method).config;
        roboMethod.parallelUniverseInterface.resetStaticState(config); // afterward too, so stuff doesn't hold on to classes?
      }
    }
  }

  @Override
  protected void finallyAfterTest(FrameworkMethod method) {
    RobolectricFrameworkMethod roboMethod = (RobolectricFrameworkMethod) method;

    roboMethod.testLifecycle = null;
    roboMethod.parallelUniverseInterface = null;
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

    Class<?> buildConstants = config.constants();
    //noinspection ConstantConditions
    if (BuckManifestFactory.isBuck()) {
      return new BuckManifestFactory();
    } else if (buildConstants != null && buildConstants != Void.class) {
      return new GradleManifestFactory();
    } else {
      return new MavenManifestFactory();
    }
  }

  Properties getBuildSystemApiProperties() {
    InputStream resourceAsStream = getClass().getResourceAsStream("/com/android/tools/test_config.properties");
    if (resourceAsStream == null) {
      return null;
    }

    try {
      Properties properties = new Properties();
      properties.load(resourceAsStream);
      return properties;
    } catch (IOException e) {
      return null;
    } finally {
      try {
        resourceAsStream.close();
      } catch (IOException e) {
        throw new RuntimeException("couldn't close test_config.properties", e);
      }
    }
  }

  protected AndroidManifest getAppManifest(Config config) {
    ManifestFactory manifestFactory = getManifestFactory(config);
    ManifestIdentifier identifier = manifestFactory.identify(config);

    synchronized (appManifestsCache) {
      AndroidManifest appManifest;
      appManifest = appManifestsCache.get(identifier);
      if (appManifest == null) {
        appManifest = manifestFactory.create(identifier);
        appManifestsCache.put(identifier, appManifest);
      }

      return appManifest;
    }
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
   * @since 2.0
   */
  public Config getConfig(Method method) {
    return configMerger.getConfig(getTestClass().getJavaClass(), method, buildGlobalConfig());
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
   * @since 3.1.3
   */
  protected Config buildGlobalConfig() {
    return new Config.Builder().build();
  }

  @Override @Nonnull
  protected Class<?>[] getExtraShadows(FrameworkMethod frameworkMethod) {
    Config config = ((RobolectricFrameworkMethod) frameworkMethod).config;
    return config.shadows();
  }

  ParallelUniverseInterface getHooksInterface(SdkEnvironment sdkEnvironment) {
    ClassLoader robolectricClassLoader = sdkEnvironment.getRobolectricClassLoader();
    try {
      Class<?> clazz = robolectricClassLoader.loadClass(ParallelUniverse.class.getName());
      Class<? extends ParallelUniverseInterface> typedClazz = clazz.asSubclass(ParallelUniverseInterface.class);
      Constructor<? extends ParallelUniverseInterface> constructor = typedClazz.getConstructor();
      return constructor.newInstance();
    } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
      throw new RuntimeException(e);
    }
  }

  protected void internalAfterTest(FrameworkMethod frameworkMethod, Method method) {
    RobolectricFrameworkMethod roboMethod = (RobolectricFrameworkMethod) frameworkMethod;
    roboMethod.testLifecycle.afterTest(method);
  }

  @Override
  protected void afterClass() {
  }

  @Override
  public Object createTest() throws Exception {
    throw new UnsupportedOperationException("this should always be invoked on the HelperTestRunner!");
  }

  private PackageResourceTable getAppResourceTable(final AndroidManifest appManifest) {
    PackageResourceTable resourceTable = appResourceTableCache.get(appManifest);
    if (resourceTable == null) {
      resourceTable = new ResourceMerger().buildResourceTable(appManifest);

      appResourceTableCache.put(appManifest, resourceTable);
    }
    return resourceTable;
  }

  private static class MethodPassThrough extends Config.Implementation {
    private final FrameworkMethod method;

    private MethodPassThrough(FrameworkMethod method, int[] sdk, int minSdk, int maxSdk, String manifest, String qualifiers, String packageName, String abiSplit, String resourceDir, String assetDir, String buildDir, Class<?>[] shadows, String[] instrumentedPackages, Class<? extends Application> application, String[] libraries, Class<?> constants) {
      super(sdk, minSdk, maxSdk, manifest, qualifiers, packageName, abiSplit, resourceDir, assetDir, buildDir, shadows, instrumentedPackages, application, libraries, constants);
      this.method = method;
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
    protected Statement methodInvoker(FrameworkMethod method, Object test) {
      final Statement invoker = super.methodInvoker(method, test);
      final RobolectricFrameworkMethod roboMethod = (RobolectricFrameworkMethod) this.frameworkMethod;
      return new Statement() {
        @Override
        public void evaluate() throws Throwable {
          Thread orig = roboMethod.parallelUniverseInterface.getMainThread();
          roboMethod.parallelUniverseInterface.setMainThread(Thread.currentThread());
          try {
            invoker.evaluate();
          } finally {
            roboMethod.parallelUniverseInterface.setMainThread(orig);
          }
        }
      };
    }
  }

  static class RobolectricFrameworkMethod extends FrameworkMethod {
    private final @Nonnull AndroidManifest appManifest;
    final @Nonnull SdkConfig sdkConfig;
    final @Nonnull Config config;
    private boolean includeApiLevelInName = true;
    TestLifecycle testLifecycle;
    ParallelUniverseInterface parallelUniverseInterface;

    RobolectricFrameworkMethod(@Nonnull Method method, @Nonnull AndroidManifest appManifest, @Nonnull SdkConfig sdkConfig, @Nonnull Config config) {
      super(method);
      this.appManifest = appManifest;
      this.sdkConfig = sdkConfig;
      this.config = config;
    }

    @Override
    public String getName() {
      // IDE focused test runs rely on preservation of the test name; we'll use the
      //   latest supported SDK for focused test runs
      return super.getName() +
          (includeApiLevelInName ? "[" + sdkConfig.getApiLevel() + "]" : "");
    }

    void dontIncludeApiLevelInName() {
      includeApiLevelInName = false;
    }

    @Nonnull
    public AndroidManifest getAppManifest() {
      return appManifest;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      if (!super.equals(o)) return false;

      RobolectricFrameworkMethod that = (RobolectricFrameworkMethod) o;

      return sdkConfig.equals(that.sdkConfig);
    }

    @Override
    public int hashCode() {
      int result = super.hashCode();
      result = 31 * result + sdkConfig.hashCode();
      return result;
    }
  }
}
