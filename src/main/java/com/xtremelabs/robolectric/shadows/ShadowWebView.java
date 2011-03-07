package com.xtremelabs.robolectric.shadows;

import android.content.Context;
import android.util.AttributeSet;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

import java.util.HashMap;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(WebView.class)
public class ShadowWebView extends ShadowAbsoluteLayout {

    private String lastUrl;
    private HashMap<String, Object> javascriptInterfaces = new HashMap<String, Object>();
    private WebSettings webSettings = Robolectric.newInstanceOf(WebSettings.class);
    private WebViewClient webViewClient = null;
    private boolean runFlag = false;

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

    @Implementation
    public WebSettings getSettings() {
        return webSettings;
    }

    @Implementation
    public void setWebViewClient(WebViewClient client) {
        webViewClient = client;
    }

    public WebViewClient getWebViewClient() {
        return webViewClient;
    }

    @Implementation
    public void addJavascriptInterface(Object obj, String interfaceName) {
        javascriptInterfaces.put(interfaceName, obj);
    }

    public Object getJavascriptInterface(String interfaceName) {
        return javascriptInterfaces.get(interfaceName);
    }
    
    @Implementation
    public void post(Runnable action) {
    	action.run();
    	runFlag = true;
    }
    
    public boolean getRunFlag() {
    	return runFlag;
    }
}
