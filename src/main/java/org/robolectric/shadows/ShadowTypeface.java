package org.robolectric.shadows;

import android.content.res.AssetManager;
import android.graphics.Typeface;
import org.robolectric.internal.HiddenApi;
import org.robolectric.internal.Implementation;
import org.robolectric.internal.Implements;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static org.robolectric.Robolectric.shadowOf;

@Implements(Typeface.class)
public class ShadowTypeface {
    private static Map<FontDesc, Integer> fonts = new HashMap<FontDesc, Integer>();
    private static int nextFontId = 1;
    private FontDesc fontDesc;

    @HiddenApi
    public void __constructor__(int fontId) {
        fontDesc = findById(fontId);
    }

    public String getAssetPath() {
        return fontDesc.familyName;
    }

    synchronized public static void reset() {
        fonts.clear();
    }

    @HiddenApi @Implementation
    synchronized public static int nativeCreate(String familyName, int style) {
        FontDesc fontDesc = new FontDesc(familyName, style);
        Integer fontId = fonts.get(fontDesc);
        if (fontId == null) {
            fontId = nextFontId++;
            fonts.put(fontDesc, fontId);
        }
        return fontId;
    }

    synchronized private static FontDesc findById(int fontId) {
        for (Map.Entry<FontDesc, Integer> entry : fonts.entrySet()) {
            if (entry.getValue().equals(fontId)) {
                return entry.getKey();
            }
        }
        throw new RuntimeException("unknown font id " + fontId);
    }

    @HiddenApi @Implementation
    public static int nativeCreateFromAsset(AssetManager mgr, String path) {
        return nativeCreateFromFile(new File(shadowOf(mgr).getAssetsDirectory(), path).getPath());
    }

    @HiddenApi @Implementation
    public static int nativeGetStyle(int native_instance) {
        return findById(native_instance).style;
    }

    @HiddenApi @Implementation
    public static int nativeCreateFromFile(String path) {
        File file = new File(path);
        if (file.exists()) {
            return nativeCreate(file.getPath(), 0);
        } else {
            throw new RuntimeException("Font not found at " + file.getAbsolutePath());
        }
    }


    private static class FontDesc {
        public final String familyName;
        public final int style;

        public FontDesc(String familyName, int style) {
            this.familyName = familyName;
            this.style = style;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            FontDesc fontDesc = (FontDesc) o;

            if (style != fontDesc.style) return false;
            if (familyName != null ? !familyName.equals(fontDesc.familyName) : fontDesc.familyName != null)
                return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = familyName != null ? familyName.hashCode() : 0;
            result = 31 * result + style;
            return result;
        }
    }
}
