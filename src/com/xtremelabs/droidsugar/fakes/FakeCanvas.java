package com.xtremelabs.droidsugar.fakes;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import com.xtremelabs.droidsugar.util.Implements;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(Canvas.class)
public class FakeCanvas {
    public Path drawnPath;
    public Paint lastPaint;

    public void drawPath(Path path, Paint paint) {
        drawnPath = path;
        lastPaint = paint;
    }
}
