package org.robolectric.shadows;

import android.content.pm.PackageInfo;
import android.os.Build;
import android.view.ViewGroup.LayoutParams;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.robolectric.annotation.HiddenApi;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.fakes.RoboWebSettings;
import org.robolectric.util.ReflectionHelpers;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(value = WebView.class)
public class ShadowWebView extends ShadowViewGroup {
  @RealObject
  private WebView realWebView;

  private String lastUrl;
  private Map<String, String> lastAdditionalHttpHeaders;
  private HashMap<String, Object> javascriptInterfaces = new HashMap<>();
  private WebSettings webSettings = new RoboWebSettings();
  private WebViewClient webViewClient = null;
  private boolean runFlag = false;
  private boolean clearCacheCalled = false;
  private boolean clearCacheIncludeDiskFiles = false;
  private boolean clearFormDataCalled = false;
  private boolean clearHistoryCalled = false;
  private boolean clearViewCalled = false;
  private boolean destroyCalled = false;
  private boolean onPauseCalled = false;
  private boolean onResumeCalled = false;
  private WebChromeClient webChromeClient;
  private boolean canGoBack;
  private int goBackInvocations = 0;
  private LoadData lastLoadData;
  private LoadDataWithBaseURL lastLoadDataWithBaseURL;
  private String originalUrl;
  private List<String> history = new ArrayList<>();
  private String lastEvaluatedJavascript;
  // TODO: Delete this when setCanGoBack is deleted. This is only used to determine which "path" we
  // use when canGoBack or goBack is called.
  private boolean canGoBackIsSet;

  @HiddenApi @Implementation
  public void ensureProviderCreated() {
    final ClassLoader classLoader = getClass().getClassLoader();
    Class<?> webViewProviderClass = getClassNamed("android.webkit.WebViewProvider");
    Field mProvider;
    try {
      mProvider = WebView.class.getDeclaredField("mProvider");
      mProvider.setAccessible(true);
      if (mProvider.get(realView) == null) {
        Object provider = Proxy.newProxyInstance(classLoader, new Class[]{webViewProviderClass}, new InvocationHandler() {
          @Override public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (method.getName().equals("getViewDelegate") || method.getName().equals("getScrollDelegate")) {
              return Proxy.newProxyInstance(classLoader, new Class[]{
                  getClassNamed("android.webkit.WebViewProvider$ViewDelegate"),
                  getClassNamed("android.webkit.WebViewProvider$ScrollDelegate")
              }, new InvocationHandler() {
                @Override
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                  return nullish(method);
                }
              });
            }

            return nullish(method);
          }
        });
        mProvider.set(realView, provider);
      }
    } catch (NoSuchFieldException | IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }

  @Implementation
  public void setLayoutParams(LayoutParams params) {
    ReflectionHelpers.setField(realWebView, "mLayoutParams", params);
  }

  private Object nullish(Method method) {
    Class<?> returnType = method.getReturnType();
    if (returnType.equals(long.class)
        || returnType.equals(double.class)
        || returnType.equals(int.class)
        || returnType.equals(float.class)
        || returnType.equals(short.class)
        || returnType.equals(byte.class)
        ) return 0;
    if (returnType.equals(char.class)) return '\0';
    if (returnType.equals(boolean.class)) return false;
    return null;
  }

  private Class<?> getClassNamed(String className) {
    try {
      return getClass().getClassLoader().loadClass(className);
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  @Implementation
  public void loadUrl(String url) {
    loadUrl(url, null);
  }

  @Implementation
  public void loadUrl(String url, Map<String, String> additionalHttpHeaders) {
    history.add(0, url);
    originalUrl = url;
    lastUrl = url;

    if (additionalHttpHeaders != null) {
      this.lastAdditionalHttpHeaders = Collections.unmodifiableMap(additionalHttpHeaders);
    } else {
      this.lastAdditionalHttpHeaders = null;
    }
  }

  @Implementation
  public void loadDataWithBaseURL(String baseUrl, String data, String mimeType, String encoding, String historyUrl) {
    if (historyUrl != null) {
      originalUrl = historyUrl;
      history.add(0, historyUrl);
    }
    lastLoadDataWithBaseURL = new LoadDataWithBaseURL(baseUrl, data, mimeType, encoding, historyUrl);
  }

  @Implementation
  public void loadData(String data, String mimeType, String encoding) {
    lastLoadData = new LoadData(data, mimeType, encoding);
  }

  /**
   * @return the last loaded url
   */
  public String getLastLoadedUrl() {
    return lastUrl;
  }

  @Implementation
  public String getOriginalUrl() {
    return originalUrl;
  }

  @Implementation
  public String getUrl() {
    return originalUrl;
  }

  /**
   * @return the additional Http headers that in the same request with last loaded url
   */
  public Map<String, String> getLastAdditionalHttpHeaders() {
    return lastAdditionalHttpHeaders;
  }

  @Implementation
  public WebSettings getSettings() {
    return webSettings;
  }

  @Implementation
  public void setWebViewClient(WebViewClient client) {
    webViewClient = client;
  }

  @Implementation
  public void setWebChromeClient(WebChromeClient client) {
    webChromeClient = client;
  }

  public WebViewClient getWebViewClient() {
    return webViewClient;
  }

  @Implementation
  public void addJavascriptInterface(Object obj, String interfaceName) {
    javascriptInterfaces.put(interfaceName, obj);
  }

  public Object getJavascriptInterface(String interfaceName) {
    return javascriptInterfaces.get(interfaceName);
  }

  @Implementation
  public void clearCache(boolean includeDiskFiles) {
    clearCacheCalled = true;
    clearCacheIncludeDiskFiles = includeDiskFiles;
  }

  public boolean wasClearCacheCalled() {
    return clearCacheCalled;
  }

  public boolean didClearCacheIncludeDiskFiles() {
    return clearCacheIncludeDiskFiles;
  }

  @Implementation
  public void clearFormData() {
    clearFormDataCalled = true;
  }

  public boolean wasClearFormDataCalled() {
    return clearFormDataCalled;
  }

  @Implementation
  public void clearHistory() {
    clearHistoryCalled = true;
    history.clear();
  }

  public boolean wasClearHistoryCalled() {
    return clearHistoryCalled;
  }

  @Implementation
  public void clearView() {
    clearViewCalled = true;
  }

  public boolean wasClearViewCalled() {
    return clearViewCalled;
  }

  @Implementation
  public void onPause(){
    onPauseCalled = true;
  }

  public boolean wasOnPauseCalled() {
    return onPauseCalled;
  }

  @Implementation
  public void onResume() {
    onResumeCalled = true;
  }

  public boolean wasOnResumeCalled() {
    return onResumeCalled;
  }

  @Implementation
  public void destroy() {
    destroyCalled = true;
  }

  public boolean wasDestroyCalled() {
    return destroyCalled;
  }

  // todo: this won't actually be called right?
  @Override @Implementation
  public void post(Runnable action) {
    action.run();
    runFlag = true;
  }

  public boolean getRunFlag() {
    return runFlag;
  }


  /**
   * @return webChromeClient
   */
  public WebChromeClient getWebChromeClient() {
    return webChromeClient;
  }

  @Implementation
  public boolean canGoBack() {
    // TODO: Remove the canGoBack check when setCanGoBack is deleted.
    if (canGoBackIsSet) {
      return canGoBack;
    }
    return history.size() > 1;
  }

  @Implementation
  public void goBack() {
    if (canGoBack()) {
      goBackInvocations++;
      // TODO: Delete this when setCanGoBack is deleted, since this creates two different behavior
      // paths.
      if (canGoBackIsSet) {
        return;
      }
      history.remove(0);
      if (!history.isEmpty()) {
        originalUrl = history.get(0);
      }
    }
  }

  @Implementation
  public static String findAddress(String addr) {
    return null;
  }

  /**
   * Overrides the system implementation for getting the webview package. Always returns null.
   */
  @Implementation(minSdk = Build.VERSION_CODES.O)
  protected static PackageInfo getCurrentWebviewPackage() {
    return null;
  }

  @Implementation(minSdk = Build.VERSION_CODES.KITKAT)
  public void evaluateJavascript(String script, ValueCallback<String> callback) {
    this.lastEvaluatedJavascript = script;
  }

  public String getLastEvaluatedJavascript() {
    return lastEvaluatedJavascript;
  }

  /**
   * Sets the value to return from {@code android.webkit.WebView#canGoBack()}
   *
   * @param canGoBack Value to return from {@code android.webkit.WebView#canGoBack()}
   * @deprecated Do not depend on this method as it will be removed in a future update. The
   *     preferered method is to populate a fake web history to use for going back.
   */
  @Deprecated
  public void setCanGoBack(boolean canGoBack) {
    canGoBackIsSet = true;
    this.canGoBack = canGoBack;
  }

  /**
   * @return goBackInvocations the number of times {@code android.webkit.WebView#goBack()} was
   *     invoked
   */
  public int getGoBackInvocations() {
    return goBackInvocations;
  }

  public LoadData getLastLoadData() {
    return lastLoadData;
  }

  public LoadDataWithBaseURL getLastLoadDataWithBaseURL() {
    return lastLoadDataWithBaseURL;
  }

  public static void setWebContentsDebuggingEnabled(boolean enabled) { }

  public static class LoadDataWithBaseURL {
    public final String baseUrl;
    public final String data;
    public final String mimeType;
    public final String encoding;
    public final String historyUrl;

    public LoadDataWithBaseURL(String baseUrl, String data, String mimeType, String encoding, String historyUrl) {
      this.baseUrl = baseUrl;
      this.data = data;
      this.mimeType = mimeType;
      this.encoding = encoding;
      this.historyUrl = historyUrl;
    }
  }

  public static class LoadData {
    public final String data;
    public final String mimeType;
    public final String encoding;

    public LoadData(String data, String mimeType, String encoding) {
      this.data = data;
      this.mimeType = mimeType;
      this.encoding = encoding;
    }
  }
}
