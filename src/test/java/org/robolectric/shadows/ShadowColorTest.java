package org.robolectric.shadows;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.TestRunners;

import android.graphics.Color;

/**
 * Test of {@link ShadowColor}.
 *
 * @author Christian Ihle
 */
@RunWith(TestRunners.WithDefaults.class)
public class ShadowColorTest {

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
