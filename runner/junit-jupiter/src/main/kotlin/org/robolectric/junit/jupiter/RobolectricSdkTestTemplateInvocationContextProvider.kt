package org.robolectric.junit.jupiter

import java.util.stream.Stream
import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.Extension
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.TestInstanceFactoryContext
import org.junit.jupiter.api.extension.TestInstancePreConstructCallback
import org.junit.jupiter.api.extension.TestTemplateInvocationContext
import org.junit.jupiter.api.extension.TestTemplateInvocationContextProvider
import org.robolectric.runner.common.ExperimentalRunnerApi
import org.robolectric.runner.common.RunnerLogger
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
 *
 * SDK context resolution failures are surfaced as explicit exceptions with diagnostics, so tests
 * are not silently skipped during discovery/execution.
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

    // Reuse the run-wide dependencies/lifecycle manager cached on the root context
    val lifecycleManager = JupiterSharedState.sandboxManager(context)

    val sdkContexts =
      try {
        lifecycleManager.createSandboxes(testClass, testMethod)
      } catch (@Suppress("TooGenericExceptionCaught", "SwallowedException") e: Exception) {
        val message = "Failed to resolve SDK contexts for ${testClass.name}.${testMethod.name}"
        RunnerLogger.error(message, e)
        throw IllegalStateException(message, e)
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
        SdkInvocationContext(displayName, sdkContext) as TestTemplateInvocationContext
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
    private val displayName: String,
    private val sdkContext: SandboxLifecycleManager.SandboxContext,
  ) : TestTemplateInvocationContext {

    override fun getDisplayName(invocationIndex: Int): String = displayName

    override fun getAdditionalExtensions(): List<Extension> {
      return listOf(SdkExecutionExtension(sdkContext))
    }
  }

  /**
   * Extension that configures test execution to use a specific SDK.
   *
   * It publishes the invocation's [SandboxLifecycleManager.SandboxContext] under
   * [JupiterSharedState.SDK_TEMPLATE_CONTEXT_KEY] *before the test instance is constructed*, so
   * [RobolectricExtension.createTestInstance] bootstraps the instance into the sandbox of the SDK
   * selected for this invocation instead of the default one.
   */
  private class SdkExecutionExtension(
    private val sdkContext: SandboxLifecycleManager.SandboxContext
  ) : TestInstancePreConstructCallback, AfterEachCallback {

    override fun preConstructTestInstance(
      factoryContext: TestInstanceFactoryContext,
      context: ExtensionContext,
    ) {
      context
        .getStore(JupiterSharedState.NAMESPACE)
        .put(JupiterSharedState.SDK_TEMPLATE_CONTEXT_KEY, sdkContext)
    }

    override fun afterEach(context: ExtensionContext) {
      context
        .getStore(JupiterSharedState.NAMESPACE)
        .remove(JupiterSharedState.SDK_TEMPLATE_CONTEXT_KEY)
    }
  }
}
