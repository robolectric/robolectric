package com.xtremelabs.robolectric.shadows;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.widget.ImageView;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;

@Implements(ImageView.class)
public class ShadowImageView extends ShadowView {
    private Drawable imageDrawable;
    private int alpha;
    private int resourceId;
    private Bitmap imageBitmap;
    private ImageView.ScaleType scaleType;
    private Matrix matrix;

    @Override
    public void applyAttributes() {
        super.applyAttributes();
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
        setImageDrawable(buildDrawable(resId));
    }

    /**
     * Build drawable, either LayerDrawable or BitmapDrawable.
     * 
     * @param resourceId
     *            Resource id
     * @return Drawable
     */
    protected Drawable buildDrawable(int resourceId) {
        if (isDrawableXml(resourceId)) {
            int[] resourceIds = shadowOf(Robolectric.application)
                    .getResourceLoader().getDrawableIds(resourceId);

            Drawable[] drawables = new Drawable[resourceIds.length];

            for (int i = 0; i < resourceIds.length; i++) {
                drawables[i] = buildDrawable(resourceIds[i]);
            }

            return new LayerDrawable(drawables);
        } else {
            return new BitmapDrawable(BitmapFactory.decodeResource(
                    getResources(), resourceId));
        }
    }

    /**
     * Does the resource id point to xml resource.
     * 
     * @param resourceId
     *            Resource id
     * @return Boolean
     */
    private boolean isDrawableXml(int resourceId) {
        return shadowOf(Robolectric.application).getResourceLoader()
                .isDrawableXml(resourceId);
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
     * @return the image drawable
     * @deprecated Use android.widget.ImageView#getDrawable() instead.
     */
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

    @Implementation
    public void setImageMatrix(Matrix matrix) {
        this.matrix = new Matrix(matrix);
    }

    @Implementation
    public void draw(Canvas canvas) {
        if (matrix != null) {
            canvas.translate(shadowOf(matrix).getTransX(), shadowOf(matrix)
                    .getTransY());
            canvas.scale(shadowOf(matrix).getScaleX(), shadowOf(matrix)
                    .getScaleY());
        }
        imageDrawable.draw(canvas);
    }

    private void applyImageAttribute() {
        String source = attributeSet.getAttributeValue("android", "src");
        if (source != null) {
            if (source.startsWith("@drawable/")) {
                setImageResource(attributeSet.getAttributeResourceValue(
                        "android", "src", 0));
            }
        }
    }
}
