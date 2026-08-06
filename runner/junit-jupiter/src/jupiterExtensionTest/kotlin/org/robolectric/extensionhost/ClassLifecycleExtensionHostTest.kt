package org.robolectric.extensionhost

import android.app.Application
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.junit.jupiter.RobolectricExtension

/**
 * Verifies that [RobolectricExtension] runs `@BeforeAll`/`@AfterAll` inside the class-level sandbox
 * under the standard JUnit Jupiter engine: static state set in `@BeforeAll` must be visible to test
 * methods, and the Android environment must be usable in both hooks.
 */
@ExtendWith(RobolectricExtension::class)
@Config(sdk = [34])
class ClassLifecycleExtensionHostTest {

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
      // Runs in the same class-level sandbox, so state set in @BeforeAll is still visible.
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
