package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.KITKAT;
import static android.os.Build.VERSION_CODES.KITKAT_WATCH;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.LOLLIPOP_MR1;
import static android.os.Build.VERSION_CODES.M;
import static android.os.Build.VERSION_CODES.N_MR1;
import static android.os.Build.VERSION_CODES.O;
import static android.os.Build.VERSION_CODES.P;
import static android.os.Build.VERSION_CODES.Q;
import static android.os.Build.VERSION_CODES.R;
import static android.os.Build.VERSION_CODES.S;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.List;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.annotation.ReflectorObject;
import org.robolectric.annotation.Resetter;
import org.robolectric.res.android.NativeObjRegistry;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.reflector.Direct;
import org.robolectric.util.reflector.ForType;

/**
 * Broken. This implementation is very specific to the application for which it was developed. Todo:
 * Reimplement. Consider using the same strategy of collecting a history of draw events and
 * providing methods for writing queries based on type, number, and order of events.
 */
@SuppressWarnings({"UnusedDeclaration"})
@Implements(value = Canvas.class, isInAndroidSdk = false)
public class ShadowLegacyCanvas extends ShadowCanvas {
  private static final NativeObjRegistry<NativeCanvas> nativeObjectRegistry =
      new NativeObjRegistry<>(NativeCanvas.class);

  @RealObject protected Canvas realCanvas;
  @ReflectorObject protected CanvasReflector canvasReflector;

  private final List<RoundRectPaintHistoryEvent> roundRectPaintEvents = new ArrayList<>();
  private List<PathPaintHistoryEvent> pathPaintEvents = new ArrayList<>();
  private List<CirclePaintHistoryEvent> circlePaintEvents = new ArrayList<>();
  private List<ArcPaintHistoryEvent> arcPaintEvents = new ArrayList<>();
  private List<RectPaintHistoryEvent> rectPaintEvents = new ArrayList<>();
  private List<LinePaintHistoryEvent> linePaintEvents = new ArrayList<>();
  private List<OvalPaintHistoryEvent> ovalPaintEvents = new ArrayList<>();
  private List<TextHistoryEvent> drawnTextEventHistory = new ArrayList<>();
  private Paint drawnPaint;
  private Bitmap targetBitmap = ReflectionHelpers.callConstructor(Bitmap.class);
  private float translateX;
  private float translateY;
  private float scaleX = 1;
  private float scaleY = 1;
  private int height;
  private int width;

  @Implementation
  protected void __constructor__(Bitmap bitmap) {
    canvasReflector.__constructor__(bitmap);
    this.targetBitmap = bitmap;
  }

  private long getNativeId() {
    return RuntimeEnvironment.getApiLevel() <= KITKAT_WATCH
        ? (int) ReflectionHelpers.getField(realCanvas, "mNativeCanvas")
        : realCanvas.getNativeCanvasWrapper();
  }

  private NativeCanvas getNativeCanvas() {
    return nativeObjectRegistry.getNativeObject(getNativeId());
  }

  @Override
  public void appendDescription(String s) {
    ShadowBitmap shadowBitmap = Shadow.extract(targetBitmap);
    shadowBitmap.appendDescription(s);
  }

  @Override
  public String getDescription() {
    ShadowBitmap shadowBitmap = Shadow.extract(targetBitmap);
    return shadowBitmap.getDescription();
  }

  @Implementation
  protected void setBitmap(Bitmap bitmap) {
    targetBitmap = bitmap;
  }

  @Implementation
  protected void drawText(String text, float x, float y, Paint paint) {
    drawnTextEventHistory.add(new TextHistoryEvent(x, y, paint, text));
  }

  @Implementation
  protected void drawText(CharSequence text, int start, int end, float x, float y, Paint paint) {
    drawnTextEventHistory.add(
        new TextHistoryEvent(x, y, paint, text.subSequence(start, end).toString()));
  }

  @Implementation
  protected void drawText(char[] text, int index, int count, float x, float y, Paint paint) {
    drawnTextEventHistory.add(new TextHistoryEvent(x, y, paint, new String(text, index, count)));
  }

  @Implementation
  protected void drawText(String text, int start, int end, float x, float y, Paint paint) {
    drawnTextEventHistory.add(new TextHistoryEvent(x, y, paint, text.substring(start, end)));
  }

  @Implementation
  protected void translate(float x, float y) {
    this.translateX = x;
    this.translateY = y;
  }

  @Implementation
  protected void scale(float sx, float sy) {
    this.scaleX = sx;
    this.scaleY = sy;
  }

  @Implementation
  protected void scale(float sx, float sy, float px, float py) {
    this.scaleX = sx;
    this.scaleY = sy;
  }

  @Implementation
  protected void drawPaint(Paint paint) {
    drawnPaint = paint;
  }

  @Implementation
  protected void drawColor(int color) {
    appendDescription("draw color " + color);
  }

  @Implementation
  protected void drawBitmap(Bitmap bitmap, float left, float top, Paint paint) {
    describeBitmap(bitmap, paint);

    int x = (int) (left + translateX);
    int y = (int) (top + translateY);
    if (x != 0 || y != 0) {
      appendDescription(" at (" + x + "," + y + ")");
    }

    if (scaleX != 1 && scaleY != 1) {
      appendDescription(" scaled by (" + scaleX + "," + scaleY + ")");
    }

    if (bitmap != null && targetBitmap != null) {
      ShadowLegacyBitmap shadowTargetBitmap = Shadow.extract(targetBitmap);
      shadowTargetBitmap.drawBitmap(bitmap, (int) left, (int) top);
    }
  }

  @Implementation
  protected void drawBitmap(Bitmap bitmap, Rect src, Rect dst, Paint paint) {
    describeBitmap(bitmap, paint);

    StringBuilder descriptionBuilder = new StringBuilder();
    if (dst != null) {
      descriptionBuilder
          .append(" at (")
          .append(dst.left)
          .append(",")
          .append(dst.top)
          .append(") with height=")
          .append(dst.height())
          .append(" and width=")
          .append(dst.width());
    }

    if (src != null) {
      descriptionBuilder.append(" taken from ").append(src.toString());
    }
    appendDescription(descriptionBuilder.toString());
  }

  @Implementation
  protected void drawBitmap(Bitmap bitmap, Rect src, RectF dst, Paint paint) {
    describeBitmap(bitmap, paint);

    StringBuilder descriptionBuilder = new StringBuilder();
    if (dst != null) {
      descriptionBuilder
          .append(" at (")
          .append(dst.left)
          .append(",")
          .append(dst.top)
          .append(") with height=")
          .append(dst.height())
          .append(" and width=")
          .append(dst.width());
    }

    if (src != null) {
      descriptionBuilder.append(" taken from ").append(src.toString());
    }
    appendDescription(descriptionBuilder.toString());
  }

  @Implementation
  protected void drawBitmap(Bitmap bitmap, Matrix matrix, Paint paint) {
    describeBitmap(bitmap, paint);

    ShadowMatrix shadowMatrix = Shadow.extract(matrix);
    appendDescription(" transformed by " + shadowMatrix.getDescription());
  }

  @Implementation
  protected void drawPath(Path path, Paint paint) {
    pathPaintEvents.add(new PathPaintHistoryEvent(new Path(path), new Paint(paint)));

    separateLines();
    ShadowPath shadowPath = Shadow.extract(path);
    appendDescription("Path " + shadowPath.getPoints().toString());
  }

  @Implementation
  protected void drawCircle(float cx, float cy, float radius, Paint paint) {
    circlePaintEvents.add(new CirclePaintHistoryEvent(cx, cy, radius, paint));
  }

  @Implementation
  protected void drawArc(
      RectF oval, float startAngle, float sweepAngle, boolean useCenter, Paint paint) {
    arcPaintEvents.add(new ArcPaintHistoryEvent(oval, startAngle, sweepAngle, useCenter, paint));
  }

  @Implementation
  protected void drawRect(float left, float top, float right, float bottom, Paint paint) {
    rectPaintEvents.add(new RectPaintHistoryEvent(left, top, right, bottom, paint));

    if (targetBitmap != null) {
      ShadowLegacyBitmap shadowTargetBitmap = Shadow.extract(targetBitmap);
      shadowTargetBitmap.drawRect(new RectF(left, top, right, bottom), paint);
    }
  }

  @Implementation
  protected void drawRect(Rect r, Paint paint) {
    rectPaintEvents.add(new RectPaintHistoryEvent(r.left, r.top, r.right, r.bottom, paint));

    if (targetBitmap != null) {
      ShadowLegacyBitmap shadowTargetBitmap = Shadow.extract(targetBitmap);
      shadowTargetBitmap.drawRect(r, paint);
    }
  }

  @Implementation
  protected void drawRoundRect(RectF rect, float rx, float ry, Paint paint) {
    roundRectPaintEvents.add(
        new RoundRectPaintHistoryEvent(
            rect.left, rect.top, rect.right, rect.bottom, rx, ry, paint));
  }

  @Implementation
  protected void drawLine(float startX, float startY, float stopX, float stopY, Paint paint) {
    linePaintEvents.add(new LinePaintHistoryEvent(startX, startY, stopX, stopY, paint));
  }

  @Implementation
  protected void drawOval(RectF oval, Paint paint) {
    ovalPaintEvents.add(new OvalPaintHistoryEvent(oval, paint));
  }

  private void describeBitmap(Bitmap bitmap, Paint paint) {
    separateLines();

    ShadowBitmap shadowBitmap = Shadow.extract(bitmap);
    appendDescription(shadowBitmap.getDescription());

    if (paint != null) {
      ColorFilter colorFilter = paint.getColorFilter();
      if (colorFilter != null) {
        appendDescription(" with " + colorFilter.getClass().getSimpleName());
      }
    }
  }

  private void separateLines() {
    if (getDescription().length() != 0) {
      appendDescription("\n");
    }
  }

  @Override
  public int getPathPaintHistoryCount() {
    return pathPaintEvents.size();
  }

  @Override
  public int getCirclePaintHistoryCount() {
    return circlePaintEvents.size();
  }

  @Override
  public int getArcPaintHistoryCount() {
    return arcPaintEvents.size();
  }

  @Override
  public boolean hasDrawnPath() {
    return getPathPaintHistoryCount() > 0;
  }

  @Override
  public boolean hasDrawnCircle() {
    return circlePaintEvents.size() > 0;
  }

  @Override
  public Paint getDrawnPathPaint(int i) {
    return pathPaintEvents.get(i).pathPaint;
  }

  @Override
  public Path getDrawnPath(int i) {
    return pathPaintEvents.get(i).drawnPath;
  }

  @Override
  public CirclePaintHistoryEvent getDrawnCircle(int i) {
    return circlePaintEvents.get(i);
  }

  @Override
  public ArcPaintHistoryEvent getDrawnArc(int i) {
    return arcPaintEvents.get(i);
  }

  @Override
  public void resetCanvasHistory() {
    drawnTextEventHistory.clear();
    pathPaintEvents.clear();
    circlePaintEvents.clear();
    rectPaintEvents.clear();
    roundRectPaintEvents.clear();
    linePaintEvents.clear();
    ovalPaintEvents.clear();
    ShadowBitmap shadowBitmap = Shadow.extract(targetBitmap);
    shadowBitmap.setDescription("");
  }

  @Override
  public Paint getDrawnPaint() {
    return drawnPaint;
  }

  @Override
  public void setHeight(int height) {
    this.height = height;
  }

  @Override
  public void setWidth(int width) {
    this.width = width;
  }

  @Implementation
  protected int getWidth() {
    if (width == 0) {
      return targetBitmap.getWidth();
    }
    return width;
  }

  @Implementation
  protected int getHeight() {
    if (height == 0) {
      return targetBitmap.getHeight();
    }
    return height;
  }

  @Implementation
  protected boolean getClipBounds(Rect bounds) {
    Preconditions.checkNotNull(bounds);
    if (targetBitmap == null) {
      return false;
    }
    bounds.set(0, 0, targetBitmap.getWidth(), targetBitmap.getHeight());
    return !bounds.isEmpty();
  }

  @Override
  public TextHistoryEvent getDrawnTextEvent(int i) {
    return drawnTextEventHistory.get(i);
  }

  @Override
  public int getTextHistoryCount() {
    return drawnTextEventHistory.size();
  }

  @Override
  public RectPaintHistoryEvent getDrawnRect(int i) {
    return rectPaintEvents.get(i);
  }

  @Override
  public RectPaintHistoryEvent getLastDrawnRect() {
    return rectPaintEvents.get(rectPaintEvents.size() - 1);
  }

  @Override
  public int getRectPaintHistoryCount() {
    return rectPaintEvents.size();
  }

  @Override
  public RoundRectPaintHistoryEvent getDrawnRoundRect(int i) {
    return roundRectPaintEvents.get(i);
  }

  @Override
  public RoundRectPaintHistoryEvent getLastDrawnRoundRect() {
    return roundRectPaintEvents.get(roundRectPaintEvents.size() - 1);
  }

  @Override
  public int getRoundRectPaintHistoryCount() {
    return roundRectPaintEvents.size();
  }

  @Override
  public LinePaintHistoryEvent getDrawnLine(int i) {
    return linePaintEvents.get(i);
  }

  @Override
  public int getLinePaintHistoryCount() {
    return linePaintEvents.size();
  }

  @Override
  public int getOvalPaintHistoryCount() {
    return ovalPaintEvents.size();
  }

  @Override
  public OvalPaintHistoryEvent getDrawnOval(int i) {
    return ovalPaintEvents.get(i);
  }

  @Implementation(maxSdk = N_MR1)
  protected int save() {
    return getNativeCanvas().save();
  }

  @Implementation(maxSdk = N_MR1)
  protected void restore() {
    getNativeCanvas().restore();
  }

  @Implementation(maxSdk = N_MR1)
  protected int getSaveCount() {
    return getNativeCanvas().getSaveCount();
  }

  @Implementation(maxSdk = N_MR1)
  protected void restoreToCount(int saveCount) {
    getNativeCanvas().restoreToCount(saveCount);
  }

  @Implementation(minSdk = KITKAT)
  protected void release() {
    nativeObjectRegistry.unregister(getNativeId());
    canvasReflector.release();
  }

  @Implementation(maxSdk = KITKAT_WATCH)
  protected static int initRaster(int bitmapHandle) {
    return (int) nativeObjectRegistry.register(new NativeCanvas());
  }

  @Implementation(minSdk = LOLLIPOP, maxSdk = LOLLIPOP_MR1)
  protected static long initRaster(long bitmapHandle) {
    return nativeObjectRegistry.register(new NativeCanvas());
  }

  @Implementation(minSdk = M, maxSdk = N_MR1)
  protected static long initRaster(Bitmap bitmap) {
    return nativeObjectRegistry.register(new NativeCanvas());
  }

  @Implementation(minSdk = O, maxSdk = P)
  protected static long nInitRaster(Bitmap bitmap) {
    return nativeObjectRegistry.register(new NativeCanvas());
  }

  @Implementation(minSdk = Q)
  protected static long nInitRaster(long bitmapHandle) {
    return nativeObjectRegistry.register(new NativeCanvas());
  }

  @Implementation(minSdk = O)
  protected static int nGetSaveCount(long canvasHandle) {
    return nativeObjectRegistry.getNativeObject(canvasHandle).getSaveCount();
  }

  @Implementation(minSdk = O)
  protected static int nSave(long canvasHandle, int saveFlags) {
    return nativeObjectRegistry.getNativeObject(canvasHandle).save();
  }

  @Implementation(maxSdk = KITKAT_WATCH)
  protected static int native_saveLayer(int nativeCanvas, RectF bounds, int paint, int layerFlags) {
    return nativeObjectRegistry.getNativeObject(nativeCanvas).save();
  }

  @Implementation(maxSdk = KITKAT_WATCH)
  protected static int native_saveLayer(
      int nativeCanvas, float l, float t, float r, float b, int paint, int layerFlags) {
    return nativeObjectRegistry.getNativeObject(nativeCanvas).save();
  }

  @Implementation(minSdk = LOLLIPOP, maxSdk = N_MR1)
  protected static int native_saveLayer(
      long nativeCanvas, float l, float t, float r, float b, long nativePaint, int layerFlags) {
    return nativeObjectRegistry.getNativeObject(nativeCanvas).save();
  }

  @Implementation(minSdk = O, maxSdk = R)
  protected static int nSaveLayer(
      long nativeCanvas, float l, float t, float r, float b, long nativePaint, int layerFlags) {
    return nativeObjectRegistry.getNativeObject(nativeCanvas).save();
  }

  @Implementation(minSdk = S)
  protected static int nSaveLayer(
      long nativeCanvas, float l, float t, float r, float b, long nativePaint) {
    return nativeObjectRegistry.getNativeObject(nativeCanvas).save();
  }

  @Implementation(maxSdk = KITKAT_WATCH)
  protected static int native_saveLayerAlpha(
      int nativeCanvas, RectF bounds, int alpha, int layerFlags) {
    return nativeObjectRegistry.getNativeObject(nativeCanvas).save();
  }

  @Implementation(maxSdk = KITKAT_WATCH)
  protected static int native_saveLayerAlpha(
      int nativeCanvas, float l, float t, float r, float b, int alpha, int layerFlags) {
    return nativeObjectRegistry.getNativeObject(nativeCanvas).save();
  }

  @Implementation(minSdk = LOLLIPOP, maxSdk = N_MR1)
  protected static int native_saveLayerAlpha(
      long nativeCanvas, float l, float t, float r, float b, int alpha, int layerFlags) {
    return nativeObjectRegistry.getNativeObject(nativeCanvas).save();
  }

  @Implementation(minSdk = O, maxSdk = R)
  protected static int nSaveLayerAlpha(
      long nativeCanvas, float l, float t, float r, float b, int alpha, int layerFlags) {
    return nativeObjectRegistry.getNativeObject(nativeCanvas).save();
  }

  @Implementation(minSdk = S)
  protected static int nSaveLayerAlpha(
      long nativeCanvas, float l, float t, float r, float b, int alpha) {
    return nativeObjectRegistry.getNativeObject(nativeCanvas).save();
  }

  @Implementation(minSdk = O)
  protected static boolean nRestore(long canvasHandle) {
    return nativeObjectRegistry.getNativeObject(canvasHandle).restore();
  }

  @Implementation(minSdk = O)
  protected static void nRestoreToCount(long canvasHandle, int saveCount) {
    nativeObjectRegistry.getNativeObject(canvasHandle).restoreToCount(saveCount);
  }

  private static class PathPaintHistoryEvent {
    private final Path drawnPath;
    private final Paint pathPaint;

    PathPaintHistoryEvent(Path drawnPath, Paint pathPaint) {
      this.drawnPath = drawnPath;
      this.pathPaint = pathPaint;
    }
  }

  @Resetter
  public static void reset() {
    nativeObjectRegistry.clear();
  }

  @SuppressWarnings("MemberName")
  @ForType(Canvas.class)
  private interface CanvasReflector {
    @Direct
    void __constructor__(Bitmap bitmap);

    @Direct
    void release();
  }

  private static class NativeCanvas {
    private int saveCount = 1;

    int save() {
      return saveCount++;
    }

    boolean restore() {
      if (saveCount > 1) {
        saveCount--;
        return true;
      } else {
        return false;
      }
    }

    int getSaveCount() {
      return saveCount;
    }

    void restoreToCount(int saveCount) {
      if (saveCount > 0) {
        this.saveCount = saveCount;
      }
    }
  }
}
