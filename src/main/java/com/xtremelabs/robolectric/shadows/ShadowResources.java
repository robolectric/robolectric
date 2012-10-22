package com.xtremelabs.robolectric.shadows;

import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Display;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;
import com.xtremelabs.robolectric.internal.RealObject;
import com.xtremelabs.robolectric.res.ResourceExtractor;
import com.xtremelabs.robolectric.res.ResourceLoader;

import java.io.InputStream;
import java.util.Locale;

import static com.xtremelabs.robolectric.Robolectric.newInstanceOf;
import static com.xtremelabs.robolectric.Robolectric.shadowOf;

/**
 * Shadow of {@code Resources} that simulates the loading of resources
 *
 * @see com.xtremelabs.robolectric.RobolectricTestRunner#RobolectricTestRunner(Class, String, String)
 */
@SuppressWarnings({"UnusedDeclaration"})
@Implements(Resources.class)
public class ShadowResources {
    private float density = 1.0f;
    Configuration configuration = null;
    private DisplayMetrics displayMetrics;
    private Display display;

    static Resources bind(Resources resources, ResourceLoader resourceLoader) {
        ShadowResources shadowResources = shadowOf(resources);
        if (shadowResources.resourceLoader != null) throw new RuntimeException("ResourceLoader already set!");
        shadowResources.resourceLoader = resourceLoader;
        return resources;
    }

    @RealObject
    Resources realResources;
    private ResourceLoader resourceLoader;

    @Implementation
    public int getIdentifier(String name, String defType, String defPackage) {
        Integer index = 0;

        ResourceExtractor resourceExtractor = resourceLoader.getResourceExtractor();

        index = resourceExtractor.getResourceId(defType + "/" + name);
        if (index == null) {
            return 0;
        }
        return index;
    }

    @Implementation
    public int getColor(int id) throws Resources.NotFoundException {
        return resourceLoader.getColorValue(id);
    }

    @Implementation
    public Configuration getConfiguration() {
        if (configuration == null) {
            configuration = new Configuration();
            configuration.setToDefaults();
        }
        if (configuration.locale == null) {
            configuration.locale = Locale.getDefault();
        }
        return configuration;
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
    public String getQuantityString(int id, int quantity, Object... formatArgs) throws Resources.NotFoundException {
        String raw = getQuantityString(id, quantity);
        return String.format(Locale.ENGLISH, raw, formatArgs);
    }

    @Implementation
    public String getQuantityString(int id, int quantity) throws Resources.NotFoundException {
        return resourceLoader.getPluralStringValue(id, quantity);
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
    public CharSequence[] getTextArray(int id) throws Resources.NotFoundException {
        return getStringArray(id);
    }

    @Implementation
    public CharSequence getText(int id) throws Resources.NotFoundException {
        return getString(id);
    }

    public void setDensity(float density) {
        this.density = density;
    }

    public void setDisplay(Display display) {
        this.display = display;
        displayMetrics = null;
    }

    @Implementation
    public DisplayMetrics getDisplayMetrics() {
        if (displayMetrics == null) {
            if (display == null) {
                display = Robolectric.newInstanceOf(Display.class);
            }

            displayMetrics = new DisplayMetrics();
            display.getMetrics(displayMetrics);
        }
        displayMetrics.density = this.density;
        return displayMetrics;
    }

    @Implementation
    public Drawable getDrawable(int drawableResourceId) throws Resources.NotFoundException {

        ResourceLoader resLoader = Robolectric.shadowOf(Robolectric.application).getResourceLoader();

        Drawable xmlDrawable = resLoader.getXmlDrawable(drawableResourceId);
        if (xmlDrawable != null) {
            return xmlDrawable;
        }

        Drawable animDrawable = resLoader.getAnimDrawable(drawableResourceId);
        if (animDrawable != null) {
            return animDrawable;
        }

        Drawable colorDrawable = resLoader.getColorDrawable(drawableResourceId);
        if (colorDrawable != null) {
            return colorDrawable;
        }

        if (resLoader.isNinePatchDrawable(drawableResourceId)) {
        	return new NinePatchDrawable(realResources, null);
        }

        return new BitmapDrawable(realResources, BitmapFactory.decodeResource(realResources, drawableResourceId));
    }

    @Implementation
    public float getDimension(int id) throws Resources.NotFoundException {
        return resourceLoader.getDimenValue(id);
    }

    @Implementation
    public int getInteger(int id) throws Resources.NotFoundException {
    	return resourceLoader.getIntegerValue( id );
    }

    @Implementation
    public boolean getBoolean(int id) throws Resources.NotFoundException {
    	return resourceLoader.getBooleanValue( id );
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
    public AssetManager getAssets() {
        return ShadowAssetManager.bind(Robolectric.newInstanceOf(AssetManager.class), resourceLoader);
    }
    
    @Implementation
    public XmlResourceParser getXml(int id)
    		throws Resources.NotFoundException {
    	XmlResourceParser parser = resourceLoader.getXml(id);
    	if (parser == null) {
    		throw new Resources.NotFoundException();
    	}
    	return parser;
    }

    @Implementation
    public final android.content.res.Resources.Theme newTheme() {
        return newInstanceOf(Resources.Theme.class);
    }

    @Implements(Resources.Theme.class)
    public static class ShadowTheme {
        @Implementation
        public TypedArray obtainStyledAttributes(int[] attrs) {
            return obtainStyledAttributes(0, attrs);
        }

        @Implementation
        public TypedArray obtainStyledAttributes(int resid, int[] attrs) throws android.content.res.Resources.NotFoundException {
            return obtainStyledAttributes(null, attrs, 0, 0);
        }

        @Implementation
        public TypedArray obtainStyledAttributes(AttributeSet set, int[] attrs, int defStyleAttr, int defStyleRes) {
            return newInstanceOf(TypedArray.class);
        }
    }

}
