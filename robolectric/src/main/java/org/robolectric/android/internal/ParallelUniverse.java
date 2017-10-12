package org.robolectric.android.internal;

import static org.robolectric.Shadows.shadowOf;
import static org.robolectric.util.ReflectionHelpers.ClassParameter;

import android.app.ActivityThread;
import android.app.Application;
import android.app.LoadedApk;
import android.app.ResourcesManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageParser;
import android.content.res.CompatibilityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import java.lang.reflect.Method;
import java.security.Security;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.ShadowsAdapter;
import org.robolectric.TestLifecycle;
import org.robolectric.android.ApplicationTestUtil;
import org.robolectric.android.fakes.RoboInstrumentation;
import org.robolectric.annotation.Config;
import org.robolectric.internal.ParallelUniverseInterface;
import org.robolectric.internal.SdkConfig;
import org.robolectric.manifest.AndroidManifest;
import org.robolectric.manifest.RoboNotFoundException;
import org.robolectric.res.Fs;
import org.robolectric.res.Qualifiers;
import org.robolectric.res.ResourceTable;
import org.robolectric.shadows.AndroidManifestPullParser;
import org.robolectric.shadows.ShadowActivityThread;
import org.robolectric.shadows.ShadowLooper;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.Scheduler;
import org.robolectric.util.TempDirectory;

public class ParallelUniverse implements ParallelUniverseInterface {
  private final ShadowsAdapter shadowsAdapter = Robolectric.getShadowsAdapter();

  private boolean loggingInitialized = false;
  private SdkConfig sdkConfig;

  @Override
  public void resetStaticState(Config config) {
    RuntimeEnvironment.setMainThread(Thread.currentThread());
    Robolectric.reset();

    if (!loggingInitialized) {
      shadowsAdapter.setupLogging();
      loggingInitialized = true;
    }
  }

  @Override
  public void setUpApplicationState(Method method, TestLifecycle testLifecycle, AndroidManifest appManifest,
                                    Config config, ResourceTable compileTimeResourceTable,
                                    ResourceTable appResourceTable,
                                    ResourceTable systemResourceTable) {
    ReflectionHelpers.setStaticField(RuntimeEnvironment.class, "apiLevel", sdkConfig.getApiLevel());

    RuntimeEnvironment.application = null;
    RuntimeEnvironment.setTempDirectory(new TempDirectory(createTestDataDirRootPath(method)));
    RuntimeEnvironment.setMasterScheduler(new Scheduler());
    RuntimeEnvironment.setMainThread(Thread.currentThread());

    RuntimeEnvironment.setCompileTimeResourceTable(compileTimeResourceTable);
    RuntimeEnvironment.setAppResourceTable(appResourceTable);
    RuntimeEnvironment.setSystemResourceTable(systemResourceTable);

    try {
      appManifest.initMetaData(appResourceTable);
    } catch (RoboNotFoundException e1) {
      throw new Resources.NotFoundException(e1.getMessage());
    }
    RuntimeEnvironment.setApplicationManifest(appManifest);

    if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
      Security.insertProviderAt(new BouncyCastleProvider(), 1);
    }

    String qualifiers = Qualifiers.addPlatformVersion(config.qualifiers(), sdkConfig.getApiLevel());
    qualifiers = Qualifiers.addSmallestScreenWidth(qualifiers, 320);
    qualifiers = Qualifiers.addScreenWidth(qualifiers, 320);
    Resources systemResources = Resources.getSystem();
    Configuration configuration = systemResources.getConfiguration();
    configuration.smallestScreenWidthDp = Qualifiers.getSmallestScreenWidth(qualifiers);
    configuration.screenWidthDp = Qualifiers.getScreenWidth(qualifiers);
    String orientation = Qualifiers.getOrientation(qualifiers);
    if ("land".equals(orientation)) {
      configuration.orientation = Configuration.ORIENTATION_LANDSCAPE;
    } else if ("port".equals(orientation)) {
      configuration.orientation = Configuration.ORIENTATION_PORTRAIT;
    } else {
      configuration.orientation = Configuration.ORIENTATION_UNDEFINED;
    }

    systemResources.updateConfiguration(configuration, systemResources.getDisplayMetrics());
    RuntimeEnvironment.setQualifiers(qualifiers);

    // Looper needs to be prepared before the activity thread is created
    if (Looper.myLooper() == null) {
      Looper.prepareMainLooper();
    }
    ShadowLooper.getShadowMainLooper().resetScheduler();
    ActivityThread activityThread = ReflectionHelpers.newInstance(ActivityThread.class);
    RuntimeEnvironment.setActivityThread(activityThread);

    PackageParser.Package packageInfo = null;
    if (appManifest.getAndroidManifestFile() != null && appManifest.getAndroidManifestFile().exists()) {

      ResourcesManager resourcesManager = ResourcesManager.getInstance();
      Resources resources;

      if (RuntimeEnvironment.getApiLevel() == Build.VERSION_CODES.M) {
        ReflectionHelpers.callInstanceMethod(ResourcesManager.class, resourcesManager, "applyConfigurationToResourcesLocked",
            ClassParameter.from(Configuration.class, configuration),
            ClassParameter.from(CompatibilityInfo.class, CompatibilityInfo.DEFAULT_COMPATIBILITY_INFO));
      } else if (RuntimeEnvironment.getApiLevel() >= Build.VERSION_CODES.KITKAT){
        resourcesManager.applyConfigurationToResourcesLocked(configuration, CompatibilityInfo.DEFAULT_COMPATIBILITY_INFO);
      }

      String resDir = appManifest.getResDirectory() != null ? appManifest.getResDirectory().getPath() : config.resourceDir();
      if (RuntimeEnvironment.getApiLevel() >= Build.VERSION_CODES.N) {
        resources = resourcesManager.getResources(null, resDir, new String[0], new String[0], new String[0], 0, configuration, CompatibilityInfo.DEFAULT_COMPATIBILITY_INFO, this.getClass().getClassLoader());
      } else if (RuntimeEnvironment.getApiLevel() >= Build.VERSION_CODES.M) {
        resources = ReflectionHelpers.callInstanceMethod(ResourcesManager.class, resourcesManager,"getTopLevelResources",
            ClassParameter.from(String.class, resDir),
            ClassParameter.from(String[].class, new String[0]),
            ClassParameter.from(String[].class, new String[0]),
            ClassParameter.from(String[].class, new String[0]),
            ClassParameter.from(int.class, 0),
            ClassParameter.from(Configuration.class, configuration),
            ClassParameter.from(CompatibilityInfo.class, CompatibilityInfo.DEFAULT_COMPATIBILITY_INFO));
      } else if (RuntimeEnvironment.getApiLevel() >= Build.VERSION_CODES.L) {
        resources = ReflectionHelpers.callInstanceMethod(ResourcesManager.class, resourcesManager,"getTopLevelResources",
            ClassParameter.from(String.class, resDir),
            ClassParameter.from(String[].class, new String[0]),
            ClassParameter.from(String[].class, new String[0]),
            ClassParameter.from(String[].class, new String[0]),
            ClassParameter.from(int.class, 0),
            ClassParameter.from(Configuration.class, configuration),
            ClassParameter.from(CompatibilityInfo.class, CompatibilityInfo.DEFAULT_COMPATIBILITY_INFO),
            ClassParameter.from(IBinder.class, null));
      } else if (RuntimeEnvironment.getApiLevel() >= Build.VERSION_CODES.KITKAT) {
        resources = ReflectionHelpers.callInstanceMethod(ResourcesManager.class, resourcesManager,"getTopLevelResources",
            ClassParameter.from(String.class, resDir),
            ClassParameter.from(int.class, 0),
            ClassParameter.from(Configuration.class, configuration),
            ClassParameter.from(CompatibilityInfo.class, CompatibilityInfo.DEFAULT_COMPATIBILITY_INFO),
            ClassParameter.from(IBinder.class, null));
      } else if (RuntimeEnvironment.getApiLevel() >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
        ReflectionHelpers.callInstanceMethod(ActivityThread.class, activityThread, "applyConfigurationToResourcesLocked", ClassParameter.from(Configuration.class, configuration),
            ClassParameter.from(CompatibilityInfo.class, CompatibilityInfo.DEFAULT_COMPATIBILITY_INFO));

        resources = ReflectionHelpers.callInstanceMethod(ActivityThread.class, activityThread, "getTopLevelResources",
            ClassParameter.from(String.class, resDir),
            ClassParameter.from(int.class, 0),
            ClassParameter.from(Configuration.class, configuration),
            ClassParameter.from(CompatibilityInfo.class, CompatibilityInfo.DEFAULT_COMPATIBILITY_INFO));
      }
      else {
        ReflectionHelpers.callInstanceMethod(ActivityThread.class, activityThread, "applyConfigurationToResourcesLocked", ClassParameter.from(Configuration.class, configuration),
            ClassParameter.from(CompatibilityInfo.class, CompatibilityInfo.DEFAULT_COMPATIBILITY_INFO));

        resources = ReflectionHelpers.callInstanceMethod(ActivityThread.class, activityThread, "getTopLevelResources",
            ClassParameter.from(String.class, resDir),
            ClassParameter.from(CompatibilityInfo.class, CompatibilityInfo.DEFAULT_COMPATIBILITY_INFO));
      }

      AndroidManifestPullParser parser = new AndroidManifestPullParser();
      packageInfo = parser.parse(appManifest.getPackageName(), appManifest.getAndroidManifestFile(),
          resources);
    } else {
      packageInfo = new PackageParser.Package(config.packageName());
    }

    // Support overriding the package name specified in the Manifest.
    if (!Config.DEFAULT_PACKAGE_NAME.equals(config.packageName())) {
      packageInfo.packageName = config.packageName();
      packageInfo.applicationInfo.packageName = config.packageName();
    }

    setUpPackageStorage(packageInfo.applicationInfo);

    ShadowActivityThread.setApplicationPackage(packageInfo);

    Class<?> contextImplClass = ReflectionHelpers.loadClass(getClass().getClassLoader(), shadowsAdapter.getShadowContextImplClassName());

    ReflectionHelpers.setField(activityThread, "mInstrumentation", new RoboInstrumentation());
    ReflectionHelpers.setField(activityThread, "mCompatConfiguration", configuration);
    ReflectionHelpers.setStaticField(ActivityThread.class, "sMainThreadHandler", new Handler(Looper.myLooper()));

    Context systemContextImpl = ReflectionHelpers.callStaticMethod(contextImplClass, "createSystemContext", ClassParameter.from(ActivityThread.class, activityThread));

    final Application application = (Application) testLifecycle.createApplication(method, appManifest, config);
    RuntimeEnvironment.application = application;

    if (application != null) {
      shadowsAdapter.bind(application, appManifest);

      final ApplicationInfo applicationInfo = packageInfo.applicationInfo;

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
        ReflectionHelpers.setField(ActivityThread.class, activityThread, "mInitialApplication", application);
        ApplicationTestUtil.attach(application, contextImpl);
      } catch (PackageManager.NameNotFoundException e) {
        throw new RuntimeException(e);
      }

      shadowOf(application.getPackageManager()).addPackage(packageInfo);

      Resources appResources = application.getResources();
      ReflectionHelpers.setField(loadedApk, "mResources", appResources);
      ReflectionHelpers.setField(loadedApk, "mApplication", application);

      appResources.updateConfiguration(configuration, appResources.getDisplayMetrics());

      application.onCreate();
    }
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
    applicationInfo.sourceDir = tempDirectory.createIfNotExists(applicationInfo.packageName + "-sourceDir").toAbsolutePath().toString();
    applicationInfo.publicSourceDir = tempDirectory.createIfNotExists(applicationInfo.packageName + "-publicSourceDir").toAbsolutePath().toString();
    applicationInfo.dataDir = tempDirectory.createIfNotExists(applicationInfo.packageName + "-dataDir").toAbsolutePath().toString();

    if (RuntimeEnvironment.getApiLevel() >= Build.VERSION_CODES.N) {
      applicationInfo.credentialProtectedDataDir = tempDirectory.createIfNotExists("userDataDir").toAbsolutePath().toString();
      applicationInfo.deviceProtectedDataDir = tempDirectory.createIfNotExists("deviceDataDir").toAbsolutePath().toString();
    }
  }
}
