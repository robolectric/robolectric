package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.Q;
import static android.os.Build.VERSION_CODES.R;
import static android.os.Build.VERSION_CODES.S;
import static android.os.Build.VERSION_CODES.S_V2;
import static android.os.Build.VERSION_CODES.TIRAMISU;

import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.HardwareRenderer;
import android.graphics.HardwareRenderer.ASurfaceTransactionCallback;
import android.graphics.HardwareRenderer.FrameCompleteCallback;
import android.graphics.HardwareRenderer.FrameDrawingCallback;
import android.graphics.HardwareRenderer.PictureCapturedCallback;
import android.graphics.HardwareRenderer.PrepareSurfaceControlForWebviewCallback;
import android.graphics.PixelFormat;
import android.graphics.RenderNode;
import android.media.Image;
import android.media.Image.Plane;
import android.media.ImageReader;
import android.view.Surface;
import java.io.FileDescriptor;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.nativeruntime.DefaultNativeRuntimeLoader;
import org.robolectric.nativeruntime.HardwareRendererNatives;
import org.robolectric.shadows.ShadowNativeHardwareRenderer.Picker;
import org.robolectric.util.reflector.ForType;
import org.robolectric.versioning.AndroidVersions.U;

/** Shadow for {@link HardwareRenderer} that is backed by native code */
@Implements(
    value = HardwareRenderer.class,
    minSdk = Q,
    looseSignatures = true,
    shadowPicker = Picker.class,
    callNativeMethodsByDefault = true)
public class ShadowNativeHardwareRenderer {
  @Implementation(maxSdk = U.SDK_INT)
  protected static void disableVsync() {
    HardwareRendererNatives.disableVsync();
  }

  @Implementation(maxSdk = U.SDK_INT)
  protected static void preload() {
    HardwareRendererNatives.preload();
  }

  @Implementation(minSdk = S, maxSdk = U.SDK_INT)
  protected static boolean isWebViewOverlaysEnabled() {
    return HardwareRendererNatives.isWebViewOverlaysEnabled();
  }

  @Implementation(maxSdk = U.SDK_INT)
  protected static void setupShadersDiskCache(String cacheFile, String skiaCacheFile) {
    HardwareRendererNatives.setupShadersDiskCache(cacheFile, skiaCacheFile);
  }

  @Implementation(maxSdk = U.SDK_INT)
  protected static void nRotateProcessStatsBuffer() {
    HardwareRendererNatives.nRotateProcessStatsBuffer();
  }

  @Implementation(maxSdk = U.SDK_INT)
  protected static void nSetProcessStatsBuffer(int fd) {
    HardwareRendererNatives.nSetProcessStatsBuffer(fd);
  }

  @Implementation(maxSdk = U.SDK_INT)
  protected static int nGetRenderThreadTid(long nativeProxy) {
    return HardwareRendererNatives.nGetRenderThreadTid(nativeProxy);
  }

  @Implementation(maxSdk = U.SDK_INT)
  protected static long nCreateRootRenderNode() {
    DefaultNativeRuntimeLoader.injectAndLoad();
    return HardwareRendererNatives.nCreateRootRenderNode();
  }

  @Implementation(minSdk = S, maxSdk = U.SDK_INT)
  protected static long nCreateProxy(boolean translucent, long rootRenderNode) {
    return HardwareRendererNatives.nCreateProxy(translucent, rootRenderNode);
  }

  @Implementation(minSdk = R, maxSdk = R)
  protected static long nCreateProxy(
      boolean translucent, boolean isWideGamut, long rootRenderNode) {
    return nCreateProxy(true, rootRenderNode);
  }

  @Implementation(minSdk = Q, maxSdk = Q)
  protected static Object nCreateProxy(Object translucent, Object rootRenderNode) {
    return nCreateProxy((boolean) translucent, (long) rootRenderNode);
  }

  @Implementation(maxSdk = U.SDK_INT)
  protected static void nDeleteProxy(long nativeProxy) {
    HardwareRendererNatives.nDeleteProxy(nativeProxy);
  }

  @Implementation(maxSdk = U.SDK_INT)
  protected static boolean nLoadSystemProperties(long nativeProxy) {
    return HardwareRendererNatives.nLoadSystemProperties(nativeProxy);
  }

  @Implementation(maxSdk = U.SDK_INT)
  protected static void nSetName(long nativeProxy, String name) {
    HardwareRendererNatives.nSetName(nativeProxy, name);
  }

  @Implementation(minSdk = Q, maxSdk = Q)
  protected static void nSetSurface(long nativeProxy, Surface window) {
    HardwareRendererNatives.nSetSurface(nativeProxy, window, false);
  }

  @Implementation(minSdk = R, maxSdk = U.SDK_INT)
  protected static void nSetSurface(long nativeProxy, Surface window, boolean discardBuffer) {
    HardwareRendererNatives.nSetSurface(nativeProxy, window, discardBuffer);
  }

  @Implementation(minSdk = S, maxSdk = U.SDK_INT)
  protected static void nSetSurfaceControl(long nativeProxy, long nativeSurfaceControl) {
    HardwareRendererNatives.nSetSurfaceControl(nativeProxy, nativeSurfaceControl);
  }

  @Implementation(maxSdk = U.SDK_INT)
  protected static boolean nPause(long nativeProxy) {
    return HardwareRendererNatives.nPause(nativeProxy);
  }

  @Implementation(maxSdk = U.SDK_INT)
  protected static void nSetStopped(long nativeProxy, boolean stopped) {
    HardwareRendererNatives.nSetStopped(nativeProxy, stopped);
  }

  @Implementation(maxSdk = U.SDK_INT)
  protected static void nSetLightGeometry(
      long nativeProxy, float lightX, float lightY, float lightZ, float lightRadius) {
    HardwareRendererNatives.nSetLightGeometry(nativeProxy, lightX, lightY, lightZ, lightRadius);
  }

  @Implementation(maxSdk = U.SDK_INT)
  protected static void nSetLightAlpha(
      long nativeProxy, float ambientShadowAlpha, float spotShadowAlpha) {
    HardwareRendererNatives.nSetLightAlpha(nativeProxy, ambientShadowAlpha, spotShadowAlpha);
  }

  @Implementation(maxSdk = U.SDK_INT)
  protected static void nSetOpaque(long nativeProxy, boolean opaque) {
    HardwareRendererNatives.nSetOpaque(nativeProxy, opaque);
  }

  @Implementation(minSdk = S, maxSdk = U.SDK_INT)
  protected static Object nSetColorMode(long nativeProxy, int colorMode) {
    HardwareRendererNatives.nSetColorMode(nativeProxy, colorMode);
    return null;
  }

  @Implementation(minSdk = S, maxSdk = U.SDK_INT)
  protected static void nSetSdrWhitePoint(long nativeProxy, float whitePoint) {
    HardwareRendererNatives.nSetSdrWhitePoint(nativeProxy, whitePoint);
  }

  @Implementation(minSdk = S, maxSdk = U.SDK_INT)
  protected static void nSetIsHighEndGfx(boolean isHighEndGfx) {
    HardwareRendererNatives.nSetIsHighEndGfx(isHighEndGfx);
  }

  @Implementation(maxSdk = U.SDK_INT)
  protected static int nSyncAndDrawFrame(long nativeProxy, long[] frameInfo, int size) {
    return HardwareRendererNatives.nSyncAndDrawFrame(nativeProxy, frameInfo, size);
  }

  @Implementation(maxSdk = U.SDK_INT)
  protected static void nDestroy(long nativeProxy, long rootRenderNode) {
    HardwareRendererNatives.nDestroy(nativeProxy, rootRenderNode);
  }

  @Implementation(maxSdk = U.SDK_INT)
  protected static void nRegisterAnimatingRenderNode(long rootRenderNode, long animatingNode) {
    HardwareRendererNatives.nRegisterAnimatingRenderNode(rootRenderNode, animatingNode);
  }

  @Implementation(maxSdk = U.SDK_INT)
  protected static void nRegisterVectorDrawableAnimator(long rootRenderNode, long animator) {
    HardwareRendererNatives.nRegisterVectorDrawableAnimator(rootRenderNode, animator);
  }

  @Implementation(maxSdk = U.SDK_INT)
  protected static long nCreateTextureLayer(long nativeProxy) {
    return HardwareRendererNatives.nCreateTextureLayer(nativeProxy);
  }

  @Implementation(maxSdk = U.SDK_INT)
  protected static void nBuildLayer(long nativeProxy, long node) {
    HardwareRendererNatives.nBuildLayer(nativeProxy, node);
  }

  @Implementation(maxSdk = U.SDK_INT)
  protected static boolean nCopyLayerInto(long nativeProxy, long layer, long bitmapHandle) {
    return HardwareRendererNatives.nCopyLayerInto(nativeProxy, layer, bitmapHandle);
  }

  @Implementation(maxSdk = U.SDK_INT)
  protected static void nPushLayerUpdate(long nativeProxy, long layer) {
    HardwareRendererNatives.nPushLayerUpdate(nativeProxy, layer);
  }

  @Implementation(maxSdk = U.SDK_INT)
  protected static void nCancelLayerUpdate(long nativeProxy, long layer) {
    HardwareRendererNatives.nCancelLayerUpdate(nativeProxy, layer);
  }

  @Implementation(maxSdk = U.SDK_INT)
  protected static void nDetachSurfaceTexture(long nativeProxy, long layer) {
    HardwareRendererNatives.nDetachSurfaceTexture(nativeProxy, layer);
  }

  @Implementation(maxSdk = U.SDK_INT)
  protected static void nDestroyHardwareResources(long nativeProxy) {
    HardwareRendererNatives.nDestroyHardwareResources(nativeProxy);
  }

  @Implementation(maxSdk = U.SDK_INT)
  protected static void nTrimMemory(int level) {
    HardwareRendererNatives.nTrimMemory(level);
  }

  @Implementation(maxSdk = U.SDK_INT)
  protected static void nOverrideProperty(String name, String value) {
    HardwareRendererNatives.nOverrideProperty(name, value);
  }

  @Implementation(maxSdk = U.SDK_INT)
  protected static void nFence(long nativeProxy) {
    HardwareRendererNatives.nFence(nativeProxy);
  }

  @Implementation(maxSdk = U.SDK_INT)
  protected static void nStopDrawing(long nativeProxy) {
    HardwareRendererNatives.nStopDrawing(nativeProxy);
  }

  @Implementation(maxSdk = U.SDK_INT)
  protected static void nNotifyFramePending(long nativeProxy) {
    HardwareRendererNatives.nNotifyFramePending(nativeProxy);
  }

  @Implementation(maxSdk = U.SDK_INT)
  protected static void nDumpProfileInfo(long nativeProxy, FileDescriptor fd, int dumpFlags) {
    HardwareRendererNatives.nDumpProfileInfo(nativeProxy, fd, dumpFlags);
  }

  @Implementation(maxSdk = U.SDK_INT)
  protected static void nAddRenderNode(long nativeProxy, long rootRenderNode, boolean placeFront) {
    HardwareRendererNatives.nAddRenderNode(nativeProxy, rootRenderNode, placeFront);
  }

  @Implementation(maxSdk = U.SDK_INT)
  protected static void nRemoveRenderNode(long nativeProxy, long rootRenderNode) {
    HardwareRendererNatives.nRemoveRenderNode(nativeProxy, rootRenderNode);
  }

  @Implementation(maxSdk = U.SDK_INT)
  protected static void nDrawRenderNode(long nativeProxy, long rootRenderNode) {
    HardwareRendererNatives.nDrawRenderNode(nativeProxy, rootRenderNode);
  }

  @Implementation(maxSdk = U.SDK_INT)
  protected static void nSetContentDrawBounds(
      long nativeProxy, int left, int top, int right, int bottom) {
    HardwareRendererNatives.nSetContentDrawBounds(nativeProxy, left, top, right, bottom);
  }

  @Implementation(maxSdk = U.SDK_INT)
  protected static void nSetPictureCaptureCallback(
      long nativeProxy, PictureCapturedCallback callback) {
    HardwareRendererNatives.nSetPictureCaptureCallback(nativeProxy, callback);
  }

  @Implementation(minSdk = S, maxSdk = U.SDK_INT)
  protected static void nSetASurfaceTransactionCallback(Object nativeProxy, Object callback) {
    // Requires looseSignatures because ASurfaceTransactionCallback is S+.
    HardwareRendererNatives.nSetASurfaceTransactionCallback(
        (long) nativeProxy, (ASurfaceTransactionCallback) callback);
  }

  @Implementation(minSdk = S, maxSdk = U.SDK_INT)
  protected static void nSetPrepareSurfaceControlForWebviewCallback(
      Object nativeProxy, Object callback) {
    // Need to use loose signatures here as PrepareSurfaceControlForWebviewCallback is S+.
    HardwareRendererNatives.nSetPrepareSurfaceControlForWebviewCallback(
        (long) nativeProxy, (PrepareSurfaceControlForWebviewCallback) callback);
  }

  @Implementation(maxSdk = U.SDK_INT)
  protected static void nSetFrameCallback(long nativeProxy, FrameDrawingCallback callback) {
    HardwareRendererNatives.nSetFrameCallback(nativeProxy, callback);
  }

  @Implementation(maxSdk = U.SDK_INT)
  protected static void nSetFrameCompleteCallback(
      long nativeProxy, FrameCompleteCallback callback) {
    HardwareRendererNatives.nSetFrameCompleteCallback(nativeProxy, callback);
  }

  @Implementation(minSdk = R, maxSdk = U.SDK_INT)
  protected static void nAddObserver(long nativeProxy, long nativeObserver) {
    HardwareRendererNatives.nAddObserver(nativeProxy, nativeObserver);
  }

  @Implementation(minSdk = R, maxSdk = U.SDK_INT)
  protected static void nRemoveObserver(long nativeProxy, long nativeObserver) {
    HardwareRendererNatives.nRemoveObserver(nativeProxy, nativeObserver);
  }

  @Implementation(maxSdk = TIRAMISU)
  protected static int nCopySurfaceInto(
      Surface surface, int srcLeft, int srcTop, int srcRight, int srcBottom, long bitmapHandle) {
    return HardwareRendererNatives.nCopySurfaceInto(
        surface, srcLeft, srcTop, srcRight, srcBottom, bitmapHandle);
  }

  /**
   * This currently always return null in host graphics. This has `maxSdk=R` because, in S and
   * above, {@link #createHardwareBitmap(RenderNode, int, int)} is shadowed.
   */
  @Implementation(maxSdk = R)
  protected static Bitmap nCreateHardwareBitmap(long renderNode, int width, int height) {
    return HardwareRendererNatives.nCreateHardwareBitmap(renderNode, width, height);
  }

  @Implementation(maxSdk = U.SDK_INT)
  protected static void nSetHighContrastText(boolean enabled) {
    HardwareRendererNatives.nSetHighContrastText(enabled);
  }

  @Implementation(minSdk = Q, maxSdk = S_V2)
  protected static void nHackySetRTAnimationsEnabled(boolean enabled) {
    DefaultNativeRuntimeLoader.injectAndLoad();
    HardwareRendererNatives.nHackySetRTAnimationsEnabled(enabled);
  }

  @Implementation(minSdk = TIRAMISU, maxSdk = U.SDK_INT)
  protected static void nSetRtAnimationsEnabled(boolean enabled) {
    nHackySetRTAnimationsEnabled(enabled);
  }

  @Implementation(maxSdk = U.SDK_INT)
  protected static void nSetDebuggingEnabled(boolean enabled) {
    HardwareRendererNatives.nSetDebuggingEnabled(enabled);
  }

  @Implementation(maxSdk = U.SDK_INT)
  protected static void nSetIsolatedProcess(boolean enabled) {
    HardwareRendererNatives.nSetIsolatedProcess(enabled);
  }

  @Implementation(maxSdk = U.SDK_INT)
  protected static void nSetContextPriority(int priority) {
    HardwareRendererNatives.nSetContextPriority(priority);
  }

  @Implementation(maxSdk = U.SDK_INT)
  protected static void nAllocateBuffers(long nativeProxy) {
    HardwareRendererNatives.nAllocateBuffers(nativeProxy);
  }

  @Implementation(maxSdk = U.SDK_INT)
  protected static void nSetForceDark(long nativeProxy, boolean enabled) {
    HardwareRendererNatives.nSetForceDark(nativeProxy, enabled);
  }

  // TODO(brettchabot): add support for V nSetForceDark(long, int)

  @Implementation(minSdk = S, maxSdk = U.SDK_INT)
  protected static void nSetDisplayDensityDpi(int densityDpi) {
    HardwareRendererNatives.nSetDisplayDensityDpi(densityDpi);
  }

  @Implementation(minSdk = S, maxSdk = TIRAMISU)
  protected static void nInitDisplayInfo(
      int width,
      int height,
      float refreshRate,
      int wideColorDataspace,
      long appVsyncOffsetNanos,
      long presentationDeadlineNanos) {
    HardwareRendererNatives.nInitDisplayInfo(
        width,
        height,
        refreshRate,
        wideColorDataspace,
        appVsyncOffsetNanos,
        presentationDeadlineNanos);
  }

  @Implementation(minSdk = U.SDK_INT, maxSdk = U.SDK_INT)
  protected static void nInitDisplayInfo(
      int width,
      int height,
      float refreshRate,
      int wideColorDataspace,
      long appVsyncOffsetNanos,
      long presentationDeadlineNanos,
      boolean supportsFp16ForHdr,
      boolean nInitDisplayInfo) {
    nInitDisplayInfo(
        width,
        height,
        refreshRate,
        wideColorDataspace,
        appVsyncOffsetNanos,
        presentationDeadlineNanos);
  }

  @Implementation(minSdk = Q)
  protected static Bitmap createHardwareBitmap(RenderNode node, int width, int height) {
    // The native counterpart of this method,
    // android_view_ThreadedRenderer_createHardwareBitmapFromRenderNode,
    // returns null in host graphics due to not supporting AImageReader and ANativeWindow.
    // We can do a Java shim here.

    // This logic is based on
    // https://android.googlesource.com/platform/frameworks/base/+/refs/heads/android12-hostruntime-dev/libs/hwui/jni/android_graphics_HardwareRenderer.cpp#709
    try (ImageReader imageReader =
        ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 1)) {
      HardwareRenderer renderer = new HardwareRenderer();
      Surface surface = imageReader.getSurface();
      renderer.setSurface(surface);
      renderer.setContentRoot(node);
      renderer.setLightSourceGeometry(0, 0, 0, 0);
      renderer.setLightSourceAlpha(0, 0);
      renderer.createRenderRequest().syncAndDraw();
      Image nativeImage = imageReader.acquireNextImage();
      Plane[] planes = nativeImage.getPlanes();
      Bitmap destBitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);
      destBitmap.copyPixelsFromBuffer(planes[0].getBuffer());
      surface.release();
      // Return an immutable copy of the Bitmap, which is what this API expects.
      return destBitmap.copy(Config.HARDWARE, false);
    }
  }

  @Implementation(maxSdk = R)
  protected static void nSetWideGamut(long nativeProxy, boolean isWideColorGamut) {
    nSetColorMode(
        nativeProxy,
        isWideColorGamut
            ? ActivityInfo.COLOR_MODE_WIDE_COLOR_GAMUT
            : ActivityInfo.COLOR_MODE_DEFAULT);
  }

  @ForType(HardwareRenderer.class)
  interface HardwareRendererReflector {
    void setWideGamut(boolean isWideColorGamut);

    void setSurface(Surface surface);
  }

  /** Shadow picker for {@link HardwareRenderer}. */
  public static final class Picker extends GraphicsShadowPicker<Object> {
    public Picker() {
      super(ShadowHardwareRenderer.class, ShadowNativeHardwareRenderer.class);
    }
  }
}
