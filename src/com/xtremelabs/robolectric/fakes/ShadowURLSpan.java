package com.xtremelabs.robolectric.fakes;

import android.text.style.URLSpan;
import com.xtremelabs.robolectric.util.Implementation;
import com.xtremelabs.robolectric.util.Implements;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(URLSpan.class)
public class ShadowURLSpan {
    private String url;

    public void __constructor__(String url) {
        this.url = url;
    }

    @Implementation
    public String getURL() {
        return url;
    }
}
