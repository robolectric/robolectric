package org.robolectric.junit.jupiter.internal

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.ObjectStreamClass
import java.lang.reflect.Parameter
import org.robolectric.runner.common.ExperimentalRunnerApi
import org.robolectric.runner.common.RobolectricEnvironment

/**
 * Turns the arguments that JUnit resolved for a method or a constructor into those of its twin in
 * the Android environment, and what JUnit injects into a test instance into what its twin gets. An
 * object is passed as it is if the environment uses the same classes for it, as it does for those
 * of the JDK and of JUnit. Otherwise it is recreated there: an enum constant by its name, and a
 * serializable object by serializing it.
 */
@OptIn(ExperimentalRunnerApi::class)
internal object TwinArguments {
  /**
   * Returns the argument for a parameter of the twin.
   *
   * @param originalType the type of the parameter that JUnit resolved the argument for
   */
  fun toTwin(
    environment: RobolectricEnvironment,
    twinParameter: Parameter,
    originalType: Class<*>,
    argument: Any?,
  ): Any? =
    when {
      // JUnit got a placeholder for what the environment provides.
      AndroidParameters.supports(originalType) ->
        AndroidParameters.resolve(environment, twinParameter)
      argument == null -> null
      else ->
        toTwin(environment, argument) {
          "the argument for ${twinParameter.name} of ${twinParameter.declaringExecutable}"
        }
    }

  /**
   * Returns the object as the environment uses it.
   *
   * @param describe names the object in the error if it can't be recreated in the environment
   */
  fun toTwin(environment: RobolectricEnvironment, value: Any, describe: () -> String): Any =
    when {
      value is Enum<*> -> toTwinEnum(environment, value)
      mayHoldReloadedClasses(environment, value) -> copyIfNeeded(environment, value, describe)
      else -> value
    }

  private fun toTwinEnum(environment: RobolectricEnvironment, constant: Enum<*>): Any {
    val twinEnum = environment.loadClass(constant.declaringJavaClass)
    return twinEnum.enumConstants.first { (it as Enum<*>).name == constant.name }
  }

  /** Returns whether the object, or what it holds, can be of a class the sandbox loads again. */
  private fun mayHoldReloadedClasses(environment: RobolectricEnvironment, value: Any): Boolean =
    isReloaded(environment, value.javaClass) ||
      value is Iterable<*> ||
      value is Map<*, *> ||
      value.javaClass.isArray && !value.javaClass.componentType.isPrimitive

  private fun isReloaded(environment: RobolectricEnvironment, type: Class<*>): Boolean =
    try {
      environment.loadClass(type) !== type
    } catch (_: ClassNotFoundException) {
      // A class without a name to load it by, such as that of a lambda, is used as it is.
      false
    }

  /**
   * Returns the object as the environment loads it if the sandbox loads a class of it again, by
   * serializing it and reading it with the classes of the environment, or the object itself.
   */
  private fun copyIfNeeded(
    environment: RobolectricEnvironment,
    argument: Any,
    describe: () -> String,
  ): Any {
    val bytes = ByteArrayOutputStream()
    var holdsReloadedClass = false
    try {
      object : ObjectOutputStream(bytes) {
          override fun annotateClass(type: Class<*>) {
            holdsReloadedClass = holdsReloadedClass || isReloaded(environment, type)
          }
        }
        .use { it.writeObject(argument) }
    } catch (e: IOException) {
      check(!isReloaded(environment, argument.javaClass)) {
        "Can't pass ${describe()} to the Android environment, which loads " +
          "${argument.javaClass.name} again: it has to be serializable, an enum constant, or of " +
          "a class of the JDK ($e)"
      }
      // What can't be serialized, such as a lambda, is used as it is.
      holdsReloadedClass = false
    }
    return if (holdsReloadedClass) read(environment, bytes.toByteArray()) else argument
  }

  /** Reads a serialized object with the classes of the environment. */
  private fun read(environment: RobolectricEnvironment, bytes: ByteArray): Any {
    val input =
      object : ObjectInputStream(ByteArrayInputStream(bytes)) {
        override fun resolveClass(description: ObjectStreamClass): Class<*> =
          try {
            Class.forName(description.name, false, environment.classLoader)
          } catch (_: ClassNotFoundException) {
            super.resolveClass(description)
          }
      }
    return input.use { it.readObject() }
  }
}
