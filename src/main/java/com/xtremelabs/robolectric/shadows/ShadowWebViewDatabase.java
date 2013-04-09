package com.xtremelabs.robolectric.shadows;

import android.content.Context;
import android.webkit.WebViewDatabase;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

@Implements(WebViewDatabase.class)
public class ShadowWebViewDatabase {
    private static WebViewDatabase instance;

    @Implementation
    public static WebViewDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Robolectric.newInstanceOf(WebViewDatabase.class);
        }
        return instance;
    }
}
