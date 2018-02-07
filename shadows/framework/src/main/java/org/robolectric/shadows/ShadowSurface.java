package org.robolectric.shadows;

import android.graphics.SurfaceTexture;
import android.view.Surface;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

@Implements(Surface.class)
public class ShadowSurface {
  private SurfaceTexture surfaceTexture;

  @Implementation
  public void __constructor__(SurfaceTexture surfaceTexture) {
    this.surfaceTexture = surfaceTexture;
  }

  public SurfaceTexture getSurfaceTexture() {
    return surfaceTexture;
  }
}
