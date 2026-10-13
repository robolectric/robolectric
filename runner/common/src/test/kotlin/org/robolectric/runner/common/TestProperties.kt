package org.robolectric.runner.common

import java.util.Properties
import org.robolectric.annotation.Config
import org.robolectric.pluginapi.config.ConfigurationStrategy.Configuration

/**
 * Returns the system properties with `robolectric.enabledSdks` set as given, or unset, so that a
 * test doesn't depend on the SDKs that the build runs tests on.
 */
fun testProperties(enabledSdks: String? = null): Properties =
  Properties().apply {
    putAll(System.getProperties())
    remove("robolectric.enabledSdks")
    enabledSdks?.let { setProperty("robolectric.enabledSdks", it) }
  }

/** The SDK of the environment that a planned configuration is for. */
val Configuration.apiLevel: Int
  get() = get(Config::class.java).sdk.single()
