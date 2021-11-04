package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;

import android.compat.Compatibility;
import android.os.Build;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

/** Tests to make sure {@link android.compat.Compatibility} is instrumented correctly */
@RunWith(RobolectricTestRunner.class)
@Config(minSdk = Build.VERSION_CODES.S)
public class CompatibilityTest {
  @Test
  public void isChangeEnabled() {
    assertThat(Compatibility.isChangeEnabled(100)).isTrue();
  }

  @Test
  public void reportUnconditionalChange() {
    // Verify this does not cause a crash due to uninstrumented System.logW.
    Compatibility.reportUnconditionalChange(100);
  }
}
