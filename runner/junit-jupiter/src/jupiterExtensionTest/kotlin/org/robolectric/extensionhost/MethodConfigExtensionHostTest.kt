package org.robolectric.extensionhost

import android.os.Build
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import org.junit.jupiter.api.extension.ExtendWith
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.junit.jupiter.RobolectricExtension

/**
 * Verifies that classes WITHOUT `@BeforeAll`/`@AfterAll` get classic Robolectric semantics under
 * [RobolectricExtension]: each test runs in an isolated per-method environment, and method-level
 * `@Config` overrides (silently ignored before the execution-policy convergence) are honored.
 *
 * The two same-SDK tests run in the same cached sandbox classloader, so companion state can record
 * environment identities across invocations; a fresh environment per method must produce distinct
 * `Application` instances.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
@ExtendWith(RobolectricExtension::class)
@Config(sdk = [33])
class MethodConfigExtensionHostTest {

  companion object {
    private val seenApplicationIdentities = mutableSetOf<Int>()
  }

  @Test
  @Order(1)
  fun classDefaultSdkApplies() {
    assertThat(Build.VERSION.SDK_INT).isEqualTo(33)
    seenApplicationIdentities.add(System.identityHashCode(RuntimeEnvironment.getApplication()))
  }

  @Test
  @Order(2)
  @Config(sdk = [34])
  fun methodConfigOverridesSdk() {
    assertThat(Build.VERSION.SDK_INT).isEqualTo(34)
  }

  @Test
  @Order(3)
  fun perMethodEnvironmentIsFresh() {
    seenApplicationIdentities.add(System.identityHashCode(RuntimeEnvironment.getApplication()))
    assertThat(seenApplicationIdentities).hasSize(2)
  }
}
