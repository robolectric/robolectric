package com.xtremelabs.robolectric.shadows;

import android.graphics.Picture;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertFalse;
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
    public void shouldRecordLastLoadedData() {
        webView.loadData("<html><body><h1>Hi</h1></body></html>", "text/html", "utf-8");
        ShadowWebView.LoadData lastLoadData = shadowOf(webView).getLastLoadData();
        assertThat(lastLoadData.data, equalTo("<html><body><h1>Hi</h1></body></html>"));
        assertThat(lastLoadData.mimeType, equalTo("text/html"));
        assertThat(lastLoadData.encoding, equalTo("utf-8"));
    }

    @Test
    public void shouldRecordLastLoadDataWithBaseURL() throws Exception {
        webView.loadDataWithBaseURL("base/url", "<html><body><h1>Hi</h1></body></html>", "text/html", "utf-8", "history/url");
        ShadowWebView.LoadDataWithBaseURL lastLoadData = shadowOf(webView).getLastLoadDataWithBaseURL();
        assertThat(lastLoadData.baseUrl, equalTo("base/url"));
        assertThat(lastLoadData.data, equalTo("<html><body><h1>Hi</h1></body></html>"));
        assertThat(lastLoadData.mimeType, equalTo("text/html"));
        assertThat(lastLoadData.encoding, equalTo("utf-8"));
        assertThat(lastLoadData.historyUrl, equalTo("history/url"));
    }

    @Test
    public void shouldReturnSettings() {
        WebSettings webSettings = webView.getSettings();

        assertThat(webSettings, notNullValue());
    }

    @Test
    public void shouldRecordWebViewClient() {
        WebViewClient webViewClient = new WebViewClient();

        assertThat(shadowWebView.getWebViewClient(), nullValue());
        webView.setWebViewClient(webViewClient);
        assertThat(shadowWebView.getWebViewClient(), sameInstance(webViewClient));
    }

    @Test
    public void shouldRecordWebChromeClient() {
        WebChromeClient webChromeClient = new WebChromeClient();
        assertThat(shadowWebView.getWebChromeClient(), nullValue());
        webView.setWebChromeClient(webChromeClient);
        assertThat(shadowWebView.getWebChromeClient(), sameInstance(webChromeClient));
    }
    
    @Test
    public void shouldRecordPictureListener() {
    	WebView.PictureListener pictureListener = new WebView.PictureListener() {	
			@Override
			public void onNewPicture(WebView view, Picture picture) {
				;
			}
		};
		
		assertThat(shadowWebView.getPictureListener(), nullValue());
		webView.setPictureListener(pictureListener);
		assertThat(shadowWebView.getPictureListener(), sameInstance(pictureListener));
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
    public void shouldStoreCanGoBack() throws Exception {
        shadowWebView.setCanGoBack(false);
        assertFalse(webView.canGoBack());
        shadowWebView.setCanGoBack(true);
        assertTrue(webView.canGoBack());
    }

    @Test
    public void shouldStoreTheNumberOfTimesGoBackWasCalled() throws Exception {
        assertEquals(0, shadowWebView.getGoBackInvocations());
        webView.goBack();
        assertEquals(1, shadowWebView.getGoBackInvocations());
        webView.goBack();
        webView.goBack();
        assertEquals(3, shadowWebView.getGoBackInvocations());
    }

    @Test
    public void shouldRecordClearCacheWithoutDiskFiles() {
        assertThat(shadowWebView.wasClearCacheCalled(), equalTo(false));

        webView.clearCache(false);
        assertThat(shadowWebView.wasClearCacheCalled(), equalTo(true));
        assertThat(shadowWebView.didClearCacheIncludeDiskFiles(), equalTo(false));
    }

    @Test
    public void shouldRecordClearCacheWithDiskFiles() {
        assertThat(shadowWebView.wasClearCacheCalled(), equalTo(false));

        webView.clearCache(true);
        assertThat(shadowWebView.wasClearCacheCalled(), equalTo(true));
        assertThat(shadowWebView.didClearCacheIncludeDiskFiles(), equalTo(true));
    }

    @Test
    public void shouldRecordClearFormData() {
        assertThat(shadowWebView.wasClearFormDataCalled(), equalTo(false));
        webView.clearFormData();
        assertThat(shadowWebView.wasClearFormDataCalled(), equalTo(true));
    }

    @Test
    public void shouldRecordClearHistory() {
        assertThat(shadowWebView.wasClearHistoryCalled(), equalTo(false));
        webView.clearHistory();
        assertThat(shadowWebView.wasClearHistoryCalled(), equalTo(true));
    }

    @Test
    public void shouldRecordClearView() {
        assertThat(shadowWebView.wasClearViewCalled(), equalTo(false));
        webView.clearView();
        assertThat(shadowWebView.wasClearViewCalled(), equalTo(true));
    }

    @Test
    public void shouldRecordDestroy() {
        assertThat(shadowWebView.wasDestroyCalled(), equalTo(false));
        webView.destroy();
        assertThat(shadowWebView.wasDestroyCalled(), equalTo(true));
    }
}
