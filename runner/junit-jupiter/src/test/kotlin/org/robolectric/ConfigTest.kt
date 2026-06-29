package org.robolectric

import com.google.common.truth.Truth.assertThat
import java.util.Arrays
import java.util.Map
import org.junit.jupiter.api.Test
import org.robolectric.annotation.Config

class ConfigTest {
  @Test
  fun testDefaults() {
    val defaults = Config.Builder.defaults().build()
    assertThat(defaults.manifest).isEqualTo("AndroidManifest.xml")
  }

  @Test
  fun withOverlay_withBaseSdk() {
    val base = Config.Builder().setSdk(16, 17, 18).build()

    assertThat(sdksIn(overlay(base, Config.Builder().build())))
      .isEqualTo("sdk=[16, 17, 18], minSdk=-1, maxSdk=-1")

    assertThat(sdksIn(overlay(base, Config.Builder().setSdk(16).build())))
      .isEqualTo("sdk=[16], minSdk=-1, maxSdk=-1")

    assertThat(sdksIn(overlay(base, Config.Builder().setMinSdk(16).build())))
      .isEqualTo("sdk=[], minSdk=16, maxSdk=-1")

    assertThat(sdksIn(overlay(base, Config.Builder().setMaxSdk(20).build())))
      .isEqualTo("sdk=[], minSdk=-1, maxSdk=20")

    assertThat(sdksIn(overlay(base, Config.Builder().setMinSdk(16).setMaxSdk(18).build())))
      .isEqualTo("sdk=[], minSdk=16, maxSdk=18")
  }

  @Test
  fun withOverlay_withShadows_maintainsOrder() {
    val base = Config.Builder().build()

    val withString = overlay(base, Config.Builder().setShadows(String::class.java).build())
    assertThat(withString.shadows.map { it.java }).contains(String::class.java)

    val withMore =
      overlay(withString, Config.Builder().setShadows(Map::class.java, String::class.java).build())
    assertThat(withMore.shadows.map { it.java })
      .containsAtLeast(String::class.java, Map::class.java, String::class.java)
  }

  @Test
  fun withOverlay_withBaseMinSdk() {
    val base = Config.Builder().setMinSdk(18).build()

    assertThat(sdksIn(overlay(base, Config.Builder().build())))
      .isEqualTo("sdk=[], minSdk=18, maxSdk=-1")

    assertThat(sdksIn(overlay(base, Config.Builder().setSdk(16).build())))
      .isEqualTo("sdk=[16], minSdk=-1, maxSdk=-1")

    assertThat(sdksIn(overlay(base, Config.Builder().setMinSdk(16).build())))
      .isEqualTo("sdk=[], minSdk=16, maxSdk=-1")

    assertThat(sdksIn(overlay(base, Config.Builder().setMaxSdk(20).build())))
      .isEqualTo("sdk=[], minSdk=18, maxSdk=20")
  }

  @Test
  fun withOverlay_withBaseMaxSdk() {
    val base = Config.Builder().setMaxSdk(18).build()

    assertThat(sdksIn(overlay(base, Config.Builder().build())))
      .isEqualTo("sdk=[], minSdk=-1, maxSdk=18")

    assertThat(sdksIn(overlay(base, Config.Builder().setSdk(16).build())))
      .isEqualTo("sdk=[16], minSdk=-1, maxSdk=-1")

    assertThat(sdksIn(overlay(base, Config.Builder().setMinSdk(16).build())))
      .isEqualTo("sdk=[], minSdk=16, maxSdk=18")

    assertThat(sdksIn(overlay(base, Config.Builder().setMaxSdk(20).build())))
      .isEqualTo("sdk=[], minSdk=-1, maxSdk=20")
  }

  @Test
  fun withOverlay_withBaseMinAndMaxSdk() {
    val base = Config.Builder().setMinSdk(17).setMaxSdk(18).build()

    assertThat(sdksIn(overlay(base, Config.Builder().build())))
      .isEqualTo("sdk=[], minSdk=17, maxSdk=18")

    assertThat(sdksIn(overlay(base, Config.Builder().setSdk(16).build())))
      .isEqualTo("sdk=[16], minSdk=-1, maxSdk=-1")

    assertThat(sdksIn(overlay(base, Config.Builder().setMinSdk(16).build())))
      .isEqualTo("sdk=[], minSdk=16, maxSdk=18")

    assertThat(sdksIn(overlay(base, Config.Builder().setMaxSdk(20).build())))
      .isEqualTo("sdk=[], minSdk=17, maxSdk=20")
  }

  @Test
  fun shouldAppendQualifiersStartingWithPlus() {
    var config = Config.Builder().setQualifiers("w100dp").build()
    config =
      overlay(config, Config.Builder().setQualifiers("w101dp").build()) as Config.Implementation
    assertThat(config.qualifiers).isEqualTo("w101dp")

    config =
      overlay(config, Config.Builder().setQualifiers("+w102dp").build()) as Config.Implementation
    config =
      overlay(config, Config.Builder().setQualifiers("+w103dp").build()) as Config.Implementation
    assertThat(config.qualifiers).isEqualTo("w101dp +w102dp +w103dp")

    config =
      overlay(config, Config.Builder().setQualifiers("+w104dp").build()) as Config.Implementation
    config =
      overlay(config, Config.Builder().setQualifiers("w105dp").build()) as Config.Implementation
    assertThat(config.qualifiers).isEqualTo("w105dp")
  }

  private fun sdksIn(config: Config): String {
    return "sdk=" +
      Arrays.toString(config.sdk) +
      ", minSdk=" +
      config.minSdk +
      ", maxSdk=" +
      config.maxSdk
  }

  private fun overlay(base: Config, build: Config.Implementation): Config {
    return Config.Builder(base).overlay(build).build()
  }
}
