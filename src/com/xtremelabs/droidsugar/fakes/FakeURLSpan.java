package com.xtremelabs.droidsugar.fakes;

import android.text.style.URLSpan;
import com.xtremelabs.droidsugar.util.Implementation;
import com.xtremelabs.droidsugar.util.Implements;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(URLSpan.class)
public class FakeURLSpan {
    private String url;

    public void __constructor__(String url) {
        this.url = url;
    }

    @Implementation
    public String getURL() {
        return url;
    }
}
