package com.xtremelabs.robolectric.shadows;

import android.graphics.Point;
import com.xtremelabs.robolectric.util.Implementation;
import com.xtremelabs.robolectric.util.Implements;
import com.xtremelabs.robolectric.util.RealObject;

import static com.xtremelabs.robolectric.Robolectric.shadowOf_;

@Implements(Point.class)
public class ShadowPoint {
    @RealObject private Point realPoint;

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

    @Override public boolean equals(Object object) {
        if (object == null) return false;
        Object o = shadowOf_(object);
        if (o == null) return false;
        if (this == o) return true;
        if (getClass() != o.getClass()) return false;

        ShadowPoint that = (ShadowPoint) o;
        if (this.realPoint.x == that.realPoint.x && this.realPoint.y == that.realPoint.y) return true;
        
        return false;
    }

    @Override public int hashCode() {
        return realPoint.x * 32713 + realPoint.y;
    }

    @Override public String toString() {
        return "Point(" + realPoint.x + ", " + realPoint.y + ")";
    }
}
