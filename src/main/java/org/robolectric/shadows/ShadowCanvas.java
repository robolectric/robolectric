package org.robolectric.shadows;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

import java.util.ArrayList;
import java.util.List;

import static org.robolectric.Robolectric.newInstanceOf;
import static org.robolectric.Robolectric.shadowOf;

/**
 * Shadows the {@code android.graphics.Canvas} class.
 * <p/>
 * Broken.
 * This implementation is very specific to the application for which it was developed.
 * Todo: Reimplement. Consider using the same strategy of collecting a history of draw events and providing methods for writing queries based on type, number, and order of events.
 */
@SuppressWarnings({"UnusedDeclaration"})
@Implements(Canvas.class)
public class ShadowCanvas {
  private List<PathPaintHistoryEvent> pathPaintEvents = new ArrayList<PathPaintHistoryEvent>();
  private List<CirclePaintHistoryEvent> circlePaintEvents = new ArrayList<CirclePaintHistoryEvent>();
  private List<TextHistoryEvent> drawnTextEventHistory = new ArrayList<TextHistoryEvent>();
  private Paint drawnPaint;
  private Bitmap targetBitmap = newInstanceOf(Bitmap.class);
  private float translateX;
  private float translateY;
  private float scaleX = 1;
  private float scaleY = 1;
  private int height;
  private int width;

  public void __constructor__(Bitmap bitmap) {
    this.targetBitmap = bitmap;
  }

  public void appendDescription(String s) {
    shadowOf(targetBitmap).appendDescription(s);
  }

  public String getDescription() {
    return shadowOf(targetBitmap).getDescription();
  }

  @Implementation
  public void setBitmap(Bitmap bitmap) {
    targetBitmap = bitmap;
  }

  @Implementation
  public void drawText(String text, float x, float y, Paint paint) {
    drawnTextEventHistory.add(new TextHistoryEvent(x, y, paint, text));
  }

  @Implementation
  public void translate(float x, float y) {
    this.translateX = x;
    this.translateY = y;
  }

  @Implementation
  public void scale(float sx, float sy) {
    this.scaleX = sx;
    this.scaleY = sy;
  }

  @Implementation
  public void scale(float sx, float sy, float px, float py) {
    this.scaleX = sx;
    this.scaleY = sy;
  }

  @Implementation
  public void drawPaint(Paint paint) {
    drawnPaint = paint;
  }

  @Implementation
  public void drawColor(int color) {
    appendDescription("draw color " + color);
  }

  @Implementation
  public void drawBitmap(Bitmap bitmap, float left, float top, Paint paint) {
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
  public void drawBitmap(Bitmap bitmap, Rect src, Rect dst, Paint paint) {
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
  public void drawBitmap(Bitmap bitmap, Matrix matrix, Paint paint) {
    describeBitmap(bitmap, paint);

    appendDescription(" transformed by matrix");
  }

  @Implementation
  public void drawPath(Path path, Paint paint) {
    pathPaintEvents.add(new PathPaintHistoryEvent(new Path(path), paint));

    separateLines();
    appendDescription("Path " + shadowOf(path).getPoints().toString());
  }

  @Implementation
  public void drawCircle(float cx, float cy, float radius, Paint paint) {
    circlePaintEvents.add(new CirclePaintHistoryEvent(cx, cy, radius, paint));
  }

  private void describeBitmap(Bitmap bitmap, Paint paint) {
    separateLines();

    appendDescription(shadowOf(bitmap).getDescription());

    if (paint != null) {
      ColorFilter colorFilter = paint.getColorFilter();
      if (colorFilter != null) {
        appendDescription(" with " + colorFilter);
      }
    }
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

  public void resetCanvasHistory() {
    drawnTextEventHistory.clear();
    pathPaintEvents.clear();
    circlePaintEvents.clear();
    shadowOf(targetBitmap).setDescription("");
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
  public int getWidth() {
    return width;
  }

  @Implementation
  public int getHeight() {
    return height;
  }

  public TextHistoryEvent getDrawnTextEvent(int i) {
    return drawnTextEventHistory.get(i);
  }

  public int getTextHistoryCount() {
    return drawnTextEventHistory.size();
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
