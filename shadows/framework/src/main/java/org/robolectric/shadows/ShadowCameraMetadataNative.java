package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.Q;

import android.hardware.camera2.impl.CameraMetadataNative;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/** Shadow class for {@link CameraMetadataNative} */
@Implements(
    value = CameraMetadataNative.class,
    minSdk = LOLLIPOP,
    maxSdk = Q,
    isInAndroidSdk = false)
public class ShadowCameraMetadataNative {
  @Implementation(minSdk = LOLLIPOP, maxSdk = Q)
  protected long nativeAllocate() {
    return 1L;
  }

  @Implementation(minSdk = LOLLIPOP, maxSdk = Q)
  protected long nativeAllocateCopy(CameraMetadataNative other) {
    return 1L;
  }
}
