package com.xtremelabs.robolectric.shadows;

import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import com.xtremelabs.robolectric.Robolectric;
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
    private static String lastOpenFdArg;
    private ResourceLoader resourceLoader;

    static AssetManager bind(AssetManager assetManager, ResourceLoader resourceLoader) {
        ShadowAssetManager shadowAssetManager = shadowOf(assetManager);
        if (shadowAssetManager.resourceLoader != null) throw new RuntimeException("ResourceLoader already set!");
        shadowAssetManager.resourceLoader = resourceLoader;
        return assetManager;
    }

    @Implementation
    public final String[] list(String path) throws IOException {
        File file = new File(resourceLoader.getAssetsBase(), path);
        if (file.isDirectory()) {
            return file.list();
        }
        return new String[0];
    }

    @Implementation
    public final InputStream open(final String fileName) throws IOException {
        return new FileInputStream(new File(resourceLoader.getAssetsBase(), fileName)) {
            @Override
            public String toString() {
                return "stream for " + fileName;
            }
        };
    }

    @Implementation
    public AssetFileDescriptor openFd(String fileName) {
        lastOpenFdArg = fileName;
        return Robolectric.newInstanceOf(AssetFileDescriptor.class);
    }

    public static String getLatestFileOpenedForDescriptor() {
        return lastOpenFdArg;
    }
}
