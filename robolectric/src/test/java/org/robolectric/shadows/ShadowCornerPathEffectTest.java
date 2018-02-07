package org.robolectric.shadows;

import static org.assertj.core.api.Assertions.assertThat;
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
    assertThat(shadowOf(cornerPathEffect).getRadius()).isEqualTo(4.0f);
  }
}
