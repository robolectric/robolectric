package org.robolectric.shadows;

import android.system.ErrnoException;
import libcore.io.BlockGuardOs;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

import java.io.FileDescriptor;

@Implements(value = BlockGuardOs.class, isInAndroidSdk = false)
public class ShadowBlockGuardOs {
  /**
   * This is a no-op in the Robolectric implementation. The file descriptor will be
   * closed when associated FileInputStreams are closed or garbage collected.
   */
  @Implementation
  public void close(FileDescriptor fd) throws ErrnoException {
  }
}
