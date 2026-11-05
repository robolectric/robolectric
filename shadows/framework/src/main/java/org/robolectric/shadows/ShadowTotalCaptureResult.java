package org.robolectric.shadows;

import android.hardware.camera2.TotalCaptureResult;
import org.robolectric.annotation.Implements;

/**
 * Shadow of {@link TotalCaptureResult}.
 *
 * @deprecated Use {@link TotalCaptureResultBuilder} instead.
 */
@Deprecated
@Implements(TotalCaptureResult.class)
public class ShadowTotalCaptureResult extends ShadowCaptureResult {

  /**
   * Convenience method which returns a new instance of {@link TotalCaptureResult}.
   *
   * @deprecated Use {@link TotalCaptureResultBuilder} instead.
   */
  @Deprecated
  public static TotalCaptureResult newTotalCaptureResult() {
    return TotalCaptureResultBuilder.newBuilder().build();
  }
}
