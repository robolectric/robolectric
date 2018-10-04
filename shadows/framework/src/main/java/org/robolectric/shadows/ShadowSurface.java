package org.robolectric.shadows;

import android.graphics.SurfaceTexture;
import android.view.Surface;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.util.ReflectionHelpers.ClassParameter;

@Implements(Surface.class)
public class ShadowSurface {
  private SurfaceTexture surfaceTexture;
  @RealObject private Surface realSurface;

  @Implementation
  protected void __constructor__(SurfaceTexture surfaceTexture) {
    this.surfaceTexture = surfaceTexture;
    Shadow.invokeConstructor(
        Surface.class, realSurface, ClassParameter.from(SurfaceTexture.class, surfaceTexture));
  }

  public SurfaceTexture getSurfaceTexture() {
    return surfaceTexture;
  }
}
