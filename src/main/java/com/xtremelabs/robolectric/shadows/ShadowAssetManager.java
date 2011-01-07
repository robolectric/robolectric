package com.xtremelabs.robolectric.shadows;

import android.content.res.AssetManager;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(AssetManager.class)
public final class ShadowAssetManager {
    @Implementation
    public final String[] list(String path) throws IOException {
        return new File("assets", path).list();
    }

    @Implementation
    public final InputStream open(String fileName) throws IOException {
        return new FileInputStream(new File("assets", fileName));
    }
}
