package org.robolectric;

import android.app.Application;
import android.os.Build;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.TestOnly;
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
import org.robolectric.annotation.Config;
import org.robolectric.internal.*;
import org.robolectric.internal.bytecode.*;
import org.robolectric.internal.dependency.*;
import org.robolectric.manifest.AndroidManifest;
import org.robolectric.res.*;
import org.robolectric.res.ResourceTable;
import org.robolectric.util.Logger;
import org.robolectric.util.ReflectionHelpers;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.security.SecureRandom;
import java.util.*;

/**
 * Installs a {@link org.robolectric.internal.bytecode.InstrumentingClassLoader} and
 * {@link ResourceProvider} in order to provide a simulation of the Android runtime environment.
 */
public class RobolectricTestRunner extends BlockJUnit4ClassRunner {

  public static final String CONFIG_PROPERTIES = "robolectric.properties";
  private static final Map<AndroidManifest, ResourceTable> appResourceTableCache = new HashMap<>();
  private static final Map<ManifestIdentifier, AndroidManifest> appManifestsCache = new HashMap<>();
  private static ResourceTable compiletimeSdkResourceTable;

  private final SdkPicker sdkPicker;
  private final ConfigMerger configMerger;

  private TestLifecycle<Application> testLifecycle;
  private DependencyResolver dependencyResolver;

  static {
    new SecureRandom(); // this starts up the Poller SunPKCS11-Darwin thread early, outside of any Robolectric classloader
  }

  private final HashSet<Class<?>> loadedTestClasses = new HashSet<>();

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

  @SuppressWarnings("unchecked")
  private void assureTestLifecycle(SdkEnvironment sdkEnvironment) {
    try {
      ClassLoader robolectricClassLoader = sdkEnvironment.getRobolectricClassLoader();
      testLifecycle = (TestLifecycle) robolectricClassLoader.loadClass(getTestLifecycleClass().getName()).newInstance();
    } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  protected DependencyResolver getJarResolver() {
    if (dependencyResolver == null) {
      if (Boolean.getBoolean("robolectric.offline")) {
        String dependencyDir = System.getProperty("robolectric.dependency.dir", ".");
        dependencyResolver = new LocalDependencyResolver(new File(dependencyDir));
      } else {
        File cacheDir = new File(new File(System.getProperty("java.io.tmpdir")), "robolectric");

        if (cacheDir.exists() || cacheDir.mkdir()) {
          Logger.info("Dependency cache location: %s", cacheDir.getAbsolutePath());
          dependencyResolver = new CachedDependencyResolver(new MavenDependencyResolver(), cacheDir, 60 * 60 * 24 * 1000);
        } else {
          dependencyResolver = new MavenDependencyResolver();
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
   * @param sdkConfig the {@link SdkConfig} in effect for this test
   * @return an appropriate {@link ClassHandler}. This implementation returns a {@link ShadowWrangler}.
   * @since 2.3
   */
  @NotNull
  protected ClassHandler createClassHandler(ShadowMap shadowMap, SdkConfig sdkConfig) {
    return new ShadowWrangler(shadowMap, sdkConfig.getApiLevel());
  }

  /**
   * Create a {@link ConfigMerger} for calculating the {@link Config} tests.
   *
   * Custom TestRunner subclasses may wish to override this method to provide alternate configuration.
   *
   * @return an {@link ConfigMerger}.
   * @since 3.2
   */
  @NotNull
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
  @NotNull
  protected SdkPicker createSdkPicker() {
    return new SdkPicker();
  }

  /**
   * Create an {@link InstrumentationConfiguration} suitable for the provided {@link Config}.
   *
   * Custom TestRunner subclasses may wish to override this method to provide alternate configuration.
   *
   * @param config the merged configuration for the test that's about to run
   * @return an {@link InstrumentationConfiguration}
   */
  @NotNull
  public InstrumentationConfiguration createClassLoaderConfig(Config config) {
    return InstrumentationConfiguration.newBuilder().withConfig(config).build();
  }

  /**
   * An instance of the returned class will be created for each test invocation.
   *
   * Custom TestRunner subclasses may wish to override this method to provide alternate configuration.
   *
   * @return a class which implements {@link TestLifecycle}. This implementation returns a {@link DefaultTestLifecycle}.
   */
  @NotNull
  protected Class<? extends TestLifecycle> getTestLifecycleClass() {
    return DefaultTestLifecycle.class;
  }

  public static void injectEnvironment(ClassLoader robolectricClassLoader,
      ClassHandler classHandler, ShadowInvalidator invalidator) {
    String className = RobolectricInternals.class.getName();
    Class<?> robolectricInternalsClass = ReflectionHelpers.loadClass(robolectricClassLoader, className);
    ReflectionHelpers.setStaticField(robolectricInternalsClass, "classHandler", classHandler);
    ReflectionHelpers.setStaticField(robolectricInternalsClass, "shadowInvalidator", invalidator);
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

  @Override
  protected List<FrameworkMethod> getChildren() {
    List<FrameworkMethod> children = new ArrayList<>();
    for (FrameworkMethod frameworkMethod : super.getChildren()) {
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
    }
    return children;
  }

  private static void invokeAfterClass(final Class<?> clazz) throws Throwable {
    final TestClass testClass = new TestClass(clazz);
    final List<FrameworkMethod> afters = testClass.getAnnotatedMethods(AfterClass.class);
    for (FrameworkMethod after : afters) {
      after.invokeExplosively(null);
    }
  }

  @Override
  protected void runChild(FrameworkMethod method, RunNotifier notifier) {
    RobolectricFrameworkMethod roboMethod = (RobolectricFrameworkMethod) method;
    Description description = describeChild(method);
    EachTestNotifier eachNotifier = new EachTestNotifier(notifier, description);

    Config config = roboMethod.config;
    if (shouldIgnore(method, config)) {
      eachNotifier.fireTestIgnored();
    } else {
      eachNotifier.fireTestStarted();
      try {
        SdkConfig sdkConfig = ((RobolectricFrameworkMethod) method).sdkConfig;
        InstrumentingClassLoaderFactory instrumentingClassLoaderFactory = new InstrumentingClassLoaderFactory(createClassLoaderConfig(config), getJarResolver());
        SdkEnvironment sdkEnvironment = instrumentingClassLoaderFactory.getSdkEnvironment(sdkConfig);
        methodBlock(method, config, roboMethod.getAppManifest(), sdkEnvironment).evaluate();
      } catch (AssumptionViolatedException e) {
        eachNotifier.addFailedAssumption(e);
      } catch (Throwable e) {
        eachNotifier.addFailure(e);
      } finally {
        eachNotifier.fireTestFinished();
      }
    }
  }

  /**
   * Returns the ResourceProvider for the compile time SDK.
   */
  @NotNull
  private static ResourceTable getCompiletimeSdkResourceTable() {
    if (compiletimeSdkResourceTable == null) {
      String androidPackage = "android";
      PackageResourceIndex resourceIndex = new PackageResourceIndex(androidPackage);
      ResourceExtractor.populate(resourceIndex, android.R.class);
      compiletimeSdkResourceTable = new ResourceTable(resourceIndex);
    }
    return compiletimeSdkResourceTable;
  }

  protected boolean shouldIgnore(FrameworkMethod method, Config config) {
    return method.getAnnotation(Ignore.class) != null;
  }

  private ParallelUniverseInterface parallelUniverseInterface;

  Statement methodBlock(final FrameworkMethod method, final Config config, final AndroidManifest appManifest, final SdkEnvironment sdkEnvironment) {
    return new Statement() {
      @Override
      public void evaluate() throws Throwable {
        // Configure shadows *BEFORE* setting the ClassLoader. This is necessary because
        // creating the ShadowMap loads all ShadowProviders via ServiceLoader and this is
        // not available once we install the Robolectric class loader.
        configureShadows(sdkEnvironment, config);

        Thread.currentThread().setContextClassLoader(sdkEnvironment.getRobolectricClassLoader());

        Class bootstrappedTestClass = sdkEnvironment.bootstrappedClass(getTestClass().getJavaClass());
        HelperTestRunner helperTestRunner = getHelperTestRunner(bootstrappedTestClass);

        final Method bootstrappedMethod;
        try {
          //noinspection unchecked
          bootstrappedMethod = bootstrappedTestClass.getMethod(method.getMethod().getName());
        } catch (NoSuchMethodException e) {
          throw new RuntimeException(e);
        }

        parallelUniverseInterface = getHooksInterface(sdkEnvironment);
        try {
          try {
            // Only invoke @BeforeClass once per class
            if (!loadedTestClasses.contains(bootstrappedTestClass)) {
              invokeBeforeClass(bootstrappedTestClass);
            }
            assureTestLifecycle(sdkEnvironment);

            parallelUniverseInterface.setSdkConfig(sdkEnvironment.getSdkConfig());
            parallelUniverseInterface.resetStaticState(config);

            SdkConfig sdkConfig = ((RobolectricFrameworkMethod) method).sdkConfig;
            Class<?> androidBuildVersionClass = sdkEnvironment.bootstrappedClass(Build.VERSION.class);
            ReflectionHelpers.setStaticField(androidBuildVersionClass, "SDK_INT", sdkConfig.getApiLevel());
            ReflectionHelpers.setStaticField(androidBuildVersionClass, "RELEASE", sdkConfig.getAndroidVersion());

            ResourceTable systemResourceTable = sdkEnvironment.getSystemResourceTable(getJarResolver());
            ResourceTable appResourceTable = getAppResourceTable(appManifest);

            parallelUniverseInterface.setUpApplicationState(bootstrappedMethod, testLifecycle, appManifest, config, new RoutingResourceProvider(getCompiletimeSdkResourceTable(), appResourceTable), new RoutingResourceProvider(systemResourceTable, appResourceTable), new RoutingResourceProvider(systemResourceTable));
            testLifecycle.beforeTest(bootstrappedMethod);
          } catch (Exception e) {
            throw new RuntimeException(e);
          }

          final Statement statement = helperTestRunner.methodBlock(new FrameworkMethod(bootstrappedMethod));

          // todo: this try/finally probably isn't right -- should mimic RunAfters? [xw]
          try {
            statement.evaluate();
          } finally {
            try {
              parallelUniverseInterface.tearDownApplication();
            } finally {
              try {
                internalAfterTest(bootstrappedMethod);
              } finally {
                parallelUniverseInterface.resetStaticState(config); // afterward too, so stuff doesn't hold on to classes?
              }
            }
          }
        } finally {
          Thread.currentThread().setContextClassLoader(RobolectricTestRunner.class.getClassLoader());
          parallelUniverseInterface = null;
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

  protected HelperTestRunner getHelperTestRunner(Class bootstrappedTestClass) {
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
    Class<?> buildConstants = config.constants();
    //noinspection ConstantConditions
    if (buildConstants != null && buildConstants != Void.class) {
      return new GradleManifestFactory();
    } else {
      return new MavenManifestFactory();
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
   * The returned object is likely to be reused for many tests.
   *
   * Custom TestRunner subclasses may wish to override this method to provide
   * alternate configuration. Consider calling <code>super.buildGlobalConfig()</code>
   * and overriding values as needed using a
   * {@link Config.org.robolectric.annotation.Config.Builder}.
   *
   * The default implementation has appropriate values for most use cases.
   *
   * @return global {@link Config} object
   * @since 3.1.3
   */
  protected Config buildGlobalConfig() {
    return Config.Builder.defaults().build();
  }

  protected void configureShadows(SdkEnvironment sdkEnvironment, Config config) {
    ShadowMap shadowMap = createShadowMap();

    if (config != null) {
      Class<?>[] shadows = config.shadows();
      if (shadows.length > 0) {
        shadowMap = shadowMap.newBuilder().addShadowClasses(shadows).build();
      }
    }

    sdkEnvironment.setShadowMap(shadowMap);

    ClassHandler classHandler = createClassHandler(shadowMap, sdkEnvironment.getSdkConfig());
    injectEnvironment(sdkEnvironment.getRobolectricClassLoader(), classHandler, sdkEnvironment.getShadowInvalidator());
  }

  ParallelUniverseInterface getHooksInterface(SdkEnvironment sdkEnvironment) {
    ClassLoader robolectricClassLoader = sdkEnvironment.getRobolectricClassLoader();
    try {
      Class<?> clazz = robolectricClassLoader.loadClass(ParallelUniverse.class.getName());
      Class<? extends ParallelUniverseInterface> typedClazz = clazz.asSubclass(ParallelUniverseInterface.class);
      Constructor<? extends ParallelUniverseInterface> constructor = typedClazz.getConstructor(SdkEnvironment.class);
      return constructor.newInstance(sdkEnvironment);
    } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
      throw new RuntimeException(e);
    }
  }

  public void internalAfterTest(final Method method) {
    testLifecycle.afterTest(method);
  }

  private void afterClass() {
    testLifecycle = null;
  }

  @TestOnly
  boolean allStateIsCleared() {
    return testLifecycle == null;
  }

  @Override
  public Object createTest() throws Exception {
    throw new UnsupportedOperationException("this should always be invoked on the HelperTestRunner!");
  }

  private final ResourceTable getAppResourceTable(final AndroidManifest appManifest) {
    ResourceTable resourceTable = appResourceTableCache.get(appManifest);
    if (resourceTable == null) {
      resourceTable = ResourceMerger.buildResourceTable(appManifest);

      appResourceTableCache.put(appManifest, resourceTable);
    }
    return resourceTable;
  }

  protected ShadowMap createShadowMap() {
    return ShadowMap.EMPTY;
  }

  public class HelperTestRunner extends BlockJUnit4ClassRunner {
    public HelperTestRunner(Class<?> testClass) throws InitializationError {
      super(testClass);
    }

    @Override protected Object createTest() throws Exception {
      Object test = super.createTest();
      testLifecycle.prepareTest(test);
      return test;
    }

    @Override public Statement classBlock(RunNotifier notifier) {
      return super.classBlock(notifier);
    }

    @Override public Statement methodBlock(FrameworkMethod method) {
      return super.methodBlock(method);
    }

    @Override
    protected Statement methodInvoker(FrameworkMethod method, Object test) {
      final Statement invoker = super.methodInvoker(method, test);
      return new Statement() {
        @Override
        public void evaluate() throws Throwable {
          Thread orig = parallelUniverseInterface.getMainThread();
          parallelUniverseInterface.setMainThread(Thread.currentThread());
          try {
            invoker.evaluate();
          } finally {
            parallelUniverseInterface.setMainThread(orig);
          }
        }
      };
    }
  }

  class RobolectricFrameworkMethod extends FrameworkMethod {
    private final AndroidManifest appManifest;
    final SdkConfig sdkConfig;
    final Config config;
    private boolean includeApiLevelInName = true;

    RobolectricFrameworkMethod(Method method, AndroidManifest appManifest, SdkConfig sdkConfig, Config config) {
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

    public AndroidManifest getAppManifest() {
      return appManifest;
    }
  }
}