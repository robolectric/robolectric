package org.robolectric.runner.common

import java.io.IOException
import java.util.Properties
import org.robolectric.annotation.Config
import org.robolectric.internal.DefaultManifestFactory
import org.robolectric.internal.ManifestIdentifier
import org.robolectric.manifest.AndroidManifest

/**
 * Utility object for resolving Android manifests and build system properties.
 *
 * This object provides methods to resolve Android manifests from Robolectric configuration and load
 * build system API properties. It serves as a central point for manifest-related operations across
 * different test frameworks.
 *
 * ## Usage
 *
 * ```kotlin
 * val config: Config = ...
 * val manifest = ManifestResolver.resolveManifest(config)
 * val buildProps = ManifestResolver.getBuildSystemApiProperties()
 * ```
 */
@ExperimentalRunnerApi
object ManifestResolver {
  /**
   * Resolves an Android manifest from the given Robolectric configuration.
   *
   * This method uses the build system properties and manifest factory to identify and create the
   * appropriate Android manifest for testing.
   *
   * @param config The Robolectric configuration containing manifest settings
   * @return The resolved AndroidManifest instance
   */
  @JvmStatic
  fun resolveManifest(config: Config): AndroidManifest {
    val buildSystemProps = getBuildSystemApiProperties() ?: Properties()
    val manifestFactory = DefaultManifestFactory(buildSystemProps)
    val manifestIdentifier = manifestFactory.identify(config)
    return createManifestFromIdentifier(manifestIdentifier)
  }

  /**
   * Creates an AndroidManifest from a ManifestIdentifier, recursively processing library
   * dependencies.
   *
   * This method replaces the deprecated `RobolectricTestRunner.createAndroidManifest()` with a
   * stable public API approach using AndroidManifest constructors directly.
   *
   * @param identifier The manifest identifier containing paths and metadata
   * @return The created AndroidManifest instance with all library dependencies resolved
   */
  @JvmStatic
  fun createManifestFromIdentifier(identifier: ManifestIdentifier): AndroidManifest {
    val libraryManifests =
      identifier.libraries.map { library -> createManifestFromIdentifier(library) }

    return AndroidManifest(
      identifier.manifestFile,
      identifier.resDir,
      identifier.assetDir,
      libraryManifests,
      identifier.packageName,
      identifier.apkFile,
    )
  }

  /**
   * Loads build system API properties from the test configuration file.
   *
   * This method attempts to load properties from the `/com/android/tools/test_config.properties`
   * resource. These properties typically include information about the Android SDK and build
   * configuration.
   *
   * @return The loaded Properties, or null if the resource is not found
   */
  @Suppress("SwallowedException")
  @JvmStatic
  fun getBuildSystemApiProperties(): Properties? {
    return try {
      ManifestResolver::class
        .java
        .getResourceAsStream("/com/android/tools/test_config.properties")
        ?.use { inputStream ->
          val properties = Properties()
          properties.load(inputStream)
          properties
        }
    } catch (e: IOException) {
      // Build system properties are optional and may not be present
      null
    }
  }
}
