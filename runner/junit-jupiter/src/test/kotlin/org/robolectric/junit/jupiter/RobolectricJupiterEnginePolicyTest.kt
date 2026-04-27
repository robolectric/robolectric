package org.robolectric.junit.jupiter

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.robolectric.annotation.Config

class RobolectricJupiterEnginePolicyTest {

  @Test
  fun hasConflictingMethodLevelConfig_detectsOverrides() {
    val method = MethodConfigFixture::class.java.getDeclaredMethod("withSdkOverride")
    val nonConflictMethod = MethodConfigFixture::class.java.getDeclaredMethod("withoutOverrides")

    assertThat(invokeBooleanMethod("hasConflictingMethodLevelConfig", method)).isTrue()
    assertThat(invokeBooleanMethod("hasConflictingMethodLevelConfig", nonConflictMethod)).isFalse()
  }

  @Test
  fun hasClassLifecycleMethods_detectsInheritedBeforeAll() {
    assertThat(invokeBooleanMethod("hasClassLifecycleMethods", InheritedLifecycleChild::class.java))
      .isTrue()
  }

  private fun invokeBooleanMethod(methodName: String, argument: Any): Boolean {
    val engine = RobolectricJupiterEngine()
    val method =
      RobolectricJupiterEngine::class.java.getDeclaredMethod(methodName, argument.javaClass)
    method.isAccessible = true
    @Suppress("UNCHECKED_CAST") return method.invoke(engine, argument) as Boolean
  }

  private class MethodConfigFixture {
    @Config(sdk = [34]) fun withSdkOverride() = Unit

    fun withoutOverrides() = Unit
  }

  @Suppress("UtilityClassWithPublicConstructor")
  private open class InheritedLifecycleBase {
    companion object {
      @JvmStatic @BeforeAll fun setupBase() = Unit
    }
  }

  private class InheritedLifecycleChild : InheritedLifecycleBase()
}
