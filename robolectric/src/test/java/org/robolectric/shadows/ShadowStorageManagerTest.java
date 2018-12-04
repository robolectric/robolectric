package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.N;
import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.RuntimeEnvironment.application;
import static org.robolectric.Shadows.shadowOf;

import android.content.Context;
import android.os.Parcel;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.io.File;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

/** Unit tests for {@link ShadowStorageManager}. */
@RunWith(AndroidJUnit4.class)
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
  public void getStorageVolumes() {
    File file1 = new File("/storage/sdcard");
    shadowOf(storageManager).addStorageVolume(buildAndGetStorageVolume(file1, "sd card"));
    assertThat(shadowOf(storageManager).getStorageVolumes()).isNotNull();
  }

  @Test
  @Config(minSdk = N)
  public void getStorageVolume() {
    File file1 = new File("/storage/internal");
    File file2 = new File("/storage/sdcard");
    shadowOf(storageManager).addStorageVolume(buildAndGetStorageVolume(file1, "internal"));
    assertThat(shadowOf(storageManager).getStorageVolume(file1)).isNotNull();
    assertThat(shadowOf(storageManager).getStorageVolume(file2)).isNull();
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

  private StorageVolume buildAndGetStorageVolume(File file, String description) {
    Parcel parcel = Parcel.obtain();
    parcel.writeInt(0);
    UserHandle userHandle = new UserHandle(parcel);
    StorageVolumeBuilder storageVolumeBuilder =
        new StorageVolumeBuilder("volume", file, description, userHandle, "mounted");
    return storageVolumeBuilder.build();
  }
}
