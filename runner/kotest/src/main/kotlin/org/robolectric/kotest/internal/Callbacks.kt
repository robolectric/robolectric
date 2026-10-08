package org.robolectric.kotest.internal

import io.kotest.core.extensions.Extension
import io.kotest.core.spec.Spec
import io.kotest.core.test.TestCase
import java.lang.reflect.InvocationHandler
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import java.lang.reflect.Proxy

/**
 * Passes what Kotest tells the callbacks of a [StandIn] on to the callbacks of the instances in the
 * sandbox. Kotest tells them about the stand-in and its tests, but a callback of an instance is to
 * be told about that instance and the tests that come from it, and as tests of it: the callbacks
 * that a spec registers in its body only run for tests of a spec of their own class.
 */
internal object Callbacks {
  private val FOR_SPEC = setOf("beforeSpec", "afterSpec")
  private const val INTERCEPT = "intercept"

  /** Returns the callback for Kotest to call in place of the callback of the member. */
  fun of(callback: Extension, member: Member, standIn: StandIn): Extension {
    val interfaces =
      generateSequence<Class<*>>(callback.javaClass) { it.superclass }
        .flatMap { it.interfaces.asSequence() }
        .flatMap { withSuperinterfaces(it) }
        .filter { Extension::class.java.isAssignableFrom(it) && Modifier.isPublic(it.modifiers) }
        .distinct()
        .toList()
    if (interfaces.isEmpty()) {
      return callback
    }
    val loader = callback.javaClass.classLoader ?: Extension::class.java.classLoader
    val handler = InvocationHandler { proxy, method, arguments ->
      forward(proxy, method, arguments ?: emptyArray(), callback, member, standIn)
    }
    return Proxy.newProxyInstance(loader, interfaces.toTypedArray(), handler) as Extension
  }

  private fun withSuperinterfaces(type: Class<*>): Sequence<Class<*>> =
    sequenceOf(type) + type.interfaces.asSequence().flatMap { withSuperinterfaces(it) }

  @Suppress("LongParameterList")
  private fun forward(
    proxy: Any,
    method: Method,
    arguments: Array<Any?>,
    callback: Extension,
    member: Member,
    standIn: StandIn,
  ): Any? {
    val testCase = arguments.firstOrNull() as? TestCase
    return when {
      method.declaringClass == Any::class.java -> ofObject(proxy, method, arguments)
      // An instance is told about these at its first and after its last test, see Member.
      method.name in FOR_SPEC && arguments.firstOrNull() is Spec -> Unit
      testCase != null && standIn.memberOf(testCase) !== member -> skip(method, arguments)
      else -> {
        // A callback that runs a test gets a function to run it with, after the test.
        val runsTest = method.name == INTERCEPT && testCase != null
        val forMember = arguments.mapIndexed { index, argument ->
          if (runsTest && index == 1) toKotest(argument, standIn)
          else toMember(argument, member, standIn)
        }
        call(method, callback, forMember)
      }
    }
  }

  /** Returns the argument as the callback of the member is to get it. */
  private fun toMember(argument: Any?, member: Member, standIn: StandIn): Any? =
    when {
      argument === standIn.spec -> member.instance.spec
      argument is TestCase -> argument.copy(spec = member.instance.spec)
      else -> argument
    }

  /**
   * Returns the function that a callback runs a test with, so that the test that the callback
   * passes on, a test of the member, is a test of the stand-in again for Kotest.
   */
  private fun toKotest(execute: Any?, standIn: StandIn): Any =
    fun(passedOn: Any?, continuation: Any?): Any? {
      val forKotest = if (passedOn is TestCase) passedOn.copy(spec = standIn.spec) else passedOn
      @Suppress("UNCHECKED_CAST")
      return (execute as Function2<Any?, Any?, Any?>).invoke(forKotest, continuation)
    }

  /** Leaves out a callback about a test of another member: a callback that runs a test runs it. */
  private fun skip(method: Method, arguments: Array<Any?>): Any? {
    val execute = arguments.getOrNull(1)
    return if (method.name == INTERCEPT && execute is Function2<*, *, *>) {
      @Suppress("UNCHECKED_CAST")
      (execute as Function2<Any?, Any?, Any?>).invoke(arguments[0], arguments[2])
    } else {
      Unit
    }
  }

  private fun call(method: Method, callback: Extension, arguments: List<Any?>): Any? =
    try {
      method.invoke(callback, *arguments.toTypedArray())
    } catch (e: InvocationTargetException) {
      throw e.targetException
    }

  private fun ofObject(proxy: Any, method: Method, arguments: Array<Any?>): Any =
    when (method.name) {
      "equals" -> proxy === arguments[0]
      "hashCode" -> System.identityHashCode(proxy)
      else -> "Callbacks@" + Integer.toHexString(System.identityHashCode(proxy))
    }
}
