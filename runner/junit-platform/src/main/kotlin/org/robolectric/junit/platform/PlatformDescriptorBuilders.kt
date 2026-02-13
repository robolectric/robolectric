package org.robolectric.junit.platform

import java.lang.reflect.Method
import org.junit.platform.engine.TestDescriptor
import org.junit.platform.engine.UniqueId
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor
import org.junit.platform.engine.support.descriptor.ClassSource
import org.junit.platform.engine.support.descriptor.MethodSource

/**
 * JUnit Platform descriptor builders for the Robolectric test engine.
 *
 * This object provides methods to build JUnit Platform test descriptor trees for JUnit 4-style
 * tests running on the Robolectric platform engine. Unlike Jupiter, this does not support nested
 * test classes.
 */
internal object PlatformDescriptorBuilders {

  /**
   * Appends a test class and its test methods to the parent descriptor.
   *
   * @param parent The parent test descriptor
   * @param testClass The test class to append
   * @param isTestMethod A predicate to determine if a method is a test method
   */
  fun appendTestClass(
    parent: TestDescriptor,
    testClass: Class<*>,
    isTestMethod: (Method) -> Boolean,
  ) {
    val classId = parent.uniqueId.append("class", testClass.name)
    val classDescriptor =
      parent.children.find { it.uniqueId == classId }
        ?: PlatformClassDescriptor(classId, testClass).also { parent.addChild(it) }

    // Add test methods directly to this class descriptor
    testClass.declaredMethods
      .filter { isTestMethod(it) }
      .forEach { method ->
        val methodId = classDescriptor.uniqueId.append("method", method.name)
        if (classDescriptor.children.none { it.uniqueId == methodId }) {
          classDescriptor.addChild(PlatformMethodDescriptor(methodId, method))
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
      classDescriptor.addChild(PlatformMethodDescriptor(methodId, method))
    }
  }

  private fun findOrCreateClassDescriptor(
    parent: TestDescriptor,
    testClass: Class<*>,
  ): TestDescriptor {
    val classId = parent.uniqueId.append("class", testClass.name)

    return parent.children.find { it.uniqueId == classId }
      ?: PlatformClassDescriptor(classId, testClass).also { parent.addChild(it) }
  }

  /** Descriptor representing a test class. */
  class PlatformClassDescriptor(uniqueId: UniqueId, val testClass: Class<*>) :
    AbstractTestDescriptor(uniqueId, testClass.simpleName, ClassSource.from(testClass)) {
    override fun getType(): TestDescriptor.Type = TestDescriptor.Type.CONTAINER
  }

  /** Descriptor representing a single test method. */
  class PlatformMethodDescriptor(uniqueId: UniqueId, val method: Method) :
    AbstractTestDescriptor(uniqueId, method.name, MethodSource.from(method)) {
    override fun getType(): TestDescriptor.Type = TestDescriptor.Type.TEST
  }
}
