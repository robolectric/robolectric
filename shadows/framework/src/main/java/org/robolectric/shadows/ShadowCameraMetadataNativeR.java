package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.R;

import android.hardware.camera2.impl.CameraMetadataNative;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/** Shadow class for {@link CameraMetadataNative} */
@Implements(
    value = CameraMetadataNative.class,
    minSdk = R,
    isInAndroidSdk = false,
    shadowPicker = ShadowCameraMetadataNativePicker.Picker.class)
public class ShadowCameraMetadataNativeR {
  // This method was changed to static in R, but otherwise has the same signature.
  @Implementation(minSdk = R)
  protected static long nativeAllocate() {
    return 1L;
  }

  @Implementation(minSdk = R)
  protected static long nativeAllocateCopy(long ptr) {
    return 1L;
  }
}
