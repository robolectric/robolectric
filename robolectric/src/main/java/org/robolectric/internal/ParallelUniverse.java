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

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.robolectric.Robolectric.shadowOf;

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
  public void setUpApplicationState(Method method, TestLifecycle testLifecycle, boolean strictI18n, ResourceLoader systemResourceLoader, AndroidManifest appManifest, Config config) {
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

    Class<?> contextImplClass;
    try {
      contextImplClass = getClass().getClassLoader().loadClass(ShadowContextImpl.CLASS_NAME);
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }

    Class<?> activityThreadClass;
    Object activityThread;
    try {
      activityThreadClass = getClass().getClassLoader().loadClass(ShadowActivityThread.CLASS_NAME);
      activityThread = activityThreadClass.getConstructor().newInstance();
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    } catch (InvocationTargetException e) {
      throw new RuntimeException(e);
    } catch (NoSuchMethodException e) {
      throw new RuntimeException(e);
    } catch (InstantiationException e) {
      throw new RuntimeException(e);
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    }
    Robolectric.activityThread = activityThread;

    try {
      Field mInstrumentation = activityThread.getClass().getDeclaredField("mInstrumentation");
      mInstrumentation.setAccessible(true);
      mInstrumentation.set(activityThread, new RoboInstrumentation());
    } catch (NoSuchFieldException e) {
      throw new RuntimeException(e);
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    }

    try {
      Field mCompatConfiguration = activityThread.getClass().getDeclaredField("mCompatConfiguration");
      mCompatConfiguration.setAccessible(true);
      mCompatConfiguration.set(activityThread, configuration);
    } catch (NoSuchFieldException e) {
      throw new RuntimeException(e);
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    }

    Context systemContextImpl;
    try {
      Method createSystemContext = contextImplClass.getMethod("createSystemContext", activityThreadClass);
      systemContextImpl = (Context) createSystemContext.invoke(null, activityThread);
    } catch (NoSuchMethodException e) {
      throw new RuntimeException(e);
    } catch (InvocationTargetException e) {
      throw new RuntimeException(e);
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    }

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

      Class<?> compatibilityInfoClass;
      try {
        compatibilityInfoClass = getClass().getClassLoader().loadClass("android.content.res.CompatibilityInfo");
      } catch (ClassNotFoundException e) {
        throw new RuntimeException(e);
      }

      Object loadedApk;
      try {
        Method getPackageInfo = activityThread.getClass().getMethod("getPackageInfo", ApplicationInfo.class, compatibilityInfoClass, ClassLoader.class, boolean.class, boolean.class);
        loadedApk = getPackageInfo.invoke(activityThread, applicationInfo, null, getClass().getClassLoader(), false, true);
      } catch (NoSuchMethodException e) {
        throw new RuntimeException(e);
      } catch (InvocationTargetException e) {
        throw new RuntimeException(e);
      } catch (IllegalAccessException e) {
        throw new RuntimeException(e);
      }

      shadowOf(application).bind(appManifest, resourceLoader);
      if (appManifest == null) {
        // todo: make this cleaner...
        shadowOf(application).setPackageName(applicationInfo.packageName);
      }
      Resources appResources = application.getResources();
      try {
        Field mResources = loadedApk.getClass().getDeclaredField("mResources");
        mResources.setAccessible(true);
        mResources.set(loadedApk, appResources);
      } catch (NoSuchFieldException e) {
        throw new RuntimeException(e);
      } catch (IllegalAccessException e) {
        throw new RuntimeException(e);
      }

      Context contextImpl;
      try {
        Method createPackageContext = systemContextImpl.getClass().getMethod("createPackageContext", String.class, int.class);
        contextImpl = (Context) createPackageContext.invoke(systemContextImpl, applicationInfo.packageName, Context.CONTEXT_INCLUDE_CODE);
      } catch (NoSuchMethodException e) {
        throw new RuntimeException(e);
      } catch (InvocationTargetException e) {
        throw new RuntimeException(e);
      } catch (IllegalAccessException e) {
        throw new RuntimeException(e);
      }

      try {
        Field mInitialApplication = activityThread.getClass().getDeclaredField("mInitialApplication");
        mInitialApplication.setAccessible(true);
        mInitialApplication.set(activityThread, application);
      } catch (NoSuchFieldException e) {
        throw new RuntimeException(e);
      } catch (IllegalAccessException e) {
        throw new RuntimeException(e);
      }

      try {
        Method attach = application.getClass().getMethod("attach", Context.class);
        attach.invoke(application, contextImpl);
      } catch (NoSuchMethodException e) {
        throw new RuntimeException(e);
      } catch (InvocationTargetException e) {
        throw new RuntimeException(e);
      } catch (IllegalAccessException e) {
        throw new RuntimeException(e);
      }

      appResources.updateConfiguration(configuration, appResources.getDisplayMetrics());
      shadowOf(appResources.getAssets()).setQualifiers(qualifiers);
      shadowOf(application).setStrictI18n(strictI18n);

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
