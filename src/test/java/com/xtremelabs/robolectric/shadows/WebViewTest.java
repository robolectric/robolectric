package com.xtremelabs.robolectric.shadows;

import android.webkit.WebSettings;
import android.webkit.WebView;

import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;
import static org.hamcrest.CoreMatchers.*;
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
    public void testGetSettings() {
        WebView webView = new WebView(null);
        WebSettings webSettings = webView.getSettings();
        ShadowWebSettings shadowWebSettings = Robolectric.shadowOf(webSettings);
        
        assertThat( webSettings, notNullValue());
        assertThat( shadowWebSettings, notNullValue());
   }
}
