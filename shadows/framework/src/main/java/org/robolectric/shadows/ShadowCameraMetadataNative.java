package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.Q;
import static android.os.Build.VERSION_CODES.R;

import android.hardware.camera2.impl.CameraMetadataNative;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/** Shadow class for {@link CameraMetadataNative} */
@Implements(value = CameraMetadataNative.class, isInAndroidSdk = false)
public class ShadowCameraMetadataNative {
  @Implementation(minSdk = R)
  protected static long nativeAllocate() {
    return 1L;
  }

  @Implementation(maxSdk = Q, methodName = "nativeAllocate")
  protected long nativeAllocateQ() {
    return 1L;
  }

  @Implementation(minSdk = R)
  protected static long nativeAllocateCopy(long other) {
    return 1L;
  }

  @Implementation(maxSdk = Q)
  protected long nativeAllocateCopy(CameraMetadataNative other) {
    return 1L;
  }
}
