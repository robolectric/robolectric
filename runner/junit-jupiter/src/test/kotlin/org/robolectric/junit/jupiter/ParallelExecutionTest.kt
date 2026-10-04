package org.robolectric.junit.jupiter

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

/** Tests that test classes and methods can run in parallel, as JUnit can be configured to. */
class ParallelExecutionTest {
  @Test
  fun `parallel Jupiter settings still execute shared sandboxes sequentially`() {
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
    assertThat(results.events).doesNotContain("ran at the same time")
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
  @TestInstance(TestInstance.Lifecycle.PER_CLASS)
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
     * Records if another test runs while this one does, counting them in the system properties that
     * all sandboxes share.
     */
    fun useEnvironmentAlone() {
      val application = RuntimeEnvironment.getApplication()
      val properties = System.getProperties()
      synchronized(properties) {
        val inUse = properties.getProperty(IN_USE, "0").toInt()
        if (inUse > 0) Fixtures.record("ran at the same time")
        properties.setProperty(IN_USE, "${inUse + 1}")
      }
      try {
        Thread.sleep(OVERLAP_MILLIS)
        check(RuntimeEnvironment.getApplication() === application) {
          "The Android environment changed during the test"
        }
      } finally {
        synchronized(properties) {
          properties.setProperty(IN_USE, "${properties.getProperty(IN_USE).toInt() - 1}")
        }
      }
    }

    private const val OVERLAP_MILLIS = 300L
  }
}
