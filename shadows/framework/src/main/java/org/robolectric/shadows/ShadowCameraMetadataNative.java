package org.robolectric.shadows;

import android.hardware.camera2.impl.CameraMetadataNative;
import android.os.Build.VERSION_CODES;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/** Shadow class for {@link CameraMetadataNative} */
@Implements(
    value = CameraMetadataNative.class,
    minSdk = VERSION_CODES.LOLLIPOP,
    isInAndroidSdk = false)
public class ShadowCameraMetadataNative {

  @Implementation(minSdk = VERSION_CODES.P)
  protected long nativeAllocate() {
    return 1L;
  }

  @Implementation(minSdk = VERSION_CODES.P)
  protected long nativeAllocateCopy(CameraMetadataNative other) {
    return 1L;
  }
}
