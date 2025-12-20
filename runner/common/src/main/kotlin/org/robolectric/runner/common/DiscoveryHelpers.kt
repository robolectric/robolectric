package org.robolectric.runner.common

import java.lang.reflect.Method
import org.junit.platform.commons.support.AnnotationSupport
import org.junit.platform.engine.TestDescriptor
import org.junit.platform.engine.UniqueId
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor
import org.junit.platform.engine.support.descriptor.ClassSource
import org.junit.platform.engine.support.descriptor.MethodSource

/**
 * Helper utilities for JUnit Platform test discovery.
 *
 * This object provides methods to build a hierarchical test descriptor tree from test classes and
 * methods, supporting nested classes.
 */
@ExperimentalRunnerApi
object DiscoveryHelpers {

  // Well-known test annotation class names
  private const val JUNIT_JUPITER_TEST = "org.junit.jupiter.api.Test"
  private const val JUNIT4_TEST = "org.junit.Test"
  private const val KOTEST_TEST = "io.kotest.core.annotation.Test"

  /**
   * Determines if a method is a test method by checking for supported @Test annotations.
   *
   * Supports:
   * - JUnit Jupiter: `@org.junit.jupiter.api.Test`
   * - JUnit 4: `@org.junit.Test`
   * - Kotest: `@io.kotest.core.annotation.Test`
   *
   * @param method The method to check
   * @return true if the method has a recognized test annotation
   */
  @JvmStatic
  fun isTestMethod(method: Method): Boolean {
    return method.annotations.any {
      val name = it.annotationClass.java.name
      name == JUNIT_JUPITER_TEST || name == JUNIT4_TEST || name == KOTEST_TEST
    }
  }

  /**
   * Appends a test class and its test methods to the parent descriptor.
   *
   * This method recursively handles nested classes annotated with @Nested.
   *
   * @param parent The parent test descriptor
   * @param testClass The test class to append
   * @param isTestMethod A predicate to determine if a method is a test method
   * @param nestedAnnotation The annotation class used to identify nested classes (e.g.
   *   org.junit.jupiter.api.Nested)
   */
  fun appendTestClass(
    parent: TestDescriptor,
    testClass: Class<*>,
    isTestMethod: (Method) -> Boolean,
    nestedAnnotation: Class<out Annotation>? = null,
  ) {
    val classId = parent.uniqueId.append("class", testClass.name)
    val classDescriptor =
      parent.children.find { it.uniqueId == classId }
        ?: CommonClassDescriptor(classId, testClass).also { parent.addChild(it) }

    // Add test methods directly to this class descriptor
    testClass.declaredMethods
      .filter { isTestMethod(it) }
      .forEach { method ->
        val methodId = classDescriptor.uniqueId.append("method", method.name)
        if (classDescriptor.children.none { it.uniqueId == methodId }) {
          classDescriptor.addChild(CommonMethodDescriptor(methodId, method))
        }
      }

    // Recursively add nested classes if annotation is provided
    if (nestedAnnotation != null) {
      testClass.declaredClasses
        .filter { AnnotationSupport.isAnnotated(it, nestedAnnotation) }
        .forEach { nestedClass ->
          appendTestClass(classDescriptor, nestedClass, isTestMethod, nestedAnnotation)
        }
    }
  }

  /**
   * Appends a test method to the parent descriptor.
   *
   * @param parent The parent test descriptor
   * @param testClass The class containing the method
   * @param method The test method to append
   */
  fun appendTestMethod(parent: TestDescriptor, testClass: Class<*>, method: Method) {
    val classDescriptor = findOrCreateClassDescriptor(parent, testClass)
    val methodId = classDescriptor.uniqueId.append("method", method.name)
    if (classDescriptor.children.none { it.uniqueId == methodId }) {
      classDescriptor.addChild(CommonMethodDescriptor(methodId, method))
    }
  }

  private fun findOrCreateClassDescriptor(
    parent: TestDescriptor,
    testClass: Class<*>,
  ): TestDescriptor {
    val classId = parent.uniqueId.append("class", testClass.name)

    return parent.children.find { it.uniqueId == classId }
      ?: CommonClassDescriptor(classId, testClass).also { parent.addChild(it) }
  }

  /** Descriptor representing a test class. */
  class CommonClassDescriptor(uniqueId: UniqueId, val testClass: Class<*>) :
    AbstractTestDescriptor(uniqueId, testClass.simpleName, ClassSource.from(testClass)) {
    override fun getType(): TestDescriptor.Type = TestDescriptor.Type.CONTAINER
  }

  /** Descriptor representing a single test method. */
  class CommonMethodDescriptor(uniqueId: UniqueId, val method: Method) :
    AbstractTestDescriptor(uniqueId, method.name, MethodSource.from(method)) {
    override fun getType(): TestDescriptor.Type = TestDescriptor.Type.TEST
  }
}
