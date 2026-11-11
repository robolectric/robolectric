package org.robolectric.junit.jupiter

import android.app.Application
import com.google.common.truth.Truth.assertThat
import java.lang.reflect.Method
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.robolectric.TestLifecycleApplication
import org.robolectric.annotation.Config

/** Tests that an application that implements [TestLifecycleApplication] is told about tests. */
class TestLifecycleApplicationTest {
  @Test
  fun `the application is told before and after each test`() {
    val results = Fixtures.run(WithRecordingApplication::class)

    assertThat(results.failed).isEmpty()
    assertThat(results.events)
      .containsExactly(
        "beforeTest runs",
        "prepareTest WithRecordingApplication",
        "setUp",
        "test",
        "tearDown",
        "afterTest runs",
      )
      .inOrder()
  }

  @Test
  fun `the application of a shared environment is told about each of its tests`() {
    val results = Fixtures.run(SharingARecordingApplication::class)

    assertThat(results.failed).isEmpty()
    assertThat(results.events)
      .containsExactly(
        "beforeTest first",
        "prepareTest SharingARecordingApplication",
        "afterTest first",
        "beforeTest second",
        "prepareTest SharingARecordingApplication",
        "afterTest second",
      )
  }

  class RecordingApplication : Application(), TestLifecycleApplication {
    override fun beforeTest(method: Method) = Fixtures.record("beforeTest ${method.name}")

    override fun prepareTest(test: Any) =
      Fixtures.record("prepareTest ${test.javaClass.simpleName}")

    override fun afterTest(method: Method) = Fixtures.record("afterTest ${method.name}")
  }

  @Fixture
  @ExtendWith(RobolectricExtension::class)
  @Config(sdk = [34], application = RecordingApplication::class)
  class WithRecordingApplication {
    @BeforeEach fun setUp() = Fixtures.record("setUp")

    @Test fun runs() = Fixtures.record("test")

    @AfterEach fun tearDown() = Fixtures.record("tearDown")
  }

  @Fixture
  @ExtendWith(RobolectricExtension::class)
  @Config(sdk = [34], application = RecordingApplication::class)
  class SharingARecordingApplication {
    @Test fun first() = Unit

    @Test fun second() = Unit

    companion object {
      @JvmStatic @BeforeAll fun setUpClass() = Unit
    }
  }
}
