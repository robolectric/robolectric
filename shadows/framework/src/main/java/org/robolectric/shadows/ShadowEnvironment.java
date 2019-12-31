package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR1;
import static android.os.Build.VERSION_CODES.KITKAT;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.M;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.os.Environment;
import java.io.File;
import java.io.IOException;
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

  @Implementation
  protected static File getExternalStorageDirectory() {
    if (!exists(EXTERNAL_CACHE_DIR)) EXTERNAL_CACHE_DIR = RuntimeEnvironment.getTempDirectory().create("external-cache");
    return EXTERNAL_CACHE_DIR.toFile();
  }

  @Implementation
  protected static File getExternalStoragePublicDirectory(String type) {
    if (!exists(EXTERNAL_FILES_DIR)) EXTERNAL_FILES_DIR = RuntimeEnvironment.getTempDirectory().create("external-files");
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

    sIsExternalStorageEmulated = false;
  }

  private static boolean exists(Path path) {
    return path != null && Files.exists(path);
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
          tmpExternalFilesDirBase = RuntimeEnvironment.getTempDirectory().create("external-files-base");
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
