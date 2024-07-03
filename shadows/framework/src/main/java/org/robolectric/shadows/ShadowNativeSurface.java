package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.O;
import static android.os.Build.VERSION_CODES.O_MR1;
import static android.os.Build.VERSION_CODES.P;
import static android.os.Build.VERSION_CODES.Q;
import static android.os.Build.VERSION_CODES.S;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.HardwareBuffer;
import android.media.ImageReader;
import android.os.Parcel;
import android.view.Surface;
import android.view.Surface.OutOfResourcesException;
import android.view.animation.AnimationUtils;
import java.util.concurrent.TimeUnit;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.nativeruntime.DefaultNativeRuntimeLoader;
import org.robolectric.nativeruntime.HardwareRendererNatives;
import org.robolectric.nativeruntime.RecordingCanvasNatives;
import org.robolectric.nativeruntime.RenderNodeNatives;
import org.robolectric.nativeruntime.SurfaceNatives;
import org.robolectric.shadows.ShadowNativeSurface.Picker;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.Direct;
import org.robolectric.util.reflector.ForType;
import org.robolectric.versioning.AndroidVersions.U;

/** Shadow for {@link Surface} that is backed by native code */
@Implements(
    value = Surface.class,
    minSdk = O,
    shadowPicker = Picker.class,
    isInAndroidSdk = false,
    callNativeMethodsByDefault = true)
public class ShadowNativeSurface {

  @RealObject private Surface realSurface;

  // This field is populated when a Surface is created by an ImageReader. It is used to support
  // the ImageReader.OnImageAvailableListener callback.
  private ImageReader containerImageReader;

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

  @Implementation(maxSdk = U.SDK_INT)
  protected static long nativeLockCanvas(long nativeObject, Canvas canvas, Rect dirty)
      throws OutOfResourcesException {
    return SurfaceNatives.nativeLockCanvas(nativeObject, canvas, dirty);
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

  void setContainerImageReader(ImageReader realImageReader) {
    this.containerImageReader = realImageReader;
  }

  @Implementation
  protected void unlockCanvasAndPost(Canvas canvas) {
    reflector(SurfaceReflector.class, realSurface).unlockCanvasAndPost(canvas);
    if (this.containerImageReader != null) {
      ShadowNativeImageReader.triggerOnImageAvailableCallbacks(this.containerImageReader);
    }
  }

  @Implementation(minSdk = P, maxSdk = Q)
  protected static long nHwuiCreate(long contentRootNode, long surface, boolean isWideColorGamut) {
    // Modeled after the HwuiContext constructor in R:
    // https://cs.android.com/android/platform/superproject/+/android11-dev:frameworks/base/core/java/android/view/Surface.java;l=1005

    // Set up the root render node.
    long rootRenderNodePtr = HardwareRendererNatives.nCreateRootRenderNode();
    RenderNodeNatives.nSetClipToBounds(rootRenderNodePtr, false);

    // Set up the HardwareRenderer
    long renderer = HardwareRendererNatives.nCreateProxy(false, rootRenderNodePtr);

    // Set up light-related properties.
    HardwareRendererNatives.nSetLightAlpha(renderer, 0, 0);
    HardwareRendererNatives.nSetLightGeometry(renderer, 0, 0, 0, 0);

    // Draw the content render node onto the root render node.
    long recordingCanvas = RecordingCanvasNatives.nCreateDisplayListCanvas(rootRenderNodePtr, 0, 0);
    RecordingCanvasNatives.nDrawRenderNode(recordingCanvas, contentRootNode);
    RecordingCanvasNatives.nFinishRecording(recordingCanvas, rootRenderNodePtr);

    // Set the surface.
    HardwareRendererNatives.nSetSurfacePtr(renderer, surface);
    return renderer;
  }

  @Implementation(minSdk = O, maxSdk = O_MR1)
  protected static long nHwuiCreate(long rootNode, long surface) {
    return nHwuiCreate(rootNode, surface, false);
  }

  @Implementation(minSdk = O, maxSdk = Q)
  protected static void nHwuiSetSurface(long renderer, long surface) {
    if (surface != 0) {
      HardwareRendererNatives.nSetSurfacePtr(renderer, surface);
    }
  }

  @Implementation(minSdk = O, maxSdk = Q)
  protected static void nHwuiDraw(long renderer) {
    // FrameInfo changed packages from android.view.FrameInfo in P to android.graphics.FrameInfo in
    // Q, so it's easier to just construct a long[] array with the frame data.
    final long vsync = TimeUnit.MILLISECONDS.toNanos(AnimationUtils.currentAnimationTimeMillis());
    final long[] frameInfo = new long[9];
    frameInfo[0] = 1 << 2;
    frameInfo[1] = vsync;
    frameInfo[2] = vsync;
    frameInfo[3] = Long.MAX_VALUE;
    frameInfo[4] = 0;
    HardwareRendererNatives.nSyncAndDrawFrame(renderer, frameInfo, 9);
  }

  @Implementation(minSdk = O, maxSdk = Q)
  protected static void nHwuiDestroy(long renderer) {
    HardwareRendererNatives.nDeleteProxy(renderer);
  }

  @ForType(Surface.class)
  interface SurfaceReflector {
    @Direct
    void unlockCanvasAndPost(Canvas canvas);

    @Accessor("mNativeObject")
    long getNativeObject();
  }

  /** Shadow picker for {@link Surface}. */
  public static final class Picker extends GraphicsShadowPicker<Object> {
    public Picker() {
      super(ShadowSurface.class, ShadowNativeSurface.class);
    }
  }
}
