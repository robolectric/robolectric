package org.robolectric.shadows;

import android.os.Build;
import android.system.ErrnoException;
import android.system.StructStat;
import libcore.io.Posix;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.util.ReflectionHelpers;

import java.io.File;

@Implements(value = Posix.class, isInAndroidSdk = false)
public class ShadowPosix {
  @Implementation
  public static void mkdir(String path, int mode) throws ErrnoException {
    new File(path).mkdirs();
  }

  @Implementation
  public static Object stat(String path) throws ErrnoException {
    if (RuntimeEnvironment.getApiLevel() >= Build.VERSION_CODES.LOLLIPOP) {
      return new StructStat(1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
    } else {
      return ReflectionHelpers.newInstance(ReflectionHelpers.loadClass(ShadowPosix.class.getClassLoader(), "libcore.io.StructStat"));
    }
  }
}
