package com.xtremelabs.robolectric.shadows;

import android.graphics.Rect;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;
import com.xtremelabs.robolectric.internal.RealObject;

import static com.xtremelabs.robolectric.Robolectric.shadowOf_;

@Implements(Rect.class)
public class ShadowRect {
    @RealObject Rect realRect;

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
    public String toString() {
        StringBuilder sb = new StringBuilder(32);
        sb.append("Rect(");
        sb.append(realRect.left);
        sb.append(", ");
        sb.append(realRect.top);
        sb.append(" - ");
        sb.append(realRect.right);
        sb.append(", ");
        sb.append(realRect.bottom);
        sb.append(")");
        return sb.toString();
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
    public void offset(int dx, int dy) {
      realRect.left += dx;
      realRect.right += dx;
      realRect.top += dy;
      realRect.bottom += dy;
    }
}
