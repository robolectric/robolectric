package org.robolectric.fakes;

import com.android.SdkConstants;
import com.android.ide.common.rendering.LayoutLibrary;
import com.android.ide.common.rendering.api.AttrResourceValue;
import com.android.ide.common.rendering.api.DeclareStyleableResourceValue;
import com.android.ide.common.rendering.api.IProjectCallback;
import com.android.ide.common.rendering.api.ResourceValue;
import com.android.ide.common.resources.FrameworkResources;
import com.android.ide.common.resources.ResourceRepository;
import com.android.ide.common.resources.ResourceResolver;
import com.android.ide.common.resources.configuration.DensityQualifier;
import com.android.ide.common.resources.configuration.FolderConfiguration;
import com.android.ide.common.resources.configuration.KeyboardStateQualifier;
import com.android.ide.common.resources.configuration.NavigationMethodQualifier;
import com.android.ide.common.resources.configuration.NavigationStateQualifier;
import com.android.ide.common.resources.configuration.ScreenDimensionQualifier;
import com.android.ide.common.resources.configuration.ScreenHeightQualifier;
import com.android.ide.common.resources.configuration.ScreenOrientationQualifier;
import com.android.ide.common.resources.configuration.ScreenRatioQualifier;
import com.android.ide.common.resources.configuration.ScreenSizeQualifier;
import com.android.ide.common.resources.configuration.ScreenWidthQualifier;
import com.android.ide.common.resources.configuration.SmallestScreenWidthQualifier;
import com.android.ide.common.resources.configuration.TextInputMethodQualifier;
import com.android.ide.common.resources.configuration.TouchScreenQualifier;
import com.android.ide.common.resources.configuration.VersionQualifier;
import com.android.ide.common.sdk.LoadStatus;
import com.android.io.FileWrapper;
import com.android.io.FolderWrapper;
import com.android.resources.Density;
import com.android.resources.Keyboard;
import com.android.resources.KeyboardState;
import com.android.resources.Navigation;
import com.android.resources.NavigationState;
import com.android.resources.ResourceType;
import com.android.resources.ScreenOrientation;
import com.android.resources.ScreenRatio;
import com.android.resources.ScreenSize;
import com.android.resources.TouchScreen;
import com.android.sdklib.internal.project.ProjectProperties;
import com.android.utils.ILogger;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import org.robolectric.shadows.ShadowLayoutLibrary;

public class RenderServiceFactory {
  private LayoutLibrary library;

  private FrameworkResources resources;

  public static RenderServiceFactory create(File platformFolder) {
    // create the factory
    RenderServiceFactory factory = new RenderServiceFactory();
    if (factory.loadLibrary(platformFolder)) {
      return factory;
    }
    return null;
  }

  public static FolderConfiguration createConfig(int w, int h, ScreenSize screenSize,
      ScreenRatio screenRatio, ScreenOrientation orientation, Density density,
      TouchScreen touchScreen, KeyboardState keyboardState, Keyboard keyboard,
      NavigationState navigationState, Navigation navigation, int apiLevel) {
    FolderConfiguration config = new FolderConfiguration();
    int width = w, height = h;
    switch (orientation) {
      case LANDSCAPE:
        width = w < h ? h : w;
        height = w < h ? w : h;
        break;
      case PORTRAIT:
        width = w < h ? w : h;
        height = w < h ? h : w;
        break;
      case SQUARE:
        width = height = w;
        break;
    }
    int wdp = (width * Density.DEFAULT_DENSITY) / density.getDpiValue();
    int hdp = (height * Density.DEFAULT_DENSITY) / density.getDpiValue();
    config.addQualifier(new SmallestScreenWidthQualifier(wdp < hdp ? wdp : hdp));
    config.addQualifier(new ScreenWidthQualifier(wdp));
    config.addQualifier(new ScreenHeightQualifier(hdp));
    config.addQualifier(new ScreenSizeQualifier(screenSize));
    config.addQualifier(new ScreenRatioQualifier(screenRatio));
    config.addQualifier(new ScreenOrientationQualifier(orientation));
    config.addQualifier(new DensityQualifier(density));
    config.addQualifier(new TouchScreenQualifier(touchScreen));
    config.addQualifier(new KeyboardStateQualifier(keyboardState));
    config.addQualifier(new TextInputMethodQualifier(keyboard));
    config.addQualifier(new NavigationStateQualifier(navigationState));
    config.addQualifier(new NavigationMethodQualifier(navigation));
    config.addQualifier(
        width > height
            ? new ScreenDimensionQualifier(width, height)
            : new ScreenDimensionQualifier(height, width));
    config.addQualifier(new VersionQualifier(apiLevel));
    config.updateScreenWidthAndHeight();
    return config;
  }

  public ResourceResolver createResourceResolver(FolderConfiguration config,
      ResourceRepository projectResources, String themeName, boolean isProjectTheme) {
    Map<ResourceType, Map<String, ResourceValue>> configedProjectRes =
        projectResources.getConfiguredResources(config);
    Map<ResourceType, Map<String, ResourceValue>> configedFrameworkRes =
        resources.getConfiguredResources(config);
    return ResourceResolver.create(
        configedProjectRes, configedFrameworkRes, themeName, isProjectTheme);
  }

  public RenderService createService(
      ResourceResolver resources, FolderConfiguration config, IProjectCallback projectCallback) {
    RenderService renderService = new RenderService(library, resources, config, projectCallback);
    return renderService;
  }

  public RenderService createService(ResourceRepository projectResources, String themeName,
      boolean isProjectTheme, FolderConfiguration config, IProjectCallback projectCallback) {
    ResourceResolver resources =
        createResourceResolver(config, projectResources, themeName, isProjectTheme);
    RenderService renderService = new RenderService(library, resources, config, projectCallback);
    return renderService;
  }

  private RenderServiceFactory() {}

  private boolean loadLibrary(File platformFolder) {
    if (platformFolder.isDirectory() == false) {
      throw new IllegalArgumentException("platform folder does not exist.");
    }
    File dataFolder = new File(platformFolder, "data");
    if (dataFolder.isDirectory() == false) {
      throw new IllegalArgumentException("platform data folder does not exist.");
    }
    File layoutLibJar = new File(dataFolder, "layoutlib.jar");
    if (layoutLibJar.isFile() == false) {
      throw new IllegalArgumentException("platform layoutlib.jar does not exist.");
    }
    File resFolder = new File(dataFolder, "res");
    if (resFolder.isDirectory() == false) {
      throw new IllegalArgumentException("platform res folder does not exist.");
    }
    File fontFolder = new File(dataFolder, "fonts");
    if (fontFolder.isDirectory() == false) {
      throw new IllegalArgumentException("platform font folder does not exist.");
    }
    FileWrapper buildProp = new FileWrapper(platformFolder, SdkConstants.FN_BUILD_PROP);
    if (buildProp.isFile() == false) {
      throw new IllegalArgumentException("platform build.prop does not exist.");
    }
    StdOutLogger log = new StdOutLogger();
    library = ShadowLayoutLibrary.load(layoutLibJar.getAbsolutePath(), log, "LayoutLibRenderer");
    if (library.getStatus() != LoadStatus.LOADED) {
      throw new IllegalArgumentException(library.getLoadMessage());
    }
    // load the framework resources
    resources = loadResources(resFolder, log);
    // get all the attr values.
    HashMap<String, Map<String, Integer>> enumMap = new HashMap<String, Map<String, Integer>>();
    FolderConfiguration config = new FolderConfiguration();
    Map<ResourceType, Map<String, ResourceValue>> res = resources.getConfiguredResources(config);
    // get the ATTR values
    Map<String, ResourceValue> attrItems = res.get(ResourceType.ATTR);
    for (ResourceValue value : attrItems.values()) {
      if (value instanceof AttrResourceValue) {
        AttrResourceValue attr = (AttrResourceValue) value;
        Map<String, Integer> values = attr.getAttributeValues();
        if (values != null) {
          enumMap.put(attr.getName(), values);
        }
      }
    }
    // get the declare-styleable values
    Map<String, ResourceValue> styleableItems = res.get(ResourceType.DECLARE_STYLEABLE);
    // get the attr from the styleable
    for (ResourceValue value : styleableItems.values()) {
      if (value instanceof DeclareStyleableResourceValue) {
        DeclareStyleableResourceValue dsrc = (DeclareStyleableResourceValue) value;
        Map<String, AttrResourceValue> attrs = dsrc.getAllAttributes();
        if (attrs != null && attrs.size() > 0) {
          for (AttrResourceValue attr : attrs.values()) {
            Map<String, Integer> values = attr.getAttributeValues();
            if (values != null) {
              enumMap.put(attr.getName(), values);
            }
          }
        }
      }
    }
    // we need to parse the build.prop for this
    Map<String, String> buildPropMap = ProjectProperties.parsePropertyFile(buildProp, log);
    return library.init(buildPropMap, fontFolder, enumMap, log);
  }

  private FrameworkResources loadResources(File resFolder, ILogger log) {
    FolderWrapper path = new FolderWrapper(resFolder);
    FrameworkResources resources = new FrameworkResources(path);
    resources.loadResources();
    resources.loadPublicResources(log);
    return resources;
  }
}
