package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.JELLY_BEAN;
import static org.robolectric.shadow.api.Shadow.directlyOn;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Point;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.DisplayInfo;
import android.view.Surface;
import android.view.WindowManager;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.util.ReflectionHelpers;

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
    WindowManager windowManager = (WindowManager) RuntimeEnvironment.systemContext
        .getSystemService(Context.WINDOW_SERVICE);
    return windowManager.getDefaultDisplay();
  }

  @RealObject Display realObject;

  private String name;
  private Integer displayId;
  private Integer flags;
  private Integer width;
  private Integer height;
  private Integer realWidth;
  private Integer realHeight;
  private Integer densityDpi;
  private Float xdpi;
  private Float ydpi;
  private Float scaledDensity;
  private Float refreshRate;
  private Integer rotation;
  private Integer pixelFormat;

  /**
   * Injects modified values into Display's private DisplayInfo.
   *
   * This behavior is deprecated and will be removed in Robolectric 3.7.
   */
  @Implementation
  public void updateDisplayInfoLocked() {
    directlyOn(realObject, Display.class, "updateDisplayInfoLocked");
    DisplayInfo displayInfo = ReflectionHelpers.getField(realObject, "mDisplayInfo");
    displayInfo = new DisplayInfo(displayInfo);

    overrideDisplayInfo(displayInfo);

    ReflectionHelpers.setField(realObject, "mDisplayInfo", displayInfo);
  }

  /**
   * If {@link #setScaledDensity(float)} has been called, {@link DisplayMetrics#scaledDensity}
   * will be modified to reflect the value specified. Note that this is not a realistic state.
   *
   * @deprecated This behavior is deprecated and will be removed in Robolectric 3.7.
   */
  @Deprecated
  @Implementation
  public void getMetrics(DisplayMetrics outMetrics) {
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
   * If {@link #setScaledDensity(float)} has been called, {@link DisplayMetrics#scaledDensity}
   * will be modified to reflect the value specified. Note that this is not a realistic state.
   *
   * @deprecated This behavior is deprecated and will be removed in Robolectric 3.7.
   */
  @Deprecated
  @Implementation
  public void getRealMetrics(DisplayMetrics outMetrics) {
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
  public int getDisplayId() {
    return displayId == null
        ? directlyOn(realObject, Display.class).getDisplayId()
        : displayId;
  }

  /**
   * If {@link #setFlags(int)} has been called, this method will return the specified value.
   *
   * @deprecated This behavior is deprecated and will be removed in Robolectric 3.7.
   */
  @Deprecated
  @Implementation
  public int getFlags() {
    return flags == null
        ? directlyOn(realObject, Display.class).getFlags()
        : flags;
  }

  /**
   * If {@link #setRefreshRate(float)} has been called, this method will return the specified value.
   *
   * @deprecated This behavior is deprecated and will be removed in Robolectric 3.7.
   */
  @Deprecated
  @Implementation
  public float getRefreshRate() {
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
  public int getPixelFormat() {
    return pixelFormat == null
        ? directlyOn(realObject, Display.class).getPixelFormat()
        : pixelFormat;
  }

  @Implementation(maxSdk = JELLY_BEAN)
  public void getSizeInternal(Point outSize, boolean doCompat) {
    outSize.x = width;
    outSize.y = height;
  }

  @Implementation(maxSdk = JELLY_BEAN)
  public void getCurrentSizeRange(Point outSmallestSize, Point outLargestSize) {
    int minimum = Math.min(width, height);
    int maximum = Math.max(width, height);
    outSmallestSize.set(minimum, minimum);
    outLargestSize.set(maximum, maximum);
  }

  @Implementation(maxSdk = JELLY_BEAN)
  public void getRealSize(Point outSize) {
    outSize.set(realWidth, realHeight);
  }

  /**
   * Overrides the density for this display.
   *
   * @deprecated This method is deprecated and will be removed in Robolectric 3.7.
   */
  @Deprecated
  public void setDensity(float density) {
    this.densityDpi = ((int) (density * DisplayMetrics.DENSITY_DEFAULT));
  }

  /**
   * Overrides the density for this display.
   *
   * @deprecated This method is deprecated and will be removed in Robolectric 3.7.
   */
  @Deprecated
  public void setDensityDpi(int densityDpi) {
    this.densityDpi = densityDpi;
  }

  /**
   * Overrides the horizontal DPI for this display.
   *
   * @deprecated This method is deprecated and will be removed in Robolectric 3.7.
   */
  @Deprecated
  public void setXdpi(float xdpi) {
    this.xdpi = xdpi;
  }

  /**
   * Overrides the vertical DPI for this display.
   *
   * @deprecated This method is deprecated and will be removed in Robolectric 3.7.
   */
  @Deprecated
  public void setYdpi(float ydpi) {
    this.ydpi = ydpi;
  }

  /**
   * Overrides the scaled density for this display.
   *
   * @deprecated This method is deprecated and will be removed in Robolectric 3.7.
   */
  @Deprecated
  public void setScaledDensity(float scaledDensity) {
    this.scaledDensity = scaledDensity;
  }

  /**
   * Overrides the ID for this display.
   *
   * @deprecated This method is deprecated and will be removed in Robolectric 3.7.
   */
  @Deprecated
  public void setDisplayId(int displayId) {
    this.displayId = displayId;
  }

  /**
   * Overrides the name for this display.
   *
   * @deprecated This method is deprecated and will be removed in Robolectric 3.7.
   */
  @Deprecated
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Overrides the flags for this display.
   *
   * @deprecated This method is deprecated and will be removed in Robolectric 3.7.
   */
  @Deprecated
  public void setFlags(int flags) {
    this.flags = flags;
  }

  /**
   * Overrides the width for this display.
   *
   * @deprecated This method is deprecated and will be removed in Robolectric 3.7.
   */
  @Deprecated
  public void setWidth(int width) {
    this.width = width;
  }

  /**
   * Overrides the height for this display.
   *
   * @deprecated This method is deprecated and will be removed in Robolectric 3.7.
   */
  @Deprecated
  public void setHeight(int height) {
    this.height = height;
  }

  /**
   * Overrides the logical width for this display.
   *
   * @deprecated This method is deprecated and will be removed in Robolectric 3.7.
   */
  @Deprecated
  public void setRealWidth(int realWidth) {
    this.realWidth = realWidth;
  }

  /**
   * Overrides the logical height for this display.
   *
   * @deprecated This method is deprecated and will be removed in Robolectric 3.7.
   */
  @Deprecated
  public void setRealHeight(int realHeight) {
    this.realHeight = realHeight;
  }

  /**
   * Overrides the refresh rate for this display.
   *
   * @deprecated This method is deprecated and will be removed in Robolectric 3.7.
   */
  @Deprecated
  public void setRefreshRate(float refreshRate) {
    this.refreshRate = refreshRate;
  }

  /**
   * Overrides the rotation for this display.
   *
   * @deprecated This method is deprecated and will be removed in Robolectric 3.7.
   */
  @Deprecated
  public void setRotation(int rotation) {
    this.rotation = rotation;
  }

  /**
   * Overrides the density for this display.
   *
   * @deprecated This method is deprecated and will be removed in Robolectric 3.7.
   */
  @Deprecated
  public void setPixelFormat(int pixelFormat) {
    this.pixelFormat = pixelFormat;
  }

  private void overrideDisplayInfo(DisplayInfo displayInfo) {
    if (name != null) {
      displayInfo.name = name;
    }

    if (flags != null) {
      displayInfo.flags = flags;
    }

    if (densityDpi != null) {
      displayInfo.logicalDensityDpi = densityDpi;
    }

    if (xdpi != null) {
      displayInfo.physicalXDpi = xdpi;
    }

    if (ydpi != null) {
      displayInfo.physicalYDpi = ydpi;
    }

    if (width != null) {
      displayInfo.appWidth = width;
    }

    if (height != null) {
      displayInfo.appHeight = height;
    }

    if (width != null || height != null) {
      int effectiveWidth = width == null ? displayInfo.appWidth : width;
      int effectiveHeight = height == null ? displayInfo.appHeight : height;
      int min = Math.min(effectiveWidth, effectiveHeight);
      int max = Math.max(effectiveWidth, effectiveHeight);
      displayInfo.smallestNominalAppHeight = displayInfo.smallestNominalAppWidth = min;
      displayInfo.largestNominalAppHeight = displayInfo.largestNominalAppWidth = max;
    }

    if (realWidth != null) {
      displayInfo.logicalWidth = realWidth;
    }

    if (realHeight != null) {
      displayInfo.logicalHeight = realHeight;
    }

    if (rotation != null) {
      displayInfo.rotation = rotation;
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
    flags = 0;
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
}
