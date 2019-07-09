package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.N_MR1;

import android.os.Build;
import android.system.ErrnoException;
import android.system.OsConstants;
import android.system.StructStat;
import android.util.Log;
import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.time.Duration;
import libcore.io.Linux;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

@Implements(value = Linux.class, minSdk = Build.VERSION_CODES.O, isInAndroidSdk = false)
public class ShadowLinux {
  @Implementation
  public void mkdir(String path, int mode) throws ErrnoException {
    new File(path).mkdirs();
  }

  @Implementation
  public StructStat stat(String path) throws ErrnoException {
    int mode = OsConstantsValues.getMode(path);
    long size = 0;
    long modifiedTime = 0;
    if (path != null) {
      File file = new File(path);
      size = file.length();
      modifiedTime = Duration.ofMillis(file.lastModified()).getSeconds();
    }
    return new StructStat(
        0, // st_dev
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
  protected StructStat lstat(String path) throws ErrnoException {
    return stat(path);
  }

  @Implementation(maxSdk = N_MR1)
  protected StructStat fstat(String path) throws ErrnoException {
    return stat(path);
  }

  @Implementation
  protected StructStat fstat(FileDescriptor fd) throws ErrnoException {
    return stat(null);
  }

  @Implementation
  protected FileDescriptor open(String path, int flags, int mode) throws ErrnoException {
    try {
      RandomAccessFile randomAccessFile = new RandomAccessFile(path, modeToString(mode));
      return randomAccessFile.getFD();
    } catch (IOException e) {
      Log.e("ShadowLinux", "open failed for " + path, e);
      throw new ErrnoException("open", OsConstants.EIO);
    }
  }

  private static String modeToString(int mode) {
    if (mode == OsConstants.O_RDONLY) {
      return "r";
    }
    return "rw";
  }
}
