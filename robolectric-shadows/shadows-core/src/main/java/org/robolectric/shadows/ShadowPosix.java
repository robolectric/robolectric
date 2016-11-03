package org.robolectric.shadows;

import android.system.ErrnoException;
import libcore.io.Posix;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

import java.io.File;

@Implements(Posix.class)
public class ShadowPosix {
  @Implementation
  public static void mkdir(String path, int mode) throws ErrnoException {
    new File(path).mkdirs();
  }
}
