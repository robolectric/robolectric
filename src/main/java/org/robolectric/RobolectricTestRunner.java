package org.robolectric;

import android.app.Application;
import android.os.Build;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.apache.maven.artifact.ant.DependenciesTask;
import org.jetbrains.annotations.TestOnly;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.DisableStrictI18n;
import org.robolectric.annotation.EnableStrictI18n;
import org.robolectric.annotation.WithConstantInt;
import org.robolectric.annotation.WithConstantString;
import org.robolectric.bytecode.AsmInstrumentingClassLoader;
import org.robolectric.bytecode.ClassHandler;
import org.robolectric.bytecode.RobolectricInternals;
import org.robolectric.bytecode.Setup;
import org.robolectric.bytecode.ShadowMap;
import org.robolectric.bytecode.ShadowWrangler;
import org.robolectric.internal.ParallelUniverse;
import org.robolectric.internal.ParallelUniverseInterface;
import org.robolectric.res.DocumentLoader;
import org.robolectric.res.Fs;
import org.robolectric.res.FsFile;
import org.robolectric.res.OverlayResourceLoader;
import org.robolectric.res.PackageResourceLoader;
import org.robolectric.res.ResourceLoader;
import org.robolectric.res.ResourcePath;
import org.robolectric.res.RoutingResourceLoader;
import org.robolectric.util.AnnotationUtil;
import org.robolectric.util.DatabaseConfig.DatabaseMap;
import org.robolectric.util.DatabaseConfig.UsingDatabaseMap;
import org.robolectric.util.Pair;
import org.robolectric.util.SQLiteMap;

import static org.fest.reflect.core.Reflection.constructor;
import static org.fest.reflect.core.Reflection.staticField;
import static org.fest.reflect.core.Reflection.type;

/**
 * Installs a {@link org.robolectric.bytecode.InstrumentingClassLoader} and
 * {@link org.robolectric.res.ResourceLoader} in order to
 * provide a simulation of the Android runtime environment.
 */
public class RobolectricTestRunner extends BlockJUnit4ClassRunner {
  private static final MavenCentral MAVEN_CENTRAL = new MavenCentral();
  private static final Map<Class<? extends RobolectricTestRunner>, EnvHolder> envHoldersByTestRunner = new HashMap<Class<? extends RobolectricTestRunner>, EnvHolder>();
  private static Map<Pair<AndroidManifest, SdkConfig>, ResourceLoader> resourceLoadersByManifestAndConfig = new HashMap<Pair<AndroidManifest, SdkConfig>, ResourceLoader>();
  private static ShadowMap mainShadowMap;
  private final EnvHolder envHolder;
  private DatabaseMap databaseMap;
  private TestLifecycle<Application> testLifecycle;

  static {
    new SecureRandom(); // this starts up the Poller SunPKCS11-Darwin thread early, outside of any Robolectric classloader
  }

  private Class<? extends RobolectricTestRunner> lastTestRunnerClass;
  private SdkConfig lastSdkConfig;
  private SdkEnvironment lastSdkEnvironment;

  /**
   * Creates a runner to run {@code testClass}. Looks in your working directory for your AndroidManifest.xml file
   * and res directory by default. Use the {@link Config} annotation to configure.
   *
   * @param testClass the test class to be run
   * @throws InitializationError if junit says so
   */
  public RobolectricTestRunner(final Class<?> testClass) throws InitializationError {
    super(testClass);

    EnvHolder envHolder;
    synchronized (envHoldersByTestRunner) {
      Class<? extends RobolectricTestRunner> testRunnerClass = getClass();
      envHolder = envHoldersByTestRunner.get(testRunnerClass);
      if (envHolder == null) {
        envHolder = new EnvHolder();
        envHoldersByTestRunner.put(testRunnerClass, envHolder);
      }
    }
    this.envHolder = envHolder;
    databaseMap = setupDatabaseMap(testClass, new SQLiteMap());
  }

  private void assureTestLifecycle(SdkEnvironment sdkEnvironment) {
    try {
      ClassLoader robolectricClassLoader = sdkEnvironment.getRobolectricClassLoader();
      testLifecycle = (TestLifecycle) robolectricClassLoader.loadClass(getTestLifecycleClass().getName()).newInstance();
    } catch (InstantiationException e) {
      throw new RuntimeException(e);
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  public SdkEnvironment createSdkEnvironment(SdkConfig sdkConfig) {
    Setup setup = createSetup();
    ClassLoader robolectricClassLoader = createRobolectricClassLoader(setup, sdkConfig);
    return new SdkEnvironment(sdkConfig, robolectricClassLoader);
  }

  protected ClassHandler createClassHandler(ShadowMap shadowMap, SdkConfig sdkConfig) {
    return new ShadowWrangler(shadowMap, sdkConfig);
  }

  protected AndroidManifest createAppManifest(FsFile manifestFile, FsFile resDir, FsFile assetsDir) {
    if (!manifestFile.exists()) {
      System.out.print("WARNING: No manifest file found at " + manifestFile.getPath() + ".");
      System.out.println("Falling back to the Android OS resources only.");
      System.out.println("To remove this warning, annotate your test class with @Config(manifest=Config.NONE).");
      return null;
    }
    return new AndroidManifest(manifestFile, resDir, assetsDir);
  }

  public Setup createSetup() {
    return new Setup();
  }

  protected Class<? extends TestLifecycle> getTestLifecycleClass() {
    return DefaultTestLifecycle.class;
  }

  protected ClassLoader createRobolectricClassLoader(Setup setup, SdkConfig sdkConfig) {
    URL[] urls = MAVEN_CENTRAL.getLocalArtifactUrls(this, sdkConfig.getSdkClasspathDependencies()).values().toArray(new URL[0]);
    return new AsmInstrumentingClassLoader(setup, urls);
  }

  public static void injectClassHandler(ClassLoader robolectricClassLoader, ClassHandler classHandler) {
    try {
      String className = RobolectricInternals.class.getName();
      Class<?> robolectricInternalsClass = robolectricClassLoader.loadClass(className);
      Field field = robolectricInternalsClass.getDeclaredField("classHandler");
      field.setAccessible(true);
      field.set(null, classHandler);
    } catch (NoSuchFieldException e) {
      throw new RuntimeException(e);
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  @SuppressWarnings("UnusedParameters")
  protected void configureMaven(DependenciesTask dependenciesTask) {
    // maybe you want to override this method and some settings?
  }

  @Override
  protected Statement classBlock(RunNotifier notifier) {
    final Statement statement = super.classBlock(notifier);
    return new Statement() {
      @Override
      public void evaluate() throws Throwable {
        try {
          statement.evaluate();
        } finally {
          afterClass();
        }
      }
    };
  }

  @Override protected Statement methodBlock(final FrameworkMethod method) {
    return new Statement() {
      @Override
      public void evaluate() throws Throwable {
        final Config config = getConfig(method.getMethod());
        AndroidManifest appManifest = getAppManifest(config);
        SdkEnvironment sdkEnvironment = getEnvironment(appManifest, config);
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

        configureShadows(sdkEnvironment, config);

        ParallelUniverseInterface parallelUniverseInterface = getHooksInterface(sdkEnvironment);
        try {
          assureTestLifecycle(sdkEnvironment);

          parallelUniverseInterface.resetStaticState();
          parallelUniverseInterface.setDatabaseMap(databaseMap); //Set static DatabaseMap in DBConfig
          parallelUniverseInterface.setSdkConfig(sdkEnvironment.getSdkConfig());

          boolean strictI18n = determineI18nStrictState(bootstrappedMethod);

          int sdkVersion = pickReportedSdkVersion(config, appManifest);
          Class<?> versionClass = sdkEnvironment.bootstrappedClass(Build.VERSION.class);
          staticField("SDK_INT").ofType(int.class).in(versionClass).set(sdkVersion);

          ResourceLoader systemResourceLoader = sdkEnvironment.getSystemResourceLoader(MAVEN_CENTRAL, RobolectricTestRunner.this);
          setUpApplicationState(bootstrappedMethod, parallelUniverseInterface, strictI18n, systemResourceLoader, appManifest, config);
          testLifecycle.beforeTest(bootstrappedMethod);
        } catch (Exception e) {
          e.printStackTrace();
          throw new RuntimeException(e);
        }

        final Statement statement = helperTestRunner.methodBlock(new FrameworkMethod(bootstrappedMethod));

        Map<Field, Object> withConstantAnnos = getWithConstantAnnotations(bootstrappedMethod);

        // todo: this try/finally probably isn't right -- should mimic RunAfters? [xw]
        try {
          if (withConstantAnnos.isEmpty()) {
            statement.evaluate();
          } else {
            synchronized (this) {
              setupConstants(withConstantAnnos);
              statement.evaluate();
              setupConstants(withConstantAnnos);
            }
          }
        } finally {
          try {
            parallelUniverseInterface.tearDownApplication();
          } finally {
            try {
              internalAfterTest(bootstrappedMethod);
            } finally {
              parallelUniverseInterface.resetStaticState(); // afterward too, so stuff doesn't hold on to classes?
              // todo: is this really needed?
              Thread.currentThread().setContextClassLoader(RobolectricTestRunner.class.getClassLoader());
            }
          }
        }
      }
    };
  }

  protected HelperTestRunner getHelperTestRunner(Class bootstrappedTestClass) {
    try {
      return new HelperTestRunner(bootstrappedTestClass);
    } catch (InitializationError initializationError) {
      throw new RuntimeException(initializationError);
    }
  }

  private SdkEnvironment getEnvironment(final AndroidManifest appManifest, final Config config) {
    final SdkConfig sdkConfig = pickSdkVersion(appManifest, config);

    // keep the most recently-used SdkEnvironment strongly reachable to prevent thrashing in low-memory situations.
    if (getClass().equals(lastTestRunnerClass) && sdkConfig.equals(lastSdkConfig)) {
      return lastSdkEnvironment;
    }

    lastTestRunnerClass = null;
    lastSdkConfig = null;
    lastSdkEnvironment = envHolder.getSdkEnvironment(sdkConfig, new SdkEnvironment.Factory() {
      @Override public SdkEnvironment create() {
        return createSdkEnvironment(sdkConfig);
      }
    });
    lastTestRunnerClass = getClass();
    lastSdkConfig = sdkConfig;
    return lastSdkEnvironment;
  }

  protected SdkConfig pickSdkVersion(AndroidManifest appManifest, Config config) {
    if (config != null) {
      return new SdkConfig(config.emulateSdk());
    } else {
      if (appManifest != null) {
        return new SdkConfig(appManifest.getTargetSdkVersion());
      } else {
        return SdkConfig.getDefaultSdk();
      }
    }
  }

  protected AndroidManifest getAppManifest(Config config) {
    if (config.manifest().equals(Config.NONE)) {
      return null;
    }

    String manifestProperty = System.getProperty("android.manifest");
    String resourcesProperty = System.getProperty("android.resources");
    String assetsProperty = System.getProperty("android.assets");

    FsFile fsFile = Fs.currentDirectory();
    FsFile manifestFile;
    FsFile resDir;
    FsFile assetsDir;

    boolean defaultManifest = config.manifest().equals(Config.DEFAULT);
    if (defaultManifest && manifestProperty != null) {
      manifestFile = Fs.fileFromPath(manifestProperty);
      resDir = Fs.fileFromPath(resourcesProperty);
      assetsDir = Fs.fileFromPath(assetsProperty);
    } else {
      manifestFile = fsFile.join(defaultManifest ? "AndroidManifest.xml" : config.manifest());
      resDir = manifestFile.getParent().join("res");
      assetsDir = manifestFile.getParent().join("assets");
    }

    synchronized (envHolder) {
      AndroidManifest appManifest;
      appManifest = envHolder.appManifestsByFile.get(manifestFile);
      if (appManifest == null) {
        long startTime = System.currentTimeMillis();
        appManifest = createAppManifest(manifestFile, resDir, assetsDir);
        if (DocumentLoader.DEBUG_PERF)
          System.out.println(String.format("%4dms spent in %s", System.currentTimeMillis() - startTime, manifestFile));

        envHolder.appManifestsByFile.put(manifestFile, appManifest);
      }
      return appManifest;
    }
  }

  public Config getConfig(Method method) {
    Config config = AnnotationUtil.defaultsFor(Config.class);

    Config globalConfig = Config.Implementation.fromProperties(getConfigProperties());
    if (globalConfig != null) {
      config = new Config.Implementation(config, globalConfig);
    }

    Config classConfig = method.getDeclaringClass().getAnnotation(Config.class);
    if (classConfig != null) {
      config = new Config.Implementation(config, classConfig);
    }

    Config methodConfig = method.getAnnotation(Config.class);
    if (methodConfig != null) {
      config = new Config.Implementation(config, methodConfig);
    }

    return config;
  }

  protected Properties getConfigProperties() {
    ClassLoader classLoader = getTestClass().getClass().getClassLoader();
    InputStream resourceAsStream = classLoader.getResourceAsStream("org.robolectric.Config.properties");
    if (resourceAsStream == null) return null;
    Properties properties = new Properties();
    try {
      properties.load(resourceAsStream);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return properties;
  }

  protected void configureShadows(SdkEnvironment sdkEnvironment, Config config) {
    ShadowMap shadowMap = createShadowMap();

    if (config != null) {
      Class<?>[] shadows = config.shadows();
      if (shadows.length > 0) {
        shadowMap = shadowMap.newBuilder()
            .addShadowClasses(shadows)
            .build();
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
      sdkEnvironment.setCurrentClassHandler(classHandler);
    }
    return classHandler;
  }

  protected void setUpApplicationState(Method method, ParallelUniverseInterface parallelUniverseInterface, boolean strictI18n, ResourceLoader systemResourceLoader, AndroidManifest appManifest, Config config) {
    parallelUniverseInterface.setUpApplicationState(method, testLifecycle, strictI18n, systemResourceLoader, appManifest, config);
  }

  private int getTargetSdkVersion(AndroidManifest appManifest) {
    return getTargetVersionWhenAppManifestMightBeNullWhaaa(appManifest);
  }

  public int getTargetVersionWhenAppManifestMightBeNullWhaaa(AndroidManifest appManifest) {
    return appManifest == null // app manifest would be null for libraries
        ? Build.VERSION_CODES.ICE_CREAM_SANDWICH // todo: how should we be picking this?
        : appManifest.getTargetSdkVersion();
  }

  protected int pickReportedSdkVersion(Config config, AndroidManifest appManifest) {
    if (config != null && config.reportSdk() != -1) {
      return config.reportSdk();
    } else {
      return getTargetSdkVersion(appManifest);
    }
  }

  private ParallelUniverseInterface getHooksInterface(SdkEnvironment sdkEnvironment) {
    ClassLoader robolectricClassLoader = sdkEnvironment.getRobolectricClassLoader();
    Class<? extends ParallelUniverseInterface> parallelUniverseClass =
        type(ParallelUniverse.class.getName())
            .withClassLoader(robolectricClassLoader)
            .loadAs(ParallelUniverseInterface.class);

    return constructor()
        .withParameterTypes(RobolectricTestRunner.class)
        .in(parallelUniverseClass)
        .newInstance(this);
  }

  public void internalAfterTest(final Method method) {
    testLifecycle.afterTest(method);
  }

  private void afterClass() {
    testLifecycle = null;
    databaseMap = null;
  }

  @TestOnly
  boolean allStateIsCleared() {
    return testLifecycle == null && databaseMap == null;
  }

  @Override
  public Object createTest() throws Exception {
    throw new UnsupportedOperationException("this should always be invoked on the HelperTestRunner!");
  }

  /**
   * Sets Robolectric config to determine if Robolectric should blacklist API calls that are not
   * I18N/L10N-safe.
   * <p/>
   * I18n-strict mode affects suitably annotated shadow methods. Robolectric will throw exceptions
   * if these methods are invoked by application code. Additionally, Robolectric's ResourceLoader
   * will throw exceptions if layout resources use bare string literals instead of string resource IDs.
   * <p/>
   * To enable or disable i18n-strict mode for specific test cases, annotate them with
   * {@link org.robolectric.annotation.EnableStrictI18n} or
   * {@link org.robolectric.annotation.DisableStrictI18n}.
   * <p/>
   * <p/>
   * By default, I18n-strict mode is disabled.
   *
   * @param method
   */
  public static boolean determineI18nStrictState(Method method) {
    // Global
    boolean strictI18n = globalI18nStrictEnabled();

    // Test case class
    Class<?> testClass = method.getDeclaringClass();
    if (testClass.getAnnotation(EnableStrictI18n.class) != null) {
      strictI18n = true;
    } else if (testClass.getAnnotation(DisableStrictI18n.class) != null) {
      strictI18n = false;
    }

    // Test case method
    if (method.getAnnotation(EnableStrictI18n.class) != null) {
      strictI18n = true;
    } else if (method.getAnnotation(DisableStrictI18n.class) != null) {
      strictI18n = false;
    }

    return strictI18n;
  }

  /**
   * Default implementation of global switch for i18n-strict mode.
   * To enable i18n-strict mode globally, set the system property
   * "robolectric.strictI18n" to true. This can be done via java
   * system properties in either Ant or Maven.
   * <p/>
   * Subclasses can override this method and establish their own policy
   * for enabling i18n-strict mode.
   *
   * @return
   */
  protected static boolean globalI18nStrictEnabled() {
    return Boolean.valueOf(System.getProperty("robolectric.strictI18n"));
  }

  /**
   * Find all the class and method annotations and pass them to
   * addConstantFromAnnotation() for evaluation.
   * <p/>
   * TODO: Add compound annotations to support defining more than one int and string at a time
   * TODO: See http://stackoverflow.com/questions/1554112/multiple-annotations-of-the-same-type-on-one-element
   *
   * @param method
   * @return
   */
  private Map<Field, Object> getWithConstantAnnotations(Method method) {
    Map<Field, Object> constants = new HashMap<Field, Object>();

    for (Annotation anno : method.getDeclaringClass().getAnnotations()) {
      addConstantFromAnnotation(constants, anno);
    }

    for (Annotation anno : method.getAnnotations()) {
      addConstantFromAnnotation(constants, anno);
    }

    return constants;
  }


  /**
   * If the annotation is a constant redefinition, add it to the provided hash
   *
   * @param constants
   * @param anno
   */
  private void addConstantFromAnnotation(Map<Field, Object> constants, Annotation anno) {
    try {
      String name = anno.annotationType().getName();
      Object newValue = null;

      if (name.equals(WithConstantString.class.getName())) {
        newValue = anno.annotationType().getMethod("newValue").invoke(anno);
      } else if (name.equals(WithConstantInt.class.getName())) {
        newValue = anno.annotationType().getMethod("newValue").invoke(anno);
      } else {
        return;
      }

      @SuppressWarnings("rawtypes")
      Class classWithField = (Class) anno.annotationType().getMethod("classWithField").invoke(anno);
      String fieldName = (String) anno.annotationType().getMethod("fieldName").invoke(anno);
      Field field = classWithField.getDeclaredField(fieldName);
      constants.put(field, newValue);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Defines static finals from the provided hash and stores the old values back
   * into the hash.
   * <p/>
   * Call it twice with the same hash, and it puts everything back the way it was originally.
   *
   * @param constants
   */
  private void setupConstants(Map<Field, Object> constants) {
    for (Field field : constants.keySet()) {
      Object newValue = constants.get(field);
      Object oldValue = Robolectric.Reflection.setFinalStaticField(field, newValue);
      constants.put(field, oldValue);
    }
  }

  public final ResourceLoader getAppResourceLoader(SdkConfig sdkConfig, ResourceLoader systemResourceLoader, final AndroidManifest appManifest) {
    Pair<AndroidManifest, SdkConfig> androidManifestSdkConfigPair = new Pair<AndroidManifest, SdkConfig>(appManifest, sdkConfig);
    ResourceLoader resourceLoader = resourceLoadersByManifestAndConfig.get(androidManifestSdkConfigPair);
    if (resourceLoader == null) {
      resourceLoader = createAppResourceLoader(systemResourceLoader, appManifest);
      resourceLoadersByManifestAndConfig.put(androidManifestSdkConfigPair, resourceLoader);
    }
    return resourceLoader;
  }

  protected ResourceLoader createAppResourceLoader(ResourceLoader systemResourceLoader, AndroidManifest appManifest) {
    List<PackageResourceLoader> appAndLibraryResourceLoaders = new ArrayList<PackageResourceLoader>();
    for (ResourcePath resourcePath : appManifest.getIncludedResourcePaths()) {
      appAndLibraryResourceLoaders.add(createResourceLoader(resourcePath));
    }
    OverlayResourceLoader overlayResourceLoader = new OverlayResourceLoader(appManifest.getPackageName(), appAndLibraryResourceLoaders);

    Map<String, ResourceLoader> resourceLoaders = new HashMap<String, ResourceLoader>();
    resourceLoaders.put("android", systemResourceLoader);
    resourceLoaders.put(appManifest.getPackageName(), overlayResourceLoader);
    return new RoutingResourceLoader(resourceLoaders);
  }

  public PackageResourceLoader createResourceLoader(ResourcePath resourcePath) {
    return new PackageResourceLoader(resourcePath);
  }

  /*
   * Specifies what database to use for testing (ex: H2 or Sqlite),
   * this will load H2 by default, the SQLite TestRunner version will override this.
   */
  protected DatabaseMap setupDatabaseMap(Class<?> testClass, DatabaseMap map) {
    DatabaseMap dbMap = map;

    if (testClass.isAnnotationPresent(UsingDatabaseMap.class)) {
      UsingDatabaseMap usingMap = testClass.getAnnotation(UsingDatabaseMap.class);
      if (usingMap.value() != null) {
        dbMap = Robolectric.newInstanceOf(usingMap.value());
      } else {
        if (dbMap == null)
          throw new RuntimeException("UsingDatabaseMap annotation value must provide a class implementing DatabaseMap");
      }
    }
    return dbMap;
  }

  protected ShadowMap createShadowMap() {
    synchronized (RobolectricTestRunner.class) {
      if (mainShadowMap != null) return mainShadowMap;

      mainShadowMap = new ShadowMap.Builder()
          //.addShadowClasses(RobolectricBase.DEFAULT_SHADOW_CLASSES)
          .build();
      //mainShadowMap = new ShadowMap.Builder()
      //        .addShadowClasses(RobolectricBase.DEFAULT_SHADOW_CLASSES)
      //        .build();
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
  }
}
