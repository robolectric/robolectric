package org.robolectric.shadows;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.TestRunners;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(TestRunners.WithDefaults.class)
public class ShadowPorterDuffColorFilterTest {
  @Test
  public void constructor_shouldWork() {
    final PorterDuffColorFilter filter = new PorterDuffColorFilter(Color.RED, PorterDuff.Mode.ADD);
    assertThat(filter.getColor()).isEqualTo(Color.RED);
    assertThat(filter.getMode()).isEqualTo(PorterDuff.Mode.ADD);
  }

  @Test
  public void setColor_shouldWork() {
    final PorterDuffColorFilter filter = new PorterDuffColorFilter(Color.RED, PorterDuff.Mode.ADD);
    filter.setColor(Color.BLUE);
    assertThat(filter.getColor()).isEqualTo(Color.BLUE);
  }

  @Test
  public void setMode_shouldWork() {
    final PorterDuffColorFilter filter = new PorterDuffColorFilter(Color.RED, PorterDuff.Mode.ADD);
    filter.setMode(PorterDuff.Mode.DST_IN);
    assertThat(filter.getMode()).isEqualTo(PorterDuff.Mode.DST_IN);
  }
}
