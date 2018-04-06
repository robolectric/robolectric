package org.robolectric;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Properties;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.robolectric.annotation.Config;
import org.robolectric.internal.SdkConfig;

@RunWith(JUnit4.class)
public class SdkPickerTest {
  private static final int[] sdkInts = { 16, 17, 18, 19, 21, 22, 23 };
  private UsesSdk usesSdk;
  private SdkPicker sdkPicker;
  private Properties properties;

  @Before
  public void setUp() throws Exception {
    usesSdk = mock(UsesSdk.class);
    properties = new Properties();
    sdkPicker = new SdkPicker(properties, sdkInts);
  }

  @Test
  public void withDefaultSdkConfig_shouldUseTargetSdkFromAndroidManifest() throws Exception {
    when(usesSdk.getTargetSdkVersion()).thenReturn(22);
    assertThat(sdkPicker.selectSdks(new Config.Builder().build(), usesSdk))
        .containsExactly(new SdkConfig(22));
  }

  @Test
  public void withAllSdksConfig_shouldUseFullSdkRangeFromAndroidManifest() throws Exception {
    when(usesSdk.getTargetSdkVersion()).thenReturn(22);
    when(usesSdk.getMinSdkVersion()).thenReturn(19);
    when(usesSdk.getMaxSdkVersion()).thenReturn(23);
    assertThat(sdkPicker.selectSdks(new Config.Builder().setSdk(Config.ALL_SDKS).build(), usesSdk))
        .containsExactly(new SdkConfig(19), new SdkConfig(21), new SdkConfig(22), new SdkConfig(23));
  }

  @Test
  public void withAllSdksConfigAndNoMinSdkVersion_shouldUseFullSdkRangeFromAndroidManifest() throws Exception {
    when(usesSdk.getTargetSdkVersion()).thenReturn(22);
    when(usesSdk.getMinSdkVersion()).thenReturn(1);
    when(usesSdk.getMaxSdkVersion()).thenReturn(22);
    assertThat(sdkPicker.selectSdks(new Config.Builder().setSdk(Config.ALL_SDKS).build(), usesSdk))
        .containsExactly(new SdkConfig(16), new SdkConfig(17), new SdkConfig(18), new SdkConfig(19),
            new SdkConfig(21), new SdkConfig(22));
  }

  @Test
  public void withAllSdksConfigAndNoMaxSdkVersion_shouldUseFullSdkRangeFromAndroidManifest() throws Exception {
    when(usesSdk.getTargetSdkVersion()).thenReturn(22);
    when(usesSdk.getMinSdkVersion()).thenReturn(19);
    when(usesSdk.getMaxSdkVersion()).thenReturn(null);
    assertThat(sdkPicker.selectSdks(new Config.Builder().setSdk(Config.ALL_SDKS).build(), usesSdk))
        .containsExactly(new SdkConfig(19), new SdkConfig(21), new SdkConfig(22), new SdkConfig(23));
  }

  @Test
  public void withMinSdkHigherThanSupportedRange_shouldReturnNone() throws Exception {
    when(usesSdk.getTargetSdkVersion()).thenReturn(23);
    when(usesSdk.getMinSdkVersion()).thenReturn(1);
    when(usesSdk.getMaxSdkVersion()).thenReturn(null);
    assertThat(sdkPicker.selectSdks(new Config.Builder().setMinSdk(24).build(), usesSdk))
        .isEmpty();
  }

  @Test
  public void withMinSdkHigherThanMaxSdk_shouldThrowError() throws Exception {
    when(usesSdk.getTargetSdkVersion()).thenReturn(23);
    when(usesSdk.getMinSdkVersion()).thenReturn(1);
    when(usesSdk.getMaxSdkVersion()).thenReturn(null);

    assertThatThrownBy(
            () ->
                sdkPicker.selectSdks(
                    new Config.Builder().setMinSdk(22).setMaxSdk(21).build(), usesSdk))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("minSdk may not be larger than maxSdk (minSdk=22, maxSdk=21)");
  }

  @Test
  public void withTargetSdkLessThanMinSdk_shouldThrowError() throws Exception {
    when(usesSdk.getMinSdkVersion()).thenReturn(23);
    when(usesSdk.getTargetSdkVersion()).thenReturn(22);
    assertThatThrownBy(() -> sdkPicker.selectSdks(new Config.Builder().build(), usesSdk))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Package targetSdkVersion=22 < minSdkVersion=23");
  }

  @Test
  public void withTargetSdkGreaterThanMaxSdk_shouldThrowError() throws Exception {
    when(usesSdk.getMaxSdkVersion()).thenReturn(21);
    when(usesSdk.getTargetSdkVersion()).thenReturn(22);
    assertThatThrownBy(() -> sdkPicker.selectSdks(new Config.Builder().build(), usesSdk))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Package targetSdkVersion=22 > maxSdkVersion=21");
  }

  @Test
  public void shouldClipSdkRangeFromAndroidManifest() throws Exception {
    when(usesSdk.getTargetSdkVersion()).thenReturn(1);
    when(usesSdk.getMinSdkVersion()).thenReturn(1);
    when(usesSdk.getMaxSdkVersion()).thenReturn(null);
    assertThat(sdkPicker.selectSdks(new Config.Builder().build(), usesSdk))
        .containsExactly(new SdkConfig(16));
  }

  @Test
  public void withMinSdkConfig_shouldClipSdkRangeFromAndroidManifest() throws Exception {
    when(usesSdk.getTargetSdkVersion()).thenReturn(22);
    when(usesSdk.getMinSdkVersion()).thenReturn(19);
    when(usesSdk.getMaxSdkVersion()).thenReturn(23);
    assertThat(sdkPicker.selectSdks(new Config.Builder().setMinSdk(21).build(), usesSdk))
        .containsExactly(new SdkConfig(21), new SdkConfig(22), new SdkConfig(23));
  }

  @Test
  public void withMaxSdkConfig_shouldUseSdkRangeFromAndroidManifest() throws Exception {
    when(usesSdk.getTargetSdkVersion()).thenReturn(22);
    when(usesSdk.getMinSdkVersion()).thenReturn(19);
    when(usesSdk.getMaxSdkVersion()).thenReturn(23);
    assertThat(sdkPicker.selectSdks(new Config.Builder().setMaxSdk(21).build(), usesSdk))
        .containsExactly(new SdkConfig(19), new SdkConfig(21));
  }

  @Test
  public void withExplicitSdkConfig_selectSdks() throws Exception {
    when(usesSdk.getTargetSdkVersion()).thenReturn(21);
    when(usesSdk.getMinSdkVersion()).thenReturn(19);
    when(usesSdk.getMaxSdkVersion()).thenReturn(22);

    assertThat(sdkPicker.selectSdks(new Config.Builder().setSdk(21).build(), usesSdk))
        .containsExactly(new SdkConfig(21));
    assertThat(sdkPicker.selectSdks(new Config.Builder().setSdk(Config.OLDEST_SDK).build(), usesSdk))
        .containsExactly(new SdkConfig(19));
    assertThat(sdkPicker.selectSdks(new Config.Builder().setSdk(Config.TARGET_SDK).build(), usesSdk))
        .containsExactly(new SdkConfig(21));
    assertThat(sdkPicker.selectSdks(new Config.Builder().setSdk(Config.NEWEST_SDK).build(), usesSdk))
        .containsExactly(new SdkConfig(22));

    assertThat(sdkPicker.selectSdks(new Config.Builder().setSdk(16).build(), usesSdk))
        .containsExactly(new SdkConfig(16));
    assertThat(sdkPicker.selectSdks(new Config.Builder().setSdk(23).build(), usesSdk))
        .containsExactly(new SdkConfig(23));
  }

  @Test
  public void withEnabledSdks_shouldRestrictAsSpecified() throws Exception {
    when(usesSdk.getMinSdkVersion()).thenReturn(16);
    when(usesSdk.getMaxSdkVersion()).thenReturn(23);
    properties.setProperty("robolectric.enabledSdks", "17,18");
    assertThat(sdkPicker.selectSdks(new Config.Builder().setSdk(Config.ALL_SDKS).build(), usesSdk))
        .containsExactly(new SdkConfig(17), new SdkConfig(18));
  }

  @Test
  public void withEnabledSdkNames_shouldRestrictAsSpecified() throws Exception {
    when(usesSdk.getMinSdkVersion()).thenReturn(16);
    when(usesSdk.getMaxSdkVersion()).thenReturn(23);
    properties.setProperty("robolectric.enabledSdks", "KITKAT, LOLLIPOP");
    assertThat(sdkPicker.selectSdks(new Config.Builder().setSdk(Config.ALL_SDKS).build(), usesSdk))
        .containsExactly(new SdkConfig(19), new SdkConfig(21));
  }
}