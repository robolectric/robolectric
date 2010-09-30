package com.xtremelabs.droidsugar.fakes;

import android.graphics.Rect;
import com.xtremelabs.droidsugar.util.Implements;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(Rect.class)
public class FakeRect {
    private int left;
    private int top;
    private int right;
    private int bottom;

    public void __constructor__(int left, int top, int right, int bottom) {
        this.left = left;
        this.top = top;
        this.right = right;
        this.bottom = bottom;
    }

    public int width() {
        return right - left;
    }

    public int height() {
        return bottom - top;
    }
}
