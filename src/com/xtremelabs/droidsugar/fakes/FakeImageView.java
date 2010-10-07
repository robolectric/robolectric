package com.xtremelabs.droidsugar.fakes;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;
import com.xtremelabs.droidsugar.util.Implementation;
import com.xtremelabs.droidsugar.util.Implements;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(ImageView.class)
public class FakeImageView extends FakeView {
    public Drawable imageDrawable;
    public int alpha;
    public int resourceId;
    private Bitmap imageBitmap;
    private ImageView.ScaleType scaleType;

    public FakeImageView(ImageView view) {
        super(view);
    }

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

    public ImageView.ScaleType getScaleType() {
        return scaleType;
    }

    @Implementation
    public void setScaleType(ImageView.ScaleType scaleType) {
        this.scaleType = scaleType;
    }
}
