package org.robolectric.shadows;

import android.os.Build;
import android.system.ErrnoException;
import android.system.StructStat;
import java.io.File;
import libcore.io.Linux;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

@Implements(value = Linux.class, minSdk = Build.VERSION_CODES.O, isInAndroidSdk = false)
public class ShadowLinux {
  @Implementation
  public static void mkdir(String path, int mode) throws ErrnoException {
    new File(path).mkdirs();
  }

  @Implementation
  public StructStat stat(String path) throws ErrnoException {
    return new StructStat(0, // st_dev
        0, // st_ino
        0, // st_mode
        0, // st_nlink
        0, // st_uid
        0, // st_gid
        0, // st_rdev
        0, // st_size
        0, // st_atime
        0, // st_mtime
        0, // st_ctime,
        0, // st_blksize
        0 // st_blocks
    );
  }
}