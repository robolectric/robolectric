package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.Q;
import static android.os.Build.VERSION_CODES.S;

import android.graphics.RecordingCanvas;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.nativeruntime.DefaultNativeRuntimeLoader;
import org.robolectric.nativeruntime.RecordingCanvasNatives;
import org.robolectric.shadows.ShadowNativeRecordingCanvas.Picker;

/** Shadow for {@link RecordingCanvas} that is backed by native code */
@Implements(value = RecordingCanvas.class, minSdk = Q, shadowPicker = Picker.class)
public class ShadowNativeRecordingCanvas extends ShadowNativeBaseRecordingCanvas {

  @Implementation
  protected static long nCreateDisplayListCanvas(long node, int width, int height) {
    DefaultNativeRuntimeLoader.injectAndLoad();
    return RecordingCanvasNatives.nCreateDisplayListCanvas(node, width, height);
  }

  @Implementation
  protected static void nResetDisplayListCanvas(long canvas, long node, int width, int height) {
    RecordingCanvasNatives.nResetDisplayListCanvas(canvas, node, width, height);
  }

  @Implementation
  protected static int nGetMaximumTextureWidth() {
    return RecordingCanvasNatives.nGetMaximumTextureWidth();
  }

  @Implementation
  protected static int nGetMaximumTextureHeight() {
    return RecordingCanvasNatives.nGetMaximumTextureHeight();
  }

  @Implementation(minSdk = S)
  protected static void nEnableZ(long renderer, boolean enableZ) {
    RecordingCanvasNatives.nEnableZ(renderer, enableZ);
  }

  @Implementation(minSdk = S)
  protected static void nFinishRecording(long renderer, long renderNode) {
    RecordingCanvasNatives.nFinishRecording(renderer, renderNode);
  }

  @Implementation
  protected static void nDrawRenderNode(long renderer, long renderNode) {
    RecordingCanvasNatives.nDrawRenderNode(renderer, renderNode);
  }

  @Implementation
  protected static void nDrawTextureLayer(long renderer, long layer) {
    RecordingCanvasNatives.nDrawTextureLayer(renderer, layer);
  }

  @Implementation
  protected static void nDrawCircle(
      long renderer, long propCx, long propCy, long propRadius, long propPaint) {
    RecordingCanvasNatives.nDrawCircle(renderer, propCx, propCy, propRadius, propPaint);
  }

  @Implementation(minSdk = S)
  protected static void nDrawRipple(
      long renderer,
      long propCx,
      long propCy,
      long propRadius,
      long propPaint,
      long propProgress,
      long turbulencePhase,
      int color,
      long runtimeEffect) {
    RecordingCanvasNatives.nDrawRipple(
        renderer,
        propCx,
        propCy,
        propRadius,
        propPaint,
        propProgress,
        turbulencePhase,
        color,
        runtimeEffect);
  }

  @Implementation
  protected static void nDrawRoundRect(
      long renderer,
      long propLeft,
      long propTop,
      long propRight,
      long propBottom,
      long propRx,
      long propRy,
      long propPaint) {
    RecordingCanvasNatives.nDrawRoundRect(
        renderer, propLeft, propTop, propRight, propBottom, propRx, propRy, propPaint);
  }

  @Implementation
  protected static void nDrawWebViewFunctor(long canvas, int functor) {
    RecordingCanvasNatives.nDrawWebViewFunctor(canvas, functor);
  }

  /** Shadow picker for {@link RecordingCanvas}. */
  public static final class Picker extends GraphicsShadowPicker<Object> {
    public Picker() {
      super(ShadowRecordingCanvas.class, ShadowNativeRecordingCanvas.class);
    }
  }
}
