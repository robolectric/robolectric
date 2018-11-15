// BEGIN-INTERNAL
package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.Q;

import android.graphics.RecordingCanvas;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

@Implements(value = RecordingCanvas.class, isInAndroidSdk = false, minSdk = Q)
public class ShadowRecordingCanvas extends ShadowCanvas {

  @Implementation
  protected static long nCreateDisplayListCanvas(long node, int width, int height) {
    return 1;
  }

  @Config
  protected static long nCreateDisplayListCanvas(int width, int height) {
    return 1;
  }

  @Config
  protected static long nCreateDisplayListCanvas() {
    return 1;
  }
}
// END-INTERNAL