package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.O;
import static android.os.Build.VERSION_CODES.O_MR1;
import static android.os.Build.VERSION_CODES.P;
import static android.os.Build.VERSION_CODES.Q;
import static android.os.Build.VERSION_CODES.S;
import static android.os.Build.VERSION_CODES.TIRAMISU;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.annotation.ColorLong;
import android.graphics.BaseCanvas;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.Path;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.nativeruntime.BaseCanvasNatives;
import org.robolectric.shadows.ShadowNativeBaseCanvas.Picker;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.ForType;

/** Shadow for {@link BaseCanvas} that is backed by native code */
@Implements(
    value = BaseCanvas.class,
    minSdk = O,
    shadowPicker = Picker.class,
    isInAndroidSdk = false)
public class ShadowNativeBaseCanvas extends ShadowCanvas {

  @RealObject BaseCanvas realBaseCanvas;

  @Implementation(minSdk = Q)
  protected static void nDrawBitmap(
      long nativeCanvas,
      long bitmapHandle,
      float left,
      float top,
      long nativePaintOrZero,
      int canvasDensity,
      int screenDensity,
      int bitmapDensity) {
    BaseCanvasNatives.nDrawBitmap(
        nativeCanvas,
        bitmapHandle,
        left,
        top,
        nativePaintOrZero,
        canvasDensity,
        screenDensity,
        bitmapDensity);
  }

  @Implementation(minSdk = Q)
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
    BaseCanvasNatives.nDrawBitmap(
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

  @Implementation(minSdk = O)
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
    BaseCanvasNatives.nDrawBitmap(
        nativeCanvas, colors, offset, stride, x, y, width, height, hasAlpha, nativePaintOrZero);
  }

  @Implementation(minSdk = O, maxSdk = P)
  protected static void nDrawBitmap(
      long nativeCanvas,
      Bitmap bitmap,
      float left,
      float top,
      long nativePaintOrZero,
      int canvasDensity,
      int screenDensity,
      int bitmapDensity) {
    BaseCanvasNatives.nDrawBitmap(
        nativeCanvas,
        bitmap.getNativeInstance(),
        left,
        top,
        nativePaintOrZero,
        canvasDensity,
        screenDensity,
        bitmapDensity);
  }

  @Implementation(minSdk = O, maxSdk = P)
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
    BaseCanvasNatives.nDrawBitmap(
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

  @Implementation(minSdk = O)
  protected static void nDrawColor(long nativeCanvas, int color, int mode) {
    BaseCanvasNatives.nDrawColor(nativeCanvas, color, mode);
  }

  @Implementation(minSdk = Q)
  protected static void nDrawColor(
      long nativeCanvas, long nativeColorSpace, @ColorLong long color, int mode) {
    BaseCanvasNatives.nDrawColor(nativeCanvas, nativeColorSpace, color, mode);
  }

  @Implementation(minSdk = O)
  protected static void nDrawPaint(long nativeCanvas, long nativePaint) {
    BaseCanvasNatives.nDrawPaint(nativeCanvas, nativePaint);
  }

  @Implementation(minSdk = O)
  protected static void nDrawPoint(long canvasHandle, float x, float y, long paintHandle) {
    BaseCanvasNatives.nDrawPoint(canvasHandle, x, y, paintHandle);
  }

  @Implementation(minSdk = O)
  protected static void nDrawPoints(
      long canvasHandle, float[] pts, int offset, int count, long paintHandle) {
    BaseCanvasNatives.nDrawPoints(canvasHandle, pts, offset, count, paintHandle);
  }

  @Implementation(minSdk = O)
  protected static void nDrawLine(
      long nativeCanvas, float startX, float startY, float stopX, float stopY, long nativePaint) {
    BaseCanvasNatives.nDrawLine(nativeCanvas, startX, startY, stopX, stopY, nativePaint);
  }

  @Implementation(minSdk = O)
  protected static void nDrawLines(
      long canvasHandle, float[] pts, int offset, int count, long paintHandle) {
    BaseCanvasNatives.nDrawLines(canvasHandle, pts, offset, count, paintHandle);
  }

  @Implementation(minSdk = O)
  protected static void nDrawRect(
      long nativeCanvas, float left, float top, float right, float bottom, long nativePaint) {
    BaseCanvasNatives.nDrawRect(nativeCanvas, left, top, right, bottom, nativePaint);
  }

  @Implementation(minSdk = O)
  protected static void nDrawOval(
      long nativeCanvas, float left, float top, float right, float bottom, long nativePaint) {
    BaseCanvasNatives.nDrawOval(nativeCanvas, left, top, right, bottom, nativePaint);
  }

  @Implementation(minSdk = O)
  protected static void nDrawCircle(
      long nativeCanvas, float cx, float cy, float radius, long nativePaint) {
    BaseCanvasNatives.nDrawCircle(nativeCanvas, cx, cy, radius, nativePaint);
  }

  @Implementation(minSdk = O)
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
    BaseCanvasNatives.nDrawArc(
        nativeCanvas, left, top, right, bottom, startAngle, sweep, useCenter, nativePaint);
  }

  @Implementation(minSdk = O)
  protected static void nDrawRoundRect(
      long nativeCanvas,
      float left,
      float top,
      float right,
      float bottom,
      float rx,
      float ry,
      long nativePaint) {
    BaseCanvasNatives.nDrawRoundRect(nativeCanvas, left, top, right, bottom, rx, ry, nativePaint);
  }

  @Implementation(minSdk = Q)
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
    BaseCanvasNatives.nDrawDoubleRoundRect(
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

  @Implementation(minSdk = Q)
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
    BaseCanvasNatives.nDrawDoubleRoundRect(
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

  @Implementation(minSdk = O)
  protected static void nDrawPath(long nativeCanvas, long nativePath, long nativePaint) {
    BaseCanvasNatives.nDrawPath(nativeCanvas, nativePath, nativePaint);
  }

  @Implementation(minSdk = O)
  protected static void nDrawRegion(long nativeCanvas, long nativeRegion, long nativePaint) {
    BaseCanvasNatives.nDrawRegion(nativeCanvas, nativeRegion, nativePaint);
  }

  @Implementation(minSdk = O)
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
    BaseCanvasNatives.nDrawNinePatch(
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

  @Implementation(minSdk = Q)
  protected static void nDrawBitmapMatrix(
      long nativeCanvas, long bitmapHandle, long nativeMatrix, long nativePaint) {
    BaseCanvasNatives.nDrawBitmapMatrix(nativeCanvas, bitmapHandle, nativeMatrix, nativePaint);
  }

  @Implementation(minSdk = O, maxSdk = P)
  protected static void nDrawBitmapMatrix(
      long nativeCanvas, Bitmap bitmap, long nativeMatrix, long nativePaint) {
    BaseCanvasNatives.nDrawBitmapMatrix(
        nativeCanvas, bitmap.getNativeInstance(), nativeMatrix, nativePaint);
  }

  @Implementation(minSdk = Q)
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
    BaseCanvasNatives.nDrawBitmapMesh(
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

  @Implementation(minSdk = O, maxSdk = P)
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
    BaseCanvasNatives.nDrawBitmapMesh(
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

  @Implementation(minSdk = O)
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
    BaseCanvasNatives.nDrawVertices(
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
    BaseCanvasNatives.nDrawGlyphs(
        nativeCanvas,
        glyphIds,
        positions,
        glyphIdStart,
        positionStart,
        glyphCount,
        nativeFont,
        nativePaint);
  }

  @Implementation(minSdk = P)
  protected static void nDrawText(
      long nativeCanvas,
      char[] text,
      int index,
      int count,
      float x,
      float y,
      int flags,
      long nativePaint) {
    // This native code calls Typeface::resolveDefault, which requires Typeface clinit to have run.
    ShadowNativeTypeface.ensureInitialized();
    BaseCanvasNatives.nDrawText(nativeCanvas, text, index, count, x, y, flags, nativePaint);
  }

  @Implementation(minSdk = P)
  protected static void nDrawText(
      long nativeCanvas,
      String text,
      int start,
      int end,
      float x,
      float y,
      int flags,
      long nativePaint) {
    // This native code calls Typeface::resolveDefault, which requires Typeface clinit to have run.
    ShadowNativeTypeface.ensureInitialized();
    BaseCanvasNatives.nDrawText(nativeCanvas, text, start, end, x, y, flags, nativePaint);
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
    // This native code calls Typeface::resolveDefault, which requires Typeface clinit to have run.
    ShadowNativeTypeface.ensureInitialized();
    BaseCanvasNatives.nDrawText(
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
    // This native code calls Typeface::resolveDefault, which requires Typeface clinit to have run.
    ShadowNativeTypeface.ensureInitialized();
    BaseCanvasNatives.nDrawText(
        nativeCanvas, text, start, end, x, y, flags, nativePaint, nativeTypeface);
  }

  @Implementation(minSdk = P)
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
    // This native code calls Typeface::resolveDefault, which requires Typeface clinit to have run.
    ShadowNativeTypeface.ensureInitialized();
    BaseCanvasNatives.nDrawTextRun(
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
    // This native code calls Typeface::resolveDefault, which requires Typeface clinit to have run.
    ShadowNativeTypeface.ensureInitialized();
    if (RuntimeEnvironment.getApiLevel() >= P) {
      BaseCanvasNatives.nDrawTextRun(
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
      BaseCanvasNatives.nDrawTextRunTypeface(
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
    // This native code calls Typeface::resolveDefault, which requires Typeface clinit to have run.
    ShadowNativeTypeface.ensureInitialized();
    BaseCanvasNatives.nDrawTextRun(
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

  @Implementation(minSdk = P)
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
    // This native code calls Typeface::resolveDefault, which requires Typeface clinit to have run.
    ShadowNativeTypeface.ensureInitialized();
    BaseCanvasNatives.nDrawTextOnPath(
        nativeCanvas, text, index, count, nativePath, hOffset, vOffset, bidiFlags, nativePaint);
  }

  @Implementation(minSdk = P)
  protected static void nDrawTextOnPath(
      long nativeCanvas,
      String text,
      long nativePath,
      float hOffset,
      float vOffset,
      int flags,
      long nativePaint) {
    // This native code calls Typeface::resolveDefault, which requires Typeface clinit to have run.
    ShadowNativeTypeface.ensureInitialized();
    BaseCanvasNatives.nDrawTextOnPath(
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
    // This native code calls Typeface::resolveDefault, which requires Typeface clinit to have run.
    ShadowNativeTypeface.ensureInitialized();
    BaseCanvasNatives.nDrawTextOnPath(
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
    // This native code calls Typeface::resolveDefault, which requires Typeface clinit to have run.
    ShadowNativeTypeface.ensureInitialized();
    BaseCanvasNatives.nDrawTextOnPath(
        nativeCanvas, text, nativePath, hOffset, vOffset, flags, nativePaint, nativeTypeface);
  }

  @Implementation(minSdk = S, maxSdk = TIRAMISU)
  protected static void nPunchHole(
      long renderer, float left, float top, float right, float bottom, float rx, float ry) {
    BaseCanvasNatives.nPunchHole(renderer, left, top, right, bottom, rx, ry);
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

  long getNativeCanvas() {
    return reflector(BaseCanvasReflector.class, realBaseCanvas).getNativeCanvas();
  }

  @Override
  public void appendDescription(String s) {
    throw new UnsupportedOperationException(
        "Legacy ShadowCanvas description APIs are not supported");
  }

  @Override
  public String getDescription() {
    throw new UnsupportedOperationException(
        "Legacy ShadowCanvas description APIs are not supported");
  }

  @Override
  public int getPathPaintHistoryCount() {
    throw new UnsupportedOperationException(
        "Legacy ShadowCanvas description APIs are not supported");
  }

  @Override
  public int getCirclePaintHistoryCount() {
    throw new UnsupportedOperationException(
        "Legacy ShadowCanvas description APIs are not supported");
  }

  @Override
  public int getArcPaintHistoryCount() {
    throw new UnsupportedOperationException(
        "Legacy ShadowCanvas description APIs are not supported");
  }

  @Override
  public boolean hasDrawnPath() {
    throw new UnsupportedOperationException(
        "Legacy ShadowCanvas description APIs are not supported");
  }

  @Override
  public boolean hasDrawnCircle() {
    throw new UnsupportedOperationException(
        "Legacy ShadowCanvas description APIs are not supported");
  }

  @Override
  public Paint getDrawnPathPaint(int i) {
    throw new UnsupportedOperationException(
        "Legacy ShadowCanvas description APIs are not supported");
  }

  @Override
  public Path getDrawnPath(int i) {
    throw new UnsupportedOperationException(
        "Legacy ShadowCanvas description APIs are not supported");
  }

  @Override
  public CirclePaintHistoryEvent getDrawnCircle(int i) {
    throw new UnsupportedOperationException(
        "Legacy ShadowCanvas description APIs are not supported");
  }

  @Override
  public ArcPaintHistoryEvent getDrawnArc(int i) {
    throw new UnsupportedOperationException(
        "Legacy ShadowCanvas description APIs are not supported");
  }

  @Override
  public void resetCanvasHistory() {
    throw new UnsupportedOperationException(
        "Legacy ShadowCanvas description APIs are not supported");
  }

  @Override
  public Paint getDrawnPaint() {
    throw new UnsupportedOperationException(
        "Legacy ShadowCanvas description APIs are not supported");
  }

  @Override
  public void setHeight(int height) {
    throw new UnsupportedOperationException("setHeight is not supported in native Canvas");
  }

  @Override
  public void setWidth(int width) {
    throw new UnsupportedOperationException("setWidth is not supported in native Canvas");
  }

  @Override
  public TextHistoryEvent getDrawnTextEvent(int i) {
    throw new UnsupportedOperationException(
        "Legacy ShadowCanvas description APIs are not supported");
  }

  @Override
  public int getTextHistoryCount() {
    throw new UnsupportedOperationException(
        "Legacy ShadowCanvas description APIs are not supported");
  }

  @Override
  public RectPaintHistoryEvent getDrawnRect(int i) {
    throw new UnsupportedOperationException(
        "Legacy ShadowCanvas description APIs are not supported");
  }

  @Override
  public RectPaintHistoryEvent getLastDrawnRect() {
    throw new UnsupportedOperationException(
        "Legacy ShadowCanvas description APIs are not supported");
  }

  @Override
  public int getRectPaintHistoryCount() {
    throw new UnsupportedOperationException(
        "Legacy ShadowCanvas description APIs are not supported");
  }

  @Override
  public RoundRectPaintHistoryEvent getDrawnRoundRect(int i) {
    throw new UnsupportedOperationException(
        "Legacy ShadowCanvas description APIs are not supported");
  }

  @Override
  public RoundRectPaintHistoryEvent getLastDrawnRoundRect() {
    throw new UnsupportedOperationException(
        "Legacy ShadowCanvas description APIs are not supported");
  }

  @Override
  public int getRoundRectPaintHistoryCount() {
    throw new UnsupportedOperationException(
        "Legacy ShadowCanvas description APIs are not supported");
  }

  @Override
  public LinePaintHistoryEvent getDrawnLine(int i) {
    throw new UnsupportedOperationException(
        "Legacy ShadowCanvas description APIs are not supported");
  }

  @Override
  public int getLinePaintHistoryCount() {
    throw new UnsupportedOperationException(
        "Legacy ShadowCanvas description APIs are not supported");
  }

  @Override
  public int getOvalPaintHistoryCount() {
    throw new UnsupportedOperationException(
        "Legacy ShadowCanvas description APIs are not supported");
  }

  @Override
  public OvalPaintHistoryEvent getDrawnOval(int i) {
    throw new UnsupportedOperationException(
        "Legacy ShadowCanvas description APIs are not supported");
  }

  @ForType(BaseCanvas.class)
  interface BaseCanvasReflector {
    @Accessor("mNativeCanvasWrapper")
    long getNativeCanvas();
  }

  /** Shadow picker for {@link BaseCanvas}. */
  public static final class Picker extends GraphicsShadowPicker<Object> {
    public Picker() {
      super(null, ShadowNativeBaseCanvas.class);
    }
  }
}
