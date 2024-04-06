package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.BAKLAVA;
import static android.os.Build.VERSION_CODES.CINNAMON_BUN;
import static android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE;
import static android.os.Build.VERSION_CODES.VANILLA_ICE_CREAM;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;

import android.net.http.HttpEngine;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = {UPSIDE_DOWN_CAKE, VANILLA_ICE_CREAM, BAKLAVA, CINNAMON_BUN})
public class ShadowHttpEngineMissingCronetTest {

  @Test
  public void cronetFallbackIsNotOnClasspath() {
    assertThrows(
        ClassNotFoundException.class,
        () -> Class.forName("org.chromium.net.impl.JavaCronetProvider"));
  }

  @Test
  public void builder_withoutCronetFallback_throwsActionableError() {
    IllegalStateException error =
        assertThrows(
            IllegalStateException.class,
            () -> new HttpEngine.Builder(RuntimeEnvironment.getApplication()));
    assertThat(error).hasCauseThat().isInstanceOf(ClassNotFoundException.class);
    assertThat(error).hasMessageThat().contains("org.chromium.net:cronet-fallback");
    assertThat(error).hasMessageThat().contains("testRuntimeOnly");
    assertThat(error).hasMessageThat().contains("google()");
    assertThat(error).hasMessageThat().contains("ShadowHttpEngine.setCronetEngineBuilder");
  }

  @Test
  public void setCronetEngineBuilder_withoutCronet_rejectsNonNullAndAllowsNull() {
    assertThrows(
        IllegalStateException.class, () -> ShadowHttpEngine.setCronetEngineBuilder("invalid"));
    ShadowHttpEngine.setCronetEngineBuilder(null);
  }

  @Test
  public void getVersionString_worksWithoutCronet() {
    assertThat(HttpEngine.getVersionString()).isNotEmpty();
  }

  @Test
  @Config(sdk = {BAKLAVA, CINNAMON_BUN})
  public void preload_worksWithoutCronet() {
    HttpEngine.preload();
    assertThrows(IllegalStateException.class, HttpEngine::preload);
  }
}
