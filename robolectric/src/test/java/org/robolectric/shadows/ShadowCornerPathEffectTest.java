package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.graphics.CornerPathEffect;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class ShadowCornerPathEffectTest {
  @Test
  public void shouldGetRadius() throws Exception {
    CornerPathEffect cornerPathEffect = new CornerPathEffect(4.0f);
    assertThat(shadowOf(cornerPathEffect).getRadius()).isEqualTo(4.0f);
  }
}
