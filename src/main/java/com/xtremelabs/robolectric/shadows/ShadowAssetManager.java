package com.xtremelabs.robolectric.shadows;

import android.content.res.AssetManager;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;
import com.xtremelabs.robolectric.res.ResourceLoader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(AssetManager.class)
public final class ShadowAssetManager {
    static AssetManager bind(AssetManager assetManager, ResourceLoader resourceLoader) {
        ShadowAssetManager shadowAssetManager = shadowOf(assetManager);
        if (shadowAssetManager.resourceLoader != null) throw new RuntimeException("ResourceLoader already set!");
        shadowAssetManager.resourceLoader = resourceLoader;
        return assetManager;
    }

    private ResourceLoader resourceLoader;

    @Implementation
    public final String[] list(String path) throws IOException {
        return new File(resourceLoader.getAssetsBase(), path).list();
    }

    @Implementation
    public final InputStream open(String fileName) throws IOException {
        return new FileInputStream(new File(resourceLoader.getAssetsBase(), fileName));
    }

}
