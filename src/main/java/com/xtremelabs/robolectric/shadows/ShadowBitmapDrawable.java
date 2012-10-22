package com.xtremelabs.robolectric.shadows;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;

import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;
import com.xtremelabs.robolectric.internal.RealObject;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(BitmapDrawable.class)
public class ShadowBitmapDrawable extends ShadowDrawable {
    private Bitmap bitmap;
    private ColorFilter colorFilter;
    private String drawableCreateFromStreamSource;
    private String drawableCreateFromPath;

    @RealObject private BitmapDrawable realBitmapDrawable;
    private TileMode tileModeX;
    private TileMode tileModeY;

    public void __constructor__(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public void __constructor__(Resources resources, Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    /**
     * Draws the contained bitmap onto the canvas at 0,0 with a default {@code Paint}
     *
     * @param canvas the canvas to draw on
     */
    @Implementation
    public void draw(Canvas canvas) {
        Paint paint = new Paint();
        paint.setColorFilter(colorFilter);
        canvas.drawBitmap(realBitmapDrawable.getBitmap(), 0, 0, paint);
    }

    @Implementation
    public void setColorFilter(android.graphics.ColorFilter colorFilter) {
        this.colorFilter = colorFilter;
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
     * @deprecated use com.xtremelabs.robolectric.shadows.ShadowBitmap#getLoadedFromResourceId() instead.
     */
    @Deprecated
    @Override
    public int getLoadedFromResourceId() {
        return shadowOf(bitmap).getLoadedFromResourceId();
    }

    // Used by ShadowDrawable.createFromStream()
    public void setSource(String drawableCreateFromStreamSource) {
        this.drawableCreateFromStreamSource = drawableCreateFromStreamSource;
    }

    public String getSource() {
        return drawableCreateFromStreamSource;
    }

    //Used by ShadowDrawable.createFromPath()
    public void setPath(String drawableCreateFromPath) {
        this.drawableCreateFromPath = drawableCreateFromPath;
    }

    public String getPath() {
        return drawableCreateFromPath;
    }

    @Implementation
    public void setTileModeX(TileMode mode) {
        tileModeX = mode;
    }

    @Implementation
    public TileMode getTileModeX() {
        return tileModeX;
    }

    @Implementation
    public void setTileModeY(TileMode mode) {
        tileModeY = mode;
    }

    @Implementation
    public TileMode getTileModeY() {
        return tileModeY;
    }

    @Implementation
    public void setTileModeXY(TileMode modeX, TileMode modeY) {
        setTileModeX(modeX);
        setTileModeY(modeY);
    }

    @Override
    @Implementation
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != ShadowBitmapDrawable.class) return false;

        ShadowBitmapDrawable that = shadowOf((BitmapDrawable) o);

        if (bitmap != null ? !bitmap.equals(that.bitmap) : that.bitmap != null) return false;

        return super.equals(o);
    }

    @Override
    @Implementation
    public int hashCode() {
        return bitmap != null ? bitmap.hashCode() : 0;
    }

    @Override
    @Implementation
    public String toString() {
        return "ShadowBitmapDrawable{" +
                "bitmap=" + bitmap +
                '}';
    }
}
