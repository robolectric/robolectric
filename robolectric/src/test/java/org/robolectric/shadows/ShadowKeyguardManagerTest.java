package org.robolectric.shadows;

import static android.content.Context.KEYGUARD_SERVICE;
import static android.os.Build.VERSION_CODES.M;
import static org.assertj.core.api.Assertions.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.app.KeyguardManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
public class ShadowKeyguardManagerTest {

  private KeyguardManager manager;

  @Before
  public void setUp() {
    manager = (KeyguardManager) RuntimeEnvironment.application.getSystemService(KEYGUARD_SERVICE);
  }

  @Test
  public void testIsInRestrictedInputMode() {
    assertThat(manager.inKeyguardRestrictedInputMode()).isFalse();
    ShadowKeyguardManager shadowMgr = shadowOf(manager);
    shadowMgr.setinRestrictedInputMode(true);
    assertThat(manager.inKeyguardRestrictedInputMode()).isTrue();
  }

  @Test
  public void testIsKeyguardLocked() {
    assertThat(manager.isKeyguardLocked()).isFalse();
    ShadowKeyguardManager shadowMgr = shadowOf(manager);
    shadowMgr.setKeyguardLocked(true);
    assertThat(manager.isKeyguardLocked()).isTrue();
  }

  @Test
  public void testShouldBeAbleToDisableTheKeyguardLock() throws Exception {
    KeyguardManager.KeyguardLock lock = manager.newKeyguardLock(KEYGUARD_SERVICE);
    assertThat(shadowOf(lock).isEnabled()).isTrue();

    lock.disableKeyguard();
    assertThat(shadowOf(lock).isEnabled()).isFalse();

    lock.reenableKeyguard();
    assertThat(shadowOf(lock).isEnabled()).isTrue();
  }

  @Test
  @Config(minSdk = M)
  public void isKeyguardSecure() {
    assertThat(manager.isKeyguardSecure()).isFalse();

    ShadowKeyguardManager shadowMgr = shadowOf(manager);
    shadowMgr.setIsKeyguardSecure(true);

    assertThat(manager.isKeyguardSecure()).isTrue();
  }

  @Test
  @Config(minSdk = M)
  public void isDeviceSecure() {
    assertThat(manager.isDeviceSecure()).isFalse();

    ShadowKeyguardManager shadowMgr = shadowOf(manager);
    shadowMgr.setIsDeviceSecure(true);

    assertThat(manager.isDeviceSecure()).isTrue();
  }
}
