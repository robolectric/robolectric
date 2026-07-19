package org.robolectric.enginefixtures

import android.os.Build
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.robolectric.annotation.Config
import org.robolectric.junit.jupiter.RobolectricExtension

/**
 * Twin fixtures for `EnginePathParityTest`. The two classes MUST have identical test bodies: one
 * runs on the custom `robolectric-junit-jupiter-engine`, the other (via `@ExtendWith`) on the
 * standard `junit-jupiter` engine, and the parity test asserts identical outcomes.
 *
 * (Duplicated rather than inherited: the custom engine discovers `declaredMethods` only.)
 */
class EngineParityFixture {

  private var ready = false

  @BeforeEach
  fun setUp() {
    ready = true
  }

  @Test
  fun passes() {
    assertThat(ready).isTrue()
  }

  @Test fun fails(): Unit = error("deliberate parity failure")

  @Test
  @Config(sdk = [34])
  fun runsOnSdk34() {
    assertThat(Build.VERSION.SDK_INT).isEqualTo(34)
  }
}

@ExtendWith(RobolectricExtension::class)
class ExtensionParityFixture {

  private var ready = false

  @BeforeEach
  fun setUp() {
    ready = true
  }

  @Test
  fun passes() {
    assertThat(ready).isTrue()
  }

  @Test fun fails(): Unit = error("deliberate parity failure")

  @Test
  @Config(sdk = [34])
  fun runsOnSdk34() {
    assertThat(Build.VERSION.SDK_INT).isEqualTo(34)
  }
}
