package org.robolectric.junit.jupiter

import java.util.stream.Stream
import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.Extension
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.TestTemplateInvocationContext
import org.junit.jupiter.api.extension.TestTemplateInvocationContextProvider
import org.robolectric.pluginapi.Sdk
import org.robolectric.runner.common.ExperimentalRunnerApi
import org.robolectric.runner.common.RobolectricDependencies
import org.robolectric.runner.common.SandboxLifecycleManager
import org.robolectric.runner.common.SystemPropertiesSupport

/**
 * Provides test template invocation contexts for SDK-parameterized test execution.
 *
 * This provider enables JUnit Jupiter's `@TestTemplate` mechanism to execute a single test method
 * across multiple Android SDK versions, similar to how `@ParameterizedTest` works.
 *
 * It reads the `-Drobolectric.enabledSdks` system property and creates one invocation context per
 * SDK, with proper display names including SDK markers when configured.
 */
@OptIn(ExperimentalRunnerApi::class)
class RobolectricSdkTestTemplateInvocationContextProvider : TestTemplateInvocationContextProvider {

  override fun supportsTestTemplate(context: ExtensionContext): Boolean {
    return context.testMethod
      .map { it.isAnnotationPresent(RobolectricSdkTest::class.java) }
      .orElse(false)
  }

  override fun provideTestTemplateInvocationContexts(
    context: ExtensionContext
  ): Stream<TestTemplateInvocationContext> {
    val testClass = context.requiredTestClass
    val testMethod = context.requiredTestMethod

    // Initialize dependencies and resolve SDK contexts
    val deps = RobolectricDependencies.create()
    val lifecycleManager = SandboxLifecycleManager(deps)

    val sdkContexts =
      try {
        lifecycleManager.createSandboxes(testClass, testMethod)
      } catch (@Suppress("TooGenericExceptionCaught", "SwallowedException") e: Exception) {
        // If sandbox creation fails, return empty stream to skip test execution
        // This allows tests to be discovered even if sandbox initialization fails
        emptyList<SandboxLifecycleManager.SandboxContext>()
      }

    if (sdkContexts.isEmpty()) {
      return Stream.empty()
    }

    val alwaysIncludeMarkers = SystemPropertiesSupport.alwaysIncludeVariantMarkersInTestName()

    return sdkContexts
      .mapIndexed { index, sdkContext ->
        val isLastSdk = index == sdkContexts.size - 1
        val displayName =
          SystemPropertiesSupport.formatTestName(
            testMethod.name,
            sdkContext.sdk.apiLevel,
            alwaysIncludeMarkers,
            isLastSdk,
          )
        SdkInvocationContext(sdkContext.sdk, displayName, lifecycleManager, sdkContext)
          as TestTemplateInvocationContext
      }
      .stream()
  }

  /**
   * Invocation context for a specific SDK.
   *
   * This context provides the display name and extensions needed to execute a test with a specific
   * Android SDK version.
   */
  private class SdkInvocationContext(
    private val sdk: Sdk,
    private val displayName: String,
    private val lifecycleManager: SandboxLifecycleManager,
    private val sdkContext: SandboxLifecycleManager.SandboxContext,
  ) : TestTemplateInvocationContext {

    override fun getDisplayName(invocationIndex: Int): String = displayName

    override fun getAdditionalExtensions(): List<Extension> {
      return listOf(SdkExecutionExtension(sdk, lifecycleManager, sdkContext))
    }
  }

  /**
   * Extension that configures test execution to use a specific SDK.
   *
   * This extension integrates with the existing RobolectricExtension to ensure the correct sandbox
   * is used for the test execution.
   */
  private class SdkExecutionExtension(
    private val sdk: Sdk,
    private val lifecycleManager: SandboxLifecycleManager,
    private val sdkContext: SandboxLifecycleManager.SandboxContext,
  ) : BeforeEachCallback, AfterEachCallback {

    companion object {
      private val NAMESPACE = ExtensionContext.Namespace.create(SdkExecutionExtension::class.java)
      private const val SDK_CONTEXT_KEY = "sdkContext"
    }

    override fun beforeEach(context: ExtensionContext) {
      // Store the SDK context for the RobolectricExtension to use
      context.getStore(NAMESPACE).put(SDK_CONTEXT_KEY, sdkContext)
    }

    override fun afterEach(context: ExtensionContext) {
      // Cleanup handled by RobolectricExtension
      context.getStore(NAMESPACE).remove(SDK_CONTEXT_KEY)
    }
  }
}
