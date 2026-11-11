package org.robolectric.runner.common.internal

import javax.inject.Inject
import org.robolectric.annotation.GraphicsMode
import org.robolectric.annotation.LooperMode
import org.robolectric.annotation.ResourcesMode
import org.robolectric.annotation.SQLiteMode
import org.robolectric.internal.AndroidSandbox
import org.robolectric.internal.SandboxManager
import org.robolectric.internal.bytecode.InstrumentationConfiguration
import org.robolectric.pluginapi.Sdk
import org.robolectric.plugins.SdkCollection

/**
 * A `SandboxManager` that gives `RobolectricTestRunner` the sandboxes of the whole JVM, which
 * sessions that share sandboxes use too, so that JUnit 4 tests run in the same sandboxes as the
 * tests of an integration. Setting the `robolectric.runner.shareSandboxes` property to `false`
 * makes the runner use sandboxes of its own again.
 *
 * It isn't a plugin of this module: an integration that shares sandboxes makes it the
 * `SandboxManager` of the runner with a `META-INF/services/org.robolectric.internal.SandboxManager`
 * file. It isn't part of the API.
 */
public class SharedSandboxManager
@Inject
constructor(
  private val sandboxBuilder: SandboxManager.SandboxBuilder,
  private val sdkCollection: SdkCollection,
) : SandboxManager(sandboxBuilder, sdkCollection) {
  private val shared = System.getProperty(PROPERTY, "true").toBoolean()

  override fun getAndroidSandbox(
    instrumentationConfig: InstrumentationConfiguration,
    sdk: Sdk,
    resourcesMode: ResourcesMode.Mode,
    looperMode: LooperMode.Mode,
    sqliteMode: SQLiteMode.Mode,
    graphicsMode: GraphicsMode.Mode,
  ): AndroidSandbox {
    if (!shared) {
      return super.getAndroidSandbox(
        instrumentationConfig,
        sdk,
        resourcesMode,
        looperMode,
        sqliteMode,
        graphicsMode,
      )
    }
    val identity =
      SandboxIdentity(instrumentationConfig, sdk, resourcesMode, looperMode, graphicsMode)
    val maxSize = sdkCollection.supportedSdks.size * SandboxPool.SANDBOXES_PER_SDK
    // The runner holds on to sandboxes without giving them back, as with its own sandbox manager.
    return SharedSandboxes.acquire(identity, maxSize, lease = false) {
        sandboxBuilder.build(
          instrumentationConfig,
          sdk,
          sdkCollection.maxSupportedSdk,
          resourcesMode,
          sqliteMode,
        )
      }
      .sandbox
  }

  private companion object {
    private const val PROPERTY = "robolectric.runner.shareSandboxes"
  }
}
