package com.xtremelabs.robolectric.shadows;

import android.graphics.Rect;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;
import com.xtremelabs.robolectric.internal.RealObject;

import java.io.PrintWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.xtremelabs.robolectric.Robolectric.shadowOf_;

@Implements(Rect.class)
public class ShadowRect {
    @RealObject Rect realRect;

    private static final Pattern FLATTENED_PATTERN = Pattern.compile(
            "(-?\\d+) (-?\\d+) (-?\\d+) (-?\\d+)");

    public void __constructor__(int left, int top, int right, int bottom) {
        realRect.left = left;
        realRect.top = top;
        realRect.right = right;
        realRect.bottom = bottom;
    }

    public void __constructor__(Rect otherRect) {
        realRect.left = otherRect.left;
        realRect.top = otherRect.top;
        realRect.right = otherRect.right;
        realRect.bottom = otherRect.bottom;
    }

    @Implementation    
    public void set(Rect rect) {
        set(rect.left, rect.top, rect.right, rect.bottom);
    }
    
    @Implementation
    public void set(int left, int top, int right, int bottom) {
        realRect.left = left;
        realRect.top = top;
        realRect.right = right;
        realRect.bottom = bottom;
    }

    @Implementation
    public int width() {
        return realRect.right - realRect.left;
    }

    @Implementation
    public int height() {
        return realRect.bottom - realRect.top;
    }

    @Implementation
    public final int centerX() {
        return (realRect.left + realRect.right) >> 1;
    }

    @Implementation
    public final int centerY() {
        return (realRect.top + realRect.bottom) >> 1;
    }

    @Implementation
    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        Object o = shadowOf_(obj);
        if (o == null) return false;
        if (getClass() != o.getClass()) return false;
        if (this == o) return true;

        Rect r = (Rect) obj;
        return realRect.left == r.left && realRect.top == r.top && realRect.right == r.right
                && realRect.bottom == r.bottom;
    }

    @Implementation
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(32);
        sb.append("Rect("); sb.append(realRect.left); sb.append(", ");
        sb.append(realRect.top); sb.append(" - "); sb.append(realRect.right);
        sb.append(", "); sb.append(realRect.bottom); sb.append(")");
        return sb.toString();
    }

    @Implementation
    public String toShortString() {
        return toShortString(new StringBuilder(32));
    }
    
    public String toShortString(StringBuilder sb) {
        sb.setLength(0);
        sb.append('['); sb.append(realRect.left); sb.append(',');
        sb.append(realRect.top); sb.append("]["); sb.append(realRect.right);
        sb.append(','); sb.append(realRect.bottom); sb.append(']');
        return sb.toString();
    }

    @Implementation
    public String flattenToString() {
        StringBuilder sb = new StringBuilder(32);
        // WARNING: Do not change the format of this string, it must be
        // preserved because Rects are saved in this flattened format.
        sb.append(realRect.left);
        sb.append(' ');
        sb.append(realRect.top);
        sb.append(' ');
        sb.append(realRect.right);
        sb.append(' ');
        sb.append(realRect.bottom);
        return sb.toString();
    }

    @Implementation
    public static Rect unflattenFromString(String str) {
        Matcher matcher = FLATTENED_PATTERN.matcher(str);
        if (!matcher.matches()) {
            return null;
        }
        return new Rect(Integer.parseInt(matcher.group(1)),
                Integer.parseInt(matcher.group(2)),
                Integer.parseInt(matcher.group(3)),
                Integer.parseInt(matcher.group(4)));
    }
    
    public void printShortString(PrintWriter pw) {
        pw.print('['); pw.print(realRect.left); pw.print(',');
        pw.print(realRect.top); pw.print("]["); pw.print(realRect.right);
        pw.print(','); pw.print(realRect.bottom); pw.print(']');
    }
    
    @Implementation
    public final boolean isEmpty() {
        return realRect.left >= realRect.right || realRect.top >= realRect.bottom;
    }

    @Implementation
    public final float exactCenterX() {
        return (realRect.left + realRect.right) * 0.5f;
    }
    
    @Implementation
    public final float exactCenterY() {
        return (realRect.top + realRect.bottom) * 0.5f;
    }

    @Implementation
    public void setEmpty() {
        realRect.left = realRect.right = realRect.top = realRect.bottom = 0;
    }

    @Implementation
    public void offset(int dx, int dy) {
        realRect.left += dx;
        realRect.top += dy;
        realRect.right += dx;
        realRect.bottom += dy;
    }

    @Implementation
    public void offsetTo(int newLeft, int newTop) {
        realRect.right += newLeft - realRect.left;
        realRect.bottom += newTop - realRect.top;
        realRect.left = newLeft;
        realRect.top = newTop;
    }

    @Implementation
    public void inset(int dx, int dy) {
        realRect.left += dx;
        realRect.top += dy;
        realRect.right -= dx;
        realRect.bottom -= dy;
    }

    @Implementation
    public boolean contains(int x, int y) {
        return x > realRect.left && x < realRect.right
                && y >= realRect.top && y <= realRect.bottom;
    }

    @Implementation
    public boolean contains(Rect r) {
        return equals(r)
                || (contains(r.left, r.top) && contains(r.right, r.top)
                && contains(r.left, r.bottom) && contains(r.right, r.bottom));
    }

    @Implementation
    public static boolean intersects(Rect a, Rect b) {
        return a.left < b.right && b.left < a.right
                && a.top < b.bottom && b.top < a.bottom;
    }

    @Implementation
    public boolean intersect(Rect r) {
        return intersects(realRect, r);
    }

    @Implementation
    public boolean intersect(int left, int top, int right, int bottom) {
        return intersect(new Rect(left, top, right, bottom));
    }

    @Implementation
    public boolean contains(int left, int top, int right, int bottom) {
               // check for empty first
        return realRect.left < realRect.right && realRect.top < realRect.bottom
               // now check for containment
                && realRect.left <= left && realRect.top <= top
                && realRect.right >= right && realRect.bottom >= bottom;
    }

    @Implementation
    public boolean setIntersect(Rect a, Rect b) {
        if (a.left < b.right && b.left < a.right
                && a.top < b.bottom && b.top < a.bottom) {
            realRect.left = Math.max(a.left, b.left);
            realRect.top = Math.max(a.top, b.top);
            realRect.right = Math.min(a.right, b.right);
            realRect.bottom = Math.min(a.bottom, b.bottom);
            return true;
        }
        return false;
    }

    @Implementation
    public boolean intersects(int left, int top, int right, int bottom) {
        return realRect.left < right && left < realRect.right
               && realRect.top < bottom && top < realRect.bottom;
    }

    @Implementation
    public void union(int left, int top, int right, int bottom) {
        if ((left < right) && (top < bottom)) {
            if ((realRect.left < realRect.right) && (realRect.top < realRect.bottom)) {
                if (realRect.left > left)
                    realRect.left = left;
                if (realRect.top > top)
                    realRect.top = top;
                if (realRect.right < right)
                    realRect.right = right;
                if (realRect.bottom < bottom)
                    realRect.bottom = bottom;
            } else {
                realRect.left = left;
                realRect.top = top;
                realRect.right = right;
                realRect.bottom = bottom;
            }
        }
    }

    @Implementation
    public void union(Rect r) {
        union(r.left, r.top, r.right, r.bottom);
    }
    
    @Implementation
    public void union(int x, int y) {
        if (x < realRect.left) {
            realRect.left = x;
        } else if (x > realRect.right) {
            realRect.right = x;
        }
        if (y < realRect.top) {
            realRect.top = y;
        } else if (y > realRect.bottom) {
            realRect.bottom = y;
        }
    }

    @Implementation
    public void sort() {
        if (realRect.left > realRect.right) {
            int temp = realRect.left;
            realRect.left = realRect.right;
            realRect.right = temp;
        }
        if (realRect.top > realRect.bottom) {
            int temp = realRect.top;
            realRect.top = realRect.bottom;
            realRect.bottom = temp;
        }
    }

    public void scale(float scale) {
        if (scale != 1.0f) {
            realRect.left = (int) (realRect.left * scale + 0.5f);
            realRect.top = (int) (realRect.top * scale + 0.5f);
            realRect.right = (int) (realRect.right * scale + 0.5f);
            realRect.bottom = (int) (realRect.bottom * scale + 0.5f);
        }
    }
}
