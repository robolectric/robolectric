package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.P;
import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.RuntimeEnvironment.application;

import android.telephony.euicc.EuiccManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;

/** Junit test for {@link ShadowEuiccManager}. */
@RunWith(RobolectricTestRunner.class)
@Config(minSdk = P)
public class ShadowEuiccManagerTest {
  private EuiccManager euiccManager;
  private ShadowEuiccManager shadowEuiccManager;

  @Before
  public void setUp() {
    euiccManager = application.getSystemService(EuiccManager.class);
    shadowEuiccManager = Shadows.shadowOf(euiccManager);
  }

  @Test
  public void isEnabled() {
    shadowEuiccManager.setIsEnabled(true);

    assertThat(euiccManager.isEnabled()).isTrue();
  }

  @Test
  public void isEnabled_whenSetToFalse() {
    shadowEuiccManager.setIsEnabled(false);

    assertThat(euiccManager.isEnabled()).isFalse();
  }
}
