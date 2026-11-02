package org.robolectric.kotest

import android.app.Application
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.maps.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotBeEmpty
import org.robolectric.annotation.Config

/**
 * Tests specs that implement [RobolectricSpec], which applies the extension, and gives it its turn
 * with Kotest's own launcher: that initializes the spec classes before anything else, so the tests
 * for it run it in a JVM of its own.
 */
class RobolectricSpecTest :
  FunSpec({
    test("a spec that implements it runs in Android") {
      val results = Fixtures.run(Implementing::class)

      results.failed.shouldBeEmpty()
      results.succeeded.shouldContainExactly("a test")
    }

    test("an object that implements it fails with an explanation") {
      val results = Fixtures.run(ImplementingObject::class)

      results.succeeded.shouldBeEmpty()
      results.failed.getValue("ImplementingObject").message shouldContain "is an object, which"
    }

    test("with Kotest's launcher, the static initializers of the spec classes can use Android") {
      // The first is being initialized when the extension gets its turn, the second is guarded.
      val first = ImplementingWithAndroidCompanion::class.java.name
      val second = WithAndroidCompanion::class.java.name
      val launch = launchKotest("$first;$second", "--listener", "teamcity")

      // Kotest's own reporter recognizes each spec that Kotest got, also one for each root test.
      launch.output shouldContain "testFinished name='$first.root one'"
      launch.output shouldContain "testFinished name='$first.root two'"
      launch.output shouldContain "testFinished name='$second.a test'"
      launch.exitValue shouldBe 0
    }

    test("so can those of a spec class that overrides how it is configured") {
      val launch = launchKotest(ImplementingWithOverride::class.java.name)

      launch.output shouldContain "Tests:   1 passed, 0 failed"
      launch.exitValue shouldBe 0
    }

    test("a spec class without a static initializer is initialized as usual, and guards the rest") {
      val specs = listOf(Implementing::class, WithAndroidCompanion::class)
      val launch = launchKotest(specs.joinToString(";") { it.java.name })

      launch.output shouldContain "Tests:   2 passed, 0 failed"
      launch.exitValue shouldBe 0
    }
  })

private fun launchKotest(specs: String, vararg arguments: String) =
  ForkedJvm.run(ForkedJvm.KOTEST, "--specs", specs, *arguments)

@Fixture
@Config(sdk = [34])
class Implementing :
  FunSpec({ test("a test") { Build.VERSION.SDK_INT shouldBe 34 } }), RobolectricSpec

@Fixture
@Config(sdk = [34])
object ImplementingObject : FunSpec({ test("a test") {} }), RobolectricSpec

@Fixture
@Config(sdk = [34])
class ImplementingWithAndroidCompanion :
  FunSpec({
    isolationMode = IsolationMode.InstancePerRoot

    test("root one") { application.packageName.shouldNotBeEmpty() }

    test("root two") { application.packageName.shouldNotBeEmpty() }
  }),
  RobolectricSpec {
  companion object {
    val application: Application = ApplicationProvider.getApplicationContext()
  }
}

@Fixture
@Config(sdk = [34])
class ImplementingWithOverride :
  FunSpec({ test("a test") { application.packageName.shouldNotBeEmpty() } }), RobolectricSpec {
  override fun isolationMode() = IsolationMode.SingleInstance

  companion object {
    val application: Application = ApplicationProvider.getApplicationContext()
  }
}
