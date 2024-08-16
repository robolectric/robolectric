package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.O;
import static android.os.Build.VERSION_CODES.P;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.media.Image;
import android.media.Image.Plane;
import android.media.ImageReader;
import android.view.Surface;
import android.view.ThreadedRenderer;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.nativeruntime.DefaultNativeRuntimeLoader;
import org.robolectric.nativeruntime.HardwareRendererNatives;
import org.robolectric.shadows.ShadowNativeThreadedRenderer.Picker;
import org.robolectric.util.reflector.ForType;
import org.robolectric.util.reflector.WithType;

/** Shadow for {@link ThreadedRenderer} that is backed by native code */
@Implements(
    value = ThreadedRenderer.class,
    minSdk = O,
    maxSdk = P,
    shadowPicker = Picker.class,
    looseSignatures = true)
public class ShadowNativeThreadedRenderer {

  // ThreadedRenderer specific functions. These do not exist in HardwareRenderer
  @Implementation(maxSdk = O)
  protected static boolean nSupportsOpenGL() {
    return false;
  }

  // HardwareRenderer methods. These exist in both ThreadedRenderer and HardwareRenderer.
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

  @Implementation
  protected static long nCreateProxy(boolean translucent, long rootRenderNode) {
    return HardwareRendererNatives.nCreateProxy(translucent, rootRenderNode);
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

  @Implementation
  protected static void nSetStopped(long nativeProxy, boolean stopped) {
    HardwareRendererNatives.nSetStopped(nativeProxy, stopped);
  }

  @Implementation
  protected static void nSetOpaque(long nativeProxy, boolean opaque) {
    HardwareRendererNatives.nSetOpaque(nativeProxy, opaque);
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
  protected static Bitmap nCreateHardwareBitmap(long renderNode, int width, int height) {
    return HardwareRendererNatives.nCreateHardwareBitmap(renderNode, width, height);
  }

  @Implementation(minSdk = P, maxSdk = P)
  protected static Object createHardwareBitmap(Object renderNode, Object width, Object height) {
    try (ImageReader imageReader =
        ImageReader.newInstance((int) width, (int) height, PixelFormat.RGBA_8888, 1)) {
      Surface surface = imageReader.getSurface();
      Canvas canvas = surface.lockHardwareCanvas();
      reflector(DisplayListCanvasReflector.class, canvas).drawRenderNode(renderNode);
      surface.unlockCanvasAndPost(canvas);
      Image nativeImage = imageReader.acquireNextImage();
      Plane[] planes = nativeImage.getPlanes();
      Bitmap destBitmap = Bitmap.createBitmap((int) width, (int) height, Config.ARGB_8888);
      destBitmap.copyPixelsFromBuffer(planes[0].getBuffer());
      surface.release();
      // Return an immutable copy of the Bitmap, which is what this API expects.
      return destBitmap.copy(Config.HARDWARE, false);
    }
  }

  @ForType(className = "android.view.DisplayListCanvas")
  interface DisplayListCanvasReflector {
    void drawRenderNode(@WithType("android.view.RenderNode") Object renderNode);
  }

  /** Shadow picker for {@link ThreadedRenderer}. */
  public static final class Picker extends GraphicsShadowPicker<Object> {
    public Picker() {
      super(ShadowThreadedRenderer.class, ShadowNativeThreadedRenderer.class);
    }
  }
}
