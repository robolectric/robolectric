package org.robolectric.junit.jupiter

import com.google.common.truth.Truth.assertThat
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import org.junit.jupiter.api.Timeout
import org.junit.jupiter.api.extension.ExtendWith
import org.robolectric.annotation.Config

/**
 * Tests that JUnit's `@Timeout` interrupts a test that takes too long in the Android environment.
 */
class TimeoutTest {
  @Test
  fun `a test that takes too long is interrupted and fails`() {
    val start = System.nanoTime()

    val results = Fixtures.run(TakingTooLong::class)

    assertThat(TimeUnit.NANOSECONDS.toSeconds(System.nanoTime() - start)).isLessThan(8)
    assertThat(results.failed["sleeps()"]).isInstanceOf(TimeoutException::class.java)
    assertThat(results.events).containsExactly("sleeps", "interrupted", "tearDown").inOrder()
  }

  @Test
  fun `a test after one that timed out in a shared environment is not interrupted`() {
    val results = Fixtures.run(SharingAfterATimeout::class)

    assertThat(results.failed.keys).containsExactly("first()")
    assertThat(results.succeeded).containsExactly("second()")
    assertThat(results.events).contains("second interrupted=false")
  }

  @Fixture
  @ExtendWith(RobolectricExtension::class)
  @Config(sdk = [34])
  class TakingTooLong {
    // Opens the environment before the test starts, so that the time limit is that of the test.
    @BeforeEach fun setUp() = Unit

    @Test
    @Timeout(value = 500, unit = TimeUnit.MILLISECONDS)
    fun sleeps() {
      Fixtures.record("sleeps")
      try {
        Thread.sleep(SLEEP_MILLIS)
        Fixtures.record("woke up")
      } catch (e: InterruptedException) {
        Fixtures.record("interrupted")
        throw e
      }
    }

    @AfterEach
    fun tearDown() {
      Fixtures.record("tearDown")
    }
  }

  @Fixture
  @ExtendWith(RobolectricExtension::class)
  @Config(sdk = [34])
  @TestMethodOrder(MethodOrderer.OrderAnnotation::class)
  class SharingAfterATimeout {
    @Test
    @Order(1)
    @Timeout(value = 500, unit = TimeUnit.MILLISECONDS)
    fun first() {
      // Spins without reacting to the interrupt for a while, so the interrupt stays pending.
      val end = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(SPIN_MILLIS)
      while (System.nanoTime() < end) {
        Thread.onSpinWait()
      }
    }

    @Test
    @Order(2)
    fun second() {
      Fixtures.record("second interrupted=${Thread.currentThread().isInterrupted}")
    }

    companion object {
      @JvmStatic @BeforeAll fun setUpClass() = Unit
    }
  }

  private companion object {
    const val SLEEP_MILLIS = 20_000L
    const val SPIN_MILLIS = 1_500L
  }
}
