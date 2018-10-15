package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR1;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static org.junit.Assert.assertEquals;

import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.display.DisplayManagerGlobal;
import android.util.DisplayMetrics;
import android.view.Display;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;

@RunWith(AndroidJUnit4.class)
@Config(minSdk = JELLY_BEAN_MR1)
public class ShadowDisplayTest {

  private Display display;
  private ShadowDisplay shadow;

  @Before
  public void setUp() throws Exception {
    display = DisplayManagerGlobal.getInstance().getRealDisplay(Display.DEFAULT_DISPLAY);
    shadow = Shadows.shadowOf(display);
  }

  @Test
  public void shouldProvideDisplayMetrics() throws Exception {
    shadow.setDensity(1.5f);
    shadow.setDensityDpi(DisplayMetrics.DENSITY_HIGH);
    shadow.setScaledDensity(1.6f);
    shadow.setWidth(1024);
    shadow.setHeight(600);
    shadow.setRealWidth(1400);
    shadow.setRealHeight(900);
    shadow.setXdpi(183.0f);
    shadow.setYdpi(184.0f);
    shadow.setRefreshRate(123f);

    DisplayMetrics metrics = new DisplayMetrics();

    display.getMetrics(metrics);

    assertEquals(1.5f, metrics.density, 0.05);
    assertEquals(DisplayMetrics.DENSITY_HIGH, metrics.densityDpi);
    assertEquals(1.6f, metrics.scaledDensity, 0.05);
    assertEquals(1024, metrics.widthPixels);
    assertEquals(600, metrics.heightPixels);
    assertEquals(183.0f, metrics.xdpi, 0.05);
    assertEquals(184.0f, metrics.ydpi, 0.05);

    metrics = new DisplayMetrics();

    display.getRealMetrics(metrics);

    assertEquals(1.5f, metrics.density, 0.05);
    assertEquals(DisplayMetrics.DENSITY_HIGH, metrics.densityDpi);
    assertEquals(1.6f, metrics.scaledDensity, 0.05);
    assertEquals(1400, metrics.widthPixels);
    assertEquals(900, metrics.heightPixels);
    assertEquals(183.0f, metrics.xdpi, 0.05);
    assertEquals(184.0f, metrics.ydpi, 0.05);

    assertEquals(0, 123f, display.getRefreshRate());
  }

  @Test
  public void changedStateShouldApplyToOtherInstancesOfSameDisplay() throws Exception {
    shadow.setName("another name");
    shadow.setWidth(1024);
    shadow.setHeight(600);

    display = DisplayManagerGlobal.getInstance().getRealDisplay(Display.DEFAULT_DISPLAY);
    assertEquals(1024, display.getWidth());
    assertEquals(600, display.getHeight());
    assertEquals("another name", display.getName());
  }

  @Test @Config(minSdk = LOLLIPOP)
  public void stateChangeShouldApplyToOtherInstancesOfSameDisplay_postKitKatFields() throws Exception {
    shadow.setState(Display.STATE_DOZE_SUSPEND);

    display = DisplayManagerGlobal.getInstance().getRealDisplay(Display.DEFAULT_DISPLAY);
    assertEquals(Display.STATE_DOZE_SUSPEND, display.getState());
  }

  @Test
  public void shouldProvideDisplaySize() throws Exception {
    Point outSmallestSize = new Point();
    Point outLargestSize = new Point();
    Point outSize = new Point();
    Rect outRect = new Rect();

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
  public void shouldProvideWeirdDisplayInformation() {
    shadow.setName("foo");
    shadow.setFlags(123);

    assertEquals("foo", display.getName());
    assertEquals(123, display.getFlags());

    display = DisplayManagerGlobal.getInstance().getRealDisplay(Display.DEFAULT_DISPLAY);
    assertEquals(123, display.getFlags());
  }

  /**
   * The {@link android.view.Display#getOrientation()} method is deprecated, but for
   * testing purposes, return the value gotten from {@link android.view.Display#getRotation()}
   */
  @Test
  public void deprecatedGetOrientation_returnsGetRotation() {
    int testValue = 33;
    shadow.setRotation(testValue);
    assertEquals(testValue, display.getOrientation());
  }
}
