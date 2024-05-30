package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.O;
import static android.os.Build.VERSION_CODES.O_MR1;
import static android.os.Build.VERSION_CODES.P;
import static android.os.Build.VERSION_CODES.Q;
import static android.os.Build.VERSION_CODES.S;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.HardwareBuffer;
import android.os.Parcel;
import android.view.Surface;
import android.view.Surface.OutOfResourcesException;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.nativeruntime.DefaultNativeRuntimeLoader;
import org.robolectric.nativeruntime.SurfaceNatives;
import org.robolectric.shadows.ShadowNativeSurface.Picker;
import org.robolectric.versioning.AndroidVersions.U;

/** Shadow for {@link Surface} that is backed by native code */
@Implements(
    value = Surface.class,
    minSdk = O,
    shadowPicker = Picker.class,
    isInAndroidSdk = false,
    callNativeMethodsByDefault = true)
public class ShadowNativeSurface {
  @Implementation
  protected static long nativeCreateFromSurfaceTexture(SurfaceTexture surfaceTexture)
      throws OutOfResourcesException {
    // SurfaceTexture is not available for host.
    return 0;
  }

  @Implementation(maxSdk = U.SDK_INT)
  protected static long nativeCreateFromSurfaceControl(long surfaceControlNativeObject) {
    DefaultNativeRuntimeLoader.injectAndLoad();
    return SurfaceNatives.nativeCreateFromSurfaceControl(surfaceControlNativeObject);
  }

  @Implementation(minSdk = Q, maxSdk = U.SDK_INT)
  protected static long nativeGetFromSurfaceControl(
      long surfaceObject, long surfaceControlNativeObject) {
    DefaultNativeRuntimeLoader.injectAndLoad();
    return SurfaceNatives.nativeGetFromSurfaceControl(surfaceObject, surfaceControlNativeObject);
  }

  @Implementation(minSdk = P, maxSdk = P)
  protected static long nativeGetFromSurfaceControl(long surfaceControlNativeObject) {
    return nativeGetFromSurfaceControl(0, surfaceControlNativeObject);
  }

  @Implementation(minSdk = S, maxSdk = U.SDK_INT)
  protected static long nativeGetFromBlastBufferQueue(
      long surfaceObject, long blastBufferQueueNativeObject) {
    return SurfaceNatives.nativeGetFromBlastBufferQueue(
        surfaceObject, blastBufferQueueNativeObject);
  }

  @Implementation
  protected static long nativeLockCanvas(long nativeObject, Canvas canvas, Rect dirty)
      throws OutOfResourcesException {
    // Do not call the nativeLockCanvas method. It is not implemented, and calling it can
    // only result in a native crash (unlocking the canvas wipes out this Surface native object).
    throw new UnsupportedOperationException("Not implemented yet");
  }

  @Implementation(maxSdk = U.SDK_INT)
  protected static void nativeUnlockCanvasAndPost(long nativeObject, Canvas canvas) {
    SurfaceNatives.nativeUnlockCanvasAndPost(nativeObject, canvas);
  }

  @Implementation(maxSdk = U.SDK_INT)
  protected static void nativeRelease(long nativeObject) {
    SurfaceNatives.nativeRelease(nativeObject);
  }

  @Implementation(maxSdk = U.SDK_INT)
  protected static boolean nativeIsValid(long nativeObject) {
    return SurfaceNatives.nativeIsValid(nativeObject);
  }

  @Implementation(maxSdk = U.SDK_INT)
  protected static boolean nativeIsConsumerRunningBehind(long nativeObject) {
    return SurfaceNatives.nativeIsConsumerRunningBehind(nativeObject);
  }

  @Implementation(maxSdk = U.SDK_INT)
  protected static long nativeReadFromParcel(long nativeObject, Parcel source) {
    return SurfaceNatives.nativeReadFromParcel(nativeObject, source);
  }

  @Implementation(maxSdk = U.SDK_INT)
  protected static void nativeWriteToParcel(long nativeObject, Parcel dest) {
    SurfaceNatives.nativeWriteToParcel(nativeObject, dest);
  }

  @Implementation(maxSdk = U.SDK_INT)
  protected static void nativeAllocateBuffers(long nativeObject) {
    SurfaceNatives.nativeAllocateBuffers(nativeObject);
  }

  @Implementation(maxSdk = U.SDK_INT)
  protected static int nativeGetWidth(long nativeObject) {
    return SurfaceNatives.nativeGetWidth(nativeObject);
  }

  @Implementation(maxSdk = U.SDK_INT)
  protected static int nativeGetHeight(long nativeObject) {
    return SurfaceNatives.nativeGetHeight(nativeObject);
  }

  @Implementation(maxSdk = U.SDK_INT)
  protected static long nativeGetNextFrameNumber(long nativeObject) {
    return SurfaceNatives.nativeGetNextFrameNumber(nativeObject);
  }

  @Implementation(maxSdk = U.SDK_INT)
  protected static int nativeSetScalingMode(long nativeObject, int scalingMode) {
    return SurfaceNatives.nativeSetScalingMode(nativeObject, scalingMode);
  }

  @Implementation(maxSdk = U.SDK_INT)
  protected static int nativeForceScopedDisconnect(long nativeObject) {
    return SurfaceNatives.nativeForceScopedDisconnect(nativeObject);
  }

  @Implementation(minSdk = S, maxSdk = U.SDK_INT)
  protected static int nativeAttachAndQueueBufferWithColorSpace(
      long nativeObject, HardwareBuffer buffer, int colorSpaceId) {
    return SurfaceNatives.nativeAttachAndQueueBufferWithColorSpace(
        nativeObject, buffer, colorSpaceId);
  }

  @Implementation(minSdk = O_MR1, maxSdk = U.SDK_INT)
  protected static int nativeSetSharedBufferModeEnabled(long nativeObject, boolean enabled) {
    return SurfaceNatives.nativeSetSharedBufferModeEnabled(nativeObject, enabled);
  }

  @Implementation(minSdk = O_MR1, maxSdk = U.SDK_INT)
  protected static int nativeSetAutoRefreshEnabled(long nativeObject, boolean enabled) {
    return SurfaceNatives.nativeSetAutoRefreshEnabled(nativeObject, enabled);
  }

  @Implementation(minSdk = S, maxSdk = U.SDK_INT)
  protected static int nativeSetFrameRate(
      long nativeObject, float frameRate, int compatibility, int changeFrameRateStrategy) {
    return SurfaceNatives.nativeSetFrameRate(
        nativeObject, frameRate, compatibility, changeFrameRateStrategy);
  }

  /** Shadow picker for {@link Surface}. */
  public static final class Picker extends GraphicsShadowPicker<Object> {
    public Picker() {
      super(ShadowSurface.class, ShadowNativeSurface.class);
    }
  }
}
