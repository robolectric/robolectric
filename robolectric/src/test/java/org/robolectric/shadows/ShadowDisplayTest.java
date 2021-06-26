package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR1;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.M;
import static android.os.Build.VERSION_CODES.N;
import static android.os.Build.VERSION_CODES.Q;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import android.graphics.Insets;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.display.DisplayManagerGlobal;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Display.HdrCapabilities;
import android.view.DisplayCutout;
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
  public void shouldProvideDisplayMetrics() {
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
  public void changedStateShouldApplyToOtherInstancesOfSameDisplay() {
    shadow.setName("another name");
    shadow.setWidth(1024);
    shadow.setHeight(600);

    display = DisplayManagerGlobal.getInstance().getRealDisplay(Display.DEFAULT_DISPLAY);
    assertEquals(1024, display.getWidth());
    assertEquals(600, display.getHeight());
    assertEquals("another name", display.getName());
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void stateChangeShouldApplyToOtherInstancesOfSameDisplay_postKitKatFields() {
    shadow.setState(Display.STATE_DOZE_SUSPEND);

    display = DisplayManagerGlobal.getInstance().getRealDisplay(Display.DEFAULT_DISPLAY);
    assertEquals(Display.STATE_DOZE_SUSPEND, display.getState());
  }

  @Test
  public void shouldProvideDisplaySize() {
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

  @Test
  @Config(minSdk = N)
  public void setDisplayHdrCapabilities_shouldReturnHdrCapabilities() {
    Display display = ShadowDisplay.getDefaultDisplay();
    int[] hdrCapabilities =
        new int[] {HdrCapabilities.HDR_TYPE_HDR10, HdrCapabilities.HDR_TYPE_DOLBY_VISION};
    shadow.setDisplayHdrCapabilities(
        display.getDisplayId(),
        /* maxLuminance= */ 100f,
        /* maxAverageLuminance= */ 100f,
        /* minLuminance= */ 100f,
        hdrCapabilities);

    HdrCapabilities capabilities = display.getHdrCapabilities();

    assertThat(capabilities).isNotNull();
    assertThat(capabilities.getSupportedHdrTypes()).isEqualTo(hdrCapabilities);
    assertThat(capabilities.getDesiredMaxAverageLuminance()).isEqualTo(100f);
    assertThat(capabilities.getDesiredMaxLuminance()).isEqualTo(100f);
    assertThat(capabilities.getDesiredMinLuminance()).isEqualTo(100f);
  }

  @Test
  @Config(maxSdk = M)
  public void setDisplayHdrCapabilities_shouldThrowUnSupportedOperationExceptionPreN() {
    Display display = ShadowDisplay.getDefaultDisplay();
    int[] hdrCapabilities =
        new int[] {HdrCapabilities.HDR_TYPE_HDR10, HdrCapabilities.HDR_TYPE_DOLBY_VISION};
    try {
      shadow.setDisplayHdrCapabilities(
          display.getDisplayId(),
          /* maxLuminance= */ 100f,
          /* maxAverageLuminance= */ 100f,
          /* minLuminance= */ 100f,
          hdrCapabilities);
      fail();
    } catch (UnsupportedOperationException e) {
      assertThat(e).hasMessageThat().contains("HDR capabilities are not supported below Android N");
    }
  }

  @Test
  @Config(minSdk = N)
  public void setDisplayHdrCapabilities_shouldntThrowUnSupportedOperationExceptionNPlus() {
    Display display = ShadowDisplay.getDefaultDisplay();
    int[] hdrCapabilities =
        new int[] {HdrCapabilities.HDR_TYPE_HDR10, HdrCapabilities.HDR_TYPE_DOLBY_VISION};

    shadow.setDisplayHdrCapabilities(
        display.getDisplayId(),
        /* maxLuminance= */ 100f,
        /* maxAverageLuminance= */ 100f,
        /* minLuminance= */ 100f,
        hdrCapabilities);
  }

  @Test
  @Config(minSdk = Q)
  public void setDisplayCutout_returnsCutout() {
    DisplayCutout cutout = new DisplayCutout(Insets.of(0, 100, 0, 100), null, null, null, null);
    shadow.setDisplayCutout(cutout);

    assertEquals(cutout, display.getCutout());
  }
}
