package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.Q;
import static android.os.Build.VERSION_CODES.R;
import static android.os.Build.VERSION_CODES.S;
import static android.os.Build.VERSION_CODES.TIRAMISU;

import android.graphics.Bitmap;
import android.graphics.HardwareRenderer;
import android.graphics.HardwareRenderer.ASurfaceTransactionCallback;
import android.graphics.HardwareRenderer.FrameCompleteCallback;
import android.graphics.HardwareRenderer.FrameDrawingCallback;
import android.graphics.HardwareRenderer.PictureCapturedCallback;
import android.graphics.HardwareRenderer.PrepareSurfaceControlForWebviewCallback;
import android.view.Surface;
import java.io.FileDescriptor;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.nativeruntime.DefaultNativeRuntimeLoader;
import org.robolectric.nativeruntime.HardwareRendererNatives;
import org.robolectric.shadows.ShadowNativeHardwareRenderer.Picker;

/** Shadow for {@link HardwareRenderer} that is backed by native code */
@Implements(
    value = HardwareRenderer.class,
    minSdk = Q,
    looseSignatures = true,
    shadowPicker = Picker.class)
public class ShadowNativeHardwareRenderer {
  @Implementation
  protected static void disableVsync() {
    HardwareRendererNatives.disableVsync();
  }

  @Implementation
  protected static void preload() {
    HardwareRendererNatives.preload();
  }

  @Implementation(minSdk = S)
  protected static boolean isWebViewOverlaysEnabled() {
    return HardwareRendererNatives.isWebViewOverlaysEnabled();
  }

  @Implementation
  protected static void setupShadersDiskCache(String cacheFile, String skiaCacheFile) {
    HardwareRendererNatives.setupShadersDiskCache(cacheFile, skiaCacheFile);
  }

  @Implementation
  protected static void nRotateProcessStatsBuffer() {
    HardwareRendererNatives.nRotateProcessStatsBuffer();
  }

  @Implementation
  protected static void nSetProcessStatsBuffer(int fd) {
    HardwareRendererNatives.nSetProcessStatsBuffer(fd);
  }

  @Implementation
  protected static int nGetRenderThreadTid(long nativeProxy) {
    return HardwareRendererNatives.nGetRenderThreadTid(nativeProxy);
  }

  @Implementation
  protected static long nCreateRootRenderNode() {
    DefaultNativeRuntimeLoader.injectAndLoad();
    return HardwareRendererNatives.nCreateRootRenderNode();
  }

  @Implementation(minSdk = S)
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

  @Implementation
  protected static void nDeleteProxy(long nativeProxy) {
    HardwareRendererNatives.nDeleteProxy(nativeProxy);
  }

  @Implementation
  protected static boolean nLoadSystemProperties(long nativeProxy) {
    return HardwareRendererNatives.nLoadSystemProperties(nativeProxy);
  }

  @Implementation
  protected static void nSetName(long nativeProxy, String name) {
    HardwareRendererNatives.nSetName(nativeProxy, name);
  }

  @Implementation(minSdk = R)
  protected static void nSetSurface(long nativeProxy, Surface window, boolean discardBuffer) {
    HardwareRendererNatives.nSetSurface(nativeProxy, window, discardBuffer);
  }

  @Implementation(minSdk = S)
  protected static void nSetSurfaceControl(long nativeProxy, long nativeSurfaceControl) {
    HardwareRendererNatives.nSetSurfaceControl(nativeProxy, nativeSurfaceControl);
  }

  @Implementation
  protected static boolean nPause(long nativeProxy) {
    return HardwareRendererNatives.nPause(nativeProxy);
  }

  @Implementation
  protected static void nSetStopped(long nativeProxy, boolean stopped) {
    HardwareRendererNatives.nSetStopped(nativeProxy, stopped);
  }

  @Implementation
  protected static void nSetLightGeometry(
      long nativeProxy, float lightX, float lightY, float lightZ, float lightRadius) {
    HardwareRendererNatives.nSetLightGeometry(nativeProxy, lightX, lightY, lightZ, lightRadius);
  }

  @Implementation
  protected static void nSetLightAlpha(
      long nativeProxy, float ambientShadowAlpha, float spotShadowAlpha) {
    HardwareRendererNatives.nSetLightAlpha(nativeProxy, ambientShadowAlpha, spotShadowAlpha);
  }

  @Implementation
  protected static void nSetOpaque(long nativeProxy, boolean opaque) {
    HardwareRendererNatives.nSetOpaque(nativeProxy, opaque);
  }

  @Implementation(minSdk = S)
  protected static void nSetColorMode(long nativeProxy, int colorMode) {
    HardwareRendererNatives.nSetColorMode(nativeProxy, colorMode);
  }

  @Implementation(minSdk = S)
  protected static void nSetSdrWhitePoint(long nativeProxy, float whitePoint) {
    HardwareRendererNatives.nSetSdrWhitePoint(nativeProxy, whitePoint);
  }

  @Implementation(minSdk = S)
  protected static void nSetIsHighEndGfx(boolean isHighEndGfx) {
    HardwareRendererNatives.nSetIsHighEndGfx(isHighEndGfx);
  }

  @Implementation
  protected static int nSyncAndDrawFrame(long nativeProxy, long[] frameInfo, int size) {
    return HardwareRendererNatives.nSyncAndDrawFrame(nativeProxy, frameInfo, size);
  }

  @Implementation
  protected static void nDestroy(long nativeProxy, long rootRenderNode) {
    HardwareRendererNatives.nDestroy(nativeProxy, rootRenderNode);
  }

  @Implementation
  protected static void nRegisterAnimatingRenderNode(long rootRenderNode, long animatingNode) {
    HardwareRendererNatives.nRegisterAnimatingRenderNode(rootRenderNode, animatingNode);
  }

  @Implementation
  protected static void nRegisterVectorDrawableAnimator(long rootRenderNode, long animator) {
    HardwareRendererNatives.nRegisterVectorDrawableAnimator(rootRenderNode, animator);
  }

  @Implementation
  protected static long nCreateTextureLayer(long nativeProxy) {
    return HardwareRendererNatives.nCreateTextureLayer(nativeProxy);
  }

  @Implementation
  protected static void nBuildLayer(long nativeProxy, long node) {
    HardwareRendererNatives.nBuildLayer(nativeProxy, node);
  }

  @Implementation
  protected static boolean nCopyLayerInto(long nativeProxy, long layer, long bitmapHandle) {
    return HardwareRendererNatives.nCopyLayerInto(nativeProxy, layer, bitmapHandle);
  }

  @Implementation
  protected static void nPushLayerUpdate(long nativeProxy, long layer) {
    HardwareRendererNatives.nPushLayerUpdate(nativeProxy, layer);
  }

  @Implementation
  protected static void nCancelLayerUpdate(long nativeProxy, long layer) {
    HardwareRendererNatives.nCancelLayerUpdate(nativeProxy, layer);
  }

  @Implementation
  protected static void nDetachSurfaceTexture(long nativeProxy, long layer) {
    HardwareRendererNatives.nDetachSurfaceTexture(nativeProxy, layer);
  }

  @Implementation
  protected static void nDestroyHardwareResources(long nativeProxy) {
    HardwareRendererNatives.nDestroyHardwareResources(nativeProxy);
  }

  @Implementation
  protected static void nTrimMemory(int level) {
    HardwareRendererNatives.nTrimMemory(level);
  }

  @Implementation
  protected static void nOverrideProperty(String name, String value) {
    HardwareRendererNatives.nOverrideProperty(name, value);
  }

  @Implementation
  protected static void nFence(long nativeProxy) {
    HardwareRendererNatives.nFence(nativeProxy);
  }

  @Implementation
  protected static void nStopDrawing(long nativeProxy) {
    HardwareRendererNatives.nStopDrawing(nativeProxy);
  }

  @Implementation
  protected static void nNotifyFramePending(long nativeProxy) {
    HardwareRendererNatives.nNotifyFramePending(nativeProxy);
  }

  @Implementation
  protected static void nDumpProfileInfo(long nativeProxy, FileDescriptor fd, int dumpFlags) {
    HardwareRendererNatives.nDumpProfileInfo(nativeProxy, fd, dumpFlags);
  }

  @Implementation
  protected static void nAddRenderNode(long nativeProxy, long rootRenderNode, boolean placeFront) {
    HardwareRendererNatives.nAddRenderNode(nativeProxy, rootRenderNode, placeFront);
  }

  @Implementation
  protected static void nRemoveRenderNode(long nativeProxy, long rootRenderNode) {
    HardwareRendererNatives.nRemoveRenderNode(nativeProxy, rootRenderNode);
  }

  @Implementation
  protected static void nDrawRenderNode(long nativeProxy, long rootRenderNode) {
    HardwareRendererNatives.nDrawRenderNode(nativeProxy, rootRenderNode);
  }

  @Implementation
  protected static void nSetContentDrawBounds(
      long nativeProxy, int left, int top, int right, int bottom) {
    HardwareRendererNatives.nSetContentDrawBounds(nativeProxy, left, top, right, bottom);
  }

  @Implementation
  protected static void nSetPictureCaptureCallback(
      long nativeProxy, PictureCapturedCallback callback) {
    HardwareRendererNatives.nSetPictureCaptureCallback(nativeProxy, callback);
  }

  @Implementation(minSdk = S)
  protected static void nSetASurfaceTransactionCallback(Object nativeProxy, Object callback) {
    // Requires looseSignatures because ASurfaceTransactionCallback is S+.
    HardwareRendererNatives.nSetASurfaceTransactionCallback(
        (long) nativeProxy, (ASurfaceTransactionCallback) callback);
  }

  @Implementation(minSdk = S)
  protected static void nSetPrepareSurfaceControlForWebviewCallback(
      Object nativeProxy, Object callback) {
    // Need to use loose signatures here as PrepareSurfaceControlForWebviewCallback is S+.
    HardwareRendererNatives.nSetPrepareSurfaceControlForWebviewCallback(
        (long) nativeProxy, (PrepareSurfaceControlForWebviewCallback) callback);
  }

  @Implementation
  protected static void nSetFrameCallback(long nativeProxy, FrameDrawingCallback callback) {
    HardwareRendererNatives.nSetFrameCallback(nativeProxy, callback);
  }

  @Implementation
  protected static void nSetFrameCompleteCallback(
      long nativeProxy, FrameCompleteCallback callback) {
    HardwareRendererNatives.nSetFrameCompleteCallback(nativeProxy, callback);
  }

  @Implementation(minSdk = R)
  protected static void nAddObserver(long nativeProxy, long nativeObserver) {
    HardwareRendererNatives.nAddObserver(nativeProxy, nativeObserver);
  }

  @Implementation(minSdk = R)
  protected static void nRemoveObserver(long nativeProxy, long nativeObserver) {
    HardwareRendererNatives.nRemoveObserver(nativeProxy, nativeObserver);
  }

  @Implementation(maxSdk = TIRAMISU)
  protected static int nCopySurfaceInto(
      Surface surface, int srcLeft, int srcTop, int srcRight, int srcBottom, long bitmapHandle) {
    return HardwareRendererNatives.nCopySurfaceInto(
        surface, srcLeft, srcTop, srcRight, srcBottom, bitmapHandle);
  }

  @Implementation
  protected static Bitmap nCreateHardwareBitmap(long renderNode, int width, int height) {
    return HardwareRendererNatives.nCreateHardwareBitmap(renderNode, width, height);
  }

  @Implementation
  protected static void nSetHighContrastText(boolean enabled) {
    HardwareRendererNatives.nSetHighContrastText(enabled);
  }

  @Implementation(minSdk = Q, maxSdk = S)
  protected static void nHackySetRTAnimationsEnabled(boolean enabled) {
    DefaultNativeRuntimeLoader.injectAndLoad();
    HardwareRendererNatives.nHackySetRTAnimationsEnabled(enabled);
  }

  @Implementation
  protected static void nSetDebuggingEnabled(boolean enabled) {
    HardwareRendererNatives.nSetDebuggingEnabled(enabled);
  }

  @Implementation
  protected static void nSetIsolatedProcess(boolean enabled) {
    HardwareRendererNatives.nSetIsolatedProcess(enabled);
  }

  @Implementation
  protected static void nSetContextPriority(int priority) {
    HardwareRendererNatives.nSetContextPriority(priority);
  }

  @Implementation
  protected static void nAllocateBuffers(long nativeProxy) {
    HardwareRendererNatives.nAllocateBuffers(nativeProxy);
  }

  @Implementation
  protected static void nSetForceDark(long nativeProxy, boolean enabled) {
    HardwareRendererNatives.nSetForceDark(nativeProxy, enabled);
  }

  @Implementation(minSdk = S)
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

  @Implementation(minSdk = 10000)
  protected static void nInitDisplayInfo(
      int width,
      int height,
      float refreshRate,
      int wideColorDataspace,
      long appVsyncOffsetNanos,
      long presentationDeadlineNanos,
      boolean supportsFp16ForHdr) {
    nInitDisplayInfo(
        width,
        height,
        refreshRate,
        wideColorDataspace,
        appVsyncOffsetNanos,
        presentationDeadlineNanos);
  }

  /** Shadow picker for {@link HardwareRenderer}. */
  public static final class Picker extends GraphicsShadowPicker<Object> {
    public Picker() {
      super(ShadowHardwareRenderer.class, ShadowNativeHardwareRenderer.class);
    }
  }
}
