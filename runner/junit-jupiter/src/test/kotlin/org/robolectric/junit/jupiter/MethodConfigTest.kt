package org.robolectric.junit.jupiter

import android.os.Build
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import org.junit.jupiter.api.extension.ExtendWith
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

/**
 * Tests that each test runs in an Android environment of its own, set up as its `@Config` says. The
 * tests run in the same sandbox, so static state records the application of each.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
@ExtendWith(RobolectricExtension::class)
@Config(sdk = [34])
class MethodConfigTest {

  companion object {
    private val seenApplicationIdentities = mutableSetOf<Int>()
  }

  @Test
  @Order(1)
  fun classDefaultSdkApplies() {
    assertThat(Build.VERSION.SDK_INT).isEqualTo(34)
    seenApplicationIdentities.add(System.identityHashCode(RuntimeEnvironment.getApplication()))
  }

  @Test
  @Order(2)
  @Config(qualifiers = "land")
  fun methodConfigOverridesQualifiers() {
    assertThat(Build.VERSION.SDK_INT).isEqualTo(34)
    assertThat(RuntimeEnvironment.getQualifiers()).contains("land")
  }

  @Test
  @Order(3)
  fun perMethodEnvironmentIsFresh() {
    seenApplicationIdentities.add(System.identityHashCode(RuntimeEnvironment.getApplication()))
    assertThat(seenApplicationIdentities).hasSize(2)
  }
}
