package com.xtremelabs.droidsugar.fakes;

import android.graphics.Point;
import com.xtremelabs.droidsugar.util.Implementation;
import com.xtremelabs.droidsugar.util.Implements;

@Implements(Point.class)
public class FakePoint {
    private Point realPoint;

    public FakePoint(Point realPoint) {
        this.realPoint = realPoint;
    }

    public void __constructor__(int x, int y) {
        realPoint.x = x;
        realPoint.y = y;
    }

    public void __constructor__(Point src) {
        realPoint.x = src.x;
        realPoint.y = src.y;
    }

    @Implementation
    public void set(int x, int y) {
        realPoint.x = x;
        realPoint.y = y;
    }

    @Implementation
    public final void negate() {
        realPoint.x = -realPoint.x;
        realPoint.y = -realPoint.y;
    }

    @Implementation
    public final void offset(int dx, int dy) {
        realPoint.x += dx;
        realPoint.y += dy;
    }

    public final boolean equals(int x, int y) {
        return realPoint.x == x && realPoint.y == y;
    }

    @Override public boolean equals(Object o) {
        if (o instanceof Point) {
            Point p = (Point) o;
            return realPoint.x == p.x && realPoint.y == p.y;
        }
        return false;
    }

    @Override public int hashCode() {
        return realPoint.x * 32713 + realPoint.y;
    }

    @Override public String toString() {
        return "Point(" + realPoint.x + ", " + realPoint.y + ")";
    }
}
