package org.robolectric.shadows;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import org.robolectric.annotation.Implements;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.shadows.ShadowCanvas.Picker;

/** Base class for {@link Canvas} shadow classes. Mainly contains public shadow API signatures. */
@Implements(value = Canvas.class, shadowPicker = Picker.class)
public abstract class ShadowCanvas {

  public static String visualize(Canvas canvas) {
    if (Shadow.extract(canvas) instanceof ShadowLegacyCanvas) {
      ShadowCanvas shadowCanvas = Shadow.extract(canvas);
      return shadowCanvas.getDescription();
    } else {
      throw new UnsupportedOperationException(
          "ShadowCanvas.visualize is only supported in legacy Canvas");
    }
  }

  public abstract void appendDescription(String s);

  public abstract String getDescription();

  public abstract int getPathPaintHistoryCount();

  public abstract int getCirclePaintHistoryCount();

  public abstract int getArcPaintHistoryCount();

  public abstract boolean hasDrawnPath();

  public abstract boolean hasDrawnCircle();

  public abstract Paint getDrawnPathPaint(int i);

  public abstract Path getDrawnPath(int i);

  public abstract CirclePaintHistoryEvent getDrawnCircle(int i);

  public abstract ArcPaintHistoryEvent getDrawnArc(int i);

  public abstract void resetCanvasHistory();

  public abstract Paint getDrawnPaint();

  public abstract void setHeight(int height);

  public abstract void setWidth(int width);

  public abstract TextHistoryEvent getDrawnTextEvent(int i);

  public abstract int getTextHistoryCount();

  public abstract RectPaintHistoryEvent getDrawnRect(int i);

  public abstract RectPaintHistoryEvent getLastDrawnRect();

  public abstract int getRectPaintHistoryCount();

  public abstract RoundRectPaintHistoryEvent getDrawnRoundRect(int i);

  public abstract RoundRectPaintHistoryEvent getLastDrawnRoundRect();

  public abstract int getRoundRectPaintHistoryCount();

  public abstract LinePaintHistoryEvent getDrawnLine(int i);

  public abstract int getLinePaintHistoryCount();

  public abstract int getOvalPaintHistoryCount();

  public abstract OvalPaintHistoryEvent getDrawnOval(int i);

  public static class LinePaintHistoryEvent {
    public Paint paint;
    public float startX;
    public float startY;
    public float stopX;
    public float stopY;

    LinePaintHistoryEvent(float startX, float startY, float stopX, float stopY, Paint paint) {
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

    OvalPaintHistoryEvent(RectF oval, Paint paint) {
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

    RectPaintHistoryEvent(float left, float top, float right, float bottom, Paint paint) {
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

  /** Captures round rectangle drawing events */
  public static class RoundRectPaintHistoryEvent {
    public final Paint paint;
    public final RectF rect;
    public final float left;
    public final float top;
    public final float right;
    public final float bottom;
    public final float rx;
    public final float ry;

    RoundRectPaintHistoryEvent(
        float left, float top, float right, float bottom, float rx, float ry, Paint paint) {
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
      this.rx = rx;
      this.ry = ry;
    }
  }

  public static class CirclePaintHistoryEvent {
    public final float centerX;
    public final float centerY;
    public final float radius;
    public final Paint paint;

    CirclePaintHistoryEvent(float centerX, float centerY, float radius, Paint paint) {
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

    public ArcPaintHistoryEvent(
        RectF oval, float startAngle, float sweepAngle, boolean useCenter, Paint paint) {
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

    TextHistoryEvent(float x, float y, Paint paint, String text) {
      this.x = x;
      this.y = y;
      this.paint = paint;
      this.text = text;
    }
  }

  /** Shadow picker for {@link Canvas}. */
  public static final class Picker extends GraphicsShadowPicker<Object> {
    public Picker() {
      super(ShadowLegacyCanvas.class, ShadowNativeCanvas.class);
    }
  }
}
