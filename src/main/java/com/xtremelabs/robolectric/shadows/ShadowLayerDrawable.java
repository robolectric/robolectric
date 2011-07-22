package com.xtremelabs.robolectric.shadows;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;

import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;
import com.xtremelabs.robolectric.internal.RealObject;

@Implements(LayerDrawable.class)
public class ShadowLayerDrawable {
    @RealObject
    protected LayerDrawable realLayerDrawable;

    protected Drawable[] drawables;

    public void __constructor__(Drawable[] drawables) {
        this.drawables = drawables;
    }

    @Implementation
    public int getNumberOfLayers() {
        return drawables.length;
    }

    @Implementation
    public boolean setDrawableByLayerId(int resourceId, Drawable replacer) {
        Drawable target = Robolectric.application.getResources().getDrawable(
                resourceId);

        for (int i = 0; i < drawables.length; i++) {
            if (drawables[i] == target) {
                drawables[i] = replacer;
                return true;
            }
        }

        return false;
    }
}
