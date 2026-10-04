package org.robolectric.junit.jupiter

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

/** Tests that test classes and methods can run in parallel, as JUnit can be configured to. */
class ParallelExecutionTest {
  @Test
  fun `tests that run at the same time have an Android environment each`() {
    val results =
      Fixtures.run(
        IsolatedTests::class,
        MoreIsolatedTests::class,
        SharingTests::class,
        configuration =
          mapOf(
            "junit.jupiter.execution.parallel.enabled" to "true",
            "junit.jupiter.execution.parallel.mode.default" to "concurrent",
            "junit.jupiter.execution.parallel.mode.classes.default" to "concurrent",
            "junit.jupiter.execution.parallel.config.strategy" to "fixed",
            "junit.jupiter.execution.parallel.config.fixed.parallelism" to "3",
          ),
      )

    assertThat(results.failed).isEmpty()
    assertThat(results.succeeded).hasSize(8)
    assertThat(results.events).contains("ran at the same time")
  }

  @Fixture
  @ExtendWith(RobolectricExtension::class)
  @Config(sdk = [34])
  open class IsolatedTests {
    @Test fun first() = useEnvironmentAlone()

    @Test fun second() = useEnvironmentAlone()

    @Test fun third() = useEnvironmentAlone()
  }

  @Fixture class MoreIsolatedTests : IsolatedTests()

  /** Tests that share an environment, so they are in it together, but in no other one. */
  @Fixture
  @ExtendWith(RobolectricExtension::class)
  @Config(sdk = [34])
  class SharingTests {
    @Test
    fun first() {
      assertThat(RuntimeEnvironment.getApplication()).isSameInstanceAs(application)
    }

    @Test
    fun second() {
      assertThat(RuntimeEnvironment.getApplication()).isSameInstanceAs(application)
    }

    companion object {
      private var application: Any? = null

      @JvmStatic
      @BeforeAll
      fun setUpClass() {
        application = RuntimeEnvironment.getApplication()
      }
    }
  }

  companion object {
    private const val IN_USE = "org.robolectric.junit.jupiter.parallel.inUse"

    /**
     * Fails if another test uses the application of this one while it does. The environments are in
     * different sandboxes, which only share the system properties.
     */
    fun useEnvironmentAlone() {
      val application = RuntimeEnvironment.getApplication()
      val id =
        "${System.identityHashCode(application.javaClass.classLoader)}/${application.hashCode()}"
      val properties = System.getProperties()
      synchronized(properties) {
        val inUse = properties.getProperty(IN_USE, "").split(',').filter { it.isNotEmpty() }
        check(id !in inUse) { "The Android environment is used by another test at the same time" }
        if (inUse.isNotEmpty()) {
          Fixtures.record("ran at the same time")
        }
        properties.setProperty(IN_USE, (inUse + id).joinToString(","))
      }
      try {
        Thread.sleep(OVERLAP_MILLIS)
        check(RuntimeEnvironment.getApplication() === application) {
          "The Android environment changed during the test"
        }
      } finally {
        synchronized(properties) {
          val inUse = properties.getProperty(IN_USE, "").split(',').filter { it.isNotEmpty() }
          properties.setProperty(IN_USE, (inUse - id).joinToString(","))
        }
      }
    }

    private const val OVERLAP_MILLIS = 300L
  }
}
