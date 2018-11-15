package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.O;
import static com.google.common.truth.Truth.assertThat;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

@RunWith(AndroidJUnit4.class)
@Config(minSdk = LOLLIPOP)
public class ShadowPorterDuffColorFilterTest {
  @Test
  public void constructor_shouldWork() {
    final PorterDuffColorFilter filter = new PorterDuffColorFilter(Color.RED, PorterDuff.Mode.ADD);
    assertThat(filter.getColor()).isEqualTo(Color.RED);
    assertThat(filter.getMode()).isEqualTo(PorterDuff.Mode.ADD);
  }

  @Config(minSdk = O)
  @Test
  public void createNativeInstance_shouldWork() {
    final PorterDuffColorFilter filter = new PorterDuffColorFilter(Color.RED, PorterDuff.Mode.ADD);
    assertThat(filter.getNativeInstance()).isEqualTo(0L);
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

  @Test
  public void equals_returnsTrueForEqualObjects() {
    PorterDuffColorFilter filter1 = new PorterDuffColorFilter(Color.RED, PorterDuff.Mode.ADD);
    PorterDuffColorFilter filter2 = new PorterDuffColorFilter(Color.RED, PorterDuff.Mode.ADD);

    assertThat(filter1).isEqualTo(filter2);
  }

  @Test
  public void equals_returnsFalseForDifferentModes() {
    PorterDuffColorFilter addFilter = new PorterDuffColorFilter(Color.RED, PorterDuff.Mode.ADD);
    PorterDuffColorFilter dstFilter = new PorterDuffColorFilter(Color.RED, PorterDuff.Mode.DST);

    assertThat(addFilter).isNotEqualTo(dstFilter);
  }

  @Test
  public void equals_returnsFalseForDifferentColors() {
    PorterDuffColorFilter blueFilter = new PorterDuffColorFilter(Color.BLUE, PorterDuff.Mode.ADD);
    PorterDuffColorFilter redFilter = new PorterDuffColorFilter(Color.RED, PorterDuff.Mode.ADD);

    assertThat(blueFilter).isNotEqualTo(redFilter);
  }
}
