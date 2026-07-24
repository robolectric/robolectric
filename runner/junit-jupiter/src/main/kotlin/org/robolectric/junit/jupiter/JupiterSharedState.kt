package org.robolectric.junit.jupiter

import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.ExtensionContext.Store
import org.robolectric.runner.common.ExperimentalRunnerApi
import org.robolectric.runner.common.RobolectricDependencies
import org.robolectric.runner.common.RobolectricSandboxExecutor
import org.robolectric.runner.common.SandboxLifecycleManager

/**
 * Shared extension-store plumbing for the Jupiter integration.
 *
 * [RobolectricExtension] and [RobolectricSdkTestTemplateInvocationContextProvider] need to
 * cooperate through the [ExtensionContext] store:
 * - Both reuse the same cached [RobolectricDependencies]/[SandboxLifecycleManager] (creating them
 *   is expensive), stored on the root context.
 * - `@RobolectricSdkTest` template invocations publish their per-SDK sandbox context under
 *   [SDK_TEMPLATE_CONTEXT_KEY] so [RobolectricExtension] creates the test instance in — and runs
 *   the test on — the SDK selected for that invocation.
 */
@OptIn(ExperimentalRunnerApi::class)
internal object JupiterSharedState {
  /** Namespace shared across the Jupiter integration. */
  val NAMESPACE: ExtensionContext.Namespace =
    ExtensionContext.Namespace.create(RobolectricExtension::class.java)

  /** Key under which [RobolectricSdkTest] invocations publish their SDK-specific sandbox. */
  const val SDK_TEMPLATE_CONTEXT_KEY = "sdkTemplateContext"

  private const val DEPENDENCIES_KEY = "dependencies"
  private const val SANDBOX_MANAGER_KEY = "sandboxManager"
  private const val SANDBOX_EXECUTOR_KEY = "sandboxExecutor"

  private fun <T : Any> Store.getOrCreate(
    key: String,
    requiredType: Class<T>,
    defaultCreator: () -> T,
  ): T = computeIfAbsent(key, { defaultCreator() }, requiredType)

  /** Gets or creates the shared [RobolectricDependencies] for this test run. */
  fun dependencies(context: ExtensionContext): RobolectricDependencies =
    context.root.getStore(NAMESPACE).getOrCreate(
      DEPENDENCIES_KEY,
      RobolectricDependencies::class.java,
    ) {
      RobolectricDependencies.create()
    }

  /** Gets or creates the shared [SandboxLifecycleManager] for this test run. */
  fun sandboxManager(context: ExtensionContext): SandboxLifecycleManager =
    context.root.getStore(NAMESPACE).getOrCreate(
      SANDBOX_MANAGER_KEY,
      SandboxLifecycleManager::class.java,
    ) {
      SandboxLifecycleManager(dependencies(context))
    }

  /** Gets or creates the shared [RobolectricSandboxExecutor] for isolated per-method runs. */
  fun sandboxExecutor(context: ExtensionContext): RobolectricSandboxExecutor =
    context.root.getStore(NAMESPACE).getOrCreate(
      SANDBOX_EXECUTOR_KEY,
      RobolectricSandboxExecutor::class.java,
    ) {
      RobolectricSandboxExecutor(sandboxManager(context))
    }

  /** Returns the SDK-template sandbox context for the current invocation, if any. */
  fun sdkTemplateContext(context: ExtensionContext): SandboxLifecycleManager.SandboxContext? =
    context.getStore(NAMESPACE).get(SDK_TEMPLATE_CONTEXT_KEY)
      as? SandboxLifecycleManager.SandboxContext
}
