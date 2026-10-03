package org.robolectric.android.internal;

import android.app.Activity;
import android.app.WindowConfiguration;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Rect;
import android.hardware.display.DisplayManagerGlobal;
import android.os.Build.VERSION_CODES;
import android.util.DisplayMetrics;
import android.util.Rational;
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

  /** The smallest size of a freeform window whose activity doesn't declare one, as on a device. */
  private static final int DEFAULT_MINIMAL_SIZE_RESIZABLE_TASK_DP = 220;

  /** The smallest width of a large screen, as the window manager defines it. */
  private static final int LARGE_SCREEN_SMALLEST_WIDTH_DP = 600;

  // How the system sizes and places a picture-in-picture window, as its resources configure.
  private static final float PICTURE_IN_PICTURE_SIZE_PERCENT = 0.23f;
  private static final int PICTURE_IN_PICTURE_MIN_SIZE_DP = 108;
  private static final int PICTURE_IN_PICTURE_EDGE_INSET_DP = 16;

  private static final float PICTURE_IN_PICTURE_DEFAULT_ASPECT_RATIO = 16f / 9;

  private static final Map<Integer, Float> splitScreenDividerPositions = new HashMap<>();
  private static final Map<Integer, Boolean> ignoreOrientationRequests = new HashMap<>();

  private WindowConfigurations() {}

  /** Forgets how each display was set up: its split screen divider and orientation requests. */
  public static void reset() {
    splitScreenDividerPositions.clear();
    ignoreOrientationRequests.clear();
  }

  /**
   * Makes the window manager ignore the orientation requests of activities on the given display,
   * and letterbox them instead, as {@code wm set-ignore-orientation-request} does.
   */
  public static void setIgnoreOrientationRequest(int displayId, boolean ignoreOrientationRequest) {
    ignoreOrientationRequests.put(displayId, ignoreOrientationRequest);
  }

  /** Returns whether the window manager ignores orientation requests on the given display. */
  public static boolean isIgnoringOrientationRequest(int displayId) {
    return ignoreOrientationRequests.getOrDefault(displayId, false);
  }

  /**
   * Returns the orientation, {@link Configuration#ORIENTATION_PORTRAIT} or {@link
   * Configuration#ORIENTATION_LANDSCAPE}, that the given screen orientation requests, or {@link
   * Configuration#ORIENTATION_UNDEFINED} if it doesn't request a fixed one.
   */
  public static int getFixedOrientation(int screenOrientation) {
    switch (screenOrientation) {
      case ActivityInfo.SCREEN_ORIENTATION_PORTRAIT:
      case ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT:
      case ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT:
      case ActivityInfo.SCREEN_ORIENTATION_USER_PORTRAIT:
        return Configuration.ORIENTATION_PORTRAIT;
      case ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE:
      case ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE:
      case ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE:
      case ActivityInfo.SCREEN_ORIENTATION_USER_LANDSCAPE:
        return Configuration.ORIENTATION_LANDSCAPE;
      default:
        return Configuration.ORIENTATION_UNDEFINED;
    }
  }

  /**
   * Returns whether the window manager ignores the orientation and resizability restrictions of the
   * app on the given display: since Android 16, on a large screen, for an app that targets it and
   * isn't a game.
   */
  public static boolean isUniversalResizeable(ApplicationInfo applicationInfo, int displayId) {
    if (RuntimeEnvironment.getApiLevel() < VERSION_CODES.BAKLAVA
        || applicationInfo.targetSdkVersion < VERSION_CODES.BAKLAVA
        || applicationInfo.category == ApplicationInfo.CATEGORY_GAME) {
      return false;
    }
    return isLargeScreen(displayId);
  }

  /**
   * Returns whether the given display is a large screen, at least 600dp wide in both directions.
   */
  private static boolean isLargeScreen(int displayId) {
    DisplayInfo displayInfo = DisplayManagerGlobal.getInstance().getDisplayInfo(displayId);
    if (displayInfo == null) {
      return false;
    }
    float density = displayInfo.logicalDensityDpi / (float) DisplayMetrics.DENSITY_DEFAULT;
    return Math.min(displayInfo.logicalWidth, displayInfo.logicalHeight) / density
        >= LARGE_SCREEN_SMALLEST_WIDTH_DP;
  }

  /**
   * Returns how the configuration of an activity requesting the given screen orientation on the
   * given display differs from the global configuration if the window manager letterboxes it: when
   * the display ignores orientation requests and the activity requests another orientation than the
   * display's. Returns null otherwise.
   */
  @Nullable
  public static Configuration getLetterboxOverrideConfiguration(
      ApplicationInfo applicationInfo, int screenOrientation, int displayId) {
    int orientation = getFixedOrientation(screenOrientation);
    if (RuntimeEnvironment.getApiLevel() < VERSION_CODES.S
        || orientation == Configuration.ORIENTATION_UNDEFINED
        || !isIgnoringOrientationRequest(displayId)
        || isUniversalResizeable(applicationInfo, displayId)) {
      return null;
    }
    DisplayInfo displayInfo = DisplayManagerGlobal.getInstance().getDisplayInfo(displayId);
    if (displayInfo == null) {
      return null;
    }
    int width = displayInfo.logicalWidth;
    int height = displayInfo.logicalHeight;
    boolean portrait = orientation == Configuration.ORIENTATION_PORTRAIT;
    if (portrait == height >= width) {
      return null;
    }
    // As the window manager does, keep the display's aspect ratio unless the device sets another.
    float aspectRatio = getFloatResource("config_fixedOrientationLetterboxAspectRatio", 0f);
    if (aspectRatio <= 1f) {
      aspectRatio = Math.max(width, height) / (float) Math.min(width, height);
    }
    Rect bounds;
    if (portrait) {
      int letterboxWidth = Math.round(height / aspectRatio);
      int left =
          Math.round(
              (width - letterboxWidth)
                  * getFloatResource("config_letterboxHorizontalPositionMultiplier", 0.5f));
      bounds = new Rect(left, 0, left + letterboxWidth, height);
    } else {
      int letterboxHeight = Math.round(width / aspectRatio);
      int top =
          Math.round(
              (height - letterboxHeight)
                  * getFloatResource("config_letterboxVerticalPositionMultiplier", 0f));
      bounds = new Rect(0, top, width, top + letterboxHeight);
    }
    Configuration configuration =
        createOverrideConfiguration(displayId, displayInfo, bounds.width(), bounds.height());
    // The maximum bounds of a letterboxed activity are its own.
    setBounds(configuration.windowConfiguration, bounds, bounds);
    return configuration;
  }

  /**
   * Moves the divider of split screen on the given display as when the user drags its middle to the
   * given fraction of the display's width, or of its height if it is portrait, and releases it. As
   * on a device, the divider snaps to the nearest position the system allows. Returns false if that
   * is past an edge of the display, which dismisses split screen.
   */
  public static boolean setSplitScreenDividerPosition(int displayId, float position) {
    DisplayInfo displayInfo = DisplayManagerGlobal.getInstance().getDisplayInfo(displayId);
    if (displayInfo == null) {
      return true;
    }
    int length = Math.max(displayInfo.logicalWidth, displayInfo.logicalHeight);
    int dividerStart =
        snapSplitScreenDivider(
            displayInfo,
            Math.round(length * position - getSplitScreenDividerSize(displayInfo) / 2f),
            /* canDismiss= */ true);
    if (dividerStart < 0 || dividerStart >= length) {
      splitScreenDividerPositions.remove(displayId);
      return false;
    }
    splitScreenDividerPositions.put(displayId, dividerStart / (float) length);
    return true;
  }

  /**
   * Returns where the divider of split screen starts once it is released at the given position, as
   * the system's DividerSnapAlgorithm decides: in the middle of the display, where one of the
   * activities gets a 16:9 window if that is at least the minimal size, or past an edge of the
   * display to dismiss split screen.
   */
  private static int snapSplitScreenDivider(
      DisplayInfo displayInfo, int position, boolean canDismiss) {
    int length = Math.max(displayInfo.logicalWidth, displayInfo.logicalHeight);
    int dividerSize = getSplitScreenDividerSize(displayInfo);
    int size =
        (int) Math.floor(9f / 16 * Math.min(displayInfo.logicalWidth, displayInfo.logicalHeight));
    boolean fitsMinimalSize =
        size
            >= Math.round(
                DEFAULT_MINIMAL_SIZE_RESIZABLE_TASK_DP
                    * displayInfo.logicalDensityDpi
                    / (float) DisplayMetrics.DENSITY_DEFAULT);
    // Before S, the divider has to get closer to an edge to dismiss split screen.
    float dismissDistanceMultiplier =
        RuntimeEnvironment.getApiLevel() >= VERSION_CODES.S ? 1 : 0.35f;
    int[] targets = {
      -dividerSize, size, length / 2 - dividerSize / 2, length - size - dividerSize, length
    };
    int snappedPosition = targets[2];
    float minDistance = Float.MAX_VALUE;
    for (int i = 0; i < targets.length; i++) {
      boolean dismisses = i == 0 || i == targets.length - 1;
      if (dismisses ? !canDismiss : (i != 2 && !fitsMinimalSize)) {
        continue;
      }
      float distance =
          Math.abs(position - targets[i]) / (dismisses ? dismissDistanceMultiplier : 1);
      if (distance < minDistance) {
        snappedPosition = targets[i];
        minDistance = distance;
      }
    }
    return snappedPosition;
  }

  private static int getSplitScreenDividerSize(DisplayInfo displayInfo) {
    return Math.round(
        SPLIT_SCREEN_DIVIDER_SIZE_DP
            * displayInfo.logicalDensityDpi
            / (float) DisplayMetrics.DENSITY_DEFAULT);
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
   * Returns how the configuration of an activity in picture-in-picture mode on the given display
   * differs from the global configuration: its pinned window has the size the system gives a window
   * with the given aspect ratio, or with the default one if it is null, in the bottom right corner
   * of the display. Returns null for a display that does not exist, and before P.
   */
  @Nullable
  public static Configuration getPictureInPictureOverrideConfiguration(
      int displayId, @Nullable Rational requestedAspectRatio) {
    DisplayInfo displayInfo = DisplayManagerGlobal.getInstance().getDisplayInfo(displayId);
    if (displayInfo == null) {
      return null;
    }
    float aspectRatio =
        requestedAspectRatio != null
            ? requestedAspectRatio.floatValue()
            : PICTURE_IN_PICTURE_DEFAULT_ASPECT_RATIO;
    float density = displayInfo.logicalDensityDpi / (float) DisplayMetrics.DENSITY_DEFAULT;
    int width = displayInfo.logicalWidth;
    int height = displayInfo.logicalHeight;
    // As the system's PipBoundsAlgorithm does, the shorter edge is a fraction of the display's.
    int minSize =
        (int)
            Math.max(
                PICTURE_IN_PICTURE_MIN_SIZE_DP * density,
                Math.min(width, height) * PICTURE_IN_PICTURE_SIZE_PERCENT);
    int pipWidth = aspectRatio <= 1 ? minSize : Math.round(minSize * aspectRatio);
    int pipHeight = aspectRatio <= 1 ? Math.round(minSize / aspectRatio) : minSize;
    int edgeInset = Math.round(PICTURE_IN_PICTURE_EDGE_INSET_DP * density);
    int right = width - edgeInset;
    int bottom = height - edgeInset;
    return getWindowOverrideConfiguration(
        displayId,
        new Rect(right - pipWidth, bottom - pipHeight, right, bottom),
        WindowConfiguration.WINDOWING_MODE_PINNED);
  }

  /** Returns whether an activity with the given configuration is in picture-in-picture mode. */
  public static boolean isInPictureInPictureMode(Configuration configuration) {
    return RuntimeEnvironment.getApiLevel() >= VERSION_CODES.P
        && configuration.windowConfiguration.getWindowingMode()
            == WindowConfiguration.WINDOWING_MODE_PINNED;
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
    int dividerSize = getSplitScreenDividerSize(displayInfo);
    // As the system does, the divider keeps its ratio of the display when the display changes,
    // then snaps to the nearest position it can rest at.
    Float dividerPosition = splitScreenDividerPositions.get(displayId);
    int length = Math.max(width, height);
    int dividerStart =
        snapSplitScreenDivider(
            displayInfo,
            dividerPosition != null
                ? (int) (length * dividerPosition)
                : length / 2 - dividerSize / 2,
            /* canDismiss= */ false);
    Rect bounds = new Rect(0, 0, width, height);
    if (width > height) {
      if (topOrLeft) {
        bounds.right = dividerStart;
      } else {
        bounds.left = dividerStart + dividerSize;
      }
    } else {
      if (topOrLeft) {
        bounds.bottom = dividerStart;
      } else {
        bounds.top = dividerStart + dividerSize;
      }
    }
    return getWindowOverrideConfiguration(
        displayId, bounds, getSplitScreenWindowingMode(topOrLeft));
  }

  /**
   * Returns how the configuration of the activity differs from the global configuration if it keeps
   * its window: a freeform window keeps its bounds, and split screen, a pinned window and a
   * letterbox are recomputed for the display. Returns null if its window fills the display.
   */
  @Nullable
  public static Configuration getCurrentWindowOverrideConfiguration(Activity activity) {
    if (RuntimeEnvironment.getApiLevel() < VERSION_CODES.P) {
      return null;
    }
    int displayId = activity.getWindowManager().getDefaultDisplay().getDisplayId();
    Configuration activityConfiguration = activity.getResources().getConfiguration();
    WindowConfiguration windowConfiguration = activityConfiguration.windowConfiguration;
    if (windowConfiguration.getWindowingMode() == WindowConfiguration.WINDOWING_MODE_FREEFORM) {
      return getFreeformOverrideConfiguration(displayId, windowConfiguration.getBounds());
    }
    if (isInSplitScreen(activityConfiguration)) {
      return getSplitScreenOverrideConfiguration(
          displayId, isInTopOrLeftOfSplitScreen(activityConfiguration));
    }
    if (isInPictureInPictureMode(activityConfiguration)) {
      Rect bounds = windowConfiguration.getBounds();
      return getPictureInPictureOverrideConfiguration(
          displayId, new Rational(bounds.width(), bounds.height()));
    }
    return getLetterboxOverrideConfiguration(
        activity.getApplicationInfo(), activity.getRequestedOrientation(), displayId);
  }

  /** Returns the configuration of the activity, given the global one, if it keeps its window. */
  public static Configuration getActivityConfiguration(
      Activity activity, Configuration globalConfiguration) {
    Configuration windowOverrideConfig = getCurrentWindowOverrideConfiguration(activity);
    return windowOverrideConfig != null
        ? withOverride(globalConfiguration, windowOverrideConfig)
        : getDisplayConfiguration(
            activity.getWindowManager().getDefaultDisplay().getDisplayId(), globalConfiguration);
  }

  /**
   * Returns whether an activity with the given configuration is in multi-window mode: in a freeform
   * window, in split screen or in picture-in-picture mode.
   */
  public static boolean isInMultiWindowMode(Configuration configuration) {
    return RuntimeEnvironment.getApiLevel() >= VERSION_CODES.P
        && (configuration.windowConfiguration.getWindowingMode()
                == WindowConfiguration.WINDOWING_MODE_FREEFORM
            || isInSplitScreen(configuration)
            || isInPictureInPictureMode(configuration));
  }

  /**
   * Returns the bounds of the window of an activity with the given configuration on the given
   * display if they are its own, such as a freeform window, one half of split screen or a
   * letterbox, or null if its window fills the display.
   */
  @Nullable
  public static Rect getWindowBounds(int displayId, Configuration configuration) {
    if (RuntimeEnvironment.getApiLevel() < VERSION_CODES.P) {
      return null;
    }
    DisplayInfo displayInfo = DisplayManagerGlobal.getInstance().getDisplayInfo(displayId);
    Rect bounds = configuration.windowConfiguration.getBounds();
    if (displayInfo == null
        || bounds.isEmpty()
        || bounds.equals(new Rect(0, 0, displayInfo.logicalWidth, displayInfo.logicalHeight))) {
      return null;
    }
    return new Rect(bounds);
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

  private static float getFloatResource(String name, float defaultValue) {
    Resources resources = Resources.getSystem();
    int id = resources.getIdentifier(name, "dimen", "android");
    return id != 0 ? resources.getFloat(id) : defaultValue;
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
