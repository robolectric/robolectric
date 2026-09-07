package org.robolectric.shadows;

import static org.robolectric.util.reflector.Reflector.reflector;

import android.hardware.camera2.extension.CameraOutputSurface;
import android.util.Size;
import android.view.Surface;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import org.robolectric.util.reflector.Constructor;
import org.robolectric.util.reflector.ForType;

/** Builder for {@link CameraOutputSurface}. */
public class CameraOutputSurfaceBuilder {
  private Surface surface;
  private Size size;

  private CameraOutputSurfaceBuilder() {}

  public static CameraOutputSurfaceBuilder newBuilder() {
    return new CameraOutputSurfaceBuilder();
  }

  @CanIgnoreReturnValue
  public CameraOutputSurfaceBuilder setSurface(Surface surface) {
    this.surface = surface;
    return this;
  }

  @CanIgnoreReturnValue
  public CameraOutputSurfaceBuilder setSize(Size size) {
    this.size = size;
    return this;
  }

  public CameraOutputSurface build() {
    return reflector(CameraOutputSurfaceReflector.class).newCameraOutputSurface(surface, size);
  }

  @ForType(CameraOutputSurface.class)
  interface CameraOutputSurfaceReflector {
    @Constructor
    CameraOutputSurface newCameraOutputSurface(Surface surface, Size size);
  }
}
