package org.robolectric.junit.jupiter

import android.os.Build
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.opentest4j.MultipleFailuresError
import org.robolectric.annotation.Config

/** Tests how a `@Test` runs when several Android SDKs are selected for it. */
class MultipleSdksTest {
  @Test
  fun `a test runs on each of its SDKs, as one test`() {
    val results = Fixtures.run(OnTwoSdks::class)

    assertThat(results.failed).isEmpty()
    assertThat(results.succeeded).containsExactly("passes()")
    assertThat(results.events)
      .containsExactly("setUp 33", "setUp 34", "test 33", "test 34", "tearDown 33", "tearDown 34")
      .inOrder()
  }

  @Test
  fun `a test runs on those of its SDKs that are enabled`() {
    val results =
      Fixtures.run(OnTwoSdks::class, properties = mapOf("robolectric.enabledSdks" to "34"))

    assertThat(results.succeeded).containsExactly("passes()")
    assertThat(results.events).containsExactly("setUp 34", "test 34", "tearDown 34").inOrder()
  }

  @Test
  fun `a test that fails on one SDK still runs on the others, and says where it failed`() {
    val results = Fixtures.run(FailingOnSdk33::class)

    val failure = results.failed["failsOnSdk33()"]
    assertThat(failure).hasMessageThat().isEqualTo("on 33")
    assertThat(failure!!.suppressed.map { it.message }).containsExactly("On Android SDK 33")
    assertThat(results.events).containsExactly("test 33", "test 34").inOrder()
  }

  @Test
  fun `a test that fails on several SDKs reports each failure`() {
    val results = Fixtures.run(FailingOnBothSdks::class)

    val failure = results.failed["fails()"] as MultipleFailuresError
    assertThat(failure).hasMessageThat().contains("Failed on the Android SDKs [33, 34]")
    assertThat(failure.failures.map { it.message }).containsExactly("on 33", "on 34").inOrder()
  }

  @Test
  fun `an assumption that fails on one SDK skips the rest of the test there`() {
    val results = Fixtures.run(AssumingSdk34::class)

    assertThat(results.succeeded).containsExactly("needsSdk34()")
    assertThat(results.aborted).isEmpty()
    assertThat(results.events).containsExactly("test 34", "tearDown 33", "tearDown 34").inOrder()
  }

  @Test
  fun `an assumption that fails on every SDK aborts the test`() {
    val results = Fixtures.run(AssumingAnotherSdk::class)

    assertThat(results.aborted).containsExactly("needsAnotherSdk()")
    assertThat(results.events).isEmpty()
  }

  @Test
  fun `a class that shares environments has one for each of its SDKs`() {
    val results = Fixtures.run(SharedOnTwoSdks::class)

    assertThat(results.failed).isEmpty()
    assertThat(results.events)
      .containsExactly(
        "setUpClass 33",
        "setUpClass 34",
        "onBoth 33 shared=true",
        "onBoth 34 shared=true",
        "onlyOnSdk34 34 shared=true",
      )
  }

  @Fixture
  @ExtendWith(RobolectricExtension::class)
  @Config(sdk = [33, 34])
  class OnTwoSdks {
    @BeforeEach
    fun setUp() {
      Fixtures.record("setUp ${Build.VERSION.SDK_INT}")
    }

    @Test
    fun passes() {
      Fixtures.record("test ${Build.VERSION.SDK_INT}")
    }

    @AfterEach
    fun tearDown() {
      Fixtures.record("tearDown ${Build.VERSION.SDK_INT}")
    }
  }

  @Fixture
  @ExtendWith(RobolectricExtension::class)
  @Config(sdk = [33, 34])
  class FailingOnSdk33 {
    @Test
    fun failsOnSdk33() {
      Fixtures.record("test ${Build.VERSION.SDK_INT}")
      check(Build.VERSION.SDK_INT != 33) { "on 33" }
    }
  }

  @Fixture
  @ExtendWith(RobolectricExtension::class)
  @Config(sdk = [33, 34])
  class FailingOnBothSdks {
    @Test
    fun fails() {
      error("on ${Build.VERSION.SDK_INT}")
    }
  }

  @Fixture
  @ExtendWith(RobolectricExtension::class)
  @Config(sdk = [33, 34])
  class AssumingSdk34 {
    @BeforeEach
    fun setUp() {
      assumeTrue(Build.VERSION.SDK_INT == 34)
    }

    @Test
    fun needsSdk34() {
      Fixtures.record("test ${Build.VERSION.SDK_INT}")
    }

    @AfterEach
    fun tearDown() {
      Fixtures.record("tearDown ${Build.VERSION.SDK_INT}")
    }
  }

  @Fixture
  @ExtendWith(RobolectricExtension::class)
  @Config(sdk = [33, 34])
  class AssumingAnotherSdk {
    @Test
    fun needsAnotherSdk() {
      assumeTrue(Build.VERSION.SDK_INT == 1)
      Fixtures.record("test ${Build.VERSION.SDK_INT}")
    }
  }

  @Fixture
  @ExtendWith(RobolectricExtension::class)
  @Config(sdk = [33, 34])
  class SharedOnTwoSdks {
    @Test
    fun onBoth() {
      Fixtures.record("onBoth ${Build.VERSION.SDK_INT} shared=$setUpClassRan")
    }

    @Test
    @Config(sdk = [34])
    fun onlyOnSdk34() {
      Fixtures.record("onlyOnSdk34 ${Build.VERSION.SDK_INT} shared=$setUpClassRan")
    }

    companion object {
      private var setUpClassRan = false

      @JvmStatic
      @BeforeAll
      fun setUpClass() {
        setUpClassRan = true
        Fixtures.record("setUpClass ${Build.VERSION.SDK_INT}")
      }
    }
  }
}
