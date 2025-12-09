package org.robolectric.shadows;

import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.impl.CameraMetadataNative;
import org.robolectric.annotation.Implements;

/** Shadow of {@link TotalCaptureResult}. */
@Implements(TotalCaptureResult.class)
public class ShadowTotalCaptureResult extends ShadowCaptureResult {

  /** Convenience method which returns a new instance of {@link TotalCaptureResult}. */
  public static TotalCaptureResult newTotalCaptureResult() {
    CameraMetadataNative cm = new CameraMetadataNative();
    return new TotalCaptureResult(cm, /* sequenceId= */ 0);
  }
}
