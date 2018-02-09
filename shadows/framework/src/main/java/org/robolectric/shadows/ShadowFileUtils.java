// BEGIN-INTERNAL
package org.robolectric.shadows;

import android.os.CancellationSignal;
import android.os.FileUtils;
import android.os.FileUtils.ProgressListener;
import java.io.FileDescriptor;
import java.io.IOException;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

@Implements(value = FileUtils.class, isInAndroidSdk = false)
public class ShadowFileUtils {

  @Implementation
  protected static long copy( FileDescriptor in,  FileDescriptor out,
      ProgressListener listener,  CancellationSignal signal, long count)
      throws IOException {
    // never do the native copy optimization block
    return FileUtils.copyInternalUserspace(in, out, listener, signal, count);
  }
}
// END-INTERNAL