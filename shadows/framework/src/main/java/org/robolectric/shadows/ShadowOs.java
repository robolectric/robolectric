package org.robolectric.shadows;

import static org.robolectric.util.reflector.Reflector.reflector;

import android.system.ErrnoException;
import android.system.Os;
import android.system.OsConstants;
import java.io.FileDescriptor;
import java.util.HashMap;
import java.util.Map;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.util.reflector.Direct;
import org.robolectric.util.reflector.ForType;
import org.robolectric.util.reflector.Static;

/** A Shadow for android.system.Os */
@Implements(Os.class)
public final class ShadowOs {

  private ShadowOs() {}

  private static final Map<Integer, Long> sysconfValues = new HashMap<>();
  private static final Map<FileDescriptor, Long> offsetValues = new HashMap<>();

  /** Configures values to be returned by sysconf. */
  public static void setSysconfValue(int name, long value) {
    sysconfValues.put(name, value);
  }

  /** Returns the value configured via setSysconfValue, or -1 if one hasn't been configured. */
  @Implementation
  protected static long sysconf(int name) {
    return sysconfValues.getOrDefault(name, -1L);
  }

  /**
   * Shadow implementation of {@link Os#lseek(FileDescriptor, long, int)}.
   *
   * <p>This method overrides the original lseek method to handle the SEEK_SET and SEEK_CUR with the
   * offset cache.
   */
  @Implementation
  protected static long lseek(FileDescriptor fd, long offset, int whence) throws ErrnoException {
    // Additional logic for handling the SEEK_SET and SEEK_CUR with the offset cache because the
    // original implementation doesn't actually change the offset in Robolectric.
    if (whence == OsConstants.SEEK_SET) {
      offsetValues.put(fd, offset);
      return offset;
    } else if (whence == OsConstants.SEEK_CUR) {
      long newOffset = offsetValues.getOrDefault(fd, 0L) + offset;
      offsetValues.put(fd, newOffset);
      return newOffset;
    }
    // For other 'whence' values, fall back to the original implementation.
    return reflector(OsReflector.class).lseek(fd, offset, whence);
  }

  @ForType(Os.class)
  interface OsReflector {
    @Direct
    @Static
    long lseek(FileDescriptor fd, long offset, int whence);
  }
}
