package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR1;
import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR2;
import static android.os.Build.VERSION_CODES.KITKAT;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.M;
import static android.os.Build.VERSION_CODES.Q;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.os.Environment;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.ForType;

@Implements(Environment.class)
@SuppressWarnings("NewApi")
public class ShadowEnvironment {
  private static String externalStorageState = Environment.MEDIA_REMOVED;
  private static final Map<File, Boolean> STORAGE_EMULATED = new HashMap<>();
  private static final Map<File, Boolean> STORAGE_REMOVABLE = new HashMap<>();
  private static boolean sIsExternalStorageEmulated;
  private static boolean isExternalStorageLegacy;
  private static Path tmpExternalFilesDirBase;
  private static final List<File> externalDirs = new ArrayList<>();
  private static Map<Path, String> storageState = new HashMap<>();

  static Path EXTERNAL_CACHE_DIR;
  static Path EXTERNAL_FILES_DIR;

  @Implementation
  protected static String getExternalStorageState() {
    return externalStorageState;
  }

  /**
   * Sets the return value of {@link #getExternalStorageState()}.
   *
   * @param externalStorageState Value to return from {@link #getExternalStorageState()}.
   */
  public static void setExternalStorageState(String externalStorageState) {
    ShadowEnvironment.externalStorageState = externalStorageState;
  }

  /**
   * Sets the return value of {@link #isExternalStorageEmulated()}.
   *
   * @param emulated Value to return from {@link #isExternalStorageEmulated()}.
   */
  public static void setIsExternalStorageEmulated(boolean emulated) {
    ShadowEnvironment.sIsExternalStorageEmulated = emulated;
  }

  /**
   * Sets the return value of {@link #isExternalStorageLegacy()} ()}.
   *
   * @param legacy Value to return from {@link #isExternalStorageLegacy()}.
   */
  public static void setIsExternalStorageLegacy(boolean legacy) {
    ShadowEnvironment.isExternalStorageLegacy = legacy;
  }

  /**
   * Sets the return value of {@link #getExternalStorageDirectory()}.  Note that
   * the default value provides a directory that is usable in the test environment.
   * If the test app uses this method to override that default directory, please
   * clean up any files written to that directory, as the Robolectric environment
   * will not purge that directory when the test ends.
   *
   * @param directory Path to return from {@link #getExternalStorageDirectory()}.
   */
  public static void setExternalStorageDirectory(Path directory) {
    EXTERNAL_CACHE_DIR = directory;
  }

  @Implementation
  protected static File getExternalStorageDirectory() {
    if (EXTERNAL_CACHE_DIR == null) {

      EXTERNAL_CACHE_DIR =
          RuntimeEnvironment.getTempDirectory().createIfNotExists("external-cache");
    }
    return EXTERNAL_CACHE_DIR.toFile();
  }

  @Implementation(minSdk = KITKAT)
  protected static File[] buildExternalStorageAppCacheDirs(String packageName) {
    Path externalStorageDirectoryPath = getExternalStorageDirectory().toPath();
    // Add cache directory in path.
    String cacheDirectory = packageName + "-cache";
    Path path = externalStorageDirectoryPath.resolve(cacheDirectory);
    try {
      Files.createDirectory(path);
    } catch (FileAlreadyExistsException e) {
      // That's ok
      return new File[] {path.toFile()};
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return new File[] {path.toFile()};
  }

  @Implementation(maxSdk = JELLY_BEAN_MR2)
  protected static File getExternalStorageAppCacheDirectory(String packageName) {
    return buildExternalStorageAppCacheDirs(packageName)[0];
  }

  @Implementation
  protected static File getExternalStoragePublicDirectory(String type) {
    if (externalStorageState.equals(Environment.MEDIA_UNKNOWN)) {
      return null;
    }
    if (EXTERNAL_FILES_DIR == null) {
      EXTERNAL_FILES_DIR =
          RuntimeEnvironment.getTempDirectory().createIfNotExists("external-files");
    }
    if (type == null) return EXTERNAL_FILES_DIR.toFile();
    Path path = EXTERNAL_FILES_DIR.resolve(type);
    try {
      Files.createDirectories(path);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return path.toFile();
  }

  @Resetter
  public static void reset() {

    EXTERNAL_CACHE_DIR = null;
    EXTERNAL_FILES_DIR = null;

    STORAGE_EMULATED.clear();
    STORAGE_REMOVABLE.clear();

    storageState = new HashMap<>();
    externalDirs.clear();
    externalStorageState = Environment.MEDIA_REMOVED;

    sIsExternalStorageEmulated = false;
    isExternalStorageLegacy = false;
  }

  @Implementation
  protected static boolean isExternalStorageRemovable() {
    final Boolean exists = STORAGE_REMOVABLE.get(getExternalStorageDirectory());
    return exists != null ? exists : false;
  }

  @Implementation(minSdk = KITKAT)
  protected static String getStorageState(File directory) {
    Path directoryPath = directory.toPath();
    for (Map.Entry<Path, String> entry : storageState.entrySet()) {
      if (directoryPath.startsWith(entry.getKey())) {
        return entry.getValue();
      }
    }
    return null;
  }

  @Implementation(minSdk = LOLLIPOP)
  protected static String getExternalStorageState(File directory) {
    Path directoryPath = directory.toPath();
    for (Map.Entry<Path, String> entry : storageState.entrySet()) {
      if (directoryPath.startsWith(entry.getKey())) {
        return entry.getValue();
      }
    }
    return null;
  }

  @Implementation(minSdk = LOLLIPOP)
  protected static boolean isExternalStorageRemovable(File path) {
    final Boolean exists = STORAGE_REMOVABLE.get(path);
    return exists != null ? exists : false;
  }

  @Implementation(minSdk = LOLLIPOP)
  protected static boolean isExternalStorageEmulated(File path) {
    final Boolean emulated = STORAGE_EMULATED.get(path);
    return emulated != null ? emulated : false;
  }

  @Implementation
  protected static boolean isExternalStorageEmulated() {
    return sIsExternalStorageEmulated;
  }

  @Implementation(minSdk = Q)
  protected static boolean isExternalStorageLegacy(File path) {
    return isExternalStorageLegacy;
  }

  @Implementation(minSdk = Q)
  protected static boolean isExternalStorageLegacy() {
    return isExternalStorageLegacy;
  }

  /**
   * Sets the "isRemovable" flag of a particular file.
   *
   * @param file Target file.
   * @param isRemovable True if the filesystem is removable.
   */
  public static void setExternalStorageRemovable(File file, boolean isRemovable) {
    STORAGE_REMOVABLE.put(file, isRemovable);
  }

  /**
   * Sets the "isEmulated" flag of a particular file.
   *
   * @param file Target file.
   * @param isEmulated True if the filesystem is emulated.
   */
  public static void setExternalStorageEmulated(File file, boolean isEmulated) {
    STORAGE_EMULATED.put(file, isEmulated);
  }

  /**
   * Adds a directory to list returned by {@link ShadowUserEnvironment#getExternalDirs()}.
   *
   * @param path the external dir to add
   */
  public static File addExternalDir(String path) {
    Path externalFileDir;
    if (path == null) {
      externalFileDir = null;
    } else {
      try {
        if (tmpExternalFilesDirBase == null) {
          tmpExternalFilesDirBase =
              RuntimeEnvironment.getTempDirectory().create("external-files-base");
        }
        externalFileDir = tmpExternalFilesDirBase.resolve(path);
        Files.createDirectories(externalFileDir);
        externalDirs.add(externalFileDir.toFile());
      } catch (IOException e) {
        throw new RuntimeException("Could not create external files dir", e);
      }
    }

    if (RuntimeEnvironment.getApiLevel() >= JELLY_BEAN_MR1
        && RuntimeEnvironment.getApiLevel() < KITKAT) {
      if (externalDirs.size() == 1 && externalFileDir != null) {
        Environment.UserEnvironment userEnvironment =
            ReflectionHelpers.getStaticField(Environment.class, "sCurrentUser");
        reflector(_UserEnvironment_.class, userEnvironment)
            .setExternalStorageAndroidData(externalFileDir.toFile());
      }
    } else if (RuntimeEnvironment.getApiLevel() >= KITKAT && RuntimeEnvironment.getApiLevel() < M) {
      Environment.UserEnvironment userEnvironment =
          ReflectionHelpers.getStaticField(Environment.class, "sCurrentUser");
      reflector(_UserEnvironment_.class, userEnvironment)
          .setExternalDirsForApp(externalDirs.toArray(new File[0]));
    }

    if (externalFileDir == null) {
      return null;
    }
    return externalFileDir.toFile();
  }

  /**
   * Sets the {@link #getExternalStorageState(File)} for given directory.
   *
   * @param externalStorageState Value to return from {@link #getExternalStorageState(File)}.
   */
  public static void setExternalStorageState(File directory, String state) {
    storageState.put(directory.toPath(), state);
  }

  @Implements(className = "android.os.Environment$UserEnvironment", isInAndroidSdk = false,
      minSdk = JELLY_BEAN_MR1)
  public static class ShadowUserEnvironment {

    @Implementation(minSdk = M)
    protected File[] getExternalDirs() {
      return externalDirs.toArray(new File[externalDirs.size()]);
    }
  }

  /** Accessor interface for Environment.UserEnvironment's internals. */
  @ForType(className = "android.os.Environment$UserEnvironment")
  interface _UserEnvironment_ {
    @Accessor("mExternalDirsForApp")
    void setExternalDirsForApp(File[] files);

    @Accessor("mExternalStorageAndroidData")
    void setExternalStorageAndroidData(File file);
  }
}
