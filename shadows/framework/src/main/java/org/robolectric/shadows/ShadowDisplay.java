package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.KITKAT;
import static android.os.Build.VERSION_CODES.LOLLIPOP;

import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.DisplayAdjustments;
import android.view.Surface;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(value = Display.class)
public class ShadowDisplay {
  @RealObject Display realObject;

  private int displayId;
  private String name = "Default Display";
  private int flags;
  private int width = 480;
  private int height = 800;
  private int realWidth = 480;
  private int realHeight = 854;
  private float density = 1.5f;
  private int densityDpi = DisplayMetrics.DENSITY_HIGH;
  private float xdpi = 240.0f;
  private float ydpi = 240.0f;
  private float scaledDensity = 1.0f;
  private float refreshRate = 60.0f;
  private int rotation = Surface.ROTATION_0;
  private int pixelFormat = PixelFormat.RGBA_4444;

  @Implementation
  public int getHeight() {
    return height;
  }

  @Implementation
  public void getMetrics(DisplayMetrics outMetrics) {
    outMetrics.density = density;
    outMetrics.densityDpi = densityDpi;
    outMetrics.scaledDensity = scaledDensity;
    outMetrics.widthPixels = width;
    outMetrics.heightPixels = height;
    outMetrics.xdpi = xdpi;
    outMetrics.ydpi = ydpi;
  }

  @Implementation
  public void getRealMetrics(DisplayMetrics outMetrics) {
    getMetrics(outMetrics);
    outMetrics.widthPixels = realWidth;
    outMetrics.heightPixels = realHeight;
  }

  @Implementation
  public int getWidth() {
    return width;
  }

  @Implementation
  public int getDisplayId() {
    return displayId;
  }

  @Implementation
  public String getName() {
    return name;
  }

  @Implementation
  public int getFlags() {
    return flags;
  }

  @Implementation
  public float getRefreshRate() {
    return refreshRate;
  }

  @Implementation
  public int getOrientation() {
    return getRotation();
  }

  @Implementation
  public int getRotation() {
    return rotation;
  }

  @Implementation
  public int getPixelFormat() {
    return pixelFormat;
  }

  @Implementation
  public void getCurrentSizeRange(Point outSmallestSize, Point outLargestSize) {
    int minimum = Math.min(width, height);
    int maximum = Math.max(width, height);
    outSmallestSize.set(minimum, minimum);
    outLargestSize.set(maximum, maximum);
  }

  @Implementation
  public void getSize(Point outSize) {
    outSize.set(width, height);
  }

  @Implementation
  public void getRectSize(Rect outSize) {
    outSize.set(0, 0, width, height);
  }

  @Implementation
  public void getRealSize(Point outSize) {
    outSize.set(realWidth, realHeight);
  }

  @Implementation(minSdk = LOLLIPOP)
  public int getState() {
    return Display.STATE_ON;
  }

  public float getDensity() {
    return density;
  }

  public void setDensity(float density) {
    this.density = density;
  }

  public int getDensityDpi() {
    return densityDpi;
  }

  public void setDensityDpi(int densityDpi) {
    this.densityDpi = densityDpi;
  }

  public float getXdpi() {
    return xdpi;
  }

  public void setXdpi(float xdpi) {
    this.xdpi = xdpi;
  }

  public float getYdpi() {
    return ydpi;
  }

  public void setYdpi(float ydpi) {
    this.ydpi = ydpi;
  }

  public float getScaledDensity() {
    return scaledDensity;
  }

  public void setScaledDensity(float scaledDensity) {
    this.scaledDensity = scaledDensity;
  }

  public void setDisplayId(int displayId) {
    this.displayId = displayId;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setFlags(int flags) {
    this.flags = flags;
  }

  public void setWidth(int width) {
    this.width = width;
  }

  public void setHeight(int height) {
    this.height = height;
  }

  public void setRealWidth(int realWidth) {
    this.realWidth = realWidth;
  }

  public void setRealHeight(int realHeight) {
    this.realHeight = realHeight;
  }

  public void setRefreshRate(float refreshRate) {
    this.refreshRate = refreshRate;
  }

  public void setRotation(int rotation) {
    this.rotation = rotation;
  }

  public void setPixelFormat(int pixelFormat) {
    this.pixelFormat = pixelFormat;
  }

  @Implementation(minSdk = KITKAT)
  public Object getDisplayAdjustments() {
    return new DisplayAdjustments();
  }
}
