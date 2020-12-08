package org.robolectric.shadows;

import android.app.usage.StorageStats;
import android.app.usage.StorageStatsManager;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.UserHandle;
import com.google.auto.value.AutoValue;
import java.io.IOException;
import java.util.Map;
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
  public void addStorageStats(
      UUID storageUUID,
      String packageName,
      UserHandle userHandle,
      StorageStats storageStatsToReturn) {
    storageStatsMap.put(
        StorageStatsKey.create(storageUUID, packageName, userHandle), storageStatsToReturn);
  }

  /**
   * Clears all {@link StorageStats} set in {@link
   * ShadowStorageStatsManager#setStorageStatsToReturn}.
   */
  public void clearStorageStats() {
    storageStatsMap.clear();
  }

  /**
   * Fake implementation of {@link StorageStatsManager#queryStatsForPackage} that returns test setup
   * values. This fake implementation does not check for access permission. It only checks for
   * arguments matching those set in {@link ShadowStorageStatsManager#setStorageStatsToReturn}.
   */
  @Implementation
  protected StorageStats queryStatsForPackage(UUID storageUuid, String packageName, UserHandle user)
      throws PackageManager.NameNotFoundException, IOException {
    StorageStats storageStat =
        storageStatsMap.get(StorageStatsKey.create(storageUuid, packageName, user));
    if (storageStat == null) {
      throw new PackageManager.NameNotFoundException(
          "queryStatsForPackage with non matching arguments. Did you forget to call"
              + " setStorageStats?");
    }
    return storageStat;
  }

  /** Simple wrapper for parameters of {@link StorageStatsManager#queryStatsForPackage} method. */
  @AutoValue
  abstract static class StorageStatsKey {

    StorageStatsKey() {}

    /** Returns the storage UUID part of this key. */
    abstract UUID storageUuid();

    /** Returns the package name part of this key. */
    abstract String packageName();

    /** Returns the user handle part of this key. */
    abstract UserHandle userHandle();

    /** Creates StorageStatsKey. */
    static StorageStatsKey create(UUID storageUuid, String packageName, UserHandle userHandle) {
      return new AutoValue_ShadowStorageStatsManager_StorageStatsKey(
          storageUuid, packageName, userHandle);
    }
  }
}
