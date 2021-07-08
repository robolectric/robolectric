package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR1;
import static android.os.Build.VERSION_CODES.P;
import static org.robolectric.shadow.api.Shadow.extract;
import static org.robolectric.shadow.api.Shadow.invokeConstructor;
import static org.robolectric.shadows.ShadowLooper.shadowMainLooper;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.content.Context;
import android.content.res.Configuration;
import android.hardware.display.BrightnessChangeEvent;
import android.hardware.display.DisplayManager;
import android.hardware.display.DisplayManagerGlobal;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.DisplayInfo;
import android.view.Surface;
import java.util.List;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.android.Bootstrap;
import org.robolectric.android.internal.DisplayConfig;
import org.robolectric.annotation.HiddenApi;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.res.Qualifiers;
import org.robolectric.util.Consumer;
import org.robolectric.util.ReflectionHelpers.ClassParameter;
import org.robolectric.util.reflector.Direct;
import org.robolectric.util.reflector.ForType;

/**
 * For tests, display properties may be changed and devices may be added or removed
 * programmatically.
 */
@Implements(value = DisplayManager.class, minSdk = JELLY_BEAN_MR1, looseSignatures = true)
public class ShadowDisplayManager {

  @RealObject private DisplayManager realDisplayManager;

  private Context context;

  @Implementation
  protected void __constructor__(Context context) {
    this.context = context;

    invokeConstructor(
        DisplayManager.class, realDisplayManager, ClassParameter.from(Context.class, context));
  }

  /**
   * Adds a simulated display and drain the main looper queue to ensure all the callbacks are
   * processed.
   *
   * @param qualifiersStr the {@link Qualifiers} string representing characteristics of the new
   *     display.
   * @return the new display's ID
   */
  public static int addDisplay(String qualifiersStr) {
    int id = getShadowDisplayManagerGlobal().addDisplay(createDisplayInfo(qualifiersStr, null));
    shadowMainLooper().idle();
    return id;
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

    Bootstrap.applyQualifiers(
        qualifiersStr, RuntimeEnvironment.getApiLevel(), configuration, displayMetrics);

    return createDisplayInfo(configuration, displayMetrics);
  }

  /**
   * Changes properties of a simulated display. If {@param qualifiersStr} starts with a plus ('+')
   * sign, the display's previous configuration is modified with the given qualifiers; otherwise
   * defaults are applied as described <a
   * href="http://robolectric.org/device-configuration/">here</a>.
   *
   * <p>Idles the main looper to ensure all listeners are notified.
   *
   * @param displayId the display id to change
   * @param qualifiersStr the {@link Qualifiers} string representing characteristics of the new
   *     display
   */
  public static void changeDisplay(int displayId, String qualifiersStr) {
    DisplayInfo baseDisplayInfo = DisplayManagerGlobal.getInstance().getDisplayInfo(displayId);
    DisplayInfo displayInfo = createDisplayInfo(qualifiersStr, baseDisplayInfo);
    getShadowDisplayManagerGlobal().changeDisplay(displayId, displayInfo);
    shadowMainLooper().idle();
  }

  /**
   * Changes properties of a simulated display. The original properties will be passed to the
   * {@param consumer}, which may modify them in place. The display will be updated with the new
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
   * Removes a simulated display and idles the main looper to ensure all listeners are notified.
   *
   * @param displayId the display id to remove
   */
  public static void removeDisplay(int displayId) {
    getShadowDisplayManagerGlobal().removeDisplay(displayId);
    shadowMainLooper().idle();
  }

  /**
   * Returns the current display saturation level set via {@link
   * android.hardware.display.DisplayManager#setSaturationLevel(float)}.
   */
  public float getSaturationLevel() {
    if (RuntimeEnvironment.getApiLevel() >= Build.VERSION_CODES.Q) {
      ShadowColorDisplayManager shadowCdm =
          extract(context.getSystemService(Context.COLOR_DISPLAY_SERVICE));
      return shadowCdm.getSaturationLevel() / 100f;
    }
    return getShadowDisplayManagerGlobal().getSaturationLevel();
  }

  /**
   * Sets the current display saturation level.
   *
   * This is a workaround for tests which cannot use the relevant hidden
   * {@link android.annotation.SystemApi},
   * {@link android.hardware.display.DisplayManager#setSaturationLevel(float)}.
   */
  public void setSaturationLevel(float level) {
    reflector(DisplayManagerReflector.class, realDisplayManager).setSaturationLevel(level);
  }

  @Implementation(minSdk = P)
  @HiddenApi
  protected void setBrightnessConfiguration(Object config) {
    setBrightnessConfigurationForUser(config, 0, context.getPackageName());
  }

  @Implementation(minSdk = P)
  @HiddenApi
  protected void setBrightnessConfigurationForUser(Object config, int userId, String packageName) {
    getShadowDisplayManagerGlobal().setBrightnessConfigurationForUser(config, userId, packageName);
  }

  /** Set the default brightness configuration for this device. */
  public static void setDefaultBrightnessConfiguration(Object config) {
    getShadowDisplayManagerGlobal().setDefaultBrightnessConfiguration(config);
  }

  /** Set the slider events the system has seen. */
  public static void setBrightnessEvents(List<BrightnessChangeEvent> events) {
    getShadowDisplayManagerGlobal().setBrightnessEvents(events);
  }

  private static ShadowDisplayManagerGlobal getShadowDisplayManagerGlobal() {
    if (Build.VERSION.SDK_INT < JELLY_BEAN_MR1) {
      throw new UnsupportedOperationException("multiple displays not supported in Jelly Bean");
    }

    return extract(DisplayManagerGlobal.getInstance());
  }

  @ForType(DisplayManager.class)
  interface DisplayManagerReflector {

    @Direct
    void setSaturationLevel(float level);
  }
}
