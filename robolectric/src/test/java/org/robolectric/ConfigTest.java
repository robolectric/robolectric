package org.robolectric;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Map;
import java.util.Properties;
import javax.annotation.Nonnull;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.Config.Builder;

@RunWith(JUnit4.class)
public class ConfigTest {
  @Test
  public void testDefaults() throws Exception {
    Config defaults = Config.Builder.defaults().build();
    assertThat(defaults.manifest()).isEqualTo("AndroidManifest.xml");
    assertThat(defaults.resourceDir()).isEqualTo("res");
    assertThat(defaults.assetDir()).isEqualTo("assets");
  }

  @Test
  public void withOverlay_withBaseSdk() throws Exception {
    Config.Implementation base = new Config.Builder().setSdk(16, 17, 18).build();

    assertThat(sdksIn(overlay(base, new Config.Builder().build())))
        .isEqualTo("sdk=[16, 17, 18], minSdk=-1, maxSdk=-1");

    assertThat(sdksIn(overlay(base, new Config.Builder().setSdk(16).build())))
        .isEqualTo("sdk=[16], minSdk=-1, maxSdk=-1");

    assertThat(sdksIn(overlay(base, new Config.Builder().setMinSdk(16).build())))
        .isEqualTo("sdk=[], minSdk=16, maxSdk=-1");

    assertThat(sdksIn(overlay(base, new Config.Builder().setMaxSdk(20).build())))
        .isEqualTo("sdk=[], minSdk=-1, maxSdk=20");

    assertThat(sdksIn(overlay(base, new Config.Builder().setMinSdk(16).setMaxSdk(18).build())))
        .isEqualTo("sdk=[], minSdk=16, maxSdk=18");
  }

  @Test
  public void withOverlay_withBaseMinSdk() throws Exception {
    Config.Implementation base = new Config.Builder().setMinSdk(18).build();

    assertThat(sdksIn(overlay(base, new Config.Builder().build())))
        .isEqualTo("sdk=[], minSdk=18, maxSdk=-1");

    assertThat(sdksIn(overlay(base, new Config.Builder().setSdk(16).build())))
        .isEqualTo("sdk=[16], minSdk=-1, maxSdk=-1");

    assertThat(sdksIn(overlay(base, new Config.Builder().setMinSdk(16).build())))
        .isEqualTo("sdk=[], minSdk=16, maxSdk=-1");

    assertThat(sdksIn(overlay(base, new Config.Builder().setMaxSdk(20).build())))
        .isEqualTo("sdk=[], minSdk=18, maxSdk=20");

    assertThat(sdksIn(overlay(base, new Config.Builder().setMinSdk(16).setMaxSdk(18).build())))
        .isEqualTo("sdk=[], minSdk=16, maxSdk=18");
  }

  @Test
  public void withOverlay_withBaseMaxSdk() throws Exception {
    Config.Implementation base = new Config.Builder().setMaxSdk(18).build();

    assertThat(sdksIn(overlay(base, new Config.Builder().build())))
        .isEqualTo("sdk=[], minSdk=-1, maxSdk=18");

    assertThat(sdksIn(overlay(base, new Config.Builder().setSdk(16).build())))
        .isEqualTo("sdk=[16], minSdk=-1, maxSdk=-1");

    assertThat(sdksIn(overlay(base, new Config.Builder().setMinSdk(16).build())))
        .isEqualTo("sdk=[], minSdk=16, maxSdk=18");

    assertThat(sdksIn(overlay(base, new Config.Builder().setMaxSdk(20).build())))
        .isEqualTo("sdk=[], minSdk=-1, maxSdk=20");

    assertThat(sdksIn(overlay(base, new Config.Builder().setMinSdk(16).setMaxSdk(18).build())))
        .isEqualTo("sdk=[], minSdk=16, maxSdk=18");
  }

  @Test
  public void withOverlay_withBaseMinAndMaxSdk() throws Exception {
    Config.Implementation base = new Config.Builder().setMinSdk(17).setMaxSdk(18).build();

    assertThat(sdksIn(overlay(base, new Config.Builder().build())))
        .isEqualTo("sdk=[], minSdk=17, maxSdk=18");

    assertThat(sdksIn(overlay(base, new Config.Builder().setSdk(16).build())))
        .isEqualTo("sdk=[16], minSdk=-1, maxSdk=-1");

    assertThat(sdksIn(overlay(base, new Config.Builder().setMinSdk(16).build())))
        .isEqualTo("sdk=[], minSdk=16, maxSdk=18");

    assertThat(sdksIn(overlay(base, new Config.Builder().setMaxSdk(20).build())))
        .isEqualTo("sdk=[], minSdk=17, maxSdk=20");

    assertThat(sdksIn(overlay(base, new Config.Builder().setMinSdk(16).setMaxSdk(18).build())))
        .isEqualTo("sdk=[], minSdk=16, maxSdk=18");
  }

  @Test
  public void withOverlay_withShadows_maintainsOrder() throws Exception {
    Config.Implementation base = new Config.Builder().build();

    Config withString = overlay(base, new Builder().setShadows(new Class[]{String.class}).build());
    assertThat(withString.shadows()).asList().contains(String.class);

    Config withMore = overlay(withString,
        new Builder().setShadows(new Class[]{Map.class, String.class}).build());
    assertThat(withMore.shadows()).asList().containsAllOf(String.class, Map.class, String.class);
  }

  @Test
  public void shouldAppendQualifiersStartingWithPlus() throws Exception {
    Config config = new Config.Builder().setQualifiers("w100dp").build();
    config = overlay(config, new Config.Builder().setQualifiers("w101dp").build());
    assertThat(config.qualifiers()).isEqualTo("w101dp");

    config = overlay(config, new Config.Builder().setQualifiers("+w102dp").build());
    config = overlay(config, new Config.Builder().setQualifiers("+w103dp").build());
    assertThat(config.qualifiers()).isEqualTo("w101dp +w102dp +w103dp");

    config = overlay(config, new Config.Builder().setQualifiers("+w104dp").build());
    config = overlay(config, new Config.Builder().setQualifiers("w105dp").build());
    assertThat(config.qualifiers()).isEqualTo("w105dp");
  }

  @Test
  public void sdksFromProperties() throws Exception {
    Properties properties = new Properties();
    properties.setProperty("sdk", "1, 2, ALL_SDKS, TARGET_SDK, OLDEST_SDK, NEWEST_SDK, 666");
    Config config = Config.Implementation.fromProperties(properties);
    assertThat(sdksIn(config))
        .isEqualTo("sdk=[1, 2, -2, -3, -4, -5, 666], minSdk=-1, maxSdk=-1");
  }

  @Test
  public void minMaxSdksFromProperties() throws Exception {
    Properties properties = new Properties();
    properties.setProperty("minSdk", "OLDEST_SDK");
    properties.setProperty("maxSdk", "NEWEST_SDK");
    Config config = Config.Implementation.fromProperties(properties);
    assertThat(sdksIn(config))
        .isEqualTo("sdk=[], minSdk=-4, maxSdk=-5");
  }

  @Test
  public void testIllegalArguments_sdkMutualExclusion() throws Exception {
    try {
      new Config.Builder()
          .setSdk(16, 17, 18).setMinSdk(16).setMaxSdk(18)
          .build();
      fail();
    } catch (IllegalArgumentException e) {
      assertThat(e.getMessage())
          .isEqualTo("sdk and minSdk/maxSdk may not be specified together (sdk=[16, 17, 18], minSdk=16, maxSdk=18)");
    }
  }

  @Test
  public void testIllegalArguments_minMaxSdkRange() throws Exception {
    try {
      new Config.Builder()
          .setMinSdk(18).setMaxSdk(16)
          .build();
      fail();
    } catch (IllegalArgumentException e) {
      assertThat(e.getMessage())
          .isEqualTo("minSdk may not be larger than maxSdk (minSdk=18, maxSdk=16)");
    }
  }

  //////////////////////////

  private String sdksIn(Config config) {
    return "sdk=" + Arrays.toString(config.sdk()) + ", minSdk=" + config.minSdk() + ", maxSdk=" + config.maxSdk();
  }

  @Nonnull
  private Config overlay(Config base, Config.Implementation build) {
    return new Config.Builder(base).overlay(build).build();
  }
}
