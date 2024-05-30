package org.robolectric.android.internal;

import static android.os.Build.VERSION_CODES.P;
import static android.os.Build.VERSION_CODES.Q;
import static org.robolectric.shadow.api.Shadow.newInstanceOf;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.annotation.SuppressLint;
import android.app.ActivityThread;
import android.app.AppCompatCallbacks;
import android.app.Application;
import android.app.Instrumentation;
import android.app.LoadedApk;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageParser;
import android.content.pm.PackageParser.Package;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.FontsContract;
import android.util.DisplayMetrics;
import androidx.test.platform.app.InstrumentationRegistry;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import java.lang.reflect.Method;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Locale;
import javax.annotation.Nonnull;
import javax.inject.Named;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.conscrypt.OkHostnameVerifier;
import org.conscrypt.OpenSSLProvider;
import org.robolectric.ApkLoader;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.android.Bootstrap;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.ConscryptMode;
import org.robolectric.annotation.GraphicsMode;
import org.robolectric.annotation.LooperMode;
import org.robolectric.annotation.SQLiteMode;
import org.robolectric.annotation.experimental.LazyApplication.LazyLoad;
import org.robolectric.config.ConfigurationRegistry;
import org.robolectric.internal.ResourcesMode;
import org.robolectric.internal.ShadowProvider;
import org.robolectric.internal.TestEnvironment;
import org.robolectric.manifest.AndroidManifest;
import org.robolectric.manifest.BroadcastReceiverData;
import org.robolectric.manifest.RoboNotFoundException;
import org.robolectric.nativeruntime.DefaultNativeRuntimeLoader;
import org.robolectric.pluginapi.Sdk;
import org.robolectric.pluginapi.TestEnvironmentLifecyclePlugin;
import org.robolectric.pluginapi.config.ConfigurationStrategy.Configuration;
import org.robolectric.res.Fs;
import org.robolectric.res.PackageResourceTable;
import org.robolectric.res.ResourcePath;
import org.robolectric.res.ResourceTable;
import org.robolectric.res.ResourceTableFactory;
import org.robolectric.res.RoutingResourceTable;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.shadows.ClassNameResolver;
import org.robolectric.shadows.ShadowActivityThread;
import org.robolectric.shadows.ShadowActivityThread._ActivityThread_;
import org.robolectric.shadows.ShadowActivityThread._AppBindData_;
import org.robolectric.shadows.ShadowApplication;
import org.robolectric.shadows.ShadowContextImpl._ContextImpl_;
import org.robolectric.shadows.ShadowInstrumentation;
import org.robolectric.shadows.ShadowInstrumentation._Instrumentation_;
import org.robolectric.shadows.ShadowLegacyLooper;
import org.robolectric.shadows.ShadowLoadedApk._LoadedApk_;
import org.robolectric.shadows.ShadowLog;
import org.robolectric.shadows.ShadowLooper;
import org.robolectric.shadows.ShadowPackageManager;
import org.robolectric.shadows.ShadowPackageParser;
import org.robolectric.shadows.ShadowPausedLooper;
import org.robolectric.shadows.ShadowView;
import org.robolectric.util.Logger;
import org.robolectric.util.PerfStatsCollector;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;
import org.robolectric.util.Scheduler;
import org.robolectric.util.TempDirectory;
import org.robolectric.versioning.AndroidVersions;
import org.robolectric.versioning.AndroidVersions.V;

@SuppressLint("NewApi")
public class AndroidTestEnvironment implements TestEnvironment {

  private static final String CONSCRYPT_PROVIDER = "Conscrypt";
  private static final int MAX_DATA_DIR_NAME_LENGTH = 120;

  private final Sdk runtimeSdk;
  private final Sdk compileSdk;

  private final int apiLevel;

  private boolean loggingInitialized = false;
  private final Path sdkJarPath;
  private final ApkLoader apkLoader;
  private PackageResourceTable systemResourceTable;
  private final ShadowProvider[] shadowProviders;
  private final TestEnvironmentLifecyclePlugin[] testEnvironmentLifecyclePlugins;
  private final Locale initialLocale = Locale.getDefault();

  public AndroidTestEnvironment(
      @Named("runtimeSdk") Sdk runtimeSdk,
      @Named("compileSdk") Sdk compileSdk,
      ResourcesMode resourcesMode,
      ApkLoader apkLoader,
      ShadowProvider[] shadowProviders,
      TestEnvironmentLifecyclePlugin[] lifecyclePlugins) {
    this.runtimeSdk = runtimeSdk;
    this.compileSdk = compileSdk;

    apiLevel = runtimeSdk.getApiLevel();
    this.apkLoader = apkLoader;
    sdkJarPath = runtimeSdk.getJarPath();
    this.shadowProviders = shadowProviders;
    this.testEnvironmentLifecyclePlugins = lifecyclePlugins;

    ReflectionHelpers.setStaticField(RuntimeEnvironment.class, "apiLevel", apiLevel);
  }

  @Override
  public void setUpApplicationState(
      Method method, Configuration configuration, AndroidManifest appManifest) {
    Config config = configuration.get(Config.class);

    ConfigurationRegistry.instance = new ConfigurationRegistry(configuration.map());

    for (TestEnvironmentLifecyclePlugin e : testEnvironmentLifecyclePlugins) {
      e.onSetupApplicationState();
    }

    clearEnvironment();

    // Starting in Android V and above, the native runtime does not support begin lazy-loaded, it
    // must be loaded upfront.
    if (shouldLoadNativeRuntime() && RuntimeEnvironment.getApiLevel() >= V.SDK_INT) {
      DefaultNativeRuntimeLoader.injectAndLoad();
    }

    RuntimeEnvironment.setTempDirectory(new TempDirectory(createTestDataDirRootPath(method)));
    if (ShadowLooper.looperMode() == LooperMode.Mode.LEGACY) {
      RuntimeEnvironment.setMasterScheduler(new Scheduler());
      RuntimeEnvironment.setMainThread(Thread.currentThread());
      ShadowLegacyLooper.internalInitializeBackgroundThreadScheduler();
    }

    if (!loggingInitialized) {
      ShadowLog.setupLogging();
      loggingInitialized = true;
    }

    ConscryptMode.Mode conscryptMode = configuration.get(ConscryptMode.Mode.class);
    Security.removeProvider(CONSCRYPT_PROVIDER);
    if (conscryptMode != ConscryptMode.Mode.OFF) {

      Security.removeProvider(BouncyCastleProvider.PROVIDER_NAME);
      if (Security.getProvider(CONSCRYPT_PROVIDER) == null) {
        Security.insertProviderAt(new OpenSSLProvider(), 1);
      }

      HttpsURLConnection.setDefaultHostnameVerifier(
          new HostnameVerifier() {
            private final OkHostnameVerifier conscryptVerifier = OkHostnameVerifier.INSTANCE;

            @Override
            public boolean verify(String hostname, SSLSession session) {
              try {
                Certificate[] certificates = session.getPeerCertificates();
                X509Certificate[] x509Certificates =
                    Arrays.copyOf(certificates, certificates.length, X509Certificate[].class);
                return conscryptVerifier.verify(x509Certificates, hostname, session);
              } catch (SSLException e) {
                return false;
              }
            }
          });
    }

    if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
      Security.addProvider(new BouncyCastleProvider());
    }

    android.content.res.Configuration androidConfiguration =
        new android.content.res.Configuration();
    DisplayMetrics displayMetrics = new DisplayMetrics();

    Bootstrap.applyQualifiers(config.qualifiers(), apiLevel, androidConfiguration, displayMetrics);

    androidConfiguration.fontScale = config.fontScale();

    if (ShadowView.useRealGraphics()) {
      Bitmap.setDefaultDensity(displayMetrics.densityDpi);
    }
    Locale locale =
        apiLevel >= VERSION_CODES.N
            ? androidConfiguration.getLocales().get(0)
            : androidConfiguration.locale;
    Locale.setDefault(locale);

    if (ShadowLooper.looperMode() == LooperMode.Mode.LEGACY) {
      if (Looper.myLooper() == null) {
        Looper.prepareMainLooper();
      }
      ShadowLooper.getShadowMainLooper().resetScheduler();
    } else {
      ShadowPausedLooper.resetLoopers();
      RuntimeEnvironment.setMasterScheduler(new LooperDelegatingScheduler(Looper.getMainLooper()));
    }

    preloadClasses(apiLevel);

    RuntimeEnvironment.setAndroidFrameworkJarPath(sdkJarPath);
    Bootstrap.setDisplayConfiguration(androidConfiguration, displayMetrics);

    Instrumentation instrumentation = createInstrumentation();
    InstrumentationRegistry.registerInstance(instrumentation, new Bundle());
    Supplier<Application> applicationSupplier = createApplicationSupplier(appManifest, config);
    RuntimeEnvironment.setApplicationSupplier(applicationSupplier);

    if (configuration.get(LazyLoad.class) == LazyLoad.ON) {
      RuntimeEnvironment.setConfiguredApplicationClass(
          getApplicationClass(appManifest, config, new ApplicationInfo()));
    } else {
      // force eager load of the application
      RuntimeEnvironment.getApplication();
    }

  }

  // If certain Android classes are required to be loaded in a particular order, do so here.
  // Android's Zygote has a class preloading mechanism, and there have been obscure crashes caused
  // by Android bugs requiring a specific initialization order.
  private void preloadClasses(int apiLevel) {
    if (apiLevel >= Q) {
      // Preload URI to avoid a static initializer cycle that can be caused by using Uri.Builder
      // before Uri.EMPTY.
      try {
        Class.forName("android.net.Uri", true, this.getClass().getClassLoader());
      } catch (ClassNotFoundException e) {
        throw new RuntimeException(e);
      }
    }
  }

  // TODO Move synchronization logic into its own class for better readability
  private Supplier<Application> createApplicationSupplier(
      AndroidManifest appManifest, Config config) {
    final ActivityThread activityThread = (ActivityThread) RuntimeEnvironment.getActivityThread();
    final _ActivityThread_ _activityThread_ = reflector(_ActivityThread_.class, activityThread);
    final ShadowActivityThread shadowActivityThread = Shadow.extract(activityThread);

    return Suppliers.memoize(
        () ->
            PerfStatsCollector.getInstance()
                .measure(
                    "installAndCreateApplication",
                    () ->
                        installAndCreateApplication(
                            appManifest,
                            config,
                            shadowActivityThread,
                            _activityThread_,
                            activityThread.getInstrumentation())));
  }

  private Application installAndCreateApplication(
      AndroidManifest appManifest,
      Config config,
      ShadowActivityThread shadowActivityThread,
      _ActivityThread_ activityThreadReflector,
      Instrumentation androidInstrumentation) {
    ActivityThread activityThread = (ActivityThread) RuntimeEnvironment.getActivityThread();

    Context systemContextImpl = reflector(_ContextImpl_.class).createSystemContext(activityThread);
    RuntimeEnvironment.systemContext = systemContextImpl;

    Application dummyInitialApplication = new Application();
    activityThreadReflector.setInitialApplication(dummyInitialApplication);
    ShadowApplication shadowInitialApplication = Shadow.extract(dummyInitialApplication);
    shadowInitialApplication.callAttach(systemContextImpl);

    Package parsedPackage = loadAppPackage(config, appManifest);

    ApplicationInfo applicationInfo = parsedPackage.applicationInfo;
    Class<? extends Application> applicationClass =
        getApplicationClass(appManifest, config, applicationInfo);
    applicationInfo.className = applicationClass.getName();

    ComponentName actualComponentName =
        new ComponentName(
            applicationInfo.packageName, androidInstrumentation.getClass().getSimpleName());
    ReflectionHelpers.setField(androidInstrumentation, "mComponent", actualComponentName);

    // unclear why, but prior to P the processName wasn't set
    if (apiLevel < P && applicationInfo.processName == null) {
      applicationInfo.processName = parsedPackage.packageName;
    }

    setUpPackageStorage(applicationInfo, parsedPackage);

    // Bit of a hack... Context.createPackageContext() is called before the application is created.
    // It calls through
    // to ActivityThread for the package which in turn calls the PackageManagerService directly.
    // This works for now
    // but it might be nicer to have ShadowPackageManager implementation move into the service as
    // there is also lots of
    // code in there that can be reusable, e.g: the XxxxIntentResolver code.
    ShadowActivityThread.setApplicationInfo(applicationInfo);

    // Bootstrap.getConfiguration gets any potential updates to configuration via
    // RuntimeEnvironment.setQualifiers.
    android.content.res.Configuration androidConfiguration = Bootstrap.getConfiguration();
    shadowActivityThread.setCompatConfiguration(androidConfiguration);

    Bootstrap.setUpDisplay();
    activityThread.applyConfigurationToResources(androidConfiguration);

    Application application = ReflectionHelpers.callConstructor(applicationClass);
    RuntimeEnvironment.setConfiguredApplicationClass(applicationClass);

    RuntimeEnvironment.application = application;

    if (application != null) {
      final Class<?> appBindDataClass;
      try {
        appBindDataClass = Class.forName("android.app.ActivityThread$AppBindData");
      } catch (ClassNotFoundException e) {
        throw new RuntimeException(e);
      }
      final Object appBindData = ReflectionHelpers.callConstructor(appBindDataClass);
      final _AppBindData_ _appBindData_ = reflector(_AppBindData_.class, appBindData);
      _appBindData_.setProcessName(parsedPackage.packageName);
      _appBindData_.setAppInfo(applicationInfo);
      activityThreadReflector.setBoundApplication(appBindData);

      final LoadedApk loadedApk =
          activityThread.getPackageInfo(applicationInfo, null, Context.CONTEXT_INCLUDE_CODE);
      final _LoadedApk_ _loadedApk_ = reflector(_LoadedApk_.class, loadedApk);

      Context contextImpl =
          reflector(_ContextImpl_.class).createAppContext(activityThread, loadedApk);
      ShadowPackageManager shadowPackageManager = Shadow.extract(contextImpl.getPackageManager());
      shadowPackageManager.addPackageInternal(parsedPackage);
      activityThreadReflector.setInitialApplication(application);
      ShadowApplication shadowApplication = Shadow.extract(application);
      shadowApplication.callAttach(contextImpl);
      reflector(_ContextImpl_.class, contextImpl).setOuterContext(application);
      if (apiLevel >= VERSION_CODES.O) {
        reflector(_ContextImpl_.class, contextImpl)
            .setClassLoader(this.getClass().getClassLoader());
      }

      Resources appResources = application.getResources();
      _loadedApk_.setResources(appResources);
      _loadedApk_.setApplication(application);
      if (RuntimeEnvironment.getApiLevel() >= VERSION_CODES.O) {
        // Preload fonts resources
        FontsContract.setApplicationContextForResources(application);
      }
      registerBroadcastReceivers(application, appManifest, loadedApk);

      appResources.updateConfiguration(androidConfiguration, Bootstrap.getDisplayMetrics());

      // Circumvent the 'No Compatibility callbacks set!' log. See #8509
      if (apiLevel >= AndroidVersions.V.SDK_INT) {
        // Adds loggableChanges parameter.
        ReflectionHelpers.callStaticMethod(
            AppCompatCallbacks.class,
            "install",
            ClassParameter.from(long[].class, new long[0]),
            ClassParameter.from(long[].class, new long[0]));
      } else if (apiLevel >= AndroidVersions.R.SDK_INT) {
        // Invoke the previous version.
        ReflectionHelpers.callStaticMethod(
            AppCompatCallbacks.class, "install", ClassParameter.from(long[].class, new long[0]));
      }

      PerfStatsCollector.getInstance()
          .measure(
              "application onCreate()",
              () -> androidInstrumentation.callApplicationOnCreate(application));
    }

    return application;
  }

  private Package loadAppPackage(Config config, AndroidManifest appManifest) {
    return PerfStatsCollector.getInstance()
        .measure("parse package", () -> loadAppPackage_measured(config, appManifest));
  }

  private Package loadAppPackage_measured(Config config, AndroidManifest appManifest) {

    Package parsedPackage;

    RuntimeEnvironment.compileTimeSystemResourcesFile = compileSdk.getJarPath();

    Path packageFile = appManifest.getApkFile();
    if (packageFile != null) {
      parsedPackage = ShadowPackageParser.callParsePackage(packageFile);
    } else {
      parsedPackage = new Package("org.robolectric.default");
      parsedPackage.applicationInfo.targetSdkVersion = appManifest.getTargetSdkVersion();
    }

    if (parsedPackage != null
        && parsedPackage.applicationInfo != null
        && RuntimeEnvironment.getApiLevel() >= P) {
      parsedPackage.applicationInfo.appComponentFactory = appManifest.getAppComponentFactory();
    }
    return parsedPackage;
  }

  private synchronized PackageResourceTable getSystemResourceTable() {
    if (systemResourceTable == null) {
      ResourcePath resourcePath = createRuntimeSdkResourcePath();
      systemResourceTable = new ResourceTableFactory().newFrameworkResourceTable(resourcePath);
    }
    return systemResourceTable;
  }

  @Nonnull
  private ResourcePath createRuntimeSdkResourcePath() {
    try {
      FileSystem zipFs = Fs.forJar(runtimeSdk.getJarPath());

      @SuppressLint("PrivateApi")
      Class<?> androidInternalRClass = Class.forName("com.android.internal.R");

      // TODO: verify these can be loaded via raw-res path
      return new ResourcePath(
          android.R.class,
          zipFs.getPath("raw-res/res"),
          zipFs.getPath("raw-res/assets"),
          androidInternalRClass);
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  private void injectResourceStuffForLegacy(AndroidManifest appManifest) {
    PackageResourceTable systemResourceTable = getSystemResourceTable();
    PackageResourceTable appResourceTable = apkLoader.getAppResourceTable(appManifest);
    RoutingResourceTable combinedAppResourceTable =
        new RoutingResourceTable(appResourceTable, systemResourceTable);

    PackageResourceTable compileTimeSdkResourceTable = apkLoader.getCompileTimeSdkResourceTable();
    ResourceTable combinedCompileTimeResourceTable =
        new RoutingResourceTable(appResourceTable, compileTimeSdkResourceTable);

    RuntimeEnvironment.setCompileTimeResourceTable(combinedCompileTimeResourceTable);
    RuntimeEnvironment.setAppResourceTable(combinedAppResourceTable);
    RuntimeEnvironment.setSystemResourceTable(new RoutingResourceTable(systemResourceTable));

    try {
      appManifest.initMetaData(combinedAppResourceTable);
    } catch (RoboNotFoundException e1) {
      throw new Resources.NotFoundException(e1.getMessage());
    }
  }

  private void populateAssetPaths(AssetManager assetManager, AndroidManifest appManifest) {
    for (AndroidManifest manifest : appManifest.getAllManifests()) {
      if (manifest.getAssetsDirectory() != null) {
        assetManager.addAssetPath(Fs.externalize(manifest.getAssetsDirectory()));
      }
    }
  }

  @VisibleForTesting
  static Class<? extends Application> getApplicationClass(
      AndroidManifest appManifest, Config config, ApplicationInfo applicationInfo) {
    Class<? extends Application> applicationClass = null;
    if (config != null && !Config.Builder.isDefaultApplication(config.application())) {
      if (config.application().getCanonicalName() != null) {
        try {
          applicationClass = ClassNameResolver.resolve(null, config.application().getName());
        } catch (ClassNotFoundException e) {
          throw new RuntimeException(e);
        }
      }
    } else if (appManifest != null && appManifest.getApplicationName() != null) {
      try {
        applicationClass =
            ClassNameResolver.resolve(
                appManifest.getPackageName(),
                getTestApplicationName(appManifest.getApplicationName()));
      } catch (ClassNotFoundException e) {
        // no problem
      }

      if (applicationClass == null) {
        try {
          applicationClass =
              ClassNameResolver.resolve(
                  appManifest.getPackageName(), appManifest.getApplicationName());
        } catch (ClassNotFoundException e) {
          throw new RuntimeException(e);
        }
      }
    } else {
      if (applicationInfo.className != null) {
        try {
          applicationClass =
              (Class<? extends Application>)
                  Class.forName(getTestApplicationName(applicationInfo.className));
        } catch (ClassNotFoundException e) {
          // no problem
        }

        if (applicationClass == null) {
          try {
            applicationClass =
                (Class<? extends Application>) Class.forName(applicationInfo.className);
          } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
          }
        }
      } else {
        applicationClass = Application.class;
      }
    }

    return applicationClass;
  }

  @VisibleForTesting
  static String getTestApplicationName(String applicationName) {
    int lastDot = applicationName.lastIndexOf('.');
    if (lastDot > -1) {
      return applicationName.substring(0, lastDot)
          + ".Test"
          + applicationName.substring(lastDot + 1);
    } else {
      return "Test" + applicationName;
    }
  }

  protected Instrumentation pickInstrumentation() {
    return new RoboMonitoringInstrumentation();
  }

  private Instrumentation createInstrumentation() {
    Instrumentation androidInstrumentation = pickInstrumentation();
    androidInstrumentation.runOnMainSync(
        () -> {
          ActivityThread activityThread = ReflectionHelpers.callConstructor(ActivityThread.class);
          ReflectionHelpers.setStaticField(
              ActivityThread.class, "sMainThreadHandler", new Handler(Looper.getMainLooper()));
          reflector(_ActivityThread_.class, activityThread)
              .setInstrumentation(androidInstrumentation);
          RuntimeEnvironment.setActivityThread(activityThread);

          Application dummyInitialApplication = new Application();
          final ComponentName dummyInitialComponent =
              new ComponentName("", androidInstrumentation.getClass().getSimpleName());
          reflector(_Instrumentation_.class, androidInstrumentation)
              .init(
                  activityThread,
                  dummyInitialApplication,
                  dummyInitialApplication,
                  dummyInitialComponent,
                  null,
                  null);
        });

    androidInstrumentation.onCreate(new Bundle());
    return androidInstrumentation;
  }

  /** Create a file system safe directory path name for the current test. */
  @SuppressWarnings("DoNotCall")
  private String createTestDataDirRootPath(Method method) {
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
  public void tearDownApplication() {
    if (RuntimeEnvironment.application != null) {
      ShadowInstrumentation.runOnMainSyncNoIdle(RuntimeEnvironment.getApplication()::onTerminate);
      ShadowInstrumentation.getInstrumentation().finish(1, new Bundle());
    }
  }

  /**
   * Clear the global variables set and used by AndroidTestEnvironment TODO Move synchronization
   * logic into its own class for better readability
   */
  private void clearEnvironment() {
    // Need to clear both the application supplier and the instrumentation here *before* clearing
    // RuntimeEnvironment.application. That way if RuntimeEnvironment.getApplication() or
    // ApplicationProvider.getApplicationContext() get called in between here and the end of this
    // method, we don't accidentally trigger application loading with stale references
    RuntimeEnvironment.setApplicationSupplier(null);
    InstrumentationRegistry.registerInstance(null, new Bundle());
    RuntimeEnvironment.setActivityThread(null);
    RuntimeEnvironment.application = null;
    RuntimeEnvironment.systemContext = null;
    Bootstrap.resetDisplayConfiguration();
  }

  @Override
  public void checkStateAfterTestFailure(Throwable t) throws Throwable {
    if (hasUnexecutedRunnables()) {
      t.addSuppressed(new UnExecutedRunnablesException());
    }
    throw t;
  }

  private static final class UnExecutedRunnablesException extends Exception {

    UnExecutedRunnablesException() {
      super(
          "Main looper has queued unexecuted runnables. "
              + "This might be the cause of the test failure. "
              + "You might need a shadowOf(Looper.getMainLooper()).idle() call.");
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
      setStackTrace(new StackTraceElement[0]);
      return this; // no stack trace, wouldn't be useful anyway
    }
  }

  private boolean hasUnexecutedRunnables() {
    ShadowLooper shadowLooper = Shadow.extract(Looper.getMainLooper());
    return !shadowLooper.isIdle();
  }

  @Override
  public void resetState() {
    Locale.setDefault(initialLocale);
    for (ShadowProvider provider : shadowProviders) {
      provider.reset();
    }
  }

  // TODO(christianw): reconcile with ShadowPackageManager.setUpPackageStorage
  private void setUpPackageStorage(
      ApplicationInfo applicationInfo, PackageParser.Package parsedPackage) {
    // TempDirectory tempDirectory = RuntimeEnvironment.getTempDirectory();
    // packageInfo.setVolumeUuid(tempDirectory.createIfNotExists(packageInfo.packageName +
    // "-dataDir").toAbsolutePath().toString());

    applicationInfo.publicSourceDir = parsedPackage.codePath;
    applicationInfo.sourceDir = parsedPackage.codePath;

    applicationInfo.dataDir = createTempDir(applicationInfo.packageName + "-dataDir");

    if (RuntimeEnvironment.getApiLevel() >= Build.VERSION_CODES.N) {
      applicationInfo.credentialProtectedDataDir = createTempDir("userDataDir");
      applicationInfo.deviceProtectedDataDir = createTempDir("deviceDataDir");
    }
  }

  private String createTempDir(String name) {
    return RuntimeEnvironment.getTempDirectory()
        .createIfNotExists(name)
        .toAbsolutePath()
        .toString();
  }

  private static BroadcastReceiver newBroadcastReceiverFromP(
      String receiverClassName, LoadedApk loadedApk) {
    ClassLoader classLoader = Shadow.class.getClassLoader();
    if (loadedApk == null || loadedApk.getAppFactory() == null) {
      return (BroadcastReceiver) newInstanceOf(receiverClassName);
    } else {
      try {
        return loadedApk.getAppFactory().instantiateReceiver(classLoader, receiverClassName, null);
      } catch (ReflectiveOperationException e) {
        Logger.warn(
            "Failed to initialize receiver %s with AppComponentFactory %s: %s",
            receiverClassName, loadedApk.getAppFactory(), e);
      }
    }
    return null;
  }

  private static boolean shouldLoadNativeRuntime() {
    GraphicsMode.Mode graphicsMode = ConfigurationRegistry.get(GraphicsMode.Mode.class);
    SQLiteMode.Mode sqliteMode = ConfigurationRegistry.get(SQLiteMode.Mode.class);
    return graphicsMode == GraphicsMode.Mode.NATIVE || sqliteMode == SQLiteMode.Mode.NATIVE;
  }

  // TODO move/replace this with packageManager
  @VisibleForTesting
  static void registerBroadcastReceivers(
      Application application, AndroidManifest androidManifest, LoadedApk loadedApk) {
    for (BroadcastReceiverData receiver : androidManifest.getBroadcastReceivers()) {
      IntentFilter filter = new IntentFilter();
      for (String action : receiver.getActions()) {
        filter.addAction(action);
      }
      String receiverClassName = receiver.getName();
      if (loadedApk != null && RuntimeEnvironment.getApiLevel() >= P) {
        application.registerReceiver(
            newBroadcastReceiverFromP(receiverClassName, loadedApk), filter);
      } else {
        application.registerReceiver((BroadcastReceiver) newInstanceOf(receiverClassName), filter);
      }
    }
  }
}
