package android.webkit;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.TestRunners;
import org.robolectric.internal.DoNotInstrument;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

@RunWith(TestRunners.WithDefaults.class) @DoNotInstrument
public class TestWebSettingsTest {

    private TestWebSettings webSettings;

    private boolean[] trueAndFalse = {true, false};

    @Before
    public void setUp() throws Exception {
        webSettings = new TestWebSettings();
    }

    @Test
    public void testDefaults() {
        assertThat(webSettings.getAllowFileAccess(), equalTo(true));
        assertThat(webSettings.getBlockNetworkImage(), equalTo(false));
        assertThat(webSettings.getBlockNetworkLoads(), equalTo(false));
        assertThat(webSettings.getBuiltInZoomControls(), equalTo(true));
        assertThat(webSettings.getDatabaseEnabled(), equalTo(false));
        assertThat(webSettings.getDomStorageEnabled(), equalTo(false));
        assertThat(webSettings.getJavaScriptEnabled(), equalTo(false));
        assertThat(webSettings.getLightTouchEnabled(), equalTo(false));
        assertThat(webSettings.getLoadWithOverviewMode(), equalTo(false));
        assertThat(webSettings.getPluginState(), equalTo(WebSettings.PluginState.OFF));

        // deprecated methods
        assertThat(webSettings.getPluginsEnabled(), equalTo(false));

        // obsoleted methods
        assertThat(webSettings.getNeedInitialFocus(), equalTo(false));
        assertThat(webSettings.getSupportMultipleWindows(), equalTo(false));
        assertThat(webSettings.getSupportZoom(), equalTo(true));
    }

    @Test
    public void testAllowFileAccess() {
        for (boolean value : trueAndFalse) {
            webSettings.setAllowFileAccess(value);
            assertThat(webSettings.getAllowFileAccess(), equalTo(value));
        }
    }

    @Test
    public void testAllowFileAccessFromFileURLs() {
        for (boolean value : trueAndFalse) {
            webSettings.setAllowFileAccessFromFileURLs(value);
            assertThat(webSettings.getAllowFileAccessFromFileURLs(), equalTo(value));
        }
    }
    
    @Test
    public void testAllowUniversalAccessFromFileURLs() {
        for (boolean value : trueAndFalse) {
            webSettings.setAllowUniversalAccessFromFileURLs(value);
            assertThat(webSettings.getAllowUniversalAccessFromFileURLs(), equalTo(value));
        }
    }

    @Test
    public void testBlockNetworkImage() {
        for (boolean value : trueAndFalse) {
            webSettings.setBlockNetworkImage(value);
            assertThat(webSettings.getBlockNetworkImage(), equalTo(value));
        }
    }

    @Test
    public void testBlockNetworkLoads() {
        for (boolean value : trueAndFalse) {
            webSettings.setBlockNetworkLoads(value);
            assertThat(webSettings.getBlockNetworkLoads(), equalTo(value));
        }
    }

    @Test
    public void testBuiltInZoomControls() {
        for (boolean value : trueAndFalse) {
            webSettings.setBuiltInZoomControls(value);
            assertThat(webSettings.getBuiltInZoomControls(), equalTo(value));
        }
    }

    @Test
    public void testDatabaseEnabled() {
        for (boolean value : trueAndFalse) {
            webSettings.setDatabaseEnabled(value);
            assertThat(webSettings.getDatabaseEnabled(), equalTo(value));
        }
    }

    @Test
    public void testDomStorageEnabled() {
        for (boolean value : trueAndFalse) {
            webSettings.setDomStorageEnabled(value);
            assertThat(webSettings.getDomStorageEnabled(), equalTo(value));
        }
    }

    @Test
    public void testJavaScriptEnabled() {
        for (boolean value : trueAndFalse) {
            webSettings.setJavaScriptEnabled(value);
            assertThat(webSettings.getJavaScriptEnabled(), equalTo(value));
        }
    }

    @Test
    public void testLightTouchEnabled() {
        for (boolean value : trueAndFalse) {
            webSettings.setLightTouchEnabled(value);
            assertThat(webSettings.getLightTouchEnabled(), equalTo(value));
        }
    }

    @Test
    public void testLoadWithOverviewMode() {
        for (boolean value : trueAndFalse) {
            webSettings.setLoadWithOverviewMode(value);
            assertThat(webSettings.getLoadWithOverviewMode(), equalTo(value));
        }
    }

    @Test
    public void testNeedInitialFocus() {
        for (boolean value : trueAndFalse) {
            webSettings.setNeedInitialFocus(value);
            assertThat(webSettings.getNeedInitialFocus(), equalTo(value));
        }
    }

    @Test
    public void testPluginsEnabled() {
        for (boolean value : trueAndFalse) {
            webSettings.setPluginsEnabled(value);
            assertThat(webSettings.getPluginsEnabled(), equalTo(value));
        }
    }

    @Test
    public void testPluginState() {
        WebSettings.PluginState[] states = {
                WebSettings.PluginState.OFF,
                WebSettings.PluginState.ON,
                WebSettings.PluginState.ON_DEMAND
        };

        for (WebSettings.PluginState state : states) {
            webSettings.setPluginState(state);
            assertThat(webSettings.getPluginState(), equalTo(state));
        }
    }

    @Test
    public void testSupportMultipleWindows() {
        for (boolean value : trueAndFalse) {
            webSettings.setSupportMultipleWindows(value);
            assertThat(webSettings.getSupportMultipleWindows(), equalTo(value));
        }
    }

    @Test
    public void testSupportZoom() {
        for (boolean value : trueAndFalse) {
            webSettings.setSupportZoom(value);
            assertThat(webSettings.getSupportZoom(), equalTo(value));
        }
    }
}
