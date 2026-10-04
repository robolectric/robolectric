package org.robolectric.kotest

import android.os.Looper
import io.kotest.core.extensions.ApplyExtension
import io.kotest.core.spec.Spec
import io.kotest.core.spec.style.FunSpec
import io.kotest.core.test.TestCase
import io.kotest.core.test.testCoroutineScheduler
import io.kotest.engine.coroutines.CoroutineDispatcherFactory
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import java.time.Duration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.withContext
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

private fun isOnMainThread() = Looper.myLooper() === Looper.getMainLooper()

/**
 * Tests that the tests of a spec can suspend and stay on Android's main thread, and that coroutines
 * work in them as Kotest and as Android make them work.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@ApplyExtension(RobolectricExtension::class)
@Config(sdk = [34])
class CoroutinesTest :
  FunSpec({
    test("a test is back on the main thread after it was suspended") {
      delay(1)
      isOnMainThread() shouldBe true
    }

    test("a test is back on the main thread after it ran on another dispatcher") {
      withContext(Dispatchers.IO) { isOnMainThread() } shouldBe false
      isOnMainThread() shouldBe true
    }

    test("a test can launch coroutines in its own scope, and in a scope inside of it") {
      var launched = 0
      launch { launched++ }.join()
      coroutineScope { launch { launched++ } }
      launched shouldBe 2
    }

    test("a test can use the virtual time of Kotest").config(coroutineTestScope = true) {
      val start = testCoroutineScheduler.currentTime
      delay(60_000)
      testCoroutineScheduler.currentTime - start shouldBe 60_000
    }

    test("the main dispatcher is that of Android, which runs coroutines on the main looper") {
      var ran = false
      val job = launch(Dispatchers.Main) { ran = isOnMainThread() }
      ran shouldBe false
      shadowOf(Looper.getMainLooper()).idle()
      ran shouldBe true
      job.join()
      withContext(Dispatchers.Main.immediate) { isOnMainThread() } shouldBe true
    }

    test("a delay on the main dispatcher takes the time of the main looper") {
      var done = false
      val job =
        launch(Dispatchers.Main) {
          delay(60_000)
          done = true
        }
      shadowOf(Looper.getMainLooper()).idle()
      done shouldBe false
      shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMinutes(1))
      done shouldBe true
      job.join()
    }

    test("a test can set a main dispatcher of its own") {
      val dispatcher = StandardTestDispatcher()
      Dispatchers.setMain(dispatcher)
      try {
        var ran = false
        val job = launch(Dispatchers.Main) { ran = true }
        shadowOf(Looper.getMainLooper()).idle()
        ran shouldBe false
        dispatcher.scheduler.advanceUntilIdle()
        ran shouldBe true
        job.join()
      } finally {
        Dispatchers.resetMain()
      }
    }

    test("the main dispatcher is that of Android again after a test reset its own") {
      withContext(Dispatchers.Main.immediate) { isOnMainThread() } shouldBe true
    }
  })

/** Tests that a test runs on the main thread although Kotest runs it on another dispatcher. */
@ApplyExtension(RobolectricExtension::class)
@Config(sdk = [34])
class DispatcherFactoryTest :
  FunSpec({
    coroutineDispatcherFactory = OnAnotherThread

    beforeTest { isOnMainThread() shouldBe true }

    test("a test runs on the main thread") {
      Looper.myLooper() shouldBeSameInstanceAs Looper.getMainLooper()
    }

    context("a container") {
      test("runs its tests on the main thread") { isOnMainThread() shouldBe true }
    }
  })

/** Makes Kotest run each spec, and each test, on a thread of the IO dispatcher. */
private object OnAnotherThread : CoroutineDispatcherFactory {
  override suspend fun <T> withDispatcher(spec: Spec, f: suspend () -> T): T =
    withContext(Dispatchers.IO) { f() }

  override suspend fun <T> withDispatcher(testCase: TestCase, f: suspend () -> T): T =
    withContext(Dispatchers.IO) { f() }
}
