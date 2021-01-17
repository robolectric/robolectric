package org.robolectric.shadows;

import static com.google.common.base.Preconditions.checkArgument;

import android.app.usage.StorageStats;
import android.app.usage.StorageStatsManager;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.UserHandle;
import android.os.storage.StorageManager;
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

  public static final long DEFAULT_STORAGE_FREE_BYTES = 4L * 1024L * 1024L * 1024L; // 4 GB
  public static final long DEFAULT_STORAGE_TOTAL_BYTES = 8L * 1024L * 1024L * 1024L; // 8 GB

  private final Map<UUID, FreeAndTotalBytesPair> freeAndTotalBytesMap =
      createFreeAndTotalBytesMapWithSingleEntry(
          StorageManager.UUID_DEFAULT, DEFAULT_STORAGE_FREE_BYTES, DEFAULT_STORAGE_TOTAL_BYTES);
  private final Map<StorageStatsKey, StorageStats> storageStatsMap = new ConcurrentHashMap<>();

  /**
   * Sets the {@code storageUuid} to return the specified {@code freeBytes} and {@code totalBytes}
   * when queried in {@link #getFreeBytes} and {@link #getTotalBytes} respectively.
   *
   * <p>Both {@code freeBytes} and {@code totalBytes} have to be non-negative, else this method will
   * throw {@link IllegalArgumentException}.
   */
  public void setStorageDeviceFreeAndTotalBytes(UUID storageUuid, long freeBytes, long totalBytes) {
    checkArgument(
        freeBytes >= 0 && totalBytes >= 0, "Both freeBytes and totalBytes must be non-negative!");
    freeAndTotalBytesMap.put(storageUuid, FreeAndTotalBytesPair.create(freeBytes, totalBytes));
  }

  /**
   * Removes a storage device identified by {@code storageUuid} if it's currently present.
   * Otherwise, this method will be a no-op.
   */
  public void removeStorageDevice(UUID storageUuid) {
    freeAndTotalBytesMap.remove(storageUuid);
  }

  /**
   * Sets the {@link StorageStats} to return when queried with matching {@code storageUuid}, {@code
   * packageName} and {@code userHandle}.
   */
  public void addStorageStats(
      UUID storageUuid,
      String packageName,
      UserHandle userHandle,
      StorageStats storageStatsToReturn) {
    storageStatsMap.put(
        StorageStatsKey.create(storageUuid, packageName, userHandle), storageStatsToReturn);
  }

  /** Clears all {@link StorageStats} set in {@link ShadowStorageStatsManager#addStorageStats}. */
  public void clearStorageStats() {
    storageStatsMap.clear();
  }

  /**
   * Fake implementation of {@link StorageStatsManager#getFreeBytes} that returns test setup values.
   * This fake implementation does not check for access permission. It only checks for arguments
   * matching those set in {@link ShadowStorageStatsManager#setStorageDeviceFreeAndTotalBytes}.
   */
  @Implementation
  protected long getFreeBytes(UUID storageUuid) throws IOException {
    FreeAndTotalBytesPair freeAndTotalBytesPair = freeAndTotalBytesMap.get(storageUuid);
    if (freeAndTotalBytesPair == null) {
      throw new IOException(
          "getFreeBytes with non-existent storageUuid! Did you forget to call"
              + " setStorageDeviceFreeAndTotalBytes?");
    }
    return freeAndTotalBytesPair.freeBytes();
  }

  /**
   * Fake implementation of {@link StorageStatsManager#getTotalBytes} that returns test setup
   * values. This fake implementation does not check for access permission. It only checks for
   * arguments matching those set in {@link
   * ShadowStorageStatsManager#setStorageDeviceFreeAndTotalBytes}.
   */
  @Implementation
  protected long getTotalBytes(UUID storageUuid) throws IOException {
    FreeAndTotalBytesPair freeAndTotalBytesPair = freeAndTotalBytesMap.get(storageUuid);
    if (freeAndTotalBytesPair == null) {
      throw new IOException(
          "getTotalBytes with non-existent storageUuid! Did you forget to call"
              + " setStorageDeviceFreeAndTotalBytes?");
    }
    return freeAndTotalBytesPair.totalBytes();
  }

  /**
   * Fake implementation of {@link StorageStatsManager#queryStatsForPackage} that returns test setup
   * values. This fake implementation does not check for access permission. It only checks for
   * arguments matching those set in {@link ShadowStorageStatsManager#addStorageStats}.
   */
  @Implementation
  protected StorageStats queryStatsForPackage(UUID storageUuid, String packageName, UserHandle user)
      throws PackageManager.NameNotFoundException, IOException {
    StorageStats storageStat =
        storageStatsMap.get(StorageStatsKey.create(storageUuid, packageName, user));
    if (storageStat == null) {
      throw new PackageManager.NameNotFoundException(
          "queryStatsForPackage with non matching arguments. Did you forget to call"
              + " addStorageStats?");
    }
    return storageStat;
  }

  private static Map<UUID, FreeAndTotalBytesPair> createFreeAndTotalBytesMapWithSingleEntry(
      UUID storageUuid, long freeBytes, long totalBytes) {
    Map<UUID, FreeAndTotalBytesPair> currMap = new ConcurrentHashMap<>();
    currMap.put(storageUuid, FreeAndTotalBytesPair.create(freeBytes, totalBytes));
    return currMap;
  }

  /** Simple wrapper to combine freeBytes and totalBytes in one object. */
  @AutoValue
  abstract static class FreeAndTotalBytesPair {

    FreeAndTotalBytesPair() {}

    /** Returns the freeBytes. */
    abstract long freeBytes();

    /** Returns the totalBytes. */
    abstract long totalBytes();

    /** Creates {@link FreeAndTotalBytesPair}. */
    static FreeAndTotalBytesPair create(long freeBytes, long totalBytes) {
      return new AutoValue_ShadowStorageStatsManager_FreeAndTotalBytesPair(freeBytes, totalBytes);
    }
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
