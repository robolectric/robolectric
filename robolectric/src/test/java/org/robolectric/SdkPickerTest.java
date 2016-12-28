package org.robolectric;

import org.junit.Before;
import org.junit.Test;
import org.robolectric.annotation.Config;
import org.robolectric.internal.SdkConfig;
import org.robolectric.manifest.AndroidManifest;

import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SdkPickerTest {
  private static final int[] sdkInts = { 16, 17, 18, 19, 21, 22, 23 };
  private AndroidManifest appManifest;
  private SdkPicker sdkPicker;

  @Before
  public void setUp() throws Exception {
    appManifest = mock(AndroidManifest.class);
    sdkPicker = new SdkPicker(new Properties(), sdkInts);
  }

  @Test
  public void withDefaultSdkConfig_shouldUseTargetSdkFromAndroidManifest() throws Exception {
    when(appManifest.getTargetSdkVersion()).thenReturn(22);
    assertThat(sdkPicker.selectSdks(new Config.Builder().build(), appManifest))
        .containsExactly(new SdkConfig(22));
  }

  @Test
  public void withAllSdksConfig_shouldUseFullSdkRangeFromAndroidManifest() throws Exception {
    when(appManifest.getTargetSdkVersion()).thenReturn(22);
    when(appManifest.getMinSdkVersion()).thenReturn(19);
    when(appManifest.getMaxSdkVersion()).thenReturn(23);
    assertThat(sdkPicker.selectSdks(new Config.Builder().setSdk(Config.ALL_SDKS).build(), appManifest))
        .containsExactly(new SdkConfig(19), new SdkConfig(21), new SdkConfig(22), new SdkConfig(23));
  }

  @Test
  public void withAllSdksConfigAndNoMinSdkVersion_shouldUseFullSdkRangeFromAndroidManifest() throws Exception {
    when(appManifest.getTargetSdkVersion()).thenReturn(22);
    when(appManifest.getMinSdkVersion()).thenReturn(1);
    when(appManifest.getMaxSdkVersion()).thenReturn(22);
    assertThat(sdkPicker.selectSdks(new Config.Builder().setSdk(Config.ALL_SDKS).build(), appManifest))
        .containsExactly(new SdkConfig(16), new SdkConfig(17), new SdkConfig(18), new SdkConfig(19),
            new SdkConfig(21), new SdkConfig(22));
  }

  @Test
  public void withAllSdksConfigAndNoMaxSdkVersion_shouldUseFullSdkRangeFromAndroidManifest() throws Exception {
    when(appManifest.getTargetSdkVersion()).thenReturn(22);
    when(appManifest.getMinSdkVersion()).thenReturn(19);
    when(appManifest.getMaxSdkVersion()).thenReturn(null);
    assertThat(sdkPicker.selectSdks(new Config.Builder().setSdk(Config.ALL_SDKS).build(), appManifest))
        .containsExactly(new SdkConfig(19), new SdkConfig(21), new SdkConfig(22), new SdkConfig(23));
  }

  @Test
  public void shouldClipSdkRangeFromAndroidManifest() throws Exception {
    when(appManifest.getTargetSdkVersion()).thenReturn(1);
    when(appManifest.getMinSdkVersion()).thenReturn(1);
    when(appManifest.getMaxSdkVersion()).thenReturn(null);
    assertThat(sdkPicker.selectSdks(new Config.Builder().build(), appManifest))
        .containsExactly(new SdkConfig(16));
  }

  @Test
  public void withMinSdkConfig_shouldClipSdkRangeFromAndroidManifest() throws Exception {
    when(appManifest.getTargetSdkVersion()).thenReturn(22);
    when(appManifest.getMinSdkVersion()).thenReturn(19);
    when(appManifest.getMaxSdkVersion()).thenReturn(23);
    assertThat(sdkPicker.selectSdks(new Config.Builder().setMinSdk(21).build(), appManifest))
        .containsExactly(new SdkConfig(21), new SdkConfig(22), new SdkConfig(23));
  }

  @Test
  public void withMaxSdkConfig_shouldUseSdkRangeFromAndroidManifest() throws Exception {
    when(appManifest.getTargetSdkVersion()).thenReturn(22);
    when(appManifest.getMinSdkVersion()).thenReturn(19);
    when(appManifest.getMaxSdkVersion()).thenReturn(23);
    assertThat(sdkPicker.selectSdks(new Config.Builder().setMaxSdk(21).build(), appManifest))
        .containsExactly(new SdkConfig(19), new SdkConfig(21));
  }

  @Test
  public void withExplicitSdkConfig_selectSdks() throws Exception {
    when(appManifest.getTargetSdkVersion()).thenReturn(21);
    when(appManifest.getMinSdkVersion()).thenReturn(19);
    when(appManifest.getMaxSdkVersion()).thenReturn(22);

    assertThat(sdkPicker.selectSdks(new Config.Builder().setSdk(21).build(), appManifest))
        .containsExactly(new SdkConfig(21));
    assertThat(sdkPicker.selectSdks(new Config.Builder().setSdk(Config.OLDEST_SDK).build(), appManifest))
        .containsExactly(new SdkConfig(19));
    assertThat(sdkPicker.selectSdks(new Config.Builder().setSdk(Config.TARGET_SDK).build(), appManifest))
        .containsExactly(new SdkConfig(21));
    assertThat(sdkPicker.selectSdks(new Config.Builder().setSdk(Config.NEWEST_SDK).build(), appManifest))
        .containsExactly(new SdkConfig(22));

    assertThat(sdkPicker.selectSdks(new Config.Builder().setSdk(16).build(), appManifest))
        .containsExactly(new SdkConfig(16));
    assertThat(sdkPicker.selectSdks(new Config.Builder().setSdk(23).build(), appManifest))
        .containsExactly(new SdkConfig(23));
  }
}