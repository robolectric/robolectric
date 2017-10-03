package org.robolectric.shadows;

import static junit.framework.Assert.assertEquals;
import static org.robolectric.Shadows.shadowOf;

import android.graphics.CornerPathEffect;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class ShadowCornerPathEffectTest {
  @Test
  public void shouldGetRadius() throws Exception {
    CornerPathEffect cornerPathEffect = new CornerPathEffect(4.0f);
    assertEquals(4.0f, shadowOf(cornerPathEffect).getRadius());
  }
}
