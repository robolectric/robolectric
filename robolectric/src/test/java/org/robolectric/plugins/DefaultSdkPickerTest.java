package org.robolectric.plugins;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import android.os.Build.VERSION_CODES;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.internal.ConfigUtils;
import org.robolectric.pluginapi.Sdk;
import org.robolectric.pluginapi.SdkPicker;
import org.robolectric.pluginapi.UsesSdk;
import org.robolectric.pluginapi.config.ConfigurationStrategy.Configuration;
import org.robolectric.plugins.HierarchicalConfigurationStrategy.ConfigurationImpl;
import org.robolectric.util.TestUtil;

@RunWith(JUnit4.class)
public class DefaultSdkPickerTest {
  private static final int[] sdkInts = { 16, 17, 18, 19, 21, 22, 23 };
  private SdkCollection sdkCollection;
  private UsesSdk usesSdk;
  private SdkPicker sdkPicker;

  @Before
  public void setUp() throws Exception {
    usesSdk = mock(UsesSdk.class);
    sdkCollection = new SdkCollection(() -> map(sdkInts));
    sdkPicker = new DefaultSdkPicker(sdkCollection, "");
  }

  @Test
  public void withDefaultSdk_shouldUseTargetSdkFromAndroidManifest() throws Exception {
    when(usesSdk.getTargetSdkVersion()).thenReturn(22);
    assertThat(sdkPicker.selectSdks(buildConfig(new Config.Builder()), usesSdk))
        .containsExactly(sdkCollection.getSdk(22));
  }

  @Test
  public void withAllSdksConfig_shouldUseFullSdkRangeFromAndroidManifest() throws Exception {
    when(usesSdk.getTargetSdkVersion()).thenReturn(22);
    when(usesSdk.getMinSdkVersion()).thenReturn(19);
    when(usesSdk.getMaxSdkVersion()).thenReturn(23);
    assertThat(sdkPicker.selectSdks(buildConfig(new Config.Builder().setSdk(Config.ALL_SDKS)), usesSdk))
        .containsExactly(sdkCollection.getSdk(19), sdkCollection.getSdk(21),
            sdkCollection.getSdk(22), sdkCollection.getSdk(23));
  }

  @Test
  public void withAllSdksConfigAndNoMinSdkVersion_shouldUseFullSdkRangeFromAndroidManifest() throws Exception {
    when(usesSdk.getTargetSdkVersion()).thenReturn(22);
    when(usesSdk.getMinSdkVersion()).thenReturn(1);
    when(usesSdk.getMaxSdkVersion()).thenReturn(22);
    assertThat(sdkPicker.selectSdks(buildConfig(new Config.Builder().setSdk(Config.ALL_SDKS)), usesSdk))
        .containsExactly(sdkCollection.getSdk(16), sdkCollection.getSdk(17),
            sdkCollection.getSdk(18), sdkCollection.getSdk(19),
            sdkCollection.getSdk(21), sdkCollection.getSdk(22));
  }

  @Test
  public void withAllSdksConfigAndNoMaxSdkVersion_shouldUseFullSdkRangeFromAndroidManifest() throws Exception {
    when(usesSdk.getTargetSdkVersion()).thenReturn(22);
    when(usesSdk.getMinSdkVersion()).thenReturn(19);
    when(usesSdk.getMaxSdkVersion()).thenReturn(null);
    assertThat(sdkPicker.selectSdks(buildConfig(new Config.Builder().setSdk(Config.ALL_SDKS)), usesSdk))
        .containsExactly(sdkCollection.getSdk(19), sdkCollection.getSdk(21),
            sdkCollection.getSdk(22), sdkCollection.getSdk(23));
  }

  @Test
  public void withMinSdkHigherThanSupportedRange_shouldReturnNone() throws Exception {
    when(usesSdk.getTargetSdkVersion()).thenReturn(23);
    when(usesSdk.getMinSdkVersion()).thenReturn(1);
    when(usesSdk.getMaxSdkVersion()).thenReturn(null);
    assertThat(sdkPicker.selectSdks(buildConfig(new Config.Builder().setMinSdk(24)), usesSdk))
        .isEmpty();
  }

  @Test
  public void withMinSdkHigherThanMaxSdk_shouldThrowError() throws Exception {
    when(usesSdk.getTargetSdkVersion()).thenReturn(23);
    when(usesSdk.getMinSdkVersion()).thenReturn(1);
    when(usesSdk.getMaxSdkVersion()).thenReturn(null);

    try {
      sdkPicker.selectSdks(
          buildConfig(new Config.Builder().setMinSdk(22).setMaxSdk(21)), usesSdk);
      fail();
    } catch (IllegalArgumentException e) {
      assertThat(e).hasMessageThat().contains("minSdk may not be larger than maxSdk (minSdk=22, maxSdk=21)");
    }
  }

  @Test
  public void withTargetSdkLessThanMinSdk_shouldThrowError() throws Exception {
    when(usesSdk.getMinSdkVersion()).thenReturn(23);
    when(usesSdk.getTargetSdkVersion()).thenReturn(22);

    try {
      sdkPicker.selectSdks(buildConfig(new Config.Builder()), usesSdk);
      fail();
    } catch (IllegalArgumentException e) {
      assertThat(e).hasMessageThat().contains("Package targetSdkVersion=22 < minSdkVersion=23");
    }
  }

  @Test
  public void withTargetSdkGreaterThanMaxSdk_shouldThrowError() throws Exception {
    when(usesSdk.getMaxSdkVersion()).thenReturn(21);
    when(usesSdk.getTargetSdkVersion()).thenReturn(22);
    try {
      sdkPicker.selectSdks(buildConfig(new Config.Builder()), usesSdk);
      fail();
    } catch (IllegalArgumentException e) {
      assertThat(e).hasMessageThat().contains("Package targetSdkVersion=22 > maxSdkVersion=21");
    }
  }

  @Test
  public void shouldClipSdkRangeFromAndroidManifest() throws Exception {
    when(usesSdk.getTargetSdkVersion()).thenReturn(1);
    when(usesSdk.getMinSdkVersion()).thenReturn(1);
    when(usesSdk.getMaxSdkVersion()).thenReturn(null);
    assertThat(sdkPicker.selectSdks(buildConfig(new Config.Builder()), usesSdk))
        .containsExactly(sdkCollection.getSdk(16));
  }

  @Test
  public void withMinSdk_shouldClipSdkRangeFromAndroidManifest() throws Exception {
    when(usesSdk.getTargetSdkVersion()).thenReturn(22);
    when(usesSdk.getMinSdkVersion()).thenReturn(19);
    when(usesSdk.getMaxSdkVersion()).thenReturn(23);
    assertThat(sdkPicker.selectSdks(buildConfig(new Config.Builder().setMinSdk(21)), usesSdk))
        .containsExactly(sdkCollection.getSdk(21), sdkCollection.getSdk(22),
            sdkCollection.getSdk(23));
  }

  @Test
  public void withMaxSdk_shouldUseSdkRangeFromAndroidManifest() throws Exception {
    when(usesSdk.getTargetSdkVersion()).thenReturn(22);
    when(usesSdk.getMinSdkVersion()).thenReturn(19);
    when(usesSdk.getMaxSdkVersion()).thenReturn(23);
    assertThat(sdkPicker.selectSdks(buildConfig(new Config.Builder().setMaxSdk(21)), usesSdk))
        .containsExactly(sdkCollection.getSdk(19), sdkCollection.getSdk(21));
  }

  @Test
  public void withExplicitSdk_selectSdks() throws Exception {
    when(usesSdk.getTargetSdkVersion()).thenReturn(21);
    when(usesSdk.getMinSdkVersion()).thenReturn(19);
    when(usesSdk.getMaxSdkVersion()).thenReturn(22);

    assertThat(sdkPicker.selectSdks(buildConfig(new Config.Builder().setSdk(21)), usesSdk))
        .containsExactly(sdkCollection.getSdk(21));
    assertThat(sdkPicker.selectSdks(buildConfig(new Config.Builder().setSdk(Config.OLDEST_SDK)), usesSdk))
        .containsExactly(sdkCollection.getSdk(19));
    assertThat(sdkPicker.selectSdks(buildConfig(new Config.Builder().setSdk(Config.TARGET_SDK)), usesSdk))
        .containsExactly(sdkCollection.getSdk(21));
    assertThat(sdkPicker.selectSdks(buildConfig(new Config.Builder().setSdk(Config.NEWEST_SDK)), usesSdk))
        .containsExactly(sdkCollection.getSdk(22));

    assertThat(sdkPicker.selectSdks(buildConfig(new Config.Builder().setSdk(16)), usesSdk))
        .containsExactly(sdkCollection.getSdk(16));
    assertThat(sdkPicker.selectSdks(buildConfig(new Config.Builder().setSdk(23)), usesSdk))
        .containsExactly(sdkCollection.getSdk(23));
  }

  @Test
  public void withEnabledSdks_shouldRestrictAsSpecified() throws Exception {
    when(usesSdk.getMinSdkVersion()).thenReturn(16);
    when(usesSdk.getMaxSdkVersion()).thenReturn(23);
    sdkPicker = new DefaultSdkPicker(sdkCollection, "17,18");
    assertThat(sdkPicker.selectSdks(buildConfig(new Config.Builder().setSdk(Config.ALL_SDKS)), usesSdk))
        .containsExactly(sdkCollection.getSdk(17), sdkCollection.getSdk(18));
  }

  @Test
  public void shouldParseSdkSpecs() throws Exception {
    assertThat(ConfigUtils.parseSdkArrayProperty("17,18"))
        .asList()
        .containsExactly(VERSION_CODES.JELLY_BEAN_MR1, VERSION_CODES.JELLY_BEAN_MR2);
    assertThat(ConfigUtils.parseSdkArrayProperty("KITKAT, LOLLIPOP"))
        .asList()
        .containsExactly(VERSION_CODES.KITKAT, VERSION_CODES.LOLLIPOP);
  }

  private Configuration buildConfig(Config.Builder builder) {
    ConfigurationImpl testConfig = new ConfigurationImpl();
    testConfig.put(Config.class, builder.build());
    return testConfig;
  }

  private List<Sdk> map(int... sdkInts) {
    SdkCollection allSdks = TestUtil.getSdkCollection();
    return Arrays.stream(sdkInts).mapToObj(allSdks::getSdk).collect(Collectors.toList());
  }
}
