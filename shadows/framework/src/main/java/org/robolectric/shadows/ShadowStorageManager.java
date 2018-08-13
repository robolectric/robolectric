package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.M;

import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/**
 * Fake implementation of {@link android.os.storage.StorageManager}
 */
@Implements(StorageManager.class)
public class ShadowStorageManager {

  @Implementation(minSdk = M)
  protected static StorageVolume[] getVolumeList(int userId, int flags) {
    return new StorageVolume[0];
  }
}
