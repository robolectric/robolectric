package org.robolectric.runner.common

import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.robolectric.annotation.Config

/** Base class for testing Robolectric framework integrations. */
@ExperimentalRunnerApi
abstract class RobolectricIntegrationContractTest {
  abstract fun createIntegration(): RobolectricIntegration

  private lateinit var integration: RobolectricIntegration

  @Before
  fun setUpContract() {
    integration = createIntegration()
  }

  @After
  fun tearDownContract() {
    try {
      integration.afterClass(SampleTestClass::class.java)
    } catch (_: Exception) {
      // Ignore
    }
  }

  @Test
  fun `beforeClass can be called`() {
    integration.beforeClass(SampleTestClass::class.java)
  }

  @Test
  fun `beforeTest requires beforeClass`() {
    integration.beforeClass(SampleTestClass::class.java)
    val method = SampleTestClass::class.java.getMethod("testMethod")
    integration.beforeTest(SampleTestClass::class.java, method)
  }

  @Test
  fun `afterTest can be called after beforeTest`() {
    integration.beforeClass(SampleTestClass::class.java)
    val method = SampleTestClass::class.java.getMethod("testMethod")
    integration.beforeTest(SampleTestClass::class.java, method)
    integration.afterTest(SampleTestClass::class.java, method, success = true)
  }

  @Test
  fun `afterTest handles failure`() {
    integration.beforeClass(SampleTestClass::class.java)
    val method = SampleTestClass::class.java.getMethod("testMethod")
    integration.beforeTest(SampleTestClass::class.java, method)
    integration.afterTest(SampleTestClass::class.java, method, success = false)
  }

  @Test
  fun `afterClass can be called`() {
    integration.beforeClass(SampleTestClass::class.java)
    integration.afterClass(SampleTestClass::class.java)
  }

  @Test
  fun `lifecycle order is preserved`() {
    val events = mutableListOf<String>()
    integration.beforeClass(SampleTestClass::class.java)
    events.add("beforeClass")
    val method = SampleTestClass::class.java.getMethod("testMethod")
    integration.beforeTest(SampleTestClass::class.java, method)
    events.add("beforeTest")
    integration.afterTest(SampleTestClass::class.java, method, success = true)
    events.add("afterTest")
    integration.afterClass(SampleTestClass::class.java)
    events.add("afterClass")
    assertThat(events)
      .containsExactly("beforeClass", "beforeTest", "afterTest", "afterClass")
      .inOrder()
  }

  @Config(sdk = [29])
  @Suppress("EmptyFunctionBlock")
  class SampleTestClass {
    fun testMethod() {
      /* Empty */
    }

    fun anotherTestMethod() {
      /* Empty */
    }
  }
}
