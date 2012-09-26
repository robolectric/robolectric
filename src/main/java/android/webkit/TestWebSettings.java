package android.webkit;

import com.xtremelabs.robolectric.internal.DoNotInstrument;
import com.xtremelabs.robolectric.internal.Implementation;

/**
 * Concrete implementation of the abstract WebSettings class.
 */
@DoNotInstrument
public class TestWebSettings extends WebSettings {
	
    private boolean allowFileAccess = true;
    private boolean allowFileAccessFromFile = true;
    private boolean allowUniversalAccessFromFile = true;    
    private boolean blockNetworkImage = false;
    private boolean blockNetworkLoads = false;
    private boolean builtInZoomControls = true;
    private boolean databaseEnabled = false;
    private boolean domStorageEnabled = false;
    private boolean javaScriptEnabled = false;
    private boolean lightTouchEnabled = false;
    private boolean loadWithOverviewMode = false;
    private boolean needInitialFocus = false;
    private boolean pluginsEnabled = false;
    private WebSettings.PluginState pluginState = WebSettings.PluginState.OFF;
    private boolean supportMultipleWindows = false;
    private boolean supportZoom = true;

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
}
