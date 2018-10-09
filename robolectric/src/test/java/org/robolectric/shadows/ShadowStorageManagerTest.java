package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.N;
import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.RuntimeEnvironment.application;
import static org.robolectric.Shadows.shadowOf;

import android.content.Context;
import android.os.UserManager;
import android.os.storage.StorageManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

/** Unit tests for {@link ShadowStorageManager}. */
@RunWith(RobolectricTestRunner.class)
public class ShadowStorageManagerTest {

  private StorageManager storageManager;

  @Before
  public void setUp() {
    storageManager = (StorageManager) application.getSystemService(Context.STORAGE_SERVICE);
  }

  @Test
  public void getVolumeList() {
    assertThat(shadowOf(storageManager).getVolumeList()).isNotNull();
  }

  @Test
  @Config(minSdk = N)
  public void isFileEncryptedNativeOrEmulated() {
    shadowOf(storageManager).setFileEncryptedNativeOrEmulated(true);
    assertThat(StorageManager.isFileEncryptedNativeOrEmulated()).isTrue();
  }

  @Test
  @Config(minSdk = N)
  public void isUserKeyUnlocked() {
    shadowOf(application.getSystemService(UserManager.class)).setUserUnlocked(true);
    assertThat(StorageManager.isUserKeyUnlocked(0)).isTrue();
  }
}
