package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR1;
import static android.os.Build.VERSION_CODES.KITKAT;
import static android.os.Build.VERSION_CODES.M;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.view.Surface;
import dalvik.system.CloseGuard;
import java.lang.ref.WeakReference;
import java.util.concurrent.atomic.AtomicBoolean;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.shadows.ShadowSurfaceTexture.SurfaceTextureReflector;
import org.robolectric.util.ReflectionHelpers.ClassParameter;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.Direct;
import org.robolectric.util.reflector.ForType;

/** Shadow for {@link android.view.Surface} */
@Implements(Surface.class)
public class ShadowSurface {
  private SurfaceTexture surfaceTexture;
  private Canvas canvas;
  @RealObject private Surface realSurface;
  private final AtomicBoolean valid = new AtomicBoolean(true);
  private final AtomicBoolean canvasLocked = new AtomicBoolean(false);

  @Implementation
  protected void __constructor__(SurfaceTexture surfaceTexture) {
    this.surfaceTexture = surfaceTexture;
    Shadow.invokeConstructor(
        Surface.class, realSurface, ClassParameter.from(SurfaceTexture.class, surfaceTexture));
  }

  public SurfaceTexture getSurfaceTexture() {
    return surfaceTexture;
  }

  @Implementation(minSdk = JELLY_BEAN_MR1)
  protected void finalize() throws Throwable {
    // Suppress noisy CloseGuard errors that may exist in SDK 17+.
    CloseGuard closeGuard = reflector(SurfaceReflector.class, realSurface).getCloseGuard();
    if (closeGuard != null) {
      closeGuard.close();
    }
    reflector(SurfaceReflector.class, realSurface).finalize();
  }

  @Implementation
  protected boolean isValid() {
    return valid.get();
  }

  @Implementation
  protected void release() {
    valid.set(false);
    reflector(SurfaceReflector.class, realSurface).release();
  }

  private void checkNotReleased() {
    if (!valid.get()) {
      throw new IllegalStateException("Surface has already been released.");
    }
  }

  private void checkNotLocked() {
    if (canvasLocked.get()) {
      throw new IllegalStateException("Surface has already been locked.");
    }
  }

  private void checkNotReleasedOrLocked() {
    checkNotReleased();
    checkNotLocked();
  }

  @Implementation
  protected Canvas lockCanvas(Rect inOutDirty) {
    checkNotReleasedOrLocked();
    canvasLocked.set(true);
    if (canvas == null) {
      canvas = new Canvas();
    }
    return canvas;
  }

  @Implementation(minSdk = M)
  protected Canvas lockHardwareCanvas() {
    checkNotReleasedOrLocked();
    canvasLocked.set(true);
    if (canvas == null) {
      canvas = new Canvas();
    }
    return canvas;
  }

  @Implementation
  protected void unlockCanvasAndPost(Canvas canvas) {
    checkNotReleased();
    if (!canvasLocked.get()) {
      throw new IllegalStateException("Canvas is not locked!");
    }
    if (surfaceTexture != null) {
      if (RuntimeEnvironment.getApiLevel() > KITKAT) {
        reflector(SurfaceTextureReflector.class, surfaceTexture)
            .postEventFromNative(new WeakReference<>(surfaceTexture));
      } else {
        reflector(SurfaceTextureReflector.class, surfaceTexture)
            .postEventFromNative((Object) new WeakReference<>(surfaceTexture));
      }
    }
    canvasLocked.set(false);
  }

  @ForType(Surface.class)
  interface SurfaceReflector {
    @Accessor("mCloseGuard")
    CloseGuard getCloseGuard();

    @Direct
    void finalize();

    @Direct
    void release();
  }
}
