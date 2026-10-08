package org.robolectric.kotest

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import java.nio.file.Files
import org.robolectric.kotest.internal.InitializerGuard
import org.robolectric.kotest.internal.SpecClassWriter

/**
 * Tests the spec classes that are guarded when they are built: put before the class path, they let
 * Kotest's own launcher, which nothing can be hooked into, initialize them outside of Android.
 */
class SpecClassWriterTest :
  FunSpec({
    // Written as a build does it: by the main class, with the class path of the tests.
    val directory = Files.createTempDirectory("guarded")
    val writer = SpecClassWriter::class.java.name

    test("it writes the spec classes that have a static initializer, guarded") {
      ForkedJvm.run(writer, directory.toString()).exitValue shouldBe 0

      val written =
        Files.walk(directory).use { files ->
          files
            .filter { Files.isRegularFile(it) }
            .map { directory.relativize(it).toString() }
            .toList()
        }
      written shouldContain "org/robolectric/kotest/WithAndroidCompanion.class"
      written shouldContain "org/robolectric/kotest/InheritingWithAndroidCompanion.class"
      written shouldNotContain "org/robolectric/kotest/TaggedInBody.class"
      written shouldNotContain "org/robolectric/kotest/Fixtures.class"
    }

    test("a class that is guarded isn't guarded again") {
      val guarded = directory.resolve("org/robolectric/kotest/WithAndroidCompanion.class")

      InitializerGuard.guard(Files.readAllBytes(guarded)) shouldBe null
    }

    test("before the class path, they let Kotest's launcher run a spec that needs them") {
      val spec = WithAndroidCompanion::class.java.name
      val launch = ForkedJvm.run(ForkedJvm.KOTEST, "--specs", spec, before = directory)

      launch.output shouldContain "a test"
      launch.exitValue shouldBe 0
    }

    test("a spec that Kotest creates still fails if its static initializer does") {
      val spec = WithFailingCompanion::class.java.name
      val launch = ForkedJvm.run(ForkedJvm.KOTEST, "--specs", spec, before = directory)

      launch.output shouldContain "fails wherever it runs"
      launch.exitValue shouldNotBe 0
    }
  })
