package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.Q;
import static android.os.Build.VERSION_CODES.R;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.impl.CameraMetadataNative;
import com.google.common.base.Preconditions;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.util.reflector.Direct;
import org.robolectric.util.reflector.ForType;

/** Shadow class for {@link CameraMetadataNative} */
@Implements(value = CameraMetadataNative.class, isInAndroidSdk = false)
public class ShadowCameraMetadataNative {
  @RealObject private CameraMetadataNative realObject;

  private final Map<CameraMetadataNative.Key<?>, Object> characteristics =
      Collections.synchronizedMap(new HashMap<>());

  @Implementation
  protected void __constructor__(CameraMetadataNative other) {
    Preconditions.checkNotNull(other);
    reflector(CameraMetadataNativeReflector.class, realObject).__constructor__(other);
    ShadowCameraMetadataNative otherShadow = Shadow.extract(other);
    this.characteristics.putAll(otherShadow.characteristics);
  }

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

  @Implementation
  protected <T> void set(CameraMetadataNative.Key<T> key, T value) {
    characteristics.put(key, value);
  }

  @Implementation
  protected <T> T get(CameraMetadataNative.Key<T> key) {
    return (T) characteristics.get(key);
  }

  /**
   * This method is called by `CameraMetadata.getKeys -> CaptureRequest.getProtected` when iterating
   * over the Key fields using reflection in CaptureRequest. When CameraMetadata iterates over the
   * fields in CaptureRequest, the `__robo_data__` field is included. This is a special field and
   * results in an NPE when CameraMetadataNative.get is called.
   *
   * <p>TODO(hoisie): It would be nice if we could fix this by making CameraMetadata skip the
   * `__robo_data__` field, but I was unable to find a good way to do that. If that field is made
   * private, there is a performance regression due to the extra access checks for native
   * animations.
   */
  @Implementation
  protected <T> T get(CaptureRequest.Key<T> key) {

    if (key == null) {
      return null;
    }
    return reflector(CameraMetadataNativeReflector.class, realObject).get(key);
  }

  @ForType(CameraMetadataNative.class)
  interface CameraMetadataNativeReflector {
    @Direct
    void __constructor__(CameraMetadataNative other);

    @Direct
    <T> T get(CaptureRequest.Key<T> key);
  }
}
