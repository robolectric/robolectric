package com.xtremelabs.robolectric.shadows;

import android.content.Context;
import android.util.AttributeSet;
import android.webkit.WebView;
import com.xtremelabs.robolectric.util.Implements;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(WebView.class)
public class ShadowWebView extends ShadowAbsoluteLayout {

    @Override public void __constructor__(Context context, AttributeSet attributeSet) {
        super.__constructor__(context, attributeSet);
    }
}
