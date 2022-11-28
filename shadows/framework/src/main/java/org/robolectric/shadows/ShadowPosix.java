package org.robolectric.shadows;

import android.os.Build;
import android.system.ErrnoException;
import android.system.StructStat;
import java.io.File;
import java.io.FileDescriptor;
import java.time.Duration;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.util.ReflectionHelpers;

/** Shadow for {@link libcore.io.Posix} */
@Implements(
    className = "libcore.io.Posix",
    maxSdk = Build.VERSION_CODES.N_MR1,
    isInAndroidSdk = false)
public class ShadowPosix {
  @Implementation
  public void mkdir(String path, int mode) throws ErrnoException {
    new File(path).mkdirs();
  }

  @Implementation
  public static Object stat(String path) throws ErrnoException {
    int mode = OsConstantsValues.getMode(path);
    long size = 0;
    long modifiedTime = 0;
    if (path != null) {
      File file = new File(path);
      size = file.length();
      modifiedTime = Duration.ofMillis(file.lastModified()).getSeconds();
    }

    if (RuntimeEnvironment.getApiLevel() >= Build.VERSION_CODES.LOLLIPOP) {
      return new StructStat(
        1, // st_dev
        0, // st_ino
        mode, // st_mode
        0, // st_nlink
        0, // st_uid
        0, // st_gid
        0, // st_rdev
        size, // st_size
        0, // st_atime
        modifiedTime, // st_mtime
        0, // st_ctime,
        0, // st_blksize
        0 // st_blocks
        );
    } else {
      Object structStat =
          ReflectionHelpers.newInstance(
              ReflectionHelpers.loadClass(
                  ShadowPosix.class.getClassLoader(), "libcore.io.StructStat"));
      setMode(mode, structStat);
      setSize(size, structStat);
      setModifiedTime(modifiedTime, structStat);
      return structStat;
    }
  }

  @Implementation
  protected static Object lstat(String path) throws ErrnoException {
    return stat(path);
  }

  @Implementation
  protected static Object fstat(FileDescriptor fd) throws ErrnoException {
    return stat(null);
  }

  private static void setMode(int mode, Object structStat) {
    ReflectionHelpers.setField(structStat, "st_mode", mode);
  }

  private static void setSize(long size, Object structStat) {
    ReflectionHelpers.setField(structStat, "st_size", size);
  }

  private static void setModifiedTime(long modifiedTime, Object structStat) {
    ReflectionHelpers.setField(structStat, "st_mtime", modifiedTime);
  }
}
