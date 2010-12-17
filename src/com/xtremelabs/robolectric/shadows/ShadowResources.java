package com.xtremelabs.robolectric.shadows;

import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.res.ResourceLoader;
import com.xtremelabs.robolectric.util.Implementation;
import com.xtremelabs.robolectric.util.Implements;

import java.io.InputStream;
import java.util.Locale;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;

/**
 * Shadow of {@code Resources} that simulates the loading of resources
 *
 * @see com.xtremelabs.robolectric.RobolectricTestRunner#RobolectricTestRunner(Class, String, String)
 */
@SuppressWarnings({"UnusedDeclaration"})
@Implements(Resources.class)
public class ShadowResources {
    static Resources bind(Resources resources, ResourceLoader resourceLoader) {
        ShadowResources shadowResources = shadowOf(resources);
        if (shadowResources.resourceLoader != null) throw new RuntimeException("ResourceLoader already set!");
        shadowResources.resourceLoader = resourceLoader;
        return resources;
    }

    private ResourceLoader resourceLoader;

    @Implementation
    public int getColor(int id) throws Resources.NotFoundException {
        return resourceLoader.getColorValue(id);
    }

    @Implementation
    public String getString(int id) throws Resources.NotFoundException {
        return resourceLoader.getStringValue(id);
    }

    @Implementation
    public String getString(int id, Object... formatArgs) throws Resources.NotFoundException {
        String raw = getString(id);
        return String.format(Locale.ENGLISH, raw, formatArgs);
    }

    @Implementation
    public InputStream openRawResource(int id) throws Resources.NotFoundException {
    	return resourceLoader.getRawValue(id);
    }
    
    @Implementation
    public String[] getStringArray(int id) throws Resources.NotFoundException {
        String[] arrayValue = resourceLoader.getStringArrayValue(id);
        if (arrayValue == null) {
            throw new Resources.NotFoundException();
        }
        return arrayValue;
    }

    @Implementation
    public CharSequence getText(int id) throws Resources.NotFoundException {
        return getString(id);
    }

    @Implementation
    public DisplayMetrics getDisplayMetrics() {
        return new DisplayMetrics();
    }

    @Implementation
    public Drawable getDrawable(int drawableResourceId) throws Resources.NotFoundException {
        RobolectricBitmapDrawable bitmapDrawable = new RobolectricBitmapDrawable(drawableResourceId);
        ShadowBitmapDrawable shadowBitmapDrawable = shadowOf(bitmapDrawable);
        shadowBitmapDrawable.loadedFromResourceId = drawableResourceId;
        return bitmapDrawable;
    }

    @Implementation
    public float getDimension(int id) throws Resources.NotFoundException {
        // todo: get this value from the xml resources and scale it by display metrics [xw 20101011]
        if (resourceLoader.dimensions.containsKey(id)) {
            return resourceLoader.dimensions.get(id);
        }
        return id - 0x7f000000;
    }

    @Implementation
    public int getDimensionPixelSize(int id) throws Resources.NotFoundException {
        // The int value returned from here is probably going to be handed to TextView.setTextSize(),
        // which takes a float. Avoid int-to-float conversion errors by returning a value generated from this
        // resource ID but which isn't too big (resource values in R.java are all greater than 0x7f000000).

        return (int) getDimension(id);
    }

    @Implementation
    public int getDimensionPixelOffset(int id) throws Resources.NotFoundException {
        return (int) getDimension(id);
    }

    @Implementation
    public AssetManager getAssets(){
        return Robolectric.newInstanceOf(AssetManager.class);
    }

    /**
     * Non-Android accessor that sets the value to be returned by {@link #getDimension(int)}
     *
     * @param id ID to set the dimension for
     * @param value value to be returned
     */
    public void setDimension(int id, int value) {
        resourceLoader.dimensions.put(id, value);
    }

    private static class RobolectricBitmapDrawable extends BitmapDrawable {
        private int drawableResourceId;

        public RobolectricBitmapDrawable(int drawableResourceId) {
            this.drawableResourceId = drawableResourceId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            RobolectricBitmapDrawable that = (RobolectricBitmapDrawable) o;

            if (drawableResourceId != that.drawableResourceId) return false;
            if (!getBounds().equals(that.getBounds())) return false;

            return true;
        }

        @Override
        public int hashCode() {
            return drawableResourceId;
        }
    }

}
