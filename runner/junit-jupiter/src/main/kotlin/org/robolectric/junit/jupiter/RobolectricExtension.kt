@file:OptIn(org.robolectric.runner.common.ExperimentalRunnerApi::class)

package org.robolectric.junit.jupiter

import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.ExtensionContext.Namespace
import org.junit.jupiter.api.extension.ExtensionContext.Store.CloseableResource
import org.junit.jupiter.api.extension.LifecycleMethodExecutionExceptionHandler
import org.junit.jupiter.api.extension.ParameterContext
import org.junit.jupiter.api.extension.ParameterResolver
import org.junit.jupiter.api.extension.TestExecutionExceptionHandler
import org.junit.jupiter.api.extension.TestInstanceFactoryContext
import org.junit.jupiter.api.extension.TestInstancePostProcessor
import org.junit.jupiter.api.extension.TestInstancePreConstructCallback
import org.junit.jupiter.api.extension.TestInstantiationAwareExtension.ExtensionContextScope
import org.robolectric.TestLifecycle
import org.robolectric.junit.jupiter.internal.AndroidParameters
import org.robolectric.junit.platform.ActiveSandboxes
import org.robolectric.runner.common.RobolectricEnvironment

/**
 * Sets up Android for tests executed by the `robolectric` JUnit Platform engine.
 *
 * The engine loads the test and all its extensions inside the sandbox. Test instances are
 * constructed after the application is set up. Class-level code runs in the sandbox before
 * application setup, as JUnit 4 class rules do. `PER_CLASS` tests share an application.
 */
@Suppress("TooManyFunctions")
public class RobolectricExtension :
  BeforeAllCallback,
  BeforeEachCallback,
  TestInstancePreConstructCallback,
  TestInstancePostProcessor,
  ParameterResolver,
  TestExecutionExceptionHandler,
  LifecycleMethodExecutionExceptionHandler {

  override fun getTestInstantiationExtensionContextScope(
    rootContext: ExtensionContext,
  ): ExtensionContextScope = ExtensionContextScope.TEST_METHOD

  override fun beforeAll(context: ExtensionContext) {
    ActiveSandboxes.forClass(context.requiredTestClass)
  }

  override fun preConstructTestInstance(
    factoryContext: TestInstanceFactoryContext,
    context: ExtensionContext,
  ) {
    environment(context)
    if (context.testMethod.isPresent) scope(context)
  }

  override fun postProcessTestInstance(testInstance: Any, context: ExtensionContext) {
    if (context.testMethod.isPresent) scope(context).lifecycle.prepareTest(testInstance)
  }

  override fun beforeEach(context: ExtensionContext) {
    val scope = scope(context)
    if (context.testInstanceLifecycle.orElse(null) == TestInstance.Lifecycle.PER_CLASS) {
      context.testInstances.ifPresent { it.allInstances.forEach(scope.lifecycle::prepareTest) }
    }
  }

  override fun supportsParameter(
    parameterContext: ParameterContext,
    extensionContext: ExtensionContext,
  ): Boolean = AndroidParameters.supports(parameterContext.parameter.type)

  override fun resolveParameter(
    parameterContext: ParameterContext,
    extensionContext: ExtensionContext,
  ): Any? = AndroidParameters.resolve(environment(extensionContext), parameterContext.parameter)

  override fun handleTestExecutionException(context: ExtensionContext, throwable: Throwable) {
    diagnose(context, throwable)
    throw throwable
  }

  override fun handleBeforeEachMethodExecutionException(
    context: ExtensionContext,
    throwable: Throwable,
  ) {
    diagnose(context, throwable)
    throw throwable
  }

  override fun handleAfterEachMethodExecutionException(
    context: ExtensionContext,
    throwable: Throwable,
  ) {
    diagnose(context, throwable)
    throw throwable
  }

  private fun diagnose(context: ExtensionContext, throwable: Throwable) {
    val resource = context.getStore(NAMESPACE).get(ENVIRONMENT, EnvironmentResource::class.java)
    resource?.environment?.diagnoseFailure(throwable)
  }

  private fun environment(context: ExtensionContext): RobolectricEnvironment {
    val active = ActiveSandboxes.forClass(context.requiredTestClass)
    val store = context.getStore(NAMESPACE)
    val existing = store.get(ENVIRONMENT, EnvironmentResource::class.java)
    val configuration = active.configuration(context.uniqueId)
    if (existing != null) {
      check(existing.environment.configuration == configuration) {
        "${context.requiredTestClass.name} is configured differently than its shared " +
          "PER_CLASS Android environment"
      }
      return existing.environment
    }
    val environment = active.sandbox.open(configuration)
    store.put(ENVIRONMENT, EnvironmentResource(environment))
    return environment
  }

  private fun scope(context: ExtensionContext): TestScope =
    context
      .getStore(NAMESPACE)
      .computeIfAbsent(
        TEST_SCOPE,
        {
          val environment = environment(context)
          val lifecycle =
            environment.classLoader
              .loadClass("org.robolectric.DefaultTestLifecycle")
              .getDeclaredConstructor()
              .newInstance() as TestLifecycle
          lifecycle.beforeTest(context.requiredTestMethod)
          TestScope(context, lifecycle)
        },
        TestScope::class.java,
      )

  private class EnvironmentResource(val environment: RobolectricEnvironment) :
    CloseableResource, AutoCloseable {
    override fun close() = environment.close()
  }

  private class TestScope(val context: ExtensionContext, val lifecycle: TestLifecycle) :
    CloseableResource, AutoCloseable {
    override fun close() {
      lifecycle.afterTest(context.requiredTestMethod)
    }
  }

  private companion object {
    val NAMESPACE: Namespace = Namespace.create(RobolectricExtension::class.java)
    const val ENVIRONMENT = "environment"
    const val TEST_SCOPE = "testScope"
  }
}
