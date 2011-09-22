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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;

/**
 * Calls through to the {@code resourceLoader} to actually load resources.
 */
@SuppressWarnings({"UnusedDeclaration"})
@Implements(Context.class)
abstract public class ShadowContext {
    public static final File CACHE_DIR = new File(System.getProperty("java.io.tmpdir"), "android-cache");
    public static final File FILES_DIR = new File(System.getProperty("java.io.tmpdir"), "android-tmp");

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
    public File getCacheDir() {
        CACHE_DIR.mkdirs();
        return CACHE_DIR;
    }

    @Implementation
    public File getFilesDir() {
        FILES_DIR.mkdirs();
        return FILES_DIR;
    }

    @Implementation
    public FileInputStream openFileInput(String path) throws FileNotFoundException {
        return new FileInputStream(getFileStreamPath(path));
    }

    @Implementation
    public FileOutputStream openFileOutput(String path, int mode) throws FileNotFoundException {
        return new FileOutputStream(getFileStreamPath(path));
    }

    @Implementation
    public File getFileStreamPath(String name) {
        if (name.contains(File.separator)) {
            throw new IllegalArgumentException("File " + name + " contains a path separator");
        }
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

    public static void clearFilesAndCache() {
        clearFiles(FILES_DIR);
        clearFiles(CACHE_DIR);
    }

    public static void clearFiles(File dir) {
        if (dir != null && dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File f : files) {
                    if (f.isDirectory()) {
                        clearFiles(f);
                    }
                    f.delete();
                }
            }
        }
    }
}
