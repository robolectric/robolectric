package org.robolectric.kotest

import android.os.Build
import android.os.Looper
import io.kotest.core.annotation.Tags
import io.kotest.core.extensions.ApplyExtension
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.Spec
import io.kotest.core.spec.style.FunSpec
import io.kotest.core.test.TestCase
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldBeIn
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.maps.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

/** Tests that a spec that several SDKs are selected for runs on each of them. */
class SeveralSdksTest :
  FunSpec({
    test("each root test is reported once for each SDK, marked as the test runner marks tests") {
      val results = Fixtures.run(OnTwoSdks::class)

      results.failed.shouldBeEmpty()
      results.succeeded.shouldContainExactly("a test[33]", "a test")
      results.events.shouldContainExactly(" on SDK 33", " on SDK 34")
    }

    test("the last SDK is marked too if the build asks for that") {
      val property = "robolectric.alwaysIncludeVariantMarkersInTestName"
      val results = Fixtures.run(OnTwoSdks::class, properties = mapOf(property to "true"))

      results.succeeded.shouldContainExactly("a test[33]", "a test[34]")
    }

    test("each SDK has an instance of the spec, with its own state and callbacks") {
      val results = Fixtures.run(WithStateOnTwoSdks::class)

      results.failed.shouldBeEmpty()
      results.events.shouldContainExactly(
        "beforeSpec on SDK 33",
        "beforeTest on SDK 33",
        "first on SDK 33, as test 1 of the instance",
        "afterTest on SDK 33",
        "beforeSpec on SDK 34",
        "beforeTest on SDK 34",
        "first on SDK 34, as test 1 of the instance",
        "afterTest on SDK 34",
        "beforeTest on SDK 33",
        "second on SDK 33, as test 2 of the instance",
        "afterTest on SDK 33",
        "beforeTest on SDK 34",
        "second on SDK 34, as test 2 of the instance",
        "afterTest on SDK 34",
        "closed on SDK 33",
        "afterSpec on SDK 33",
        "closed on SDK 34",
        "afterSpec on SDK 34",
      )
    }

    test("the callbacks that a spec overrides run for each instance, around the tests in a test") {
      val results = Fixtures.run(WithOverriddenCallbacksOnTwoSdks::class)

      results.failed.shouldBeEmpty()
      results.events.shouldContainExactly(
        "beforeSpec on SDK 33",
        "beforeContainer on SDK 33",
        "beforeEach on SDK 33",
        "nested on SDK 33",
        "beforeSpec on SDK 34",
        "beforeContainer on SDK 34",
        "beforeEach on SDK 34",
        "nested on SDK 34",
        "afterSpec on SDK 33",
        "afterSpec on SDK 34",
      )
    }

    test("a test that fails on one SDK fails there only") {
      val results = Fixtures.run(FailsOnOneSdk::class)

      results.failed.keys shouldBe setOf("a test[33]")
      results.succeeded.shouldContainExactly("a test")
    }

    test("the spec is configured as its instances are") {
      val results = Fixtures.run(WithTimeoutOnTwoSdks::class)

      results.failed.keys shouldBe setOf("a slow test[33]", "a slow test")
      results.failed.getValue("a slow test").message shouldContain "50ms"
    }

    test("the main dispatcher is that of the environment of the test that runs") {
      val results = Fixtures.run(WithMainDispatcherOnTwoSdks::class)

      results.failed.shouldBeEmpty()
      results.events.shouldContainExactly("main on SDK 33", "main on SDK 34")
    }

    test("the tags of the spec class apply to its tests on each SDK") {
      val included =
        Fixtures.run(TaggedOnTwoSdks::class, properties = mapOf("kotest.tags" to "Slow"))
      val excluded =
        Fixtures.run(TaggedOnTwoSdks::class, properties = mapOf("kotest.tags" to "!Slow"))

      included.succeeded.shouldContainExactly("a test[33]", "a test")
      excluded.succeeded.shouldBeEmpty()
    }

    test("each root test has an instance of its own for each SDK if the spec asks for that") {
      val results = Fixtures.run(PerRootOnTwoSdks::class)

      results.failed.shouldBeEmpty()
      results.events.shouldContainExactly(
        "first on SDK 33, as test 1 of the instance",
        "first on SDK 34, as test 1 of the instance",
        "second on SDK 33, as test 1 of the instance",
        "second on SDK 34, as test 1 of the instance",
      )
    }
  })

/** A spec on two SDKs that runs with the other tests, on the SDKs that the build enables. */
@ApplyExtension(RobolectricExtension::class)
@Config(sdk = [33, 34])
class OnSeveralSdksTest :
  FunSpec({
    test("a test runs on the SDK that it is marked with") {
      Build.VERSION.SDK_INT shouldBeIn listOf(33, 34)
      if (testCase.name.name.endsWith("[33]")) {
        Build.VERSION.SDK_INT shouldBe 33
      }
      (Looper.myLooper() === Looper.getMainLooper()) shouldBe true
    }
  })

private fun record(event: String) {
  check(Looper.myLooper() === Looper.getMainLooper()) { "$event is not on the main thread" }
  Fixtures.record("$event on SDK ${Build.VERSION.SDK_INT}")
}

@Fixture
@ApplyExtension(RobolectricExtension::class)
@Config(sdk = [33, 34])
class OnTwoSdks : FunSpec({ test("a test") { record("") } })

@Fixture
@ApplyExtension(RobolectricExtension::class)
@Config(sdk = [33, 34])
class WithStateOnTwoSdks :
  FunSpec({
    var tests = 0
    autoClose(AutoCloseable { record("closed") })

    beforeSpec { record("beforeSpec") }
    beforeTest { record("beforeTest") }
    afterTest { record("afterTest") }
    afterSpec { record("afterSpec") }

    test("first") {
      Fixtures.record("first on SDK ${Build.VERSION.SDK_INT}, as test ${++tests} of the instance")
    }

    test("second") {
      Fixtures.record("second on SDK ${Build.VERSION.SDK_INT}, as test ${++tests} of the instance")
    }
  })

@Fixture
@ApplyExtension(RobolectricExtension::class)
@Config(sdk = [33, 34])
class WithOverriddenCallbacksOnTwoSdks : FunSpec() {
  init {
    context("a container") { test("a nested test") { record("nested") } }
  }

  override suspend fun beforeSpec(spec: Spec) = record("beforeSpec")

  override suspend fun beforeContainer(testCase: TestCase) = record("beforeContainer")

  override suspend fun beforeEach(testCase: TestCase) = record("beforeEach")

  override suspend fun afterSpec(spec: Spec) = record("afterSpec")
}

@Fixture
@ApplyExtension(RobolectricExtension::class)
@Config(sdk = [33, 34])
class FailsOnOneSdk : FunSpec({ test("a test") { Build.VERSION.SDK_INT shouldBe 34 } })

@Fixture
@ApplyExtension(RobolectricExtension::class)
@Config(sdk = [33, 34])
class WithTimeoutOnTwoSdks :
  FunSpec({
    timeout = 50

    test("a slow test") { delay(10_000) }
  })

@Fixture
@ApplyExtension(RobolectricExtension::class)
@Config(sdk = [33, 34])
class WithMainDispatcherOnTwoSdks :
  FunSpec({
    test("a test") {
      val job = launch(Dispatchers.Main) { record("main") }
      shadowOf(Looper.getMainLooper()).idle()
      job.isCompleted shouldBe true
    }
  })

@Fixture
@ApplyExtension(RobolectricExtension::class)
@Config(sdk = [33, 34])
@Tags("Slow")
class TaggedOnTwoSdks : FunSpec({ test("a test") {} })

@Fixture
@ApplyExtension(RobolectricExtension::class)
@Config(sdk = [33, 34])
class PerRootOnTwoSdks :
  FunSpec({
    isolationMode = IsolationMode.InstancePerRoot
    var tests = 0

    test("first") {
      Fixtures.record("first on SDK ${Build.VERSION.SDK_INT}, as test ${++tests} of the instance")
    }

    test("second") {
      Fixtures.record("second on SDK ${Build.VERSION.SDK_INT}, as test ${++tests} of the instance")
    }
  })
