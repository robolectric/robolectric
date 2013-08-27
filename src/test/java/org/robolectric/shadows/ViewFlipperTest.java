package org.robolectric.shadows;

import android.widget.ViewFlipper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.TestRunners;

import static org.junit.Assert.assertEquals;

@RunWith(TestRunners.WithDefaults.class)
public class ViewFlipperTest {
  protected ViewFlipper flipper;

  @Before
  public void setUp() {
    flipper = new ViewFlipper(Robolectric.application);
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
