package org.robolectric.junit.jupiter.internal

import java.lang.reflect.Parameter
import java.lang.reflect.ParameterizedType
import org.robolectric.runner.common.ExperimentalRunnerApi
import org.robolectric.runner.common.RobolectricEnvironment

/** The parameters of test and lifecycle methods that the Android environment provides. */
@OptIn(ExperimentalRunnerApi::class)
internal object AndroidParameters {
  private const val CONTEXT = "android.content.Context"
  private const val APPLICATION = "android.app.Application"
  private const val ACTIVITY_CONTROLLER = "org.robolectric.android.controller.ActivityController"
  private const val SERVICE_CONTROLLER = "org.robolectric.android.controller.ServiceController"

  /** Returns whether the environment provides arguments of the type. */
  fun supports(type: Class<*>): Boolean =
    type.name in setOf(CONTEXT, APPLICATION, ACTIVITY_CONTROLLER, SERVICE_CONTROLLER)

  /**
   * Returns the argument for a parameter of a method that the environment loaded, or null if it
   * doesn't provide one. It has to be called inside of the environment.
   */
  fun resolve(environment: RobolectricEnvironment, parameter: Parameter): Any? =
    when (parameter.type.name) {
      CONTEXT,
      APPLICATION ->
        environment.classLoader
          .loadClass("org.robolectric.RuntimeEnvironment")
          .getMethod("getApplication")
          .invoke(null)
      ACTIVITY_CONTROLLER -> buildController(environment, parameter, "buildActivity")
      SERVICE_CONTROLLER -> buildController(environment, parameter, "buildService")
      else -> null
    }

  private fun buildController(
    environment: RobolectricEnvironment,
    parameter: Parameter,
    factoryMethod: String,
  ): Any {
    val component =
      (parameter.parameterizedType as? ParameterizedType)?.actualTypeArguments?.firstOrNull()
        as? Class<*>
        ?: error(
          "The parameter ${parameter.name} of ${parameter.declaringExecutable} has to name its " +
            "component, as ActivityController<MyActivity> does"
        )
    return environment.classLoader
      .loadClass("org.robolectric.Robolectric")
      .getMethod(factoryMethod, Class::class.java)
      .invoke(null, component)
  }
}
