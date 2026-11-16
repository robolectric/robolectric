package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.N;
import static android.os.Build.VERSION_CODES.O;
import static android.os.Build.VERSION_CODES.S;
import static android.os.Build.VERSION_CODES.TIRAMISU;
import static android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE;
import static com.google.common.base.Preconditions.checkArgument;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.os.StatFs;
import android.os.UserManager;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.HiddenApi;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.annotation.Resetter;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.ForType;

/** Fake implementation of {@link android.os.storage.StorageManager} */
@Implements(StorageManager.class)
public class ShadowStorageManager {

  // Matches com.android.server.usage.StorageStatsService.DEFAULT_QUOTA (64 MiB).
  private static final long DEFAULT_CACHE_QUOTA_BYTES = 64L * 1024 * 1024;
  private static final long DEFAULT_ALLOCATABLE_BYTES = 10L * 1024 * 1024 * 1024;

  private static boolean isFileEncryptionSupported = true;
  private static final List<StorageVolume> storageVolumeList = new CopyOnWriteArrayList<>();
  private static final Map<UUID, Long> cacheQuotaBytesByUuid = new ConcurrentHashMap<>();
  private static final Map<UUID, Long> allocatableBytesByUuid = new ConcurrentHashMap<>();

  @RealObject private StorageManager realStorageManager;

  @Implementation
  protected static StorageVolume[] getVolumeList(int userId, int flags) {
    return storageVolumeList.toArray(new StorageVolume[0]);
  }

  /**
   * Gets the volume list from {@link #getVolumeList(int, int)}
   *
   * @return volume list
   */
  @Implementation
  public StorageVolume[] getVolumeList() {
    return getVolumeList(0, 0);
  }

  /**
   * Adds a {@link StorageVolume} to the list returned by {@link #getStorageVolumes()}.
   *
   * @param storageVolume to add to list
   */
  public void addStorageVolume(StorageVolume storageVolume) {
    Objects.requireNonNull(storageVolume);
    storageVolumeList.add(storageVolume);
  }

  /**
   * Returns the storage volumes configured via {@link #addStorageVolume(StorageVolume)}.
   *
   * @return StorageVolume list
   */
  @Implementation(minSdk = N)
  protected List<StorageVolume> getStorageVolumes() {
    return storageVolumeList;
  }

  /** Clears the storageVolumeList. */
  public void resetStorageVolumeList() {
    storageVolumeList.clear();
  }

  /**
   * Checks whether File belongs to any {@link StorageVolume} in the list returned by {@link
   * #getStorageVolumes()}.
   *
   * @param file to check
   * @return StorageVolume for the file
   */
  @Implementation(minSdk = N)
  public StorageVolume getStorageVolume(File file) {
    for (StorageVolume volume : storageVolumeList) {
      File volumeFile = volume.getPathFile();
      if (file.getAbsolutePath().startsWith(volumeFile.getAbsolutePath())) {
        return volume;
      }
    }
    return null;
  }

  // Use maxSdk=T for this method, since starting in U, this method in StorageManager is deprecated
  // and is no longer called by the Android framework. It's planned to be removed entirely in V.
  @HiddenApi
  @Implementation(minSdk = N, maxSdk = TIRAMISU)
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

  // Use maxSdk=U, as this method is planned to be removed from StorageManager in V.
  @HiddenApi
  @Implementation(minSdk = N, maxSdk = UPSIDE_DOWN_CAKE)
  protected static boolean isUserKeyUnlocked(int userId) {
    ShadowUserManager extract =
        Shadow.extract(RuntimeEnvironment.getApplication().getSystemService(UserManager.class));
    return extract.isUserUnlocked();
  }

  /**
   * Returns the {@link UUID} for the storage volume hosting {@code path}.
   *
   * <p>Paths hosted under any {@link StorageVolume} added via {@link
   * #addStorageVolume(StorageVolume)} return the volume's UUID (selecting the longest matching
   * volume path). Paths hosted under {@link Environment#getDataDirectory()}, an application's
   * internal data directories, or emulated external storage return {@link
   * StorageManager#UUID_DEFAULT}. Otherwise throws {@link FileNotFoundException}.
   */
  @Implementation(minSdk = O)
  protected UUID getUuidForPath(File path) throws IOException {
    Objects.requireNonNull(path);
    Path targetPath = path.getCanonicalFile().toPath();

    UUID matchedVolumeUuid = null;
    int matchedVolumeNameCount = -1;
    for (StorageVolume volume : storageVolumeList) {
      File volumeFile = volume.getPathFile();
      if (volumeFile == null) {
        continue;
      }
      Path volumePath = volumeFile.getCanonicalFile().toPath();
      if (targetPath.startsWith(volumePath) && volumePath.getNameCount() > matchedVolumeNameCount) {
        UUID volumeUuid = tryGetVolumeUuid(volume);
        if (volumeUuid != null) {
          matchedVolumeUuid = volumeUuid;
          matchedVolumeNameCount = volumePath.getNameCount();
        }
      }
    }
    if (matchedVolumeUuid != null) {
      return matchedVolumeUuid;
    }

    if (isDefaultStoragePath(targetPath)) {
      return StorageManager.UUID_DEFAULT;
    }

    throw new FileNotFoundException("Failed to find a storage device for " + path);
  }

  /**
   * Sets the non-negative cache quota in bytes returned by {@link
   * StorageManager#getCacheQuotaBytes(UUID)} for {@code storageUuid}.
   */
  public void setCacheQuotaBytes(UUID storageUuid, long bytes) {
    Objects.requireNonNull(storageUuid);
    checkArgument(bytes >= 0, "bytes must be non-negative: %s", bytes);
    cacheQuotaBytesByUuid.put(storageUuid, bytes);
  }

  @Implementation(minSdk = O)
  protected long getCacheQuotaBytes(UUID storageUuid) throws IOException {
    Objects.requireNonNull(storageUuid);
    Long quotaBytes = cacheQuotaBytesByUuid.get(storageUuid);
    if (quotaBytes != null) {
      return quotaBytes;
    }
    StorageVolume volume = findRegisteredVolume(storageUuid);
    if (volume != null) {
      Long volumeQuotaBytes = getConfiguredBytesForVolume(cacheQuotaBytesByUuid, volume);
      return volumeQuotaBytes != null ? volumeQuotaBytes : DEFAULT_CACHE_QUOTA_BYTES;
    }
    if (StorageManager.UUID_DEFAULT.equals(storageUuid)) {
      return DEFAULT_CACHE_QUOTA_BYTES;
    }
    throw new IOException("Failed to find storage device for UUID " + storageUuid);
  }

  /**
   * Sets the non-negative allocatable space in bytes returned by {@link
   * StorageManager#getAllocatableBytes(UUID)} for {@code storageUuid}.
   */
  public void setAllocatableBytes(UUID storageUuid, long bytes) {
    Objects.requireNonNull(storageUuid);
    checkArgument(bytes >= 0, "bytes must be non-negative: %s", bytes);
    allocatableBytesByUuid.put(storageUuid, bytes);
  }

  @HiddenApi
  @Implementation(minSdk = O)
  protected long getAllocatableBytes(UUID storageUuid, int flags) throws IOException {
    Objects.requireNonNull(storageUuid);
    Long configuredBytes = allocatableBytesByUuid.get(storageUuid);
    if (configuredBytes != null) {
      return configuredBytes;
    }
    StorageVolume volume = findRegisteredVolume(storageUuid);
    if (volume != null) {
      Long volumeBytes = getConfiguredBytesForVolume(allocatableBytesByUuid, volume);
      if (volumeBytes != null) {
        return volumeBytes;
      }
      return resolveDefaultAllocatableBytes(volume.getPathFile());
    }
    if (StorageManager.UUID_DEFAULT.equals(storageUuid)) {
      return resolveDefaultAllocatableBytesForDefaultUuid();
    }
    throw new IOException("Failed to find storage device for UUID " + storageUuid);
  }

  private boolean isDefaultStoragePath(Path targetPath) throws IOException {
    if (containsCanonicalPath(Environment.getDataDirectory(), targetPath)) {
      return true;
    }
    if (Environment.isExternalStorageEmulated()
        && (containsCanonicalPath(ShadowEnvironment.EXTERNAL_CACHE_DIR, targetPath)
            || containsCanonicalPath(ShadowEnvironment.EXTERNAL_FILES_DIR, targetPath))) {
      return true;
    }
    Context context = reflector(StorageManagerReflector.class, realStorageManager).getContext();
    if (context != null) {
      if (isApplicationStoragePath(context.getApplicationInfo(), targetPath)) {
        return true;
      }
      PackageManager packageManager = context.getPackageManager();
      if (packageManager != null) {
        for (ApplicationInfo installedApp : packageManager.getInstalledApplications(0)) {
          if (isApplicationStoragePath(installedApp, targetPath)) {
            return true;
          }
        }
      }
    }
    @SuppressWarnings("deprecation")
    Context application = RuntimeEnvironment.application;
    return application != null
        && isApplicationStoragePath(application.getApplicationInfo(), targetPath);
  }

  private static boolean isApplicationStoragePath(ApplicationInfo applicationInfo, Path targetPath)
      throws IOException {
    return applicationInfo != null
        && (containsCanonicalPath(applicationInfo.dataDir, targetPath)
            || containsCanonicalPath(applicationInfo.credentialProtectedDataDir, targetPath)
            || containsCanonicalPath(applicationInfo.deviceProtectedDataDir, targetPath));
  }

  private static boolean containsCanonicalPath(String directoryPath, Path targetPath)
      throws IOException {
    return directoryPath != null && containsCanonicalPath(new File(directoryPath), targetPath);
  }

  private static boolean containsCanonicalPath(Path directory, Path targetPath) throws IOException {
    return directory != null && containsCanonicalPath(directory.toFile(), targetPath);
  }

  private static boolean containsCanonicalPath(File directory, Path targetPath) throws IOException {
    return directory != null && targetPath.startsWith(directory.getCanonicalFile().toPath());
  }

  private static UUID tryGetVolumeUuid(StorageVolume volume) {
    String fsUuid = volume.getUuid();
    if (fsUuid == null && RuntimeEnvironment.getApiLevel() >= S) {
      UUID storageUuid = volume.getStorageUuid();
      if (storageUuid != null) {
        return storageUuid;
      }
    }
    try {
      return StorageManager.convert(fsUuid);
    } catch (IllegalArgumentException ignored) {
      // Matches Android's StorageManager.getUuidForPath, which skips volumes whose fsUuid
      // cannot be converted by StorageManager.convert.
      return null;
    }
  }

  private static StorageVolume findRegisteredVolume(UUID storageUuid) {
    for (StorageVolume volume : storageVolumeList) {
      UUID convertedUuid = tryGetVolumeUuid(volume);
      if (convertedUuid == null) {
        continue;
      }
      UUID storageVolumeUuid =
          RuntimeEnvironment.getApiLevel() >= S ? volume.getStorageUuid() : null;
      if (storageUuid.equals(convertedUuid) || storageUuid.equals(storageVolumeUuid)) {
        return volume;
      }
    }
    return null;
  }

  private static Long getConfiguredBytesForVolume(
      Map<UUID, Long> configuredBytesByUuid, StorageVolume volume) {
    UUID convertedUuid = tryGetVolumeUuid(volume);
    if (convertedUuid != null) {
      Long configuredConvertedBytes = configuredBytesByUuid.get(convertedUuid);
      if (configuredConvertedBytes != null) {
        return configuredConvertedBytes;
      }
    }
    if (RuntimeEnvironment.getApiLevel() >= S) {
      UUID storageVolumeUuid = volume.getStorageUuid();
      if (storageVolumeUuid != null) {
        Long configuredVolumeBytes = configuredBytesByUuid.get(storageVolumeUuid);
        if (configuredVolumeBytes != null) {
          return configuredVolumeBytes;
        }
      }
    }
    return null;
  }

  private long resolveDefaultAllocatableBytesForDefaultUuid() {
    Long dataDirStatFsBytes = findRegisteredStatFsAvailableBytes(Environment.getDataDirectory());
    if (dataDirStatFsBytes != null) {
      return dataDirStatFsBytes;
    }
    File applicationDataDir = getApplicationDataDir();
    if (applicationDataDir != null) {
      Long appDataStatFsBytes = findRegisteredStatFsAvailableBytes(applicationDataDir);
      if (appDataStatFsBytes != null) {
        return appDataStatFsBytes;
      }
      Long appFilesStatFsBytes =
          findRegisteredStatFsAvailableBytes(new File(applicationDataDir, "files"));
      if (appFilesStatFsBytes != null) {
        return appFilesStatFsBytes;
      }
      Long appCacheStatFsBytes =
          findRegisteredStatFsAvailableBytes(new File(applicationDataDir, "cache"));
      if (appCacheStatFsBytes != null) {
        return appCacheStatFsBytes;
      }
      if (applicationDataDir.exists()) {
        long usableSpace = applicationDataDir.getUsableSpace();
        if (usableSpace > 0) {
          return usableSpace;
        }
      }
    }
    return getDefaultTempDirectoryUsableSpace();
  }

  private static long resolveDefaultAllocatableBytes(File directory) {
    Long statFsBytes = findRegisteredStatFsAvailableBytes(directory);
    if (statFsBytes != null) {
      return statFsBytes;
    }
    if (directory != null && directory.exists()) {
      long usableSpace = directory.getUsableSpace();
      if (usableSpace > 0) {
        return usableSpace;
      }
    }
    return getDefaultTempDirectoryUsableSpace();
  }

  private static Long findRegisteredStatFsAvailableBytes(File directory) {
    if (directory == null) {
      return null;
    }
    StatFs statFs = new StatFs(directory.getAbsolutePath());
    if (statFs.getTotalBytes() > 0) {
      return statFs.getAvailableBytes();
    }
    return null;
  }

  private static long getDefaultTempDirectoryUsableSpace() {
    File tempDirectory = RuntimeEnvironment.getTempDirectory().getBasePath().toFile();
    long usableSpace = tempDirectory.getUsableSpace();
    return usableSpace > 0 ? usableSpace : DEFAULT_ALLOCATABLE_BYTES;
  }

  private File getApplicationDataDir() {
    Context context = reflector(StorageManagerReflector.class, realStorageManager).getContext();
    if (context != null
        && context.getApplicationInfo() != null
        && context.getApplicationInfo().dataDir != null) {
      return new File(context.getApplicationInfo().dataDir);
    }
    @SuppressWarnings("deprecation")
    Context application = RuntimeEnvironment.application;
    if (application != null
        && application.getApplicationInfo() != null
        && application.getApplicationInfo().dataDir != null) {
      return new File(application.getApplicationInfo().dataDir);
    }
    return null;
  }

  @Resetter
  public static void reset() {
    storageVolumeList.clear();
    cacheQuotaBytesByUuid.clear();
    allocatableBytesByUuid.clear();
    isFileEncryptionSupported = true;
  }

  @ForType(StorageManager.class)
  interface StorageManagerReflector {
    @Accessor("mContext")
    Context getContext();

    long getAllocatableBytes(UUID storageUuid, int flags) throws IOException;
  }
}
