package org.robolectric.android.internal;

import static com.google.common.base.Strings.isNullOrEmpty;
import static org.robolectric.util.ReflectionHelpers.ClassParameter;

import android.app.ActivityThread;
import android.app.Application;
import android.app.LoadedApk;
import android.app.ResourcesManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.CompatibilityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build.VERSION_CODES;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
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
import org.robolectric.internal.dependency.DependencyResolver;
import org.robolectric.manifest.AndroidManifest;
import org.robolectric.manifest.RoboNotFoundException;
import org.robolectric.res.Qualifiers;
import org.robolectric.res.ResourceTable;
import org.robolectric.res.android.ConfigDescription;
import org.robolectric.res.android.ResTable_config;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.shadows.ShadowLooper;
import org.robolectric.shadows.ShadowResourcesManager;
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
      DependencyResolver jarResolver, Config config, ResourceTable compileTimeResourceTable,
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
    RuntimeEnvironment.setAndroidFrameworkJarPath(
        jarResolver.getLocalArtifactUrl(sdkConfig.getAndroidSdkDependency()).getFile());

    try {
      appManifest.initMetaData(appResourceTable);
    } catch (RoboNotFoundException e1) {
      throw new Resources.NotFoundException(e1.getMessage());
    }
    RuntimeEnvironment.setApplicationManifest(appManifest);

    if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
      Security.insertProviderAt(new BouncyCastleProvider(), 1);
    }

    String qualifiers = config.qualifiers();
    qualifiers = Qualifiers.addSmallestScreenWidth(qualifiers, 320);
    qualifiers = Qualifiers.addScreenWidth(qualifiers, 320);
    qualifiers = Qualifiers.addPlatformVersion(qualifiers, sdkConfig.getApiLevel());
    RuntimeEnvironment.setQualifiers(qualifiers);

    ConfigDescription configDescription = new ConfigDescription();
    ResTable_config resTab = new ResTable_config();
    configDescription.parse(qualifiers, resTab);

    Resources systemResources = Resources.getSystem();
    Configuration configuration = systemResources.getConfiguration();
    DisplayMetrics displayMetrics = systemResources.getDisplayMetrics();
    configuration.smallestScreenWidthDp = resTab.smallestScreenWidthDp;
    configuration.screenWidthDp = resTab.screenWidthDp;
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
    if (sdkConfig.getApiLevel() >= VERSION_CODES.JELLY_BEAN_MR1) {
      configuration.densityDpi = resTab.density;
    }
    // TODO: handle the else case aka API 16
    //displayMetrics.density = ((float)resTab.density)/160;
    //configuration.
    // end new stuff

    // JDK has a default locale of en_US. A previous test may have changed the default, so reset it
    // here
    Locale.setDefault(Locale.US);
    Locale locale = null;
    if (!isNullOrEmpty(resTab.languageString()) || !isNullOrEmpty(resTab.regionString())) {
      locale = new Locale(resTab.languageString(), resTab.regionString());
    } else if (!isNullOrEmpty(resTab.languageString())) {
      locale = new Locale(resTab.languageString());
    }
    if (locale != null) {
      if (sdkConfig.getApiLevel() >= VERSION_CODES.JELLY_BEAN_MR1) {
        configuration.setLocale(locale);
      } else {
        configuration.locale = locale;
      }
    }

    if (sdkConfig.getApiLevel() >= VERSION_CODES.KITKAT) {
      ResourcesManager resourcesManager = ResourcesManager.getInstance();
      ShadowResourcesManager shadowResourcesManager = Shadow.extract(resourcesManager);
      shadowResourcesManager.callApplyConfigurationToResourcesLocked(
          configuration, CompatibilityInfo.DEFAULT_COMPATIBILITY_INFO);
    }

    systemResources.updateConfiguration(configuration, displayMetrics);

    Class<?> contextImplClass = ReflectionHelpers.loadClass(getClass().getClassLoader(), shadowsAdapter.getShadowContextImplClassName());

    // Looper needs to be prepared before the activity thread is created
    if (Looper.myLooper() == null) {
      Looper.prepareMainLooper();
    }
    ShadowLooper.getShadowMainLooper().resetScheduler();
    ActivityThread activityThread = ReflectionHelpers.newInstance(ActivityThread.class);
    RuntimeEnvironment.setActivityThread(activityThread);

    ReflectionHelpers.setField(activityThread, "mInstrumentation", new RoboInstrumentation());
    ReflectionHelpers.setField(activityThread, "mCompatConfiguration", configuration);
    ReflectionHelpers.setStaticField(ActivityThread.class, "sMainThreadHandler", new Handler(Looper.myLooper()));

    Context systemContextImpl = ReflectionHelpers.callStaticMethod(contextImplClass, "createSystemContext", ClassParameter.from(ActivityThread.class, activityThread));

    final Application application = (Application) testLifecycle.createApplication(method, appManifest, config);
    RuntimeEnvironment.application = application;

    if (application != null) {
      shadowsAdapter.bind(application, appManifest);

      final ApplicationInfo applicationInfo;
      try {
        applicationInfo = systemContextImpl.getPackageManager().getApplicationInfo(appManifest.getPackageName(), 0);
      } catch (PackageManager.NameNotFoundException e) {
        throw new RuntimeException(e);
      }

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

      Resources appResources = application.getResources();
      ReflectionHelpers.setField(loadedApk, "mResources", appResources);
      ReflectionHelpers.setField(loadedApk, "mApplication", application);

      appResources.updateConfiguration(configuration, displayMetrics);

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
}
