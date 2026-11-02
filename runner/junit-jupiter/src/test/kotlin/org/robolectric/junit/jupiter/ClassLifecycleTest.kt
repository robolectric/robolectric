package org.robolectric.junit.jupiter

import android.app.Application
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

/**
 * Tests that [RobolectricExtension] runs `@BeforeAll` and `@AfterAll` methods in the Android
 * environment that the tests of the class share: static state set in `@BeforeAll` is visible to the
 * tests, and Android can be used in both.
 */
@ExtendWith(RobolectricExtension::class)
@Config(sdk = [34])
class ClassLifecycleTest {

  companion object {
    private var beforeAllRan = false
    private var applicationAtBeforeAll: Application? = null

    @JvmStatic
    @BeforeAll
    fun setUpClass() {
      beforeAllRan = true
      applicationAtBeforeAll = RuntimeEnvironment.getApplication()
    }

    @JvmStatic
    @AfterAll
    fun tearDownClass() {
      // Runs in the same environment, so state set in @BeforeAll is still visible.
      assertThat(beforeAllRan).isTrue()
      assertThat(applicationAtBeforeAll).isNotNull()
    }
  }

  @Test
  fun beforeAllStateIsVisibleToTests() {
    assertThat(beforeAllRan).isTrue()
  }

  @Test
  fun beforeAllSawTheSameApplicationAsTests() {
    assertThat(applicationAtBeforeAll).isSameInstanceAs(RuntimeEnvironment.getApplication())
  }
}
