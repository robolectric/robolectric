package com.xtremelabs.robolectric.res.drawable;

import java.util.HashMap;
import java.util.Map;

import android.graphics.drawable.Drawable;

import com.xtremelabs.robolectric.res.ResourceExtractor;

/**
 * DrawableFactory
 * 
 * All drawables ought to be created and cached references stored here.
 */
public class DrawableFactory {
    /** cache: by id */
    protected Map<Integer, Drawable> cacheById = new HashMap<Integer, Drawable>();

    /** resource extractor */
    protected ResourceExtractor resourceExtractor;

    /**
     * DrawableFactory constructor.
     * @param resourceExtractor Resource extractor
     */
    public DrawableFactory(ResourceExtractor resourceExtractor) {
        this.resourceExtractor = resourceExtractor;
    }

    /**
     * Get drawable by key.
     * @param key Key
     * @return Drawable
     */
    public Drawable getDrawable(String key) {
        Integer resourceId = resourceExtractor.getResourceId(key);
        if (resourceId == null) {
            throw new IllegalArgumentException("Unknown resource: " + key);
        } else {
            return getDrawable(resourceId);
        }
    }

    /**
     * Get drawable by resource id.
     * @param resourceId Resource id
     * @return Drawable
     */
    public Drawable getDrawable(int resourceId) {
        if (contains(resourceId)) {
            return hit(resourceId);
        } else {
            return miss(resourceId);
        }
    }

    /**
     * Check if cache contains resource id.
     * @param resourceId Resource id
     * @return Contains
     */
    protected boolean contains(int resourceId) {
        return cacheById.containsKey(resourceId);
    }

    /**
     * Get drawable from cache.
     * @param resourceId Resource id
     * @return Drawable
     */
    protected Drawable hit(int resourceId) {
        return cacheById.get(resourceId);
    }

    /**
     * Build drawable, add to cache, return drawable.
     * @param resourceId Resource id
     * @return Drawable
     */
    protected Drawable miss(int resourceId) {
        String key = resourceExtractor.getResourceName(resourceId);
        if (key == null) {
            throw new IllegalArgumentException("Unknown resource: "
                    + resourceId);
        }

        // TODO: build drawable
        return null;
    }
}
