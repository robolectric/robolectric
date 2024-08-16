package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.P;

import android.graphics.Bitmap;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.nativeruntime.BaseRecordingCanvasNatives;
import org.robolectric.shadows.ShadowNativeRecordingCanvasOP.Picker;

/**
 * Shadow for android.view.RecordingCanvas. This class was renamed to {@link BaseRecordingCanvas} in
 * Q.
 */
@Implements(
    className = "android.view.RecordingCanvas",
    isInAndroidSdk = false,
    minSdk = P,
    maxSdk = P,
    shadowPicker = Picker.class)
public class ShadowNativeRecordingCanvasOP extends ShadowNativeCanvas {
  @Implementation
  protected static void nDrawBitmap(
      long nativeCanvas,
      Bitmap bitmap,
      float left,
      float top,
      long nativePaintOrZero,
      int canvasDensity,
      int screenDensity,
      int bitmapDensity) {
    BaseRecordingCanvasNatives.nDrawBitmap(
        nativeCanvas,
        bitmap.getNativeInstance(),
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
      Bitmap bitmap,
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
        bitmap.getNativeInstance(),
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
      long nativeCanvas, Bitmap bitmap, long nativeMatrix, long nativePaint) {
    BaseRecordingCanvasNatives.nDrawBitmapMatrix(
        nativeCanvas, bitmap.getNativeInstance(), nativeMatrix, nativePaint);
  }

  @Implementation
  protected static void nDrawBitmapMesh(
      long nativeCanvas,
      Bitmap bitmap,
      int meshWidth,
      int meshHeight,
      float[] verts,
      int vertOffset,
      int[] colors,
      int colorOffset,
      long nativePaint) {
    BaseRecordingCanvasNatives.nDrawBitmapMesh(
        nativeCanvas,
        bitmap.getNativeInstance(),
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

  @Implementation
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
      long nativePrecomputedText) {
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
        nativePrecomputedText);
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

  /** Shadow picker for android.view.RecordingCanvasOP. */
  public static final class Picker extends GraphicsShadowPicker<Object> {
    public Picker() {
      super(null, ShadowNativeRecordingCanvasOP.class);
    }
  }
}
