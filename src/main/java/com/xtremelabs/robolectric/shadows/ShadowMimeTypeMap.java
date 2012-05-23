package com.xtremelabs.robolectric.shadows;

import android.webkit.MimeTypeMap;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

import java.util.HashMap;
import java.util.Map;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;

/**
 * Shadow for {@code MimeTypeMap} that allows custom extension <-> mimetype mapping to be set up by tests.
 */
@Implements(MimeTypeMap.class)
public class ShadowMimeTypeMap {

    Map<String, String> extensionToMimeTypeMap = new HashMap<String, String>();
    Map<String, String> mimeTypeToExtensionMap = new HashMap<String, String>();

    static MimeTypeMap sSingleton = null;
    static Object sSingletonLock = new Object();

    @Implementation
    public static MimeTypeMap getSingleton() {
        if (sSingleton == null) {
            synchronized (sSingletonLock) {
                if (sSingleton == null) {
                    sSingleton = Robolectric.newInstanceOf(MimeTypeMap.class);
                }
            }
        }

        return sSingleton;
    }

    public static void reset() {
        shadowOf(getSingleton()).clearMappings();
    }

    @Implementation
    public String getMimeTypeFromExtension(String extension) {
        if (extensionToMimeTypeMap.containsKey(extension))
            return extensionToMimeTypeMap.get(extension);

        return null;
    }

    @Implementation
    public String getExtensionFromMimeType(String mimeType) {
        if (mimeTypeToExtensionMap.containsKey(mimeType))
            return mimeTypeToExtensionMap.get(mimeType);

        return null;
    }

    public void addExtensionMimeTypMapping(String extension, String mimeType) {
        extensionToMimeTypeMap.put(extension, mimeType);
        mimeTypeToExtensionMap.put(mimeType, extension);
    }

    public void clearMappings() {
        extensionToMimeTypeMap.clear();
        mimeTypeToExtensionMap.clear();
    }

    @Implementation
    public boolean hasExtension(String extension) {
        return extensionToMimeTypeMap.containsKey(extension);
    }

    @Implementation
    public boolean hasMimeType(String mimeType) {
        return mimeTypeToExtensionMap.containsKey(mimeType);
    }
}
