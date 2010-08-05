package com.xtremelabs.droidsugar.fakes;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

@SuppressWarnings({"ALL"})
public class FakeImageView extends FakeView {
    public Drawable imageDrawable;
    public int alpha;
    public int resourceId;
    private Bitmap imageBitmap;
    private ImageView.ScaleType scaleType;

    public FakeImageView(ImageView view) {
        super(view);
    }

    public void setImageBitmap(Bitmap imageBitmap) {
        this.imageBitmap = imageBitmap;
    }

    public Bitmap getImageBitmap() {
        return imageBitmap;
    }

    public void setImageDrawable(Drawable drawable) {
        this.imageDrawable = drawable;
    }

    public void setImageResource(int resId) {
        this.resourceId = resId;
    }

    public void setAlpha(int alpha) {
        this.alpha = alpha;
    }

    public ImageView.ScaleType getScaleType() {
        return scaleType;
    }

    public void setScaleType(ImageView.ScaleType scaleType) {
        this.scaleType = scaleType;
    }
}
