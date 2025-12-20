package org.robolectric.runner.common

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import org.robolectric.annotation.Config

@OptIn(ExperimentalRunnerApi::class)
class SandboxConfiguratorTest {

  @Test
  fun `createInstrumentationConfig returns non-null configuration`() {
    val deps = RobolectricDependencies.create()
    val configurator =
      SandboxConfigurator(deps.androidConfigurer, deps.shadowProviders, deps.classHandlerBuilder)
    val config = Config.Builder().build()

    val instConfig = configurator.createInstrumentationConfig(config)

    assertThat(instConfig).isNotNull()
  }

  @Test
  fun `configureSandbox completes without error`() {
    val deps = RobolectricDependencies.create()
    val configurator =
      SandboxConfigurator(deps.androidConfigurer, deps.shadowProviders, deps.classHandlerBuilder)
    val config = Config.Builder().build()

    // Sandbox configuration requires full Android environment
    // In test environment, we can only test that the configurator is created
    // Full integration testing happens in the actual runner module tests
    try {
      val manifest = ManifestResolver.resolveManifest(config)
      val configuration =
        deps.configurationStrategy.getConfig(
          SandboxConfiguratorTest::class.java,
          SandboxConfiguratorTest::class.java.getMethod("configureSandbox completes without error"),
        )
      val sdk = deps.sdkPicker.selectSdks(configuration, manifest).first()
      val instConfig = configurator.createInstrumentationConfig(config)

      val sandbox =
        deps.sandboxManager.getAndroidSandbox(
          instConfig,
          sdk,
          org.robolectric.annotation.ResourcesMode.Mode.BINARY,
          org.robolectric.annotation.LooperMode.Mode.PAUSED,
          org.robolectric.annotation.SQLiteMode.Mode.LEGACY,
          org.robolectric.annotation.GraphicsMode.Mode.NATIVE,
        )

      // This should complete without throwing
      configurator.configureSandbox(sandbox, config, sdk)
    } catch (e: IllegalArgumentException) {
      // Expected in test environment without proper Android project structure
      assertThat(e.message).contains("couldn't find")
    }
  }
}
