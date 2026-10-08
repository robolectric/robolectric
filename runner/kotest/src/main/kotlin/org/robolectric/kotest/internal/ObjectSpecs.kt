package org.robolectric.kotest.internal

import io.kotest.core.extensions.ApplyExtension
import java.lang.reflect.Modifier
import org.robolectric.kotest.RobolectricExtension

/**
 * Tells about the specs that are objects. Kotest takes the object as the spec, so the extension
 * can't create it in an Android environment, and it runs outside of one.
 */
internal object ObjectSpecs {
  /** Returns whether the class is that of an object, also if the object couldn't be created. */
  fun isObject(type: Class<*>): Boolean =
    type.declaredFields.any {
      it.name == "INSTANCE" && Modifier.isStatic(it.modifiers) && it.type == type
    }

  /**
   * Returns whether the class applies the extension to itself: with an annotation of its own, or of
   * a class that it extends or an interface that it implements, where Kotest looks for it too.
   */
  fun appliesExtension(type: Class<*>): Boolean =
    supertypesOf(type).any { supertype ->
      supertype.getAnnotationsByType(ApplyExtension::class.java).any { annotation ->
        annotation.extensions.any { it == RobolectricExtension::class }
      }
    }

  private fun supertypesOf(type: Class<*>): Sequence<Class<*>> = sequence {
    yield(type)
    (listOfNotNull(type.superclass) + type.interfaces).forEach { yieldAll(supertypesOf(it)) }
  }

  /** Returns what to tell about an object that applies the extension. */
  fun hint(type: Class<*>): String =
    "${type.name} is an object, which Kotest creates itself, outside of the Android environment. " +
      "Make it a class."
}
