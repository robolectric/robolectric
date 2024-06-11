package org.robolectric.shadows;

import android.os.Build;
import android.system.ErrnoException;
import android.system.StructStat;
import java.io.File;
import java.io.FileDescriptor;
import java.time.Duration;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/** Shadow for {@link libcore.io.Posix} */
@Implements(
    className = "libcore.io.Posix",
    maxSdk = Build.VERSION_CODES.N_MR1,
    isInAndroidSdk = false,
    looseSignatures = true)
public class ShadowPosix {
  @Implementation
  public void mkdir(String path, int mode) throws ErrnoException {
    new File(path).mkdirs();
  }

  @Implementation
  // actually preventing a 'static' mismatch
  @SuppressWarnings("robolectric.ShadowReturnTypeMismatch")
  public static Object stat(String path) throws ErrnoException {
    int mode = OsConstantsValues.getMode(path);
    long size = 0;
    long modifiedTime = 0;
    if (path != null) {
      File file = new File(path);
      size = file.length();
      modifiedTime = Duration.ofMillis(file.lastModified()).getSeconds();
    }

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
  }

  @Implementation
  // actually preventing a 'static' mismatch
  @SuppressWarnings("robolectric.ShadowReturnTypeMismatch")
  protected static Object lstat(String path) throws ErrnoException {
    return stat(path);
  }

  @Implementation
  // actually preventing a 'static' mismatch
  @SuppressWarnings("robolectric.ShadowReturnTypeMismatch")
  protected static Object fstat(FileDescriptor fd) throws ErrnoException {
    return stat(null);
  }
}
