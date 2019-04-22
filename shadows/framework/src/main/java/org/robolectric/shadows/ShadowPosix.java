package org.robolectric.shadows;

import android.os.Build;
import android.system.ErrnoException;
import android.system.StructStat;
import java.io.File;
import java.io.FileDescriptor;
import java.lang.reflect.Field;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.util.ReflectionHelpers;

@Implements(className = "libcore.io.Posix", maxSdk = Build.VERSION_CODES.N_MR1, isInAndroidSdk = false)
public class ShadowPosix {
  @Implementation
  public static void mkdir(String path, int mode) throws ErrnoException {
    new File(path).mkdirs();
  }

  @Implementation
  public static Object stat(String path) throws ErrnoException {
    int mode = OsConstantsValues.getMode(path);
    if (RuntimeEnvironment.getApiLevel() >= Build.VERSION_CODES.LOLLIPOP) {
      return new StructStat(1, 0, mode, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
    } else {
      Object structStat =
          ReflectionHelpers.newInstance(
              ReflectionHelpers.loadClass(
                  ShadowPosix.class.getClassLoader(), "libcore.io.StructStat"));
      setMode(mode, structStat);
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
    try {
      Field f = structStat.getClass().getDeclaredField("st_mode");
      f.setAccessible(true);
      f.setInt(structStat, mode);
    } catch (Throwable t) {
      throw new RuntimeException(t);
    }
  }
}
