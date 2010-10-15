package com.xtremelabs.robolectric.fakes;

import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import com.xtremelabs.robolectric.util.Implementation;
import com.xtremelabs.robolectric.util.Implements;

import java.io.InputStream;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(Drawable.class)
public class FakeDrawable {
    public Rect bounds = new Rect(0, 0, 0, 0);
    public int intrinsicWidth = -1;

    @Implementation
    public static Drawable createFromStream(InputStream is, String srcName) {
        return new BitmapDrawable();
    }

    @Implementation
    public final Rect getBounds() {
        return bounds;
    }

    @Implementation
    public int getIntrinsicWidth() {
        return intrinsicWidth;
    }

}
