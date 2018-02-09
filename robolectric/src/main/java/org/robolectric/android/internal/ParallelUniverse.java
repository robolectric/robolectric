package org.robolectric.android.internal;

import static org.robolectric.Shadows.shadowOf;
import static org.robolectric.shadow.api.Shadow.newInstanceOf;
import static org.robolectric.util.ReflectionHelpers.ClassParameter.from;

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
import android.util.DisplayMetrics;
import com.google.common.annotations.VisibleForTesting;
import java.lang.reflect.Method;
import java.security.Security;
import java.util.Locale;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.android.Bootstrap;
import org.robolectric.annotation.Config;
import org.robolectric.internal.ParallelUniverseInterface;
import org.robolectric.internal.SdkConfig;
import org.robolectric.manifest.AndroidManifest;
import org.robolectric.manifest.BroadcastReceiverData;
import org.robolectric.manifest.RoboNotFoundException;
import org.robolectric.res.ResourceTable;
import org.robolectric.shadows.ClassNameResolver;
import org.robolectric.shadows.LegacyManifestParser;
import org.robolectric.shadows.ShadowActivityThread;
import org.robolectric.shadows.ShadowContextImpl;
import org.robolectric.shadows.ShadowLog;
import org.robolectric.shadows.ShadowLooper;
import org.robolectric.shadows.ShadowPackageParser;
import org.robolectric.util.PerfStatsCollector;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.Scheduler;
import org.robolectric.util.TempDirectory;

public class ParallelUniverse implements ParallelUniverseInterface {

  private boolean loggingInitialized = false;
  private SdkConfig sdkConfig;

  @Override
  public void setUpApplicationState(
      Method method,
      AndroidManifest appManifest,
      Config config,
      ResourceTable compileTimeResourceTable,
      ResourceTable appResourceTable,
      ResourceTable systemResourceTable) {
    ReflectionHelpers.setStaticField(RuntimeEnvironment.class, "apiLevel", sdkConfig.getApiLevel());

    RuntimeEnvironment.application = null;
    RuntimeEnvironment.setActivityThread(null);
    RuntimeEnvironment.setTempDirectory(new TempDirectory(createTestDataDirRootPath(method)));
    RuntimeEnvironment.setMasterScheduler(new Scheduler());
    RuntimeEnvironment.setMainThread(Thread.currentThread());

    RuntimeEnvironment.setCompileTimeResourceTable(compileTimeResourceTable);
    RuntimeEnvironment.setAppResourceTable(appResourceTable);
    RuntimeEnvironment.setSystemResourceTable(systemResourceTable);

    if (!loggingInitialized) {
      ShadowLog.setupLogging();
      loggingInitialized = true;
    }

    try {
      appManifest.initMetaData(appResourceTable);
    } catch (RoboNotFoundException e1) {
      throw new Resources.NotFoundException(e1.getMessage());
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

    PackageParser.Package parsedPackage = null;

    ApplicationInfo applicationInfo = null;
    if (appManifest.getAndroidManifestFile() != null
        && appManifest.getAndroidManifestFile().exists()) {
      if (Boolean.parseBoolean(System.getProperty("use_framework_manifest_parser", "false"))) {
        parsedPackage = ShadowPackageParser.callParsePackage(appManifest.getAndroidManifestFile());
      } else {
        parsedPackage = LegacyManifestParser.createPackage(appManifest);
      }
    } else {
      parsedPackage = new PackageParser.Package("org.robolectric.default");
      parsedPackage.applicationInfo.targetSdkVersion = appManifest.getTargetSdkVersion();
    }
    applicationInfo = parsedPackage.applicationInfo;

    // Support overriding the package name specified in the Manifest.
    if (!Config.DEFAULT_PACKAGE_NAME.equals(config.packageName())) {
      parsedPackage.packageName = config.packageName();
      parsedPackage.applicationInfo.packageName = config.packageName();
    } else {
      parsedPackage.packageName = appManifest.getPackageName();
      parsedPackage.applicationInfo.packageName = appManifest.getPackageName();
    }
    // TempDirectory tempDirectory = RuntimeEnvironment.getTempDirectory();
    // packageInfo.setVolumeUuid(tempDirectory.createIfNotExists(packageInfo.packageName +
    // "-dataDir").toAbsolutePath().toString());
    setUpPackageStorage(applicationInfo);

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
    ReflectionHelpers.setStaticField(ActivityThread.class, "sMainThreadHandler", new Handler(Looper.myLooper()));

    Bootstrap.setUpDisplay(configuration, displayMetrics);

    Resources systemResources = Resources.getSystem();
    systemResources.updateConfiguration(configuration, displayMetrics);

    Context systemContextImpl = ReflectionHelpers.callStaticMethod(contextImplClass,
        "createSystemContext", from(ActivityThread.class, activityThread));
    RuntimeEnvironment.systemContext = systemContextImpl;

    Application application = createApplication(appManifest, config);
    RuntimeEnvironment.application = application;

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

      LoadedApk loadedApk = activityThread.getPackageInfo(applicationInfo, null, Context.CONTEXT_INCLUDE_CODE);

      try {
        Context contextImpl = systemContextImpl.createPackageContext(applicationInfo.packageName, Context.CONTEXT_INCLUDE_CODE);
        shadowOf(contextImpl.getPackageManager()).addPackage(parsedPackage);
        ReflectionHelpers.setField(ActivityThread.class, activityThread, "mInitialApplication", application);
        shadowOf(application).callAttach(contextImpl);
      } catch (PackageManager.NameNotFoundException e) {
        throw new RuntimeException(e);
      }

      Resources appResources = application.getResources();
      ReflectionHelpers.setField(loadedApk, "mResources", appResources);
      ReflectionHelpers.setField(loadedApk, "mApplication", application);

      appResources.updateConfiguration(configuration, displayMetrics);
      populateAssetPaths(appResources.getAssets(), appManifest);

      initInstrumentation(activityThread, applicationInfo);

      PerfStatsCollector.getInstance().measure("application onCreate()", () -> {
        application.onCreate();
      });
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

    if (appManifest != null) {
      registerBroadcastReceivers(application, appManifest);
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

  private void initInstrumentation(
      ActivityThread activityThread,
      ApplicationInfo applicationInfo) {
    Instrumentation androidInstrumentation = createInstrumentation();
    ReflectionHelpers.setField(activityThread, "mInstrumentation", androidInstrumentation);

    final ComponentName component =
        new ComponentName(
            applicationInfo.packageName, androidInstrumentation.getClass().getSimpleName());
    if (RuntimeEnvironment.getApiLevel() <= VERSION_CODES.JELLY_BEAN_MR1) {
      ReflectionHelpers.callInstanceMethod(androidInstrumentation, "init",
          from(ActivityThread.class, activityThread),
          from(Context.class, RuntimeEnvironment.application),
          from(Context.class, RuntimeEnvironment.application),
          from(ComponentName.class, component),
          from(IInstrumentationWatcher.class, null));
    } else {
      ReflectionHelpers.callInstanceMethod(androidInstrumentation, "init",
          from(ActivityThread.class, activityThread),
          from(Context.class, RuntimeEnvironment.application),
          from(Context.class, RuntimeEnvironment.application),
          from(ComponentName.class, component),
          from(IInstrumentationWatcher.class, null),
          from(IUiAutomationConnection.class, null));
    }

    androidInstrumentation.onCreate(new Bundle());
  }

  private Instrumentation createInstrumentation() {
    // Use RoboInstrumentation if its parent class from optional dependency android.support.test is
    // available. Otherwise use Instrumentation
    try {
      Class<? extends Instrumentation> roboInstrumentationClass =
          Class.forName("org.robolectric.android.fakes.RoboInstrumentation").asSubclass(
              Instrumentation.class);
      return ReflectionHelpers.newInstance(roboInstrumentationClass);
    } catch (ClassNotFoundException | NoClassDefFoundError e) {
      // fall through
    }
    return new Instrumentation();
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

  @Override
  public void setSdkConfig(SdkConfig sdkConfig) {
    this.sdkConfig = sdkConfig;
    ReflectionHelpers.setStaticField(RuntimeEnvironment.class, "apiLevel", sdkConfig.getApiLevel());
  }

  private static void setUpPackageStorage(ApplicationInfo applicationInfo) {
    TempDirectory tempDirectory = RuntimeEnvironment.getTempDirectory();
    applicationInfo.sourceDir =
        tempDirectory
            .createIfNotExists(applicationInfo.packageName + "-sourceDir")
            .toAbsolutePath()
            .toString();
    applicationInfo.publicSourceDir =
        tempDirectory
            .createIfNotExists(applicationInfo.packageName + "-publicSourceDir")
            .toAbsolutePath()
            .toString();
    applicationInfo.dataDir =
        tempDirectory
            .createIfNotExists(applicationInfo.packageName + "-dataDir")
            .toAbsolutePath()
            .toString();

    if (RuntimeEnvironment.getApiLevel() >= Build.VERSION_CODES.N) {
      applicationInfo.credentialProtectedDataDir =
          tempDirectory.createIfNotExists("userDataDir").toAbsolutePath().toString();
      applicationInfo.deviceProtectedDataDir =
          tempDirectory.createIfNotExists("deviceDataDir").toAbsolutePath().toString();
    }
  }

  // TODO move/replace this with packageManager
  private static void registerBroadcastReceivers(
      Application application, AndroidManifest androidManifest) {
    for (BroadcastReceiverData receiver : androidManifest.getBroadcastReceivers()) {
      IntentFilter filter = new IntentFilter();
      for (String action : receiver.getActions()) {
        filter.addAction(action);
      }
      String receiverClassName = replaceLastDotWith$IfInnerStaticClass(receiver.getName());
      shadowOf(application)
          .registerReceiver((BroadcastReceiver) newInstanceOf(receiverClassName), filter);
    }
  }

  private static String replaceLastDotWith$IfInnerStaticClass(String receiverClassName) {
    String[] splits = receiverClassName.split("\\.");
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
