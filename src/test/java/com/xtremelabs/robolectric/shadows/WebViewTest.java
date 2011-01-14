package com.xtremelabs.robolectric.shadows;

import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;

@RunWith(WithTestDefaultsRunner.class)
public class WebViewTest {

    @Test
    public void shouldRecordLastLoadedUrl() {
        WebView webView = new WebView(null);
        webView.loadUrl("http://example.com");
        assertThat(shadowOf(webView).getLastLoadedUrl(), equalTo("http://example.com"));
    }

    @Test
    public void shouldReturnSettings() {
        WebView webView = new WebView(null);
        WebSettings webSettings = webView.getSettings();
        ShadowWebSettings shadowWebSettings = Robolectric.shadowOf(webSettings);

        assertThat(webSettings, notNullValue());
        assertThat(shadowWebSettings, notNullValue());
    }

    @Test
    public void shouldRecordWebViewClient() {
        WebView webView = new WebView(null);
        ShadowWebView shadowWebView = Robolectric.shadowOf(webView);
        WebViewClient webViewClient = new WebViewClient();

        assertThat(shadowWebView.getWebViewClient(), nullValue());
        webView.setWebViewClient(webViewClient);
        assertThat(shadowWebView.getWebViewClient(), sameInstance(webViewClient));
    }

    @Test
    public void shouldRecordJavascriptInteraces() {
        WebView webView = new WebView(null);
        ShadowWebView shadowWebView = Robolectric.shadowOf(webView);

        String[] names = {"name1", "name2"};
        for (String name : names) {
            Object obj = new Object();
            assertThat(shadowWebView.getJavascriptInterface(name), nullValue());
            webView.addJavascriptInterface(obj, name);
            assertThat(shadowWebView.getJavascriptInterface(name), sameInstance(obj));
        }
    }
}
