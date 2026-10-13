package org.robolectric.junit.jupiter

import android.os.Build
import android.os.Looper
import com.google.common.truth.Truth.assertThat
import java.util.stream.Stream
import org.junit.jupiter.api.DynamicContainer.dynamicContainer
import org.junit.jupiter.api.DynamicNode
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.extension.ExtendWith
import org.robolectric.annotation.Config

/** Tests that the dynamic tests of a `@TestFactory` run in the Android environment. */
class TestFactoryTest {
  @Test
  fun `dynamic tests are created and run in the Android environment`() {
    val results = Fixtures.run(OneSdk::class)

    assertThat(results.failed.keys).containsExactly("fails")
    assertThat(results.succeeded).containsExactly("first", "nested")
    assertThat(results.events)
      .containsExactly("created on 34", "first on 34 main=true", "nested on 34 main=true")
      .inOrder()
  }

  @Test
  fun `dynamic tests are created one after the other, as they run`() {
    val results = Fixtures.run(Streaming::class)

    assertThat(results.failed).isEmpty()
    assertThat(results.events)
      .containsExactly("created 1", "ran 1", "created 2", "ran 2", "created 3", "ran 3")
      .inOrder()
  }

  @Test
  fun `the dynamic tests of each SDK are grouped`() {
    val results = Fixtures.run(TwoSdks::class)

    assertThat(results.failed).isEmpty()
    assertThat(results.containers).containsAtLeast("SDK 33", "SDK 34")
    assertThat(results.events).containsExactly("test on 33", "test on 34").inOrder()
  }

  @Fixture
  @ExtendWith(RobolectricExtension::class)
  @Config(sdk = [34])
  class OneSdk {
    @TestFactory
    fun tests(): List<DynamicNode> {
      Fixtures.record("created on ${Build.VERSION.SDK_INT}")
      return listOf(
        dynamicTest("first") { Fixtures.record("first on ${describeThread()}") },
        dynamicContainer(
          "container",
          listOf(dynamicTest("nested") { Fixtures.record("nested on ${describeThread()}") }),
        ),
        dynamicTest("fails") { error("from the dynamic test") },
      )
    }

    private fun describeThread() =
      "${Build.VERSION.SDK_INT} main=${Looper.getMainLooper().thread === Thread.currentThread()}"
  }

  @Fixture
  @ExtendWith(RobolectricExtension::class)
  @Config(sdk = [34])
  class Streaming {
    @TestFactory
    fun tests(): Stream<DynamicNode> =
      Stream.iterate(1) { it + 1 }
        .map<DynamicNode> { number ->
          Fixtures.record("created $number")
          dynamicTest("test $number") { Fixtures.record("ran $number") }
        }
        .limit(STREAMED_TESTS)
  }

  @Fixture
  @ExtendWith(RobolectricExtension::class)
  @Config(sdk = [33, 34])
  class TwoSdks {
    @TestFactory
    fun tests() = dynamicTest("test") { Fixtures.record("test on ${Build.VERSION.SDK_INT}") }
  }

  private companion object {
    const val STREAMED_TESTS = 3L
  }
}
