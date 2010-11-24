package com.xtremelabs.robolectric.shadows;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import com.xtremelabs.robolectric.util.Implementation;
import com.xtremelabs.robolectric.util.Implements;
import com.xtremelabs.robolectric.util.RealObject;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(BitmapDrawable.class)
public class ShadowBitmapDrawable extends ShadowDrawable {
    int loadedFromResourceId;

    @RealObject private BitmapDrawable realBitmapDrawable;

    /**
     * Draws the contained bitmap onto the canvas at 0,0 with a default {@code Paint}
     *
     * @param canvas the canvas to draw on
     */
    @Implementation
    public void draw(Canvas canvas) {
        canvas.drawBitmap(realBitmapDrawable.getBitmap(), 0, 0, new Paint());
    }

    /**
     * Non-Android accessor that tells you the resource id that this {@code BitmapDrawable} was loaded from. This lets
     * your tests assert that the bitmap is correct without having to actually load the bitmap.
     * 
     * @return resource id from which this {@code BitmapDrawable} was loaded
     */
    public int getLoadedFromResourceId() {
        return loadedFromResourceId;
    }
}
