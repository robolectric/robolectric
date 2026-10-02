package org.robolectric.android.internal;

import android.app.WindowConfiguration;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.hardware.display.DisplayManagerGlobal;
import android.os.Build.VERSION_CODES;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.DisplayInfo;
import javax.annotation.Nullable;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.android.DeviceConfig.ScreenSize;

/**
 * Computes the configuration the window manager gives windows on a display.
 *
 * <p>Robolectric internal, do not use.
 */
public final class WindowConfigurations {
  private WindowConfigurations() {}

  /**
   * Returns how the configuration of a window filling the given display differs from the global
   * configuration, for use as an override applied with {@link Configuration#updateFrom}.
   *
   * <p>Returns null for the default display, whose windows use the global configuration, for a
   * display that does not exist, and before O, where activities can't be launched on other
   * displays.
   */
  @Nullable
  public static Configuration getDisplayOverrideConfiguration(int displayId) {
    if (displayId == Display.DEFAULT_DISPLAY
        || displayId == Display.INVALID_DISPLAY
        || RuntimeEnvironment.getApiLevel() < VERSION_CODES.O) {
      return null;
    }
    DisplayInfo displayInfo = DisplayManagerGlobal.getInstance().getDisplayInfo(displayId);
    if (displayInfo == null) {
      return null;
    }
    Configuration configuration =
        createOverrideConfiguration(
            displayId, displayInfo, displayInfo.logicalWidth, displayInfo.logicalHeight);
    if (RuntimeEnvironment.getApiLevel() >= VERSION_CODES.P) {
      Rect bounds = new Rect(0, 0, displayInfo.logicalWidth, displayInfo.logicalHeight);
      setBounds(configuration.windowConfiguration, bounds, bounds);
    }
    return configuration;
  }

  private static Configuration createOverrideConfiguration(
      int displayId, DisplayInfo displayInfo, int widthPx, int heightPx) {
    Configuration configuration = new Configuration();
    configuration.unset();
    float density = displayInfo.logicalDensityDpi / (float) DisplayMetrics.DENSITY_DEFAULT;
    int widthDp = (int) (widthPx / density + 0.5f);
    int heightDp = (int) (heightPx / density + 0.5f);
    configuration.densityDpi = displayInfo.logicalDensityDpi;
    configuration.screenWidthDp = widthDp;
    configuration.screenHeightDp = heightDp;
    configuration.smallestScreenWidthDp = Math.min(widthDp, heightDp);
    configuration.orientation =
        widthDp > heightDp
            ? Configuration.ORIENTATION_LANDSCAPE
            : Configuration.ORIENTATION_PORTRAIT;
    configuration.screenLayout =
        getScreenLayoutSize(widthDp, heightDp)
            | (Math.max(widthDp, heightDp) >= 1.75f * Math.min(widthDp, heightDp)
                ? Configuration.SCREENLAYOUT_LONG_YES
                : Configuration.SCREENLAYOUT_LONG_NO)
            | Configuration.SCREENLAYOUT_ROUND_NO;
    if (displayId != Display.DEFAULT_DISPLAY
        && RuntimeEnvironment.getApiLevel() < VERSION_CODES.S) {
      // Before S, resources on another display report that it has no touchscreen.
      configuration.touchscreen = Configuration.TOUCHSCREEN_NOTOUCH;
    }
    return configuration;
  }

  /**
   * Returns the screen layout size of a window with the given size, as the qualifiers define it.
   */
  private static int getScreenLayoutSize(int widthDp, int heightDp) {
    int shortSide = Math.min(widthDp, heightDp);
    int longSide = Math.max(widthDp, heightDp);
    ScreenSize screenSize = ScreenSize.small;
    for (ScreenSize size : ScreenSize.values()) {
      if (size.width <= shortSide && size.height <= longSide) {
        screenSize = size;
      }
    }
    switch (screenSize) {
      case xlarge:
        return Configuration.SCREENLAYOUT_SIZE_XLARGE;
      case large:
        return Configuration.SCREENLAYOUT_SIZE_LARGE;
      case normal:
        return Configuration.SCREENLAYOUT_SIZE_NORMAL;
      default:
        return Configuration.SCREENLAYOUT_SIZE_SMALL;
    }
  }

  private static void setBounds(
      WindowConfiguration windowConfiguration, Rect bounds, Rect displayBounds) {
    windowConfiguration.setBounds(bounds);
    windowConfiguration.setAppBounds(bounds);
    if (RuntimeEnvironment.getApiLevel() >= VERSION_CODES.S
        && Boolean.parseBoolean(
            System.getProperty("robolectric.deviceconfig.useMaxBounds", "true"))) {
      windowConfiguration.setMaxBounds(displayBounds);
    }
  }
}
