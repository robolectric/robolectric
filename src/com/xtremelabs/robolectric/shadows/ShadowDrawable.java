package com.xtremelabs.robolectric.shadows;

import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import com.xtremelabs.robolectric.util.Implementation;
import com.xtremelabs.robolectric.util.Implements;

import java.io.InputStream;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(Drawable.class)
public class ShadowDrawable {
    private Rect bounds = new Rect(0, 0, 0, 0);
    private int intrinsicWidth = -1;
    private int intrinsicHeight = -1;

    @Implementation
    public static Drawable createFromStream(InputStream is, String srcName) {
        return new BitmapDrawable();
    }

    @Implementation
    public final Rect getBounds() {
        return bounds;
    }

    public void setBounds(Rect rect) {
        this.bounds = rect;
    }

    @Implementation
    public void setBounds(int left, int top, int right, int bottom) {
        bounds = new Rect(left, top, right, bottom);
    }

    @Implementation
    public int getIntrinsicWidth() {
        return intrinsicWidth;
    }

    @Implementation
    public int getIntrinsicHeight() {
        return intrinsicHeight;
    }

    public void setIntrinsicWidth(int intrinsicWidth) {
        this.intrinsicWidth = intrinsicWidth;
    }

    public void setIntrinsicHeight(int intrinsicHeight) {
        this.intrinsicHeight = intrinsicHeight;
    }
}
