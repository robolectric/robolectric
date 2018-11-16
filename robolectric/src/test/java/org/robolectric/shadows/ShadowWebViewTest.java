package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.view.ViewGroup.LayoutParams;
import android.webkit.WebBackForwardList;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

@RunWith(AndroidJUnit4.class)
public class ShadowWebViewTest {

  private WebView webView;

  @Before
  public void setUp() throws Exception {
    webView = new WebView(ApplicationProvider.getApplicationContext());
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

    assertThat(shadowOf(webView).getWebViewClient()).isNull();
    webView.setWebViewClient(webViewClient);
    assertThat(shadowOf(webView).getWebViewClient()).isSameAs(webViewClient);
  }

  @Test
  public void shouldRecordWebChromeClient() {
    WebChromeClient webChromeClient = new WebChromeClient();
    assertThat(shadowOf(webView).getWebChromeClient()).isNull();
    webView.setWebChromeClient(webChromeClient);
    assertThat(shadowOf(webView).getWebChromeClient()).isSameAs(webChromeClient);
  }

  @Test
  public void shouldRecordJavascriptInteraces() {
    String[] names = {"name1", "name2"};
    for (String name : names) {
      Object obj = new Object();
      assertThat(shadowOf(webView).getJavascriptInterface(name)).isNull();
      webView.addJavascriptInterface(obj, name);
      assertThat(shadowOf(webView).getJavascriptInterface(name)).isSameAs(obj);
    }
  }

  @Test
  public void canGoBack() throws Exception {
    webView.clearHistory();
    assertThat(webView.canGoBack()).isFalse();
    webView.loadUrl("fake.url", null);
    webView.loadUrl("fake.url", null);
    assertThat(webView.canGoBack()).isTrue();
    webView.goBack();
    assertThat(webView.canGoBack()).isFalse();
  }

  @Test
  public void shouldStoreTheNumberOfTimesGoBackWasCalled() throws Exception {
    assertThat(shadowOf(webView).getGoBackInvocations()).isEqualTo(0);
    webView.goBack();
    webView.loadUrl("foo.bar", null);
    // If there is no history (only one page), we shouldn't invoke go back.
    assertThat(shadowOf(webView).getGoBackInvocations()).isEqualTo(0);
    webView.loadUrl("foo.bar", null);
    webView.loadUrl("foo.bar", null);
    webView.loadUrl("foo.bar", null);
    webView.loadUrl("foo.bar", null);
    webView.loadUrl("foo.bar", null);
    webView.goBack();
    assertThat(shadowOf(webView).getGoBackInvocations()).isEqualTo(1);
    webView.goBack();
    webView.goBack();
    assertThat(shadowOf(webView).getGoBackInvocations()).isEqualTo(3);
    webView.goBack();
    webView.goBack();
    webView.goBack();
    // We've gone back one too many times for the history, so we should only have 5 invocations.
    assertThat(shadowOf(webView).getGoBackInvocations()).isEqualTo(5);
  }

  @Test
  public void shouldStoreTheNumberOfTimesGoBackWasCalled_SetCanGoBack() {
    shadowOf(webView).setCanGoBack(true);
    webView.goBack();
    webView.goBack();
    assertThat(shadowOf(webView).getGoBackInvocations()).isEqualTo(2);
    shadowOf(webView).setCanGoBack(false);
    webView.goBack();
    webView.goBack();
    assertThat(shadowOf(webView).getGoBackInvocations()).isEqualTo(2);
  }

  @Test
  public void shouldRecordClearCacheWithoutDiskFiles() {
    assertThat(shadowOf(webView).wasClearCacheCalled()).isFalse();

    webView.clearCache(false);
    assertThat(shadowOf(webView).wasClearCacheCalled()).isTrue();
    assertThat(shadowOf(webView).didClearCacheIncludeDiskFiles()).isFalse();
  }

  @Test
  public void shouldRecordClearCacheWithDiskFiles() {
    assertThat(shadowOf(webView).wasClearCacheCalled()).isFalse();

    webView.clearCache(true);
    assertThat(shadowOf(webView).wasClearCacheCalled()).isTrue();
    assertThat(shadowOf(webView).didClearCacheIncludeDiskFiles()).isTrue();
  }

  @Test
  public void shouldRecordClearFormData() {
    assertThat(shadowOf(webView).wasClearFormDataCalled()).isFalse();
    webView.clearFormData();
    assertThat(shadowOf(webView).wasClearFormDataCalled()).isTrue();
  }

  @Test
  public void shouldRecordClearHistory() {
    assertThat(shadowOf(webView).wasClearHistoryCalled()).isFalse();
    webView.clearHistory();
    assertThat(shadowOf(webView).wasClearHistoryCalled()).isTrue();
  }

  @Test
  public void shouldRecordClearView() {
    assertThat(shadowOf(webView).wasClearViewCalled()).isFalse();
    webView.clearView();
    assertThat(shadowOf(webView).wasClearViewCalled()).isTrue();
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
    assertThat(shadowOf(webView).getLastEvaluatedJavascript()).isNull();
    webView.evaluateJavascript("myScript", null);
    assertThat(shadowOf(webView).getLastEvaluatedJavascript()).isEqualTo("myScript");
  }

  @Test
  public void shouldRecordDestroy() {
    assertThat(shadowOf(webView).wasDestroyCalled()).isFalse();
    webView.destroy();
    assertThat(shadowOf(webView).wasDestroyCalled()).isTrue();
  }

  @Test
  public void shouldRecordOnPause() {
    assertThat(shadowOf(webView).wasOnPauseCalled()).isFalse();
    webView.onPause();
    assertThat(shadowOf(webView).wasOnPauseCalled()).isTrue();
  }

  @Test
  public void shouldRecordOnResume() {
    assertThat(shadowOf(webView).wasOnResumeCalled()).isFalse();
    webView.onResume();
    assertThat(shadowOf(webView).wasOnResumeCalled()).isTrue();
  }

  @Test
  public void shouldReturnPreviouslySetLayoutParams() {
    assertThat(webView.getLayoutParams()).isNull();
    LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
    webView.setLayoutParams(params);
    assertThat(webView.getLayoutParams()).isSameAs(params);
  }

  @Test
  public void shouldSaveAndRestoreHistoryList() {
    webView.loadUrl("foo1.bar");
    webView.loadUrl("foo2.bar");

    Bundle outState = new Bundle();
    webView.saveState(outState);

    WebView newWebView = new WebView(ApplicationProvider.getApplicationContext());
    WebBackForwardList historyList = newWebView.restoreState(outState);

    assertThat(newWebView.canGoBack()).isTrue();
    assertThat(newWebView.getUrl()).isEqualTo("foo2.bar");

    assertThat(historyList.getSize()).isEqualTo(2);
    assertThat(historyList.getCurrentItem().getUrl()).isEqualTo("foo2.bar");
  }

  @Test
  public void shouldReturnHistoryFromSaveState() {
    webView.loadUrl("foo1.bar");
    webView.loadUrl("foo2.bar");

    Bundle outState = new Bundle();
    WebBackForwardList historyList = webView.saveState(outState);

    assertThat(historyList.getSize()).isEqualTo(2);
    assertThat(historyList.getCurrentItem().getUrl()).isEqualTo("foo2.bar");
  }

  @Test
  public void shouldReturnNullFromRestoreStateIfNoHistoryAvailable() {
    Bundle inState = new Bundle();
    WebBackForwardList historyList = webView.restoreState(inState);

    assertThat(historyList).isNull();
  }

  @Test
  public void shouldCopyBackForwardListWhenEmpty() {
    WebBackForwardList historyList = webView.copyBackForwardList();

    assertThat(historyList.getSize()).isEqualTo(0);
    assertThat(historyList.getCurrentIndex()).isEqualTo(-1);
    assertThat(historyList.getCurrentItem()).isNull();
  }

  @Test
  public void shouldCopyBackForwardListWhenPopulated() {
    webView.loadUrl("foo1.bar");
    webView.loadUrl("foo2.bar");

    WebBackForwardList historyList = webView.copyBackForwardList();

    assertThat(historyList.getSize()).isEqualTo(2);
    assertThat(historyList.getCurrentItem().getUrl()).isEqualTo("foo2.bar");
  }

  @Test
  public void shouldReturnCopyFromCopyBackForwardList() {
    WebBackForwardList historyList = webView.copyBackForwardList();

    // Adding history after copying should not affect the copy.
    webView.loadUrl("foo1.bar");
    webView.loadUrl("foo2.bar");

    assertThat(historyList.getSize()).isEqualTo(0);
    assertThat(historyList.getCurrentIndex()).isEqualTo(-1);
    assertThat(historyList.getCurrentItem()).isNull();
  }

  @Test
  @Config(minSdk = 26)
  public void shouldReturnNullForGetCurrentWebViewPackageIfNotSet() {
    assertThat(WebView.getCurrentWebViewPackage()).isNull();
  }

  @Test
  @Config(minSdk = 26)
  public void shouldReturnStoredPackageInfoForGetCurrentWebViewPackageIfSet() {
    PackageInfo packageInfo = new PackageInfo();
    packageInfo.packageName = "org.robolectric.shadows.shadowebviewtest";
    ShadowWebView.setCurrentWebViewPackage(packageInfo);
    assertThat(WebView.getCurrentWebViewPackage()).isEqualTo(packageInfo);
  }
}
