package org.robolectric.shadows;

import android.hardware.camera2.TotalCaptureResult;
import android.os.Build.VERSION_CODES;
import org.robolectric.annotation.Implements;
import org.robolectric.util.ReflectionHelpers;

/** Shadow of {@link TotalCaptureResult}. */
@Implements(value = TotalCaptureResult.class, minSdk = VERSION_CODES.LOLLIPOP)
public class ShadowTotalCaptureResult extends ShadowCaptureResult {

  /** Convenience method which returns a new instance of {@link TotalCaptureResult}. */
  public static TotalCaptureResult newTotalCaptureResult() {
    return ReflectionHelpers.callConstructor(TotalCaptureResult.class);
  }
}
