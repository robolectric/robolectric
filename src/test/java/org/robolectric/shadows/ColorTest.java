package org.robolectric.shadows;

import android.graphics.Color;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.TestRunners;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

@RunWith(TestRunners.WithDefaults.class)
public class ColorTest {

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
    assertEquals(-1, Color.parseColor("#ffffffff"));
    assertEquals(0, Color.parseColor("#00000000"));
    assertEquals(-5588020, Color.parseColor("#ffaabbcc"));
    assertEquals(-5588020, Color.parseColor("#fabc"));
    assertEquals(-5588020, Color.parseColor("#abc"));
  }

  /**
   * Color.BLUE is hex(#0000FF), rgb(0, 0, 255) and hsv(240, 1, 1).
   *
   * <p>From the javadoc of {@link Color#colorToHSV(int, float[])}:<br/>
   * <code>hsv[0] is Hue [0 .. 360) hsv[1] is Saturation [0...1] hsv[2] is Value [0...1]</code></p>
   */
  @Test
  public void colorToHSVShouldBeCorrectForBlue() {
    float[] hsv = new float[3];

    Color.colorToHSV(Color.BLUE, hsv);

    assertEquals(240f, hsv[0], 0);
    assertEquals(1.0f, hsv[1], 0);
    assertEquals(1.0f, hsv[2], 0);
  }

  @Test
  public void colorToHSVShouldBeCorrectForBlack() {
    float[] hsv = new float[3];

    Color.colorToHSV(Color.BLACK, hsv);

    assertEquals(0f, hsv[0], 0);
    assertEquals(0f, hsv[1], 0);
    assertEquals(0f, hsv[2], 0);
  }

  @Test
  public void RGBToHSVShouldBeCorrectForBlue() {
    float[] hsv = new float[3];

    Color.RGBToHSV(0, 0, 255, hsv);

    assertEquals(240f, hsv[0], 0);
    assertEquals(1.0f, hsv[1], 0);
    assertEquals(1.0f, hsv[2], 0);
  }
}
