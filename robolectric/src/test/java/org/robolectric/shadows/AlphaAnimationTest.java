package org.robolectric.shadows;

import android.view.animation.AlphaAnimation;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.TestRunners;

import static junit.framework.Assert.assertEquals;
import static org.robolectric.Robolectric.shadowOf;

@RunWith(TestRunners.WithDefaults.class)
public class AlphaAnimationTest {
  @Test
  public void getFromAlpha_shouldReturnTheFromAlphaThatWasPassedToTheConstructor() throws Exception {
    assertEquals(99.0f, shadowOf(new AlphaAnimation(99, 88)).getFromAlpha());
    assertEquals(66.7f, shadowOf(new AlphaAnimation(66.7f, 88)).getFromAlpha());
  }

  @Test
  public void getToAlpha_shouldReturnTheToAlphaThatWasPassedToTheConstructor() throws Exception {
    assertEquals(88.0f, shadowOf(new AlphaAnimation(99, 88)).getToAlpha());
    assertEquals(98f, shadowOf(new AlphaAnimation(66.7f, 98)).getToAlpha());
  }
}
