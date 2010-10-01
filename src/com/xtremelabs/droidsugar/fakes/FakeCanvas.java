package com.xtremelabs.droidsugar.fakes;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import com.xtremelabs.droidsugar.util.Implements;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(Canvas.class)
public class FakeCanvas {
    private List<PaintEvents> pathPaintEvents = new ArrayList<PaintEvents>();
    public Paint drawnPaint;
    public Paint circlePaint;
    public float circleCenterX;
    public float circleCenterY;
    public float circleRadius;
    public boolean drewSomethingAfterCircle;

    public void drawPaint(Paint paint) {
        drawnPaint = paint;
    }

    public void drawPath(Path path, Paint paint) {
        pathPaintEvents.add(new PaintEvents(path, paint));
        if(hasDrawnCircle()) {
            drewSomethingAfterCircle = true;
        }
    }

    public void drawCircle(float cx, float cy, float radius, Paint paint) {
        circleCenterX = cx;
        circleCenterY = cy;
        circleRadius = radius;
        circlePaint = paint;
        drewSomethingAfterCircle = false;
    }

    public int getPathPaintCount() {
        return pathPaintEvents.size();
    }

    public boolean hasDrawnPath() {
        return getPathPaintCount() > 0;
    }

    public boolean hasDrawnCircle() {
        return circlePaint != null;
    }

    public Paint getDrawnPathPaint(int i) {
        return pathPaintEvents.get(i).pathPaint;
    }

    public Path getDrawnPath(int i) {
        return pathPaintEvents.get(i).drawnPath;
    }

    private static class PaintEvents {
        private Path drawnPath;
        private Paint pathPaint;

        PaintEvents(Path drawnPath, Paint pathPaint) {
            this.drawnPath = drawnPath;
            this.pathPaint = pathPaint;
        }
    }
}
