package org.robolectric.android.internal;

import static android.location.LocationManager.GPS_PROVIDER;
import static android.os.Build.VERSION_CODES.P;
import static org.robolectric.shadow.api.Shadow.newInstanceOf;
import static org.robolectric.util.ReflectionHelpers.ClassParameter.from;

import android.annotation.SuppressLint;
import android.app.ActivityThread;
import android.app.Application;
import android.app.IInstrumentationWatcher;
import android.app.IUiAutomationConnection;
import android.app.Instrumentation;
import android.app.LoadedApk;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageParser;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings.Secure;
import android.util.DisplayMetrics;
import com.google.common.annotations.VisibleForTesting;
import java.lang.reflect.Method;
import java.security.Security;
import java.util.Locale;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.robolectric.ApkLoader;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.android.Bootstrap;
import org.robolectric.android.fakes.RoboMonitoringInstrumentation;
import org.robolectric.annotation.Config;
import org.robolectric.internal.ParallelUniverseInterface;
import org.robolectric.internal.SdkConfig;
import org.robolectric.internal.SdkEnvironment;
import org.robolectric.manifest.AndroidManifest;
import org.robolectric.manifest.BroadcastReceiverData;
import org.robolectric.manifest.RoboNotFoundException;
import org.robolectric.res.FsFile;
import org.robolectric.res.PackageResourceTable;
import org.robolectric.res.ResourceTable;
import org.robolectric.res.RoutingResourceTable;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.shadows.ClassNameResolver;
import org.robolectric.shadows.LegacyManifestParser;
import org.robolectric.shadows.ShadowActivityThread;
import org.robolectric.shadows.ShadowApplication;
import org.robolectric.shadows.ShadowAssetManager;
import org.robolectric.shadows.ShadowContextImpl;
import org.robolectric.shadows.ShadowLog;
import org.robolectric.shadows.ShadowLooper;
import org.robolectric.shadows.ShadowPackageManager;
import org.robolectric.shadows.ShadowPackageParser;
import org.robolectric.util.PerfStatsCollector;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.Scheduler;
import org.robolectric.util.TempDirectory;

@SuppressLint("NewApi")
public class ParallelUniverse implements ParallelUniverseInterface {

  private boolean loggingInitialized = false;
  private SdkConfig sdkConfig;

  @Override
  public void setSdkConfig(SdkConfig sdkConfig) {
    this.sdkConfig = sdkConfig;
    ReflectionHelpers.setStaticField(RuntimeEnvironment.class, "apiLevel", sdkConfig.getApiLevel());
  }

  @Override
  public void setResourcesMode(boolean legacyResources) {
    RuntimeEnvironment.setUseLegacyResources(legacyResources);
  }

  @Override
  public void setUpApplicationState(ApkLoader apkLoader, Method method, Config config,
      AndroidManifest appManifest, SdkEnvironment sdkEnvironment) {
    ReflectionHelpers.setStaticField(RuntimeEnvironment.class, "apiLevel", sdkConfig.getApiLevel());

    RuntimeEnvironment.application = null;
    RuntimeEnvironment.setActivityThread(null);
    RuntimeEnvironment.setTempDirectory(new TempDirectory(createTestDataDirRootPath(method)));
    RuntimeEnvironment.setMasterScheduler(new Scheduler());
    RuntimeEnvironment.setMainThread(Thread.currentThread());

    if (!loggingInitialized) {
      ShadowLog.setupLogging();
      loggingInitialized = true;
    }

    if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
      Security.insertProviderAt(new BouncyCastleProvider(), 1);
    }

    Configuration configuration = new Configuration();
    DisplayMetrics displayMetrics = new DisplayMetrics();

    Bootstrap.applyQualifiers(config.qualifiers(), sdkConfig.getApiLevel(), configuration,
        displayMetrics);

    Locale locale = sdkConfig.getApiLevel() >= VERSION_CODES.N
        ? configuration.getLocales().get(0)
        : configuration.locale;
    Locale.setDefault(locale);

    // Looper needs to be prepared before the activity thread is created
    if (Looper.myLooper() == null) {
      Looper.prepareMainLooper();
    }
    ShadowLooper.getShadowMainLooper().resetScheduler();
    ActivityThread activityThread = ReflectionHelpers.newInstance(ActivityThread.class);
    RuntimeEnvironment.setActivityThread(activityThread);

    PackageParser.Package parsedPackage;
    if (RuntimeEnvironment.useLegacyResources()) {
      injectResourceStuffForLegacy(apkLoader, appManifest, sdkEnvironment);

      if (appManifest.getAndroidManifestFile() != null
          && appManifest.getAndroidManifestFile().exists()) {
        parsedPackage = LegacyManifestParser.createPackage(appManifest);
      } else {
        parsedPackage = new PackageParser.Package("org.robolectric.default");
        parsedPackage.applicationInfo.targetSdkVersion = appManifest.getTargetSdkVersion();
      }
      // Support overriding the package name specified in the Manifest.
      if (!Config.DEFAULT_PACKAGE_NAME.equals(config.packageName())) {
        parsedPackage.packageName = config.packageName();
        parsedPackage.applicationInfo.packageName = config.packageName();
      } else {
        parsedPackage.packageName = appManifest.getPackageName();
        parsedPackage.applicationInfo.packageName = appManifest.getPackageName();
      }
    } else {
      RuntimeEnvironment.compileTimeSystemResourcesFile =
          apkLoader.getCompileTimeSystemResourcesFile(sdkEnvironment);

      RuntimeEnvironment.setAndroidFrameworkJarPath(
          apkLoader.getArtifactUrl(sdkConfig.getAndroidSdkDependency()).getFile());

      FsFile packageFile = appManifest.getApkFile();
      parsedPackage = ShadowPackageParser.callParsePackage(packageFile);
    }

    ApplicationInfo applicationInfo = parsedPackage.applicationInfo;

    // unclear why, but prior to P the processName wasn't set
    if (sdkConfig.getApiLevel() < P && applicationInfo.processName == null) {
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

    Class<?> contextImplClass =
        ReflectionHelpers.loadClass(
            getClass().getClassLoader(), ShadowContextImpl.CLASS_NAME);

    ReflectionHelpers.setField(activityThread, "mCompatConfiguration", configuration);
    ReflectionHelpers
        .setStaticField(ActivityThread.class, "sMainThreadHandler", new Handler(Looper.myLooper()));

    Bootstrap.setUpDisplay(configuration, displayMetrics);
    activityThread.applyConfigurationToResources(configuration);

    Resources systemResources = Resources.getSystem();
    systemResources.updateConfiguration(configuration, displayMetrics);

    Context systemContextImpl = ReflectionHelpers.callStaticMethod(contextImplClass,
        "createSystemContext", from(ActivityThread.class, activityThread));
    RuntimeEnvironment.systemContext = systemContextImpl;

    Application application = createApplication(appManifest, config);
    RuntimeEnvironment.application = application;

    Instrumentation instrumentation =
        createInstrumentation(activityThread, applicationInfo, application);

    if (application != null) {
      final Class<?> appBindDataClass;
      try {
        appBindDataClass = Class.forName("android.app.ActivityThread$AppBindData");
      } catch (ClassNotFoundException e) {
        throw new RuntimeException(e);
      }
      Object data = ReflectionHelpers.newInstance(appBindDataClass);
      ReflectionHelpers.setField(data, "processName", "org.robolectric");
      ReflectionHelpers.setField(data, "appInfo", applicationInfo);
      ReflectionHelpers.setField(activityThread, "mBoundApplication", data);

      LoadedApk loadedApk = activityThread
          .getPackageInfo(applicationInfo, null, Context.CONTEXT_INCLUDE_CODE);

      try {
        Context contextImpl = systemContextImpl
            .createPackageContext(applicationInfo.packageName, Context.CONTEXT_INCLUDE_CODE);

        ShadowPackageManager shadowPackageManager = Shadow.extract(contextImpl.getPackageManager());
        shadowPackageManager.addPackageInternal(parsedPackage);
        ReflectionHelpers
            .setField(ActivityThread.class, activityThread, "mInitialApplication", application);
        ShadowApplication shadowApplication = Shadow.extract(application);
        shadowApplication.callAttach(contextImpl);
        ReflectionHelpers.callInstanceMethod(
            contextImpl,
            "setOuterContext",
            ReflectionHelpers.ClassParameter.from(Context.class, application));
      } catch (PackageManager.NameNotFoundException e) {
        throw new RuntimeException(e);
      }

      Secure.setLocationProviderEnabled(application.getContentResolver(), GPS_PROVIDER, true);

      Resources appResources = application.getResources();
      ReflectionHelpers.setField(loadedApk, "mResources", appResources);
      ReflectionHelpers.setField(loadedApk, "mApplication", application);

      registerBroadcastReceivers(application, appManifest);

      appResources.updateConfiguration(configuration, displayMetrics);

      if (ShadowAssetManager.useLegacy()) {
        populateAssetPaths(appResources.getAssets(), appManifest);
      }

      instrumentation.onCreate(new Bundle());

      PerfStatsCollector.getInstance()
          .measure("application onCreate()", () -> application.onCreate());
    }
  }

  private void injectResourceStuffForLegacy(ApkLoader apkLoader, AndroidManifest appManifest,
      SdkEnvironment sdkEnvironment) {
    PackageResourceTable systemResourceTable = apkLoader.getSystemResourceTable(sdkEnvironment);
    PackageResourceTable appResourceTable = apkLoader.getAppResourceTable(appManifest);
    RoutingResourceTable combinedAppResourceTable = new RoutingResourceTable(appResourceTable,
        systemResourceTable);

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
        assetManager.addAssetPath(manifest.getAssetsDirectory().getPath());
      }
    }
  }

  @VisibleForTesting
  static Application createApplication(AndroidManifest appManifest, Config config) {
    Application application = null;
    if (config != null && !Config.Builder.isDefaultApplication(config.application())) {
      if (config.application().getCanonicalName() != null) {
        Class<? extends Application> applicationClass;
        try {
          applicationClass = ClassNameResolver.resolve(null, config.application().getName());
        } catch (ClassNotFoundException e) {
          throw new RuntimeException(e);
        }
        application = ReflectionHelpers.callConstructor(applicationClass);
      }
    } else if (appManifest != null && appManifest.getApplicationName() != null) {
      Class<? extends Application> applicationClass = null;
      try {
        applicationClass = ClassNameResolver.resolve(appManifest.getPackageName(),
            getTestApplicationName(appManifest.getApplicationName()));
      } catch (ClassNotFoundException e) {
        // no problem
      }

      if (applicationClass == null) {
        try {
          applicationClass = ClassNameResolver.resolve(appManifest.getPackageName(),
              appManifest.getApplicationName());
        } catch (ClassNotFoundException e) {
          throw new RuntimeException(e);
        }
      }

      application = ReflectionHelpers.callConstructor(applicationClass);
    } else {
      application = new Application();
    }

    return application;
  }

  @VisibleForTesting
  static String getTestApplicationName(String applicationName) {
    int lastDot = applicationName.lastIndexOf('.');
    if (lastDot > -1) {
      return applicationName.substring(0, lastDot) + ".Test" + applicationName.substring(lastDot + 1);
    } else {
      return "Test" + applicationName;
    }
  }

  private static Instrumentation createInstrumentation(
      ActivityThread activityThread,
      ApplicationInfo applicationInfo, Application application) {
    Instrumentation androidInstrumentation = new RoboMonitoringInstrumentation();
    ReflectionHelpers.setField(activityThread, "mInstrumentation", androidInstrumentation);

    final ComponentName component =
        new ComponentName(
            applicationInfo.packageName, androidInstrumentation.getClass().getSimpleName());
    if (RuntimeEnvironment.getApiLevel() <= VERSION_CODES.JELLY_BEAN_MR1) {
      ReflectionHelpers.callInstanceMethod(androidInstrumentation, "init",
          from(ActivityThread.class, activityThread),
          from(Context.class, application),
          from(Context.class, application),
          from(ComponentName.class, component),
          from(IInstrumentationWatcher.class, null));
    } else {
      ReflectionHelpers.callInstanceMethod(androidInstrumentation,
          "init",
          from(ActivityThread.class, activityThread),
          from(Context.class, application),
          from(Context.class, application),
          from(ComponentName.class, component),
          from(IInstrumentationWatcher.class, null),
          from(IUiAutomationConnection.class, null));
    }

    return androidInstrumentation;
  }

  /**
   * Create a file system safe directory path name for the current test.
   */
  private String createTestDataDirRootPath(Method method) {
    return method.getClass().getSimpleName() + "_" + method.getName().replaceAll("[^a-zA-Z0-9.-]", "_");
  }

  @Override
  public Thread getMainThread() {
    return RuntimeEnvironment.getMainThread();
  }

  @Override
  public void setMainThread(Thread newMainThread) {
    RuntimeEnvironment.setMainThread(newMainThread);
  }

  @Override
  public void tearDownApplication() {
    if (RuntimeEnvironment.application != null) {
      RuntimeEnvironment.application.onTerminate();
    }
  }

  @Override
  public Object getCurrentApplication() {
    return RuntimeEnvironment.application;
  }

  // TODO(christianw): reconcile with ShadowPackageManager.setUpPackageStorage
  private void setUpPackageStorage(ApplicationInfo applicationInfo,
      PackageParser.Package parsedPackage) {
    // TempDirectory tempDirectory = RuntimeEnvironment.getTempDirectory();
    // packageInfo.setVolumeUuid(tempDirectory.createIfNotExists(packageInfo.packageName +
    // "-dataDir").toAbsolutePath().toString());

    if (RuntimeEnvironment.useLegacyResources()) {
      applicationInfo.sourceDir =
          createTempDir(applicationInfo.packageName + "-sourceDir");
      applicationInfo.publicSourceDir =
          createTempDir(applicationInfo.packageName + "-publicSourceDir");
    } else {
      if (sdkConfig.getApiLevel() <= VERSION_CODES.KITKAT) {
        String sourcePath = ReflectionHelpers.getField(parsedPackage, "mPath");
        if (sourcePath == null) {
          sourcePath = createTempDir("sourceDir");
        }
        applicationInfo.publicSourceDir = sourcePath;
        applicationInfo.sourceDir = sourcePath;
      } else {
        applicationInfo.publicSourceDir = parsedPackage.codePath;
        applicationInfo.sourceDir = parsedPackage.codePath;
      }
    }

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

  // TODO move/replace this with packageManager
  @VisibleForTesting
  static void registerBroadcastReceivers(
      Application application, AndroidManifest androidManifest) {
    for (BroadcastReceiverData receiver : androidManifest.getBroadcastReceivers()) {
      IntentFilter filter = new IntentFilter();
      for (String action : receiver.getActions()) {
        filter.addAction(action);
      }
      String receiverClassName = replaceLastDotWith$IfInnerStaticClass(receiver.getName());
      application.registerReceiver((BroadcastReceiver) newInstanceOf(receiverClassName), filter);
    }
  }

  private static String replaceLastDotWith$IfInnerStaticClass(String receiverClassName) {
    String[] splits = receiverClassName.split("\\.", 0);
    String staticInnerClassRegex = "[A-Z][a-zA-Z]*";
    if (splits.length > 1
        && splits[splits.length - 1].matches(staticInnerClassRegex)
        && splits[splits.length - 2].matches(staticInnerClassRegex)) {
      int lastDotIndex = receiverClassName.lastIndexOf(".");
      StringBuilder buffer = new StringBuilder(receiverClassName);
      buffer.setCharAt(lastDotIndex, '$');
      return buffer.toString();
    }
    return receiverClassName;
  }
}
