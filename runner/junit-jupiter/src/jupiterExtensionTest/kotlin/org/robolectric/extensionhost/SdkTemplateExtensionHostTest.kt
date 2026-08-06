package org.robolectric.extensionhost

import android.os.Build
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.extension.ExtendWith
import org.robolectric.annotation.Config
import org.robolectric.junit.jupiter.RobolectricExtension
import org.robolectric.junit.jupiter.RobolectricSdkTest

/**
 * Verifies that `@RobolectricSdkTest` really executes once per configured SDK, each invocation in a
 * sandbox of that SDK, when running under the standard JUnit Jupiter engine.
 *
 * Each per-SDK invocation runs in its own sandbox classloader, so static test-class state cannot be
 * used to aggregate across invocations; JVM-global system properties are used instead, and the
 * `@AfterAll` hook (which runs once, in the class-level sandbox) asserts both SDKs were seen.
 */
@ExtendWith(RobolectricExtension::class)
class SdkTemplateExtensionHostTest {

  companion object {
    private const val SEEN_SDKS_PROPERTY = "org.robolectric.extensionhost.seenSdks"

    @JvmStatic
    @AfterAll
    fun assertEachConfiguredSdkExecuted() {
      val seen =
        System.getProperty(SEEN_SDKS_PROPERTY, "")
          .split(",")
          .filter { it.isNotEmpty() }
          .map { it.toInt() }
      System.clearProperty(SEEN_SDKS_PROPERTY)
      assertThat(seen).containsExactly(33, 34)
    }
  }

  @RobolectricSdkTest
  @Config(sdk = [33, 34])
  fun runsOncePerConfiguredSdk() {
    val sdk = Build.VERSION.SDK_INT
    assertThat(sdk).isAnyOf(33, 34)
    val seen = System.getProperty(SEEN_SDKS_PROPERTY, "")
    System.setProperty(SEEN_SDKS_PROPERTY, "$seen,$sdk")
  }
}
