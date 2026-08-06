package org.robolectric.extensionhost

import android.content.Context
import android.os.Build
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.robolectric.annotation.Config
import org.robolectric.junit.jupiter.RobolectricExtension

/**
 * Exercises [RobolectricExtension] under the standard JUnit Jupiter engine.
 *
 * Everything else in this module runs on the custom `robolectric-junit-jupiter-engine`, which
 * bootstraps test classes itself and never invokes `@ExtendWith` extensions. This package is
 * executed by the `jupiterExtensionTest` Gradle task on the vanilla `junit-jupiter` engine instead,
 * so the extension's TestInstanceFactory/InvocationInterceptor path is what runs the tests.
 */
@ExtendWith(RobolectricExtension::class)
@Config(sdk = [34])
class RobolectricExtensionHostTest {

  private var setUpRan = false

  @BeforeEach
  fun setUp() {
    setUpRan = true
  }

  @AfterEach
  fun tearDown() {
    setUpRan = false
  }

  @Test
  fun sdkFromClassConfigIsApplied() {
    assertThat(Build.VERSION.SDK_INT).isEqualTo(34)
  }

  @Test
  fun beforeEachRunsBeforeTestMethod() {
    assertThat(setUpRan).isTrue()
  }

  @Test
  fun contextParameterIsInjected(context: Context) {
    assertThat(context.packageName).isNotEmpty()
  }
}
