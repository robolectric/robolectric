package org.robolectric.shadows;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import java.util.ArrayList;
import java.util.List;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.util.Join;
import org.robolectric.util.ReflectionHelpers;

/**
 * Broken. This implementation is very specific to the application for which it was developed.
 * Todo: Reimplement. Consider using the same strategy of collecting a history of draw events
 * and providing methods for writing queries based on type, number, and order of events.
 */
@SuppressWarnings({"UnusedDeclaration"})
@Implements(Canvas.class)
public class ShadowCanvas {
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

  /**
   * Returns a textual representation of the appearance of the object.
   *
   * @param canvas the canvas to visualize
   * @return The textual representation of the appearance of the object.
   */
  public static String visualize(Canvas canvas) {
    ShadowCanvas shadowCanvas = Shadow.extract(canvas);
    return shadowCanvas.getDescription();
  }

  @Implementation
  protected void __constructor__(Bitmap bitmap) {
    this.targetBitmap = bitmap;
  }

  public void appendDescription(String s) {
    ShadowBitmap shadowBitmap = Shadow.extract(targetBitmap);
    shadowBitmap.appendDescription(s);
  }

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
  }

  @Implementation
  protected void drawBitmap(Bitmap bitmap, Rect src, Rect dst, Paint paint) {
    describeBitmap(bitmap, paint);

    StringBuilder descriptionBuilder = new StringBuilder();
    if (dst != null) {
      descriptionBuilder.append(" at (").append(dst.left).append(",").append(dst.top)
          .append(") with height=").append(dst.height()).append(" and width=").append(dst.width());
    }

    if (src != null) {
      descriptionBuilder.append( " taken from ").append(src.toString());
    }
    appendDescription(descriptionBuilder.toString());
  }

  @Implementation
  protected void drawBitmap(Bitmap bitmap, Rect src, RectF dst, Paint paint) {
    describeBitmap(bitmap, paint);

    StringBuilder descriptionBuilder = new StringBuilder();
    if (dst != null) {
      descriptionBuilder.append(" at (").append(dst.left).append(",").append(dst.top)
          .append(") with height=").append(dst.height()).append(" and width=").append(dst.width());
    }

    if (src != null) {
      descriptionBuilder.append( " taken from ").append(src.toString());
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
  }

  @Implementation
  protected void drawLine(float startX, float startY, float stopX, float stopY, Paint paint) {
    linePaintEvents.add(new LinePaintHistoryEvent(startX, startY, stopX, stopY, paint));
  }

  @Implementation
  protected void drawOval(RectF oval, Paint paint) {
    ovalPaintEvents.add(new OvalPaintHistoryEvent(oval, paint));
  }

  @Implementation
  protected int save() {
    return 1;
  }

  @Implementation
  protected void restore() {}

  private void describeBitmap(Bitmap bitmap, Paint paint) {
    separateLines();

    ShadowBitmap shadowBitmap = Shadow.extract(bitmap);
    appendDescription(shadowBitmap.getDescription());

    if (paint != null) {
      ColorFilter colorFilter = paint.getColorFilter();
      if (colorFilter != null) {
        if (colorFilter instanceof ColorMatrixColorFilter) {
          ColorMatrixColorFilter colorMatrixColorFilter = (ColorMatrixColorFilter) colorFilter;
          ShadowColorMatrixColorFilter shadowColorMatrixColorFilter =
              Shadow.extract(colorMatrixColorFilter);
          ColorMatrix colorMatrix = shadowColorMatrixColorFilter.getMatrix();
          appendDescription(" with ColorMatrixColorFilter<" + formatColorMatric(colorMatrix) + ">");
        } else {
          appendDescription(" with " + colorFilter);
        }
      }
    }
  }

  private String formatColorMatric(ColorMatrix colorMatrix) {
    List<String> floats = new ArrayList<>();
    for (float f : colorMatrix.getArray()) {
      String format = String.format("%.2f", f);
      format = format.replace(".00", "");
      floats.add(format);
    }
    return Join.join(",", floats);
  }

  private void separateLines() {
    if (getDescription().length() != 0) {
      appendDescription("\n");
    }
  }

  public int getPathPaintHistoryCount() {
    return pathPaintEvents.size();
  }

  public int getCirclePaintHistoryCount() {
    return circlePaintEvents.size();
  }

  public int getArcPaintHistoryCount() {
    return arcPaintEvents.size();
  }

  public boolean hasDrawnPath() {
    return getPathPaintHistoryCount() > 0;
  }

  public boolean hasDrawnCircle() {
    return circlePaintEvents.size() > 0;
  }

  public Paint getDrawnPathPaint(int i) {
    return pathPaintEvents.get(i).pathPaint;
  }

  public Path getDrawnPath(int i) {
    return pathPaintEvents.get(i).drawnPath;
  }

  public CirclePaintHistoryEvent getDrawnCircle(int i) {
    return circlePaintEvents.get(i);
  }

  public ArcPaintHistoryEvent getDrawnArc(int i) {
    return arcPaintEvents.get(i);
  }

  public void resetCanvasHistory() {
    drawnTextEventHistory.clear();
    pathPaintEvents.clear();
    circlePaintEvents.clear();
    rectPaintEvents.clear();
    linePaintEvents.clear();
    ovalPaintEvents.clear();
    ShadowBitmap shadowBitmap = Shadow.extract(targetBitmap);
    shadowBitmap.setDescription("");
  }

  public Paint getDrawnPaint() {
    return drawnPaint;
  }

  public void setHeight(int height) {
    this.height = height;
  }

  public void setWidth(int width) {
    this.width = width;
  }

  @Implementation
  protected int getWidth() {
    return width;
  }

  @Implementation
  protected int getHeight() {
    return height;
  }

  public TextHistoryEvent getDrawnTextEvent(int i) {
    return drawnTextEventHistory.get(i);
  }

  public int getTextHistoryCount() {
    return drawnTextEventHistory.size();
  }

  public RectPaintHistoryEvent getDrawnRect(int i) {
    return rectPaintEvents.get(i);
  }

  public RectPaintHistoryEvent getLastDrawnRect() {
    return rectPaintEvents.get(rectPaintEvents.size() - 1);
  }

  public int getRectPaintHistoryCount() {
    return rectPaintEvents.size();
  }

  public LinePaintHistoryEvent getDrawnLine(int i) {
    return linePaintEvents.get(i);
  }

  public int getLinePaintHistoryCount() {
    return linePaintEvents.size();
  }

  public int getOvalPaintHistoryCount() {
    return ovalPaintEvents.size();
  }

  public OvalPaintHistoryEvent getDrawnOval(int i) {
    return ovalPaintEvents.get(i);
  }

  public static class LinePaintHistoryEvent {
    public Paint paint;
    public float startX;
    public float startY;
    public float stopX;
    public float stopY;

    private LinePaintHistoryEvent(
        float startX, float startY, float stopX, float stopY, Paint paint) {
      this.paint = new Paint(paint);
      this.paint.setColor(paint.getColor());
      this.paint.setStrokeWidth(paint.getStrokeWidth());
      this.startX = startX;
      this.startY = startY;
      this.stopX = stopX;
      this.stopY = stopY;
    }
  }

  public static class OvalPaintHistoryEvent {
    public final RectF oval;
    public final Paint paint;

    private OvalPaintHistoryEvent(RectF oval, Paint paint) {
      this.oval = new RectF(oval);
      this.paint = new Paint(paint);
      this.paint.setColor(paint.getColor());
      this.paint.setStrokeWidth(paint.getStrokeWidth());
    }
  }

  public static class RectPaintHistoryEvent {
    public final Paint paint;
    public final RectF rect;
    public final float left;
    public final float top;
    public final float right;
    public final float bottom;

    private RectPaintHistoryEvent(
        float left, float top, float right, float bottom, Paint paint){
      this.rect = new RectF(left, top, right, bottom);
      this.paint = new Paint(paint);
      this.paint.setColor(paint.getColor());
      this.paint.setStrokeWidth(paint.getStrokeWidth());
      this.paint.setTextSize(paint.getTextSize());
      this.paint.setStyle(paint.getStyle());
      this.left = left;
      this.top = top;
      this.right = right;
      this.bottom = bottom;
    }
  }

  private static class PathPaintHistoryEvent {
    private final Path drawnPath;
    private final Paint pathPaint;

    PathPaintHistoryEvent(Path drawnPath, Paint pathPaint) {
      this.drawnPath = drawnPath;
      this.pathPaint = pathPaint;
    }
  }

  public static class CirclePaintHistoryEvent {
    public final float centerX;
    public final float centerY;
    public final float radius;
    public final Paint paint;

    private CirclePaintHistoryEvent(float centerX, float centerY, float radius, Paint paint) {
      this.centerX = centerX;
      this.centerY = centerY;
      this.radius = radius;
      this.paint = paint;
    }
  }

  public static class ArcPaintHistoryEvent {
    public final RectF oval;
    public final float startAngle;
    public final float sweepAngle;
    public final boolean useCenter;
    public final Paint paint;

    public ArcPaintHistoryEvent(RectF oval, float startAngle, float sweepAngle, boolean useCenter,
                                Paint paint) {
      this.oval = oval;
      this.startAngle = startAngle;
      this.sweepAngle = sweepAngle;
      this.useCenter = useCenter;
      this.paint = paint;
    }
  }

  public static class TextHistoryEvent {
    public final float x;
    public final float y;
    public final Paint paint;
    public final String text;

    private TextHistoryEvent(float x, float y, Paint paint, String text) {
      this.x = x;
      this.y = y;
      this.paint = paint;
      this.text = text;
    }
  }
}
