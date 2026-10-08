@file:OptIn(KotestInternal::class)

package org.robolectric.kotest

import android.os.Build
import io.kotest.common.KotestInternal
import io.kotest.core.Tag
import io.kotest.core.extensions.ApplyExtension
import io.kotest.core.spec.SpecRef
import io.kotest.core.spec.style.FunSpec
import io.kotest.engine.TestEngineLauncher
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.maps.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import kotlin.reflect.KClass
import kotlinx.coroutines.runBlocking
import org.robolectric.annotation.Config

/**
 * Tests that the spec that Kotest gets for a spec class stands in for the instances of it in the
 * sandbox in what Kotest takes from a spec itself.
 */
class StandInTest :
  FunSpec({
    test("the reporters of Kotest, which know a spec by its class, recognize the spec") {
      val fixtures =
        listOf(
          WithCallbacks::class,
          OnTwoSdks::class,
          WithCompanion::class,
          WithAndroidCompanion::class,
          TaggedByOverride::class,
        )
      for (fixture in fixtures) {
        val report = teamCityReportOf(fixture)

        report shouldNotContain "Cannot render test"
        report shouldNotContain "testFailed"
        report shouldContain "testFinished name='${fixture.qualifiedName}"
      }
    }

    test("a spec whose class overrides what Kotest calls on a spec runs in Android too") {
      val properties = mapOf("robolectric.enabledSdks" to "34")
      val results = Fixtures.run(WithOverriddenCallbacksOnTwoSdks::class, properties = properties)

      results.failed.shouldBeEmpty()
      results.events.shouldContainExactly(
        "beforeSpec on SDK 34",
        "beforeContainer on SDK 34",
        "beforeEach on SDK 34",
        "nested on SDK 34",
        "afterSpec on SDK 34",
      )
    }

    test("the tests of a spec see the static state of its class as Android initialized it") {
      val results = Fixtures.run(WithCompanion::class)

      results.failed.shouldBeEmpty()
      results.events.shouldContainExactly("companion on SDK 34", "test with companion of SDK 34")
    }

    test("a callback of a spec that runs its tests runs them, on each SDK") {
      val results = Fixtures.run(WithAroundTest::class)

      results.failed.shouldBeEmpty()
      results.events.shouldContainExactly(
        "before a test[33] on SDK 33",
        "test on SDK 33",
        "after a test[33] on SDK 33",
        "before a test on SDK 34",
        "test on SDK 34",
        "after a test on SDK 34",
      )
    }

    test("a spec that is an object fails with an explanation") {
      val results = Fixtures.run(AnObject::class)

      results.succeeded.shouldBeEmpty()
      results.failed.getValue("AnObject").message shouldContain "is an object, which Kotest creates"
    }

    test("the tags that a spec adds in its body apply to its tests") {
      Fixtures.run(TaggedInBody::class, properties = mapOf("kotest.tags" to "Slow"))
        .succeeded
        .shouldContainExactly("a test")
      Fixtures.run(TaggedInBody::class, properties = mapOf("kotest.tags" to "!Slow"))
        .succeeded
        .shouldBeEmpty()
    }

    test("the tags that a spec returns from tags apply to its tests") {
      Fixtures.run(TaggedByOverride::class, properties = mapOf("kotest.tags" to "Slow"))
        .succeeded
        .shouldContainExactly("a test")
      Fixtures.run(TaggedByOverride::class, properties = mapOf("kotest.tags" to "!Slow"))
        .succeeded
        .shouldBeEmpty()
    }

    test("what a spec wants to run after the project runs then, on one SDK and on several") {
      Fixtures.run(WithAfterProject::class, properties = mapOf("robolectric.enabledSdks" to "34"))
        .events
        .shouldContainExactly("test", "afterProject")
      Fixtures.run(WithAfterProject::class).events.count { it == "afterProject" } shouldBe 2
    }
  })

/** Returns what Kotest's reporter for TeamCity, which its IDE plugin reads, prints for a spec. */
private fun teamCityReportOf(fixture: KClass<out io.kotest.core.spec.Spec>): String {
  val printed = ByteArrayOutputStream()
  val out = System.out
  System.setOut(PrintStream(printed, true))
  try {
    Fixtures.launch {
      runBlocking {
        TestEngineLauncher()
          .withTeamCityListener()
          .withSpecRefs(SpecRef.Reference(fixture))
          .execute()
      }
    }
  } finally {
    System.setOut(out)
  }
  return printed.toString()
}

object Slow : Tag()

@Fixture
@ApplyExtension(RobolectricExtension::class)
@Config(sdk = [33, 34])
class WithAroundTest :
  FunSpec({
    aroundTest { (testCase, execute) ->
      Fixtures.record("before ${testCase.name.name} on SDK ${Build.VERSION.SDK_INT}")
      execute(testCase).also {
        Fixtures.record("after ${testCase.name.name} on SDK ${Build.VERSION.SDK_INT}")
      }
    }

    test("a test") { Fixtures.record("test on SDK ${Build.VERSION.SDK_INT}") }
  })

@Fixture
@ApplyExtension(RobolectricExtension::class)
@Config(sdk = [34])
object AnObject : FunSpec({ test("a test") {} })

@Fixture
@ApplyExtension(RobolectricExtension::class)
@Config(sdk = [34])
class WithCompanion :
  FunSpec({ test("a test") { Fixtures.record("test with companion of SDK $sdkOfCompanion") } }) {
  companion object {
    val sdkOfCompanion = Build.VERSION.SDK_INT.also { Fixtures.record("companion on SDK $it") }
  }
}

@Fixture
@ApplyExtension(RobolectricExtension::class)
@Config(sdk = [34])
class TaggedInBody :
  FunSpec({
    tags(Slow)

    test("a test") {}
  })

@Fixture
@ApplyExtension(RobolectricExtension::class)
@Config(sdk = [34])
class TaggedByOverride : FunSpec() {
  init {
    test("a test") {}
  }

  override fun tags(): Set<Tag> = setOf(Slow)
}

@Fixture
@ApplyExtension(RobolectricExtension::class)
@Config(sdk = [33, 34])
class WithAfterProject :
  FunSpec({
    afterProject { Fixtures.record("afterProject") }

    test("a test") { Fixtures.record("test") }
  })
