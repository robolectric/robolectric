package org.robolectric.shadows;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.widget.ViewFlipper;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class ShadowViewFlipperTest {
  protected ViewFlipper flipper;

  @Before
  public void setUp() {
    flipper = new ViewFlipper(ApplicationProvider.getApplicationContext());
  }

  @Test
  public void testStartFlipping() {
    flipper.startFlipping();
    assertTrue("flipping", flipper.isFlipping());
  }

  @Test
  public void testStopFlipping() {
    flipper.stopFlipping();
    assertFalse("flipping", flipper.isFlipping());
  }
}
