package org.robolectric.runner.common.internal

import java.lang.reflect.Method
import java.nio.file.Path
import java.util.Properties
import org.robolectric.annotation.Config
import org.robolectric.pluginapi.SdkPicker
import org.robolectric.pluginapi.config.ConfigurationStrategy.Configuration
import org.robolectric.runner.common.ExperimentalRunnerApi
import org.robolectric.util.inject.Injector

/**
 * Plans environments the way `RobolectricTestRunner` does before it runs a test: it reads the
 * configuration and selects SDKs with the [SdkPicker]. Nothing is set up for that.
 */
@OptIn(ExperimentalRunnerApi::class)
internal class Planner(
  injector: Injector,
  private val properties: Properties,
  private val plugins: ClassLoader?,
  apk: Path?,
) {
  private val sdkPicker = injector.getInstance(SdkPicker::class.java)
  private val configurations = Configurations(injector)
  private val manifests = Manifests()

  // The app of the session if it is given as an APK, in place of the one that Robolectric finds
  // for a test.
  private val app = apk?.let { manifests.of(it) }

  // Which SDKs tests run on doesn't limit what a tool plans, so the picker for that doesn't know
  // of the property.
  private val unrestrictedSdkPicker by lazy {
    val unrestricted = Properties()
    properties.stringPropertyNames().forEach {
      unrestricted.setProperty(it, properties.getProperty(it))
    }
    unrestricted.remove(ENABLED_SDKS)
    Injectors.create(unrestricted, plugins).getInstance(SdkPicker::class.java)
  }

  /** Returns the environments of a test: one for each SDK that is selected for it. */
  fun plan(testClass: Class<*>, testMethod: Method?): List<Configuration> {
    val name = testClass.simpleName + if (testMethod != null) "_${testMethod.name}" else ""
    val configuration =
      try {
        configurations.get(testClass, testMethod)
      } catch (e: IllegalArgumentException) {
        throw IllegalArgumentException("Failed to configure $name: ${e.message}", e)
      }
    return plan(configuration, name, sdkPicker)
  }

  /**
   * Returns the environments for the global configuration with the given one and the given modes
   * applied on top of it, for use without a test.
   */
  fun plan(config: Config, modes: List<Enum<*>>): List<Configuration> =
    plan(configurations.with(config, modes), TOOL_NAME, unrestrictedSdkPicker).ifEmpty {
      error(
        "No Android SDK is selected (sdk=${config.sdk.joinToString(",").ifEmpty { "default" }}). " +
          "It has to be known to Robolectric and supported by this JVM."
      )
    }

  private fun plan(
    configuration: Configuration,
    name: String,
    picker: SdkPicker,
  ): List<Configuration> {
    val manifest = app ?: manifests.get(configuration.get(Config::class.java))
    // An SDK that can't run on this JVM is not selected, as the test runner skips tests on it.
    return picker
      .selectSdks(configuration, manifest)
      .filter { it.isSupported }
      .map { sdk ->
        val values = configurations.forSdk(configuration, sdk.apiLevel)
        val identity = configurations.identity(values)
        EnvironmentConfiguration(sdk, values, manifest, sanitize(name), identity)
      }
  }

  private companion object {
    private const val MAX_NAME_LENGTH = 120
    private const val ENABLED_SDKS = "robolectric.enabledSdks"

    /** Names the environment of a configuration that is not that of a test. */
    private const val TOOL_NAME = "RobolectricSession"
    private val UNSAFE_NAME_CHARACTERS = Regex("[^a-zA-Z0-9.-]")

    /** Returns a name that is safe to use as a directory name. */
    private fun sanitize(name: String): String =
      UNSAFE_NAME_CHARACTERS.replace(name, "_").take(MAX_NAME_LENGTH)
  }
}
