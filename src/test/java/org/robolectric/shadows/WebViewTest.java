package org.robolectric.shadows;

import android.graphics.Picture;
import android.view.ViewGroup.LayoutParams;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.TestRunners;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.robolectric.Robolectric.shadowOf;

@RunWith(TestRunners.WithDefaults.class)
public class WebViewTest {

  private WebView webView;
  private ShadowWebView shadowWebView;

  @Before
  public void setUp() throws Exception {
    webView = new WebView(Robolectric.application);
    shadowWebView = Robolectric.shadowOf(webView);
  }

  @Test
  public void shouldRecordLastLoadedUrl() {
    webView.loadUrl("http://example.com");
    assertThat(shadowOf(webView).getLastLoadedUrl()).isEqualTo("http://example.com");
  }

  @Test
  public void shouldRecordLastLoadedData() {
    webView.loadData("<html><body><h1>Hi</h1></body></html>", "text/html", "utf-8");
    ShadowWebView.LoadData lastLoadData = shadowOf(webView).getLastLoadData();
    assertThat(lastLoadData.data).isEqualTo("<html><body><h1>Hi</h1></body></html>");
    assertThat(lastLoadData.mimeType).isEqualTo("text/html");
    assertThat(lastLoadData.encoding).isEqualTo("utf-8");
  }

  @Test
  public void shouldRecordLastLoadDataWithBaseURL() throws Exception {
    webView.loadDataWithBaseURL("base/url", "<html><body><h1>Hi</h1></body></html>", "text/html", "utf-8", "history/url");
    ShadowWebView.LoadDataWithBaseURL lastLoadData = shadowOf(webView).getLastLoadDataWithBaseURL();
    assertThat(lastLoadData.baseUrl).isEqualTo("base/url");
    assertThat(lastLoadData.data).isEqualTo("<html><body><h1>Hi</h1></body></html>");
    assertThat(lastLoadData.mimeType).isEqualTo("text/html");
    assertThat(lastLoadData.encoding).isEqualTo("utf-8");
    assertThat(lastLoadData.historyUrl).isEqualTo("history/url");
  }

  @Test
  public void shouldReturnSettings() {
    WebSettings webSettings = webView.getSettings();

    assertThat(webSettings).isNotNull();
  }

  @Test
  public void shouldRecordWebViewClient() {
    WebViewClient webViewClient = new WebViewClient();

    assertThat(shadowWebView.getWebViewClient()).isNull();
    webView.setWebViewClient(webViewClient);
    assertThat(shadowWebView.getWebViewClient()).isSameAs(webViewClient);
  }

  @Test
  public void shouldRecordWebChromeClient() {
    WebChromeClient webChromeClient = new WebChromeClient();
    assertThat(shadowWebView.getWebChromeClient()).isNull();
    webView.setWebChromeClient(webChromeClient);
    assertThat(shadowWebView.getWebChromeClient()).isSameAs(webChromeClient);
  }

  @Test
  public void shouldRecordPictureListener() {
    WebView.PictureListener pictureListener = new WebView.PictureListener() {
      @Override
      public void onNewPicture(WebView view, Picture picture) {
        ;
      }
    };

    assertThat(shadowWebView.getPictureListener()).isNull();
    webView.setPictureListener(pictureListener);
    assertThat(shadowWebView.getPictureListener()).isSameAs(pictureListener);
  }

  @Test
  public void shouldRecordJavascriptInteraces() {
    String[] names = {"name1", "name2"};
    for (String name : names) {
      Object obj = new Object();
      assertThat(shadowWebView.getJavascriptInterface(name)).isNull();
      webView.addJavascriptInterface(obj, name);
      assertThat(shadowWebView.getJavascriptInterface(name)).isSameAs(obj);
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
    assertThat(shadowWebView.getRunFlag()).isFalse();
    shadowWebView.post(testRun);
    assertThat(shadowWebView.getRunFlag()).isTrue();
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
    assertThat(shadowWebView.wasClearCacheCalled()).isFalse();

    webView.clearCache(false);
    assertThat(shadowWebView.wasClearCacheCalled()).isTrue();
    assertThat(shadowWebView.didClearCacheIncludeDiskFiles()).isFalse();
  }

  @Test
  public void shouldRecordClearCacheWithDiskFiles() {
    assertThat(shadowWebView.wasClearCacheCalled()).isFalse();

    webView.clearCache(true);
    assertThat(shadowWebView.wasClearCacheCalled()).isTrue();
    assertThat(shadowWebView.didClearCacheIncludeDiskFiles()).isTrue();
  }

  @Test
  public void shouldRecordClearFormData() {
    assertThat(shadowWebView.wasClearFormDataCalled()).isFalse();
    webView.clearFormData();
    assertThat(shadowWebView.wasClearFormDataCalled()).isTrue();
  }

  @Test
  public void shouldRecordClearHistory() {
    assertThat(shadowWebView.wasClearHistoryCalled()).isFalse();
    webView.clearHistory();
    assertThat(shadowWebView.wasClearHistoryCalled()).isTrue();
  }

  @Test
  public void shouldRecordClearView() {
    assertThat(shadowWebView.wasClearViewCalled()).isFalse();
    webView.clearView();
    assertThat(shadowWebView.wasClearViewCalled()).isTrue();
  }

  @Test
  public void shouldRecordDestroy() {
    assertThat(shadowWebView.wasDestroyCalled()).isFalse();
    webView.destroy();
    assertThat(shadowWebView.wasDestroyCalled()).isTrue();
  }

  @Test
  public void shouldRecordOnPause() {
    assertThat(shadowWebView.wasOnPauseCalled()).isFalse();
    webView.onPause();
    assertThat(shadowWebView.wasOnPauseCalled()).isTrue();
  }

  @Test
  public void shouldRecordOnResume() {
    assertThat(shadowWebView.wasOnResumeCalled()).isFalse();
    webView.onResume();
    assertThat(shadowWebView.wasOnResumeCalled()).isTrue();
  }
  
  @Test
  public void shouldReturnPreviouslySetLayoutParams() {
    assertThat(webView.getLayoutParams()).isNull();
    LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
    webView.setLayoutParams(params);
    assertThat(webView.getLayoutParams()).isSameAs(params);
  }
  
}
