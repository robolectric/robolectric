package org.robolectric.runner.common

import com.google.common.truth.Truth.assertThat
import java.util.concurrent.atomic.AtomicInteger
import org.junit.jupiter.api.Test
import org.robolectric.annotation.Config

@ExperimentalRunnerApi
class DefaultRobolectricIntegrationPerformanceTest {

  @Test
  fun `per sdk execution reuses sdk lookup from beforeTest`() {
    val resolveCalls = AtomicInteger(0)
    val bridge =
      object : FrameworkClassLoadingBridge {
        override fun resolveSourceClassLoader(testClass: Class<*>): ClassLoader? {
          resolveCalls.incrementAndGet()
          return testClass.classLoader
        }
      }
    val integration =
      RobolectricIntegrationBuilder()
        .sandboxSharing(SandboxSharingStrategy.PER_SDK)
        .classLoadingBridge(bridge)
        .build()
    val testClass = PerfSdkClass::class.java
    val testMethod = testClass.getMethod("testMethod")

    integration.beforeClass(testClass)
    integration.beforeTest(testClass, testMethod)
    try {
      val sdkLevel = integration.executeInSandbox(testClass, testMethod) { it.sdk.apiLevel }
      assertThat(sdkLevel).isAtLeast(1)
    } finally {
      integration.afterTest(testClass, testMethod, success = true)
      integration.afterClass(testClass)
    }

    assertThat(resolveCalls.get()).isEqualTo(1)
  }

  @Config(sdk = [29])
  class PerfSdkClass {
    fun testMethod() = Unit
  }
}
