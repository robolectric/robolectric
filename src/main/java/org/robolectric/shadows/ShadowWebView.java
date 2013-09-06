package org.robolectric.shadows;

import static org.fest.reflect.core.Reflection.field;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;

import org.fest.reflect.field.Invoker;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.internal.HiddenApi;

import android.view.ViewGroup.LayoutParams;
import android.webkit.TestWebSettings;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(value = WebView.class, inheritImplementationMethods = true)
public class ShadowWebView extends ShadowAbsoluteLayout {
  public static boolean DEBUG = false;

  @RealObject
  private WebView realWebView;
  
  private String lastUrl;
  private HashMap<String, Object> javascriptInterfaces = new HashMap<String, Object>();
  private WebSettings webSettings = new TestWebSettings();
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
  private WebView.PictureListener pictureListener;

  @HiddenApi @Implementation
  public void ensureProviderCreated() {
    final ClassLoader classLoader = getClass().getClassLoader();
    Class<?> webViewProviderClass = getClassNamed("android.webkit.WebViewProvider");
    Invoker<Object> mProviderField = field("mProvider").ofType((Class<Object>) webViewProviderClass).in(realView);
    if (mProviderField.get() == null) {
      Object provider = Proxy.newProxyInstance(classLoader, new Class[]{webViewProviderClass}, new InvocationHandler() {
        @Override public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
          if (DEBUG) System.out.println("[DEBUG] WebView: " + method);

          if (method.getName().equals("getViewDelegate") || method.getName().equals("getScrollDelegate")) {
            return Proxy.newProxyInstance(classLoader, new Class[]{
                getClassNamed("android.webkit.WebViewProvider$ViewDelegate"),
                getClassNamed("android.webkit.WebViewProvider$ScrollDelegate")
            }, new InvocationHandler() {
              @Override
              public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                if (DEBUG) System.out.println("[DEBUG] WebView delegate: " + method);
                return nullish(method);
              }
            });
          }

          return nullish(method);
        }
      });
      mProviderField.set(provider);
    }
  }

  @Implementation
  public void setLayoutParams(LayoutParams params) {
    field("mLayoutParams").ofType(LayoutParams.class).in(realWebView).set(params);
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
    lastUrl = url;
  }

  @Implementation
  public void loadDataWithBaseURL(String baseUrl, String data, String mimeType, String encoding, String historyUrl) {
    lastLoadDataWithBaseURL = new LoadDataWithBaseURL(baseUrl, data, mimeType, encoding, historyUrl);
  }

  @Implementation
  public void loadData(String data, String mimeType, String encoding) {
    lastLoadData = new LoadData(data, mimeType, encoding);
  }

  /**
   * Non-Android accessor.
   *
   * @return the last loaded url
   */
  public String getLastLoadedUrl() {
    return lastUrl;
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
  public void setPictureListener(WebView.PictureListener listener) {
    pictureListener = listener;
  }

  public WebView.PictureListener getPictureListener() {
    return pictureListener;
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

  @Implementation
  public void post(Runnable action) {
    action.run();
    runFlag = true;
  }

  public boolean getRunFlag() {
    return runFlag;
  }


  /**
   * Non-Android accessor.
   *
   * @return webChromeClient
   */
  public WebChromeClient getWebChromeClient() {
    return webChromeClient;
  }

  @Implementation
  public boolean canGoBack() {
    return canGoBack;
  }

  @Implementation
  public void goBack() {
    goBackInvocations++;
  }


  /**
   * Non-Android accessor.
   *
   * @return goBackInvocations the number of times {@code android.webkit.WebView#goBack()}
   * was invoked
   */
  public int getGoBackInvocations() {
    return goBackInvocations;
  }

  /**
   * Non-Android setter.
   *
   * Sets the value to return from {@code android.webkit.WebView#canGoBack()}
   *
   * @param canGoBack
   */
  public void setCanGoBack(boolean canGoBack) {
    this.canGoBack = canGoBack;
  }

  public LoadData getLastLoadData() {
    return lastLoadData;
  }

  public LoadDataWithBaseURL getLastLoadDataWithBaseURL() {
    return lastLoadDataWithBaseURL;
  }

  public class LoadDataWithBaseURL {
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

  public class LoadData {
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
