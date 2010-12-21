package com.xtremelabs.robolectric.shadows;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

import java.util.ArrayList;
import java.util.List;

/**
 * Shadows the {@code android.graphics.Canvas} class.
 *
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
    private boolean drewSomethingAfterCircle;

    @Implementation
    public void drawPaint(Paint paint) {
        drawnPaint = paint;
    }

    @Implementation
    public void drawPath(Path path, Paint paint) {
        pathPaintEvents.add(new PathPaintHistoryEvent(path, paint));
        if (hasDrawnCircle()) {
            drewSomethingAfterCircle = true;
        }
    }

    @Implementation
    public void drawCircle(float cx, float cy, float radius, Paint paint) {
        circlePaintEvents.add(new CirclePaintHistoryEvent(cx, cy, radius, paint));
        drewSomethingAfterCircle = false;
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

    public boolean isDrewSomethingAfterCircle() {
        return drewSomethingAfterCircle;
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
