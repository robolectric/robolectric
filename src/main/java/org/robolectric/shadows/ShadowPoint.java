package com.xtremelabs.robolectric.shadows;

import android.graphics.Point;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;
import com.xtremelabs.robolectric.internal.RealObject;

import static com.xtremelabs.robolectric.Robolectric.shadowOf_;

/**
 * Shadow implementation of {@code Point}
 */
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

    @Override @Implementation
    public boolean equals(Object object) {
        if (object == null) return false;
        Object o = shadowOf_(object);
        if (o == null) return false;
        if (this == o) return true;
        if (getClass() != o.getClass()) return false;

        ShadowPoint that = (ShadowPoint) o;
        if (this.realPoint.x == that.realPoint.x && this.realPoint.y == that.realPoint.y) return true;

        return false;
    }

    @Override @Implementation
    public int hashCode() {
        return realPoint.x * 32713 + realPoint.y;
    }

    @Override @Implementation
    public String toString() {
        return "Point(" + realPoint.x + ", " + realPoint.y + ")";
    }

    /**
     * Non-Android utility method for comparing a point to a well-known value
     *
     * @param x x
     * @param y y
     * @return this.x == x && this.y == y
     */
    @Implementation
    public final boolean equals(int x, int y) {
        return realPoint.x == x && realPoint.y == y;
    }
}
