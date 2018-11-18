package org.robolectric.shadows;

import static android.content.Context.KEYGUARD_SERVICE;
import static android.os.Build.VERSION_CODES.LOLLIPOP_MR1;
import static android.os.Build.VERSION_CODES.M;
import static android.os.Build.VERSION_CODES.O;
import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.robolectric.Shadows.shadowOf;

import android.app.Activity;
import android.app.KeyguardManager;
import android.app.KeyguardManager.KeyguardDismissCallback;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Config;

@RunWith(AndroidJUnit4.class)
public class ShadowKeyguardManagerTest {
  private static final int USER_ID = 1001;

  private KeyguardManager manager;

  @Before
  public void setUp() {
    manager =
        (KeyguardManager)
            ApplicationProvider.getApplicationContext().getSystemService(KEYGUARD_SERVICE);
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

  @Test
  @Config(minSdk = M)
  public void isDeviceSecureByUserId() {
    assertThat(manager.isDeviceSecure(USER_ID)).isFalse();

    ShadowKeyguardManager shadowMgr = shadowOf(manager);
    shadowMgr.setIsDeviceSecure(USER_ID, true);

    assertThat(manager.isDeviceSecure(USER_ID)).isTrue();
    assertThat(manager.isDeviceSecure(USER_ID + 1)).isFalse();

    shadowMgr.setIsDeviceSecure(USER_ID, false);
    assertThat(manager.isDeviceSecure(USER_ID)).isFalse();
  }

  @Test
  @Config(minSdk = LOLLIPOP_MR1)
  public void isDeviceLocked() {
    assertThat(manager.isDeviceLocked()).isFalse();

    ShadowKeyguardManager shadowMgr = shadowOf(manager);
    shadowMgr.setIsDeviceLocked(true);

    assertThat(manager.isDeviceLocked()).isTrue();
  }

  @Test
  @Config(minSdk = LOLLIPOP_MR1)
  public void isDeviceLockedByUserId() {
    assertThat(manager.isDeviceLocked(USER_ID)).isFalse();

    ShadowKeyguardManager shadowMgr = shadowOf(manager);
    shadowMgr.setIsDeviceLocked(USER_ID, true);
    assertThat(manager.isDeviceLocked(USER_ID)).isTrue();
    assertThat(manager.isDeviceLocked(USER_ID + 1)).isFalse();

    shadowMgr.setIsDeviceLocked(USER_ID, false);
    assertThat(manager.isDeviceLocked(USER_ID)).isFalse();
  }


  @Test
  @Config(minSdk = O)
  public void requestDismissKeyguard_dismissCancelled() {
    Activity activity = Robolectric.setupActivity(Activity.class);

    KeyguardDismissCallback mockCallback = mock(KeyguardDismissCallback.class);

    ShadowKeyguardManager shadowMgr = shadowOf(manager);
    shadowMgr.setKeyguardLocked(true);

    manager.requestDismissKeyguard(activity, mockCallback);

    // Keep the keyguard locked
    shadowMgr.setKeyguardLocked(true);

    verify(mockCallback).onDismissCancelled();
  }

  @Test
  @Config(minSdk = O)
  public void requestDismissKeyguard_dismissSucceeded() {
    Activity activity = Robolectric.setupActivity(Activity.class);

    KeyguardDismissCallback mockCallback = mock(KeyguardDismissCallback.class);

    ShadowKeyguardManager shadowMgr = shadowOf(manager);
    shadowMgr.setKeyguardLocked(true);

    manager.requestDismissKeyguard(activity, mockCallback);

    // Unlock the keyguard
    shadowMgr.setKeyguardLocked(false);

    verify(mockCallback).onDismissSucceeded();
  }
}
