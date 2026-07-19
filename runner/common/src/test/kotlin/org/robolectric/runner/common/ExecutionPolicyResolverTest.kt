package org.robolectric.runner.common

import com.google.common.truth.Truth.assertThat
import java.nio.file.Path
import java.nio.file.Paths
import org.junit.jupiter.api.Test
import org.robolectric.annotation.Config
import org.robolectric.pluginapi.Sdk
import org.robolectric.runner.common.ExecutionPolicyResolver.ExecutionPolicy

@OptIn(ExperimentalRunnerApi::class)
class ExecutionPolicyResolverTest {

  @Test
  fun noClassContext_yieldsIsolatedMethodEnvironment() {
    val policy =
      ExecutionPolicyResolver.resolve(
        testClass = Fixture::class.java,
        method = method("plain"),
        methodSdk = sdk(34),
        classContextSdk = null,
      )

    assertThat(policy).isEqualTo(ExecutionPolicy.IsolatedMethodEnvironment(sdk(34)))
  }

  @Test
  fun explicitIsolation_yieldsIsolatedEnvironmentEvenWithClassContext() {
    val policy =
      ExecutionPolicyResolver.resolve(
        testClass = Fixture::class.java,
        method = method("withSdkOverride"),
        methodSdk = sdk(34),
        classContextSdk = sdk(33),
        explicitIsolation = true,
      )

    assertThat(policy).isEqualTo(ExecutionPolicy.IsolatedMethodEnvironment(sdk(34)))
  }

  @Test
  fun sdkMismatchWithClassContext_failsFast() {
    val policy =
      ExecutionPolicyResolver.resolve(
        testClass = Fixture::class.java,
        method = method("withSdkOverride"),
        methodSdk = sdk(34),
        classContextSdk = sdk(33),
      )

    val conflict = policy as ExecutionPolicy.FailFastConflict
    assertThat(conflict.message).contains("Configuration conflict")
    assertThat(conflict.message).contains("SDK 33")
    assertThat(conflict.message).contains("SDK 34")
    assertThat(conflict.message).contains("@RobolectricSdkTest")
  }

  @Test
  fun methodConfigOverridesWithClassContext_failFast() {
    val policy =
      ExecutionPolicyResolver.resolve(
        testClass = Fixture::class.java,
        method = method("withQualifiersOverride"),
        methodSdk = sdk(33),
        classContextSdk = sdk(33),
      )

    val conflict = policy as ExecutionPolicy.FailFastConflict
    assertThat(conflict.message).contains("method-level @Config overrides")
    assertThat(conflict.message).contains("qualifiers=land")
  }

  @Test
  fun matchingSdkWithoutOverrides_yieldsSharedClassEnvironment() {
    val policy =
      ExecutionPolicyResolver.resolve(
        testClass = Fixture::class.java,
        method = method("plain"),
        methodSdk = sdk(33),
        classContextSdk = sdk(33),
      )

    assertThat(policy).isEqualTo(ExecutionPolicy.SharedClassEnvironment)
  }

  @Test
  fun hasConflictingMethodLevelConfig_detectsOverrides() {
    assertThat(ExecutionPolicyResolver.hasConflictingMethodLevelConfig(method("withSdkOverride")))
      .isTrue()
    assertThat(
        ExecutionPolicyResolver.hasConflictingMethodLevelConfig(method("withQualifiersOverride"))
      )
      .isTrue()
    assertThat(ExecutionPolicyResolver.hasConflictingMethodLevelConfig(method("plain"))).isFalse()
  }

  private fun method(name: String) = Fixture::class.java.getDeclaredMethod(name)

  private fun sdk(apiLevel: Int): Sdk =
    object : Sdk(apiLevel) {
      override fun getAndroidVersion(): String = apiLevel.toString()

      override fun getAndroidCodeName(): String = "TEST"

      override fun getJarPath(): Path = Paths.get("nonexistent")

      override fun isSupported(): Boolean = true

      override fun getUnsupportedMessage(): String = ""

      override fun verifySupportedSdk(testClassName: String?) = Unit
    }

  @Suppress("unused")
  private class Fixture {
    fun plain() = Unit

    @Config(sdk = [34]) fun withSdkOverride() = Unit

    @Config(qualifiers = "land") fun withQualifiersOverride() = Unit
  }
}
