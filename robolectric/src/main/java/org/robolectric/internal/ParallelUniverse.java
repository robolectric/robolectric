package org.robolectric.internal;

import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import org.robolectric.*;
import org.robolectric.annotation.Config;
import org.robolectric.res.ResBunch;
import org.robolectric.res.ResourceLoader;
import org.robolectric.res.builder.RobolectricPackageManager;
import org.robolectric.shadows.ShadowActivityThread;
import org.robolectric.shadows.ShadowContextImpl;
import org.robolectric.shadows.ShadowLog;
import org.robolectric.shadows.ShadowResources;
import org.robolectric.util.ReflectionHelpers;

import java.lang.reflect.Method;

import static org.robolectric.Shadows.shadowOf;
import static org.robolectric.util.ReflectionHelpers.ClassParameter;

public class ParallelUniverse implements ParallelUniverseInterface {
  private static final String DEFAULT_PACKAGE_NAME = "org.robolectric.default";
  private final RobolectricTestRunner robolectricTestRunner;

  private boolean loggingInitialized = false;
  private SdkConfig sdkConfig;

  public ParallelUniverse(RobolectricTestRunner robolectricTestRunner) {
    this.robolectricTestRunner = robolectricTestRunner;
  }

  @Override
  public void resetStaticState(Config config) {
    Robolectric.reset(config);

    if (!loggingInitialized) {
      ShadowLog.setupLogging();
      loggingInitialized = true;
    }
  }

  /*
   * If the Config already has a version qualifier, do nothing. Otherwise, add a version
   * qualifier for the target api level (which comes from the manifest or Config.emulateSdk()).
   */
  private String addVersionQualifierToQualifiers(String qualifiers) {
    int versionQualifierApiLevel = ResBunch.getVersionQualifierApiLevel(qualifiers);
    if (versionQualifierApiLevel == -1) {
      if (qualifiers.length() > 0) {
        qualifiers += "-";
      }
      qualifiers += "v" + sdkConfig.getApiLevel();
    }
    return qualifiers;
  }

  @Override
  public void setUpApplicationState(Method method, TestLifecycle testLifecycle, ResourceLoader systemResourceLoader, AndroidManifest appManifest, Config config) {
    Robolectric.application = null;
    Robolectric.packageManager = new RobolectricPackageManager();
    Robolectric.packageManager.addPackage(DEFAULT_PACKAGE_NAME);
    ResourceLoader resourceLoader;
    if (appManifest != null) {
      resourceLoader = robolectricTestRunner.getAppResourceLoader(sdkConfig, systemResourceLoader, appManifest);
      Robolectric.packageManager.addManifest(appManifest, resourceLoader);
    } else {
      resourceLoader = systemResourceLoader;
    }

    ShadowResources.setSystemResources(systemResourceLoader);
    String qualifiers = addVersionQualifierToQualifiers(config.qualifiers());
    Resources systemResources = Resources.getSystem();
    Configuration configuration = systemResources.getConfiguration();
    shadowOf(configuration).overrideQualifiers(qualifiers);
    systemResources.updateConfiguration(configuration, systemResources.getDisplayMetrics());
    shadowOf(systemResources.getAssets()).setQualifiers(qualifiers);

    Class<?> contextImplClass = ReflectionHelpers.loadClassReflectively(getClass().getClassLoader(), ShadowContextImpl.CLASS_NAME);

    Class<?> activityThreadClass = ReflectionHelpers.loadClassReflectively(getClass().getClassLoader(), ShadowActivityThread.CLASS_NAME);
    Object activityThread = ReflectionHelpers.callConstructorReflectively(activityThreadClass);
    Robolectric.activityThread = activityThread;

    ReflectionHelpers.setFieldReflectively(activityThread, "mInstrumentation", new RoboInstrumentation());
    ReflectionHelpers.setFieldReflectively(activityThread, "mCompatConfiguration", configuration);

    Context systemContextImpl = ReflectionHelpers.callStaticMethodReflectively(contextImplClass, "createSystemContext", new ClassParameter(activityThreadClass, activityThread));

    final Application application = (Application) testLifecycle.createApplication(method, appManifest, config);
    if (application != null) {
      String packageName = appManifest != null ? appManifest.getPackageName() : null;
      if (packageName == null) packageName = DEFAULT_PACKAGE_NAME;

      ApplicationInfo applicationInfo;
      try {
        applicationInfo = Robolectric.packageManager.getApplicationInfo(packageName, 0);
      } catch (PackageManager.NameNotFoundException e) {
        throw new RuntimeException(e);
      }

      Class<?> compatibilityInfoClass = ReflectionHelpers.loadClassReflectively(getClass().getClassLoader(), "android.content.res.CompatibilityInfo");

      Object loadedApk = ReflectionHelpers.callInstanceMethodReflectively(activityThread, "getPackageInfo", new ClassParameter(ApplicationInfo.class, applicationInfo),
          new ClassParameter(compatibilityInfoClass, null), new ClassParameter(ClassLoader.class, getClass().getClassLoader()), new ClassParameter(boolean.class, false),
          new ClassParameter(boolean.class, true));

      shadowOf(application).bind(appManifest, resourceLoader);
      if (appManifest == null) {
        // todo: make this cleaner...
        shadowOf(application).setPackageName(applicationInfo.packageName);
      }
      Resources appResources = application.getResources();
      ReflectionHelpers.setFieldReflectively(loadedApk, "mResources", appResources);
      Context contextImpl = ReflectionHelpers.callInstanceMethodReflectively(systemContextImpl, "createPackageContext", new ClassParameter(String.class, applicationInfo.packageName), new ClassParameter(int.class, Context.CONTEXT_INCLUDE_CODE));
      ReflectionHelpers.setFieldReflectively(activityThread, "mInitialApplication", application);
      ReflectionHelpers.callInstanceMethodReflectively(application, "attach", new ClassParameter(Context.class, contextImpl));

      appResources.updateConfiguration(configuration, appResources.getDisplayMetrics());
      shadowOf(appResources.getAssets()).setQualifiers(qualifiers);

      Robolectric.application = application;
      application.onCreate();
    }
  }

  @Override
  public void tearDownApplication() {
    if (Robolectric.application != null) {
      Robolectric.application.onTerminate();
    }
  }

  @Override
  public Object getCurrentApplication() {
    return Robolectric.application;
  }

  @Override
  public void setSdkConfig(SdkConfig sdkConfig) {
    this.sdkConfig = sdkConfig;
  }
}
