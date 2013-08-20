package org.robolectric.shadows;

import android.graphics.Point;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.view.Display;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.TestRunners;

import static org.junit.Assert.assertEquals;
import static org.robolectric.Robolectric.newInstanceOf;
import static org.robolectric.Robolectric.shadowOf;

@RunWith(TestRunners.WithDefaults.class)
public class DisplayTest {
  @Test
  public void shouldProvideDisplayMetrics() throws Exception {

    Display display = newInstanceOf(Display.class);
    ShadowDisplay shadow = shadowOf(display);

    shadow.setDensity(1.5f);
    shadow.setDensityDpi(DisplayMetrics.DENSITY_MEDIUM);
    shadow.setScaledDensity(1.6f);
    shadow.setWidth(1024);
    shadow.setHeight(600);
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
  }

  @Test
  public void shouldProvideDisplaySize() throws Exception {
    Point outSmallestSize = new Point();
    Point outLargestSize = new Point();
    Point outSize = new Point();
    Rect outRect = new Rect();

    Display display = newInstanceOf(Display.class);
    ShadowDisplay shadow = shadowOf(display);

    shadow.setWidth(400);
    shadow.setHeight(600);

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
  }

  /**
   * The {@link android.view.Display#getOrientation()} method is deprecated, but for
   * testing purposes, return the value gotten from {@link android.view.Display#getRotation()}
   */
  @Test
  public void deprecatedGetOrientation_returnsGetRotation() {
    Display display = newInstanceOf(Display.class);
    int testValue = 33;
    shadowOf(display).setRotation(testValue);
    assertEquals(testValue, display.getOrientation());
  }

}
