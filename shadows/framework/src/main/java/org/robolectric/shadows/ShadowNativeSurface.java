package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.O;
import static android.os.Build.VERSION_CODES.O_MR1;
import static android.os.Build.VERSION_CODES.P;
import static android.os.Build.VERSION_CODES.Q;
import static android.os.Build.VERSION_CODES.S;
import static android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE;
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
import java.util.concurrent.atomic.AtomicReference;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.nativeruntime.DefaultNativeRuntimeLoader;
import org.robolectric.nativeruntime.HardwareRendererNatives;
import org.robolectric.nativeruntime.RecordingCanvasNatives;
import org.robolectric.nativeruntime.RenderNodeNatives;
import org.robolectric.nativeruntime.SurfaceNatives;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.shadows.ShadowNativeSurface.Picker;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.Direct;
import org.robolectric.util.reflector.ForType;

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
  // the ImageReader.OnImageAvailableListener callback and drawing.
  private final AtomicReference<ImageReader> containerImageReader = new AtomicReference<>(null);

  @Implementation
  protected static long nativeCreateFromSurfaceTexture(SurfaceTexture surfaceTexture)
      throws OutOfResourcesException {
    // SurfaceTexture is not available for host.
    return 0;
  }

  @Implementation(maxSdk = UPSIDE_DOWN_CAKE)
  protected static long nativeCreateFromSurfaceControl(long surfaceControlNativeObject) {
    DefaultNativeRuntimeLoader.injectAndLoad();
    return SurfaceNatives.nativeCreateFromSurfaceControl(surfaceControlNativeObject);
  }

  @Implementation(minSdk = Q)
  protected static long nativeGetFromSurfaceControl(
      long surfaceObject, long surfaceControlNativeObject) {
    return 0;
  }

  @Implementation(minSdk = P, maxSdk = P)
  protected static long nativeGetFromSurfaceControl(long surfaceControlNativeObject) {
    return nativeGetFromSurfaceControl(0, surfaceControlNativeObject);
  }

  @Implementation(minSdk = S, maxSdk = UPSIDE_DOWN_CAKE)
  protected static long nativeGetFromBlastBufferQueue(
      long surfaceObject, long blastBufferQueueNativeObject) {
    return SurfaceNatives.nativeGetFromBlastBufferQueue(
        surfaceObject, blastBufferQueueNativeObject);
  }

  @Implementation(maxSdk = UPSIDE_DOWN_CAKE)
  protected static long nativeLockCanvas(long nativeObject, Canvas canvas, Rect dirty)
      throws OutOfResourcesException {
    return SurfaceNatives.nativeLockCanvas(nativeObject, canvas, dirty);
  }

  @Implementation(maxSdk = UPSIDE_DOWN_CAKE)
  protected static void nativeUnlockCanvasAndPost(long nativeObject, Canvas canvas) {
    SurfaceNatives.nativeUnlockCanvasAndPost(nativeObject, canvas);
  }

  @Implementation(maxSdk = UPSIDE_DOWN_CAKE)
  protected static void nativeRelease(long nativeObject) {
    SurfaceNatives.nativeRelease(nativeObject);
  }

  @Implementation(maxSdk = UPSIDE_DOWN_CAKE)
  protected static boolean nativeIsValid(long nativeObject) {
    return SurfaceNatives.nativeIsValid(nativeObject);
  }

  @Implementation(maxSdk = UPSIDE_DOWN_CAKE)
  protected static boolean nativeIsConsumerRunningBehind(long nativeObject) {
    return SurfaceNatives.nativeIsConsumerRunningBehind(nativeObject);
  }

  @Implementation
  protected static long nativeReadFromParcel(long nativeObject, Parcel source) {
    return 0;
  }

  @Implementation
  protected static void nativeWriteToParcel(long nativeObject, Parcel dest) {}

  @Implementation(maxSdk = UPSIDE_DOWN_CAKE)
  protected static void nativeAllocateBuffers(long nativeObject) {
    SurfaceNatives.nativeAllocateBuffers(nativeObject);
  }

  @Implementation(maxSdk = UPSIDE_DOWN_CAKE)
  protected static int nativeGetWidth(long nativeObject) {
    return SurfaceNatives.nativeGetWidth(nativeObject);
  }

  @Implementation(maxSdk = UPSIDE_DOWN_CAKE)
  protected static int nativeGetHeight(long nativeObject) {
    return SurfaceNatives.nativeGetHeight(nativeObject);
  }

  @Implementation(maxSdk = UPSIDE_DOWN_CAKE)
  protected static long nativeGetNextFrameNumber(long nativeObject) {
    return SurfaceNatives.nativeGetNextFrameNumber(nativeObject);
  }

  @Implementation(maxSdk = UPSIDE_DOWN_CAKE)
  protected static int nativeSetScalingMode(long nativeObject, int scalingMode) {
    return SurfaceNatives.nativeSetScalingMode(nativeObject, scalingMode);
  }

  @Implementation(maxSdk = UPSIDE_DOWN_CAKE)
  protected static int nativeForceScopedDisconnect(long nativeObject) {
    return SurfaceNatives.nativeForceScopedDisconnect(nativeObject);
  }

  @Implementation(minSdk = S, maxSdk = UPSIDE_DOWN_CAKE)
  protected static int nativeAttachAndQueueBufferWithColorSpace(
      long nativeObject, HardwareBuffer buffer, int colorSpaceId) {
    return SurfaceNatives.nativeAttachAndQueueBufferWithColorSpace(
        nativeObject, buffer, colorSpaceId);
  }

  @Implementation(minSdk = O_MR1, maxSdk = UPSIDE_DOWN_CAKE)
  protected static int nativeSetSharedBufferModeEnabled(long nativeObject, boolean enabled) {
    return SurfaceNatives.nativeSetSharedBufferModeEnabled(nativeObject, enabled);
  }

  @Implementation(minSdk = O_MR1, maxSdk = UPSIDE_DOWN_CAKE)
  protected static int nativeSetAutoRefreshEnabled(long nativeObject, boolean enabled) {
    return SurfaceNatives.nativeSetAutoRefreshEnabled(nativeObject, enabled);
  }

  @Implementation(minSdk = S, maxSdk = UPSIDE_DOWN_CAKE)
  protected static int nativeSetFrameRate(
      long nativeObject, float frameRate, int compatibility, int changeFrameRateStrategy) {
    return SurfaceNatives.nativeSetFrameRate(
        nativeObject, frameRate, compatibility, changeFrameRateStrategy);
  }

  @Implementation
  protected void transferFrom(Surface other) {
    reflector(SurfaceReflector.class, realSurface).transferFrom(other);
    ShadowNativeSurface shadowNativeSurface = Shadow.extract(other);
    this.containerImageReader.set(shadowNativeSurface.containerImageReader.get());
  }

  ImageReader getContainerImageReader() {
    return containerImageReader.get();
  }

  void setContainerImageReader(ImageReader realImageReader) {
    this.containerImageReader.set(realImageReader);
  }

  @Implementation
  protected void unlockCanvasAndPost(Canvas canvas) {
    reflector(SurfaceReflector.class, realSurface).unlockCanvasAndPost(canvas);
    ImageReader imageReader = containerImageReader.get();
    if (imageReader != null) {
      ShadowNativeImageReader.triggerOnImageAvailableCallbacks(imageReader);
    }
  }

  @Implementation
  protected void release() {
    reflector(SurfaceReflector.class, realSurface).release();
    // imageReader.close will also call Surface.release. So need to guard here against infinite
    // recursion
    ImageReader imageReader = containerImageReader.getAndSet(null);
    if (imageReader != null) {
      imageReader.close();
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

    @Direct
    void transferFrom(Surface other);

    @Direct
    void release();
  }

  /** Shadow picker for {@link Surface}. */
  public static final class Picker extends GraphicsShadowPicker<Object> {
    public Picker() {
      super(ShadowSurface.class, ShadowNativeSurface.class);
    }
  }
}
