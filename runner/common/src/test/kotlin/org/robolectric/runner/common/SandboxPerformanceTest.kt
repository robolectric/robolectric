package org.robolectric.runner.common

import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.robolectric.annotation.Config

/** Performance tests for runner:common components. */
@ExperimentalRunnerApi
class SandboxPerformanceTest {
  private lateinit var deps: RobolectricDependencies
  private lateinit var lifecycleManager: SandboxLifecycleManager

  @Before
  fun setUp() {
    deps = RobolectricDependencies.create()
    lifecycleManager = SandboxLifecycleManager(deps)
    RunnerMetrics.enable()
    RunnerMetrics.enableTiming()
  }

  @After
  fun tearDown() {
    RunnerMetrics.reset()
  }

  @Test
  fun `sandbox creation completes in reasonable time`() {
    val startTime = System.currentTimeMillis()
    val context = lifecycleManager.createSandbox(PerformanceTestClass::class.java)
    val duration = System.currentTimeMillis() - startTime
    assertThat(context).isNotNull()
    assertThat(duration).isLessThan(10_000)
  }

  @Test
  fun `sandbox reuse is faster than creation`() {
    val classLifecycleManager = ClassLifecycleManager(lifecycleManager)
    val startCreate = System.currentTimeMillis()
    classLifecycleManager.setupForClass(PerformanceTestClass::class.java)
    val createDuration = System.currentTimeMillis() - startCreate
    val startReuse = System.currentTimeMillis()
    val context = classLifecycleManager.getClassContext(PerformanceTestClass::class.java)
    val reuseDuration = System.currentTimeMillis() - startReuse
    assertThat(context).isNotNull()
    assertThat(reuseDuration).isLessThan(createDuration)
    classLifecycleManager.tearDownForClass(PerformanceTestClass::class.java)
  }

  @Test
  fun `test method discovery is fast`() {
    val startTime = System.currentTimeMillis()
    val methods =
      DiscoveryHelpers.discoverTestMethods(
        PerformanceTestClass::class.java,
        listOf(org.junit.Test::class.java),
      )
    val duration = System.currentTimeMillis() - startTime
    assertThat(methods).isNotEmpty()
    assertThat(duration).isLessThan(100)
  }

  @Test
  fun `filter evaluation is fast`() {
    val filter =
      TestFilter.allOf(
        TestFilter.byClassName(Regex(".*PerformanceTestClass.*")),
        TestFilter.byMethodName(Regex(".*test.*")),
      )
    val method = PerformanceTestClass::class.java.getMethod("testMethod1")
    val iterations = 10_000
    val startTime = System.currentTimeMillis()
    repeat(iterations) { filter.shouldRun(PerformanceTestClass::class.java, method) }
    val duration = System.currentTimeMillis() - startTime
    assertThat(duration).isLessThan(1000)
  }

  @Config(sdk = [29])
  @Suppress("EmptyFunctionBlock")
  class PerformanceTestClass {
    @Test
    fun testMethod1() {
      /* Empty */
    }

    @Test
    fun testMethod2() {
      /* Empty */
    }

    @Test
    fun testMethod3() {
      /* Empty */
    }
  }
}
