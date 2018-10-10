package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.P;
import static org.robolectric.util.ReflectionHelpers.ClassParameter.from;

import android.os.CancellationSignal;
import android.os.FileUtils;
import android.os.FileUtils.ProgressListener;
import java.io.FileDescriptor;
import java.io.IOException;
import java.util.concurrent.Executor;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.util.ReflectionHelpers;

@Implements(value = FileUtils.class, isInAndroidSdk = false, minSdk = P)
public class ShadowFileUtils {

  @Implementation(minSdk = P, maxSdk = P)
  protected static long copy(
      FileDescriptor in,
      FileDescriptor out,
      ProgressListener listener,
      CancellationSignal signal,
      long count)
      throws IOException {
    // never do the native copy optimization block
    return ReflectionHelpers.callStaticMethod(FileUtils.class,
        "copyInternalUserspace",
        from(FileDescriptor.class, in),
        from(FileDescriptor.class, out),
        from(ProgressListener.class, listener),
        from(CancellationSignal.class, signal),
        from(long.class, count));
  }

  // BEGIN-INTERNAL
  @Implementation(
      minSdk = android.os.Build.VERSION_CODES.Q,
      maxSdk = android.os.Build.VERSION_CODES.Q)
  protected static long copy(
      FileDescriptor in,
      FileDescriptor out,
      long count,
      CancellationSignal signal,
      Executor executor,
      ProgressListener listener)
      throws IOException {
    // never do the native copy optimization block
    return ReflectionHelpers.callStaticMethod(
        FileUtils.class,
        "copyInternalUserspace",
        from(FileDescriptor.class, in),
        from(FileDescriptor.class, out),
        from(long.class, count),
        from(CancellationSignal.class, signal),
        from(Executor.class, executor),
        from(ProgressListener.class, listener));
  }
  // END-INTERNAL

}
