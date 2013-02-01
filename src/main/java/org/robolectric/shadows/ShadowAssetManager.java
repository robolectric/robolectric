package org.robolectric.shadows;

import android.content.res.AssetManager;
import org.robolectric.AndroidManifest;
import org.robolectric.internal.Implementation;
import org.robolectric.internal.Implements;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.robolectric.Robolectric.shadowOf;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(AssetManager.class)
public final class ShadowAssetManager {
    static AssetManager bind(AssetManager assetManager, AndroidManifest androidManifest) {
        ShadowAssetManager shadowAssetManager = shadowOf(assetManager);
        if (shadowAssetManager.appManifest != null) throw new RuntimeException("ResourceLoader already set!");
        shadowAssetManager.appManifest = androidManifest;
        return assetManager;
    }

    private AndroidManifest appManifest;

    public final void __constructor__() {
    }

    @Implementation
    public final String[] list(String path) throws IOException {
        File file = new File(appManifest.getAssetsDirectory(), path);
        if (file.isDirectory()) {
            return file.list();
        }
        return new String[0];
    }

    @Implementation
    public final InputStream open(String fileName) throws IOException {
        return new FileInputStream(new File(appManifest.getAssetsDirectory(), fileName));
    }

}
