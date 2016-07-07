package org.robolectric.shadows;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.os.Build;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.TestRunners;
import org.robolectric.annotation.Config;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(TestRunners.MultiApiWithDefaults.class)
@Config(sdk = {
    Build.VERSION_CODES.LOLLIPOP })
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

  @Test
  public void hashCode_returnsDifferentValuesForDifferentModes() {
    PorterDuffColorFilter addFilter = new PorterDuffColorFilter(Color.RED, PorterDuff.Mode.ADD);
    PorterDuffColorFilter dstFilter = new PorterDuffColorFilter(Color.RED, PorterDuff.Mode.DST);

    assertThat(addFilter.hashCode()).isNotEqualTo(dstFilter.hashCode());
  }

  @Test
  public void hashCode_returnsDifferentValuesForDifferentColors() {
    PorterDuffColorFilter blueFilter = new PorterDuffColorFilter(Color.BLUE, PorterDuff.Mode.ADD);
    PorterDuffColorFilter redFilter = new PorterDuffColorFilter(Color.RED, PorterDuff.Mode.ADD);

    assertThat(blueFilter.hashCode()).isNotEqualTo(redFilter.hashCode());
  }
}
