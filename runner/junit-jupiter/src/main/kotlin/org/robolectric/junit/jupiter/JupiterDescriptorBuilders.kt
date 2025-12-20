package org.robolectric.junit.jupiter

import java.lang.reflect.Method
import org.junit.jupiter.api.Nested
import org.junit.platform.commons.support.AnnotationSupport
import org.junit.platform.engine.TestDescriptor
import org.junit.platform.engine.UniqueId
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor
import org.junit.platform.engine.support.descriptor.ClassSource
import org.junit.platform.engine.support.descriptor.MethodSource

/**
 * JUnit Platform descriptor builders for JUnit Jupiter tests.
 *
 * This object provides methods to build JUnit Platform test descriptor trees specifically for JUnit
 * Jupiter tests, including support for `@Nested` test classes.
 */
internal object JupiterDescriptorBuilders {

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
    nestedAnnotation: Class<out Annotation>? = Nested::class.java,
  ) {
    val classId = parent.uniqueId.append("class", testClass.name)
    val classDescriptor =
      parent.children.find { it.uniqueId == classId }
        ?: JupiterClassDescriptor(classId, testClass).also { parent.addChild(it) }

    // Add test methods directly to this class descriptor
    testClass.declaredMethods
      .filter { isTestMethod(it) }
      .forEach { method ->
        val methodId = classDescriptor.uniqueId.append("method", method.name)
        if (classDescriptor.children.none { it.uniqueId == methodId }) {
          classDescriptor.addChild(JupiterMethodDescriptor(methodId, method))
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
      classDescriptor.addChild(JupiterMethodDescriptor(methodId, method))
    }
  }

  private fun findOrCreateClassDescriptor(
    parent: TestDescriptor,
    testClass: Class<*>,
  ): TestDescriptor {
    val classId = parent.uniqueId.append("class", testClass.name)

    return parent.children.find { it.uniqueId == classId }
      ?: JupiterClassDescriptor(classId, testClass).also { parent.addChild(it) }
  }

  /** Descriptor representing a test class. */
  class JupiterClassDescriptor(uniqueId: UniqueId, val testClass: Class<*>) :
    AbstractTestDescriptor(uniqueId, testClass.simpleName, ClassSource.from(testClass)) {
    override fun getType(): TestDescriptor.Type = TestDescriptor.Type.CONTAINER
  }

  /** Descriptor representing a single test method. */
  class JupiterMethodDescriptor(uniqueId: UniqueId, val method: Method) :
    AbstractTestDescriptor(uniqueId, method.name, MethodSource.from(method)) {
    override fun getType(): TestDescriptor.Type = TestDescriptor.Type.TEST
  }
}
