package org.robolectric.shadows;

import static android.content.Context.KEYGUARD_SERVICE;
import static android.os.Build.VERSION_CODES.LOLLIPOP_MR1;
import static android.os.Build.VERSION_CODES.M;
import static android.os.Build.VERSION_CODES.O;
import static android.os.Build.VERSION_CODES.O_MR1;
import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.robolectric.Shadows.shadowOf;

import android.app.Activity;
import android.app.KeyguardManager;
import android.app.KeyguardManager.KeyguardDismissCallback;
import android.content.Intent;
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
    shadowOf(manager).setinRestrictedInputMode(true);
    assertThat(manager.inKeyguardRestrictedInputMode()).isTrue();
  }

  @Test
  public void testIsKeyguardLocked() {
    assertThat(manager.isKeyguardLocked()).isFalse();
    shadowOf(manager).setKeyguardLocked(true);
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

    shadowOf(manager).setIsKeyguardSecure(true);

    assertThat(manager.isKeyguardSecure()).isTrue();
  }

  @Test
  @Config(minSdk = M)
  public void isDeviceSecure() {
    assertThat(manager.isDeviceSecure()).isFalse();

    shadowOf(manager).setIsDeviceSecure(true);

    assertThat(manager.isDeviceSecure()).isTrue();
  }

  @Test
  @Config(minSdk = M)
  public void isDeviceSecureByUserId() {
    assertThat(manager.isDeviceSecure(USER_ID)).isFalse();

    shadowOf(manager).setIsDeviceSecure(USER_ID, true);

    assertThat(manager.isDeviceSecure(USER_ID)).isTrue();
    assertThat(manager.isDeviceSecure(USER_ID + 1)).isFalse();

    shadowOf(manager).setIsDeviceSecure(USER_ID, false);
    assertThat(manager.isDeviceSecure(USER_ID)).isFalse();
  }

  @Test
  @Config(minSdk = LOLLIPOP_MR1)
  public void isDeviceLocked() {
    assertThat(manager.isDeviceLocked()).isFalse();

    shadowOf(manager).setIsDeviceLocked(true);

    assertThat(manager.isDeviceLocked()).isTrue();
  }

  @Test
  @Config(minSdk = LOLLIPOP_MR1)
  public void isDeviceLockedByUserId() {
    assertThat(manager.isDeviceLocked(USER_ID)).isFalse();

    shadowOf(manager).setIsDeviceLocked(USER_ID, true);
    assertThat(manager.isDeviceLocked(USER_ID)).isTrue();
    assertThat(manager.isDeviceLocked(USER_ID + 1)).isFalse();

    shadowOf(manager).setIsDeviceLocked(USER_ID, false);
    assertThat(manager.isDeviceLocked(USER_ID)).isFalse();
  }


  @Test
  @Config(minSdk = O)
  public void requestDismissKeyguard_dismissCancelled() {
    Activity activity = Robolectric.setupActivity(Activity.class);

    KeyguardDismissCallback mockCallback = mock(KeyguardDismissCallback.class);

    shadowOf(manager).setKeyguardLocked(true);

    manager.requestDismissKeyguard(activity, mockCallback);

    // Keep the keyguard locked
    shadowOf(manager).setKeyguardLocked(true);

    verify(mockCallback).onDismissCancelled();
  }

  @Test
  @Config(minSdk = O)
  public void requestDismissKeyguard_dismissSucceeded() {
    Activity activity = Robolectric.setupActivity(Activity.class);

    KeyguardDismissCallback mockCallback = mock(KeyguardDismissCallback.class);

    shadowOf(manager).setKeyguardLocked(true);

    manager.requestDismissKeyguard(activity, mockCallback);

    // Unlock the keyguard
    shadowOf(manager).setKeyguardLocked(false);

    verify(mockCallback).onDismissSucceeded();
  }

  @Test
  @Config(minSdk = O_MR1)
  public void testCreateConfirmFactoryResetCredentialIntent_nullIntent() {
    assertThat(manager.isDeviceLocked()).isFalse();

    shadowOf(manager).setConfirmFactoryResetCredentialIntent(null);

    assertThat(manager.createConfirmFactoryResetCredentialIntent(null, null, null)).isNull();
  }

  @Test
  @Config(minSdk = O_MR1)
  public void testCreateConfirmFactoryResetCredentialIntent() {
    assertThat(manager.isDeviceLocked()).isFalse();

    Intent intent = new Intent();
    shadowOf(manager).setConfirmFactoryResetCredentialIntent(intent);

    assertThat(manager.createConfirmFactoryResetCredentialIntent(null, null, null))
        .isEqualTo(intent);
  }

  /**
   * On Android L and below, calling {@link android.content.Context#getSystemService(String)} for
   * {@link android.content.Context#KEYGUARD_SERVICE} will return a new instance each time.
   */
  @Test
  public void isKeyguardLocked_retainedAcrossMultipleInstances() {
    assertThat(manager.isKeyguardLocked()).isFalse();
    shadowOf(manager).setKeyguardLocked(true);
    KeyguardManager manager2 =
        (KeyguardManager)
            ApplicationProvider.getApplicationContext().getSystemService(KEYGUARD_SERVICE);
    assertThat(manager2.isKeyguardLocked()).isTrue();
    assertThat(shadowOf(manager.newKeyguardLock("tag")).isEnabled()).isTrue();
    KeyguardManager.KeyguardLock keyguardLock = manager2.newKeyguardLock("tag");
    keyguardLock.disableKeyguard();
    assertThat(shadowOf(manager.newKeyguardLock("tag")).isEnabled()).isFalse();
  }
}
