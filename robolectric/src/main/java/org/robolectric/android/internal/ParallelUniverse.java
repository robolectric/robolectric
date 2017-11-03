package org.robolectric.android.internal;

import static android.content.res.Configuration.DENSITY_DPI_ANY;
import static android.content.res.Configuration.DENSITY_DPI_NONE;
import static android.content.res.Configuration.DENSITY_DPI_UNDEFINED;
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
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.Handler;
import android.os.LocaleList;
import android.os.Looper;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import com.google.common.annotations.VisibleForTesting;
import java.lang.reflect.Method;
import java.security.Security;
import java.util.ArrayList;
import java.util.List;
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
import org.robolectric.res.ResourceTable;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.android.Bootstrap;
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

    Resources systemResources = Resources.getSystem();
    Configuration configuration = systemResources.getConfiguration();
    DisplayMetrics displayMetrics = systemResources.getDisplayMetrics();

    String newQualifiers = Bootstrap.applySystemConfiguration(config.qualifiers(),
        sdkConfig.getApiLevel(), configuration, displayMetrics);
    RuntimeEnvironment.setQualifiers(newQualifiers);

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

  @VisibleForTesting
  String applySystemConfiguration(Configuration configuration,
      DisplayMetrics displayMetrics, String qualifiers) {
    ConfigDescription configDescription = new ConfigDescription();
    ResTable_config resTab = new ResTable_config();

    if (Qualifiers.getPlatformVersion(qualifiers) != -1) {
      throw new IllegalArgumentException("Cannot specify platform version in qualifiers: \"" + qualifiers + "\"");
    }

    if (!qualifiers.isEmpty() && !configDescription.parse(qualifiers, resTab)) {
      throw new IllegalArgumentException("Invalid qualifiers \"" + qualifiers + "\"");
    }

    if (resTab.smallestScreenWidthDp == 0) {
      resTab.smallestScreenWidthDp = 320;
    }

    if (resTab.screenWidthDp == 0) {
      resTab.screenWidthDp = 320;
    }

    configuration.smallestScreenWidthDp = resTab.smallestScreenWidthDp;
    configuration.screenWidthDp = resTab.screenWidthDp;
    configuration.orientation = resTab.orientation;

    // begin new stuff
    configuration.mcc = resTab.mcc;
    configuration.mnc = resTab.mnc;
    configuration.screenLayout = resTab.screenLayout | (resTab.screenLayout2 << 8);
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
    } else {
      //displayMetrics.density = ((float) resTab.density) / 160;
    }
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

    return resourceQualifierString(configuration);
  }

  public static String localesToResourceQualifier(List<Locale> locs) {
    final StringBuilder sb = new StringBuilder();
    for (int i = 0; i < locs.size(); i++) {
      final Locale loc = locs.get(i);
      final int l = loc.getLanguage().length();
      if (l == 0) {
        continue;
      }
      final int s = loc.getScript().length();
      final int c = loc.getCountry().length();
      final int v = loc.getVariant().length();
      // We ignore locale extensions, since they are not supported by AAPT

      if (sb.length() != 0) {
        sb.append(",");
      }
      if (l == 2 && s == 0 && (c == 0 || c == 2) && v == 0) {
        // Traditional locale format: xx or xx-rYY
        sb.append(loc.getLanguage());
        if (c == 2) {
          sb.append("-r").append(loc.getCountry());
        }
      } else {
        sb.append("b+");
        sb.append(loc.getLanguage());
        if (s != 0) {
          sb.append("+");
          sb.append(loc.getScript());
        }
        if (c != 0) {
          sb.append("+");
          sb.append(loc.getCountry());
        }
        if (v != 0) {
          sb.append("+");
          sb.append(loc.getVariant());
        }
      }
    }
    return sb.toString();
  }

  /**
   * Returns a string representation of the configuration that can be parsed
   * by build tools (like AAPT).
   *
   * @hide
   */
  public static String resourceQualifierString(Configuration config) {
    List<String> parts = new ArrayList<String>();

    if (config.mcc != 0) {
      parts.add("mcc" + config.mcc);
      if (config.mnc != 0) {
        parts.add("mnc" + config.mnc);
      }
    }

    List<Locale> locales = new ArrayList<>();
    if (RuntimeEnvironment.getApiLevel() > Build.VERSION_CODES.M) {
      LocaleList localeList = config.getLocales();
      for (int i = 0; i < localeList.size(); i++) {
        locales.add(l)
      }

    }
    if (!locales.isEmpty()) {
      final String resourceQualifier = localesToResourceQualifier(locales);
      if (!resourceQualifier.isEmpty()) {
        parts.add(resourceQualifier);
      }
    }

    switch (config.screenLayout & Configuration.SCREENLAYOUT_LAYOUTDIR_MASK) {
      case Configuration.SCREENLAYOUT_LAYOUTDIR_LTR:
        parts.add("ldltr");
        break;
      case Configuration.SCREENLAYOUT_LAYOUTDIR_RTL:
        parts.add("ldrtl");
        break;
      default:
        break;
    }

    if (config.smallestScreenWidthDp != 0) {
      parts.add("sw" + config.smallestScreenWidthDp + "dp");
    }

    if (config.screenWidthDp != 0) {
      parts.add("w" + config.screenWidthDp + "dp");
    }

    if (config.screenHeightDp != 0) {
      parts.add("h" + config.screenHeightDp + "dp");
    }

    switch (config.screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) {
      case Configuration.SCREENLAYOUT_SIZE_SMALL:
        parts.add("small");
        break;
      case Configuration.SCREENLAYOUT_SIZE_NORMAL:
        parts.add("normal");
        break;
      case Configuration.SCREENLAYOUT_SIZE_LARGE:
        parts.add("large");
        break;
      case Configuration.SCREENLAYOUT_SIZE_XLARGE:
        parts.add("xlarge");
        break;
      default:
        break;
    }

    switch (config.screenLayout & Configuration.SCREENLAYOUT_LONG_MASK) {
      case Configuration.SCREENLAYOUT_LONG_YES:
        parts.add("long");
        break;
      case Configuration.SCREENLAYOUT_LONG_NO:
        parts.add("notlong");
        break;
      default:
        break;
    }

    switch (config.screenLayout & Configuration.SCREENLAYOUT_ROUND_MASK) {
      case Configuration.SCREENLAYOUT_ROUND_YES:
        parts.add("round");
        break;
      case Configuration.SCREENLAYOUT_ROUND_NO:
        parts.add("notround");
        break;
      default:
        break;
    }

    switch (config.orientation) {
      case Configuration.ORIENTATION_LANDSCAPE:
        parts.add("land");
        break;
      case Configuration.ORIENTATION_PORTRAIT:
        parts.add("port");
        break;
      default:
        break;
    }

    switch (config.uiMode & Configuration.UI_MODE_TYPE_MASK) {
      case Configuration.UI_MODE_TYPE_APPLIANCE:
        parts.add("appliance");
        break;
      case Configuration.UI_MODE_TYPE_DESK:
        parts.add("desk");
        break;
      case Configuration.UI_MODE_TYPE_TELEVISION:
        parts.add("television");
        break;
      case Configuration.UI_MODE_TYPE_CAR:
        parts.add("car");
        break;
      case Configuration.UI_MODE_TYPE_WATCH:
        parts.add("watch");
        break;
      default:
        break;
    }

    switch (config.uiMode & Configuration.UI_MODE_NIGHT_MASK) {
      case Configuration.UI_MODE_NIGHT_YES:
        parts.add("night");
        break;
      case Configuration.UI_MODE_NIGHT_NO:
        parts.add("notnight");
        break;
      default:
        break;
    }

    switch (config.densityDpi) {
      case DENSITY_DPI_UNDEFINED:
        break;
      case 120:
        parts.add("ldpi");
        break;
      case 160:
        parts.add("mdpi");
        break;
      case 213:
        parts.add("tvdpi");
        break;
      case 240:
        parts.add("hdpi");
        break;
      case 320:
        parts.add("xhdpi");
        break;
      case 480:
        parts.add("xxhdpi");
        break;
      case 640:
        parts.add("xxxhdpi");
        break;
      case DENSITY_DPI_ANY:
        parts.add("anydpi");
        break;
      case DENSITY_DPI_NONE:
        parts.add("nodpi");
      default:
        parts.add(config.densityDpi + "dpi");
        break;
    }

    switch (config.touchscreen) {
      case Configuration.TOUCHSCREEN_NOTOUCH:
        parts.add("notouch");
        break;
      case Configuration.TOUCHSCREEN_FINGER:
        parts.add("finger");
        break;
      default:
        break;
    }

    switch (config.keyboardHidden) {
      case Configuration.KEYBOARDHIDDEN_NO:
        parts.add("keysexposed");
        break;
      case Configuration.KEYBOARDHIDDEN_YES:
        parts.add("keyshidden");
        break;
      case Configuration.KEYBOARDHIDDEN_SOFT:
        parts.add("keyssoft");
        break;
      default:
        break;
    }

    switch (config.keyboard) {
      case Configuration.KEYBOARD_NOKEYS:
        parts.add("nokeys");
        break;
      case Configuration.KEYBOARD_QWERTY:
        parts.add("qwerty");
        break;
      case Configuration.KEYBOARD_12KEY:
        parts.add("12key");
        break;
      default:
        break;
    }

    switch (config.navigationHidden) {
      case Configuration.NAVIGATIONHIDDEN_NO:
        parts.add("navexposed");
        break;
      case Configuration.NAVIGATIONHIDDEN_YES:
        parts.add("navhidden");
        break;
      default:
        break;
    }

    switch (config.navigation) {
      case Configuration.NAVIGATION_NONAV:
        parts.add("nonav");
        break;
      case Configuration.NAVIGATION_DPAD:
        parts.add("dpad");
        break;
      case Configuration.NAVIGATION_TRACKBALL:
        parts.add("trackball");
        break;
      case Configuration.NAVIGATION_WHEEL:
        parts.add("wheel");
        break;
      default:
        break;
    }

    parts.add("v" + Build.VERSION.RESOURCES_SDK_INT);
    return TextUtils.join("-", parts);
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