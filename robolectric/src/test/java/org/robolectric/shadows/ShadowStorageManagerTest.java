package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.N;
import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.RuntimeEnvironment.getApplication;
import static org.robolectric.Shadows.shadowOf;

import android.content.Context;
import android.os.Parcel;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.io.File;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

/** Unit tests for {@link ShadowStorageManager}. */
@RunWith(AndroidJUnit4.class)
public class ShadowStorageManagerTest {

  private final String internalStorage = "/storage/internal";
  private final String sdcardStorage = "/storage/sdcard";

  private StorageManager storageManager;

  @Before
  public void setUp() {
    storageManager = (StorageManager) getApplication().getSystemService(Context.STORAGE_SERVICE);
  }

  @Test
  public void getVolumeList() {
    assertThat(shadowOf(storageManager).getVolumeList()).isNotNull();
  }

  @Test
  @Config(minSdk = N)
  public void getStorageVolumes() {
    File file1 = new File(sdcardStorage);
    shadowOf(storageManager).addStorageVolume(buildAndGetStorageVolume(file1, "sd card"));
    assertThat(shadowOf(storageManager).getStorageVolumes()).isNotNull();
  }

  @Test
  @Config(minSdk = N)
  public void getStorageVolumesHaveDifferentUUID() {
    File file1 = new File(sdcardStorage);
    File file2 = new File(internalStorage);

    shadowOf(storageManager).addStorageVolume(buildAndGetStorageVolume(file1, "sd card"));
    shadowOf(storageManager).addStorageVolume(buildAndGetStorageVolume(file2, "internal"));

    List<StorageVolume> volumeList = shadowOf(storageManager).getStorageVolumes();
    assertThat(volumeList).hasSize(2);
    StorageVolume storage1 = volumeList.get(0);
    StorageVolume storage2 = volumeList.get(1);
    assertThat(storage1.getUuid()).isNotEqualTo(storage2.getUuid());
  }

  @Test
  @Config(minSdk = N)
  public void getStorageVolume() {
    File file1 = new File(internalStorage);
    File file2 = new File(sdcardStorage);
    File file3 = new File(internalStorage, "test_folder");
    File file4 = new File(sdcardStorage, "test_folder");
    shadowOf(storageManager).addStorageVolume(buildAndGetStorageVolume(file1, "internal"));
    assertThat(shadowOf(storageManager).getStorageVolume(file1)).isNotNull();
    assertThat(shadowOf(storageManager).getStorageVolume(file2)).isNull();
    assertThat(shadowOf(storageManager).getStorageVolume(file3)).isNotNull();
    assertThat(shadowOf(storageManager).getStorageVolume(file4)).isNull();
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
    shadowOf(getApplication().getSystemService(UserManager.class)).setUserUnlocked(true);
    assertThat(StorageManager.isUserKeyUnlocked(0)).isTrue();
  }

  private StorageVolume buildAndGetStorageVolume(File file, String description) {
    Parcel parcel = Parcel.obtain();
    parcel.writeInt(0);
    parcel.setDataPosition(0);
    UserHandle userHandle = new UserHandle(parcel);
    StorageVolumeBuilder storageVolumeBuilder =
        new StorageVolumeBuilder(
            "volume" + " " + description, file, description, userHandle, "mounted");
    return storageVolumeBuilder.build();
  }
}
