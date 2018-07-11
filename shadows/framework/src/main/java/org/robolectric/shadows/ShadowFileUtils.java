package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.P;
import static org.robolectric.util.ReflectionHelpers.ClassParameter.from;

import android.os.CancellationSignal;
import android.os.FileUtils;
import android.os.FileUtils.ProgressListener;
import java.io.FileDescriptor;
import java.io.IOException;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.util.ReflectionHelpers;

@Implements(value = FileUtils.class, isInAndroidSdk = false, minSdk=P)
public class ShadowFileUtils {

  // BEGIN-INTERNAL
  @Implementation(minSdk=android.os.Build.VERSION_CODES.Q)
  protected static void __staticInitializer__() {
    // don't do native copy optimizations on Robolectric
    ReflectionHelpers.setStaticField(FileUtils.class, "sEnableCopyOptimizations", false);
  }
  // END-INTERNAL

  @Implementation(minSdk=P, maxSdk=P)
  protected static long copy( FileDescriptor in,  FileDescriptor out,
      ProgressListener listener,  CancellationSignal signal, long count)
      throws IOException {
    // ENABLE_COPY_OPTIMIZATIONS is final on P so call directly to userspace method
    // to simulate behavior when ENABLE_COPY_OPTIMIZATIONS is false
    return ReflectionHelpers.callStaticMethod(FileUtils.class,
        "copyInternalUserspace",
        from(FileDescriptor.class, in),
        from(FileDescriptor.class, out),
        from(ProgressListener.class, listener),
        from(CancellationSignal.class, signal),
        from(long.class, count));
  }
}
