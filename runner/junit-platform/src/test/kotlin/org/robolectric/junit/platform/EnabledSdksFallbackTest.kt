package org.robolectric.junit.platform

import android.os.Build
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.robolectric.annotation.Config

/**
 * Tests for enabledSdks fallback behavior.
 *
 * Run with: ./gradlew :runner:junit-platform:test --tests EnabledSdksFallbackTest \
 * -Drobolectric.enabledSdks=23,24,25
 *
 * Expected: Tests run with their configured SDKs (fallback), warnings printed
 */
@Config(sdk = [28])
class EnabledSdksFallbackTest {

  @Test
  fun testFallsBackToConfiguredSdk() {
    // This test has @Config(sdk=[28]) but enabledSdks=23,24,25 doesn't match
    // Should fallback to SDK 28 with warning
    val sdkVersion = Build.VERSION.SDK_INT
    assertThat(sdkVersion).isEqualTo(28)
  }

  @Test
  @Config(sdk = [34])
  fun testFallsBackToMethodConfiguredSdk() {
    // Method-level config should take precedence
    val sdkVersion = Build.VERSION.SDK_INT
    assertThat(sdkVersion).isEqualTo(34)
  }
}
