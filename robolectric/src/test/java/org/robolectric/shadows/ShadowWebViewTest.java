package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.O;
import static android.os.Looper.getMainLooper;
import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.robolectric.Shadows.shadowOf;

import android.content.pm.PackageInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.view.ViewGroup.LayoutParams;
import android.webkit.DownloadListener;
import android.webkit.WebBackForwardList;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebView.HitTestResult;
import android.webkit.WebViewClient;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
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
  @Config(minSdk = O)
  public void shouldPerformPageLoadCallbacksOnLoadUrl() {
    WebChromeClient mockWebChromeClient = mock(WebChromeClient.class);
    WebViewClient mockWebViewClient = mock(WebViewClient.class);
    webView.setWebChromeClient(mockWebChromeClient);
    webView.setWebViewClient(mockWebViewClient);
    String url = "http://example.com";

    webView.loadUrl(url);
    shadowOf(getMainLooper()).idle();

    verifyNoMoreInteractions(mockWebChromeClient);
    verifyNoMoreInteractions(mockWebViewClient);

    shadowOf(webView).performSuccessfulPageLoadClientCallbacks();
    webView.loadUrl(url);
    shadowOf(getMainLooper()).idle();

    InOrder inOrder = inOrder(mockWebViewClient, mockWebChromeClient);
    inOrder.verify(mockWebViewClient).onPageStarted(webView, url, null);
    inOrder.verify(mockWebViewClient).onPageCommitVisible(webView, url);
    inOrder.verify(mockWebChromeClient).onReceivedTitle(webView, url);
    inOrder.verify(mockWebChromeClient).onProgressChanged(webView, 100);
    inOrder.verify(mockWebViewClient).onPageFinished(webView, url);
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
  public void shouldRecordLastLoadDataWithBaseURL() {
    webView.loadDataWithBaseURL(
        "base/url", "<html><body><h1>Hi</h1></body></html>", "text/html", "utf-8", "history/url");
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
    assertThat(shadowOf(webView).getWebViewClient()).isSameInstanceAs(webViewClient);
  }

  @Test
  public void shouldRecordWebChromeClient() {
    WebChromeClient webChromeClient = new WebChromeClient();
    assertThat(shadowOf(webView).getWebChromeClient()).isNull();
    webView.setWebChromeClient(webChromeClient);
    assertThat(shadowOf(webView).getWebChromeClient()).isSameInstanceAs(webChromeClient);
  }

  @Test
  public void shouldRecordJavascriptInterfaces() {
    String[] names = {"name1", "name2"};
    for (String name : names) {
      Object obj = new Object();
      assertThat(shadowOf(webView).getJavascriptInterface(name)).isNull();
      webView.addJavascriptInterface(obj, name);
      assertThat(shadowOf(webView).getJavascriptInterface(name)).isSameInstanceAs(obj);
    }
  }

  @Test
  public void shouldRemoveJavascriptInterfaces() {
    String name = "myJavascriptInterface";
    webView.addJavascriptInterface(new Object(), name);
    assertThat(shadowOf(webView).getJavascriptInterface(name)).isNotNull();
    webView.removeJavascriptInterface(name);
    assertThat(shadowOf(webView).getJavascriptInterface(name)).isNull();
  }

  @Test
  public void canGoBack() {
    webView.clearHistory();
    assertThat(webView.canGoBack()).isFalse();
    shadowOf(webView).pushEntryToHistory("fake.url");
    shadowOf(webView).pushEntryToHistory("fake.url");
    assertThat(webView.canGoBack()).isTrue();
    webView.goBack();
    assertThat(webView.canGoBack()).isFalse();
  }

  @Test
  public void canGoForward() {
    webView.clearHistory();
    assertThat(webView.canGoForward()).isFalse();
    shadowOf(webView).pushEntryToHistory("fake.url");
    shadowOf(webView).pushEntryToHistory("fake.url");
    assertThat(webView.canGoForward()).isFalse();
    webView.goBack();
    assertThat(webView.canGoForward()).isTrue();
    webView.goForward();
    assertThat(webView.canGoForward()).isFalse();
  }

  @Test
  public void shouldStoreTheNumberOfTimesGoBackWasCalled() {
    assertThat(shadowOf(webView).getGoBackInvocations()).isEqualTo(0);
    webView.goBack();
    shadowOf(webView).pushEntryToHistory("foo.bar");
    // If there is no history (only one page), we shouldn't invoke go back.
    assertThat(shadowOf(webView).getGoBackInvocations()).isEqualTo(0);
    shadowOf(webView).pushEntryToHistory("foo.bar");
    shadowOf(webView).pushEntryToHistory("foo.bar");
    shadowOf(webView).pushEntryToHistory("foo.bar");
    shadowOf(webView).pushEntryToHistory("foo.bar");
    shadowOf(webView).pushEntryToHistory("foo.bar");
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
  public void shouldStoreTheNumberOfTimesGoBackWasCalled_goBackOrForward() {
    assertThat(shadowOf(webView).getGoBackInvocations()).isEqualTo(0);
    webView.goBackOrForward(-1);
    shadowOf(webView).pushEntryToHistory("foo.bar");
    // If there is no history (only one page), we shouldn't invoke go back.
    assertThat(shadowOf(webView).getGoBackInvocations()).isEqualTo(0);
    shadowOf(webView).pushEntryToHistory("foo.bar");
    shadowOf(webView).pushEntryToHistory("foo.bar");
    shadowOf(webView).pushEntryToHistory("foo.bar");
    shadowOf(webView).pushEntryToHistory("foo.bar");
    shadowOf(webView).pushEntryToHistory("foo.bar");
    webView.goBackOrForward(-1);
    assertThat(shadowOf(webView).getGoBackInvocations()).isEqualTo(1);
    webView.goBackOrForward(-2);
    assertThat(shadowOf(webView).getGoBackInvocations()).isEqualTo(3);
    webView.goBackOrForward(-3);
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
  public void shouldStoreTheNumberOfTimesGoBackWasCalled_SetCanGoBack_goBackOrForward() {
    shadowOf(webView).setCanGoBack(true);
    webView.goBackOrForward(-2);
    assertThat(shadowOf(webView).getGoBackInvocations()).isEqualTo(2);
    shadowOf(webView).setCanGoBack(false);
    webView.goBackOrForward(-2);
    assertThat(shadowOf(webView).getGoBackInvocations()).isEqualTo(2);
  }

  @Test
  public void shouldStoreTheNumberOfTimesGoForwardWasCalled() {
    shadowOf(webView).pushEntryToHistory("foo.bar");
    shadowOf(webView).pushEntryToHistory("foo.bar");
    shadowOf(webView).pushEntryToHistory("foo.bar");

    webView.goBackOrForward(-2);
    webView.goForward();
    webView.goForward();
    assertThat(shadowOf(webView).getGoForwardInvocations()).isEqualTo(2);

    webView.goForward();
    assertThat(shadowOf(webView).getGoForwardInvocations()).isEqualTo(2);
  }

  @Test
  public void shouldStoreTheNumberOfTimesGoForwardWasCalled_goBackOrForward() {
    shadowOf(webView).pushEntryToHistory("foo.bar");
    shadowOf(webView).pushEntryToHistory("foo.bar");
    shadowOf(webView).pushEntryToHistory("foo.bar");

    webView.goBackOrForward(-2);
    webView.goBackOrForward(2);
    assertThat(shadowOf(webView).getGoForwardInvocations()).isEqualTo(2);

    webView.goBackOrForward(2);
    assertThat(shadowOf(webView).getGoForwardInvocations()).isEqualTo(2);
  }

  @Test
  public void shouldUpdateUrlWhenGoBackIsCalled() {
    shadowOf(webView).pushEntryToHistory("foo1.bar");
    shadowOf(webView).pushEntryToHistory("foo2.bar");

    webView.goBack();

    assertThat(webView.getUrl()).isEqualTo("foo1.bar");
  }

  @Test
  public void shouldUpdateUrlWhenGoForwardIsCalled() {
    shadowOf(webView).pushEntryToHistory("foo1.bar");
    shadowOf(webView).pushEntryToHistory("foo2.bar");
    webView.goBack();

    webView.goForward();

    assertThat(webView.getUrl()).isEqualTo("foo2.bar");
  }

  @Test
  public void shouldClearForwardHistoryWhenPushEntryToHistoryIsCalled() {
    shadowOf(webView).pushEntryToHistory("foo1.bar");
    shadowOf(webView).pushEntryToHistory("foo2.bar");

    webView.goBack();
    shadowOf(webView).pushEntryToHistory("foo3.bar");

    assertThat(webView.getUrl()).isEqualTo("foo3.bar");
    assertThat(webView.canGoForward()).isFalse();
    assertThat(webView.canGoBack()).isTrue();
    webView.goBack();
    assertThat(webView.getUrl()).isEqualTo("foo1.bar");
  }

  @Test
  public void shouldNotPushEntryFromLoadUrlToHistoryUntilRequested() {
    webView.loadUrl("foo1.bar");

    assertThat(webView.getUrl()).isEqualTo("foo1.bar");
    WebBackForwardList history = webView.copyBackForwardList();
    assertThat(history.getSize()).isEqualTo(0);

    shadowOf(webView).pushEntryToHistory("foo1.bar");

    history = webView.copyBackForwardList();
    assertThat(history.getSize()).isEqualTo(1);
    assertThat(history.getItemAtIndex(0).getUrl()).isEqualTo("foo1.bar");
  }

  @Test
  public void shouldNotPushEntryFromLoadDataWithBaseUrlToHistoryUntilRequested() {
    webView.loadDataWithBaseURL("foo1.bar", "data", "mime", "encoding", "foo1.bar");

    assertThat(webView.getUrl()).isEqualTo("foo1.bar");
    WebBackForwardList history = webView.copyBackForwardList();
    assertThat(history.getSize()).isEqualTo(0);

    shadowOf(webView).pushEntryToHistory("foo1.bar");

    history = webView.copyBackForwardList();
    assertThat(history.getSize()).isEqualTo(1);
    assertThat(history.getItemAtIndex(0).getUrl()).isEqualTo("foo1.bar");
  }

  @Test
  public void shouldUpdateUrlWhenEntryPushedToHistory() {
    shadowOf(webView).pushEntryToHistory("foo1.bar");

    assertThat(webView.getUrl()).isEqualTo("foo1.bar");
  }

  @Test
  public void shouldClearForwardHistoryWhenPushEntryIntoHistoryIsCalled() {
    shadowOf(webView).pushEntryToHistory("foo1.bar");
    shadowOf(webView).pushEntryToHistory("foo2.bar");

    webView.goBack();
    shadowOf(webView).pushEntryToHistory("foo3.bar");

    assertThat(webView.getUrl()).isEqualTo("foo3.bar");
    assertThat(webView.canGoForward()).isFalse();
    assertThat(webView.canGoBack()).isTrue();
    webView.goBack();
    assertThat(webView.getUrl()).isEqualTo("foo1.bar");
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
  public void getFavicon() {
    assertThat(webView.getFavicon()).isNull();
  }

  @Test
  public void getFavicon_withMockFaviconSet_returnsMockFavicon() {
    Bitmap emptyFavicon = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);

    shadowOf(webView).setFavicon(emptyFavicon);
    assertThat(webView.getFavicon()).isEqualTo(emptyFavicon);
  }

  @Test
  public void getFavicon_withMockFaviconSetMultipleTimes_returnsCorrectMockFavicon() {
    Bitmap emptyFavicon = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
    Bitmap emptyFavicon2 = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);

    shadowOf(webView).setFavicon(emptyFavicon);
    assertThat(webView.getFavicon()).isEqualTo(emptyFavicon);
    shadowOf(webView).setFavicon(emptyFavicon2);
    assertThat(webView.getFavicon()).isEqualTo(emptyFavicon2);
  }

  @Test
  public void getOriginalUrl() {
    webView.clearHistory();
    assertThat(webView.getOriginalUrl()).isNull();
    webView.loadUrl("fake.url", null);
    assertThat(webView.getOriginalUrl()).isEqualTo("fake.url");
  }

  @Test
  public void getTitle() {
    webView.clearHistory();
    assertThat(webView.getTitle()).isNull();
    webView.loadUrl("fake.url", null);
    assertThat(webView.getTitle()).isEqualTo("fake.url");
  }

  @Test
  public void getUrl() {
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
  public void shouldRecordReloadInvocations() {
    assertThat(shadowOf(webView).getReloadInvocations()).isEqualTo(0);
    webView.reload();
    assertThat(shadowOf(webView).getReloadInvocations()).isEqualTo(1);
    webView.reload();
    assertThat(shadowOf(webView).getReloadInvocations()).isEqualTo(2);
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
    assertThat(webView.getLayoutParams()).isSameInstanceAs(params);
  }

  @Test
  public void shouldSaveAndRestoreHistoryList() {
    shadowOf(webView).pushEntryToHistory("foo1.bar");
    shadowOf(webView).pushEntryToHistory("foo2.bar");

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
  public void shouldSaveAndRestoreHistoryList_goBack() {
    shadowOf(webView).pushEntryToHistory("foo1.bar");
    shadowOf(webView).pushEntryToHistory("foo2.bar");
    webView.goBack();

    Bundle outState = new Bundle();
    webView.saveState(outState);

    WebView newWebView = new WebView(ApplicationProvider.getApplicationContext());
    WebBackForwardList historyList = newWebView.restoreState(outState);

    assertThat(newWebView.canGoBack()).isFalse();
    assertThat(newWebView.canGoForward()).isTrue();
    assertThat(newWebView.getUrl()).isEqualTo("foo1.bar");

    assertThat(historyList.getSize()).isEqualTo(2);
    assertThat(historyList.getCurrentItem().getUrl()).isEqualTo("foo1.bar");
  }

  @Test
  public void shouldSaveAndRestoreHistoryList_noPushedEntries() {
    webView.loadUrl("foo1.bar");

    Bundle outState = new Bundle();
    webView.saveState(outState);

    WebView newWebView = new WebView(ApplicationProvider.getApplicationContext());
    WebBackForwardList historyList = newWebView.restoreState(outState);

    assertThat(newWebView.canGoBack()).isFalse();
    assertThat(newWebView.canGoForward()).isFalse();
    assertThat(newWebView.getUrl()).isNull();

    assertThat(historyList).isNull();
  }

  @Test
  public void shouldReturnHistoryFromSaveState() {
    shadowOf(webView).pushEntryToHistory("foo1.bar");
    shadowOf(webView).pushEntryToHistory("foo2.bar");

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
    shadowOf(webView).pushEntryToHistory("foo1.bar");
    shadowOf(webView).pushEntryToHistory("foo2.bar");

    WebBackForwardList historyList = webView.copyBackForwardList();

    assertThat(historyList.getSize()).isEqualTo(2);
    assertThat(historyList.getCurrentItem().getUrl()).isEqualTo("foo2.bar");
  }

  @Test
  public void shouldReturnCopyFromCopyBackForwardList() {
    WebBackForwardList historyList = webView.copyBackForwardList();

    // Adding history after copying should not affect the copy.
    shadowOf(webView).pushEntryToHistory("foo1.bar");
    shadowOf(webView).pushEntryToHistory("foo2.bar");

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

  @Test
  public void getHitTestResult() {
    shadowOf(webView)
        .setHitTestResult(ShadowWebView.createHitTestResult(HitTestResult.ANCHOR_TYPE, "extra"));

    HitTestResult result = webView.getHitTestResult();

    assertThat(result.getType()).isEqualTo(HitTestResult.ANCHOR_TYPE);
    assertThat(result.getExtra()).isEqualTo("extra");
  }

  @Test
  @Config(minSdk = 21)
  public void canEnableSlowWholeDocumentDraw() {
    WebView.enableSlowWholeDocumentDraw();
  }

  @Test
  @Config(minSdk = 21)
  public void canClearClientCertPreferences() {
    WebView.clearClientCertPreferences(null);
  }

  @Test
  @Config(minSdk = 27)
  public void canStartSafeBrowsing() {
    WebView.startSafeBrowsing(null, null);
  }

  @Test
  @Config(minSdk = 27)
  public void shouldReturnStoredUrlForGetSafeBrowsingPrivacyPolicyUrl() {
    assertThat(WebView.getSafeBrowsingPrivacyPolicyUrl()).isNull();
  }

  @Test
  @Config(minSdk = 19)
  public void canSetWebContentsDebuggingEnabled() {
    WebView.setWebContentsDebuggingEnabled(false);
    WebView.setWebContentsDebuggingEnabled(true);
  }

  @Test
  @Config(minSdk = 28)
  public void shouldReturnClassLoaderForGetWebViewClassLoader() {
    assertThat(WebView.getWebViewClassLoader()).isNull();
  }

  @Test
  public void getBackgroundColor_backgroundColorNotSet_returnsZero() {
    assertThat(shadowOf(webView).getBackgroundColor()).isEqualTo(0);
  }

  @Test
  public void getBackgroundColor_backgroundColorHasBeenSet_returnsCorrectBackgroundColor() {
    webView.setBackgroundColor(Color.RED);

    assertThat(shadowOf(webView).getBackgroundColor()).isEqualTo(Color.RED);
  }

  @Test
  public void getBackgroundColor_backgroundColorSetMultipleTimes_returnsLastBackgroundColor() {
    webView.setBackgroundColor(Color.RED);
    webView.setBackgroundColor(Color.BLUE);
    webView.setBackgroundColor(Color.GREEN);

    assertThat(shadowOf(webView).getBackgroundColor()).isEqualTo(Color.GREEN);
  }

  @Test
  public void getDownloadListener_noListenerSet_returnsNull() {
    assertThat(shadowOf(webView).getDownloadListener()).isEqualTo(null);
  }

  @Test
  public void getDownloadListener_listenerSet_returnsLastSetListener() {
    webView.setDownloadListener(mock(DownloadListener.class));

    DownloadListener lastListener = mock(DownloadListener.class);
    webView.setDownloadListener(lastListener);

    assertThat(shadowOf(webView).getDownloadListener()).isEqualTo(lastListener);
  }

  @Test
  public void restoreAndSaveState() {
    webView.restoreState(new Bundle());
    webView.saveState(new Bundle());
  }
}
