package org.robolectric.runner.common

import java.lang.reflect.Method
import org.robolectric.internal.AndroidSandbox

/**
 * Utility object for bootstrapping test classes and methods through the Robolectric sandbox.
 *
 * This object provides methods to:
 * - Load test classes through the sandbox classloader
 * - Create test instances from bootstrapped classes
 * - Invoke test methods reflectively
 *
 * Bootstrapping is essential for Robolectric to work correctly, as it ensures that test code and
 * Android classes are loaded by the same classloader with instrumentation applied.
 *
 * ## Usage
 *
 * ```kotlin
 * // Bootstrap the test class through sandbox
 * val bootstrappedClass = TestBootstrapper.bootstrapClass<Any>(sandbox, MyTest::class.java)
 *
 * // Create an instance
 * val testInstance = TestBootstrapper.createTestInstance(bootstrappedClass)
 *
 * // Invoke a test method
 * val method = MyTest::class.java.getMethod("testSomething")
 * TestBootstrapper.invokeTestMethod(testInstance, method, emptyArray())
 * ```
 */
@ExperimentalRunnerApi
object TestBootstrapper {
  /**
   * Bootstraps a test class through the Robolectric sandbox classloader.
   *
   * This method loads the specified class through the sandbox's classloader, ensuring that it and
   * all its dependencies are properly instrumented for Robolectric testing.
   *
   * @param T The type to cast the bootstrapped class to
   * @param sandbox The AndroidSandbox containing the instrumented classloader
   * @param testClass The class to bootstrap
   * @return The bootstrapped class loaded through the sandbox classloader
   */
  @JvmStatic
  fun <T : Any> bootstrapClass(sandbox: AndroidSandbox, testClass: Class<*>): Class<T> {
    return sandbox.bootstrappedClass(testClass)
  }

  /**
   * Creates an instance of a bootstrapped test class.
   *
   * This method uses the default (no-argument) constructor to create the instance. If the class is
   * a non-static inner class, it will recursively create an instance of the enclosing class first
   * and use it as the outer instance.
   *
   * @param bootstrappedClass The bootstrapped class to instantiate
   * @return A new instance of the bootstrapped class
   * @throws NoSuchMethodException if the class doesn't have a suitable constructor
   * @throws IllegalAccessException if the constructor is not accessible
   * @throws InstantiationException if the class cannot be instantiated
   */
  @JvmStatic
  fun createTestInstance(bootstrappedClass: Class<*>): Any {
    // Handle non-static inner classes by creating an instance of the enclosing class first
    val enclosing = bootstrappedClass.enclosingClass
    val isNonStaticInner =
      enclosing != null && !java.lang.reflect.Modifier.isStatic(bootstrappedClass.modifiers)
    return if (isNonStaticInner) {
      val outerBootstrapped = enclosing!!
      val outerInstance = createTestInstance(outerBootstrapped)
      val ctor = bootstrappedClass.getDeclaredConstructor(outerBootstrapped)
      ctor.isAccessible = true
      ctor.newInstance(outerInstance)
    } else {
      val ctor = bootstrappedClass.getDeclaredConstructor()
      ctor.isAccessible = true
      ctor.newInstance()
    }
  }

  /**
   * Invokes a test method on a test instance.
   *
   * This method handles reflection-based method invocation, making it suitable for test framework
   * integration where methods need to be called dynamically.
   *
   * @param testInstance The test instance (must be from bootstrapped class)
   * @param method The method to invoke (from original, non-bootstrapped class)
   * @param args The arguments to pass to the method
   * @return The result of the method invocation
   * @throws NoSuchMethodException if the method doesn't exist
   * @throws IllegalAccessException if the method is not accessible
   * @throws java.lang.reflect.InvocationTargetException if the method throws an exception
   */
  @JvmStatic
  @Suppress("SpreadOperator")
  fun invokeTestMethod(testInstance: Any, method: Method, args: Array<Any?>): Any? {
    val bootstrappedClass = testInstance::class.java
    val bootstrappedMethod = bootstrappedClass.getMethod(method.name, *method.parameterTypes)
    bootstrappedMethod.isAccessible = true
    return bootstrappedMethod.invoke(testInstance, *args)
  }

  /**
   * Bootstraps method parameter types through the sandbox classloader.
   *
   * This method is useful when invoking methods with complex parameter types that need to be loaded
   * through the sandbox classloader.
   *
   * @param sandbox The AndroidSandbox containing the instrumented classloader
   * @param parameterTypes The parameter types to bootstrap
   * @return Array of bootstrapped parameter types
   */
  @JvmStatic
  fun bootstrapParameterTypes(
    sandbox: AndroidSandbox,
    parameterTypes: Array<Class<*>>,
  ): Array<Class<*>> {
    return parameterTypes
      .map { type ->
        if (type.isPrimitive) {
          type // Primitive types don't need bootstrapping
        } else {
          sandbox.bootstrappedClass<Any>(type)
        }
      }
      .toTypedArray()
  }
}
