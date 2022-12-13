package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.O;
import static android.os.Build.VERSION_CODES.P;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.nativeruntime.DefaultNativeRuntimeLoader;
import org.robolectric.nativeruntime.RecordingCanvasNatives;
import org.robolectric.shadows.ShadowNativeDisplayListCanvas.Picker;

/** Shadow for {@link android.view.DisplayListCanvas} that is backed by native code */
@Implements(
    className = "android.view.DisplayListCanvas",
    minSdk = O,
    maxSdk = P,
    shadowPicker = Picker.class)
public class ShadowNativeDisplayListCanvas extends ShadowNativeRecordingCanvas {

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

  @Implementation
  protected static void nDrawRenderNode(long renderer, long renderNode) {
    RecordingCanvasNatives.nDrawRenderNode(renderer, renderNode);
  }

  @Implementation
  protected static void nDrawCircle(
      long renderer, long propCx, long propCy, long propRadius, long propPaint) {
    RecordingCanvasNatives.nDrawCircle(renderer, propCx, propCy, propRadius, propPaint);
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

  /** Shadow picker for {@link android.view.DisplayListCanvas}. */
  public static final class Picker extends GraphicsShadowPicker<Object> {
    public Picker() {
      super(ShadowDisplayListCanvas.class, ShadowNativeDisplayListCanvas.class);
    }
  }
}
