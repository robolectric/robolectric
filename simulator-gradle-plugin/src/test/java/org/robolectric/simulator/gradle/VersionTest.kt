package org.robolectric.simulator.gradle

import com.google.common.truth.Truth.assertThat
import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import com.google.testing.junit.testparameterinjector.TestParameterValuesProvider
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(TestParameterInjector::class)
class VersionTest {
  @Test
  fun testCompareTo(
    @TestParameter(valuesProvider = CompareToValuesProvider::class) testCase: CompareToTestCase
  ) {
    val version1 = Version(testCase.version1)
    val version2 = Version(testCase.version2)

    assertThat(version1.compareTo(version2)).isEqualTo(testCase.expected)
  }

  @Test
  fun testToString(
    @TestParameter("", "foo", "1", "1.2", "1.2.3-rc1", "1.2.3", "1.2.3.4") version: String
  ) {
    assertThat(Version(version).toString()).isEqualTo(version)
  }

  data class CompareToTestCase(val version1: String, val version2: String, val expected: Int) {
    override fun toString(): String {
      val operator =
        when {
          expected < 0 -> "<"
          expected > 0 -> ">"
          else -> "=="
        }

      return "'$version1' $operator '$version2'"
    }
  }

  class CompareToValuesProvider : TestParameterValuesProvider() {
    override fun provideValues(context: Context?): List<*>? {
      return listOf(
        CompareToTestCase("1.2.3", "1.2.4", -1),
        CompareToTestCase("1.2.3", "1.3.0", -1),
        CompareToTestCase("1.2.3", "2.0.0", -1),
        CompareToTestCase("1.2.4", "1.2.3", 1),
        CompareToTestCase("1.3.0", "1.2.3", 1),
        CompareToTestCase("2.0.0", "1.2.3", 1),
        CompareToTestCase("1.2.3", "1.2.3", 0),
        CompareToTestCase("1.2.3.4", "1.2.3", 0),
        CompareToTestCase("1.2.10", "1.2.3", 1),
        CompareToTestCase("1.2.3-rc1", "1.2.3", -1),
        CompareToTestCase("1.2.3", "1.2.3-rc1", 1),
        CompareToTestCase("1.2.3-rc1", "1.2.3-rc2", -1),
        CompareToTestCase("foo", "1.0.0", -1),
        CompareToTestCase("", "1.0.0", -1),
        CompareToTestCase("foo", "", 0),
      )
    }
  }
}
