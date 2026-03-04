package org.robolectric.runner.common

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.robolectric.annotation.Config

@ExperimentalRunnerApi
class ClassLifecycleManagerIntegrationTest {
  private lateinit var deps: RobolectricDependencies
  private lateinit var sandboxLifecycleManager: SandboxLifecycleManager
  private lateinit var classLifecycleManager: ClassLifecycleManager

  @BeforeEach
  fun setUp() {
    deps = RobolectricDependencies.create()
    sandboxLifecycleManager = SandboxLifecycleManager(deps)
    classLifecycleManager = ClassLifecycleManager(sandboxLifecycleManager)
  }

  @AfterEach
  fun tearDown() {
    classLifecycleManager.tearDownForClass(TestClass::class.java)
  }

  @Test
  fun `executeInClassContext runs in persistent class context when configuration is null`() {
    classLifecycleManager.setupForClass(TestClass::class.java)

    val result =
      classLifecycleManager.executeInClassContext(TestClass::class.java, "testMethod") { "ok" }

    assertThat(result).isEqualTo("ok")
  }

  @Test
  fun `executeInClassContext allows method configuration with isolated environment`() {
    classLifecycleManager.setupForClass(TestClass::class.java)
    val testMethod = TestClass::class.java.getMethod("testMethod")
    val methodConfig = deps.configurationStrategy.getConfig(TestClass::class.java, testMethod)

    val result =
      classLifecycleManager.executeInClassContext(
        TestClass::class.java,
        testMethod.name,
        methodConfig,
      ) {
        "configured"
      }

    assertThat(result).isEqualTo("configured")
  }

  @Config(sdk = [29])
  class TestClass {
    fun testMethod() {
      /* no-op */
    }
  }
}
