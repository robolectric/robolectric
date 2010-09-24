package com.xtremelabs.droidsugar.fakes;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import com.xtremelabs.droidsugar.util.Implements;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(Canvas.class)
public class FakeCanvas {
    public Path drawnPath;
    public Paint pathPaint;
    public Paint circlePaint;
    public float circleCenterX;
    public float circleCenterY;
    public float circleRadius;

    public void drawPath(Path path, Paint paint) {
        drawnPath = path;
        pathPaint = paint;
    }

    public void drawCircle(float cx, float cy, float radius, Paint paint) {
        circleCenterX = cx;
        circleCenterY = cy;
        circleRadius = radius;
        circlePaint = paint;
    }
}
