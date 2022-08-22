package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR1;
import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR2;
import static android.os.Build.VERSION_CODES.KITKAT;
import static android.os.Build.VERSION_CODES.M;
import static android.os.Build.VERSION_CODES.Q;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.view.Surface;
import dalvik.system.CloseGuard;
import java.lang.ref.WeakReference;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.annotation.ReflectorObject;
import org.robolectric.annotation.Resetter;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.shadows.ShadowSurfaceTexture.SurfaceTextureReflector;
import org.robolectric.util.ReflectionHelpers.ClassParameter;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.Direct;
import org.robolectric.util.reflector.ForType;

/** Shadow for {@link android.view.Surface} */
@Implements(value = Surface.class, looseSignatures = true)
public class ShadowSurface {
  private static final AtomicInteger nativeObject = new AtomicInteger();

  private SurfaceTexture surfaceTexture;
  private Canvas canvas;
  @RealObject private Surface realSurface;
  @ReflectorObject private SurfaceReflector surfaceReflector;

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
    CloseGuard closeGuard = surfaceReflector.getCloseGuard();
    if (closeGuard != null) {
      closeGuard.close();
    }
    surfaceReflector.finalize();
  }

  @Implementation
  protected boolean isValid() {
    return valid.get();
  }

  @Implementation
  protected void release() {
    valid.set(false);
    surfaceReflector.release();
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
    canvas = surfaceReflector.lockHardwareCanvas();
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

  @Implementation(minSdk = JELLY_BEAN_MR2)
  protected static Object nativeCreateFromSurfaceTexture(Object surfaceTexture) {
    return nativeObject.incrementAndGet();
  }

  @Implementation(minSdk = JELLY_BEAN_MR2)
  protected static Object nativeCreateFromSurfaceControl(Object surfaceControlNativeObject) {
    return nativeObject.incrementAndGet();
  }

  @Implementation(minSdk = Q)
  protected static long nativeGetFromSurfaceControl(
      long surfaceObject, long surfaceControlNativeObject) {
    return nativeObject.incrementAndGet();
  }

  @Resetter
  public static void reset() {
    nativeObject.set(0);
  }

  @ForType(Surface.class)
  interface SurfaceReflector {
    @Accessor("mCloseGuard")
    CloseGuard getCloseGuard();

    @Direct
    void finalize();

    @Direct
    void release();

    @Direct
    Canvas lockHardwareCanvas();
  }
}
