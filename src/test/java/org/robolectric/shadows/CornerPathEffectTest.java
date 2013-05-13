package org.robolectric.shadows;

import android.graphics.CornerPathEffect;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.TestRunners;

import static junit.framework.Assert.assertEquals;
import static org.robolectric.Robolectric.shadowOf;


@RunWith(TestRunners.WithDefaults.class)
public class CornerPathEffectTest {
  @Test
  public void shouldGetRadius() throws Exception {
    CornerPathEffect cornerPathEffect = new CornerPathEffect(4.0f);
    assertEquals(4.0f, shadowOf(cornerPathEffect).getRadius());
  }
}
