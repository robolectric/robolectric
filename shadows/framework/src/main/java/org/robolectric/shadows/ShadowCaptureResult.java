package org.robolectric.shadows;

import static org.robolectric.util.reflector.Reflector.reflector;

import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.impl.CameraMetadataNative;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.ForType;

/** Shadow of {@link CaptureResult}. */
@Implements(CaptureResult.class)
public class ShadowCaptureResult {

  @RealObject private CaptureResult realObject;

  /** Convenience method which returns a new instance of {@link CaptureResult}. */
  public static CaptureResult newCaptureResult() {
    CameraMetadataNative cm = new CameraMetadataNative();
    return new CaptureResult(cm, /* sequenceId= */ 0);
  }

  public <T> void set(CaptureResult.Key<T> key, T value) {
    reflector(CaptureResultReflector.class, realObject).getResults().set(key, value);
  }

  @ForType(CaptureResult.class)
  interface CaptureResultReflector {
    @Accessor("mResults")
    CameraMetadataNative getResults();
  }
}
