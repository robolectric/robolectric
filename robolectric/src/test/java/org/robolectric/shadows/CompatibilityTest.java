package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE;
import static android.os.Build.VERSION_CODES.VANILLA_ICE_CREAM;
import static com.google.common.truth.Truth.assertThat;

import android.compat.Compatibility;
import android.os.Build;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.experimental.LazyApplication;
import org.robolectric.annotation.experimental.LazyApplication.LazyLoad;

/** Tests to make sure {@link android.compat.Compatibility} is instrumented correctly */
@RunWith(RobolectricTestRunner.class)
@Config(minSdk = Build.VERSION_CODES.S)
public class CompatibilityTest {

  private static final long ENFORCE_EDGE_TO_EDGE = 309578419L;

  @Test
  public void isChangeEnabled() {
    assertThat(Compatibility.isChangeEnabled(100)).isTrue();
  }

  @Test
  public void reportUnconditionalChange() {
    // Verify this does not cause a crash due to uninstrumented System.logW.
    Compatibility.reportUnconditionalChange(100);
  }

  @Test
  @LazyApplication(LazyLoad.OFF)
  public void isChangeEnabled_logging() {
    Compatibility.isChangeEnabled(100);
    // verify there are no CompatibilityChangeReporter spam logs
    assertThat(ShadowLog.getLogsForTag("CompatibilityChangeReporter")).isEmpty();
  }

  @Test
  public void edgeToEdgeEnforcement_minSdk() {
    assertThat(ShadowCompatibility.isEnabled(ENFORCE_EDGE_TO_EDGE, UPSIDE_DOWN_CAKE)).isFalse();
    assertThat(ShadowCompatibility.isEnabled(ENFORCE_EDGE_TO_EDGE, VANILLA_ICE_CREAM)).isTrue();
  }
}
