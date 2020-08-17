package org.robolectric.shadows;

import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureRequest.Key;
import android.os.Build.VERSION_CODES;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/** Shadow class for {@link CaptureRequest.Builder}. */
@Implements(value = CaptureRequest.Builder.class, minSdk = VERSION_CODES.LOLLIPOP)
public class ShadowCaptureRequestBuilder {
  private final Map<Key<?>, Object> characteristics = Collections.synchronizedMap(new HashMap<>());

  /**
   * Original implementation would store its state in a local CameraMetadataNative object. Trying
   * to set these values causes issues while testing as that starts to involve native code. We write
   * to a managed map stored in the shadow instead.
   */
  @Implementation
  protected <T> void set(CaptureRequest.Key<T> key, T value) {
    characteristics.put(key, value);
  }

  /**
   * Original implementation would store its state in a local CameraMetadataNative object. Instead,
   * we are extracting the data from a managed map stored in the shadow.
   */
  @SuppressWarnings("unchecked")
  @Implementation
  protected <T> T get(CaptureRequest.Key<T> key) {
    return (T) characteristics.get(key);
  }
}
