package com.xtremelabs.robolectric.shadows;

import android.content.Context;
import android.util.AttributeSet;
import android.webkit.WebView;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(WebView.class)
public class ShadowWebView extends ShadowAbsoluteLayout {

    private String lastUrl;

    @Override public void __constructor__(Context context, AttributeSet attributeSet) {
        super.__constructor__(context, attributeSet);
    }

    @Implementation
    public void loadUrl(String url) {
        lastUrl = url;
    }

    /**
     * Non-Android accessor.
     *
     * @return the last loaded url
     */
    public String getLastLoadedUrl() {
        return lastUrl;
    }
}
