package org.robolectric.android.internal;

import android.app.WindowConfiguration;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.hardware.display.DisplayManagerGlobal;
import android.os.Build.VERSION_CODES;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.DisplayInfo;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.android.DeviceConfig.ScreenSize;
import org.robolectric.util.ReflectionHelpers;

/**
 * Computes the configuration the window manager gives windows: a window filling a display, a
 * freeform window, or one half of split screen.
 *
 * <p>Robolectric internal, do not use.
 */
public final class WindowConfigurations {
  /** The size of the divider between the halves of split screen, as on a device. */
  private static final int SPLIT_SCREEN_DIVIDER_SIZE_DP = 10;

  private static final Map<Integer, Float> splitScreenDividerPositions = new HashMap<>();

  private WindowConfigurations() {}

  /** Forgets where the divider of split screen was moved on each display. */
  public static void reset() {
    splitScreenDividerPositions.clear();
  }

  /**
   * Moves the divider of split screen on the given display to the given fraction of the display's
   * width, or of its height if it is portrait.
   */
  public static void setSplitScreenDividerPosition(int displayId, float position) {
    splitScreenDividerPositions.put(displayId, position);
  }

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

  /**
   * Returns the configuration of a window filling the given display, given the global
   * configuration.
   */
  public static Configuration getDisplayConfiguration(
      int displayId, Configuration globalConfiguration) {
    return withOverride(globalConfiguration, getDisplayOverrideConfiguration(displayId));
  }

  /**
   * Returns the display metrics of a window filling the given display, given the global display
   * metrics.
   */
  @Nullable
  public static DisplayMetrics getDisplayMetrics(
      int displayId, @Nullable DisplayMetrics globalMetrics) {
    if (getDisplayOverrideConfiguration(displayId) == null) {
      return globalMetrics;
    }
    DisplayMetrics displayMetrics = new DisplayMetrics();
    DisplayManagerGlobal.getInstance().getRealDisplay(displayId).getMetrics(displayMetrics);
    return displayMetrics;
  }

  /** Returns the display metrics of a window with the given bounds, given the display's. */
  @Nullable
  public static DisplayMetrics getWindowMetrics(
      @Nullable DisplayMetrics displayMetrics, Rect windowBounds) {
    if (displayMetrics == null) {
      return null;
    }
    DisplayMetrics windowMetrics = new DisplayMetrics();
    windowMetrics.setTo(displayMetrics);
    windowMetrics.widthPixels = windowMetrics.noncompatWidthPixels = windowBounds.width();
    windowMetrics.heightPixels = windowMetrics.noncompatHeightPixels = windowBounds.height();
    return windowMetrics;
  }

  /**
   * Returns how the configuration of an activity in a freeform window with the given bounds on the
   * given display differs from the global configuration. Returns null for a display that does not
   * exist, and before P.
   */
  @Nullable
  public static Configuration getFreeformOverrideConfiguration(int displayId, Rect bounds) {
    return getWindowOverrideConfiguration(
        displayId, bounds, WindowConfiguration.WINDOWING_MODE_FREEFORM);
  }

  /**
   * Returns how the configuration of an activity in the system's split screen differs from the
   * global configuration. As the system does, the display is split along its longer side by a
   * divider, and the activity is in its top or left half, or in its bottom or right half. Returns
   * null for a display that does not exist, and before P.
   */
  @Nullable
  public static Configuration getSplitScreenOverrideConfiguration(
      int displayId, boolean topOrLeft) {
    if (RuntimeEnvironment.getApiLevel() < VERSION_CODES.P) {
      return null;
    }
    DisplayInfo displayInfo = DisplayManagerGlobal.getInstance().getDisplayInfo(displayId);
    if (displayInfo == null) {
      return null;
    }
    int width = displayInfo.logicalWidth;
    int height = displayInfo.logicalHeight;
    int dividerSize =
        Math.round(
            SPLIT_SCREEN_DIVIDER_SIZE_DP
                * displayInfo.logicalDensityDpi
                / (float) DisplayMetrics.DENSITY_DEFAULT);
    float dividerPosition = splitScreenDividerPositions.getOrDefault(displayId, 0.5f);
    Rect bounds = new Rect(0, 0, width, height);
    if (width > height) {
      int dividerLeft = Math.round(width * dividerPosition - dividerSize / 2f);
      if (topOrLeft) {
        bounds.right = dividerLeft;
      } else {
        bounds.left = dividerLeft + dividerSize;
      }
    } else {
      int dividerTop = Math.round(height * dividerPosition - dividerSize / 2f);
      if (topOrLeft) {
        bounds.bottom = dividerTop;
      } else {
        bounds.top = dividerTop + dividerSize;
      }
    }
    return getWindowOverrideConfiguration(
        displayId, bounds, getSplitScreenWindowingMode(topOrLeft));
  }

  /**
   * Returns how the configuration of an activity with the given configuration differs from the
   * global configuration if it keeps its window on the given display: a freeform window keeps its
   * bounds, and split screen its half of the display. Returns null if its window fills the display.
   */
  @Nullable
  public static Configuration getCurrentWindowOverrideConfiguration(
      int displayId, Configuration activityConfiguration) {
    if (RuntimeEnvironment.getApiLevel() < VERSION_CODES.P) {
      return null;
    }
    WindowConfiguration windowConfiguration = activityConfiguration.windowConfiguration;
    if (windowConfiguration.getWindowingMode() == WindowConfiguration.WINDOWING_MODE_FREEFORM) {
      return getFreeformOverrideConfiguration(displayId, windowConfiguration.getBounds());
    }
    if (isInSplitScreen(activityConfiguration)) {
      return getSplitScreenOverrideConfiguration(
          displayId, isInTopOrLeftOfSplitScreen(activityConfiguration));
    }
    return null;
  }

  /**
   * Returns the configuration of an activity with the given configuration on the given display,
   * given the global configuration, if it keeps its window.
   */
  public static Configuration getActivityConfiguration(
      int displayId, Configuration activityConfiguration, Configuration globalConfiguration) {
    Configuration windowOverrideConfig =
        getCurrentWindowOverrideConfiguration(displayId, activityConfiguration);
    return windowOverrideConfig != null
        ? withOverride(globalConfiguration, windowOverrideConfig)
        : getDisplayConfiguration(displayId, globalConfiguration);
  }

  /**
   * Returns the bounds of the window of an activity with the given configuration if it is in a
   * window of its own, such as a freeform window or one half of split screen, or null if its window
   * fills its display.
   */
  @Nullable
  public static Rect getWindowBounds(Configuration configuration) {
    if (RuntimeEnvironment.getApiLevel() < VERSION_CODES.P) {
      return null;
    }
    WindowConfiguration windowConfiguration = configuration.windowConfiguration;
    return windowConfiguration.getWindowingMode() == WindowConfiguration.WINDOWING_MODE_FREEFORM
            || isInSplitScreen(configuration)
        ? new Rect(windowConfiguration.getBounds())
        : null;
  }

  /** Returns whether an activity with the given configuration is in the system's split screen. */
  public static boolean isInSplitScreen(Configuration configuration) {
    if (RuntimeEnvironment.getApiLevel() < VERSION_CODES.P) {
      return false;
    }
    int windowingMode = configuration.windowConfiguration.getWindowingMode();
    return windowingMode == getSplitScreenWindowingMode(/* topOrLeft= */ true)
        || windowingMode == getSplitScreenWindowingMode(/* topOrLeft= */ false);
  }

  /**
   * Returns whether an activity with the given configuration is in the top or left half of split
   * screen.
   */
  public static boolean isInTopOrLeftOfSplitScreen(Configuration configuration) {
    if (!isInSplitScreen(configuration)) {
      return false;
    }
    WindowConfiguration windowConfiguration = configuration.windowConfiguration;
    if (RuntimeEnvironment.getApiLevel() >= VERSION_CODES.S_V2) {
      Rect bounds = windowConfiguration.getBounds();
      return bounds.left == 0 && bounds.top == 0;
    }
    return windowConfiguration.getWindowingMode()
        == getSplitScreenWindowingMode(/* topOrLeft= */ true);
  }

  /**
   * Returns the windowing mode of the system's split screen: the primary and secondary split screen
   * modes before S_V2, and multi-window mode after, when split screen moved to Shell.
   */
  private static int getSplitScreenWindowingMode(boolean topOrLeft) {
    if (RuntimeEnvironment.getApiLevel() >= VERSION_CODES.S_V2) {
      return WindowConfiguration.WINDOWING_MODE_MULTI_WINDOW;
    }
    return ReflectionHelpers.getStaticField(
        WindowConfiguration.class,
        topOrLeft
            ? "WINDOWING_MODE_SPLIT_SCREEN_PRIMARY"
            : "WINDOWING_MODE_SPLIT_SCREEN_SECONDARY");
  }

  @Nullable
  private static Configuration getWindowOverrideConfiguration(
      int displayId, Rect bounds, int windowingMode) {
    if (RuntimeEnvironment.getApiLevel() < VERSION_CODES.P) {
      return null;
    }
    DisplayInfo displayInfo = DisplayManagerGlobal.getInstance().getDisplayInfo(displayId);
    if (displayInfo == null) {
      return null;
    }
    Configuration configuration =
        createOverrideConfiguration(displayId, displayInfo, bounds.width(), bounds.height());
    setBounds(
        configuration.windowConfiguration,
        bounds,
        new Rect(0, 0, displayInfo.logicalWidth, displayInfo.logicalHeight));
    configuration.windowConfiguration.setWindowingMode(windowingMode);
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

  private static Configuration withOverride(
      Configuration configuration, @Nullable Configuration overrideConfig) {
    if (overrideConfig == null) {
      return configuration;
    }
    Configuration result = new Configuration(configuration);
    result.updateFrom(overrideConfig);
    return result;
  }
}
