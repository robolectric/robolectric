package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.P;
import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.telephony.euicc.EuiccManager;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

/** Junit test for {@link ShadowEuiccManager}. */
@RunWith(AndroidJUnit4.class)
@Config(minSdk = P)
public class ShadowEuiccManagerTest {
  private EuiccManager euiccManager;

  @Before
  public void setUp() {
    euiccManager = ApplicationProvider.getApplicationContext().getSystemService(EuiccManager.class);
  }

  @Test
  public void isEnabled() {
    shadowOf(euiccManager).setIsEnabled(true);

    assertThat(euiccManager.isEnabled()).isTrue();
  }

  @Test
  public void isEnabled_whenSetToFalse() {
    shadowOf(euiccManager).setIsEnabled(false);

    assertThat(euiccManager.isEnabled()).isFalse();
  }

  @Test
  public void getEid() {
    String eid = "testEid";
    shadowOf(euiccManager).setEid(eid);

    assertThat(euiccManager.getEid()).isEqualTo(eid);
  }
}
