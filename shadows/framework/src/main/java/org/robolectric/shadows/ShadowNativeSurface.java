package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.O;
import static android.os.Build.VERSION_CODES.O_MR1;
import static android.os.Build.VERSION_CODES.P;
import static android.os.Build.VERSION_CODES.Q;
import static android.os.Build.VERSION_CODES.S;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.graphics.Canvas;
import android.graphics.HardwareRenderer;
import android.graphics.Rect;
import android.graphics.RenderNode;
import android.graphics.SurfaceTexture;
import android.hardware.HardwareBuffer;
import android.os.Parcel;
import android.view.Surface;
import android.view.Surface.OutOfResourcesException;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.nativeruntime.DefaultNativeRuntimeLoader;
import org.robolectric.nativeruntime.SurfaceNatives;
import org.robolectric.shadows.ShadowNativeHardwareRenderer.HardwareRendererReflector;
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

  /**
   * Shadow for {@link Surface$HwuiContext} for Q and below that invokes HardwareRenderer methods.
   * In Q and below, HwuiContext had its own native methods.
   */
  @Implements(
      className = "android.view.Surface$HwuiContext",
      minSdk = Q,
      maxSdk = Q,
      isInAndroidSdk = false,
      shadowPicker = ShadowNativeHwuiContext.Picker.class)
  public static class ShadowNativeHwuiContext {
    @RealObject Object realHwuiContext;

    // This object is a HardwareRenderer in Q, but is a ThreadedRenderer in O and P.
    private Object hardwareRenderer;

    @Implementation
    protected void __constructor__(Surface surface, boolean isWideColorGamut) {
      // Modeled after the HwuiContext constructor in R:
      // https://cs.android.com/android/platform/superproject/+/android11-dev:frameworks/base/core/java/android/view/Surface.java
      reflector(HwuiContextReflector.class, realHwuiContext)
          .__constructor__(surface, isWideColorGamut);
      HardwareRenderer hardwareRenderer = new HardwareRenderer();
      RenderNode renderNode =
          reflector(HwuiContextReflector.class, realHwuiContext).getRenderNode();
      hardwareRenderer.setContentRoot(renderNode);
      hardwareRenderer.setSurface(surface);
      reflector(HardwareRendererReflector.class, hardwareRenderer).setWideGamut(isWideColorGamut);
      hardwareRenderer.setLightSourceAlpha(0.0f, 0.0f);
      hardwareRenderer.setLightSourceGeometry(0.0f, 0.0f, 0.0f, 0.0f);
      this.hardwareRenderer = hardwareRenderer;
    }

    @Implementation
    protected void unlockAndPost(Canvas canvas) {
      RenderNode renderNode =
          reflector(HwuiContextReflector.class, realHwuiContext).getRenderNode();
      renderNode.endRecording();
      reflector(HwuiContextReflector.class, realHwuiContext).setCanvas(null);
      ((HardwareRenderer) hardwareRenderer)
          .createRenderRequest()
          .setVsyncTime(System.nanoTime())
          .syncAndDraw();
    }

    @Implementation
    protected void updateSurface() {
      Surface surface = reflector(HwuiContextReflector.class, realHwuiContext).getOuterSurface();
      reflector(HardwareRendererReflector.class, hardwareRenderer).setSurface(surface);
    }

    @Implementation
    protected void destroy() {
      ((HardwareRenderer) hardwareRenderer).destroy();
    }

    @ForType(className = "android.view.Surface$HwuiContext")
    interface HwuiContextReflector {
      @Direct
      void __constructor__(Surface surface, boolean isWideColorGamut);

      @Accessor("mRenderNode")
      RenderNode getRenderNode();

      @Accessor("mCanvas")
      void setCanvas(Canvas canvas);

      @Accessor("this$0")
      Surface getOuterSurface();
    }

    /** Shadow picker for HwuiContext. */
    public static final class Picker extends GraphicsShadowPicker<Object> {
      public Picker() {
        super(null, ShadowNativeHwuiContext.class);
      }
    }
  }

  @Implementation(minSdk = Q, maxSdk = Q)
  protected static long nHwuiCreate(long rootNode, long surface, boolean isWideColorGamut) {
    return 0; // no-op
  }

  /** Shadow picker for {@link Surface}. */
  public static final class Picker extends GraphicsShadowPicker<Object> {
    public Picker() {
      super(ShadowSurface.class, ShadowNativeSurface.class);
    }
  }
}
