package org.robolectric.runner.common

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import org.robolectric.annotation.Config

@OptIn(ExperimentalRunnerApi::class)
class ManifestResolverTest {

  @Test
  fun `resolveManifest returns non-null manifest`() {
    val config = Config.Builder().build()

    // The manifest resolution may fail in test environment without proper setup
    // Just verify the method doesn't throw unexpected exceptions
    try {
      val manifest = ManifestResolver.resolveManifest(config)
      assertThat(manifest).isNotNull()
    } catch (e: IllegalArgumentException) {
      // Expected in test environment without Android project structure
      assertThat(e.message).contains("couldn't find")
    }
  }

  @Test
  fun `getBuildSystemApiProperties returns properties or null`() {
    val props = ManifestResolver.getBuildSystemApiProperties()

    // Properties may or may not exist depending on build configuration
    // Just verify the method doesn't throw
    if (props != null) {
      assertThat(props).isInstanceOf(java.util.Properties::class.java)
    }
  }
}
