package org.robolectric.internal;

import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.robolectric.*;
import org.robolectric.annotation.Config;
import org.robolectric.internal.fakes.RoboInstrumentation;
import org.robolectric.manifest.AndroidManifest;
import org.robolectric.res.ResBunch;
import org.robolectric.res.ResourceLoader;
import org.robolectric.res.builder.DefaultPackageManager;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.ShadowsAdapter;

import java.io.File;
import java.lang.reflect.Method;
import java.security.Security;

import static org.robolectric.util.ReflectionHelpers.ClassParameter;

public class ParallelUniverse implements ParallelUniverseInterface {
  private static final String DEFAULT_PACKAGE_NAME = "org.robolectric.default";
  private final RobolectricTestRunner robolectricTestRunner;
  private final ShadowsAdapter shadowsAdapter = Robolectric.getShadowsAdapter();

  private boolean loggingInitialized = false;
  private SdkConfig sdkConfig;

  public ParallelUniverse(RobolectricTestRunner robolectricTestRunner) {
    this.robolectricTestRunner = robolectricTestRunner;
  }

  @Override
  public void resetStaticState(Config config) {
    Robolectric.reset();

    if (!loggingInitialized) {
      shadowsAdapter.setupLogging();
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
    RuntimeEnvironment.application = null;
    RuntimeEnvironment.setRobolectricPackageManager(new DefaultPackageManager(shadowsAdapter));
    RuntimeEnvironment.getRobolectricPackageManager().addPackage(DEFAULT_PACKAGE_NAME);
    ResourceLoader resourceLoader;
    if (appManifest != null) {
      resourceLoader = robolectricTestRunner.getAppResourceLoader(sdkConfig, systemResourceLoader, appManifest);
      RuntimeEnvironment.getRobolectricPackageManager().addManifest(appManifest, resourceLoader);
    } else {
      resourceLoader = systemResourceLoader;
    }

    Security.insertProviderAt(new BouncyCastleProvider(), 1);

    shadowsAdapter.setSystemResources(systemResourceLoader);
    String qualifiers = addVersionQualifierToQualifiers(config.qualifiers());
    Resources systemResources = Resources.getSystem();
    Configuration configuration = systemResources.getConfiguration();
    shadowsAdapter.overrideQualifiers(configuration, qualifiers);
    systemResources.updateConfiguration(configuration, systemResources.getDisplayMetrics());
    RuntimeEnvironment.setQualifiers(qualifiers);

    Class<?> contextImplClass = ReflectionHelpers.loadClass(getClass().getClassLoader(), shadowsAdapter.getShadowContextImplClassName());

    Class<?> activityThreadClass = ReflectionHelpers.loadClass(getClass().getClassLoader(), shadowsAdapter.getShadowActivityThreadClassName());
    Object activityThread = ReflectionHelpers.callConstructor(activityThreadClass);
    RuntimeEnvironment.setActivityThread(activityThread);

    ReflectionHelpers.setField(activityThread, "mInstrumentation", new RoboInstrumentation());
    ReflectionHelpers.setField(activityThread, "mCompatConfiguration", configuration);

    Context systemContextImpl = ReflectionHelpers.callStaticMethod(contextImplClass, "createSystemContext", ClassParameter.from(activityThreadClass, activityThread));

    final Application application = (Application) testLifecycle.createApplication(method, appManifest, config);
    if (application != null) {
      String packageName = appManifest != null ? appManifest.getPackageName() : null;
      if (packageName == null) packageName = DEFAULT_PACKAGE_NAME;

      ApplicationInfo applicationInfo;
      try {
        applicationInfo = RuntimeEnvironment.getPackageManager().getApplicationInfo(packageName, 0);
      } catch (PackageManager.NameNotFoundException e) {
        throw new RuntimeException(e);
      }

      Class<?> compatibilityInfoClass = ReflectionHelpers.loadClass(getClass().getClassLoader(), "android.content.res.CompatibilityInfo");

      Object loadedApk = ReflectionHelpers.callInstanceMethod(activityThread, "getPackageInfo",
          ClassParameter.from(ApplicationInfo.class, applicationInfo),
          ClassParameter.from(compatibilityInfoClass, null),
          ClassParameter.from(int.class, Context.CONTEXT_INCLUDE_CODE));
      
      shadowsAdapter.bind(application, appManifest, resourceLoader);
      if (appManifest == null) {
        // todo: make this cleaner...
        shadowsAdapter.setPackageName(application, applicationInfo.packageName);
      }
      Resources appResources = application.getResources();
      ReflectionHelpers.setField(loadedApk, "mResources", appResources);
      Context contextImpl = ReflectionHelpers.callInstanceMethod(systemContextImpl, "createPackageContext", ClassParameter.from(String.class, applicationInfo.packageName), ClassParameter.from(int.class, Context.CONTEXT_INCLUDE_CODE));
      ReflectionHelpers.setField(activityThread, "mInitialApplication", application);
      ReflectionHelpers.callInstanceMethod(application, "attach", ClassParameter.from(Context.class, contextImpl));

      appResources.updateConfiguration(configuration, appResources.getDisplayMetrics());
      shadowsAdapter.setAssetsQualifiers(appResources.getAssets(), qualifiers);

      RuntimeEnvironment.application = application;
      application.onCreate();
    }
  }

  @Override
  public void tearDownApplication() {
    if (RuntimeEnvironment.application != null) {
      clearFiles(new File(RuntimeEnvironment.application.getApplicationInfo().dataDir));
      RuntimeEnvironment.application.onTerminate();
    }
  }

  private static void clearFiles(File dir) {
    if (dir != null && dir.isDirectory()) {
      File[] files = dir.listFiles();
      if (files != null) {
        for (File f : files) {
          if (f.isDirectory()) {
            clearFiles(f);
          }
          f.delete();
        }
      }
      dir.delete();
      dir.getParentFile().delete();
    }
  }

  @Override
  public Object getCurrentApplication() {
    return RuntimeEnvironment.application;
  }

  @Override
  public void setSdkConfig(SdkConfig sdkConfig) {
    this.sdkConfig = sdkConfig;
  }
}
