package org.robolectric.kotest

import android.os.Build
import android.os.Handler
import android.os.Looper
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.extensions.ApplyExtension
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.maps.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.robolectric.annotation.Config
import org.robolectric.kotest.internal.Sessions

/** Tests what the extension does with specs that can't run as they are, and around a spec. */
class RobolectricExtensionBehaviorTest :
  FunSpec({
    test("the extension runs without JUnit 4") {
      shouldThrow<ClassNotFoundException> {
        Class.forName("org.junit.runners.BlockJUnit4ClassRunner")
      }
      Fixtures.run(WithCallbacks::class).failed.shouldBeEmpty()
    }

    test("the callbacks of a spec run in its environment, on the main thread") {
      val results = Fixtures.run(WithCallbacks::class)

      results.failed.shouldBeEmpty()
      results.events.shouldContainExactly(
        "beforeSpec on SDK 34, main thread: true",
        "beforeTest, main thread: true",
        "test",
        "afterTest, main thread: true",
        "afterSpec, main thread: true",
      )
    }

    test("a spec that no SDK is enabled for is skipped, and says why") {
      val results =
        Fixtures.run(WithCallbacks::class, properties = mapOf("robolectric.enabledSdks" to "33"))

      results.failed.shouldBeEmpty()
      results.events.shouldBeEmpty()
      results.skipped.keys.single() shouldContain "is enabled by robolectric.enabledSdks=33"
    }

    test("a spec runs on one of its SDKs if only that one is enabled") {
      val results =
        Fixtures.run(OnTwoSdks::class, properties = mapOf("robolectric.enabledSdks" to "33"))

      results.failed.shouldBeEmpty()
      results.succeeded.shouldContainExactly("a test")
      results.events.shouldContainExactly(" on SDK 33")
    }

    test("a spec without a constructor without parameters fails with an explanation") {
      val results = Fixtures.run(WithConstructorParameter::class)

      results.failed.getValue("WithConstructorParameter").message shouldContain
        "it needs a constructor without parameters"
    }

    test("the failure of a test carries Robolectric's hints") {
      val results = Fixtures.run(LeavesTaskOnMainLooper::class)

      val failure = results.failed["fails with a task left on the main looper"]
      failure shouldNotBe null
      failure!!.message shouldBe "the test failed"
      failure.suppressed.single().message shouldContain
        "Main looper has queued unexecuted runnables"
    }

    test("a coroutine that a test launches in its own scope is part of the test") {
      val results = Fixtures.run(LaunchesInTestScope::class)

      results.succeeded.shouldContainExactly("waits for what it launched")
      results.failed.getValue("fails if what it launched fails").message shouldBe
        "launched and failed"
      results.events.shouldContainExactly("launched on the main thread: true")
    }

    test("a session is closed when the project of Kotest is over") {
      Fixtures.run(WithCallbacks::class, closesSession = false)

      Sessions.closeIfIdle() shouldBe false
    }

    test("specs that are configured the same use the same sandbox, one after the other") {
      val results = Fixtures.run(RecordsSandbox::class, AlsoRecordsSandbox::class)

      results.failed.shouldBeEmpty()
      results.events.size shouldBe 2
      results.events.toSet().size shouldBe 1
    }
  })

private fun isOnMainThread() = Looper.myLooper() === Looper.getMainLooper()

@Fixture
@ApplyExtension(RobolectricExtension::class)
@Config(sdk = [34])
class WithCallbacks :
  FunSpec({
    beforeSpec {
      Fixtures.record(
        "beforeSpec on SDK ${Build.VERSION.SDK_INT}, main thread: ${isOnMainThread()}"
      )
    }
    beforeTest { Fixtures.record("beforeTest, main thread: ${isOnMainThread()}") }
    afterTest { Fixtures.record("afterTest, main thread: ${isOnMainThread()}") }
    afterSpec { Fixtures.record("afterSpec, main thread: ${isOnMainThread()}") }

    test("a test") { Fixtures.record("test") }
  })

@Fixture
@ApplyExtension(RobolectricExtension::class)
@Config(sdk = [34])
class WithConstructorParameter(@Suppress("unused") private val parameter: String) :
  FunSpec({ test("a test") {} })

@Fixture
@ApplyExtension(RobolectricExtension::class)
@Config(sdk = [34])
class LeavesTaskOnMainLooper :
  FunSpec({
    test("fails with a task left on the main looper") {
      Handler(Looper.getMainLooper()).post {}
      error("the test failed")
    }
  })

@Fixture
@ApplyExtension(RobolectricExtension::class)
@Config(sdk = [34])
class LaunchesInTestScope :
  FunSpec({
    test("waits for what it launched") {
      launch {
        delay(10)
        Fixtures.record("launched on the main thread: ${isOnMainThread()}")
      }
    }

    test("fails if what it launched fails") { launch { error("launched and failed") } }
  })

@Fixture
@ApplyExtension(RobolectricExtension::class)
@Config(sdk = [34])
class RecordsSandbox :
  FunSpec({
    test("a test") { Fixtures.record("${System.identityHashCode(Build::class.java.classLoader)}") }
  })

@Fixture
@ApplyExtension(RobolectricExtension::class)
@Config(sdk = [34])
class AlsoRecordsSandbox :
  FunSpec({
    test("a test") { Fixtures.record("${System.identityHashCode(Build::class.java.classLoader)}") }
  })
