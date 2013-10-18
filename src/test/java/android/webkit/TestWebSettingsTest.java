package android.webkit;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.TestRunners;
import org.robolectric.internal.DoNotInstrument;

import static org.fest.assertions.api.Assertions.assertThat;

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
    assertThat(webSettings.getAllowFileAccess()).isTrue();
    assertThat(webSettings.getAppCacheEnabled()).isFalse();
    assertThat(webSettings.getBlockNetworkImage()).isFalse();
    assertThat(webSettings.getBlockNetworkLoads()).isFalse();
    assertThat(webSettings.getBuiltInZoomControls()).isTrue();
    assertThat(webSettings.getDatabaseEnabled()).isFalse();
    assertThat(webSettings.getDomStorageEnabled()).isFalse();
    assertThat(webSettings.getGeolocationEnabled()).isFalse();
    assertThat(webSettings.getJavaScriptEnabled()).isFalse();
    assertThat(webSettings.getLightTouchEnabled()).isFalse();
    assertThat(webSettings.getLoadWithOverviewMode()).isFalse();
    assertThat(webSettings.getPluginState()).isEqualTo(WebSettings.PluginState.OFF);
    assertThat(webSettings.getSaveFormData()).isFalse();

    // deprecated methods
    assertThat(webSettings.getPluginsEnabled()).isFalse();

    // obsoleted methods
    assertThat(webSettings.getNeedInitialFocus()).isFalse();
    assertThat(webSettings.getSupportMultipleWindows()).isFalse();
    assertThat(webSettings.getSupportZoom()).isTrue();
  }

  @Test
  public void testAllowFileAccess() {
    for (boolean value : trueAndFalse) {
      webSettings.setAllowFileAccess(value);
      assertThat(webSettings.getAllowFileAccess()).isEqualTo(value);
    }
  }

  @Test
  public void testAllowFileAccessFromFileURLs() {
    for (boolean value : trueAndFalse) {
      webSettings.setAllowFileAccessFromFileURLs(value);
      assertThat(webSettings.getAllowFileAccessFromFileURLs()).isEqualTo(value);
    }
  }

  @Test
  public void testAllowUniversalAccessFromFileURLs() {
    for (boolean value : trueAndFalse) {
      webSettings.setAllowUniversalAccessFromFileURLs(value);
      assertThat(webSettings.getAllowUniversalAccessFromFileURLs()).isEqualTo(value);
    }
  }

  @Test
  public void testBlockNetworkImage() {
    for (boolean value : trueAndFalse) {
      webSettings.setBlockNetworkImage(value);
      assertThat(webSettings.getBlockNetworkImage()).isEqualTo(value);
    }
  }

  @Test
  public void testBlockNetworkLoads() {
    for (boolean value : trueAndFalse) {
      webSettings.setBlockNetworkLoads(value);
      assertThat(webSettings.getBlockNetworkLoads()).isEqualTo(value);
    }
  }

  @Test
  public void testBuiltInZoomControls() {
    for (boolean value : trueAndFalse) {
      webSettings.setBuiltInZoomControls(value);
      assertThat(webSettings.getBuiltInZoomControls()).isEqualTo(value);
    }
  }

  @Test
  public void testDatabaseEnabled() {
    for (boolean value : trueAndFalse) {
      webSettings.setDatabaseEnabled(value);
      assertThat(webSettings.getDatabaseEnabled()).isEqualTo(value);
    }
  }

  @Test
  public void testDomStorageEnabled() {
    for (boolean value : trueAndFalse) {
      webSettings.setDomStorageEnabled(value);
      assertThat(webSettings.getDomStorageEnabled()).isEqualTo(value);
    }
  }

  @Test
  public void testJavaScriptEnabled() {
    for (boolean value : trueAndFalse) {
      webSettings.setJavaScriptEnabled(value);
      assertThat(webSettings.getJavaScriptEnabled()).isEqualTo(value);
    }
  }

  @Test
  public void testLightTouchEnabled() {
    for (boolean value : trueAndFalse) {
      webSettings.setLightTouchEnabled(value);
      assertThat(webSettings.getLightTouchEnabled()).isEqualTo(value);
    }
  }

  @Test
  public void testLoadWithOverviewMode() {
    for (boolean value : trueAndFalse) {
      webSettings.setLoadWithOverviewMode(value);
      assertThat(webSettings.getLoadWithOverviewMode()).isEqualTo(value);
    }
  }

  @Test
  public void testNeedInitialFocus() {
    for (boolean value : trueAndFalse) {
      webSettings.setNeedInitialFocus(value);
      assertThat(webSettings.getNeedInitialFocus()).isEqualTo(value);
    }
  }

  @Test
  public void testPluginsEnabled() {
    for (boolean value : trueAndFalse) {
      webSettings.setPluginsEnabled(value);
      assertThat(webSettings.getPluginsEnabled()).isEqualTo(value);
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
      assertThat(webSettings.getPluginState()).isEqualTo(state);
    }
  }

  @Test
  public void testSupportMultipleWindows() {
    for (boolean value : trueAndFalse) {
      webSettings.setSupportMultipleWindows(value);
      assertThat(webSettings.getSupportMultipleWindows()).isEqualTo(value);
    }
  }

  @Test
  public void testSupportZoom() {
    for (boolean value : trueAndFalse) {
      webSettings.setSupportZoom(value);
      assertThat(webSettings.getSupportZoom()).isEqualTo(value);
    }
  }

  @Test
  public void testSetCacheMode() throws Exception {
    webSettings.setCacheMode(7);
    assertThat(webSettings.getCacheMode()).isEqualTo(7);
  }

  @Test
  public void testSetUseWideViewPort() throws Exception {
    for (boolean value : trueAndFalse) {
      webSettings.setUseWideViewPort(value);
      assertThat(webSettings.getUseWideViewPort()).isEqualTo(value);
    }
  }

  @Test
  public void testSetAppCacheEnabled() throws Exception {
    for (boolean value : trueAndFalse) {
      webSettings.setAppCacheEnabled(value);
      assertThat(webSettings.getAppCacheEnabled()).isEqualTo(value);
    }
  }

  @Test
  public void testSetGeolocationEnabled() throws Exception {
    for (boolean value : trueAndFalse) {
      webSettings.setGeolocationEnabled(value);
      assertThat(webSettings.getGeolocationEnabled()).isEqualTo(value);
    }
  }

  @Test
  public void testSetSaveFormData() throws Exception {
    for (boolean value : trueAndFalse) {
      webSettings.setSaveFormData(value);
      assertThat(webSettings.getSaveFormData()).isEqualTo(value);
    }
  }

  @Test
  public void testSetDatabasePath() throws Exception {
    webSettings.setDatabasePath("new_path");
    assertThat(webSettings.getDatabasePath()).isEqualTo("new_path");
  }

  @Test
  public void testSetRenderPriority() throws Exception {
    webSettings.setRenderPriority(WebSettings.RenderPriority.HIGH);
    assertThat(webSettings.getRenderPriority()).isEqualTo(WebSettings.RenderPriority.HIGH);
  }

  @Test
  public void testSetAppCachePath() throws Exception {
    webSettings.setAppCachePath("new_path");
    assertThat(webSettings.getAppCachePath()).isEqualTo("new_path");
  }

  @Test
  public void testSetGeolocationDatabasePath() throws Exception {
    webSettings.setGeolocationDatabasePath("new_path");
    assertThat(webSettings.getGeolocationDatabasePath()).isEqualTo("new_path");
  }
}
