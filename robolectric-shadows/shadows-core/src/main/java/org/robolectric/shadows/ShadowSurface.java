package org.robolectric.shadows;

import android.view.Surface;
import android.graphics.SurfaceTexture;
import org.robolectric.annotation.Implements;

/**
 * Shadow for {@link android.view.Surface}.
 */
@Implements(Surface.class)
public class ShadowSurface {
  private SurfaceTexture surfaceTexture;

  public void __constructor__(SurfaceTexture surfaceTexture) {
    this.surfaceTexture = surfaceTexture;
  }

  public SurfaceTexture getSurfaceTexture() {
    return surfaceTexture;
  }
}
