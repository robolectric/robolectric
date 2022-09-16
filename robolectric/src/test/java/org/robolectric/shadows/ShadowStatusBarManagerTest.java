package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.M;
import static android.os.Build.VERSION_CODES.Q;
import static android.os.Build.VERSION_CODES.S_V2;
import static android.os.Build.VERSION_CODES.TIRAMISU;
import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static com.google.common.truth.Truth.assertThat;

import android.app.StatusBarManager;
import android.content.Context;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.shadow.api.Shadow;

/** Unit tests for {@link ShadowStatusBarManager}. */
@RunWith(AndroidJUnit4.class)
public final class ShadowStatusBarManagerTest {

  private static final int TEST_NAV_BAR_MODE = 100;

  private final StatusBarManager statusBarManager =
      (StatusBarManager) getApplicationContext().getSystemService(Context.STATUS_BAR_SERVICE);

  private final ShadowStatusBarManager shadowStatusBarManager = Shadow.extract(statusBarManager);

  @Test
  public void getDisable() throws ClassNotFoundException {
    statusBarManager.disable(ShadowStatusBarManager.DEFAULT_DISABLE_MASK);
    assertThat(shadowStatusBarManager.getDisableFlags())
        .isEqualTo(ShadowStatusBarManager.DEFAULT_DISABLE_MASK);
  }

  @Test
  @Config(minSdk = M)
  public void getDisable2() throws ClassNotFoundException {
    statusBarManager.disable2(ShadowStatusBarManager.DEFAULT_DISABLE2_MASK);
    assertThat(shadowStatusBarManager.getDisable2Flags())
        .isEqualTo(ShadowStatusBarManager.DEFAULT_DISABLE2_MASK);
  }

  @Test
  @Config(minSdk = Q)
  public void setDisabledForSetup() {
    getApplicationContext().getSystemService(StatusBarManager.class).setDisabledForSetup(true);

    assertThat(shadowStatusBarManager.getDisableFlags())
        .isEqualTo(ShadowStatusBarManager.getDefaultSetupDisableFlags());

    int disable2Flags = shadowStatusBarManager.getDisable2Flags();
    assertThat(disable2Flags).isEqualTo(ShadowStatusBarManager.getDefaultSetupDisable2Flags());

    // The default disable2 flags changed in Android T.
    int expectedDisable2Flags =
        RuntimeEnvironment.getApiLevel() <= S_V2
            ? ShadowStatusBarManager.DISABLE2_ROTATE_SUGGESTIONS
            : ShadowStatusBarManager.DISABLE2_NONE;
    assertThat(disable2Flags).isEqualTo(expectedDisable2Flags);

    getApplicationContext().getSystemService(StatusBarManager.class).setDisabledForSetup(false);
    assertThat(shadowStatusBarManager.getDisableFlags()).isEqualTo(StatusBarManager.DISABLE_NONE);
    assertThat(shadowStatusBarManager.getDisable2Flags()).isEqualTo(StatusBarManager.DISABLE2_NONE);
  }

  @Config(minSdk = TIRAMISU)
  @Test
  public void getNavBarMode_returnsNavBarMode() throws Exception {
    statusBarManager.setNavBarMode(TEST_NAV_BAR_MODE);
    assertThat(shadowStatusBarManager.getNavBarMode()).isEqualTo(TEST_NAV_BAR_MODE);
  }

  @Config(minSdk = TIRAMISU)
  @Test
  public void setNavBarMode_storesNavBarMode() throws Exception {
    shadowStatusBarManager.setNavBarMode(TEST_NAV_BAR_MODE);
    assertThat(shadowStatusBarManager.getNavBarMode()).isEqualTo(TEST_NAV_BAR_MODE);
  }
}
