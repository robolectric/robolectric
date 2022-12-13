package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.O;
import static android.os.Build.VERSION_CODES.O_MR1;
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

/** Shadow for {@link Surface} that is backed by native code */
@Implements(value = Surface.class, minSdk = O, shadowPicker = Picker.class, isInAndroidSdk = false)
public class ShadowNativeSurface {
  @Implementation
  protected static long nativeCreateFromSurfaceTexture(SurfaceTexture surfaceTexture)
      throws OutOfResourcesException {
    DefaultNativeRuntimeLoader.injectAndLoad();
    return SurfaceNatives.nativeCreateFromSurfaceTexture(surfaceTexture);
  }

  @Implementation
  protected static long nativeCreateFromSurfaceControl(long surfaceControlNativeObject) {
    DefaultNativeRuntimeLoader.injectAndLoad();
    return SurfaceNatives.nativeCreateFromSurfaceControl(surfaceControlNativeObject);
  }

  @Implementation(minSdk = Q)
  protected static long nativeGetFromSurfaceControl(
      long surfaceObject, long surfaceControlNativeObject) {
    DefaultNativeRuntimeLoader.injectAndLoad();
    return SurfaceNatives.nativeGetFromSurfaceControl(surfaceObject, surfaceControlNativeObject);
  }

  @Implementation(minSdk = S)
  protected static long nativeGetFromBlastBufferQueue(
      long surfaceObject, long blastBufferQueueNativeObject) {
    return SurfaceNatives.nativeGetFromBlastBufferQueue(
        surfaceObject, blastBufferQueueNativeObject);
  }

  @Implementation
  protected static long nativeLockCanvas(long nativeObject, Canvas canvas, Rect dirty)
      throws OutOfResourcesException {
    return SurfaceNatives.nativeLockCanvas(nativeObject, canvas, dirty);
  }

  @Implementation
  protected static void nativeUnlockCanvasAndPost(long nativeObject, Canvas canvas) {
    SurfaceNatives.nativeUnlockCanvasAndPost(nativeObject, canvas);
  }

  @Implementation
  protected static void nativeRelease(long nativeObject) {
    SurfaceNatives.nativeRelease(nativeObject);
  }

  @Implementation
  protected static boolean nativeIsValid(long nativeObject) {
    return SurfaceNatives.nativeIsValid(nativeObject);
  }

  @Implementation
  protected static boolean nativeIsConsumerRunningBehind(long nativeObject) {
    return SurfaceNatives.nativeIsConsumerRunningBehind(nativeObject);
  }

  @Implementation
  protected static long nativeReadFromParcel(long nativeObject, Parcel source) {
    return SurfaceNatives.nativeReadFromParcel(nativeObject, source);
  }

  @Implementation
  protected static void nativeWriteToParcel(long nativeObject, Parcel dest) {
    SurfaceNatives.nativeWriteToParcel(nativeObject, dest);
  }

  @Implementation
  protected static void nativeAllocateBuffers(long nativeObject) {
    SurfaceNatives.nativeAllocateBuffers(nativeObject);
  }

  @Implementation
  protected static int nativeGetWidth(long nativeObject) {
    return SurfaceNatives.nativeGetWidth(nativeObject);
  }

  @Implementation
  protected static int nativeGetHeight(long nativeObject) {
    return SurfaceNatives.nativeGetHeight(nativeObject);
  }

  @Implementation
  protected static long nativeGetNextFrameNumber(long nativeObject) {
    return SurfaceNatives.nativeGetNextFrameNumber(nativeObject);
  }

  @Implementation
  protected static int nativeSetScalingMode(long nativeObject, int scalingMode) {
    return SurfaceNatives.nativeSetScalingMode(nativeObject, scalingMode);
  }

  @Implementation
  protected static int nativeForceScopedDisconnect(long nativeObject) {
    return SurfaceNatives.nativeForceScopedDisconnect(nativeObject);
  }

  @Implementation(minSdk = S)
  protected static int nativeAttachAndQueueBufferWithColorSpace(
      long nativeObject, HardwareBuffer buffer, int colorSpaceId) {
    return SurfaceNatives.nativeAttachAndQueueBufferWithColorSpace(
        nativeObject, buffer, colorSpaceId);
  }

  @Implementation(minSdk = O_MR1)
  protected static int nativeSetSharedBufferModeEnabled(long nativeObject, boolean enabled) {
    return SurfaceNatives.nativeSetSharedBufferModeEnabled(nativeObject, enabled);
  }

  @Implementation(minSdk = O_MR1)
  protected static int nativeSetAutoRefreshEnabled(long nativeObject, boolean enabled) {
    return SurfaceNatives.nativeSetAutoRefreshEnabled(nativeObject, enabled);
  }

  @Implementation(minSdk = S)
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
