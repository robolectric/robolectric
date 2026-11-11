package org.robolectric.runner.common.internal

import java.security.MessageDigest
import org.robolectric.android.AndroidSdkShadowMatcher
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.robolectric.annotation.LooperMode
import org.robolectric.annotation.ResourcesMode
import org.robolectric.annotation.SQLiteMode
import org.robolectric.config.AndroidConfigurer
import org.robolectric.interceptors.AndroidInterceptors
import org.robolectric.internal.AndroidSandbox
import org.robolectric.internal.bytecode.ClassHandlerBuilder
import org.robolectric.internal.bytecode.InstrumentationConfiguration
import org.robolectric.internal.bytecode.Interceptors
import org.robolectric.internal.bytecode.ShadowProviders
import org.robolectric.pluginapi.MethodHandleDecorator
import org.robolectric.runner.common.ExperimentalRunnerApi
import org.robolectric.runner.common.SandboxKey
import org.robolectric.util.inject.Injector

/**
 * Tells which sandbox fits a configuration, and configures a sandbox for it, as
 * `RobolectricTestRunner` does for a test.
 */
@OptIn(ExperimentalRunnerApi::class)
internal class SandboxSetup(
  injector: Injector,
  private val sharedPackages: List<String>,
  /** Whether the session's sandboxes get Kotlin's standard library of their own. */
  private val ownKotlin: Boolean,
) {
  private val androidConfigurer = injector.getInstance(AndroidConfigurer::class.java)
  private val shadowProviders = injector.getInstance(ShadowProviders::class.java)
  private val classHandlerBuilder = injector.getInstance(ClassHandlerBuilder::class.java)
  private val methodHandleDecorators =
    injector.getInstance(Array<MethodHandleDecorator>::class.java).toList()
  private val interceptors = Interceptors(AndroidInterceptors.all())

  /** Returns the key of the sandboxes that fit the configuration. */
  fun key(configuration: EnvironmentConfiguration): SandboxKey {
    val identity =
      SandboxIdentity(
        instrumentation(configuration),
        configuration.sdk,
        configuration.get(ResourcesMode.Mode::class.java),
        configuration.get(LooperMode.Mode::class.java),
        configuration.get(GraphicsMode.Mode::class.java),
      )
    return SandboxKey(identity, configuration.sdk.apiLevel, id(identity))
  }

  /** Configures a sandbox that fits the configuration, as the test runner does before a test. */
  fun configure(sandbox: AndroidSandbox, configuration: EnvironmentConfiguration) {
    val config = configuration.get(Config::class.java)
    sandbox.updateModes(configuration.get(SQLiteMode.Mode::class.java))
    // Code that runs before an application is set up, such as that of a test class, reads it.
    sandbox.updateConfigurationRegistry(configuration)
    val shadowMapBuilder = shadowProviders.baseShadowMap.newBuilder()
    config.shadows.forEach { shadowMapBuilder.addShadowClasses(it.java) }
    val shadowMap = shadowMapBuilder.build()
    sandbox.replaceShadowMap(shadowMap)
    sandbox.configure(
      classHandlerBuilder.build(
        shadowMap,
        AndroidSdkShadowMatcher(configuration.sdk.apiLevel),
        interceptors,
        methodHandleDecorators,
      ),
      interceptors,
    )
  }

  private fun instrumentation(
    configuration: EnvironmentConfiguration,
  ): InstrumentationConfiguration {
    val builder = InstrumentationConfiguration.newBuilder()
    val customPackages = System.getProperty("org.robolectric.packagesToNotAcquire", "").split(',')
    (customPackages.filter { it.isNotEmpty() } + sharedPackages).forEach {
      builder.doNotAcquirePackage(it)
    }
    val classesToNotInstrument =
      System.getProperty("org.robolectric.classesToNotInstrumentRegex", "")
    if (classesToNotInstrument.isNotEmpty()) {
      builder.setDoNotInstrumentClassRegex(classesToNotInstrument)
    }
    androidConfigurer.configure(builder, interceptors)
    androidConfigurer.withConfig(builder, configuration.get(Config::class.java))
    configuration.instrumentedPackages.forEach { builder.addInstrumentedPackage(it) }
    if (ownKotlin) {
      // An app that brings Kotlin's standard library, such as one that its build has desugared,
      // gets that in its sandbox, where a test shares that of the JVM.
      builder.packagesToNotAcquire.remove(KOTLIN_PACKAGE)
    }
    return builder.build()
  }

  private companion object {
    private const val KOTLIN_PACKAGE = "kotlin."
    private const val ID_HASH_LENGTH = 8

    /**
     * Returns an ID of a sandbox identity that is the same in every JVM: the SDK, and a hash of the
     * rest of what makes sandboxes interchangeable.
     */
    private fun id(identity: SandboxIdentity): String {
      val instrumentation = InstrumentationConfiguration.Builder(identity.instrumentation)
      val description =
        listOf(
            identity.resourcesMode.name,
            identity.looperMode.name,
            identity.graphicsMode.name,
            instrumentation.instrumentedPackages.sorted(),
            instrumentation.instrumentedClasses.sorted(),
            instrumentation.classesToNotAcquire.sorted(),
            instrumentation.classNameTranslations.entries.map { "${it.key}=${it.value}" }.sorted(),
            instrumentation.interceptedMethods.map { "${it.className}#${it.methodName}" }.sorted(),
          )
          .joinToString("\n")
      val digest = MessageDigest.getInstance("SHA-256").digest(description.toByteArray())
      val hash = digest.joinToString("") { "%02x".format(it) }.take(ID_HASH_LENGTH)
      return "${identity.sdk.apiLevel}-$hash"
    }
  }
}
