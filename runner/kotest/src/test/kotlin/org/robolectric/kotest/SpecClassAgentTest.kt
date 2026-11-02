package org.robolectric.kotest

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain

/**
 * Tests the Java agent of the module where nothing of Kotest or of the JUnit Platform can be hooked
 * into before a spec class is loaded.
 */
class SpecClassAgentTest :
  FunSpec({
    test("Kotest's launcher can't initialize a spec class whose static initializers use Android") {
      val launch = launch(KOTEST, "--specs", ANDROID_COMPANION, withAgent = false)

      launch.exitValue shouldNotBe 0
      launch.output shouldContain "ExceptionInInitializerError"
    }

    test("it can if what uses Android is lazy, and the spec runs") {
      val lazy = WithLazyAndroidCompanion::class.java.name
      val launch = launch(KOTEST, "--specs", lazy, withAgent = false)

      launch.output shouldContain "a test"
      launch.exitValue shouldBe 0
    }

    test("with the agent it can, and the spec runs") {
      val launch = launch(KOTEST, "--specs", ANDROID_COMPANION, withAgent = true)

      launch.output shouldContain "a test"
      launch.exitValue shouldBe 0
    }

    test("with the agent, a spec that Kotest creates still fails if its static initializer does") {
      val failing = WithFailingCompanion::class.java.name
      val launch = launch(KOTEST, "--specs", failing, withAgent = true)

      launch.output shouldContain "fails wherever it runs"
      launch.exitValue shouldNotBe 0
    }

    test("a spec class that is loaded before the launcher session opens can't be guarded") {
      val launch = launch(LOADS_CLASS_FIRST, ANDROID_COMPANION, withAgent = false)

      launch.exitValue shouldNotBe 0
      launch.output shouldContain "ExceptionInInitializerError"
    }

    test("with the agent it is guarded as it is loaded, and the spec runs") {
      val launch = launch(LOADS_CLASS_FIRST, ANDROID_COMPANION, withAgent = true)

      launch.output shouldContain "succeeded: [a test], failed: []"
      launch.exitValue shouldBe 0
    }
  })

private const val KOTEST = ForkedJvm.KOTEST
private const val LOADS_CLASS_FIRST = "org.robolectric.kotest.LoadsClassFirstKt"
private val ANDROID_COMPANION = WithAndroidCompanion::class.java.name

private fun launch(mainClass: String, vararg arguments: String, withAgent: Boolean) =
  ForkedJvm.run(mainClass, *arguments, withAgent = withAgent)
