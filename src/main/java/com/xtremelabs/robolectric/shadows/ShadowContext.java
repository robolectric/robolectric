package com.xtremelabs.robolectric.shadows;

import android.app.Application;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;
import com.xtremelabs.robolectric.internal.RealObject;
import com.xtremelabs.robolectric.res.ResourceLoader;

import java.io.File;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;

/**
 * Calls through to the {@code resourceLoader} to actually load resources.
 */
@SuppressWarnings({"UnusedDeclaration"})
@Implements(Context.class)
abstract public class ShadowContext {
    @RealObject private Context realContext;

    @Implementation
    public String getString(int resId) {
        return realContext.getResources().getString(resId);
    }

    @Implementation
    public CharSequence getText(int resId) {
        return realContext.getResources().getText(resId);
    }

    @Implementation
    public String getString(int resId, Object... formatArgs) {
        return realContext.getResources().getString(resId, formatArgs);
    }

    @Implementation
    abstract public Resources.Theme getTheme();

    @Implementation
    public final TypedArray obtainStyledAttributes(
            int[] attrs) {
        return getTheme().obtainStyledAttributes(attrs);
    }

    @Implementation
    public final TypedArray obtainStyledAttributes(
            int resid, int[] attrs) throws Resources.NotFoundException {
        return getTheme().obtainStyledAttributes(resid, attrs);
    }

    @Implementation
    public final TypedArray obtainStyledAttributes(
            AttributeSet set, int[] attrs) {
        return getTheme().obtainStyledAttributes(set, attrs, 0, 0);
    }

    @Implementation
    public final TypedArray obtainStyledAttributes(
            AttributeSet set, int[] attrs, int defStyleAttr, int defStyleRes) {
        return getTheme().obtainStyledAttributes(
            set, attrs, defStyleAttr, defStyleRes);
    }

    @Implementation
    public File getFilesDir() {
        // todo: clean this up when tests finish? [xw 20110124]
        File file = new File(System.getProperty("java.io.tmpdir"), "android-tmp");
        file.mkdirs();
        return file;
    }

    @Implementation
    public File getFileStreamPath(String name) {
        return new File(getFilesDir(), name);
    }

    /**
     * Non-Android accessor.
     *
     * @return the {@code ResourceLoader} associated with this {@code Context}
     */
    public ResourceLoader getResourceLoader() {
        return shadowOf((Application) realContext.getApplicationContext()).getResourceLoader();
    }
}
