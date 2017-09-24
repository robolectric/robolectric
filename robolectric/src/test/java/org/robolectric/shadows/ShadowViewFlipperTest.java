package org.robolectric.shadows;

import static org.junit.Assert.assertEquals;

import android.widget.ViewFlipper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class ShadowViewFlipperTest {
  protected ViewFlipper flipper;

  @Before
  public void setUp() {
    flipper = new ViewFlipper(RuntimeEnvironment.application);
  }

  @Test
  public void testStartFlipping() {
    flipper.startFlipping();
    assertEquals("flipping", true, flipper.isFlipping());
  }

  @Test
  public void testStopFlipping() {
    flipper.stopFlipping();
    assertEquals("flipping", false, flipper.isFlipping());
  }
}
