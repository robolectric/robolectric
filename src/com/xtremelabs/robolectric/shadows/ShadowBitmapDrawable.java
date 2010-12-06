package com.xtremelabs.robolectric.shadows;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import com.xtremelabs.robolectric.util.Implementation;
import com.xtremelabs.robolectric.util.Implements;
import com.xtremelabs.robolectric.util.RealObject;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(BitmapDrawable.class)
public class ShadowBitmapDrawable extends ShadowDrawable {
    int loadedFromResourceId;
    private Bitmap bitmap;

    @RealObject private BitmapDrawable realBitmapDrawable;

    public void __constructor__(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    /**
     * Draws the contained bitmap onto the canvas at 0,0 with a default {@code Paint}
     *
     * @param canvas the canvas to draw on
     */
    @Implementation
    public void draw(Canvas canvas) {
        canvas.drawBitmap(realBitmapDrawable.getBitmap(), 0, 0, new Paint());
    }

    @Implementation
    public android.graphics.Bitmap getBitmap() {
        return bitmap;
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

    @Override @Implementation
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != ShadowBitmapDrawable.class) return false;

        ShadowBitmapDrawable that = shadowOf((BitmapDrawable) o);

        if (bitmap != null ? !bitmap.equals(that.bitmap) : that.bitmap != null) return false;

        return true;
    }

    @Override @Implementation
    public int hashCode() {
        return bitmap != null ? bitmap.hashCode() : 0;
    }

    @Override @Implementation
    public String toString() {
        return "ShadowBitmapDrawable{" +
                "bitmap=" + bitmap +
                '}';
    }
}
