package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;

import android.graphics.Color;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class ShadowColorTest {

  @Test
  public void testRgb() {
    int color = Color.rgb(160, 160, 160);
    assertThat(color).isEqualTo(-6250336);
  }

  @Test
  public void testArgb() {
    int color = Color.argb(100, 160, 160, 160);
    assertThat(color).isEqualTo(1688248480);
  }

  @Test
  public void testParseColor() throws Exception {
    assertThat(Color.parseColor("#ffffffff")).isEqualTo(-1);
    assertThat(Color.parseColor("#00000000")).isEqualTo(0);

    assertThat(Color.parseColor("#ffaabbcc")).isEqualTo(-5588020);
  }

  @Test
  public void testParseColorWithStringName() {
    assertThat(Color.parseColor("blue")).isEqualTo(-16776961);
    assertThat(Color.parseColor("black")).isEqualTo(-16777216);
    assertThat(Color.parseColor("green")).isEqualTo(-16711936);
  }

  @Test
  public void colorToHSVShouldBeCorrectForBlue() {
    float[] hsv = new float[3];
    Color.colorToHSV(Color.BLUE, hsv);

    assertThat(hsv[0]).isEqualTo(240f);
    assertThat(hsv[1]).isEqualTo(1.0f);
    assertThat(hsv[2]).isEqualTo(1.0f);
  }

  @Test
  public void colorToHSVShouldBeCorrectForBlack() {
    float[] hsv = new float[3];
    Color.colorToHSV(Color.BLACK, hsv);

    assertThat(hsv[0]).isEqualTo(0f);
    assertThat(hsv[1]).isEqualTo(0f);
    assertThat(hsv[2]).isEqualTo(0f);
  }

  @Test
  public void RGBToHSVShouldBeCorrectForBlue() {
    float[] hsv = new float[3];
    Color.RGBToHSV(0, 0, 255, hsv);

    assertThat(hsv[0]).isEqualTo(240f);
    assertThat(hsv[1]).isEqualTo(1.0f);
    assertThat(hsv[2]).isEqualTo(1.0f);
  }

  @Test
  public void HSVToColorShouldReverseColorToHSV() {
      float[] hsv = new float[3];
      Color.colorToHSV(Color.RED, hsv);

      assertThat(Color.HSVToColor(hsv)).isEqualTo(Color.RED);
  }
}
