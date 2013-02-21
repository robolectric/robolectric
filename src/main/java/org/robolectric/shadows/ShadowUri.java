package org.robolectric.shadows;

import android.net.Uri;
import org.robolectric.internal.Implementation;
import org.robolectric.internal.Implements;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

@Implements(value = Uri.class, callThroughByDefault = true)
public class ShadowUri {
    /**
     * Avoid a reference to Android's custom java.nio.charset.Charsets.
     */
    @Implementation
    public static String decode(String s) {
        if (s == null) {
            return null;
        }
        try {
            return URLDecoder.decode(s, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

}
