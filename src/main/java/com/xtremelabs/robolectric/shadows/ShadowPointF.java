package com.xtremelabs.robolectric.shadows;

import android.graphics.Point;
import android.graphics.PointF;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;
import com.xtremelabs.robolectric.internal.RealObject;

import static com.xtremelabs.robolectric.Robolectric.shadowOf_;

/**
 * Shadow implementation of {@code Point}
 */
@SuppressWarnings({"UnusedDeclaration"})
@Implements(PointF.class)
public class ShadowPointF {
    @RealObject private PointF realPointF;

    public void __constructor__(float x, float y) {
        realPointF.x = x;
        realPointF.y = y;
    }

    public void __constructor__(Point src) {
        realPointF.x = src.x;
        realPointF.y = src.y;
    }

    @Implementation
    public void set(float x, float y) {
        realPointF.x = x;
        realPointF.y = y;
    }

    @Implementation
    public final void negate() {
        realPointF.x = -realPointF.x;
        realPointF.y = -realPointF.y;
    }

    @Implementation
    public final void offset(float dx, float dy) {
        realPointF.x += dx;
        realPointF.y += dy;
    }

    @Override @Implementation
    public boolean equals(Object object) {
        if (object == null) return false;
        Object o = shadowOf_(object);
        if (o == null) return false;
        if (this == o) return true;
        if (getClass() != o.getClass()) return false;

        ShadowPointF that = (ShadowPointF) o;
        if (this.realPointF.x == that.realPointF.x && this.realPointF.y == that.realPointF.y) return true;

        return false;
    }

    @Override @Implementation
    public int hashCode() {
        return (int) (realPointF.x * 32713 + realPointF.y);
    }

    @Override @Implementation
    public String toString() {
        return "Point(" + realPointF.x + ", " + realPointF.y + ")";
    }

    /**
     * Non-Android utility method for comparing a point to a well-known value
     *
     * @param x x
     * @param y y
     * @return this.x == x && this.y == y
     */
    @Implementation
    public final boolean equals(float x, float y) {
        return realPointF.x == x && realPointF.y == y;
    }
}
