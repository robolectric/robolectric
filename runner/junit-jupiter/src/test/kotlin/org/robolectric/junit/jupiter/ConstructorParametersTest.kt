package org.robolectric.junit.jupiter

import android.app.Application
import android.content.Context
import android.os.Build
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInfo
import org.junit.jupiter.api.extension.ExtendWith
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

/**
 * Tests that the test instance is constructed in the Android environment only, with the arguments
 * that JUnit resolves and those that the environment provides.
 */
@ExtendWith(RobolectricExtension::class)
@Config(sdk = [34])
class ConstructorParametersTest(private val testInfo: TestInfo, private val context: Context) {
  private val applicationAtConstruction: Application = RuntimeEnvironment.getApplication()
  private val sdkAtConstruction = Build.VERSION.SDK_INT

  @Test
  fun `the constructor gets the arguments that JUnit resolved`() {
    assertThat(testInfo.testClass.get().name).isEqualTo(ConstructorParametersTest::class.java.name)
  }

  @Test
  fun `the constructor gets the arguments that the Android environment provides`() {
    assertThat(context).isSameInstanceAs(RuntimeEnvironment.getApplication())
  }

  @Test
  fun `field initializers can use Android`() {
    assertThat(applicationAtConstruction).isSameInstanceAs(RuntimeEnvironment.getApplication())
    assertThat(sdkAtConstruction).isEqualTo(34)
  }

  @Nested
  inner class Inner(private val innerInfo: TestInfo, private val innerContext: Context) {
    @Test
    fun `the constructor of a nested class gets its arguments and the enclosing instance`() {
      assertThat(innerInfo.testClass.get().name).isEqualTo(Inner::class.java.name)
      assertThat(innerContext).isSameInstanceAs(context)
      assertThat(applicationAtConstruction).isSameInstanceAs(RuntimeEnvironment.getApplication())
    }
  }
}
