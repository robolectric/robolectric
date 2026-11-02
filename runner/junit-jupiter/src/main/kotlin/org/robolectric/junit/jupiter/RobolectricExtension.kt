@file:OptIn(ExperimentalRunnerApi::class)

package org.robolectric.junit.jupiter

import java.lang.reflect.Constructor
import java.lang.reflect.Method
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.ConditionEvaluationResult
import org.junit.jupiter.api.extension.ExecutionCondition
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.InvocationInterceptor
import org.junit.jupiter.api.extension.ParameterContext
import org.junit.jupiter.api.extension.ParameterResolutionException
import org.junit.jupiter.api.extension.ParameterResolver
import org.junit.jupiter.api.extension.ReflectiveInvocationContext
import org.robolectric.junit.jupiter.internal.AndroidParameters
import org.robolectric.junit.jupiter.internal.DynamicTests
import org.robolectric.junit.jupiter.internal.Environments
import org.robolectric.junit.jupiter.internal.Placeholders
import org.robolectric.junit.jupiter.internal.Plans
import org.robolectric.junit.jupiter.internal.Twins
import org.robolectric.runner.common.ExperimentalRunnerApi

/**
 * Runs the tests of a JUnit Jupiter class in Robolectric's Android environment:
 * ```
 * @ExtendWith(RobolectricExtension::class)
 * class MyTest {
 *   @Test
 *   fun usesAndroid() {
 *     assertThat(ApplicationProvider.getApplicationContext<Context>().packageName).isNotEmpty()
 *   }
 * }
 * ```
 *
 * Tests are configured as with `RobolectricTestRunner`, for example with
 * [org.robolectric.annotation.Config]. A `@Nested` class is configured like the class that encloses
 * it, unless it is configured itself.
 * - JUnit gets a placeholder for the test instance, whose constructor doesn't run. Each call that
 *   JUnit makes on it runs on a twin in Robolectric's sandbox, on Android's main thread, so the
 *   constructor, the lifecycle methods and the tests can use Android. What JUnit injects into the
 *   instance, such as a `@TempDir`, is copied into the twin.
 * - A test runs on every SDK that is selected for it, as one test, and is skipped if none is. A
 *   [RobolectricSdkTest] reports each SDK as a test of its own.
 * - Each test has its own Android environment, unless its class has `@BeforeAll` or `@AfterAll`
 *   methods or uses `@TestInstance(PER_CLASS)`: the tests of such a class share one for each SDK.
 * - Constructors, test methods and lifecycle methods can take the `Context`, the `Application`, an
 *   `ActivityController<T>` or a `ServiceController<T>` as parameters.
 *
 * `runner/README.md` has the details and the limits. This extension is experimental: its behavior
 * may change in future releases.
 */
// It has one function for each kind of call that JUnit makes on a test class.
@Suppress("TooManyFunctions")
public class RobolectricExtension :
  BeforeAllCallback, ExecutionCondition, InvocationInterceptor, ParameterResolver {

  override fun evaluateExecutionCondition(context: ExtensionContext): ConditionEvaluationResult {
    val skipReason = Plans.skipReason(context)
    return if (skipReason != null) {
      ConditionEvaluationResult.disabled(skipReason)
    } else {
      ConditionEvaluationResult.enabled("An Android SDK is selected")
    }
  }

  override fun beforeAll(context: ExtensionContext) {
    Environments.openForClass(context)
  }

  override fun supportsParameter(
    parameterContext: ParameterContext,
    extensionContext: ExtensionContext,
  ): Boolean = AndroidParameters.supports(parameterContext.parameter.type)

  /** Returns a placeholder: the argument is created in the Android environment instead. */
  override fun resolveParameter(
    parameterContext: ParameterContext,
    extensionContext: ExtensionContext,
  ): Any? {
    val executable = parameterContext.declaringExecutable
    if (executable is Constructor<*> && Placeholders.needsConstruction(executable.declaringClass)) {
      throw ParameterResolutionException(
        "${executable.declaringClass.name} registers extensions from instance fields, so JUnit " +
          "constructs it outside of the Android environment, where its constructor can't get a " +
          "${parameterContext.parameter.type.simpleName}. Register the extensions from static " +
          "fields instead"
      )
    }
    return null
  }

  /**
   * Gives JUnit a placeholder instead of constructing the test instance, whose constructor and
   * field initializers only run for its twin, in the Android environment.
   */
  override fun <T : Any> interceptTestClassConstructor(
    invocation: InvocationInterceptor.Invocation<T>,
    invocationContext: ReflectiveInvocationContext<Constructor<T>>,
    extensionContext: ExtensionContext,
  ): T {
    val testClass = invocationContext.executable.declaringClass
    val instance =
      if (Placeholders.needsConstruction(testClass)) {
        invocation.proceed()
      } else {
        invocation.skip()
        Placeholders.allocate(testClass)
      }
    Twins.recordConstruction(extensionContext, instance, invocationContext)
    return instance
  }

  override fun interceptBeforeAllMethod(
    invocation: InvocationInterceptor.Invocation<Void?>,
    invocationContext: ReflectiveInvocationContext<Method>,
    extensionContext: ExtensionContext,
  ): Unit = runInClassEnvironment(invocation, invocationContext, extensionContext)

  override fun interceptAfterAllMethod(
    invocation: InvocationInterceptor.Invocation<Void?>,
    invocationContext: ReflectiveInvocationContext<Method>,
    extensionContext: ExtensionContext,
  ): Unit = runInClassEnvironment(invocation, invocationContext, extensionContext)

  override fun interceptBeforeEachMethod(
    invocation: InvocationInterceptor.Invocation<Void?>,
    invocationContext: ReflectiveInvocationContext<Method>,
    extensionContext: ExtensionContext,
  ): Unit = runInTestEnvironment(invocation, invocationContext, extensionContext)

  override fun interceptTestMethod(
    invocation: InvocationInterceptor.Invocation<Void?>,
    invocationContext: ReflectiveInvocationContext<Method>,
    extensionContext: ExtensionContext,
  ): Unit = runInTestEnvironment(invocation, invocationContext, extensionContext)

  override fun interceptTestTemplateMethod(
    invocation: InvocationInterceptor.Invocation<Void?>,
    invocationContext: ReflectiveInvocationContext<Method>,
    extensionContext: ExtensionContext,
  ): Unit = runInTestEnvironment(invocation, invocationContext, extensionContext)

  override fun interceptAfterEachMethod(
    invocation: InvocationInterceptor.Invocation<Void?>,
    invocationContext: ReflectiveInvocationContext<Method>,
    extensionContext: ExtensionContext,
  ) {
    invocation.skip()
    // If the test has no environments, setting them up failed, and that is the failure to report.
    val testEnvironments = Environments.ofTest(extensionContext) ?: return
    // As JUnit does, clean up where an assumption failed too.
    Twins.invoke(extensionContext, testEnvironments, invocationContext, evenIfAborted = true) { _, _
      ->
    }
  }

  @Suppress("UNCHECKED_CAST")
  override fun <T> interceptTestFactoryMethod(
    invocation: InvocationInterceptor.Invocation<T>,
    invocationContext: ReflectiveInvocationContext<Method>,
    extensionContext: ExtensionContext,
  ): T {
    invocation.skip()
    val tests =
      Twins.invoke(
        extensionContext,
        Environments.forTest(extensionContext),
        invocationContext,
        result = DynamicTests::inEnvironment,
      )
    return DynamicTests.grouped(tests) as T
  }
}

private fun runInClassEnvironment(
  invocation: InvocationInterceptor.Invocation<Void?>,
  invocationContext: ReflectiveInvocationContext<Method>,
  context: ExtensionContext,
) {
  invocation.skip()
  Twins.invoke(context, Environments.ofClass(context), invocationContext) { _, _ -> }
}

private fun runInTestEnvironment(
  invocation: InvocationInterceptor.Invocation<Void?>,
  invocationContext: ReflectiveInvocationContext<Method>,
  context: ExtensionContext,
) {
  invocation.skip()
  Twins.invoke(context, Environments.forTest(context), invocationContext) { _, _ -> }
}
