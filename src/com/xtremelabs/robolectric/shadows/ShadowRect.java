package com.xtremelabs.robolectric.shadows;

import android.graphics.Rect;
import com.xtremelabs.robolectric.util.Implementation;
import com.xtremelabs.robolectric.util.Implements;
import com.xtremelabs.robolectric.util.RealObject;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(Rect.class)
public class ShadowRect {
    @RealObject Rect realRect;

    public void __constructor__(int left, int top, int right, int bottom) {
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
    public boolean equals(Object obj) {
        Rect r = (Rect) obj;
        if (r != null) {
            return realRect.left == r.left && realRect.top == r.top && realRect.right == r.right
                    && realRect.bottom == r.bottom;
        }
        return false;
    }

    @Implementation
    public String toString() {
        StringBuilder sb = new StringBuilder(32);
        sb.append("Rect("); sb.append(realRect.left); sb.append(", ");
        sb.append(realRect.top); sb.append(" - "); sb.append(realRect.right);
        sb.append(", "); sb.append(realRect.bottom); sb.append(")");
        return sb.toString();
    }
}
