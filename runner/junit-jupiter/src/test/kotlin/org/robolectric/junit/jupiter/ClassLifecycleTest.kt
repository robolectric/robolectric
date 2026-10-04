package org.robolectric.junit.jupiter

import android.app.Application
import android.os.Build
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

/**
 * Tests that `@BeforeAll`, `@AfterAll` and the tests of a class, including those of its `@Nested`
 * classes, share one Android environment.
 */
@ExtendWith(RobolectricExtension::class)
@Config(sdk = [34])
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ClassLifecycleTest {

  companion object {
    private var applicationAtBeforeAll: Application? = null

    @JvmStatic
    @BeforeAll
    fun setUpClass() {
      applicationAtBeforeAll = RuntimeEnvironment.getApplication()
    }

    @JvmStatic
    @AfterAll
    fun tearDownClass() {
      assertThat(applicationAtBeforeAll).isNotNull()
    }
  }

  @Test
  fun beforeAllSawTheSameApplicationAsTests() {
    assertThat(applicationAtBeforeAll).isSameInstanceAs(RuntimeEnvironment.getApplication())
  }

  @Nested
  inner class NestedTests {
    @Test
    fun nestedTestsShareTheEnvironmentOfTheEnclosingClass() {
      assertThat(Build.VERSION.SDK_INT).isEqualTo(34)
      assertThat(applicationAtBeforeAll).isSameInstanceAs(RuntimeEnvironment.getApplication())
    }
  }
}
