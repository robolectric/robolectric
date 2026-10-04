package org.robolectric.junit.jupiter

import android.app.Application
import android.os.Build
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

/** Tests that a `@Nested` class runs in the Android environment its enclosing class shares. */
@ExtendWith(RobolectricExtension::class)
@Config(sdk = [33])
class NestedInSharedEnvironmentTest {
  @Test
  fun `a test of the class runs in the shared environment`() {
    assertThat(applicationAtSetUp).isSameInstanceAs(RuntimeEnvironment.getApplication())
  }

  @Nested
  inner class ConfiguredByTheEnclosingClass {
    @Test
    fun `a test of the nested class runs in the shared environment too`() {
      assertThat(Build.VERSION.SDK_INT).isEqualTo(33)
      assertThat(applicationAtSetUp).isSameInstanceAs(RuntimeEnvironment.getApplication())
    }
  }

  companion object {
    private var applicationAtSetUp: Application? = null

    @JvmStatic
    @BeforeAll
    fun setUpClass() {
      applicationAtSetUp = RuntimeEnvironment.getApplication()
    }
  }
}
