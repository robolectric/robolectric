package org.robolectric.runner.common

import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import org.robolectric.internal.AndroidSandbox

/**
 * Canonical "bootstrap the test class, run @Before*, invoke the method, run @After* in finally"
 * helper. Shared by both `:runner:junit-platform` and `:runner:junit-jupiter` so the three prior
 * near-identical copies (Platform engine, Jupiter engine, RobolectricSandboxExecutor) cannot drift
 * apart.
 *
 * Designed to be called **from the sandbox main thread** (the caller arranges
 * `sandbox.runOnMainThread { … }`). This keeps the helper stateless and lets the caller choose
 * between persistent and isolated environments.
 *
 * Parameter resolution is delegated to [ParameterResolutionHelper], which in turn uses the supplied
 * [ParameterResolver] (defaults to [DefaultRobolectricParameterResolver]).
 */
@ExperimentalRunnerApi
object TestMethodInvoker {

  /**
   * Bootstraps the given test class into the sandbox, runs `@Before*` methods, invokes the test
   * method (with parameter resolution if needed), then runs `@After*` methods in a `finally` block.
   *
   * `@After*` failures are logged via [RunnerLogger.error] but do not mask a test-body failure. If
   * only the `@After*` fails, that exception propagates.
   *
   * @param sandbox the sandbox the test runs in
   * @param testClass the original (un-bootstrapped) test class
   * @param testMethod the test method to invoke
   * @param beforeEachAnnotations annotations whose methods should be invoked pre-test (ordered:
   *   superclass-first, see [LifecycleHelper.invokeLifecycleMethods])
   * @param afterEachAnnotations annotations whose methods should be invoked post-test (ordered:
   *   subclass-first)
   * @param parameterResolver resolver for test method parameters
   */
  @Suppress("LongParameterList", "SpreadOperator")
  @JvmStatic
  @JvmOverloads
  fun invoke(
    sandbox: AndroidSandbox,
    testClass: Class<*>,
    testMethod: Method,
    beforeEachAnnotations: List<Class<out Annotation>> = emptyList(),
    afterEachAnnotations: List<Class<out Annotation>> = emptyList(),
    parameterResolver: ParameterResolver = DefaultRobolectricParameterResolver,
  ) {
    val bootstrappedTestClass = TestBootstrapper.bootstrapClass<Any>(sandbox, testClass)
    val testInstance = TestBootstrapper.createTestInstance(bootstrappedTestClass)

    beforeEachAnnotations.forEach { annotation ->
      LifecycleHelper.invokeLifecycleMethods(bootstrappedTestClass, testInstance, annotation)
    }

    try {
      invokeTestMethod(bootstrappedTestClass, testInstance, testMethod, sandbox, parameterResolver)
    } finally {
      invokeAfterEachLoggingFailures(
        bootstrappedTestClass,
        testInstance,
        testMethod,
        testClass,
        afterEachAnnotations,
      )
    }
  }

  @Suppress("SpreadOperator")
  private fun invokeTestMethod(
    bootstrappedTestClass: Class<*>,
    testInstance: Any,
    testMethod: Method,
    sandbox: AndroidSandbox,
    parameterResolver: ParameterResolver,
  ) {
    try {
      if (testMethod.parameterCount > 0) {
        val bootstrappedParameterTypes =
          TestBootstrapper.bootstrapParameterTypes(sandbox, testMethod.parameterTypes)
        val bootstrappedMethod =
          bootstrappedTestClass.getMethod(testMethod.name, *bootstrappedParameterTypes)
        bootstrappedMethod.isAccessible = true
        val args =
          ParameterResolutionHelper.resolveParameters(
            testMethod.parameters,
            sandbox,
            parameterResolver,
          )
        bootstrappedMethod.invoke(testInstance, *args)
      } else {
        TestBootstrapper.invokeTestMethod(testInstance, testMethod, emptyArray())
      }
    } catch (e: InvocationTargetException) {
      // Unwrap so test frameworks see the actual assertion failure.
      throw e.targetException ?: e
    }
  }

  @Suppress("TooGenericExceptionCaught", "SwallowedException")
  private fun invokeAfterEachLoggingFailures(
    bootstrappedTestClass: Class<*>,
    testInstance: Any,
    testMethod: Method,
    originalTestClass: Class<*>,
    afterEachAnnotations: List<Class<out Annotation>>,
  ) {
    // Run in reverse so that "After" semantics mirror the Before order: subclass after methods
    // run before superclass after methods. LifecycleHelper already handles inner ordering, we
    // just feed annotations in reverse so a user-supplied ordered list is honored.
    afterEachAnnotations.asReversed().forEach { annotation ->
      try {
        LifecycleHelper.invokeLifecycleMethods(bootstrappedTestClass, testInstance, annotation)
      } catch (e: Throwable) {
        RunnerLogger.error(
          "Error in ${annotation.simpleName} for " +
            "${originalTestClass.simpleName}.${testMethod.name}",
          e,
        )
      }
    }
  }
}
