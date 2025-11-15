package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.Q;
import static android.os.Build.VERSION_CODES.R;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.hardware.camera2.impl.CameraMetadataNative;
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

  @ForType(CameraMetadataNative.class)
  interface CameraMetadataNativeReflector {
    @Direct
    void __constructor__(CameraMetadataNative other);
  }
}
