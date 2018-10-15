package org.robolectric.fakes;

import static com.google.common.truth.Truth.assertThat;

import android.webkit.WebSettings;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.internal.DoNotInstrument;

@DoNotInstrument
@RunWith(AndroidJUnit4.class)
public class RoboWebSettingsTest {
  private final RoboWebSettings webSettings = new RoboWebSettings();
  private static final boolean[] TRUE_AND_FALSE = {true, false};

  @Test
  public void testDefaults() {
    assertThat(webSettings.getAllowContentAccess()).isTrue();
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
    assertThat(webSettings.getMediaPlaybackRequiresUserGesture()).isTrue();
    assertThat(webSettings.getPluginState()).isEqualTo(WebSettings.PluginState.OFF);
    assertThat(webSettings.getSaveFormData()).isFalse();
    assertThat(webSettings.getTextZoom()).isEqualTo(100);
    assertThat(webSettings.getDefaultTextEncodingName()).isEqualTo("UTF-8");
    assertThat(webSettings.getDefaultFontSize()).isEqualTo(16);

    // deprecated methods
    assertThat(webSettings.getPluginsEnabled()).isFalse();

    // obsoleted methods
    assertThat(webSettings.getNeedInitialFocus()).isFalse();
    assertThat(webSettings.getSupportMultipleWindows()).isFalse();
    assertThat(webSettings.getSupportZoom()).isTrue();
  }

  @Test
  public void testAllowContentAccess() {
    for (boolean value : TRUE_AND_FALSE) {
      webSettings.setAllowContentAccess(value);
      assertThat(webSettings.getAllowContentAccess()).isEqualTo(value);
    }
  }

  @Test
  public void testAllowFileAccess() {
    for (boolean value : TRUE_AND_FALSE) {
      webSettings.setAllowFileAccess(value);
      assertThat(webSettings.getAllowFileAccess()).isEqualTo(value);
    }
  }

  @Test
  public void testAllowFileAccessFromFileURLs() {
    for (boolean value : TRUE_AND_FALSE) {
      webSettings.setAllowFileAccessFromFileURLs(value);
      assertThat(webSettings.getAllowFileAccessFromFileURLs()).isEqualTo(value);
    }
  }

  @Test
  public void testAllowUniversalAccessFromFileURLs() {
    for (boolean value : TRUE_AND_FALSE) {
      webSettings.setAllowUniversalAccessFromFileURLs(value);
      assertThat(webSettings.getAllowUniversalAccessFromFileURLs()).isEqualTo(value);
    }
  }

  @Test
  public void testBlockNetworkImage() {
    for (boolean value : TRUE_AND_FALSE) {
      webSettings.setBlockNetworkImage(value);
      assertThat(webSettings.getBlockNetworkImage()).isEqualTo(value);
    }
  }

  @Test
  public void testBlockNetworkLoads() {
    for (boolean value : TRUE_AND_FALSE) {
      webSettings.setBlockNetworkLoads(value);
      assertThat(webSettings.getBlockNetworkLoads()).isEqualTo(value);
    }
  }

  @Test
  public void testBuiltInZoomControls() {
    for (boolean value : TRUE_AND_FALSE) {
      webSettings.setBuiltInZoomControls(value);
      assertThat(webSettings.getBuiltInZoomControls()).isEqualTo(value);
    }
  }

  @Test
  public void testDatabaseEnabled() {
    for (boolean value : TRUE_AND_FALSE) {
      webSettings.setDatabaseEnabled(value);
      assertThat(webSettings.getDatabaseEnabled()).isEqualTo(value);
    }
  }

  @Test
  public void testDomStorageEnabled() {
    for (boolean value : TRUE_AND_FALSE) {
      webSettings.setDomStorageEnabled(value);
      assertThat(webSettings.getDomStorageEnabled()).isEqualTo(value);
    }
  }

  @Test
  public void testJavaScriptEnabled() {
    for (boolean value : TRUE_AND_FALSE) {
      webSettings.setJavaScriptEnabled(value);
      assertThat(webSettings.getJavaScriptEnabled()).isEqualTo(value);
    }
  }

  @Test
  public void testLightTouchEnabled() {
    for (boolean value : TRUE_AND_FALSE) {
      webSettings.setLightTouchEnabled(value);
      assertThat(webSettings.getLightTouchEnabled()).isEqualTo(value);
    }
  }

  @Test
  public void testLoadWithOverviewMode() {
    for (boolean value : TRUE_AND_FALSE) {
      webSettings.setLoadWithOverviewMode(value);
      assertThat(webSettings.getLoadWithOverviewMode()).isEqualTo(value);
    }
  }

  @Test
  public void testMediaPlaybackRequiresUserGesture() throws Exception {
    for (boolean value : TRUE_AND_FALSE) {
      webSettings.setMediaPlaybackRequiresUserGesture(value);
      assertThat(webSettings.getMediaPlaybackRequiresUserGesture()).isEqualTo(value);
    }
  }

  @Test
  public void testNeedInitialFocus() {
    for (boolean value : TRUE_AND_FALSE) {
      webSettings.setNeedInitialFocus(value);
      assertThat(webSettings.getNeedInitialFocus()).isEqualTo(value);
    }
  }

  @Test
  public void testPluginsEnabled() {
    for (boolean value : TRUE_AND_FALSE) {
      webSettings.setPluginsEnabled(value);
      assertThat(webSettings.getPluginsEnabled()).isEqualTo(value);
    }
  }

  @Test
  public void testPluginState() {

    for (WebSettings.PluginState state : WebSettings.PluginState.values()) {
      webSettings.setPluginState(state);
      assertThat(webSettings.getPluginState()).isEqualTo(state);
    }
  }

  @Test
  public void testSupportMultipleWindows() {
    for (boolean value : TRUE_AND_FALSE) {
      webSettings.setSupportMultipleWindows(value);
      assertThat(webSettings.getSupportMultipleWindows()).isEqualTo(value);
    }
  }

  @Test
  public void testSupportZoom() {
    for (boolean value : TRUE_AND_FALSE) {
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
    for (boolean value : TRUE_AND_FALSE) {
      webSettings.setUseWideViewPort(value);
      assertThat(webSettings.getUseWideViewPort()).isEqualTo(value);
    }
  }

  @Test
  public void testSetAppCacheEnabled() throws Exception {
    for (boolean value : TRUE_AND_FALSE) {
      webSettings.setAppCacheEnabled(value);
      assertThat(webSettings.getAppCacheEnabled()).isEqualTo(value);
    }
  }

  @Test
  public void testSetGeolocationEnabled() throws Exception {
    for (boolean value : TRUE_AND_FALSE) {
      webSettings.setGeolocationEnabled(value);
      assertThat(webSettings.getGeolocationEnabled()).isEqualTo(value);
    }
  }

  @Test
  public void testSetSaveFormData() throws Exception {
    for (boolean value : TRUE_AND_FALSE) {
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
  public void testSetAppCacheMaxSize() throws Exception {
    webSettings.setAppCacheMaxSize(100);
    assertThat(webSettings.getAppCacheMaxSize()).isEqualTo(100);
  }

  @Test
  public void testSetGeolocationDatabasePath() throws Exception {
    webSettings.setGeolocationDatabasePath("new_path");
    assertThat(webSettings.getGeolocationDatabasePath()).isEqualTo("new_path");
  }

  @Test
  public void testSetJavascriptCanOpenWindowsAutomaticallyIsTrue() throws Exception {
    webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
    assertThat(webSettings.getJavaScriptCanOpenWindowsAutomatically()).isTrue();
  }

  @Test
  public void testSetJavascriptCanOpenWindowsAutomaticallyIsFalse() throws Exception {
    webSettings.setJavaScriptCanOpenWindowsAutomatically(false);
    assertThat(webSettings.getJavaScriptCanOpenWindowsAutomatically()).isFalse();
  }

  @Test
  public void testSetTextZoom() throws Exception {
    webSettings.setTextZoom(50);
    assertThat(webSettings.getTextZoom()).isEqualTo(50);
  }

  @Test
  public void setDefaultTextEncodingName_shouldGetSetValue() {
    webSettings.setDefaultTextEncodingName("UTF-16");
    assertThat(webSettings.getDefaultTextEncodingName()).isEqualTo("UTF-16");
  }

  @Test
  public void setDefaultFontSize_shouldGetSetValues() {
    webSettings.setDefaultFontSize(2);
    assertThat(webSettings.getDefaultFontSize()).isEqualTo(2);
  }
}
