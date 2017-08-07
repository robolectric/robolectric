package org.robolectric.android.internal;

import static org.robolectric.util.ReflectionHelpers.ClassParameter;

import android.app.ActivityThread;
import android.app.Application;
import android.app.LoadedApk;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import com.android.ide.common.rendering.api.HardwareConfig;
import com.android.ide.common.rendering.api.SessionParams;
import com.android.ide.common.resources.ResourceItem;
import com.android.ide.common.resources.ResourceRepository;
import com.android.ide.common.resources.ResourceResolver;
import com.android.ide.common.resources.configuration.FolderConfiguration;
import com.android.io.FolderWrapper;
import com.android.layoutlib.bridge.android.BridgeContext;
import com.android.layoutlib.bridge.impl.RenderAction;
import com.android.resources.Density;
import com.android.resources.Keyboard;
import com.android.resources.KeyboardState;
import com.android.resources.Navigation;
import com.android.resources.NavigationState;
import com.android.resources.ScreenOrientation;
import com.android.resources.ScreenRatio;
import com.android.resources.ScreenSize;
import com.android.resources.TouchScreen;
import java.io.File;
import java.io.FileNotFoundException;
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
import org.robolectric.fakes.ProjectCallback;
import org.robolectric.fakes.RenderService;
import org.robolectric.fakes.RenderServiceFactory;
import org.robolectric.internal.ParallelUniverseInterface;
import org.robolectric.internal.SdkConfig;
import org.robolectric.manifest.AndroidManifest;
import org.robolectric.manifest.RoboNotFoundException;
import org.robolectric.res.Qualifiers;
import org.robolectric.res.ResourceTable;
import org.robolectric.shadows.ShadowBridgeContext;
import org.robolectric.shadows.ShadowLooper;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.Scheduler;
import org.robolectric.util.TempDirectory;
import org.xmlpull.v1.XmlPullParserException;

public class ParallelUniverse implements ParallelUniverseInterface {
  private static final String DEFAULT_PACKAGE_NAME = "org.robolectric.default";
  private static RenderService renderService;
  private static ResourceResolver renderResources;
  private final RobolectricTestRunner robolectricTestRunner;
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
      throw new Resources.NotFoundException(e1.getMessage(), e1);
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
    RuntimeEnvironment.setRendering(sdkConfig.isRendering());

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

      ApplicationInfo applicationInfo;
      try {
        applicationInfo = systemContextImpl.getPackageManager().getApplicationInfo(appManifest.getPackageName(), 0);
      } catch (PackageManager.NameNotFoundException e) {
        throw new RuntimeException(e);
      }

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

      appResources.updateConfiguration(configuration, appResources.getDisplayMetrics());

      if (sdkConfig.isRendering()) {
        final String SDK = System.getenv("ANDROID_HOME");
        File f = new File(SDK + "/platforms/android-" + sdkConfig.getApiLevel());
        RenderServiceFactory factory = RenderServiceFactory.create(f);
        String appResDir = "";
        if (appManifest != null) {
          appResDir = appManifest.getResDirectory().getPath();
          RuntimeEnvironment.setResourceDir(appResDir);
        }
        ResourceRepository projectRes =
            new ResourceRepository(new FolderWrapper(appResDir), false/*isFramework*/) {
              @Override
              protected ResourceItem createResourceItem(String name) {
                return new ResourceItem(name);
              }
            };
        projectRes.loadResources();
        // create the rendering config
        FolderConfiguration folderConfig =
            RenderServiceFactory.createConfig(1280, 800, ScreenSize.XLARGE, ScreenRatio.LONG,
                ScreenOrientation.PORTRAIT, Density.MEDIUM, TouchScreen.FINGER, KeyboardState.SOFT,
                Keyboard.QWERTY, NavigationState.EXPOSED, Navigation.NONAV, 21/*api level*/);
        // create the resource resolver once for the given config.
        String themeKey = null;
        boolean isProjectTheme;
        if (appManifest.getThemeRef() != null && appManifest.getThemeRef().length() > 1) {
          String themeRef = appManifest.getThemeRef().substring(1); //Remove '@'
          if (themeRef.contains("android")) {
            // Framework theme
            isProjectTheme = false;
          } else {
            // Project theme
            isProjectTheme = true;
          }
          int indexOfSlash = themeRef.indexOf('/');
          if (indexOfSlash >= 0) {
            themeKey = themeRef.substring(indexOfSlash + 1);
          } else {
            themeKey = "Theme";
            isProjectTheme = false;
          }
        } else {
          isProjectTheme = false;
          themeKey = "Theme";
        }

        // create the render service
        if (renderService == null) {
          renderResources = factory.createResourceResolver(folderConfig, projectRes, themeKey, isProjectTheme);
          renderService = factory.createService(renderResources, folderConfig, new ProjectCallback());
        }

        try {
          initBridgeResources("activity_main");
          } catch (FileNotFoundException e) {
          e.printStackTrace();
        } catch (XmlPullParserException e) {
          e.printStackTrace();
        }
      }

      application.onCreate();
    } else if (sdkConfig.isRendering() && appManifest == null) {
      throw new RuntimeException("Please provide a manifest for rendering");
    }
  }

  public static void initBridgeResources(String layoutName) throws FileNotFoundException, XmlPullParserException {
    SessionParams params;
    params = renderService.getParamsFromFile(layoutName);
    HardwareConfig hardwareConfig = params.getHardwareConfig();

    DisplayMetrics metrics = new DisplayMetrics();
    metrics.densityDpi = (metrics.noncompatDensityDpi = hardwareConfig.getDensity().getDpiValue());

    metrics.density = (metrics.noncompatDensity = metrics.densityDpi / 160.0F);

    metrics.scaledDensity = (metrics.noncompatScaledDensity = metrics.density);

    metrics.widthPixels = (metrics.noncompatWidthPixels = hardwareConfig.getScreenWidth());
    metrics.heightPixels = (metrics.noncompatHeightPixels = hardwareConfig.getScreenHeight());
    metrics.xdpi = (metrics.noncompatXdpi = hardwareConfig.getXdpi());
    metrics.ydpi = (metrics.noncompatYdpi = hardwareConfig.getYdpi());
    BridgeContext bridgeContext =
        ShadowBridgeContext.obtain(params.getProjectKey(), metrics, renderResources, null, params.getProjectCallback(),
            getConfiguration(params), params.getTargetSdkVersion(), params.isRtlSupported());
    bridgeContext.initResources();
    ReflectionHelpers.setStaticField(RenderAction.class, "sCurrentContext", bridgeContext);
  }

  private static Configuration getConfiguration(SessionParams params) {
    Configuration config = new Configuration();

    HardwareConfig hardwareConfig = params.getHardwareConfig();

    ScreenSize screenSize = hardwareConfig.getScreenSize();
    if (screenSize != null) {
      switch (screenSize)
      {
      case SMALL:
        config.screenLayout |= 0x1;
        break;
      case NORMAL:
        config.screenLayout |= 0x2;
        break;
      case LARGE:
        config.screenLayout |= 0x3;
        break;
      case XLARGE:
        config.screenLayout |= 0x4;
      }
    }
    Density density = hardwareConfig.getDensity();
    if (density == null) {
      density = Density.MEDIUM;
    }
    config.screenWidthDp = (hardwareConfig.getScreenWidth() / density.getDpiValue());
    config.screenHeightDp = (hardwareConfig.getScreenHeight() / density.getDpiValue());
    if (config.screenHeightDp < config.screenWidthDp) {
      config.smallestScreenWidthDp = config.screenHeightDp;
    } else {
      config.smallestScreenWidthDp = config.screenWidthDp;
    }
    config.densityDpi = density.getDpiValue();

    config.compatScreenWidthDp = config.screenWidthDp;
    config.compatScreenHeightDp = config.screenHeightDp;

    ScreenOrientation orientation = hardwareConfig.getOrientation();
    if (orientation != null) {
      switch (orientation)
      {
      case PORTRAIT:
        config.orientation = 1;
        break;
      case LANDSCAPE:
        config.orientation = 2;
        break;
      case SQUARE:
        config.orientation = 3;
      }
    } else {
      config.orientation = 0;
    }
    return config;
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
