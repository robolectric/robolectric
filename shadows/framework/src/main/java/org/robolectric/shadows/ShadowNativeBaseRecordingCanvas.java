package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.O;
import static android.os.Build.VERSION_CODES.O_MR1;
import static android.os.Build.VERSION_CODES.P;
import static android.os.Build.VERSION_CODES.Q;
import static android.os.Build.VERSION_CODES.S;
import static android.os.Build.VERSION_CODES.TIRAMISU;

import android.annotation.ColorLong;
import android.graphics.BaseRecordingCanvas;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.nativeruntime.BaseRecordingCanvasNatives;
import org.robolectric.shadows.ShadowNativeBaseRecordingCanvas.Picker;

/** Shadow for {@link BaseRecordingCanvas} that is backed by native code */
@Implements(
    value = BaseRecordingCanvas.class,
    minSdk = Q,
    shadowPicker = Picker.class,
    isInAndroidSdk = false)
public class ShadowNativeBaseRecordingCanvas extends ShadowNativeCanvas {

  @Implementation
  protected static void nDrawBitmap(
      long nativeCanvas,
      long bitmapHandle,
      float left,
      float top,
      long nativePaintOrZero,
      int canvasDensity,
      int screenDensity,
      int bitmapDensity) {
    BaseRecordingCanvasNatives.nDrawBitmap(
        nativeCanvas,
        bitmapHandle,
        left,
        top,
        nativePaintOrZero,
        canvasDensity,
        screenDensity,
        bitmapDensity);
  }

  @Implementation
  protected static void nDrawBitmap(
      long nativeCanvas,
      long bitmapHandle,
      float srcLeft,
      float srcTop,
      float srcRight,
      float srcBottom,
      float dstLeft,
      float dstTop,
      float dstRight,
      float dstBottom,
      long nativePaintOrZero,
      int screenDensity,
      int bitmapDensity) {
    BaseRecordingCanvasNatives.nDrawBitmap(
        nativeCanvas,
        bitmapHandle,
        srcLeft,
        srcTop,
        srcRight,
        srcBottom,
        dstLeft,
        dstTop,
        dstRight,
        dstBottom,
        nativePaintOrZero,
        screenDensity,
        bitmapDensity);
  }

  @Implementation
  protected static void nDrawBitmap(
      long nativeCanvas,
      int[] colors,
      int offset,
      int stride,
      float x,
      float y,
      int width,
      int height,
      boolean hasAlpha,
      long nativePaintOrZero) {
    BaseRecordingCanvasNatives.nDrawBitmap(
        nativeCanvas, colors, offset, stride, x, y, width, height, hasAlpha, nativePaintOrZero);
  }

  @Implementation
  protected static void nDrawColor(long nativeCanvas, int color, int mode) {
    BaseRecordingCanvasNatives.nDrawColor(nativeCanvas, color, mode);
  }

  @Implementation
  protected static void nDrawColor(
      long nativeCanvas, long nativeColorSpace, @ColorLong long color, int mode) {
    BaseRecordingCanvasNatives.nDrawColor(nativeCanvas, nativeColorSpace, color, mode);
  }

  @Implementation
  protected static void nDrawPaint(long nativeCanvas, long nativePaint) {
    BaseRecordingCanvasNatives.nDrawPaint(nativeCanvas, nativePaint);
  }

  @Implementation
  protected static void nDrawPoint(long canvasHandle, float x, float y, long paintHandle) {
    BaseRecordingCanvasNatives.nDrawPoint(canvasHandle, x, y, paintHandle);
  }

  @Implementation
  protected static void nDrawPoints(
      long canvasHandle, float[] pts, int offset, int count, long paintHandle) {
    BaseRecordingCanvasNatives.nDrawPoints(canvasHandle, pts, offset, count, paintHandle);
  }

  @Implementation
  protected static void nDrawLine(
      long nativeCanvas, float startX, float startY, float stopX, float stopY, long nativePaint) {
    BaseRecordingCanvasNatives.nDrawLine(nativeCanvas, startX, startY, stopX, stopY, nativePaint);
  }

  @Implementation
  protected static void nDrawLines(
      long canvasHandle, float[] pts, int offset, int count, long paintHandle) {
    BaseRecordingCanvasNatives.nDrawLines(canvasHandle, pts, offset, count, paintHandle);
  }

  @Implementation
  protected static void nDrawRect(
      long nativeCanvas, float left, float top, float right, float bottom, long nativePaint) {
    BaseRecordingCanvasNatives.nDrawRect(nativeCanvas, left, top, right, bottom, nativePaint);
  }

  @Implementation
  protected static void nDrawOval(
      long nativeCanvas, float left, float top, float right, float bottom, long nativePaint) {
    BaseRecordingCanvasNatives.nDrawOval(nativeCanvas, left, top, right, bottom, nativePaint);
  }

  @Implementation
  protected static void nDrawCircle(
      long nativeCanvas, float cx, float cy, float radius, long nativePaint) {
    BaseRecordingCanvasNatives.nDrawCircle(nativeCanvas, cx, cy, radius, nativePaint);
  }

  @Implementation
  protected static void nDrawArc(
      long nativeCanvas,
      float left,
      float top,
      float right,
      float bottom,
      float startAngle,
      float sweep,
      boolean useCenter,
      long nativePaint) {
    BaseRecordingCanvasNatives.nDrawArc(
        nativeCanvas, left, top, right, bottom, startAngle, sweep, useCenter, nativePaint);
  }

  @Implementation
  protected static void nDrawRoundRect(
      long nativeCanvas,
      float left,
      float top,
      float right,
      float bottom,
      float rx,
      float ry,
      long nativePaint) {
    BaseRecordingCanvasNatives.nDrawRoundRect(
        nativeCanvas, left, top, right, bottom, rx, ry, nativePaint);
  }

  @Implementation
  protected static void nDrawDoubleRoundRect(
      long nativeCanvas,
      float outerLeft,
      float outerTop,
      float outerRight,
      float outerBottom,
      float outerRx,
      float outerRy,
      float innerLeft,
      float innerTop,
      float innerRight,
      float innerBottom,
      float innerRx,
      float innerRy,
      long nativePaint) {
    BaseRecordingCanvasNatives.nDrawDoubleRoundRect(
        nativeCanvas,
        outerLeft,
        outerTop,
        outerRight,
        outerBottom,
        outerRx,
        outerRy,
        innerLeft,
        innerTop,
        innerRight,
        innerBottom,
        innerRx,
        innerRy,
        nativePaint);
  }

  @Implementation
  protected static void nDrawDoubleRoundRect(
      long nativeCanvas,
      float outerLeft,
      float outerTop,
      float outerRight,
      float outerBottom,
      float[] outerRadii,
      float innerLeft,
      float innerTop,
      float innerRight,
      float innerBottom,
      float[] innerRadii,
      long nativePaint) {
    BaseRecordingCanvasNatives.nDrawDoubleRoundRect(
        nativeCanvas,
        outerLeft,
        outerTop,
        outerRight,
        outerBottom,
        outerRadii,
        innerLeft,
        innerTop,
        innerRight,
        innerBottom,
        innerRadii,
        nativePaint);
  }

  @Implementation
  protected static void nDrawPath(long nativeCanvas, long nativePath, long nativePaint) {
    BaseRecordingCanvasNatives.nDrawPath(nativeCanvas, nativePath, nativePaint);
  }

  @Implementation
  protected static void nDrawRegion(long nativeCanvas, long nativeRegion, long nativePaint) {
    BaseRecordingCanvasNatives.nDrawRegion(nativeCanvas, nativeRegion, nativePaint);
  }

  @Implementation
  protected static void nDrawNinePatch(
      long nativeCanvas,
      long nativeBitmap,
      long ninePatch,
      float dstLeft,
      float dstTop,
      float dstRight,
      float dstBottom,
      long nativePaintOrZero,
      int screenDensity,
      int bitmapDensity) {
    BaseRecordingCanvasNatives.nDrawNinePatch(
        nativeCanvas,
        nativeBitmap,
        ninePatch,
        dstLeft,
        dstTop,
        dstRight,
        dstBottom,
        nativePaintOrZero,
        screenDensity,
        bitmapDensity);
  }

  @Implementation
  protected static void nDrawBitmapMatrix(
      long nativeCanvas, long bitmapHandle, long nativeMatrix, long nativePaint) {
    BaseRecordingCanvasNatives.nDrawBitmapMatrix(
        nativeCanvas, bitmapHandle, nativeMatrix, nativePaint);
  }

  @Implementation
  protected static void nDrawBitmapMesh(
      long nativeCanvas,
      long bitmapHandle,
      int meshWidth,
      int meshHeight,
      float[] verts,
      int vertOffset,
      int[] colors,
      int colorOffset,
      long nativePaint) {
    BaseRecordingCanvasNatives.nDrawBitmapMesh(
        nativeCanvas,
        bitmapHandle,
        meshWidth,
        meshHeight,
        verts,
        vertOffset,
        colors,
        colorOffset,
        nativePaint);
  }

  @Implementation
  protected static void nDrawVertices(
      long nativeCanvas,
      int mode,
      int n,
      float[] verts,
      int vertOffset,
      float[] texs,
      int texOffset,
      int[] colors,
      int colorOffset,
      short[] indices,
      int indexOffset,
      int indexCount,
      long nativePaint) {
    BaseRecordingCanvasNatives.nDrawVertices(
        nativeCanvas,
        mode,
        n,
        verts,
        vertOffset,
        texs,
        texOffset,
        colors,
        colorOffset,
        indices,
        indexOffset,
        indexCount,
        nativePaint);
  }

  @Implementation(minSdk = S)
  protected static void nDrawGlyphs(
      long nativeCanvas,
      int[] glyphIds,
      float[] positions,
      int glyphIdStart,
      int positionStart,
      int glyphCount,
      long nativeFont,
      long nativePaint) {
    BaseRecordingCanvasNatives.nDrawGlyphs(
        nativeCanvas,
        glyphIds,
        positions,
        glyphIdStart,
        positionStart,
        glyphCount,
        nativeFont,
        nativePaint);
  }

  @Implementation
  protected static void nDrawText(
      long nativeCanvas,
      char[] text,
      int index,
      int count,
      float x,
      float y,
      int flags,
      long nativePaint) {
    BaseRecordingCanvasNatives.nDrawText(
        nativeCanvas, text, index, count, x, y, flags, nativePaint);
  }

  @Implementation
  protected static void nDrawText(
      long nativeCanvas,
      String text,
      int start,
      int end,
      float x,
      float y,
      int flags,
      long nativePaint) {
    BaseRecordingCanvasNatives.nDrawText(nativeCanvas, text, start, end, x, y, flags, nativePaint);
  }

  @Implementation(minSdk = O, maxSdk = O_MR1)
  protected static void nDrawText(
      long nativeCanvas,
      char[] text,
      int index,
      int count,
      float x,
      float y,
      int flags,
      long nativePaint,
      long nativeTypeface) {
    BaseRecordingCanvasNatives.nDrawText(
        nativeCanvas, text, index, count, x, y, flags, nativePaint, nativeTypeface);
  }

  @Implementation(minSdk = O, maxSdk = O_MR1)
  protected static void nDrawText(
      long nativeCanvas,
      String text,
      int start,
      int end,
      float x,
      float y,
      int flags,
      long nativePaint,
      long nativeTypeface) {
    BaseRecordingCanvasNatives.nDrawText(
        nativeCanvas, text, start, end, x, y, flags, nativePaint, nativeTypeface);
  }

  @Implementation
  protected static void nDrawTextRun(
      long nativeCanvas,
      String text,
      int start,
      int end,
      int contextStart,
      int contextEnd,
      float x,
      float y,
      boolean isRtl,
      long nativePaint) {
    BaseRecordingCanvasNatives.nDrawTextRun(
        nativeCanvas, text, start, end, contextStart, contextEnd, x, y, isRtl, nativePaint);
  }

  /**
   * The signature of this method is the same from SDK levels O and above, but the last native
   * pointer changed from a Typeface pointer to a MeasuredParagraph pointer in P.
   */
  @Implementation(minSdk = O)
  protected static void nDrawTextRun(
      long nativeCanvas,
      char[] text,
      int start,
      int count,
      int contextStart,
      int contextCount,
      float x,
      float y,
      boolean isRtl,
      long nativePaint,
      long nativeTypefaceOrPrecomputedText) {
    if (RuntimeEnvironment.getApiLevel() >= P) {
      BaseRecordingCanvasNatives.nDrawTextRun(
          nativeCanvas,
          text,
          start,
          count,
          contextStart,
          contextCount,
          x,
          y,
          isRtl,
          nativePaint,
          nativeTypefaceOrPrecomputedText);
    } else {
      BaseRecordingCanvasNatives.nDrawTextRunTypeface(
          nativeCanvas,
          text,
          start,
          count,
          contextStart,
          contextCount,
          x,
          y,
          isRtl,
          nativePaint,
          nativeTypefaceOrPrecomputedText);
    }
  }

  @Implementation(minSdk = O, maxSdk = O_MR1)
  protected static void nDrawTextRun(
      long nativeCanvas,
      String text,
      int start,
      int end,
      int contextStart,
      int contextEnd,
      float x,
      float y,
      boolean isRtl,
      long nativePaint,
      long nativeTypeface) {
    BaseRecordingCanvasNatives.nDrawTextRun(
        nativeCanvas,
        text,
        start,
        end,
        contextStart,
        contextEnd,
        x,
        y,
        isRtl,
        nativePaint,
        nativeTypeface);
  }

  @Implementation
  protected static void nDrawTextOnPath(
      long nativeCanvas,
      char[] text,
      int index,
      int count,
      long nativePath,
      float hOffset,
      float vOffset,
      int bidiFlags,
      long nativePaint) {
    BaseRecordingCanvasNatives.nDrawTextOnPath(
        nativeCanvas, text, index, count, nativePath, hOffset, vOffset, bidiFlags, nativePaint);
  }

  @Implementation
  protected static void nDrawTextOnPath(
      long nativeCanvas,
      String text,
      long nativePath,
      float hOffset,
      float vOffset,
      int flags,
      long nativePaint) {
    BaseRecordingCanvasNatives.nDrawTextOnPath(
        nativeCanvas, text, nativePath, hOffset, vOffset, flags, nativePaint);
  }

  @Implementation(minSdk = O, maxSdk = O_MR1)
  protected static void nDrawTextOnPath(
      long nativeCanvas,
      char[] text,
      int index,
      int count,
      long nativePath,
      float hOffset,
      float vOffset,
      int bidiFlags,
      long nativePaint,
      long nativeTypeface) {
    BaseRecordingCanvasNatives.nDrawTextOnPath(
        nativeCanvas,
        text,
        index,
        count,
        nativePath,
        hOffset,
        vOffset,
        bidiFlags,
        nativePaint,
        nativeTypeface);
  }

  @Implementation(minSdk = O, maxSdk = O_MR1)
  protected static void nDrawTextOnPath(
      long nativeCanvas,
      String text,
      long nativePath,
      float hOffset,
      float vOffset,
      int flags,
      long nativePaint,
      long nativeTypeface) {
    BaseRecordingCanvasNatives.nDrawTextOnPath(
        nativeCanvas, text, nativePath, hOffset, vOffset, flags, nativePaint, nativeTypeface);
  }

  @Implementation(minSdk = S, maxSdk = TIRAMISU)
  protected static void nPunchHole(
      long renderer, float left, float top, float right, float bottom, float rx, float ry) {
    BaseRecordingCanvasNatives.nPunchHole(renderer, left, top, right, bottom, rx, ry);
  }

  @Implementation(minSdk = 10000)
  protected static void nPunchHole(
      long renderer,
      float left,
      float top,
      float right,
      float bottom,
      float rx,
      float ry,
      float alpha) {
    nPunchHole(renderer, left, top, right, bottom, rx, ry);
  }

  /** Shadow picker for {@link BaseRecordingCanvas}. */
  public static final class Picker extends GraphicsShadowPicker<Object> {
    public Picker() {
      super(null, ShadowNativeBaseRecordingCanvas.class);
    }
  }
}
