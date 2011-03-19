package com.xtremelabs.robolectric.shadows;

import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

@RunWith(WithTestDefaultsRunner.class)
public class WebViewTest {
	
	private WebView webView;
	private ShadowWebView shadowWebView;

    @Before
    public void setUp() throws Exception {
    	webView = new WebView(null);
    	shadowWebView = Robolectric.shadowOf(webView);
    }
    	
    @Test
    public void shouldRecordLastLoadedUrl() {
        webView.loadUrl("http://example.com");
        assertThat(shadowOf(webView).getLastLoadedUrl(), equalTo("http://example.com"));
    }

    @Test
    public void shouldReturnSettings() {
        WebSettings webSettings = webView.getSettings();
        ShadowWebSettings shadowWebSettings = Robolectric.shadowOf(webSettings);

        assertThat(webSettings, notNullValue());
        assertThat(shadowWebSettings, notNullValue());
    }

    @Test
    public void shouldRecordWebViewClient() {
        WebViewClient webViewClient = new WebViewClient();

        assertThat(shadowWebView.getWebViewClient(), nullValue());
        webView.setWebViewClient(webViewClient);
        assertThat(shadowWebView.getWebViewClient(), sameInstance(webViewClient));
    }

    @Test
    public void shouldRecordJavascriptInteraces() {
        String[] names = {"name1", "name2"};
        for (String name : names) {
            Object obj = new Object();
            assertThat(shadowWebView.getJavascriptInterface(name), nullValue());
            webView.addJavascriptInterface(obj, name);
            assertThat(shadowWebView.getJavascriptInterface(name), sameInstance(obj));
        }
    }
    
    @Test
    public void shouldStartPostRun() {
    	Runnable testRun = new Runnable() {
    		public void run() {
    			//Do something...
    			return;
    		}
    	};
    	assertThat(shadowWebView.getRunFlag(), equalTo(false));
    	shadowWebView.post(testRun);
    	assertThat(shadowWebView.getRunFlag(), equalTo(true));
    }
    
    @Test
    public void shouldRecordClearCacheWithoutDiskFiles() {
    	assertThat( shadowWebView.wasClearCacheCalled(), equalTo( false ) );
 
    	webView.clearCache( false );
    	assertThat( shadowWebView.wasClearCacheCalled(), equalTo( true ) );
    	assertThat( shadowWebView.didClearCacheIncludeDiskFiles(), equalTo( false ) );    	
    }
    
    @Test
    public void shouldRecordClearCacheWithDiskFiles() {
    	assertThat( shadowWebView.wasClearCacheCalled(), equalTo( false ) );
 
    	webView.clearCache( true );
    	assertThat( shadowWebView.wasClearCacheCalled(), equalTo( true ) );
    	assertThat( shadowWebView.didClearCacheIncludeDiskFiles(), equalTo( true ) );    	
    }
}
