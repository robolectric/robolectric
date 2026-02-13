package org.robolectric.junit.jupiter

import java.lang.reflect.Method
import org.junit.jupiter.api.extension.AfterAllCallback
import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.InvocationInterceptor
import org.junit.jupiter.api.extension.ReflectiveInvocationContext
import org.junit.jupiter.api.extension.TestInstanceFactory
import org.junit.jupiter.api.extension.TestInstanceFactoryContext
import org.robolectric.android.controller.ComponentController
import org.robolectric.runner.common.ClassLifecycleManager
import org.robolectric.runner.common.ExperimentalRunnerApi
import org.robolectric.runner.common.RobolectricDependencies
import org.robolectric.runner.common.RunnerLogger
import org.robolectric.runner.common.RunnerMetrics
import org.robolectric.runner.common.SandboxLifecycleManager
import org.robolectric.runner.common.TestBootstrapper

/**
 * JUnit Jupiter extension for Robolectric.
 *
 * This extension provides parameter resolution for common Android components and manages the
 * lifecycle of injected controllers.
 *
 * ## Features
 * - Automatic cleanup of injected controllers
 * - Proper lifecycle management using ExtensionContext.Store
 * - Support for pure JUnit Jupiter execution (TestInstanceFactory, InvocationInterceptor)
 *
 * ## Usage
 *
 * ```kotlin
 * @ExtendWith(RobolectricExtension::class)
 * class MyTest {
 *   @Test
 *   fun testWithContext(context: Context) {
 *     // context is automatically injected
 *   }
 *
 *   @Test
 *   fun testWithActivity(controller: ActivityController<MyActivity>) {
 *     val activity = controller.setup().get()
 *     // controller is automatically cleaned up after test
 *   }
 * }
 * ```
 */
@Suppress("TooManyFunctions")
@OptIn(ExperimentalRunnerApi::class)
class RobolectricExtension :
  BeforeAllCallback,
  AfterAllCallback,
  BeforeEachCallback,
  AfterEachCallback,
  TestInstanceFactory,
  InvocationInterceptor {

  companion object {
    private val NAMESPACE = ExtensionContext.Namespace.create(RobolectricExtension::class.java)
    private const val CONTROLLERS_KEY = "controllers"
    private const val SANDBOX_KEY = "sandbox"
    private const val CLASS_LIFECYCLE_KEY = "classLifecycle"
    private const val DEPENDENCIES_KEY = "dependencies"
    private const val SANDBOX_MANAGER_KEY = "sandboxManager"
  }

  /** Gets or creates shared RobolectricDependencies for this test class. */
  private fun getOrCreateDependencies(context: ExtensionContext): RobolectricDependencies {
    return context.root
      .getStore(NAMESPACE)
      .getOrComputeIfAbsent(
        DEPENDENCIES_KEY,
        { RobolectricDependencies.create() },
        RobolectricDependencies::class.java,
      )!!
  }

  /** Gets or creates shared SandboxLifecycleManager for this test class. */
  private fun getOrCreateSandboxManager(context: ExtensionContext): SandboxLifecycleManager {
    return context.root
      .getStore(NAMESPACE)
      .getOrComputeIfAbsent(
        SANDBOX_MANAGER_KEY,
        { SandboxLifecycleManager(getOrCreateDependencies(context)) },
        SandboxLifecycleManager::class.java,
      )!!
  }

  override fun beforeAll(context: ExtensionContext) {
    val testClass = context.requiredTestClass

    // Get cached sandbox lifecycle manager
    val sandboxLifecycleManager = getOrCreateSandboxManager(context)
    val classLifecycleManager = ClassLifecycleManager(sandboxLifecycleManager)

    // Setup class-level Robolectric environment
    classLifecycleManager.setupForClass(testClass)

    // Store the manager for use in beforeEach and afterAll
    context
      .getStore(NAMESPACE)
      .put(CLASS_LIFECYCLE_KEY, ClassLifecycleResource(testClass, classLifecycleManager))
  }

  override fun afterAll(context: ExtensionContext) {
    // Cleanup is handled automatically by ClassLifecycleResource
  }

  override fun createTestInstance(
    factoryContext: TestInstanceFactoryContext,
    extensionContext: ExtensionContext,
  ): Any {
    val testClass = factoryContext.testClass
    val testMethod = extensionContext.testMethod.orElse(null)

    // Check if we have a class-level lifecycle manager with shared sandbox
    val classResource =
      extensionContext.getStore(NAMESPACE).get(CLASS_LIFECYCLE_KEY) as? ClassLifecycleResource
    val sandboxContext =
      if (classResource != null) {
        // Reuse class-level sandbox
        checkNotNull(classResource.manager.getClassContext(testClass)) {
          "Class context not found for $testClass"
        }
      } else {
        // Fall back to per-test sandbox (for backward compatibility)
        val lifecycleManager = getOrCreateSandboxManager(extensionContext)
        lifecycleManager.createSandbox(testClass, testMethod)
      }

    // Store sandbox context for later use
    extensionContext.getStore(NAMESPACE).put(SANDBOX_KEY, SandboxResource(sandboxContext))

    // Bootstrap and create instance
    val bootstrappedClass = TestBootstrapper.bootstrapClass<Any>(sandboxContext.sandbox, testClass)
    return TestBootstrapper.createTestInstance(bootstrappedClass)
  }

  override fun interceptTestMethod(
    invocation: InvocationInterceptor.Invocation<Void?>,
    invocationContext: ReflectiveInvocationContext<Method>,
    extensionContext: ExtensionContext,
  ) {
    val resource = extensionContext.getStore(NAMESPACE).get(SANDBOX_KEY) as? SandboxResource
    val classResource =
      extensionContext.getStore(NAMESPACE).get(CLASS_LIFECYCLE_KEY) as? ClassLifecycleResource

    if (resource != null) {
      val sandboxContext = resource.context
      val testClass = extensionContext.requiredTestClass
      val testMethod = invocationContext.executable.name

      val startTime = System.currentTimeMillis()
      var success = false

      try {
        RunnerLogger.logTestStart(testClass.simpleName, testMethod, sandboxContext.sdk.apiLevel)

        if (classResource != null) {
          // Use ClassLifecycleManager to execute in persistent environment
          RunnerMetrics.timed(RunnerMetrics.PHASE_TEST_EXECUTION) {
            classResource.manager.executeInClassContext(testClass, testMethod) {
              invocation.proceed()
            }
          }
        } else {
          val lifecycleManager = getOrCreateSandboxManager(extensionContext)

          RunnerMetrics.timed(RunnerMetrics.PHASE_TEST_EXECUTION) {
            sandboxContext.sandbox.runOnMainThread {
              lifecycleManager.executeInSandbox(sandboxContext, testMethod) { invocation.proceed() }
            }
          }
        }
        success = true
      } finally {
        val duration = System.currentTimeMillis() - startTime
        RunnerLogger.logTestEnd(testClass.simpleName, testMethod, duration, success)
        RunnerMetrics.recordTestExecution(success)
      }
    } else {
      invocation.proceed()
    }
  }

  override fun interceptBeforeAllMethod(
    invocation: InvocationInterceptor.Invocation<Void?>,
    invocationContext: ReflectiveInvocationContext<Method>,
    extensionContext: ExtensionContext,
  ) {
    val classResource =
      extensionContext.getStore(NAMESPACE).get(CLASS_LIFECYCLE_KEY) as? ClassLifecycleResource

    if (classResource != null) {
      val testClass = extensionContext.requiredTestClass
      // Execute @BeforeAll inside the sandbox
      classResource.manager.getClassContext(testClass)?.sandbox?.runOnMainThread {
        classResource.manager.executeInClassContext(testClass, invocationContext.executable.name) {
          invocation.proceed()
        }
      }
    } else {
      invocation.proceed()
    }
  }

  override fun interceptAfterAllMethod(
    invocation: InvocationInterceptor.Invocation<Void?>,
    invocationContext: ReflectiveInvocationContext<Method>,
    extensionContext: ExtensionContext,
  ) {
    val classResource =
      extensionContext.getStore(NAMESPACE).get(CLASS_LIFECYCLE_KEY) as? ClassLifecycleResource

    if (classResource != null) {
      val testClass = extensionContext.requiredTestClass
      // Execute @AfterAll inside the sandbox
      classResource.manager.getClassContext(testClass)?.sandbox?.runOnMainThread {
        classResource.manager.executeInClassContext(testClass, invocationContext.executable.name) {
          invocation.proceed()
        }
      }
    } else {
      invocation.proceed()
    }
  }

  override fun beforeEach(context: ExtensionContext) {
    // Initialize controller tracker with automatic cleanup
    context
      .getStore(NAMESPACE)
      .getOrComputeIfAbsent(CONTROLLERS_KEY, { ControllerTracker() }, ControllerTracker::class.java)
  }

  override fun afterEach(context: ExtensionContext) {
    release(context)
  }

  /** Internal use by the Engine to clean up controllers. */
  fun release(context: ExtensionContext?) {
    if (context == null) {
      return
    }
    val tracker = context.getStore(NAMESPACE).get(CONTROLLERS_KEY) as? ControllerTracker
    tracker?.close()
  }

  /** Holds the sandbox context and implements CloseableResource for cleanup. */
  private class SandboxResource(val context: SandboxLifecycleManager.SandboxContext) :
    AutoCloseable {
    override fun close() {
      // Sandbox cleanup is handled by executeInSandbox, but we can clear references here if needed
    }
  }

  /**
   * Tracks component controllers and provides automatic cleanup. Implements CloseableResource for
   * proper lifecycle management.
   */
  private class ControllerTracker : AutoCloseable {
    private val controllers = mutableListOf<ComponentController<*, *>>()

    fun add(controller: ComponentController<*, *>) {
      controllers.add(controller)
    }

    override fun close() {
      controllers.forEach { runCatching { it.destroy() } }
      controllers.clear()
    }
  }

  /**
   * Holds the class lifecycle manager and implements CloseableResource for cleanup. This ensures
   * that the class-level Robolectric environment is properly torn down after all tests complete.
   */
  private class ClassLifecycleResource(
    private val testClass: Class<*>,
    val manager: ClassLifecycleManager,
  ) : AutoCloseable {
    override fun close() {
      manager.tearDownForClass(testClass)
    }
  }
}
