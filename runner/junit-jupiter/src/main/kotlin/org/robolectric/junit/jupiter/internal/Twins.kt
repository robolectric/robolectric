package org.robolectric.junit.jupiter.internal

import java.lang.reflect.Constructor
import java.lang.reflect.Field
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.ReflectiveInvocationContext
import org.opentest4j.MultipleFailuresError
import org.opentest4j.TestAbortedException
import org.robolectric.TestLifecycle
import org.robolectric.runner.common.ExperimentalRunnerApi
import org.robolectric.runner.common.RobolectricEnvironment

/**
 * Redirects the calls JUnit makes on a test instance to its twin: an instance of the same class
 * that the Android environment loads, so it can use Android classes.
 */
@OptIn(ExperimentalRunnerApi::class)
internal object Twins {
  /** A twin, wrapped so that JUnit's stores don't close a test instance that is closeable. */
  private class Twin(val instance: Any, own: Map<Field, Any>) {
    /**
     * What the fields of JUnit's instance held when the twin last got it. It starts with what they
     * held from the start, which the twin has its [own] of.
     */
    val mirrored = HashMap(own)
  }

  /** Identifies the twin of a test instance in an environment. */
  private class TwinKey(val original: Any, val environment: RobolectricEnvironment) {
    override fun equals(other: Any?) =
      other is TwinKey && other.original === original && other.environment === environment

    override fun hashCode() =
      31 * System.identityHashCode(original) + System.identityHashCode(environment)
  }

  /**
   * How JUnit constructed a test instance, or would have if the instance weren't a placeholder, and
   * the [references] that its fields held from the start: none in a placeholder.
   */
  private class Construction(
    val constructor: Constructor<*>,
    val arguments: List<Any?>,
    val references: Map<Field, Any>,
  )

  /** Identifies the [Construction] of a test instance. */
  private class ConstructionKey(val instance: Any) {
    override fun equals(other: Any?) = other is ConstructionKey && other.instance === instance

    override fun hashCode() = System.identityHashCode(instance)
  }

  /** Tells which SDK a failure of a test that runs on several SDKs happened on. */
  private class OnAndroidSdk(apiLevel: Int) :
    RuntimeException("On Android SDK $apiLevel", null, false, false)

  /**
   * Remembers how JUnit constructs a test instance, to construct its twins the same way, and what
   * the new instance holds, to tell what is injected into it later.
   */
  fun recordConstruction(
    context: ExtensionContext,
    instance: Any,
    invocation: ReflectiveInvocationContext<out Constructor<*>>,
  ) {
    context
      .getStore(Plans.NAMESPACE)
      .put(
        ConstructionKey(instance),
        Construction(
          invocation.executable,
          invocation.arguments,
          Placeholders.referencesOf(instance),
        ),
      )
  }

  /**
   * Invokes the method that JUnit is about to invoke on the twin of its target instead, in each of
   * the environments, and rethrows what it throws. Returns what [result] makes of the value the
   * method returns in each environment, inside of that environment.
   *
   * An environment in which an assumption fails is left out for the rest of the test, unless
   * [evenIfAborted], and the assumption only aborts the test once it failed in all of them.
   */
  fun <R> invoke(
    context: ExtensionContext,
    target: TestEnvironments,
    invocation: ReflectiveInvocationContext<Method>,
    evenIfAborted: Boolean = false,
    result: (RobolectricEnvironment, Any?) -> R,
  ): Map<RobolectricEnvironment, R> {
    val results = LinkedHashMap<RobolectricEnvironment, R>()
    val failures = mutableListOf<Throwable>()
    val failedSdks = mutableListOf<Int>()
    var abort: TestAbortedException? = null
    for (environment in target.environments) {
      if (environment in target.aborted && !evenIfAborted) {
        continue
      }
      try {
        results[environment] = invokeIn(context, target, environment, invocation, result)
      } catch (e: TestAbortedException) {
        target.aborted.add(environment)
        abort = e
      } catch (@Suppress("TooGenericExceptionCaught") e: Throwable) {
        diagnose(environment, e)
        if (target.environments.size > 1) {
          e.addSuppressed(OnAndroidSdk(environment.configuration.apiLevel))
        }
        failures.add(e)
        failedSdks.add(environment.configuration.apiLevel)
      }
    }
    // An assumption of a class, rather than of a test, that fails anywhere aborts the class.
    val abortsAll = target.aborted.size == target.environments.size || !context.testMethod.isPresent
    val outcome =
      when {
        failures.size == 1 -> failures.single()
        failures.size > 1 ->
          MultipleFailuresError("Failed on the Android SDKs $failedSdks", failures)
        abortsAll -> abort
        else -> null
      }
    outcome?.let { throw it }
    return results
  }

  /** Adds what the environment knows about a failure of a test to it, as the test runner does. */
  fun diagnose(environment: RobolectricEnvironment, failure: Throwable) {
    try {
      environment.diagnoseFailure(failure)
    } catch (@Suppress("TooGenericExceptionCaught") e: Throwable) {
      failure.addSuppressed(e)
    }
  }

  private fun <R> invokeIn(
    context: ExtensionContext,
    target: TestEnvironments,
    environment: RobolectricEnvironment,
    invocation: ReflectiveInvocationContext<Method>,
    result: (RobolectricEnvironment, Any?) -> R,
  ): R {
    val method = invocation.executable
    return environment.run {
      startTest(context, target, environment)
      // Every instance of the test, such as the enclosing one of a nested test, gets what JUnit
      // injected since the last call.
      context.testInstances.ifPresent { instances ->
        instances.allInstances.forEach { twinOf(context, target, environment, it) }
      }
      val twinMethod = twinOf(environment, method)
      val twin =
        if (Modifier.isStatic(method.modifiers)) {
          null
        } else {
          twinOf(context, target, environment, invocation.target.get())
        }
      val arguments =
        Array(method.parameterCount) { index ->
          TwinArguments.toTwin(
            environment,
            twinMethod.parameters[index],
            method.parameterTypes[index],
            invocation.arguments[index],
          )
        }
      try {
        result(environment, twinMethod.invoke(twin, *arguments))
      } catch (e: InvocationTargetException) {
        throw e.targetException
      }
    }
  }

  /**
   * Tells the application that a test starts, the first time something of the test runs in the
   * environment, and that it is over once the test is, as the test runner does for an application
   * that implements TestLifecycleApplication.
   */
  private fun startTest(
    context: ExtensionContext,
    target: TestEnvironments,
    environment: RobolectricEnvironment,
  ) {
    val testMethod = context.testMethod.orElse(null)
    if (testMethod == null || target.hasStartedIn(environment)) {
      return
    }
    val lifecycle =
      environment.classLoader
        .loadClass("org.robolectric.DefaultTestLifecycle")
        .getDeclaredConstructor()
        .newInstance() as TestLifecycle
    val twinTestMethod = twinOf(environment, testMethod)
    target.onTestEnd(environment) { lifecycle.afterTest(twinTestMethod) }
    lifecycle.beforeTest(twinTestMethod)
    context.testInstance.ifPresent {
      lifecycle.prepareTest(twinOf(context, target, environment, it))
    }
  }

  private fun twinOf(environment: RobolectricEnvironment, method: Method): Method {
    val parameterTypes = method.parameterTypes.map { environment.loadClass(it) }.toTypedArray()
    return environment
      .loadClass(method.declaringClass)
      .getDeclaredMethod(method.name, *parameterTypes)
      .apply { isAccessible = true }
  }

  /**
   * Returns the twin of the test instance in the environment, creating it the first time. It lives
   * as long as the instance does there: for the test, or for the class if the tests share it.
   */
  private fun twinOf(
    context: ExtensionContext,
    target: TestEnvironments,
    environment: RobolectricEnvironment,
    original: Any,
  ): Any {
    val owner = if (target.shared) outermostContextWith(context, original) else context
    val twin =
      owner
        .getStore(Plans.NAMESPACE)
        .computeIfAbsent(
          TwinKey(original, environment),
          { createTwin(context, target, environment, original) },
          Twin::class.java,
        )
    mirrorInjectedFields(environment, original, twin)
    return twin.instance
  }

  /**
   * Copies what JUnit or an extension injected into the fields of its instance, such as a
   * `@TempDir`, into the twin: what the fields didn't hold when the instance was created.
   */
  private fun mirrorInjectedFields(environment: RobolectricEnvironment, original: Any, twin: Twin) {
    val injected =
      Placeholders.referencesOf(original).filter { (field, value) ->
        twin.mirrored[field] !== value
      }
    for ((field, value) in injected) {
      val twinField =
        environment.loadClass(field.declaringClass).getDeclaredField(field.name).apply {
          isAccessible = true
        }
      val description = { "the value that was injected into the field ${field.name} of the test" }
      try {
        twinField.set(twin.instance, TwinArguments.toTwin(environment, value, description))
      } catch (e: IllegalArgumentException) {
        throw IllegalStateException(
          "Can't pass ${description()} to the Android environment, which loads the type of " +
            "the field, ${field.type.name}, again, so the ${value.javaClass.name} doesn't fit",
          e,
        )
      }
      twin.mirrored[field] = value
    }
  }

  /** Creates the twin the way JUnit created the test instance, with the twins of its arguments. */
  private fun createTwin(
    context: ExtensionContext,
    target: TestEnvironments,
    environment: RobolectricEnvironment,
    original: Any,
  ): Twin {
    val instances = context.testInstances.map { it.allInstances }.orElse(emptyList())
    val construction =
      context.getStore(Plans.NAMESPACE).get(ConstructionKey(original), Construction::class.java)
    // If JUnit didn't construct the instance itself, the instance of an inner class, such as a
    // @Nested one, is taken to be created in its enclosing instance, and any other without
    // arguments.
    val enclosing = instances.getOrNull(instances.indexOfFirst { it === original } - 1)
    val parameterTypes =
      construction?.constructor?.parameterTypes
        ?: listOfNotNull(enclosing?.javaClass).toTypedArray()
    val arguments = construction?.arguments ?: listOfNotNull(enclosing)
    try {
      val twinConstructor =
        environment
          .loadClass(original.javaClass)
          .getDeclaredConstructor(*parameterTypes.map { environment.loadClass(it) }.toTypedArray())
          .apply { isAccessible = true }
      val twinArguments =
        Array(arguments.size) { index ->
          val argument = arguments[index]
          if (argument != null && instances.any { it === argument }) {
            twinOf(context, target, environment, argument)
          } else {
            val twinParameter = twinConstructor.parameters[index]
            TwinArguments.toTwin(environment, twinParameter, parameterTypes[index], argument)
          }
        }
      val twin = twinConstructor.newInstance(*twinArguments)
      // What an instance that JUnit didn't construct here holds by now is taken to be its own.
      return Twin(twin, construction?.references ?: Placeholders.referencesOf(original))
    } catch (e: NoSuchMethodException) {
      throw IllegalStateException(
        "${original.javaClass.name} can't be created in the Android environment: it needs a " +
          "constructor that JUnit calls, or one without parameters",
        e,
      )
    } catch (e: InvocationTargetException) {
      throw e.targetException
    }
  }

  /** Returns the outermost context that the test instance belongs to, starting from this one. */
  private fun outermostContextWith(context: ExtensionContext, instance: Any): ExtensionContext {
    var owner = context
    var parent = context.parent.orElse(null)
    while (parent != null && parent.hasInstance(instance)) {
      owner = parent
      parent = parent.parent.orElse(null)
    }
    return owner
  }

  private fun ExtensionContext.hasInstance(instance: Any): Boolean =
    testInstances.map { instances -> instances.allInstances.any { it === instance } }.orElse(false)
}
