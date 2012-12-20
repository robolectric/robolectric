package com.xtremelabs.robolectric.shadows;

import android.content.res.AssetManager;
import android.graphics.Typeface;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

import java.io.IOException;
import java.util.ArrayList;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;

@Implements(Typeface.class)
public class ShadowTypeface {
    private String assetPath;
    private static ArrayList<String> fontPaths = new ArrayList<String>();

    @Implementation
    public static Typeface createFromAsset(AssetManager assetManager, String path) {
        try {
            assetManager.open(path);
            Typeface typeface = Robolectric.newInstanceOf(Typeface.class);
            shadowOf(typeface).setAssetPath(path);
            return typeface;
        } catch (IOException e) {
            throw new RuntimeException("Font not found");
        }
    }

    @Implementation
    public static Typeface createFromFile(String path) {
        if (fontPaths.contains(path)) {
            Typeface typeface = Robolectric.newInstanceOf(Typeface.class);
            shadowOf(typeface).setAssetPath(path);
            return typeface;
        }
        throw new RuntimeException("Font not found");
    }

    public String getAssetPath() {
        return assetPath;
    }

    public void setAssetPath(String assetPath) {
        this.assetPath = assetPath;
    }

    public static void addAllowedTypefacePath(String pathname) {
        fontPaths.add(pathname);
    }
}
