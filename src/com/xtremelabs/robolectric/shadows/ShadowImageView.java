package com.xtremelabs.robolectric.shadows;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;
import com.xtremelabs.robolectric.util.Implementation;
import com.xtremelabs.robolectric.util.Implements;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(ImageView.class)
public class ShadowImageView extends ShadowView {
    private Drawable imageDrawable;
    private int alpha;
    private int resourceId;
    private Bitmap imageBitmap;
    private ImageView.ScaleType scaleType;

    @Override public void __constructor__(Context context, AttributeSet attributeSet) {
        super.__constructor__(context, attributeSet);
        applyImageAttribute();
    }

    @Implementation
    public void setImageBitmap(Bitmap imageBitmap) {
        setImageDrawable(new BitmapDrawable(imageBitmap));
        this.imageBitmap = imageBitmap;
    }

    @Deprecated
    public Bitmap getImageBitmap() {
        return imageBitmap;
    }

    @Implementation
    public void setImageDrawable(Drawable drawable) {
        this.imageDrawable = drawable;
    }

    @Implementation
    public void setImageResource(int resId) {
        this.resourceId = resId;
        setImageDrawable(new BitmapDrawable(BitmapFactory.decodeResource(getResources(), resId)));
    }

    @Implementation
    public void setAlpha(int alpha) {
        this.alpha = alpha;
    }

    @Implementation
    public ImageView.ScaleType getScaleType() {
        return scaleType;
    }

    @Implementation
    public void setScaleType(ImageView.ScaleType scaleType) {
        this.scaleType = scaleType;
    }

    @Implementation
    public Drawable getDrawable() {
        return imageDrawable;
    }

    /**
     * @deprecated Use android.widget.ImageView#getDrawable() instead.
     * @return the image drawable */
    @Deprecated
    public Drawable getImageDrawable() {
        return imageDrawable;
    }

    public int getAlpha() {
        return alpha;
    }

    @Deprecated
    public int getResourceId() {
        return resourceId;
    }

    private void applyImageAttribute() {
        String source = attributeSet.getAttributeValue("android", "src");
        if (source != null) {
            if (source.startsWith("@drawable/")) {
                setImageResource(attributeSet.getAttributeResourceValue("android", "src", 0));
            }
        }
    }
}
