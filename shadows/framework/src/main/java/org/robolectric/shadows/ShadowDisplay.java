package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.JELLY_BEAN;
import static org.robolectric.shadow.api.Shadow.directlyOn;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Point;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.ForType;

/**
 * It is possible to override some display properties using setters on {@link ShadowDisplay};
 * however, this behavior is deprecated as of Robolectric 3.6 and will be removed in 3.7.
 *
 * Use [device configuration](http://robolectric.org/device-configuration/) to set up your
 * display properties instead.
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
        (WindowManager) RuntimeEnvironment.application.getSystemService(Context.WINDOW_SERVICE);
    return windowManager.getDefaultDisplay();
  }

  @RealObject Display realObject;

  private Float refreshRate;

  // the following fields are used only for Jelly Bean...
  private String name;
  private Integer displayId;
  private Integer width;
  private Integer height;
  private Integer realWidth;
  private Integer realHeight;
  private Integer densityDpi;
  private Float xdpi;
  private Float ydpi;
  private Float scaledDensity;
  private Integer rotation;
  private Integer pixelFormat;

  /**
   * If {@link #setScaledDensity(float)} has been called, {@link DisplayMetrics#scaledDensity} will
   * be modified to reflect the value specified. Note that this is not a realistic state.
   *
   * @deprecated This behavior is deprecated and will be removed in Robolectric 3.7.
   */
  @Deprecated
  @Implementation
  protected void getMetrics(DisplayMetrics outMetrics) {
    if (isJB()) {
      outMetrics.density = densityDpi * DisplayMetrics.DENSITY_DEFAULT_SCALE;
      outMetrics.densityDpi = densityDpi;
      outMetrics.scaledDensity = scaledDensity;
      outMetrics.widthPixels = width;
      outMetrics.heightPixels = height;
      outMetrics.xdpi = xdpi;
      outMetrics.ydpi = ydpi;
    } else {
      directlyOn(realObject, Display.class).getMetrics(outMetrics);
      if (scaledDensity != null) {
        outMetrics.scaledDensity = scaledDensity;
      }
    }
  }

  /**
   * If {@link #setScaledDensity(float)} has been called, {@link DisplayMetrics#scaledDensity} will
   * be modified to reflect the value specified. Note that this is not a realistic state.
   *
   * @deprecated This behavior is deprecated and will be removed in Robolectric 3.7.
   */
  @Deprecated
  @Implementation
  protected void getRealMetrics(DisplayMetrics outMetrics) {
    if (isJB()) {
      getMetrics(outMetrics);
      outMetrics.widthPixels = realWidth;
      outMetrics.heightPixels = realHeight;
    } else {
      directlyOn(realObject, Display.class).getRealMetrics(outMetrics);
      if (scaledDensity != null) {
        outMetrics.scaledDensity = scaledDensity;
      }
    }
  }

  /**
   * If {@link #setDisplayId(int)} has been called, this method will return the specified value.
   *
   * @deprecated This behavior is deprecated and will be removed in Robolectric 3.7.
   */
  @Deprecated
  @Implementation
  protected int getDisplayId() {
    return displayId == null
        ? directlyOn(realObject, Display.class).getDisplayId()
        : displayId;
  }

  /**
   * If {@link #setRefreshRate(float)} has been called, this method will return the specified value.
   *
   * @deprecated This behavior is deprecated and will be removed in Robolectric 3.7.
   */
  @Deprecated
  @Implementation
  protected float getRefreshRate() {
    return refreshRate == null
        ? directlyOn(realObject, Display.class).getRefreshRate()
        : refreshRate;
  }

  /**
   * If {@link #setPixelFormat(int)} has been called, this method will return the specified value.
   *
   * @deprecated This behavior is deprecated and will be removed in Robolectric 3.7.
   */
  @Deprecated
  @Implementation
  protected int getPixelFormat() {
    return pixelFormat == null
        ? directlyOn(realObject, Display.class).getPixelFormat()
        : pixelFormat;
  }

  @Implementation(maxSdk = JELLY_BEAN)
  protected void getSizeInternal(Point outSize, boolean doCompat) {
    outSize.x = width;
    outSize.y = height;
  }

  @Implementation(maxSdk = JELLY_BEAN)
  protected void getCurrentSizeRange(Point outSmallestSize, Point outLargestSize) {
    int minimum = Math.min(width, height);
    int maximum = Math.max(width, height);
    outSmallestSize.set(minimum, minimum);
    outLargestSize.set(maximum, maximum);
  }

  @Implementation(maxSdk = JELLY_BEAN)
  protected void getRealSize(Point outSize) {
    outSize.set(realWidth, realHeight);
  }

  /**
   * Changes the density for this display.
   *
   * Any registered {@link android.hardware.display.DisplayManager.DisplayListener}s will be
   * notified of the change.
   */
  public void setDensity(float density) {
    setDensityDpi((int) (density * DisplayMetrics.DENSITY_DEFAULT));
  }

  /**
   * Changes the density for this display.
   *
   * Any registered {@link android.hardware.display.DisplayManager.DisplayListener}s will be
   * notified of the change.
   */
  public void setDensityDpi(int densityDpi) {
    if (isJB()) {
      this.densityDpi = densityDpi;
    } else {
      ShadowDisplayManager.changeDisplay(realObject.getDisplayId(),
          di -> di.logicalDensityDpi = densityDpi);
    }
  }

  /**
   * Changes the horizontal DPI for this display.
   *
   * Any registered {@link android.hardware.display.DisplayManager.DisplayListener}s will be
   * notified of the change.
   */
  public void setXdpi(float xdpi) {
    if (isJB()) {
      this.xdpi = xdpi;
    } else {
      ShadowDisplayManager.changeDisplay(realObject.getDisplayId(),
          di -> di.physicalXDpi = xdpi);
    }
  }

  /**
   * Changes the vertical DPI for this display.
   *
   * Any registered {@link android.hardware.display.DisplayManager.DisplayListener}s will be
   * notified of the change.
   */
  public void setYdpi(float ydpi) {
    if (isJB()) {
      this.ydpi = ydpi;
    } else {
      ShadowDisplayManager.changeDisplay(realObject.getDisplayId(),
          di -> di.physicalYDpi = ydpi);
    }
  }

  /**
   * Changes the scaled density for this display.
   *
   * @deprecated This method is deprecated and will be removed in Robolectric 3.7.
   */
  @Deprecated
  public void setScaledDensity(float scaledDensity) {
    this.scaledDensity = scaledDensity;
  }

  /**
   * Changes the ID for this display.
   *
   * Any registered {@link android.hardware.display.DisplayManager.DisplayListener}s will be
   * notified of the change.
   *
   * @deprecated This method is deprecated and will be removed in Robolectric 3.7.
   */
  @Deprecated
  public void setDisplayId(int displayId) {
    this.displayId = displayId;
  }

  /**
   * Changes the name for this display.
   *
   * Any registered {@link android.hardware.display.DisplayManager.DisplayListener}s will be
   * notified of the change.
   */
  public void setName(String name) {
    if (isJB()) {
      this.name = name;
    } else {
      ShadowDisplayManager.changeDisplay(realObject.getDisplayId(),
          di -> di.name = name);
    }
  }

  /**
   * Changes the flags for this display.
   *
   * Any registered {@link android.hardware.display.DisplayManager.DisplayListener}s will be
   * notified of the change.
   */
  public void setFlags(int flags) {
    reflector(_Display_.class, realObject).setFlags(flags);

    if (!isJB()) {
      ShadowDisplayManager.changeDisplay(realObject.getDisplayId(),
          di -> di.flags = flags);
    }
  }

  /**
   * Changes the width available to the application for this display.
   *
   * Any registered {@link android.hardware.display.DisplayManager.DisplayListener}s will be
   * notified of the change.
   *
   * @param width the new width in pixels
   */
  public void setWidth(int width) {
    if (isJB()) {
      this.width = width;
    } else {
      ShadowDisplayManager.changeDisplay(realObject.getDisplayId(),
          di -> di.appWidth = width);
    }
  }

  /**
   * Changes the height available to the application for this display.
   *
   * Any registered {@link android.hardware.display.DisplayManager.DisplayListener}s will be
   * notified of the change.
   *
   * @param height new height in pixels
   */
  public void setHeight(int height) {
    if (isJB()) {
      this.height = height;
    } else {
      ShadowDisplayManager.changeDisplay(realObject.getDisplayId(),
          di -> di.appHeight = height);
    }
  }

  /**
   * Changes the simulated physical width for this display.
   *
   * Any registered {@link android.hardware.display.DisplayManager.DisplayListener}s will be
   * notified of the change.
   *
   * @param width the new width in pixels
   */
  public void setRealWidth(int width) {
    if (isJB()) {
      this.realWidth = width;
    } else {
      ShadowDisplayManager.changeDisplay(realObject.getDisplayId(),
          di -> di.logicalWidth = width);
    }
  }

  /**
   * Changes the simulated physical height for this display.
   *
   * Any registered {@link android.hardware.display.DisplayManager.DisplayListener}s will be
   * notified of the change.
   *
   * @param height the new height in pixels
   */
  public void setRealHeight(int height) {
    if (isJB()) {
      this.realHeight = height;
    } else {
      ShadowDisplayManager.changeDisplay(realObject.getDisplayId(),
          di -> di.logicalHeight = height);
    }
  }

  /**
   * Changes the refresh rate for this display.
   *
   * Any registered {@link android.hardware.display.DisplayManager.DisplayListener}s will be
   * notified of the change.
   */
  public void setRefreshRate(float refreshRate) {
    this.refreshRate = refreshRate;
  }

  /**
   * Changes the rotation for this display.
   *
   * Any registered {@link android.hardware.display.DisplayManager.DisplayListener}s will be
   * notified of the change.
   *
   * @param rotation one of {@link Surface#ROTATION_0}, {@link Surface#ROTATION_90},
   *                 {@link Surface#ROTATION_180}, {@link Surface#ROTATION_270}

   */
  public void setRotation(int rotation) {
    if (isJB()) {
      this.rotation = rotation;
    } else {
      ShadowDisplayManager.changeDisplay(realObject.getDisplayId(),
          di -> di.rotation = rotation);
    }
  }

  /**
   * Changes the pixel format for this display.
   *
   * @deprecated This method is deprecated and will be removed in Robolectric 3.7.
   */
  @Deprecated
  public void setPixelFormat(int pixelFormat) {
    this.pixelFormat = pixelFormat;
  }

  /**
   * Changes the simulated state for this display, such as whether it is on or off
   *
   * Any registered {@link android.hardware.display.DisplayManager.DisplayListener}s will be
   * notified of the change.
   *
   * @param state the new state: one of {@link Display#STATE_OFF}, {@link Display#STATE_ON},
   *        {@link Display#STATE_DOZE}, {@link Display#STATE_DOZE_SUSPEND}, or
   *        {@link Display#STATE_UNKNOWN}.
   */
  public void setState(int state) {
    if (!isJB()) {
      ShadowDisplayManager.changeDisplay(realObject.getDisplayId(),
          di -> di.state = state);
    }
  }

  private boolean isJB() {
    return RuntimeEnvironment.getApiLevel() == JELLY_BEAN;
  }

  void configureForJBOnly(Configuration configuration, DisplayMetrics displayMetrics) {
    int widthPx = (int) (configuration.screenWidthDp * displayMetrics.density);
    int heightPx = (int) (configuration.screenHeightDp * displayMetrics.density);

    name = "Built-in screen";
    displayId = 0;
    width = widthPx;
    height = heightPx;
    realWidth = widthPx;
    realHeight = heightPx;
    densityDpi = displayMetrics.densityDpi;
    xdpi = (float) displayMetrics.densityDpi;
    ydpi = (float) displayMetrics.densityDpi;
    scaledDensity = displayMetrics.densityDpi * DisplayMetrics.DENSITY_DEFAULT_SCALE;
    rotation = configuration.orientation == Configuration.ORIENTATION_PORTRAIT
        ? Surface.ROTATION_0
        : Surface.ROTATION_90;
  }

  /** Accessor interface for {@link Display}'s internals. */
  @ForType(Display.class)
  interface _Display_ {

    @Accessor("mFlags")
    void setFlags(int flags);
  }
}
