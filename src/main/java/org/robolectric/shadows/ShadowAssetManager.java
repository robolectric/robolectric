package org.robolectric.shadows;

import android.content.res.AssetManager;
import android.util.TypedValue;
import org.robolectric.AndroidManifest;
import org.robolectric.internal.HiddenApi;
import org.robolectric.internal.Implementation;
import org.robolectric.internal.Implements;
import org.robolectric.res.FsFile;
import org.robolectric.res.ResName;
import org.robolectric.res.ResourceLoader;
import org.robolectric.res.TypedResource;

import java.io.IOException;
import java.io.InputStream;

import static org.robolectric.Robolectric.shadowOf;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(AssetManager.class)
public final class ShadowAssetManager {
    private String qualifiers = "";

    static AssetManager bind(AssetManager assetManager, AndroidManifest androidManifest, ResourceLoader resourceLoader) {
        ShadowAssetManager shadowAssetManager = shadowOf(assetManager);
        if (shadowAssetManager.appManifest != null) throw new RuntimeException("ResourceLoader already set!");
        shadowAssetManager.appManifest = androidManifest;
        shadowAssetManager.resourceLoader = resourceLoader;
        return assetManager;
    }

    private AndroidManifest appManifest;
    private ResourceLoader resourceLoader;

    public final void __constructor__() {
    }

    @HiddenApi @Implementation
    public CharSequence getResourceText(int ident) {
        ResName resName = resourceLoader.getResourceIndex().getResName(ident);
        TypedResource value = getAndResolve(resName, getQualifiers());
        if (value == null) return null;
        return (CharSequence) value.getData();
    }

    @HiddenApi @Implementation
    public CharSequence getResourceBagText(int ident, int bagEntryId) {
        throw new UnsupportedOperationException(); // todo
    }

    @HiddenApi @Implementation
    public String[] getResourceStringArray(final int id) {
        CharSequence[] resourceTextArray = getResourceTextArray(id);
        if (resourceTextArray == null) return null;
        String[] strings = new String[resourceTextArray.length];
        for (int i = 0; i < strings.length; i++) {
            strings[i] = resourceTextArray[i].toString();
        }
        return strings;
    }

    @HiddenApi @Implementation
    public boolean getResourceValue(int ident, int density, TypedValue outValue, boolean resolveRefs) {
        ResName resName = resourceLoader.getResourceIndex().getResName(ident);
        TypedResource value = getAndResolve(resName, getQualifiers());
        if (value == null) return false;

        getConverter(value).fillTypedValue(value, outValue);
        return true;
    }

    private Converter getConverter(TypedResource value) {
        return Converter.getConverter(value.getResType());
    }

    @HiddenApi @Implementation
    public CharSequence[] getResourceTextArray(final int id) {
        ResName resName = resourceLoader.getResourceIndex().getResName(id);
        TypedResource value = getAndResolve(resName, getQualifiers());
        if (value == null) return null;
        TypedResource[] items = getConverter(value).getItems(value);
        CharSequence[] charSequences = new CharSequence[items.length];
        for (int i = 0; i < items.length; i++) {
            TypedResource typedResource = resolve(items[i], getQualifiers(), resName);
            charSequences[i] = getConverter(typedResource).asCharSequence(typedResource);
        }
        return charSequences;
    }

    @HiddenApi @Implementation
    public boolean getThemeValue(int theme, int ident, TypedValue outValue, boolean resolveRefs) {
        throw new UnsupportedOperationException(); // todo
    }

    @HiddenApi @Implementation
    public void ensureStringBlocks() {
    }

    @Implementation
    public final InputStream open(String fileName) throws IOException {
        return appManifest.getAssetsDirectory().join(fileName).getInputStream();
    }

    @Implementation
    public final String[] list(String path) throws IOException {
        FsFile file = appManifest.getAssetsDirectory().join(path);
        if (file.isDirectory()) {
            return file.listFileNames();
        }
        return new String[0];
    }

    @HiddenApi @Implementation
    public void setConfiguration(int mcc, int mnc, String locale,
                                 int orientation, int touchscreen, int density, int keyboard,
                                 int keyboardHidden, int navigation, int screenWidth, int screenHeight,
                                 int smallestScreenWidthDp, int screenWidthDp, int screenHeightDp,
                                 int screenLayout, int uiMode, int majorVersion) {
    }

    @HiddenApi @Implementation
    public int[] getArrayIntResource(int arrayRes) {
        ResName resName = resourceLoader.getResourceIndex().getResName(arrayRes);
        TypedResource value = getAndResolve(resName, getQualifiers());
        if (value == null) return null;
        TypedResource[] items = getConverter(value).getItems(value);
        int[] ints = new int[items.length];
        for (int i = 0; i < items.length; i++) {
            TypedResource typedResource = resolve(items[i], getQualifiers(), resName);
            ints[i] = getConverter(typedResource).asInt(typedResource);
        }
        return ints;
    }



    /////////////////////////

    TypedResource getAndResolve(ResName resName, String qualifiers) {
        if (resName == null) return null;
        TypedResource value = resourceLoader.getValue(resName, qualifiers);
        return resolve(value, qualifiers, resName);
    }

    TypedResource resolve(TypedResource value, String qualifiers, ResName contextResName) {
        while (true) {
            if (value == null) return null;

            Object data = value.getData();
            if (data instanceof String) {
                String s = (String) data;
                if (s.equals("@null")) {
                    return null;
                } else if (s.startsWith("@")) {
                    String refStr = s.substring(1).replace("+", "");
                    contextResName = ResName.qualifyResName(refStr, contextResName);
                    value = resourceLoader.getValue(contextResName, qualifiers);
                    // back through...
                } else {
                    return value;
                }
            } else {
                return value;
            }
        }
    }

    public FsFile getAssetsDirectory() {
        return appManifest.getAssetsDirectory();
    }

    public String getQualifiers() {
        return qualifiers;
    }

    public void setQualifiers(String qualifiers) {
        this.qualifiers = qualifiers;
    }
}
