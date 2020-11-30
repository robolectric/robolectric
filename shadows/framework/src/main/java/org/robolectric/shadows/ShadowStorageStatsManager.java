package org.robolectric.shadows;

import android.app.usage.StorageStats;
import android.app.usage.StorageStatsManager;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.UserHandle;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/**
 * Fake implementation of {@link android.app.usage.StorageStatsManager} that provides a fake
 * implementation of query for {@link StorageStats} of a package.
 */
@Implements(value = StorageStatsManager.class, minSdk = Build.VERSION_CODES.O)
public class ShadowStorageStatsManager {

  private final Map<StorageStatsKey, StorageStats> storageStatsMap = new ConcurrentHashMap<>();

  /**
   * Sets the {@link StorageStats} to return when queried with matching {@code storageUUID}, {@code
   * packageName} and {@code userHandle}.
   */
  public void setStorageStatsToReturn(
      UUID storageUUID,
      String packageName,
      UserHandle userHandle,
      StorageStats storageStatsToReturn) {
    storageStatsMap.put(
        new StorageStatsKey(storageUUID, packageName, userHandle), storageStatsToReturn);
  }

  /**
   * Clears all {@link StorageStats} set in {@link
   * ShadowStorageStatsManager#setStorageStatsToReturn}.
   */
  public void clearStorageStatsToReturn() {
    storageStatsMap.clear();
  }

  /**
   * Fake implementation of {@link StorageStatsManager#queryStatsForPackage} that returns test setup
   * values. This fake implementation does not check for access permission. It only checks for
   * arguments matching those set in {@link ShadowStorageStatsManager#setStorageStatsToReturn}.
   */
  @Implementation
  public StorageStats queryStatsForPackage(UUID storageUuid, String packageName, UserHandle user)
      throws PackageManager.NameNotFoundException, IOException {
    StorageStats storageStat =
        storageStatsMap.get(new StorageStatsKey(storageUuid, packageName, user));
    if (storageStat == null) {
      throw new PackageManager.NameNotFoundException(
          "queryStatsForPackage with non matching arguments. Did you forget to call"
              + " setStorageStatsToReturn?");
    }
    return storageStat;
  }

  /** Simple wrapper for parameters of {@link StorageStatsManager#queryStatsForPackage} method. */
  private static final class StorageStatsKey {
    private final UUID storageUuid;
    private final String packageName;
    private final UserHandle userHandle;

    private StorageStatsKey(UUID storageUuid, String packageName, UserHandle userHandle) {
      this.storageUuid = storageUuid;
      this.packageName = packageName;
      this.userHandle = userHandle;
    }

    /** Checks for equality by comparing underlying objects. */
    @Override
    public boolean equals(Object obj) {
      if (!(obj instanceof StorageStatsKey)) {
        return false;
      }
      StorageStatsKey other = (StorageStatsKey) obj;
      return Objects.equals(storageUuid, other.storageUuid)
          && Objects.equals(packageName, other.packageName)
          && Objects.equals(userHandle, other.userHandle);
    }

    /**
     * Computes a simple hash code using the hash codes of the underlying objects
     *
     * @return a hashcode of the key
     */
    @Override
    public int hashCode() {
      return (storageUuid == null ? 0 : storageUuid.hashCode())
          ^ (packageName == null ? 0 : packageName.hashCode())
          ^ (userHandle == null ? 0 : userHandle.hashCode());
    }
  }
}
