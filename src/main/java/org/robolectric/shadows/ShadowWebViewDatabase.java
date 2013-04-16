package org.robolectric.shadows;

import android.content.Context;
import android.webkit.WebViewDatabase;
import org.robolectric.Robolectric;
import org.robolectric.internal.Implementation;
import org.robolectric.internal.Implements;

@Implements(value = WebViewDatabase.class, callThroughByDefault = false)
public class ShadowWebViewDatabase {
    @Implementation
    public static WebViewDatabase getInstance(Context ignored) {
        return Robolectric.newInstanceOf(WebViewDatabase.class);
    }
}
