package org.robolectric.shadows;

import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureRequest.Key;
import android.os.Build.VERSION_CODES;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/** Shadow class for {@link CaptureRequest.Builder} */
@Implements(value = CaptureRequest.Builder.class, minSdk = VERSION_CODES.LOLLIPOP)
public class ShadowCaptureRequestBuilder {

  /**
   * Original implementation would store these values in a local CameraMetadataNative object. Trying
   * to set these values causes issues while testing as that starts to involve native code.
   */
  @Implementation(minSdk = VERSION_CODES.P)
  protected <T> void set(Key<T> key, T value) {}
}
