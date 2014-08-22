package org.robolectric.shadows;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.TestRunners;

import static org.fest.assertions.api.Assertions.assertThat;

@RunWith(TestRunners.WithDefaults.class)
public class PorterDuffColorFilterTest {
  @Test
  public void testConstructor() {
    PorterDuffColorFilter colorFilter = new PorterDuffColorFilter(Color.RED, PorterDuff.Mode.ADD);
    ShadowPorterDuffColorFilter shadow = Robolectric.shadowOf_(colorFilter);

    assertThat(shadow.getSrcColor()).isEqualTo(Color.RED);
    assertThat(shadow.getMode()).isEqualTo(PorterDuff.Mode.ADD);
  }
}
