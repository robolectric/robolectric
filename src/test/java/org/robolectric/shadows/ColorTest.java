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
}