package org.robolectric.runner.common

import com.google.common.truth.Truth.assertThat
import java.lang.reflect.Method
import org.junit.Test

class TestFilterTest {
  @Test
  fun `ACCEPT_ALL accepts all tests`() {
    val filter = TestFilter.ACCEPT_ALL
    assertThat(filter.shouldRun(TestClass::class.java, getMethod("testOne"))).isTrue()
    assertThat(filter.shouldRun(TestClass::class.java, getMethod("testTwo"))).isTrue()
  }

  @Test
  fun `byMethodName filters by regex pattern`() {
    val filter = TestFilter.byMethodName(Regex(".*One.*"))
    assertThat(filter.shouldRun(TestClass::class.java, getMethod("testOne"))).isTrue()
    assertThat(filter.shouldRun(TestClass::class.java, getMethod("testTwo"))).isFalse()
  }

  @Test
  fun `byClassName filters by class name pattern`() {
    val filter = TestFilter.byClassName(Regex(".*TestClass.*"))
    assertThat(filter.shouldRun(TestClass::class.java, getMethod("testOne"))).isTrue()
    val otherFilter = TestFilter.byClassName(Regex(".*OtherClass.*"))
    assertThat(otherFilter.shouldRun(TestClass::class.java, getMethod("testOne"))).isFalse()
  }

  @Test
  fun `byAnnotation filters by annotation presence`() {
    val filter = TestFilter.byAnnotation(Deprecated::class.java)
    assertThat(filter.shouldRun(TestClass::class.java, getMethod("deprecatedTest"))).isTrue()
    assertThat(filter.shouldRun(TestClass::class.java, getMethod("testOne"))).isFalse()
  }

  @Test
  fun `allOf combines filters with AND logic`() {
    val filter =
      TestFilter.allOf(
        TestFilter.byMethodName(Regex(".*test.*")),
        TestFilter.byMethodName(Regex(".*One.*")),
      )
    assertThat(filter.shouldRun(TestClass::class.java, getMethod("testOne"))).isTrue()
    assertThat(filter.shouldRun(TestClass::class.java, getMethod("testTwo"))).isFalse()
  }

  @Test
  fun `anyOf combines filters with OR logic`() {
    val filter =
      TestFilter.anyOf(
        TestFilter.byMethodName(Regex(".*One.*")),
        TestFilter.byMethodName(Regex(".*Two.*")),
      )
    assertThat(filter.shouldRun(TestClass::class.java, getMethod("testOne"))).isTrue()
    assertThat(filter.shouldRun(TestClass::class.java, getMethod("testTwo"))).isTrue()
    assertThat(filter.shouldRun(TestClass::class.java, getMethod("otherMethod"))).isFalse()
  }

  @Test
  fun `not negates a filter`() {
    val filter = TestFilter.not(TestFilter.byMethodName(Regex(".*One.*")))
    assertThat(filter.shouldRun(TestClass::class.java, getMethod("testOne"))).isFalse()
    assertThat(filter.shouldRun(TestClass::class.java, getMethod("testTwo"))).isTrue()
  }

  private fun getMethod(name: String): Method = TestClass::class.java.getMethod(name)

  @Suppress("EmptyFunctionBlock")
  class TestClass {
    fun testOne() {
      /* Empty test method for filter testing */
    }

    fun testTwo() {
      /* Empty test method for filter testing */
    }

    fun otherMethod() {
      /* Empty method for filter testing */
    }

    @Deprecated("test")
    fun deprecatedTest() {
      /* Empty deprecated test method */
    }
  }
}
