package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR1;
import static org.junit.Assert.assertEquals;

import android.graphics.Point;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.view.Display;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Shadows;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadow.api.Shadow;

@RunWith(RobolectricTestRunner.class)
public class ShadowDisplayTest {
  @Test
  public void shouldProvideDisplayMetrics() throws Exception {
    Display display = Shadow.newInstanceOf(Display.class);
    ShadowDisplay shadow = Shadows.shadowOf(display);

    shadow.setDensity(1.5f);
    shadow.setDensityDpi(DisplayMetrics.DENSITY_MEDIUM);
    shadow.setScaledDensity(1.6f);
    shadow.setWidth(1024);
    shadow.setHeight(600);
    shadow.setRealWidth(1400);
    shadow.setRealHeight(900);
    shadow.setXdpi(183.0f);
    shadow.setYdpi(184.0f);

    DisplayMetrics metrics = new DisplayMetrics();

    display.getMetrics(metrics);

    assertEquals(1.5f, metrics.density, 0.05);
    assertEquals(DisplayMetrics.DENSITY_MEDIUM, metrics.densityDpi);
    assertEquals(1.6f, metrics.scaledDensity, 0.05);
    assertEquals(1024, metrics.widthPixels);
    assertEquals(600, metrics.heightPixels);
    assertEquals(183.0f, metrics.xdpi, 0.05);
    assertEquals(184.0f, metrics.ydpi, 0.05);

    metrics = new DisplayMetrics();

    display.getRealMetrics(metrics);

    assertEquals(1.5f, metrics.density, 0.05);
    assertEquals(DisplayMetrics.DENSITY_MEDIUM, metrics.densityDpi);
    assertEquals(1.6f, metrics.scaledDensity, 0.05);
    assertEquals(1400, metrics.widthPixels);
    assertEquals(900, metrics.heightPixels);
    assertEquals(183.0f, metrics.xdpi, 0.05);
    assertEquals(184.0f, metrics.ydpi, 0.05);
  }

  @Test
  public void shouldProvideDisplaySize() throws Exception {
    Point outSmallestSize = new Point();
    Point outLargestSize = new Point();
    Point outSize = new Point();
    Rect outRect = new Rect();

    Display display = Shadow.newInstanceOf(Display.class);
    ShadowDisplay shadow = Shadows.shadowOf(display);

    shadow.setWidth(400);
    shadow.setHeight(600);
    shadow.setRealWidth(480);
    shadow.setRealHeight(800);

    display.getCurrentSizeRange(outSmallestSize, outLargestSize);
    assertEquals(400, outSmallestSize.x);
    assertEquals(400, outSmallestSize.y);
    assertEquals(600, outLargestSize.x);
    assertEquals(600, outLargestSize.y);

    display.getSize(outSize);
    assertEquals(400, outSize.x);
    assertEquals(600, outSize.y);

    display.getRectSize(outRect);
    assertEquals(400, outRect.width());
    assertEquals(600, outRect.height());

    display.getRealSize(outSize);
    assertEquals(480, outSize.x);
    assertEquals(800, outSize.y);
  }

  @Test
  @Config(minSdk = JELLY_BEAN_MR1)
  public void shouldProvideDisplayInformation() {
    Display display = Shadow.newInstanceOf(Display.class);
    ShadowDisplay shadow = Shadows.shadowOf(display);

    shadow.setDisplayId(42);
    shadow.setName("foo");
    shadow.setFlags(8);

    assertEquals(42, display.getDisplayId());
    assertEquals("foo", display.getName());
    assertEquals(8, display.getFlags());
  }

  /**
   * The {@link android.view.Display#getOrientation()} method is deprecated, but for
   * testing purposes, return the value gotten from {@link android.view.Display#getRotation()}
   */
  @Test
  public void deprecatedGetOrientation_returnsGetRotation() {
    Display display = Shadow.newInstanceOf(Display.class);
    int testValue = 33;
    Shadows.shadowOf(display).setRotation(testValue);
    assertEquals(testValue, display.getOrientation());
  }
}
