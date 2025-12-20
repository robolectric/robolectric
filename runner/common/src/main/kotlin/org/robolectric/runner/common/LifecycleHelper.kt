package org.robolectric.runner.common

import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.lang.reflect.Modifier

/** Utilities for handling test lifecycle methods. */
@ExperimentalRunnerApi
object LifecycleHelper {

  /**
   * Invokes static lifecycle methods (e.g. @BeforeAll, @AfterAll) on the test class.
   *
   * Per JUnit 5 specification:
   * - @BeforeAll methods in superclasses are executed before those in subclasses
   * - @AfterAll methods in subclasses are executed before those in superclasses
   * - Overridden methods are only executed once (in the subclass)
   *
   * @param bootstrappedTestClass The test class loaded in the sandbox
   * @param annotationClass The annotation class to search for (e.g.
   *   org.junit.jupiter.api.BeforeAll)
   */
  @JvmStatic
  fun invokeStaticLifecycleMethods(bootstrappedTestClass: Class<*>, annotationClass: Class<*>) {
    // Collect all static lifecycle methods from the class hierarchy
    val lifecycleMethods = mutableListOf<Method>()
    val seenSignatures = mutableSetOf<String>()
    var currentClass: Class<*>? = bootstrappedTestClass

    while (currentClass != null && currentClass != Object::class.java) {
      currentClass.declaredMethods
        .filter { method -> Modifier.isStatic(method.modifiers) }
        .filter { method -> hasLifecycleAnnotation(method, annotationClass) }
        .forEach { method ->
          val signature = methodSignature(method)
          // Only add if not already seen (prevents calling overridden methods twice)
          if (seenSignatures.add(signature)) {
            lifecycleMethods.add(method)
          }
        }
      currentClass = currentClass.superclass
    }

    // Order methods: superclass-first for "Before", subclass-first for "After"
    val orderedMethods =
      if (isBeforeAnnotation(annotationClass)) {
        lifecycleMethods.asReversed()
      } else {
        lifecycleMethods
      }

    // Invoke all lifecycle methods
    orderedMethods.forEach { method ->
      try {
        method.isAccessible = true
        method.invoke(null)
      } catch (e: InvocationTargetException) {
        // Rethrow the actual exception from the lifecycle method
        throw e.targetException ?: e
      }
    }
  }

  /**
   * Invokes lifecycle methods (e.g. @BeforeEach, @AfterEach) on the test instance. Searches the
   * class hierarchy for methods annotated with the specified annotation.
   *
   * Per JUnit 5 specification:
   * - @BeforeEach methods in superclasses are executed before those in subclasses
   * - @AfterEach methods in subclasses are executed before those in superclasses
   * - Overridden methods are only executed once (in the subclass)
   *
   * @param bootstrappedTestClass The test class loaded in the sandbox
   * @param testInstance The test instance
   * @param annotationClass The annotation class to search for (e.g.
   *   org.junit.jupiter.api.BeforeEach)
   */
  fun invokeLifecycleMethods(
    bootstrappedTestClass: Class<*>,
    testInstance: Any,
    annotationClass: Class<*>,
  ) {
    // Collect all lifecycle methods from the class hierarchy
    val lifecycleMethods = mutableListOf<Method>()
    val seenSignatures = mutableSetOf<String>()
    var currentClass: Class<*>? = bootstrappedTestClass

    while (currentClass != null && currentClass != Object::class.java) {
      currentClass.declaredMethods
        .filter { method -> !Modifier.isStatic(method.modifiers) }
        .filter { method -> hasLifecycleAnnotation(method, annotationClass) }
        .forEach { method ->
          val signature = methodSignature(method)
          // Only add if not already seen (prevents calling overridden methods twice)
          if (seenSignatures.add(signature)) {
            lifecycleMethods.add(method)
          }
        }
      currentClass = currentClass.superclass
    }

    // Order methods: superclass-first for "Before", subclass-first for "After"
    val orderedMethods =
      if (isBeforeAnnotation(annotationClass)) {
        lifecycleMethods.asReversed()
      } else {
        lifecycleMethods
      }

    // Invoke all lifecycle methods
    orderedMethods.forEach { method ->
      try {
        method.isAccessible = true
        method.invoke(testInstance)
      } catch (e: InvocationTargetException) {
        // Rethrow the actual exception from the lifecycle method
        throw e.targetException ?: e
      }
    }
  }

  /** Creates a signature string for a method to detect overrides. */
  private fun methodSignature(method: Method): String {
    return method.name + method.parameterTypes.joinToString(",") { it.name }
  }

  /** Checks if the annotation is a "Before" type (BeforeAll, BeforeEach, Before, etc.). */
  private fun isBeforeAnnotation(annotationClass: Class<*>): Boolean {
    val name = annotationClass.simpleName
    return name.startsWith("Before") || name == "Before"
  }

  /** Checks if a method has a lifecycle annotation, handling classloader differences. */
  @Suppress("SwallowedException", "TooGenericExceptionCaught")
  private fun hasLifecycleAnnotation(method: Method, annotationClass: Class<*>): Boolean {
    return method.annotations.any { methodAnnotation ->
      // Match by annotation class name to handle classloader differences
      methodAnnotation.annotationClass.java.name == annotationClass.name ||
        methodAnnotation.annotationClass.qualifiedName == annotationClass.name ||
        // Also try direct class comparison (may fail in sandbox classloader context)
        try {
          methodAnnotation.annotationClass.java == annotationClass
        } catch (_: Throwable) {
          false
        }
    }
  }
}
