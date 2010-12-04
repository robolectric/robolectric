package com.xtremelabs.robolectric.shadows;

import android.webkit.WebView;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

@RunWith(WithTestDefaultsRunner.class)
public class WebViewTest {

    @Test
    public void shouldRecordLastLoadedUrl() {
        WebView webView = new WebView(null);
        webView.loadUrl("http://example.com");
        assertThat(shadowOf(webView).getLastLoadedUrl(), equalTo("http://example.com"));
    }
}
