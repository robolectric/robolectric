package org.robolectric;

import android.app.Application;
import android.os.Build;
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
import org.robolectric.shadows.ShadowLog;
import org.robolectric.util.AnnotationUtil;
import org.robolectric.util.DatabaseConfig.DatabaseMap;
import org.robolectric.util.DatabaseConfig.UsingDatabaseMap;
import org.robolectric.util.SQLiteMap;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
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

import static org.fest.reflect.core.Reflection.staticField;

/**
 * Installs a {@link org.robolectric.bytecode.InstrumentingClassLoader} and
 * {@link org.robolectric.res.ResourceLoader} in order to
 * provide a simulation of the Android runtime environment.
 */
public class RobolectricTestRunner extends BlockJUnit4ClassRunner {
  private static final MavenCentral MAVEN_CENTRAL = new MavenCentral();

  private static final Map<Class<? extends RobolectricTestRunner>, EnvHolder> envHoldersByTestRunner = new HashMap<Class<? extends RobolectricTestRunner>, EnvHolder>();
  private static final Map<AndroidManifest, ResourceLoader> resourceLoadersByAppManifest = new HashMap<AndroidManifest, ResourceLoader>();

  private static Class<? extends RobolectricTestRunner> lastTestRunnerClass;
  private static SdkConfig lastSdkConfig;
  private static SdkEnvironment lastSdkEnvironment;

  private static ShadowMap mainShadowMap;

  private final EnvHolder envHolder;
  private DatabaseMap databaseMap;
  private TestLifecycle<Application> testLifecycle;

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

  protected ClassHandler createClassHandler(ShadowMap shadowMap) {
    return new ShadowWrangler(shadowMap);
  }

  protected AndroidManifest createAppManifest(FsFile manifestFile) {
    if (!manifestFile.exists()) {
      System.out.print("WARNING: No manifest file found at " + manifestFile.getPath() + ".");
      System.out.println("Falling back to the Android OS resources only.");
      System.out.println("To remove this warning, annotate your test class with @Config(manifest=Config.NONE).");
      return null;
    }

    FsFile appBaseDir = manifestFile.getParent();
    return new AndroidManifest(manifestFile, appBaseDir.join("res"), appBaseDir.join("assets"));
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
      @Override public void evaluate() throws Throwable {
        final Config config = getConfig(method.getMethod());
        AndroidManifest appManifest = getAppManifest(config);
        SdkEnvironment sdkEnvironment = getEnvironment(appManifest, config);

        // todo: is this really needed?
        Thread.currentThread().setContextClassLoader(sdkEnvironment.getRobolectricClassLoader());

        Class bootstrappedTestClass = sdkEnvironment.bootstrappedClass(getTestClass().getJavaClass());
        HelperTestRunner helperTestRunner;
        try {
          helperTestRunner = new HelperTestRunner(bootstrappedTestClass);
        } catch (InitializationError initializationError) {
          throw new RuntimeException(initializationError);
        }

        final Method bootstrappedMethod;
        try {
          //noinspection unchecked
          bootstrappedMethod = bootstrappedTestClass.getMethod(method.getName());
        } catch (NoSuchMethodException e) {
          throw new RuntimeException(e);
        }

        configureShadows(sdkEnvironment, config);
        setupLogging();

        ParallelUniverseInterface parallelUniverseInterface = getHooksInterface(sdkEnvironment);
        try {
          assureTestLifecycle(sdkEnvironment);

          parallelUniverseInterface.resetStaticState();
          parallelUniverseInterface.setDatabaseMap(databaseMap); //Set static DatabaseMap in DBConfig

          boolean strictI18n = RobolectricTestRunner.determineI18nStrictState(bootstrappedMethod);

          int sdkVersion = pickReportedSdkVersion(config, appManifest);
          Class<?> versionClass = sdkEnvironment.bootstrappedClass(Build.VERSION.class);
          staticField("SDK_INT").ofType(int.class).in(versionClass).set(sdkVersion);

          ResourceLoader systemResourceLoader = sdkEnvironment.getSystemResourceLoader(MAVEN_CENTRAL, RobolectricTestRunner.this);
          setUpApplicationState(bootstrappedMethod, parallelUniverseInterface, strictI18n, systemResourceLoader, appManifest);
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

  private SdkEnvironment getEnvironment(final AndroidManifest appManifest, final Config config) {
    final SdkConfig sdkConfig = pickSdkVersion(appManifest, config);

    // keep the most recently-used SdkEnvironment strongly reachable to prevent thrashing in low-memory situations.
    if (getClass().equals(lastTestRunnerClass) && sdkConfig.equals(sdkConfig)) {
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
    if (config != null && config.emulateSdk() != -1) {
      throw new UnsupportedOperationException("Sorry, emulateSdk is not yet supported... coming soon!");
    }

    if (appManifest != null) {
      // todo: something smarter
      int useSdkVersion = appManifest.getTargetSdkVersion();
    }

    // right now we only have real jars for Ice Cream Sandwich aka 4.1 aka API 16
    return new SdkConfig("4.1.2_r1_rc");
  }

  protected AndroidManifest getAppManifest(Config config) {
    if (config.manifest().equals(Config.NONE)) {
      return null;
    }

    FsFile fsFile = Fs.currentDirectory();
    String manifestStr = config.manifest().equals(Config.DEFAULT) ? "AndroidManifest.xml" : config.manifest();
    FsFile manifestFile = fsFile.join(manifestStr);
    synchronized (envHolder) {
      AndroidManifest appManifest;
      appManifest = envHolder.appManifestsByFile.get(manifestFile);
      if (appManifest == null) {

        long startTime = System.currentTimeMillis();
        appManifest = createAppManifest(manifestFile);
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
        classHandler = createClassHandler(shadowMap);
      }
      sdkEnvironment.setCurrentClassHandler(classHandler);
    }
    return classHandler;
  }

  protected void setUpApplicationState(Method method, ParallelUniverseInterface parallelUniverseInterface, boolean strictI18n, ResourceLoader systemResourceLoader, AndroidManifest appManifest) {
    parallelUniverseInterface.setUpApplicationState(method, testLifecycle, strictI18n, systemResourceLoader, appManifest);
  }

  private int getTargetSdkVersion(AndroidManifest appManifest) {
    return getTargetVersionWhenAppManifestMightBeNullWhaaa(appManifest);
  }

  public static int getTargetVersionWhenAppManifestMightBeNullWhaaa(AndroidManifest appManifest) {
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
    try {
      @SuppressWarnings("unchecked")
      Class<ParallelUniverseInterface> aClass = (Class<ParallelUniverseInterface>) sdkEnvironment.getRobolectricClassLoader().loadClass(ParallelUniverse.class.getName());
      return aClass.newInstance();
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    } catch (InstantiationException e) {
      throw new RuntimeException(e);
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    }
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

  public static String determineResourceQualifiers(Method method) {
    String qualifiers = "";
    Config config = method.getAnnotation(Config.class);
    if (config != null) {
      qualifiers = config.qualifiers();
    }
    return qualifiers;
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

  public static ResourceLoader getAppResourceLoader(ResourceLoader systemResourceLoader, final AndroidManifest appManifest) {
    ResourceLoader resourceLoader = resourceLoadersByAppManifest.get(appManifest);
    if (resourceLoader == null) {
      resourceLoader = createAppResourceLoader(systemResourceLoader, appManifest);
      resourceLoadersByAppManifest.put(appManifest, resourceLoader);
    }
    return resourceLoader;
  }

  protected static ResourceLoader createAppResourceLoader(ResourceLoader systemResourceLoader, AndroidManifest appManifest) {
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

  public static PackageResourceLoader createResourceLoader(ResourcePath systemResourcePath) {
    return new PackageResourceLoader(systemResourcePath);
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


  private void setupLogging() {
    String logging = System.getProperty("robolectric.logging");
    if (logging != null && ShadowLog.stream == null) {
      PrintStream stream = null;
      if ("stdout".equalsIgnoreCase(logging)) {
        stream = System.out;
      } else if ("stderr".equalsIgnoreCase(logging)) {
        stream = System.err;
      } else {
        try {
          final PrintStream file = new PrintStream(new FileOutputStream(logging));
          stream = file;
          Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override public void run() {
              try {
                file.close();
              } catch (Exception ignored) {
              }
            }
          });
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
      ShadowLog.stream = stream;
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
