package com.xtremelabs.robolectric.shadows;

import android.graphics.Rect;
import android.graphics.RectF;
import android.util.FloatMath;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;
import com.xtremelabs.robolectric.internal.RealObject;

/**
 * Shadow implementation of {@code RectF}
 * 
 * (lifted from Android RectF.)
 */
@SuppressWarnings({"UnusedDeclaration"})
@Implements(RectF.class)
public class ShadowRectF {
    @RealObject private RectF realRectF;

    public void __constructor__(float left, float top, float right, float bottom) {
        realRectF.left = left;
        realRectF.top = top;
        realRectF.right = right;
        realRectF.bottom = bottom;
    }

    public void __constructor__(RectF r) {
        realRectF.left = r.left;
        realRectF.top = r.top;
        realRectF.right = r.right;
        realRectF.bottom = r.bottom;
    }

    public void __constructor__(Rect r) {
        realRectF.left = r.left;
        realRectF.top = r.top;
        realRectF.right = r.right;
        realRectF.bottom = r.bottom;
    }

    @Implementation
    public String toString() {
        return "RectF(" + realRectF.left + ", " + realRectF.top + ", "
                + realRectF.right + ", " + realRectF.bottom + ")";
    }

    @Implementation
    public final boolean isEmpty() {
        return realRectF.left >= realRectF.right || realRectF.top >= realRectF.bottom;
    }

    @Implementation
    public final float width() {
        return realRectF.right - realRectF.left;
    }

    @Implementation
    public final float height() {
        return realRectF.bottom - realRectF.top;
    }

    @Implementation
    public final float centerX() {
        return (realRectF.left + realRectF.right) * 0.5f;
    }

    @Implementation
    public final float centerY() {
        return (realRectF.top + realRectF.bottom) * 0.5f;
    }

    @Implementation
    public void setEmpty() {
        realRectF.left = realRectF.right = realRectF.top = realRectF.bottom = 0;
    }

    @Implementation
    public void set(float left, float top, float right, float bottom) {
        realRectF.left = left;
        realRectF.top = top;
        realRectF.right = right;
        realRectF.bottom = bottom;
    }

    @Implementation
    public void set(RectF src) {
        realRectF.left = src.left;
        realRectF.top = src.top;
        realRectF.right = src.right;
        realRectF.bottom = src.bottom;
    }

    @Implementation
    public void set(Rect src) {
        realRectF.left = src.left;
        realRectF.top = src.top;
        realRectF.right = src.right;
        realRectF.bottom = src.bottom;
    }

    @Implementation
    public void offset(float dx, float dy) {
        realRectF.left += dx;
        realRectF.top += dy;
        realRectF.right += dx;
        realRectF.bottom += dy;
    }

    @Implementation
    public void offsetTo(float newLeft, float newTop) {
        realRectF.right += newLeft - realRectF.left;
        realRectF.bottom += newTop - realRectF.top;
        realRectF.left = newLeft;
        realRectF.top = newTop;
    }

    @Implementation
    public void inset(float dx, float dy) {
        realRectF.left += dx;
        realRectF.top += dy;
        realRectF.right -= dx;
        realRectF.bottom -= dy;
    }

    @Implementation
    public boolean contains(float x, float y) {
        return realRectF.left < realRectF.right && realRectF.top < realRectF.bottom  // check for empty first
                && x >= realRectF.left && x < realRectF.right && y >= realRectF.top && y < realRectF.bottom;
    }

    @Implementation
    public boolean contains(float left, float top, float right, float bottom) {
        return realRectF.left < realRectF.right && realRectF.top < realRectF.bottom
                && realRectF.left <= left && realRectF.top <= top
                && realRectF.right >= right && realRectF.bottom >= bottom;
    }

    @Implementation
    public boolean contains(RectF r) {
        return realRectF.left < realRectF.right && realRectF.top < realRectF.bottom
                && realRectF.left <= r.left && realRectF.top <= r.top
                && realRectF.right >= r.right && realRectF.bottom >= r.bottom;
    }

    @Implementation
    public boolean intersect(float left, float top, float right, float bottom) {
        if (realRectF.left < right && left < realRectF.right
                && realRectF.top < bottom && top < realRectF.bottom) {
            if (realRectF.left < left) {
                realRectF.left = left;
            }
            if (realRectF.top < top) {
                realRectF.top = top;
            }
            if (realRectF.right > right) {
                realRectF.right = right;
            }
            if (realRectF.bottom > bottom) {
                realRectF.bottom = bottom;
            }
            return true;
        }
        return false;
    }

    @Implementation
    public boolean intersect(RectF r) {
        return intersect(r.left, r.top, r.right, r.bottom);
    }

    @Implementation
    public boolean setIntersect(RectF a, RectF b) {
        if (a.left < b.right && b.left < a.right
                && a.top < b.bottom && b.top < a.bottom) {
            realRectF.left = Math.max(a.left, b.left);
            realRectF.top = Math.max(a.top, b.top);
            realRectF.right = Math.min(a.right, b.right);
            realRectF.bottom = Math.min(a.bottom, b.bottom);
            return true;
        }
        return false;
    }

    @Implementation
    public boolean intersects(float left, float top, float right, float bottom) {
        return realRectF.left < right && left < realRectF.right
                && realRectF.top < bottom && top < realRectF.bottom;
    }

    @Implementation
    public static boolean intersects(RectF a, RectF b) {
        return a.left < b.right && b.left < a.right
                && a.top < b.bottom && b.top < a.bottom;
    }

    @Implementation
    public void round(Rect dst) {
        dst.set(Math.round(realRectF.left), Math.round(realRectF.top),
                Math.round(realRectF.right), Math.round(realRectF.bottom));
    }

    @Implementation
    public void roundOut(Rect dst) {
        dst.set((int) FloatMath.floor(realRectF.left), (int) FloatMath.floor(realRectF.top),
                (int) FloatMath.ceil(realRectF.right), (int) FloatMath.ceil(realRectF.bottom));
    }

    @Implementation
    public void union(float left, float top, float right, float bottom) {
        if ((left < right) && (top < bottom)) {
            if ((realRectF.left < realRectF.right) && (realRectF.top < realRectF.bottom)) {
                if (realRectF.left > left)
                    realRectF.left = left;
                if (realRectF.top > top)
                    realRectF.top = top;
                if (realRectF.right < right)
                    realRectF.right = right;
                if (realRectF.bottom < bottom)
                    realRectF.bottom = bottom;
            } else {
                realRectF.left = left;
                realRectF.top = top;
                realRectF.right = right;
                realRectF.bottom = bottom;
            }
        }
    }

    @Implementation
    public void union(RectF r) {
        union(r.left, r.top, r.right, r.bottom);
    }

    @Implementation
    public void union(float x, float y) {
        if (x < realRectF.left) {
            realRectF.left = x;
        } else if (x > realRectF.right) {
            realRectF.right = x;
        }
        if (y < realRectF.top) {
            realRectF.top = y;
        } else if (y > realRectF.bottom) {
            realRectF.bottom = y;
        }
    }

    @Implementation
    public void sort() {
        if (realRectF.left > realRectF.right) {
            float temp = realRectF.left;
            realRectF.left = realRectF.right;
            realRectF.right = temp;
        }
        if (realRectF.top > realRectF.bottom) {
            float temp = realRectF.top;
            realRectF.top = realRectF.bottom;
            realRectF.bottom = temp;
        }
    }
}
