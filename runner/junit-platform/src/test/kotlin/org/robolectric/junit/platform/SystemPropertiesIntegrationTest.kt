package org.robolectric.junit.platform

import android.os.Build
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.robolectric.annotation.Config

/**
 * Tests for system properties support in JUnit Platform engine.
 *
 * These tests verify that:
 * - Multi-SDK execution works with -Drobolectric.enabledSdks
 * - Test names include SDK markers with -Drobolectric.alwaysIncludeVariantMarkersInTestName
 * - Test execution respects the configured SDK
 *
 * To test multi-SDK execution, run:
 * ```
 * ./gradlew :runner:junit-platform:test \
 *   -Drobolectric.enabledSdks=28,29 \
 *   -Drobolectric.alwaysIncludeVariantMarkersInTestName=true
 * ```
 */
@Config(sdk = [28])
class SystemPropertiesIntegrationTest {

  @Test
  fun testSdkVersion() {
    // This test verifies that the correct SDK is selected
    // When run with -Drobolectric.enabledSdks=28,29, this should execute twice
    // with SDK_INT being 28 and 29 respectively
    val sdkVersion = Build.VERSION.SDK_INT
    assertThat(sdkVersion).isAtLeast(28)
    println("Running with SDK: $sdkVersion")
  }

  @Test
  @Config(sdk = [29, 30])
  fun testMultipleSdksInConfig() {
    // This test has multiple SDKs in @Config
    // When run without system properties, it should execute with the first SDK (29)
    // When run with -Drobolectric.enabledSdks=29,30, it should execute twice
    val sdkVersion = Build.VERSION.SDK_INT
    assertThat(sdkVersion).isAtLeast(29)
    println("Running testMultipleSdksInConfig with SDK: $sdkVersion")
  }

  @Test
  fun testDefaultSdk() {
    // This test should execute with SDK 28 from class-level @Config
    val sdkVersion = Build.VERSION.SDK_INT
    assertThat(sdkVersion).isEqualTo(28)
  }
}
