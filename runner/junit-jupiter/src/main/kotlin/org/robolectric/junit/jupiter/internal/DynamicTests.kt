package org.robolectric.junit.jupiter.internal

import java.util.Spliterator
import java.util.Spliterators
import java.util.stream.Stream
import java.util.stream.StreamSupport
import org.junit.jupiter.api.DynamicContainer
import org.junit.jupiter.api.DynamicNode
import org.junit.jupiter.api.DynamicTest
import org.junit.platform.commons.JUnitException
import org.robolectric.runner.common.ExperimentalRunnerApi
import org.robolectric.runner.common.RobolectricEnvironment

/**
 * Makes the dynamic tests that a `@TestFactory` method creates in an Android environment be created
 * and run in that environment, one after the other, as JUnit asks for them.
 */
@OptIn(ExperimentalRunnerApi::class)
internal object DynamicTests {
  /** Returns the dynamic tests of a factory method's result, bound to the environment. */
  fun inEnvironment(environment: RobolectricEnvironment, result: Any?): Stream<DynamicNode> {
    val nodes = toIterator(result)
    val bound =
      object : Iterator<DynamicNode> {
        // A test factory can use Android to create its tests.
        override fun hasNext(): Boolean = environment.run { nodes.hasNext() }

        override fun next(): DynamicNode = environment.run {
          if (!nodes.hasNext()) {
            throw NoSuchElementException()
          }
          inEnvironment(environment, nodes.next() as DynamicNode)
        }
      }
    return StreamSupport.stream(
        Spliterators.spliteratorUnknownSize(bound, Spliterator.ORDERED),
        false,
      )
      .onClose { (result as? Stream<*>)?.let { environment.run { it.close() } } }
  }

  /** Returns the tests of each environment, grouped by its SDK if there are several. */
  fun grouped(tests: Map<RobolectricEnvironment, Stream<DynamicNode>>): Stream<DynamicNode> =
    tests.values.singleOrNull()
      ?: tests.entries.stream().map { (environment, stream) ->
        DynamicContainer.dynamicContainer("SDK ${environment.configuration.apiLevel}", stream)
      }

  private fun inEnvironment(environment: RobolectricEnvironment, node: DynamicNode): DynamicNode =
    when (node) {
      is DynamicTest ->
        DynamicTest.dynamicTest(node.displayName, node.testSourceUri.orElse(null)) {
          try {
            environment.run { node.executable.execute() }
          } catch (@Suppress("TooGenericExceptionCaught") e: Throwable) {
            Twins.diagnose(environment, e)
            throw e
          }
        }
      is DynamicContainer ->
        DynamicContainer.dynamicContainer(
          node.displayName,
          node.testSourceUri.orElse(null),
          inEnvironment(environment, node.children),
        )
      else -> node
    }

  private fun toIterator(result: Any?): Iterator<*> =
    when (result) {
      is DynamicNode -> listOf(result).iterator()
      is Stream<*> -> result.iterator()
      is Iterable<*> -> result.iterator()
      is Iterator<*> -> result
      is Array<*> -> result.iterator()
      else ->
        throw JUnitException(
          "A @TestFactory method has to return a DynamicNode, or a Stream, Collection, " +
            "Iterable, Iterator or array of them"
        )
    }
}
