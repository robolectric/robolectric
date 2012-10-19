package com.xtremelabs.robolectric.shadows;

import android.content.res.AssetManager;
import android.graphics.Typeface;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

import java.io.IOException;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;

@Implements(Typeface.class)
public class ShadowTypeface {
    private String assetPath;

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

    public String getAssetPath() {
        return assetPath;
    }

    public void setAssetPath(String assetPath) {
        this.assetPath = assetPath;
    }
}
