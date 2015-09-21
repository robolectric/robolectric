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
import org.robolectric.annotation.*;
import org.robolectric.internal.InstrumentingClassLoaderFactory;
import org.robolectric.internal.bytecode.*;
import org.robolectric.internal.dependency.CachedDependencyResolver;
import org.robolectric.internal.dependency.DependencyResolver;
import org.robolectric.internal.dependency.LocalDependencyResolver;
import org.robolectric.internal.dependency.MavenDependencyResolver;
import org.robolectric.internal.ParallelUniverse;
import org.robolectric.internal.ParallelUniverseInterface;
import org.robolectric.internal.SdkConfig;
import org.robolectric.internal.SdkEnvironment;
import org.robolectric.manifest.AndroidManifest;
import org.robolectric.res.Fs;
import org.robolectric.res.FsFile;
import org.robolectric.res.OverlayResourceLoader;
import org.robolectric.res.PackageResourceLoader;
import org.robolectric.res.ResourceLoader;
import org.robolectric.res.ResourcePath;
import org.robolectric.res.RoutingResourceLoader;
import org.robolectric.util.Logger;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.Pair;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.security.SecureRandom;
import java.util.*;

/**
 * Installs a {@link org.robolectric.internal.bytecode.InstrumentingClassLoader} and
 * {@link org.robolectric.res.ResourceLoader} in order to provide a simulation of the Android runtime environment.
 */
public class RobolectricTestRunner extends BlockJUnit4ClassRunner {
  private static final String CONFIG_PROPERTIES = "robolectric.properties";
  private static final Config DEFAULT_CONFIG = new Config.Implementation(defaultsFor(Config.class));
  private static final Map<Pair<AndroidManifest, SdkConfig>, ResourceLoader> resourceLoadersByManifestAndConfig = new HashMap<>();
  private static final Map<ManifestIdentifier, AndroidManifest> appManifestsByFile = new HashMap<>();
  private static ShadowMap mainShadowMap;
  private InstrumentingClassLoaderFactory instrumentingClassLoaderFactory;
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
        cacheDir.mkdir();

        if (cacheDir.exists()) {
          Logger.info("Dependency cache location: %s", cacheDir.getAbsolutePath());
          dependencyResolver = new CachedDependencyResolver(new MavenDependencyResolver(), cacheDir, 60 * 60 * 24 * 1000);
        } else {
          dependencyResolver = new MavenDependencyResolver();
        }
      }
    }

    return dependencyResolver;
  }

  protected ClassHandler createClassHandler(ShadowMap shadowMap, SdkConfig sdkConfig) {
    return new ShadowWrangler(shadowMap);
  }

  protected AndroidManifest createAppManifest(FsFile manifestFile, FsFile resDir, FsFile assetDir, String packageName) {
    if (!manifestFile.exists()) {
      System.out.print("WARNING: No manifest file found at " + manifestFile.getPath() + ".");
      System.out.println("Falling back to the Android OS resources only.");
      System.out.println("To remove this warning, annotate your test class with @Config(manifest=Config.NONE).");
      return null;
    }

    Logger.debug("Robolectric assets directory: " + assetDir.getPath());
    Logger.debug("   Robolectric res directory: " + resDir.getPath());
    Logger.debug("   Robolectric manifest path: " + manifestFile.getPath());
    Logger.debug("    Robolectric package name: " + packageName);
    return new AndroidManifest(manifestFile, resDir, assetDir, packageName);
  }

  public InstrumentationConfiguration createClassLoaderConfig() {
    return InstrumentationConfiguration.newBuilder().build();
  }

  protected Class<? extends TestLifecycle> getTestLifecycleClass() {
    return DefaultTestLifecycle.class;
  }

  public static void injectClassHandler(ClassLoader robolectricClassLoader, ClassHandler classHandler) {
    String className = RobolectricInternals.class.getName();
    Class<?> robolectricInternalsClass = ReflectionHelpers.loadClass(robolectricClassLoader, className);
    ReflectionHelpers.setStaticField(robolectricInternalsClass, "classHandler", classHandler);
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

  private static void invokeAfterClass(final Class<?> clazz) throws Throwable {
    final TestClass testClass = new TestClass(clazz);
    final List<FrameworkMethod> afters = testClass.getAnnotatedMethods(AfterClass.class);
    for (FrameworkMethod after : afters) {
      after.invokeExplosively(null);
    }
  }

  @Override
  protected void runChild(FrameworkMethod method, RunNotifier notifier) {
    Description description = describeChild(method);
    EachTestNotifier eachNotifier = new EachTestNotifier(notifier, description);

    final Config config = getConfig(method.getMethod());
    if (shouldIgnore(method, config)) {
      eachNotifier.fireTestIgnored();
    } else if(shouldRunApiVersion(config)) {
      eachNotifier.fireTestStarted();
      try {
        AndroidManifest appManifest = getAppManifest(config);
        if (instrumentingClassLoaderFactory == null) {
          instrumentingClassLoaderFactory = new InstrumentingClassLoaderFactory(createClassLoaderConfig(), getJarResolver());
        }
        SdkEnvironment sdkEnvironment = instrumentingClassLoaderFactory.getSdkEnvironment(new SdkConfig(pickSdkVersion(config, appManifest)));
        methodBlock(method, config, appManifest, sdkEnvironment).evaluate();
      } catch (AssumptionViolatedException e) {
        eachNotifier.addFailedAssumption(e);
      } catch (Throwable e) {
        eachNotifier.addFailure(e);
      } finally {
        eachNotifier.fireTestFinished();
      }
    }
  }

  protected boolean shouldRunApiVersion(Config config) {
    return true;
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
          bootstrappedMethod = bootstrappedTestClass.getMethod(method.getName());
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

            parallelUniverseInterface.resetStaticState(config);
            parallelUniverseInterface.setSdkConfig(sdkEnvironment.getSdkConfig());

            int sdkVersion = pickSdkVersion(config, appManifest);
            ReflectionHelpers.setStaticField(sdkEnvironment.bootstrappedClass(Build.VERSION.class), "SDK_INT", sdkVersion);

            ResourceLoader systemResourceLoader = sdkEnvironment.getSystemResourceLoader(getJarResolver());
            setUpApplicationState(bootstrappedMethod, parallelUniverseInterface, systemResourceLoader, appManifest, config);
            testLifecycle.beforeTest(bootstrappedMethod);
          } catch (Exception e) {
            e.printStackTrace();
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
                // todo: is this really needed?
                Thread.currentThread().setContextClassLoader(RobolectricTestRunner.class.getClassLoader());
              }
            }
          }
        } finally {
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

  protected AndroidManifest getAppManifest(Config config) {
    if (config.manifest().equals(Config.NONE)) {
      return null;
    }

    String manifestProperty = System.getProperty("android.manifest");
    String resourcesProperty = System.getProperty("android.resources");
    String assetsProperty = System.getProperty("android.assets");
    String packageName = System.getProperty("android.package");

    FsFile baseDir;
    FsFile manifestFile;
    FsFile resDir;
    FsFile assetDir;

    boolean defaultManifest = config.manifest().equals(Config.DEFAULT);
    if (defaultManifest && manifestProperty != null) {
      manifestFile = Fs.fileFromPath(manifestProperty);
      baseDir = manifestFile.getParent();
    } else {
      manifestFile = getBaseDir().join(defaultManifest ? AndroidManifest.DEFAULT_MANIFEST_NAME : config.manifest());
      baseDir = manifestFile.getParent();
    }

    boolean defaultRes = Config.DEFAULT_RES_FOLDER.equals(config.resourceDir());
    if (defaultRes && resourcesProperty != null) {
      resDir = Fs.fileFromPath(resourcesProperty);
    } else {
      resDir = baseDir.join(config.resourceDir());
    }

    boolean defaultAssets = Config.DEFAULT_ASSET_FOLDER.equals(config.assetDir());
    if (defaultAssets && assetsProperty != null) {
      assetDir = Fs.fileFromPath(assetsProperty);
    } else {
      assetDir = baseDir.join(config.assetDir());
    }

    String configPackageName = config.packageName();
    if (configPackageName != null && !configPackageName.isEmpty()) {
      packageName = configPackageName;
    }

    List<FsFile> libraryDirs = null;
    if (config.libraries().length > 0) {
      libraryDirs = new ArrayList<>();
      for (String libraryDirName : config.libraries()) {
        libraryDirs.add(baseDir.join(libraryDirName));
      }
    }

    ManifestIdentifier identifier = new ManifestIdentifier(manifestFile, resDir, assetDir, packageName, libraryDirs);
    synchronized (appManifestsByFile) {
      AndroidManifest appManifest;
      appManifest = appManifestsByFile.get(identifier);
      if (appManifest == null) {
        appManifest = createAppManifest(manifestFile, resDir, assetDir, packageName);
        if (libraryDirs != null) {
          appManifest.setLibraryDirectories(libraryDirs);
        }
        appManifestsByFile.put(identifier, appManifest);
      }
      return appManifest;
    }
  }

  protected FsFile getBaseDir() {
    return Fs.currentDirectory();
  }

  public Config getConfig(Method method) {
    Config config = DEFAULT_CONFIG;

    Config globalConfig = Config.Implementation.fromProperties(getConfigProperties());
    if (globalConfig != null) {
      config = new Config.Implementation(config, globalConfig);
    }

    Config methodClassConfig = method.getDeclaringClass().getAnnotation(Config.class);
    if (methodClassConfig != null) {
      config = new Config.Implementation(config, methodClassConfig);
    }

    ArrayList<Class> testClassHierarchy = new ArrayList<>();
    Class testClass = getTestClass().getJavaClass();

    while (testClass != null) {
      testClassHierarchy.add(0, testClass);
      testClass = testClass.getSuperclass();
    }

    for (Class clazz : testClassHierarchy) {
      Config classConfig = (Config) clazz.getAnnotation(Config.class);
      if (classConfig != null) {
        config = new Config.Implementation(config, classConfig);
      }
    }

    Config methodConfig = method.getAnnotation(Config.class);
    if (methodConfig != null) {
      config = new Config.Implementation(config, methodConfig);
    }

    return config;
  }

  protected Properties getConfigProperties() {
    ClassLoader classLoader = getClass().getClassLoader();
    try (InputStream resourceAsStream = classLoader.getResourceAsStream(CONFIG_PROPERTIES)) {
      if (resourceAsStream == null) return null;
      Properties properties = new Properties();
      properties.load(resourceAsStream);
      return properties;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  protected void configureShadows(SdkEnvironment sdkEnvironment, Config config) {
    ShadowMap shadowMap = createShadowMap();

    if (config != null) {
      Class<?>[] shadows = config.shadows();
      if (shadows.length > 0) {
        shadowMap = shadowMap.newBuilder().addShadowClasses(shadows).build();
      }
    }

    ClassHandler classHandler = getClassHandler(sdkEnvironment, shadowMap);
    injectClassHandler(sdkEnvironment.getRobolectricClassLoader(), classHandler);
  }

  private ClassHandler getClassHandler(SdkEnvironment sdkEnvironment, ShadowMap shadowMap) {
    ClassHandler classHandler;
    synchronized (sdkEnvironment) {
      classHandler = sdkEnvironment.classHandlersByShadowMap.get(shadowMap);
      if (classHandler == null) {
        classHandler = createClassHandler(shadowMap, sdkEnvironment.getSdkConfig());
      }
    }
    return classHandler;
  }

  protected void setUpApplicationState(Method method, ParallelUniverseInterface parallelUniverseInterface, ResourceLoader systemResourceLoader, AndroidManifest appManifest, Config config) {
    parallelUniverseInterface.setUpApplicationState(method, testLifecycle, systemResourceLoader, appManifest, config);
  }

  protected int pickSdkVersion(Config config, AndroidManifest manifest) {
    if (config != null && config.sdk().length > 1) {
      throw new IllegalArgumentException("RobolectricTestRunner does not support multiple values for @Config.sdk");
    } else if (config != null && config.sdk().length == 1) {
      return config.sdk()[0];
    } else if (manifest != null) {
      return manifest.getTargetSdkVersion();
    } else {
      return SdkConfig.FALLBACK_SDK_VERSION;
    }
  }

  private ParallelUniverseInterface getHooksInterface(SdkEnvironment sdkEnvironment) {
    ClassLoader robolectricClassLoader = sdkEnvironment.getRobolectricClassLoader();
    try {
      Class<?> clazz = robolectricClassLoader.loadClass(ParallelUniverse.class.getName());
      Class<? extends ParallelUniverseInterface> typedClazz = clazz.asSubclass(ParallelUniverseInterface.class);
      Constructor<? extends ParallelUniverseInterface> constructor = typedClazz.getConstructor(RobolectricTestRunner.class);
      return constructor.newInstance(this);
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

  public final ResourceLoader getAppResourceLoader(SdkConfig sdkConfig, ResourceLoader systemResourceLoader, final AndroidManifest appManifest) {
    Pair<AndroidManifest, SdkConfig> androidManifestSdkConfigPair = new Pair<>(appManifest, sdkConfig);
    ResourceLoader resourceLoader = resourceLoadersByManifestAndConfig.get(androidManifestSdkConfigPair);
    if (resourceLoader == null) {
      resourceLoader = createAppResourceLoader(systemResourceLoader, appManifest);
      resourceLoadersByManifestAndConfig.put(androidManifestSdkConfigPair, resourceLoader);
    }
    return resourceLoader;
  }

  protected ResourceLoader createAppResourceLoader(ResourceLoader systemResourceLoader, AndroidManifest appManifest) {
    List<PackageResourceLoader> appAndLibraryResourceLoaders = new ArrayList<>();
    for (ResourcePath resourcePath : appManifest.getIncludedResourcePaths()) {
      appAndLibraryResourceLoaders.add(createResourceLoader(resourcePath));
    }
    OverlayResourceLoader overlayResourceLoader = new OverlayResourceLoader(appManifest.getPackageName(), appAndLibraryResourceLoaders);

    Map<String, ResourceLoader> resourceLoaders = new HashMap<>();
    resourceLoaders.put("android", systemResourceLoader);
    resourceLoaders.put(appManifest.getPackageName(), overlayResourceLoader);
    return new RoutingResourceLoader(resourceLoaders);
  }

  public PackageResourceLoader createResourceLoader(ResourcePath resourcePath) {
    return new PackageResourceLoader(resourcePath);
  }

  protected ShadowMap createShadowMap() {
    synchronized (RobolectricTestRunner.class) {
      if (mainShadowMap != null) return mainShadowMap;
      mainShadowMap = new ShadowMap.Builder().build();
      return mainShadowMap;
    }
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

  private static class ManifestIdentifier {
    private final FsFile manifestFile;
    private final FsFile resDir;
    private final FsFile assetDir;
    private final String packageName;
    private final List<FsFile> libraryDirs;

    public ManifestIdentifier(FsFile manifestFile, FsFile resDir, FsFile assetDir, String packageName,
        List<FsFile> libraryDirs) {
      this.manifestFile = manifestFile;
      this.resDir = resDir;
      this.assetDir = assetDir;
      this.packageName = packageName;
      this.libraryDirs = libraryDirs != null ? libraryDirs : Collections.<FsFile>emptyList();
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      ManifestIdentifier that = (ManifestIdentifier) o;

      return assetDir.equals(that.assetDir)
          && libraryDirs.equals(that.libraryDirs)
          && manifestFile.equals(that.manifestFile)
          && resDir.equals(that.resDir)
          && ((packageName == null && that.packageName == null) || (packageName != null && packageName.equals(that.packageName)));
    }

    @Override
    public int hashCode() {
      int result = manifestFile.hashCode();
      result = 31 * result + resDir.hashCode();
      result = 31 * result + assetDir.hashCode();
      result = 31 * result + (packageName == null ? 0 : packageName.hashCode());
      result = 31 * result + libraryDirs.hashCode();
      return result;
    }
  }

  private static <A extends Annotation> A defaultsFor(Class<A> annotation) {
    return annotation.cast(
        Proxy.newProxyInstance(annotation.getClassLoader(), new Class[] { annotation },
            new InvocationHandler() {
              public Object invoke(Object proxy, @NotNull Method method, Object[] args)
                  throws Throwable {
                return method.getDefaultValue();
              }
            }));
  }
}
