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
 * Tests that each test of a class without `@BeforeAll` or `@AfterAll` methods runs in an Android
 * environment of its own, set up as the test's `@Config` says.
 *
 * The two tests on the same SDK run in the same sandbox, so static state records the application of
 * each: a fresh environment for each test means distinct applications.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
@ExtendWith(RobolectricExtension::class)
@Config(sdk = [33])
class MethodConfigTest {

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
