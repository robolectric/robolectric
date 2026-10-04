@file:OptIn(org.robolectric.runner.common.ExperimentalRunnerApi::class)

package org.robolectric.junit.platform

import java.util.Properties
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.robolectric.annotation.LooperMode
import org.robolectric.pluginapi.config.ConfigurationStrategy.Configuration
import org.robolectric.runner.common.RobolectricSandbox
import org.robolectric.runner.common.RobolectricSession

/** Keeps the Jupiter sandbox alive across launcher executions in this JVM. */
internal object ProcessSandbox {
  /** The first SDK on which native graphics doesn't fall back to legacy graphics. */
  private const val MIN_SDK = 26

  private val session by lazy { createSession() }
  private var sandbox: RobolectricSandbox? = null

  fun createSession(): RobolectricSession {
    val properties =
      Properties().apply {
        putAll(System.getProperties())
        putIfAbsent("robolectric.graphicsMode", GraphicsMode.Mode.NATIVE.name)
      }
    return RobolectricSession.builder().properties(properties).shareSandboxes().build()
  }

  fun validate(configuration: Configuration) {
    require(configuration.get(LooperMode.Mode::class.java) != LooperMode.Mode.LEGACY) {
      "The robolectric JUnit Platform engine requires LooperMode.PAUSED or INSTRUMENTATION_TEST"
    }
    require(configuration.get(Config::class.java).sdk.single() >= MIN_SDK) {
      "The robolectric JUnit Platform engine requires SDK $MIN_SDK or newer"
    }
  }

  @Synchronized
  fun acquire(configuration: Configuration): RobolectricSandbox {
    val key = session.sandboxKey(configuration)
    val existing = sandbox ?: return session.lease(configuration).also { sandbox = it }
    check(existing.key == key) {
      "The robolectric JUnit Platform engine uses one sandbox per JVM. " +
        "The existing sandbox is ${existing.key.id}, but the test requires ${key.id}. " +
        "Use separate test tasks or JVM forks for different SDKs, looper modes, or instrumentation."
    }
    return existing
  }
}
