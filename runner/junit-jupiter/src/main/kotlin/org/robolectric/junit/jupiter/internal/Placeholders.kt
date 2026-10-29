package org.robolectric.junit.jupiter.internal

import java.lang.reflect.Field
import java.lang.reflect.Modifier
import org.junit.jupiter.api.extension.RegisterExtension
import org.junit.platform.commons.support.AnnotationSupport
import org.junit.platform.commons.support.HierarchyTraversalMode
import sun.misc.Unsafe

/**
 * Creates the test instances that JUnit holds. JUnit needs an instance of the test class to call
 * methods on and to inject into, but the instance that runs the test is its twin in the Android
 * environment. JUnit's instance is therefore a placeholder that is created without running its
 * constructor and field initializers, which may use Android.
 */
internal object Placeholders {
  private val unsafe: Unsafe by lazy {
    Unsafe::class.java.getDeclaredField("theUnsafe").apply { isAccessible = true }.get(null)
      as Unsafe
  }

  /**
   * Returns whether JUnit's instance of the class has to be constructed after all, because JUnit
   * reads what its constructor creates: the extensions that it registers from instance fields.
   */
  fun needsConstruction(testClass: Class<*>): Boolean =
    AnnotationSupport.findAnnotatedFields(
        testClass,
        RegisterExtension::class.java,
        { !Modifier.isStatic(it.modifiers) },
        HierarchyTraversalMode.TOP_DOWN,
      )
      .isNotEmpty()

  /** Returns an instance of the class whose constructor and field initializers didn't run. */
  fun <T : Any> allocate(testClass: Class<T>): T =
    try {
      testClass.cast(unsafe.allocateInstance(testClass))
    } catch (e: LinkageError) {
      // An instance needs its class to be initialized, here as JUnit loads it.
      throw IllegalStateException(
        "${testClass.name} couldn't be initialized outside of the Android environment, where " +
          "JUnit needs an instance of it. Its static initializers, such as those of a " +
          "companion object, can't use Android: use a @BeforeAll method instead",
        e,
      )
    }

  /**
   * Returns the objects that the instance fields of a test instance refer to: none in a new
   * placeholder, so that what it refers to later was injected into it.
   */
  fun referencesOf(instance: Any): Map<Field, Any> {
    val references = HashMap<Field, Any>()
    generateSequence<Class<*>>(instance.javaClass) { it.superclass }
      .flatMap { it.declaredFields.asSequence() }
      .filter { !Modifier.isStatic(it.modifiers) && !it.type.isPrimitive && !it.isSynthetic }
      .forEach { field ->
        field.isAccessible = true
        field.get(instance)?.let { references[field] = it }
      }
    return references
  }
}
