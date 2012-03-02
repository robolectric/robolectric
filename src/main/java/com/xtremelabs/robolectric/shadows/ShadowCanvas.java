package com.xtremelabs.robolectric.shadows;

import android.graphics.*;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

import java.util.ArrayList;
import java.util.List;

import static com.xtremelabs.robolectric.Robolectric.newInstanceOf;
import static com.xtremelabs.robolectric.Robolectric.shadowOf;

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
    private Paint drawnPaint;
    private Bitmap targetBitmap = newInstanceOf(Bitmap.class);
    private float translateX;
    private float translateY;
    private float scaleX = 1;
    private float scaleY = 1;

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
        if (x != 0 && y != 0) {
            appendDescription(" at (" + x + "," + y + ")");
        }

        if (scaleX != 1 && scaleY != 1) {
            appendDescription(" scaled by (" + scaleX + "," + scaleY + ")");
        }
    }

    @Implementation
    public void drawPath(Path path, Paint paint) {
        pathPaintEvents.add(new PathPaintHistoryEvent(path, paint));

        separateLines();
        appendDescription("Path " + shadowOf(path).getPoints().toString());
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

    @Implementation
    public void drawBitmap(Bitmap bitmap, Matrix matrix, Paint paint) {
        describeBitmap(bitmap, paint);

        appendDescription(" transformed by matrix");
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
        pathPaintEvents.clear();
        circlePaintEvents.clear();
    }

    public Paint getDrawnPaint() {
        return drawnPaint;
    }

    private static class PathPaintHistoryEvent {
        private Path drawnPath;
        private Paint pathPaint;

        PathPaintHistoryEvent(Path drawnPath, Paint pathPaint) {
            this.drawnPath = drawnPath;
            this.pathPaint = pathPaint;
        }
    }

    public static class CirclePaintHistoryEvent {
        public Paint paint;
        public float centerX;
        public float centerY;
        public float radius;

        private CirclePaintHistoryEvent(float centerX, float centerY, float radius, Paint paint) {
            this.paint = paint;
            this.centerX = centerX;
            this.centerY = centerY;
            this.radius = radius;
        }
    }
}
