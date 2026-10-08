package org.robolectric.junit.jupiter

import android.os.Build
import android.os.Handler
import android.os.Looper
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.opentest4j.TestAbortedException
import org.robolectric.annotation.Config

/** Tests what [RobolectricExtension] reports for tests that don't pass. */
class RobolectricExtensionFailureTest {
  @Test
  fun `a failing AfterEach method fails the test`() {
    val results = Fixtures.run(FailingAfterEach::class)

    assertThat(results.succeeded).isEmpty()
    assertThat(results.failed["passes()"]).hasMessageThat().isEqualTo("from tearDown")
    assertThat(results.events).containsExactly("test").inOrder()
  }

  @Test
  fun `AfterEach methods run after a failing BeforeEach method`() {
    val results = Fixtures.run(FailingBeforeEach::class)

    assertThat(results.failed["neverRuns()"]).hasMessageThat().isEqualTo("from setUp")
    assertThat(results.events).containsExactly("tearDown")
  }

  @Test
  fun `a failed assumption aborts the test`() {
    val results = Fixtures.run(FailedAssumption::class)

    assertThat(results.aborted).containsExactly("assumesTheImpossible()", "abortsItself()")
    assertThat(results.failed).isEmpty()
  }

  @Test
  fun `a test whose SDK is not enabled is skipped`() {
    val results =
      Fixtures.run(Sdk34Only::class, properties = mapOf("robolectric.enabledSdks" to "33"))

    assertThat(results.skipped["onSdk34()"])
      .isEqualTo(
        "None of the Android SDKs that ${Sdk34Only::class.java.name}.onSdk34 is configured for " +
          "is enabled by robolectric.enabledSdks=33"
      )
    assertThat(results.succeeded).isEmpty()
    assertThat(results.failed).isEmpty()
  }

  @Test
  fun `a class that shares an environment is skipped if its SDK is not enabled`() {
    val results =
      Fixtures.run(SharedSdk34::class, properties = mapOf("robolectric.enabledSdks" to "33"))

    assertThat(results.skipped.values.single())
      .contains("${SharedSdk34::class.java.name} is configured for is enabled by")
    assertThat(results.events).isEmpty()
  }

  @Test
  fun `a test configured differently than its shared environment fails`() {
    val results = Fixtures.run(ConflictWithSharedEnvironment::class)

    assertThat(results.succeeded).containsExactly("sharesTheEnvironment()")
    val failure = results.failed["wantsAnotherSdk()"]
    assertThat(failure).hasMessageThat().contains("is configured differently than the Android")
    assertThat(failure).hasMessageThat().contains("@RobolectricSdkTest")
  }

  @Test
  fun `a failure carries the hints of the test runner`() {
    val results = Fixtures.run(LeavingATaskOnTheMainLooper::class)

    val failure = results.failed["failsWithAPendingTask()"]
    assertThat(failure).hasMessageThat().isEqualTo("from the test")
    assertThat(failure!!.suppressed.single())
      .hasMessageThat()
      .contains("Main looper has queued unexecuted runnables")
  }

  @Test
  fun `an argument that can't be recreated in the Android environment fails with an explanation`() {
    val results = Fixtures.run(NotSerializableArgument::class)

    assertThat(results.failed.values.single())
      .hasMessageThat()
      .contains("it has to be serializable, an enum constant, or of a class of the JDK")
  }

  @Fixture
  @ExtendWith(RobolectricExtension::class)
  @Config(sdk = [34])
  class FailingAfterEach {
    @Test
    fun passes() {
      Fixtures.record("test")
    }

    @AfterEach
    fun tearDown() {
      error("from tearDown")
    }
  }

  @Fixture
  @ExtendWith(RobolectricExtension::class)
  @Config(sdk = [34])
  class FailingBeforeEach {
    @BeforeEach
    fun setUp() {
      error("from setUp")
    }

    @Test
    fun neverRuns() {
      Fixtures.record("test")
    }

    @AfterEach
    fun tearDown() {
      Fixtures.record("tearDown")
    }
  }

  @Fixture
  @ExtendWith(RobolectricExtension::class)
  @Config(sdk = [34])
  class FailedAssumption {
    @Test
    fun assumesTheImpossible() {
      assumeTrue(Build.VERSION.SDK_INT == 1)
    }

    @Test
    fun abortsItself() {
      throw TestAbortedException("thrown by the test")
    }
  }

  @Fixture
  @ExtendWith(RobolectricExtension::class)
  @Config(sdk = [34])
  class Sdk34Only {
    @Test
    fun onSdk34() {
      Fixtures.record("test")
    }
  }

  @Fixture
  @ExtendWith(RobolectricExtension::class)
  @Config(sdk = [34])
  class SharedSdk34 {
    @Test
    fun onSdk34() {
      Fixtures.record("test")
    }

    companion object {
      @JvmStatic
      @BeforeAll
      fun setUpClass() {
        Fixtures.record("setUpClass")
      }
    }
  }

  @Fixture
  @ExtendWith(RobolectricExtension::class)
  @Config(sdk = [33])
  class ConflictWithSharedEnvironment {
    @Test
    fun sharesTheEnvironment() {
      assertThat(Build.VERSION.SDK_INT).isEqualTo(33)
    }

    @Test
    @Config(sdk = [34])
    fun wantsAnotherSdk() {
      error("never runs")
    }

    companion object {
      @JvmStatic @BeforeAll fun setUpClass() = Unit
    }
  }

  @Fixture
  @ExtendWith(RobolectricExtension::class)
  @Config(sdk = [34])
  class LeavingATaskOnTheMainLooper {
    @Test
    fun failsWithAPendingTask() {
      Handler(Looper.getMainLooper()).post {}
      error("from the test")
    }
  }

  @Fixture
  @ExtendWith(RobolectricExtension::class)
  @Config(sdk = [34])
  class NotSerializableArgument {
    class Plain(val name: String)

    @ParameterizedTest
    @MethodSource("plainArguments")
    fun takesAPlainObject(argument: Plain) {
      Fixtures.record(argument.name)
    }

    companion object {
      @JvmStatic fun plainArguments() = listOf(Plain("first"))
    }
  }
}
