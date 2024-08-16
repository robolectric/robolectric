package org.robolectric.fakes;

import android.webkit.WebSettings;

/** Robolectric implementation of {@link android.webkit.WebSettings}. */
public class RoboWebSettings extends WebSettings {
  private boolean blockNetworkImage = false;
  private boolean javaScriptEnabled = false;
  private boolean javaScriptCanOpenWindowAutomatically = false;
  private boolean lightTouchEnabled = false;
  private boolean needInitialFocus = false;
  private RenderPriority renderPriority = RenderPriority.NORMAL;
  private boolean pluginsEnabled = false;
  private boolean saveFormData = false;
  private boolean supportMultipleWindows = false;
  private boolean supportZoom = true;
  private boolean useWideViewPort = false;
  private int cacheMode;
  private WebSettings.LayoutAlgorithm layoutAlgorithm = WebSettings.LayoutAlgorithm.NARROW_COLUMNS;
  private String defaultTextEncoding = "UTF-8";
  private int defaultFontSize = 16;
  private boolean loadsImagesAutomatically;
  private int defaultFixedFontSize;
  private int minimumLogicalFontSize;
  private int minimumFontSize;
  private String fantasyFontFamily;
  private String cursiveFontFamily;
  private String serifFontFamily;
  private String sansSerifFontFamily;
  private String fixedFontFamily;
  private String standardFontFamily;
  private boolean savePassword;
  private int userAgent;
  private boolean navDump;
  private int forceDark;

  @Override
  public synchronized boolean getBlockNetworkImage() {
    return blockNetworkImage;
  }

  @Override
  public synchronized void setBlockNetworkImage(boolean flag) {
    blockNetworkImage = flag;
  }

  @Override
  public synchronized boolean getJavaScriptEnabled() {
    return javaScriptEnabled;
  }

  @Override
  public synchronized void setJavaScriptEnabled(boolean flag) {
    javaScriptEnabled = flag;
  }

  @Override
  public boolean getLightTouchEnabled() {
    return lightTouchEnabled;
  }

  @Override
  public void setLightTouchEnabled(boolean flag) {
    lightTouchEnabled = flag;
  }

  public boolean getNeedInitialFocus() {
    return needInitialFocus;
  }

  @Override
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

  @Override
  public synchronized boolean getPluginsEnabled() {
    return pluginsEnabled;
  }

  @Override
  public synchronized void setPluginsEnabled(boolean flag) {
    pluginsEnabled = flag;
  }

  public boolean getSupportMultipleWindows() {
    return supportMultipleWindows;
  }

  @Override
  public synchronized void setSupportMultipleWindows(boolean support) {
    supportMultipleWindows = support;
  }

  public boolean getSupportZoom() {
    return supportZoom;
  }

  @Override
  public void setSupportZoom(boolean support) {
    supportZoom = support;
  }

  @Override
  public void setCacheMode(int mode) {
    this.cacheMode = mode;
  }

  @Override
  public int getCacheMode() {
    return cacheMode;
  }

  @Override
  public boolean getUseWideViewPort() {
    return useWideViewPort;
  }

  @Override
  public void setUseWideViewPort(boolean useWideViewPort) {
    this.useWideViewPort = useWideViewPort;
  }

  @Override
  public boolean getSaveFormData() {
    return saveFormData;
  }

  @Override
  public void setSaveFormData(boolean saveFormData) {
    this.saveFormData = saveFormData;
  }

  @Override
  public void setJavaScriptCanOpenWindowsAutomatically(
      boolean javaScriptCanOpenWindowAutomatically) {
    this.javaScriptCanOpenWindowAutomatically = javaScriptCanOpenWindowAutomatically;
  }

  @Override
  public boolean getJavaScriptCanOpenWindowsAutomatically() {
    return this.javaScriptCanOpenWindowAutomatically;
  }

  @Override
  public synchronized void setLayoutAlgorithm(WebSettings.LayoutAlgorithm algorithm) {
    this.layoutAlgorithm = algorithm;
  }

  @Override
  public String getDefaultTextEncodingName() {
    return this.defaultTextEncoding;
  }

  @Override
  public void setDefaultTextEncodingName(String defaultTextEncoding) {
    this.defaultTextEncoding = defaultTextEncoding;
  }

  @Override
  public int getDefaultFontSize() {
    return defaultFontSize;
  }

  @Override
  public void setDefaultFontSize(int defaultFontSize) {
    this.defaultFontSize = defaultFontSize;
  }

  @Override
  public boolean getLoadsImagesAutomatically() {
    return loadsImagesAutomatically;
  }

  @Override
  public void setLoadsImagesAutomatically(boolean loadsImagesAutomatically) {
    this.loadsImagesAutomatically = loadsImagesAutomatically;
  }

  @Override
  public int getDefaultFixedFontSize() {
    return defaultFixedFontSize;
  }

  @Override
  public void setDefaultFixedFontSize(int defaultFixedFontSize) {
    this.defaultFixedFontSize = defaultFixedFontSize;
  }

  @Override
  public int getMinimumLogicalFontSize() {
    return minimumLogicalFontSize;
  }

  @Override
  public void setMinimumLogicalFontSize(int minimumLogicalFontSize) {
    this.minimumLogicalFontSize = minimumLogicalFontSize;
  }

  @Override
  public int getMinimumFontSize() {
    return minimumFontSize;
  }

  @Override
  public void setMinimumFontSize(int minimumFontSize) {
    this.minimumFontSize = minimumFontSize;
  }

  @Override
  public String getFantasyFontFamily() {
    return fantasyFontFamily;
  }

  @Override
  public void setFantasyFontFamily(String fantasyFontFamily) {
    this.fantasyFontFamily = fantasyFontFamily;
  }

  @Override
  public String getCursiveFontFamily() {
    return cursiveFontFamily;
  }

  @Override
  public void setCursiveFontFamily(String cursiveFontFamily) {
    this.cursiveFontFamily = cursiveFontFamily;
  }

  @Override
  public String getSerifFontFamily() {
    return serifFontFamily;
  }

  @Override
  public void setSerifFontFamily(String serifFontFamily) {
    this.serifFontFamily = serifFontFamily;
  }

  @Override
  public String getSansSerifFontFamily() {
    return sansSerifFontFamily;
  }

  @Override
  public void setSansSerifFontFamily(String sansSerifFontFamily) {
    this.sansSerifFontFamily = sansSerifFontFamily;
  }

  @Override
  public String getFixedFontFamily() {
    return fixedFontFamily;
  }

  @Override
  public void setFixedFontFamily(String fixedFontFamily) {
    this.fixedFontFamily = fixedFontFamily;
  }

  @Override
  public String getStandardFontFamily() {
    return standardFontFamily;
  }

  @Override
  public void setStandardFontFamily(String standardFontFamily) {
    this.standardFontFamily = standardFontFamily;
  }

  @Override
  public LayoutAlgorithm getLayoutAlgorithm() {
    return layoutAlgorithm;
  }

  @Override
  public boolean supportMultipleWindows() {
    return supportMultipleWindows;
  }

  @Override
  public boolean getSavePassword() {
    return savePassword;
  }

  @Override
  public void setSavePassword(boolean savePassword) {
    this.savePassword = savePassword;
  }

  @Override
  public boolean supportZoom() {
    return supportZoom;
  }

  @Override
  public int getUserAgent() {
    return userAgent;
  }

  @Override
  public void setUserAgent(int userAgent) {
    this.userAgent = userAgent;
  }

  @Override
  public boolean getNavDump() {
    return navDump;
  }

  @Override
  public void setNavDump(boolean navDump) {
    this.navDump = navDump;
  }

  private boolean allowFileAccess = true;
  private boolean builtInZoomControls = true;
  private String userAgentString =
      "Mozilla/5.0 (Linux; U; Android 4.0.3; ko-kr; LG-L160L Build/IML74K) AppleWebkit/534.30"
          + " (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30";

  @Override
  public boolean getAllowFileAccess() {
    return allowFileAccess;
  }

  @Override
  public void setAllowFileAccess(boolean allow) {
    allowFileAccess = allow;
  }

  @Override
  public boolean getBuiltInZoomControls() {
    return builtInZoomControls;
  }

  @Override
  public void setBuiltInZoomControls(boolean enabled) {
    builtInZoomControls = enabled;
  }

  @Override
  public synchronized void setUserAgentString(String ua) {
    userAgentString = ua;
  }

  @Override
  public synchronized String getUserAgentString() {
    return userAgentString;
  }

  // End API 3

  private boolean databaseEnabled = false;
  private String databasePath = "database";
  private String geolocationDatabasePath = "geolocation";
  private boolean geolocationEnabled = false;

  @Override
  public synchronized boolean getDatabaseEnabled() {
    return databaseEnabled;
  }

  @Override
  public synchronized void setDatabaseEnabled(boolean flag) {
    databaseEnabled = flag;
  }

  @Override
  public synchronized void setDatabasePath(String path) {
    databasePath = path;
  }

  @Override
  public synchronized String getDatabasePath() {
    return databasePath;
  }

  public String getGeolocationDatabasePath() {
    return geolocationDatabasePath;
  }

  @Override
  public void setGeolocationDatabasePath(String geolocationDatabasePath) {
    this.geolocationDatabasePath = geolocationDatabasePath;
  }

  public boolean getGeolocationEnabled() {
    return geolocationEnabled;
  }

  @Override
  public void setGeolocationEnabled(boolean geolocationEnabled) {
    this.geolocationEnabled = geolocationEnabled;
  }

  // End API 5

  private ZoomDensity defaultZoom;
  private boolean domStorageEnabled = false;
  private boolean loadWithOverviewMode = false;
  private boolean appCacheEnabled = false;
  private long appCacheMaxSize;
  private String appCachePath = "appcache";

  @Override
  public void setDefaultZoom(ZoomDensity zoom) {
    this.defaultZoom = zoom;
  }

  @Override
  public ZoomDensity getDefaultZoom() {
    return defaultZoom;
  }

  @Override
  public synchronized boolean getDomStorageEnabled() {
    return domStorageEnabled;
  }

  @Override
  public synchronized void setDomStorageEnabled(boolean flag) {
    domStorageEnabled = flag;
  }

  @Override
  public boolean getLoadWithOverviewMode() {
    return loadWithOverviewMode;
  }

  @Override
  public void setLoadWithOverviewMode(boolean flag) {
    loadWithOverviewMode = flag;
  }

  public boolean getAppCacheEnabled() {
    return appCacheEnabled;
  }

  @Override
  public void setAppCacheEnabled(boolean appCacheEnabled) {
    this.appCacheEnabled = appCacheEnabled;
  }

  @Override
  public void setAppCacheMaxSize(long appCacheMaxSize) {
    this.appCacheMaxSize = appCacheMaxSize;
  }

  public long getAppCacheMaxSize() {
    return appCacheMaxSize;
  }

  public String getAppCachePath() {
    return appCachePath;
  }

  @Override
  public void setAppCachePath(String appCachePath) {
    this.appCachePath = appCachePath;
  }

  // End API 7

  private boolean blockNetworkLoads = false;
  private WebSettings.PluginState pluginState = WebSettings.PluginState.OFF;

  @Override
  public synchronized boolean getBlockNetworkLoads() {
    return blockNetworkLoads;
  }

  @Override
  public synchronized void setBlockNetworkLoads(boolean flag) {
    blockNetworkLoads = flag;
  }

  @Override
  public synchronized WebSettings.PluginState getPluginState() {
    return pluginState;
  }

  @Override
  public synchronized void setPluginState(WebSettings.PluginState state) {
    pluginState = state;
  }

  // End API 8

  private boolean useWebViewBackgroundForOverscrollBackground;

  @Override
  public boolean getUseWebViewBackgroundForOverscrollBackground() {
    return useWebViewBackgroundForOverscrollBackground;
  }

  @Override
  public void setUseWebViewBackgroundForOverscrollBackground(
      boolean useWebViewBackgroundForOverscrollBackground) {
    this.useWebViewBackgroundForOverscrollBackground = useWebViewBackgroundForOverscrollBackground;
  }

  // End API 9

  private boolean enableSmoothTransition;
  private boolean allowContentAccess = true;
  private boolean displayZoomControls;

  @Override
  public boolean enableSmoothTransition() {
    return enableSmoothTransition;
  }

  @Override
  public void setEnableSmoothTransition(boolean enableSmoothTransition) {
    this.enableSmoothTransition = enableSmoothTransition;
  }

  @Override
  public void setAllowContentAccess(boolean allow) {
    allowContentAccess = allow;
  }

  @Override
  public boolean getAllowContentAccess() {
    return allowContentAccess;
  }

  @Override
  public void setDisplayZoomControls(boolean enabled) {
    displayZoomControls = enabled;
  }

  @Override
  public boolean getDisplayZoomControls() {
    return displayZoomControls;
  }

  // End API 11

  private int textZoom = 100;

  @Override
  public int getTextZoom() {
    return textZoom;
  }

  @Override
  public void setTextZoom(int textZoom) {
    this.textZoom = textZoom;
  }

  // End API 14

  private boolean allowFileAccessFromFile = true;
  private boolean allowUniversalAccessFromFile = true;

  @Override
  public boolean getAllowFileAccessFromFileURLs() {
    return allowFileAccessFromFile;
  }

  @Override
  public void setAllowFileAccessFromFileURLs(boolean allow) {
    allowFileAccessFromFile = allow;
  }

  @Override
  public boolean getAllowUniversalAccessFromFileURLs() {
    return allowUniversalAccessFromFile;
  }

  @Override
  public void setAllowUniversalAccessFromFileURLs(boolean allow) {
    allowUniversalAccessFromFile = allow;
  }

  // End API 16

  private boolean mediaPlaybackRequiresUserGesture = true;

  @Override
  public boolean getMediaPlaybackRequiresUserGesture() {
    return mediaPlaybackRequiresUserGesture;
  }

  @Override
  public void setMediaPlaybackRequiresUserGesture(boolean require) {
    mediaPlaybackRequiresUserGesture = require;
  }

  // End API 17

  private int mixedContentMode;
  private boolean acceptThirdPartyCookies;
  private boolean videoOverlayForEmbeddedEncryptedVideoEnabled;

  @Override
  public void setMixedContentMode(int mixedContentMode) {
    this.mixedContentMode = mixedContentMode;
  }

  @Override
  public int getMixedContentMode() {
    return mixedContentMode;
  }

  @Override
  public void setVideoOverlayForEmbeddedEncryptedVideoEnabled(boolean b) {
    videoOverlayForEmbeddedEncryptedVideoEnabled = b;
  }

  @Override
  public boolean getVideoOverlayForEmbeddedEncryptedVideoEnabled() {
    return videoOverlayForEmbeddedEncryptedVideoEnabled;
  }

  @Override
  public boolean getAcceptThirdPartyCookies() {
    return acceptThirdPartyCookies;
  }

  @Override
  public void setAcceptThirdPartyCookies(boolean acceptThirdPartyCookies) {
    this.acceptThirdPartyCookies = acceptThirdPartyCookies;
  }

  // End API 21

  @Override
  public void setOffscreenPreRaster(boolean enabled) {}

  @Override
  public boolean getOffscreenPreRaster() {
    return false;
  }

  // End API 23

  @Override
  public int getDisabledActionModeMenuItems() {
    return 0;
  }

  @Override
  public void setDisabledActionModeMenuItems(int menuItems) {}

  // End API 24.

  @Override
  public boolean getSafeBrowsingEnabled() {
    return false;
  }

  @Override
  public void setSafeBrowsingEnabled(boolean enabled) {}

  // End API 26

  @Override
  public int getForceDark() {
    return forceDark;
  }

  @Override
  public void setForceDark(int forceDark) {
    this.forceDark = forceDark;
  }

  // End API 29
}
