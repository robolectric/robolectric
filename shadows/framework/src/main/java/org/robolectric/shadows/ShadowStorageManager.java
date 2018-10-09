package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.M;
import static android.os.Build.VERSION_CODES.N;
import static org.robolectric.RuntimeEnvironment.application;

import android.os.UserManager;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import org.robolectric.annotation.HiddenApi;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.shadow.api.Shadow;

/**
 * Fake implementation of {@link android.os.storage.StorageManager}
 */
@Implements(StorageManager.class)
public class ShadowStorageManager {

  private static boolean isFileEncryptionSupported = true;

  @Implementation(minSdk = M)
  protected static StorageVolume[] getVolumeList(int userId, int flags) {
    return new StorageVolume[0];
  }

  /**
   * Gets the volume list from {@link #getVolumeList(int, int)}
   *
   * @return volume list
   */
  public StorageVolume[] getVolumeList() {
    return getVolumeList(0, 0);
  }

  @HiddenApi
  @Implementation(minSdk = N)
  protected static boolean isFileEncryptedNativeOrEmulated() {
    return isFileEncryptionSupported;
  }

  /**
   * Setter for {@link #isFileEncryptedNativeOrEmulated()}
   *
   * @param isSupported a boolean value to set file encrypted native or not
   */
  public void setFileEncryptedNativeOrEmulated(boolean isSupported) {
    isFileEncryptionSupported = isSupported;
  }

  @HiddenApi
  @Implementation(minSdk = N)
  protected static boolean isUserKeyUnlocked(int userId) {
    ShadowUserManager extract = Shadow.extract(application.getSystemService(UserManager.class));
    return extract.isUserUnlocked();
  }
}
