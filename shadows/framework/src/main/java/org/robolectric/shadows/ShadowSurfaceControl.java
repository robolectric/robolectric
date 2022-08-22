package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR2;
import static android.os.Build.VERSION_CODES.N_MR1;
import static android.os.Build.VERSION_CODES.O;
import static android.os.Build.VERSION_CODES.P;
import static android.os.Build.VERSION_CODES.Q;

import android.os.Parcel;
import android.view.SurfaceControl;
import android.view.SurfaceSession;
import dalvik.system.CloseGuard;
import java.util.concurrent.atomic.AtomicInteger;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.ReflectorObject;
import org.robolectric.annotation.Resetter;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.Direct;
import org.robolectric.util.reflector.ForType;

/** Shadow for {@link android.view.SurfaceControl} */
@Implements(value = SurfaceControl.class, isInAndroidSdk = false, minSdk = JELLY_BEAN_MR2)
public class ShadowSurfaceControl {
  private static final AtomicInteger nativeObject = new AtomicInteger();

  @ReflectorObject private SurfaceControlReflector surfaceControlReflector;

  @Resetter
  public static void reset() {
    nativeObject.set(0);
  }

  @Implementation
  protected void finalize() throws Throwable {
    // Suppress noisy CloseGuard errors.
    CloseGuard closeGuard = surfaceControlReflector.getCloseGuard();
    if (closeGuard != null) {
      closeGuard.close();
    }
    surfaceControlReflector.finalize();
  }

  @Implementation(maxSdk = N_MR1)
  protected static Number nativeCreate(
      SurfaceSession session, String name, int w, int h, int format, int flags) {
    // Return a non-zero value otherwise constructing a SurfaceControl fails with
    // OutOfResourcesException.
    return nativeObject.incrementAndGet();
  }

  @Implementation(minSdk = O, maxSdk = P)
  protected static long nativeCreate(
      SurfaceSession session,
      String name,
      int w,
      int h,
      int format,
      int flags,
      long parentObject,
      int windowType,
      int ownerUid) {
    // Return a non-zero value otherwise constructing a SurfaceControl fails with
    // OutOfResourcesException.
    return nativeObject.incrementAndGet();
  }

  @Implementation(minSdk = Q)
  protected static long nativeCreate(
      SurfaceSession session,
      String name,
      int w,
      int h,
      int format,
      int flags,
      long parentObject,
      Parcel metadata) {
    // Return a non-zero value otherwise constructing a SurfaceControl fails with
    // OutOfResourcesException.
    return nativeObject.incrementAndGet();
  }

  void initializeNativeObject() {
    surfaceControlReflector.setNativeObject(nativeObject.incrementAndGet());
  }

  @ForType(SurfaceControl.class)
  interface SurfaceControlReflector {
    @Accessor("mCloseGuard")
    CloseGuard getCloseGuard();

    @Accessor("mNativeObject")
    void setNativeObject(long nativeObject);

    @Direct
    void finalize();
  }
}
