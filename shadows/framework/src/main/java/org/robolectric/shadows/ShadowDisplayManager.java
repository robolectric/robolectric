package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR1;

import android.content.res.Configuration;
import android.hardware.display.DisplayManager;
import android.hardware.display.DisplayManagerGlobal;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.DisplayInfo;
import android.view.Surface;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.android.Bootstrap;
import org.robolectric.android.internal.DisplayConfig;
import org.robolectric.annotation.Implements;
import org.robolectric.res.Qualifiers;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.util.Consumer;

/**
 * For tests, display properties may be changed and devices may be added or removed
 * programmatically.
 */
@Implements(value = DisplayManager.class, minSdk = JELLY_BEAN_MR1)
public class ShadowDisplayManager {

  /**
   * Adds a simulated display.
   *
   * @param qualifiersStr the {@link Qualifiers} string representing characteristics of the new
   *     display.
   * @return the new display's ID
   */
  public static int addDisplay(String qualifiersStr) {
    return getShadowDisplayManagerGlobal().addDisplay(createDisplayInfo(qualifiersStr, null));
  }

  /** internal only */
  public static void configureDefaultDisplay(Configuration configuration, DisplayMetrics displayMetrics) {
    ShadowDisplayManagerGlobal shadowDisplayManagerGlobal = getShadowDisplayManagerGlobal();
    if (DisplayManagerGlobal.getInstance().getDisplayIds().length != 0) {
      throw new IllegalStateException("this method should only be called by Robolectric");
    }

    shadowDisplayManagerGlobal.addDisplay(createDisplayInfo(configuration, displayMetrics));
  }

  private static DisplayInfo createDisplayInfo(Configuration configuration, DisplayMetrics displayMetrics) {
    int widthPx = (int) (configuration.screenWidthDp * displayMetrics.density);
    int heightPx = (int) (configuration.screenHeightDp * displayMetrics.density);

    DisplayInfo displayInfo = new DisplayInfo();
    displayInfo.name = "Built-in screen";
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      displayInfo.uniqueId = "screen0";
    }
    displayInfo.appWidth = widthPx;
    displayInfo.appHeight = heightPx;
    fixNominalDimens(displayInfo);
    displayInfo.logicalWidth = widthPx;
    displayInfo.logicalHeight = heightPx;
    displayInfo.rotation = configuration.orientation == Configuration.ORIENTATION_PORTRAIT
        ? Surface.ROTATION_0
        : Surface.ROTATION_90;
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      displayInfo.modeId = 0;
      displayInfo.defaultModeId = 0;
      displayInfo.supportedModes = new Display.Mode[] {
          new Display.Mode(0, widthPx, heightPx, 60)
      };
    }
    displayInfo.logicalDensityDpi = displayMetrics.densityDpi;
    displayInfo.physicalXDpi = displayMetrics.densityDpi;
    displayInfo.physicalYDpi = displayMetrics.densityDpi;
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      displayInfo.state = Display.STATE_ON;
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
      displayInfo.getAppMetrics(displayMetrics);
    }

    return displayInfo;
  }

  private static void fixNominalDimens(DisplayInfo displayInfo) {
    int smallest = Math.min(displayInfo.appWidth, displayInfo.appHeight);
    int largest = Math.max(displayInfo.appWidth, displayInfo.appHeight);

    displayInfo.smallestNominalAppWidth = smallest;
    displayInfo.smallestNominalAppHeight = smallest;
    displayInfo.largestNominalAppWidth = largest;
    displayInfo.largestNominalAppHeight = largest;
  }

  private static DisplayInfo createDisplayInfo(String qualifiersStr, DisplayInfo baseDisplayInfo) {
    Configuration configuration = new Configuration();
    DisplayMetrics displayMetrics = new DisplayMetrics();

    if (qualifiersStr.startsWith("+") && baseDisplayInfo != null) {
      configuration.orientation =
          (baseDisplayInfo.rotation == Surface.ROTATION_0
              || baseDisplayInfo.rotation == Surface.ROTATION_180)
              ? Configuration.ORIENTATION_PORTRAIT
              : Configuration.ORIENTATION_LANDSCAPE;
      configuration.screenWidthDp = baseDisplayInfo.logicalWidth * DisplayMetrics.DENSITY_DEFAULT
          / baseDisplayInfo.logicalDensityDpi;
      configuration.screenHeightDp = baseDisplayInfo.logicalHeight * DisplayMetrics.DENSITY_DEFAULT
          / baseDisplayInfo.logicalDensityDpi;
      configuration.densityDpi = baseDisplayInfo.logicalDensityDpi;
      displayMetrics.densityDpi = baseDisplayInfo.logicalDensityDpi;
      displayMetrics.density =
          baseDisplayInfo.logicalDensityDpi * DisplayMetrics.DENSITY_DEFAULT_SCALE;
    }

    Bootstrap.applyQualifiers(qualifiersStr, RuntimeEnvironment.getApiLevel(), configuration,
        displayMetrics);

    return createDisplayInfo(configuration, displayMetrics);
  }

  /**
   * Changes properties of a simulated display. If `qualifiersStr` starts with a plus (`+`) sign,
   * the display's previous configuration is modified with the given qualifiers; otherwise defaults
   * are applied as described [here](http://robolectric.org/device-configuration/).
   *
   *
   * @param displayId the display id to change
   * @param qualifiersStr the {@link Qualifiers} string representing characteristics of the new
   *     display
   */
  public static void changeDisplay(int displayId, String qualifiersStr) {
    DisplayInfo baseDisplayInfo = DisplayManagerGlobal.getInstance().getDisplayInfo(displayId);
    DisplayInfo displayInfo = createDisplayInfo(qualifiersStr, baseDisplayInfo);
    getShadowDisplayManagerGlobal().changeDisplay(displayId, displayInfo);
  }

  /**
   * Changes properties of a simulated display. The original properties will be passed to the
   * `consumer`, which may modify them in place. The display will be updated with the new
   * properties.
   *
   * @param displayId the display id to change
   * @param consumer a function which modifies the display properties
   */
  static void changeDisplay(int displayId, Consumer<DisplayConfig> consumer) {
    DisplayInfo displayInfo = DisplayManagerGlobal.getInstance().getDisplayInfo(displayId);
    if (displayInfo != null) {
      DisplayConfig displayConfig = new DisplayConfig(displayInfo);
      consumer.accept(displayConfig);
      displayConfig.copyTo(displayInfo);
      fixNominalDimens(displayInfo);
    }

    getShadowDisplayManagerGlobal().changeDisplay(displayId, displayInfo);
  }

  /**
   * Removes a simulated display.
   *
   * @param displayId the display id to remove
   */
  public static void removeDisplay(int displayId) {
    getShadowDisplayManagerGlobal().removeDisplay(displayId);
  }

  private static ShadowDisplayManagerGlobal getShadowDisplayManagerGlobal() {
    if (Build.VERSION.SDK_INT < JELLY_BEAN_MR1) {
      throw new UnsupportedOperationException("multiple displays not supported in Jelly Bean");
    }

    return Shadow.extract(DisplayManagerGlobal.getInstance());
  }
}
