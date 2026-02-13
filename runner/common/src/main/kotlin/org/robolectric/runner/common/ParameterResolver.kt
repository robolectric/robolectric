package org.robolectric.runner.common

import java.lang.reflect.Parameter
import java.lang.reflect.ParameterizedType
import org.robolectric.internal.AndroidSandbox

/** Interface for resolving test method parameters. */
@ExperimentalRunnerApi
interface ParameterResolver {
  /**
   * Resolves a parameter for a test method execution.
   *
   * @param parameter The parameter to resolve
   * @param sandbox The AndroidSandbox where the test is running
   * @return The resolved argument value, or null if not resolved
   */
  fun resolveParameter(parameter: Parameter, sandbox: AndroidSandbox): Any?
}

/**
 * Default parameter resolver for Robolectric tests.
 *
 * Supports injection of:
 * - android.content.Context
 * - android.app.Application
 * - org.robolectric.android.controller.ActivityController
 * - org.robolectric.android.controller.ServiceController
 */
@ExperimentalRunnerApi
object DefaultRobolectricParameterResolver : ParameterResolver {

  override fun resolveParameter(parameter: Parameter, sandbox: AndroidSandbox): Any? {
    val parameterType = parameter.type

    return when (parameterType.name) {
      "android.content.Context",
      "android.app.Application" -> {
        // Load RuntimeEnvironment through sandbox classloader
        val runtimeEnvClass =
          sandbox.robolectricClassLoader.loadClass("org.robolectric.RuntimeEnvironment")
        val getAppMethod = runtimeEnvClass.getMethod("getApplication")
        getAppMethod.invoke(null)
      }
      "org.robolectric.android.controller.ActivityController" -> {
        resolveController(parameter, sandbox, "buildActivity")
      }
      "org.robolectric.android.controller.ServiceController" -> {
        resolveController(parameter, sandbox, "buildService")
      }
      else -> null
    }
  }

  private fun resolveController(
    parameter: Parameter,
    sandbox: AndroidSandbox,
    buildMethodName: String,
  ): Any {
    // Get the generic type (the Activity/Service class)
    val componentClass = getGenericType(parameter)

    // Load Robolectric class through sandbox classloader
    val robolectricClass = sandbox.robolectricClassLoader.loadClass("org.robolectric.Robolectric")
    val buildMethod = robolectricClass.getMethod(buildMethodName, Class::class.java)

    // Bootstrap the component class - this ensures shadows are applied
    val bootstrappedComponentClass = sandbox.bootstrappedClass<Any>(componentClass)

    // Build the controller
    return buildMethod.invoke(null, bootstrappedComponentClass)
  }

  private fun getGenericType(parameter: Parameter): Class<*> {
    val type = parameter.parameterizedType
    if (type is ParameterizedType) {
      val arg = type.actualTypeArguments[0]
      if (arg is Class<*>) {
        return arg
      }
    }
    throw IllegalArgumentException(
      "Failed to resolve generic type for parameter: ${parameter.name}"
    )
  }
}

@ExperimentalRunnerApi
object ParameterResolutionHelper {
  /** Resolves an array of parameters using the given [ParameterResolver]. */
  fun resolveParameters(parameters: Array<Parameter>, sandbox: AndroidSandbox): Array<Any?> {
    return parameters
      .map { param -> DefaultRobolectricParameterResolver.resolveParameter(param, sandbox) }
      .toTypedArray()
  }
}
