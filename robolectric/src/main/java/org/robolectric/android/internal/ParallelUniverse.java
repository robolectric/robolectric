package org.robolectric.android.internal;

import static org.robolectric.util.ReflectionHelpers.ClassParameter;

import android.app.Application;
import android.app.LoadedApk;
import android.app.ResourcesManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.CompatibilityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Handler;
import android.os.Looper;
import java.io.File;
import java.lang.reflect.Method;
import java.security.Security;
import java.util.Locale;
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
import org.robolectric.res.Qualifiers;
import org.robolectric.res.ResName;
import org.robolectric.res.ResourceTable;
import org.robolectric.res.android.ConfigDescription;
import org.robolectric.res.android.CppAssetManager;
import org.robolectric.res.android.ResTableConfig;
import org.robolectric.res.builder.DefaultPackageManager;
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

    DefaultPackageManager packageManager = new DefaultPackageManager();
    RuntimeEnvironment.setRobolectricPackageManager(packageManager);

    RuntimeEnvironment.setCompileTimeResourceTable(compileTimeResourceTable);
    RuntimeEnvironment.setAppResourceTable(appResourceTable);
    RuntimeEnvironment.setSystemResourceTable(systemResourceTable);

    hackySetSystemResources(); // todo: remove this before merge to master

    initializeAppManifest(appManifest, appResourceTable, packageManager);
    packageManager.setDependencies(appManifest, appResourceTable);

    if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
      Security.insertProviderAt(new BouncyCastleProvider(), 1);
    }

    ConfigDescription configDescription = new ConfigDescription();
    ResTableConfig resTab = new ResTableConfig();
    configDescription.parse(config.qualifiers(), resTab);

    String qualifiers = Qualifiers.addPlatformVersion(config.qualifiers(), sdkConfig.getApiLevel());
    qualifiers = Qualifiers.addSmallestScreenWidth(qualifiers, 320);
    qualifiers = Qualifiers.addScreenWidth(qualifiers, 320);
    RuntimeEnvironment.setQualifiers(qualifiers);

    Resources systemResources = Resources.getSystem();
    Configuration configuration = systemResources.getConfiguration();
    configuration.smallestScreenWidthDp = resTab.smallestScreenWidthDp != 0 ? resTab.smallestScreenWidthDp : 320;
    configuration.screenWidthDp = resTab.screenWidthDp != 0 ? resTab.screenWidthDp : 320 ;
    configuration.orientation = resTab.orientation;

    // begin new stuff
    configuration.mcc = resTab.mcc;
    configuration.mnc = resTab.mnc;
    configuration.screenLayout = resTab.screenLayout;
    configuration.touchscreen = resTab.touchscreen;
    configuration.keyboard = resTab.keyboard;
    configuration.keyboardHidden = resTab.keyboardHidden();
    configuration.navigation = resTab.navigation;
    configuration.navigationHidden = resTab.navigationHidden();
    configuration.orientation = resTab.orientation;
    configuration.uiMode = resTab.uiMode;
    configuration.screenHeightDp = resTab.screenHeightDp;
    configuration.densityDpi = resTab.density;
    // end new stuff

    if (resTab.languageString() != null && resTab.regionString() != null) {
      configuration.setLocale(new Locale(resTab.languageString(), resTab.regionString()));
    } else if (resTab.languageString() != null) {
      configuration.setLocale(new Locale(resTab.languageString()));
    }

    ResourcesManager.getInstance().applyConfigurationToResourcesLocked(configuration,
        CompatibilityInfo.DEFAULT_COMPATIBILITY_INFO);

    systemResources.updateConfiguration(configuration, systemResources.getDisplayMetrics());

    Class<?> contextImplClass = ReflectionHelpers.loadClass(getClass().getClassLoader(), shadowsAdapter.getShadowContextImplClassName());

    Class<?> activityThreadClass = ReflectionHelpers.loadClass(getClass().getClassLoader(), shadowsAdapter.getShadowActivityThreadClassName());
    // Looper needs to be prepared before the activity thread is created
    if (Looper.myLooper() == null) {
      Looper.prepareMainLooper();
    }
    ShadowLooper.getShadowMainLooper().resetScheduler();
    Object activityThread = ReflectionHelpers.newInstance(activityThreadClass);
    RuntimeEnvironment.setActivityThread(activityThread);

    ReflectionHelpers.setField(activityThread, "mInstrumentation", new RoboInstrumentation());
    ReflectionHelpers.setField(activityThread, "mCompatConfiguration", configuration);
    ReflectionHelpers.setStaticField(activityThreadClass, "sMainThreadHandler", new Handler(Looper.myLooper()));

    Context systemContextImpl = ReflectionHelpers.callStaticMethod(contextImplClass, "createSystemContext", ClassParameter.from(activityThreadClass, activityThread));

    final Application application = (Application) testLifecycle.createApplication(method, appManifest, config);
    RuntimeEnvironment.application = application;

    if (application != null) {
      shadowsAdapter.bind(application, appManifest);

      ApplicationInfo applicationInfo;
      try {
        applicationInfo = packageManager.getApplicationInfo(appManifest.getPackageName(), 0);
      } catch (PackageManager.NameNotFoundException e) {
        throw new RuntimeException(e);
      }

      Class<?> compatibilityInfoClass = ReflectionHelpers.loadClass(getClass().getClassLoader(), "android.content.res.CompatibilityInfo");

      LoadedApk loadedApk = ReflectionHelpers.callInstanceMethod(activityThread, "getPackageInfo",
          ClassParameter.from(ApplicationInfo.class, applicationInfo),
          ClassParameter.from(compatibilityInfoClass, null),
          ClassParameter.from(int.class, Context.CONTEXT_INCLUDE_CODE));

      try {
        Context contextImpl = systemContextImpl.createPackageContext(applicationInfo.packageName, Context.CONTEXT_INCLUDE_CODE);
        ReflectionHelpers.setField(activityThreadClass, activityThread, "mInitialApplication", application);
        ApplicationTestUtil.attach(application, contextImpl);
      } catch (PackageManager.NameNotFoundException e) {
        throw new RuntimeException(e);
      }

      Resources appResources = application.getResources();
      ReflectionHelpers.setField(loadedApk, "mResources", appResources);
      ReflectionHelpers.setField(loadedApk, "mApplication", application);

      appResources.updateConfiguration(configuration, appResources.getDisplayMetrics());

      application.onCreate();
    }
  }

  private void hackySetSystemResources() {
    File defaultAndroidHome = new File(System.getProperty("user.home"), "Android/Sdk");
    String androidHomeString = System.getenv("ANDROID_HOME");
    File androidHome = androidHomeString == null ? defaultAndroidHome : new File(androidHomeString);
    File sdkDir = new File(androidHome,
        "platforms/android-" + sdkConfig.getApiLevel());
    if (!new File(sdkDir, "android.jar").exists()) {
      throw new RuntimeException(new File(sdkDir, "android.jar ") + "not found, install it!");
    }
    CppAssetManager.setSystemResourcesPathHackHackHack(
        sdkDir);
  }

  /**
   * Create a file system safe directory path name for the current test.
   */
  private String createTestDataDirRootPath(Method method) {
    return method.getClass().getSimpleName() + "_" + method.getName().replaceAll("[^a-zA-Z0-9.-]", "_");
  }

  private void initializeAppManifest(AndroidManifest appManifest, ResourceTable appResourceTable, DefaultPackageManager packageManager) {
    try {
      appManifest.initMetaData(appResourceTable);
    } catch (RoboNotFoundException e) {
      throw new Resources.NotFoundException(e.getMessage(), e);
    }

    int labelRes = 0;
    if (appManifest.getLabelRef() != null) {
      String fullyQualifiedName = ResName.qualifyResName(appManifest.getLabelRef(), appManifest.getPackageName());
      Integer id = fullyQualifiedName == null ? null : appResourceTable.getResourceId(new ResName(fullyQualifiedName));
      labelRes = id != null ? id : 0;
    }
    packageManager.addManifest(appManifest, labelRes);
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
}
