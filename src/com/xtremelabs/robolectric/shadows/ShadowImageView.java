package com.xtremelabs.robolectric.shadows;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;
import com.xtremelabs.robolectric.res.ViewLoader;
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

    @Implementation
    public void setImageBitmap(Bitmap imageBitmap) {
        this.imageBitmap = imageBitmap;
    }

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

    public Drawable getImageDrawable() {
        return imageDrawable;
    }

    public int getAlpha() {
        return alpha;
    }

    public int getResourceId() {
        return resourceId;
    }

    @Override public void applyViewNodeAttributes(ViewLoader.ViewNode viewNode) {
        super.applyViewNodeAttributes(viewNode);
        applyImageAttribute(viewNode);
    }

    private void applyImageAttribute(ViewLoader.ViewNode viewNode) {
        String source = viewNode.getAttributeValue("android:src");
        if (source != null) {
            if (source.startsWith("@drawable/")) {
                Integer resId = viewNode.getResourceId(source);
                setImageResource(resId);
            }
        }
    }
}
