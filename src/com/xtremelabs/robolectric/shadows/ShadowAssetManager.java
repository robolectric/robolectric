package com.xtremelabs.robolectric.shadows;

import android.content.res.AssetManager;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

@Implements(AssetManager.class)
public final class ShadowAssetManager {
    String separator = System.getProperty("file.separator");

    @Implementation
    public final String[] list(String path) throws IOException {
        String filePath = "assets" + separator + path;
        return new File(filePath).list();
    }

    @Implementation
    public final InputStream open(String fileName) throws IOException {
        String filePath = "assets" + separator + fileName;
        File file = new File(filePath);
        return new FileInputStream(file);
    }
}
