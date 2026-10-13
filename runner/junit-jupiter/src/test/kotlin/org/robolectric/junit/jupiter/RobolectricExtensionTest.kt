package org.robolectric.junit.jupiter

import android.content.Context
import android.os.Build
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.robolectric.annotation.Config

/** Tests that [RobolectricExtension] runs tests and their lifecycle methods on Android. */
@ExtendWith(RobolectricExtension::class)
@Config(sdk = [34])
class RobolectricExtensionTest {

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
