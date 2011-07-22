package com.xtremelabs.robolectric.res.drawable;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;

import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;

import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.res.ResourceLoader;

/**
 * DrawableBuilder
 */
public class DrawableBuilder {
    /** drawable factory */
    protected DrawableFactory drawableFactory;

    /**
     * DrawableBuilder constructor.
     * @param drawableFactory DrawableFactory
     */
    public DrawableBuilder(DrawableFactory drawableFactory) {
        this.drawableFactory = drawableFactory;
    }

    /**
     * Get resource loader.
     * @return ResourceLoader
     */
    protected ResourceLoader getResourceLoader() {
        return shadowOf(Robolectric.application).getResourceLoader();
    }

    /**
     * Get resources.
     * @return Resources
     */
    protected Resources getResources() {
        return Robolectric.application.getResources();
    }

    /**
     * Build drawable.
     * @param resourceId Resource id
     * @return Drawable
     */
    public Drawable build(int resourceId) {
        if (isDrawableXml(resourceId)) {
            return buildLayerDrawable(resourceId);
        } else {
            return buildBitmapDrawable(resourceId);
        }
    }

    /**
     * Check if resource id points to drawable xml definition.
     * @param resourceId Resource id
     * @return Boolean
     */
    protected boolean isDrawableXml(int resourceId) {
        return getResourceLoader().isDrawableXml(resourceId);
    }

    /**
     * Build bitmap drawable.
     * @param resourceId Resource id
     * @return BitmapDrawable
     */
    protected BitmapDrawable buildBitmapDrawable(int resourceId) {
        return new BitmapDrawable(BitmapFactory.decodeResource(getResources(),
                resourceId));
    }

    /**
     * Build layer drawable.
     * @param resourceId Resource id
     * @return LayerDrawable
     */
    protected LayerDrawable buildLayerDrawable(int resourceId) {
        int[] resourceIds = getResourceLoader().getDrawableIds(resourceId);
        Drawable[] drawables = new Drawable[resourceIds.length];

        for (int i = 0; i < resourceIds.length; i++) {
            drawables[i] = drawableFactory.getDrawable(resourceIds[i]);
        }

        return new LayerDrawable(drawables);
    }
}
