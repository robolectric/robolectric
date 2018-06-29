package org.robolectric.shadows;

import android.system.ErrnoException;
import java.io.FileDescriptor;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

@Implements(className = "libcore.io.BlockGuardOs", isInAndroidSdk = false)
public class ShadowBlockGuardOs {
  // override to avoid call to non-existent FileDescriptor.isSocket
  @Implementation
  protected void close(FileDescriptor fd) throws ErrnoException {
    // ignore
  }
}