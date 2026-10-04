package org.robolectric.runner.common.internal

import java.util.Properties
import org.robolectric.annotation.Config
import org.robolectric.internal.DefaultManifestFactory
import org.robolectric.internal.ManifestIdentifier
import org.robolectric.manifest.AndroidManifest

/** Finds the app manifest a configuration refers to, reading each manifest once. */
internal class Manifests {
  private val factory by lazy {
    DefaultManifestFactory(loadBuildSystemProperties() ?: Properties())
  }
  private val manifests = HashMap<ManifestIdentifier, AndroidManifest>()

  fun get(config: Config): AndroidManifest {
    val identifier = factory.identify(config)
    return synchronized(manifests) { manifests.getOrPut(identifier) { create(identifier) } }
  }

  private fun create(identifier: ManifestIdentifier): AndroidManifest =
    AndroidManifest(
      identifier.manifestFile,
      identifier.resDir,
      identifier.assetDir,
      identifier.libraries.map { create(it) },
      identifier.packageName,
      identifier.apkFile,
    )

  /** Returns the properties the Android Gradle plugin writes for unit tests, if there are any. */
  private fun loadBuildSystemProperties(): Properties? =
    Manifests::class.java.getResourceAsStream("/com/android/tools/test_config.properties")?.use {
      stream ->
      Properties().apply { load(stream) }
    }
}
