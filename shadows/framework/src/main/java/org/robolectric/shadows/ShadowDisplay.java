package org.robolectric.shadows;

import static org.robolectric.util.reflector.Reflector.reflector;

import android.content.Context;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Display.HdrCapabilities;
import android.view.Surface;
import android.view.WindowManager;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.Direct;
import org.robolectric.util.reflector.ForType;

/**
 * It is possible to override some display properties using setters on {@link ShadowDisplay}.
 *
 * @see <a href="http://robolectric.org/device-configuration/">device configuration</a> for details.
 */
@SuppressWarnings({"UnusedDeclaration"})
@Implements(value = Display.class)
public class ShadowDisplay {

  /**
   * Returns the default display.
   *
   * @return the default display
   */
  public static Display getDefaultDisplay() {
    WindowManager windowManager =
        (WindowManager)
            RuntimeEnvironment.getApplication().getSystemService(Context.WINDOW_SERVICE);
    return windowManager.getDefaultDisplay();
  }

  @RealObject Display realObject;

  private Float refreshRate;
  private Float scaledDensity;

  /**
   * If {@link #setScaledDensity(float)} has been called, {@link DisplayMetrics#scaledDensity} will
   * be modified to reflect the value specified. Note that this is not a realistic state.
   *
   * @deprecated This behavior is deprecated and will be removed in Robolectric 4.13.
   */
  @Deprecated
  @Implementation
  protected void getMetrics(DisplayMetrics outMetrics) {
    reflector(_Display_.class, realObject).getMetrics(outMetrics);
    if (scaledDensity != null) {
      outMetrics.scaledDensity = scaledDensity;
    }
  }

  /**
   * If {@link #setScaledDensity(float)} has been called, {@link DisplayMetrics#scaledDensity} will
   * be modified to reflect the value specified. Note that this is not a realistic state.
   *
   * @deprecated This behavior is deprecated and will be removed in Robolectric 4.13.
   */
  @Deprecated
  @Implementation
  protected void getRealMetrics(DisplayMetrics outMetrics) {
    reflector(_Display_.class, realObject).getRealMetrics(outMetrics);
    if (scaledDensity != null) {
      outMetrics.scaledDensity = scaledDensity;
    }
  }

  /**
   * Changes the scaled density for this display.
   *
   * @deprecated This method is deprecated and will be removed in Robolectric 4.13.
   */
  @Deprecated
  public void setScaledDensity(float scaledDensity) {
    this.scaledDensity = scaledDensity;
  }

  /**
   * If {@link #setRefreshRate(float)} has been called, this method will return the specified value.
   *
   * @deprecated This behavior is deprecated and will be removed in Robolectric 3.7.
   */
  @Deprecated
  @Implementation
  protected float getRefreshRate() {
    if (refreshRate != null) {
      return refreshRate;
    }
    float realRefreshRate = reflector(_Display_.class, realObject).getRefreshRate();
    // refresh rate may be set by native code. if its 0, set to 60fps
    if (realRefreshRate < 0.1) {
      realRefreshRate = 60;
    }
    return realRefreshRate;
  }

  /**
   * Changes the density for this display.
   *
   * <p>Any registered {@link android.hardware.display.DisplayManager.DisplayListener}s will be
   * notified of the change.
   */
  public void setDensity(float density) {
    setDensityDpi((int) (density * DisplayMetrics.DENSITY_DEFAULT));
  }

  /**
   * Changes the density for this display.
   *
   * <p>Any registered {@link android.hardware.display.DisplayManager.DisplayListener}s will be
   * notified of the change.
   */
  public void setDensityDpi(int densityDpi) {
    ShadowDisplayManager.changeDisplay(
        realObject.getDisplayId(), di -> di.logicalDensityDpi = densityDpi);
  }

  /**
   * Changes the horizontal DPI for this display.
   *
   * <p>Any registered {@link android.hardware.display.DisplayManager.DisplayListener}s will be
   * notified of the change.
   */
  public void setXdpi(float xdpi) {
    ShadowDisplayManager.changeDisplay(realObject.getDisplayId(), di -> di.physicalXDpi = xdpi);
  }

  /**
   * Changes the vertical DPI for this display.
   *
   * <p>Any registered {@link android.hardware.display.DisplayManager.DisplayListener}s will be
   * notified of the change.
   */
  public void setYdpi(float ydpi) {
    ShadowDisplayManager.changeDisplay(realObject.getDisplayId(), di -> di.physicalYDpi = ydpi);
  }

  /**
   * Changes the name for this display.
   *
   * <p>Any registered {@link android.hardware.display.DisplayManager.DisplayListener}s will be
   * notified of the change.
   */
  public void setName(String name) {
    ShadowDisplayManager.changeDisplay(realObject.getDisplayId(), di -> di.name = name);
  }

  /**
   * Changes the flags for this display.
   *
   * <p>Any registered {@link android.hardware.display.DisplayManager.DisplayListener}s will be
   * notified of the change.
   */
  public void setFlags(int flags) {
    reflector(_Display_.class, realObject).setFlags(flags);

    ShadowDisplayManager.changeDisplay(realObject.getDisplayId(), di -> di.flags = flags);
  }

  /**
   * Changes the width available to the application for this display.
   *
   * <p>Any registered {@link android.hardware.display.DisplayManager.DisplayListener}s will be
   * notified of the change.
   *
   * @param width the new width in pixels
   */
  public void setWidth(int width) {
    ShadowDisplayManager.changeDisplay(realObject.getDisplayId(), di -> di.appWidth = width);
  }

  /**
   * Changes the height available to the application for this display.
   *
   * <p>Any registered {@link android.hardware.display.DisplayManager.DisplayListener}s will be
   * notified of the change.
   *
   * @param height new height in pixels
   */
  public void setHeight(int height) {
    ShadowDisplayManager.changeDisplay(realObject.getDisplayId(), di -> di.appHeight = height);
  }

  /**
   * Changes the simulated physical width for this display.
   *
   * <p>Any registered {@link android.hardware.display.DisplayManager.DisplayListener}s will be
   * notified of the change.
   *
   * @param width the new width in pixels
   */
  public void setRealWidth(int width) {
    ShadowDisplayManager.changeDisplay(realObject.getDisplayId(), di -> di.logicalWidth = width);
  }

  /**
   * Changes the simulated physical height for this display.
   *
   * <p>Any registered {@link android.hardware.display.DisplayManager.DisplayListener}s will be
   * notified of the change.
   *
   * @param height the new height in pixels
   */
  public void setRealHeight(int height) {
    ShadowDisplayManager.changeDisplay(realObject.getDisplayId(), di -> di.logicalHeight = height);
  }

  /**
   * Changes the refresh rate for this display.
   *
   * <p>Any registered {@link android.hardware.display.DisplayManager.DisplayListener}s will be
   * notified of the change.
   */
  public void setRefreshRate(float refreshRate) {
    this.refreshRate = refreshRate;
  }

  /**
   * Changes the rotation for this display.
   *
   * <p>Any registered {@link android.hardware.display.DisplayManager.DisplayListener}s will be
   * notified of the change.
   *
   * @param rotation one of {@link Surface#ROTATION_0}, {@link Surface#ROTATION_90}, {@link
   *     Surface#ROTATION_180}, {@link Surface#ROTATION_270}
   */
  public void setRotation(int rotation) {
    ShadowDisplayManager.changeDisplay(realObject.getDisplayId(), di -> di.rotation = rotation);
  }

  /**
   * Changes the simulated state for this display, such as whether it is on or off
   *
   * <p>Any registered {@link android.hardware.display.DisplayManager.DisplayListener}s will be
   * notified of the change.
   *
   * @param state the new state: one of {@link Display#STATE_OFF}, {@link Display#STATE_ON}, {@link
   *     Display#STATE_DOZE}, {@link Display#STATE_DOZE_SUSPEND}, or {@link Display#STATE_UNKNOWN}.
   */
  public void setState(int state) {
    ShadowDisplayManager.changeDisplay(realObject.getDisplayId(), di -> di.state = state);
  }

  /**
   * Set HDR capabilities to the display sourced with displayId. see {@link HdrCapabilities} for
   * supportedHdrTypes.
   *
   * @throws UnsupportedOperationException if the method is called below Android vesrsion N.
   */
  public void setDisplayHdrCapabilities(
      int displayId,
      float maxLuminance,
      float maxAverageLuminance,
      float minLuminance,
      int... supportedHdrTypes) {
    if (Build.VERSION.SDK_INT < VERSION_CODES.N) {
      throw new UnsupportedOperationException("HDR capabilities are not supported below Android N");
    }

    if (RuntimeEnvironment.getApiLevel() > VERSION_CODES.TIRAMISU) {
      reflector(DisplayModeReflector.class, realObject.getMode())
          .setSupportedHdrTypes(supportedHdrTypes);
    }

    ShadowDisplayManager.changeDisplay(
        displayId,
        displayConfig -> {
          displayConfig.hdrCapabilities =
              new HdrCapabilities(
                  supportedHdrTypes, maxLuminance, maxAverageLuminance, minLuminance);
        });
  }

  /**
   * Sets current hdr/sdr ratio expressed as the ratio of targetHdrPeakBrightnessInNits /
   * targetSdrWhitePointInNits. This will have the intended side effect of making {@link
   * Display#isHdrSdrRatioAvailable()} equal to true if set to any value other than {@link
   * Float#NaN}.
   *
   * @throws UnsupportedOperationException if the method is called below Android version U.
   */
  public void setHdrSdrRatio(float hdrSdrRatio) {
    if (Build.VERSION.SDK_INT < VERSION_CODES.UPSIDE_DOWN_CAKE) {
      throw new UnsupportedOperationException("setHdrSdrRatio is not supported below Android U");
    }

    ShadowDisplayManager.changeDisplay(
        realObject.getDisplayId(), displayConfig -> displayConfig.hdrSdrRatio = hdrSdrRatio);
  }

  /**
   * Changes the display cutout for this display.
   *
   * @throws UnsupportedOperationException if the method is called below Android version Q.
   */
  public void setDisplayCutout(Object displayCutout) {
    if (Build.VERSION.SDK_INT < VERSION_CODES.Q) {
      throw new UnsupportedOperationException("Display cutouts are not supported below Android Q");
    }

    ShadowDisplayManager.changeDisplay(
        realObject.getDisplayId(), displayConfig -> displayConfig.displayCutout = displayCutout);
  }

  /** Reflector interface for {@link Display}'s internals. */
  @ForType(Display.class)
  interface _Display_ {
    @Direct
    void getMetrics(DisplayMetrics outMetrics);

    @Direct
    void getRealMetrics(DisplayMetrics outMetrics);

    @Direct
    float getRefreshRate();

    @Accessor("mFlags")
    void setFlags(int flags);
  }

  @ForType(Display.Mode.class)
  interface DisplayModeReflector {
    @Accessor("mSupportedHdrTypes")
    void setSupportedHdrTypes(int[] types);
  }
}
