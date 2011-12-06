package com.xtremelabs.robolectric.shadows;

import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Message;
import android.view.KeyEvent;
import android.webkit.HttpAuthHandler;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;

import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

@Implements(android.webkit.WebViewClient.class)
public class ShadowWebViewClient {

    @Implementation
    public boolean shouldOverrideKeyEvent(WebView view, KeyEvent event) {
        return false;
    }

    @Implementation
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        return false;
    }

    @Implementation
    public void onFormResubmission(WebView view, Message dontResend, Message resend) { }
    @Implementation
    public void onLoadResource(WebView view, String url) { }
    @Implementation
    public void onPageFinished(WebView view, String url) { }
    @Implementation
    public void onPageStarted(WebView view, String url, Bitmap favicon) { }
    @Implementation
    public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) { }
    @Implementation
    public void onReceivedHttpAuthRequest(WebView view, HttpAuthHandler handler, String host, String realm) { }
/*
 * Introduced in api 12
    @Implementation
    public void onReceivedLoginRequest(WebView view, String realm, String account, String args) { }
*/

    @Implementation
    public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) { }
    @Implementation
    public void onScaleChanged(WebView view, float oldScale, float newScale) { }
    @Implementation
    public void onTooManyRedirects(WebView view, Message cancelMsg, Message continueMsg) { }
    @Implementation
    public void onUnhandledKeyEvent(WebView view, KeyEvent event) { }
}
