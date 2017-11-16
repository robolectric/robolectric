package org.robolectric.shadows;

import static org.assertj.core.api.Assertions.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.view.ViewGroup.LayoutParams;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
public class ShadowWebViewTest {

  private WebView webView;
  private ShadowWebView shadowWebView;

  @Before
  public void setUp() throws Exception {
    webView = new WebView(RuntimeEnvironment.application);
    shadowWebView = Shadows.shadowOf(webView);
  }

  @Test
  public void shouldRecordLastLoadedUrl() {
    webView.loadUrl("http://example.com");
    assertThat(shadowOf(webView).getLastLoadedUrl()).isEqualTo("http://example.com");
  }

  @Test
  public void shouldRecordLastLoadedUrlForRequestWithAdditionalHeaders() {
    webView.loadUrl("http://example.com", null);
    assertThat(shadowOf(webView).getLastLoadedUrl()).isEqualTo("http://example.com");
    assertThat(shadowOf(webView).getLastAdditionalHttpHeaders()).isNull();

    Map<String, String> additionalHttpHeaders = new HashMap<>(1);
    additionalHttpHeaders.put("key1", "value1");
    webView.loadUrl("http://example.com", additionalHttpHeaders);
    assertThat(shadowOf(webView).getLastLoadedUrl()).isEqualTo("http://example.com");
    assertThat(shadowOf(webView).getLastAdditionalHttpHeaders()).isNotNull();
    assertThat(shadowOf(webView).getLastAdditionalHttpHeaders()).containsKey("key1");
    assertThat(shadowOf(webView).getLastAdditionalHttpHeaders().get("key1")).isEqualTo("value1");
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
  public void canGoBack() throws Exception {
    shadowWebView.clearHistory();
    assertThat(webView.canGoBack()).isFalse();
    shadowWebView.loadUrl("fake.url", null);
    shadowWebView.loadUrl("fake.url", null);
    assertThat(webView.canGoBack()).isTrue();
    webView.goBack();
    assertThat(webView.canGoBack()).isFalse();
  }

  @Test
  public void shouldStoreTheNumberOfTimesGoBackWasCalled() throws Exception {
    assertThat(shadowWebView.getGoBackInvocations()).isEqualTo(0);
    webView.goBack();
    webView.loadUrl("foo.bar", null);
    // If there is no history (only one page), we shouldn't invoke go back.
    assertThat(shadowWebView.getGoBackInvocations()).isEqualTo(0);
    webView.loadUrl("foo.bar", null);
    webView.loadUrl("foo.bar", null);
    webView.loadUrl("foo.bar", null);
    webView.loadUrl("foo.bar", null);
    webView.loadUrl("foo.bar", null);
    webView.goBack();
    assertThat(shadowWebView.getGoBackInvocations()).isEqualTo(1);
    webView.goBack();
    webView.goBack();
    assertThat(shadowWebView.getGoBackInvocations()).isEqualTo(3);
    webView.goBack();
    webView.goBack();
    webView.goBack();
    // We've gone back one too many times for the history, so we should only have 5 invocations.
    assertThat(shadowWebView.getGoBackInvocations()).isEqualTo(5);
  }

  @Test
  public void shouldStoreTheNumberOfTimesGoBackWasCalled_SetCanGoBack() {
    shadowWebView.setCanGoBack(true);
    webView.goBack();
    webView.goBack();
    assertThat(shadowWebView.getGoBackInvocations()).isEqualTo(2);
    shadowWebView.setCanGoBack(false);
    webView.goBack();
    webView.goBack();
    assertThat(shadowWebView.getGoBackInvocations()).isEqualTo(2);
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
  public void getOriginalUrl() throws Exception {
    webView.clearHistory();
    assertThat(webView.getOriginalUrl()).isNull();
    webView.loadUrl("fake.url", null);
    assertThat(webView.getOriginalUrl()).isEqualTo("fake.url");
  }

  @Test
  public void getUrl() throws Exception {
    webView.clearHistory();
    assertThat(webView.getUrl()).isNull();
    webView.loadUrl("fake.url", null);
    assertThat(webView.getUrl()).isEqualTo("fake.url");
  }

  @Test
  @Config(minSdk = 19)
  public void evaluateJavascript() {
    assertThat(shadowWebView.getLastEvaluatedJavascript()).isNull();
    webView.evaluateJavascript("myScript", null);
    assertThat(shadowWebView.getLastEvaluatedJavascript()).isEqualTo("myScript");
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
