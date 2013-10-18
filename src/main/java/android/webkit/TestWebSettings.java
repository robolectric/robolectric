package android.webkit;

import org.robolectric.internal.DoNotInstrument;
import org.robolectric.annotation.Implementation;

/**
 * Concrete implementation of the abstract WebSettings class.
 */
@DoNotInstrument
public class TestWebSettings extends WebSettings {

  private boolean allowFileAccess = true;
  private boolean allowFileAccessFromFile = true;
  private boolean allowUniversalAccessFromFile = true;
  private boolean appCacheEnabled = false;
  private boolean blockNetworkImage = false;
  private boolean blockNetworkLoads = false;
  private boolean builtInZoomControls = true;
  private boolean databaseEnabled = false;
  private boolean domStorageEnabled = false;
  private boolean geolocationEnabled = false;
  private boolean javaScriptEnabled = false;
  private boolean lightTouchEnabled = false;
  private boolean loadWithOverviewMode = false;
  private boolean needInitialFocus = false;
  private RenderPriority renderPriority = RenderPriority.NORMAL;
  private boolean pluginsEnabled = false;
  private boolean saveFormData = false;
  private String appCachePath = "appcache";
  private String databasePath = "database";
  private String geolocationDatabasePath = "geolocation";
  private WebSettings.PluginState pluginState = WebSettings.PluginState.OFF;
  private boolean supportMultipleWindows = false;
  private boolean supportZoom = true;
  private String userAgentString = "Mozilla/5.0 (Linux; U; Android 4.0.3; ko-kr; LG-L160L Build/IML74K) AppleWebkit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30";
  private boolean useWideViewPort = false;
  private int cacheMode;

  public TestWebSettings() {
  }

  public boolean getAllowFileAccessFromFileURLs() {
    return allowFileAccessFromFile;
  }

  public boolean getAllowUniversalAccessFromFileURLs() {
    return allowUniversalAccessFromFile;
  }

  public void setAllowFileAccessFromFileURLs(boolean allow) {
    allowFileAccessFromFile = allow;
  }

  public void setAllowUniversalAccessFromFileURLs(boolean allow) {
    allowUniversalAccessFromFile = allow;
  }

  @Implementation
  public boolean getAllowFileAccess() {
    return allowFileAccess;
  }

  @Implementation
  public void setAllowFileAccess(boolean allow) {
    allowFileAccess = allow;
  }

  @Implementation
  public synchronized boolean getBlockNetworkImage() {
    return blockNetworkImage;
  }

  @Implementation
  public synchronized void setBlockNetworkImage(boolean flag) {
    blockNetworkImage = flag;
  }

  @Implementation
  public synchronized boolean getBlockNetworkLoads() {
    return blockNetworkLoads;
  }

  @Implementation
  public synchronized void setBlockNetworkLoads(boolean flag) {
    blockNetworkLoads = flag;
  }

  @Implementation
  public boolean getBuiltInZoomControls() {
    return builtInZoomControls;
  }

  @Implementation
  public void setBuiltInZoomControls(boolean enabled) {
    builtInZoomControls = enabled;
  }

  @Implementation
  public synchronized boolean getDatabaseEnabled() {
    return databaseEnabled;
  }

  @Implementation
  public synchronized void setDatabaseEnabled(boolean flag) {
    databaseEnabled = flag;
  }

  @Implementation
  public synchronized boolean getDomStorageEnabled() {
    return domStorageEnabled;
  }

  @Implementation
  public synchronized void setDomStorageEnabled(boolean flag) {
    domStorageEnabled = flag;
  }

  @Implementation
  public synchronized boolean getJavaScriptEnabled() {
    return javaScriptEnabled;
  }

  @Implementation
  public synchronized void setJavaScriptEnabled(boolean flag) {
    javaScriptEnabled = flag;
  }

  @Implementation
  public boolean getLightTouchEnabled() {
    return lightTouchEnabled;
  }

  @Implementation
  public void setLightTouchEnabled(boolean flag) {
    lightTouchEnabled = flag;
  }

  @Implementation
  public boolean getLoadWithOverviewMode() {
    return loadWithOverviewMode;
  }

  @Implementation
  public void setLoadWithOverviewMode(boolean flag) {
    loadWithOverviewMode = flag;
  }

  public boolean getNeedInitialFocus() {
    return needInitialFocus;
  }

  @Implementation
  public void setNeedInitialFocus(boolean flag) {
    needInitialFocus = flag;
  }

  @Override
  public synchronized void setRenderPriority(RenderPriority priority) {
    renderPriority = priority;
  }

  public RenderPriority getRenderPriority() {
    return renderPriority;
  }

  @Implementation
  public synchronized boolean getPluginsEnabled() {
    return pluginsEnabled;
  }

  @Implementation
  public synchronized void setPluginsEnabled(boolean flag) {
    pluginsEnabled = flag;
  }

  @Implementation
  public synchronized WebSettings.PluginState getPluginState() {
    return pluginState;
  }

  @Implementation
  public synchronized void setPluginState(WebSettings.PluginState state) {
    pluginState = state;
  }

  public boolean getSupportMultipleWindows() {
    return supportMultipleWindows;
  }

  @Implementation
  public synchronized void setDatabasePath(String path) {
    databasePath = path;
  }

  @Implementation
  public synchronized String getDatabasePath() {
    return databasePath;
  }

  @Implementation
  public synchronized void setSupportMultipleWindows(boolean support) {
    supportMultipleWindows = support;
  }

  public boolean getSupportZoom() {
    return supportZoom;
  }

  @Implementation
  public void setSupportZoom(boolean support) {
    supportZoom = support;
  }

  @Implementation
  public synchronized void setUserAgentString(String ua) {
    userAgentString = ua;
  }

  @Implementation
  public synchronized String getUserAgentString() {
    return userAgentString;
  }

  @Implementation
  public void setCacheMode(int mode) {
    this.cacheMode = mode;
  }

  @Implementation
  public int getCacheMode() {
    return cacheMode;
  }

  @Implementation
  public boolean getUseWideViewPort() {
    return useWideViewPort;
  }

  @Implementation
  public void setUseWideViewPort(boolean useWideViewPort) {
    this.useWideViewPort = useWideViewPort;
  }

  @Implementation
  public String getAppCachePath() {
    return appCachePath;
  }

  @Implementation
  public void setAppCachePath(String appCachePath) {
    this.appCachePath = appCachePath;
  }

  @Implementation
  public boolean getAppCacheEnabled() {
    return appCacheEnabled;
  }

  @Implementation
  public void setAppCacheEnabled(boolean appCacheEnabled) {
    this.appCacheEnabled = appCacheEnabled;
  }

  @Implementation
  public boolean getSaveFormData() {
    return saveFormData;
  }

  @Implementation
  public void setSaveFormData(boolean saveFormData) {
    this.saveFormData = saveFormData;
  }

  @Implementation
  public String getGeolocationDatabasePath() {
    return geolocationDatabasePath;
  }

  @Implementation
  public void setGeolocationDatabasePath(String geolocationDatabasePath) {
    this.geolocationDatabasePath = geolocationDatabasePath;
  }

  @Implementation
  public boolean getGeolocationEnabled() {
    return geolocationEnabled;
  }

  @Implementation
  public void setGeolocationEnabled(boolean geolocationEnabled) {
    this.geolocationEnabled = geolocationEnabled;
  }
}
