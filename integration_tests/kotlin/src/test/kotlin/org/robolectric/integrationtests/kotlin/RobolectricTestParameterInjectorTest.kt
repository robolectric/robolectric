package org.robolectric.integrationtests.kotlin

import com.google.common.truth.Truth.assertThat
import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterValuesProvider
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.JUnitCore
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.robolectric.RobolectricTestParameterInjector
import org.robolectric.annotation.Config
import org.robolectric.junit.rules.SetSystemPropertyRule

@RunWith(JUnit4::class)
class RobolectricTestParameterInjectorTest {
  @get:Rule val properties = SetSystemPropertyRule()

  @Test
  fun providerIsComputedOncePerMethodPerSandbox() {
    properties.set(PROVIDER_CALLS, "0")
    val result = JUnitCore().run(RobolectricTestParameterInjector(Fixture::class.java))
    assertThat(result.failures).isEmpty()
    assertThat(result.runCount).isAtLeast(4)
    assertThat(System.getProperty(PROVIDER_CALLS).toInt()).isEqualTo(result.runCount / 2)
  }

  @Ignore
  @Config(sdk = [Config.ALL_SDKS])
  class Fixture {
    @field:TestParameter(valuesProvider = ValuesProvider::class) lateinit var value: Value

    @Test
    fun first() {
      assertThat(value.number).isAnyOf(1, 2)
    }

    @Test
    fun second() {
      assertThat(value).isInstanceOf(Value::class.java)
    }
  }

  data class Value(val number: Int)

  class ValuesProvider : TestParameterValuesProvider() {
    override fun provideValues(context: Context): List<Value> {
      System.setProperty(
        PROVIDER_CALLS,
        (System.getProperty(PROVIDER_CALLS).toInt() + 1).toString(),
      )
      return listOf(Value(1), Value(2))
    }
  }

  companion object {
    private const val PROVIDER_CALLS = "KotlinRobolectricTestParameterInjectorTest.providerCalls"
  }
}
