package org.robolectric.kotest

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.assertions.throwables.shouldThrowAny
import io.kotest.core.extensions.ApplyExtension
import io.kotest.core.spec.Spec
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.maps.shouldBeEmpty
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotBeEmpty
import java.net.URLClassLoader
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.jar.JarEntry
import java.util.jar.JarOutputStream
import kotlin.io.path.name
import kotlin.reflect.KClass
import org.junit.platform.engine.DiscoverySelector
import org.junit.platform.engine.discovery.DiscoverySelectors
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.commons.ClassRemapper
import org.objectweb.asm.commons.SimpleRemapper
import org.robolectric.annotation.Config
import org.robolectric.kotest.internal.SpecClassGuard
import org.robolectric.kotest.internal.StaticInitializers

/**
 * Tests that the static initializers of a spec class can use Android, although Kotest initializes
 * the class when it discovers it, outside of Android. The fixtures of this file would fail the
 * discovery of all specs of this module otherwise.
 */
class StaticInitializersTest :
  FunSpec({
    test("a spec class whose static initializers use Android runs") {
      val results = Fixtures.run(WithAndroidCompanion::class)

      results.failed.shouldBeEmpty()
      results.succeeded.shouldContainExactly("a test")
    }

    test("so does one that applies the extension through a class that it extends") {
      val results = Fixtures.run(InheritingWithAndroidCompanion::class)

      results.failed.shouldBeEmpty()
      results.succeeded.shouldContainExactly("a test")
    }

    test("so does one that the extension is registered for with all specs") {
      val parameters = mapOf("kotest.extensions" to RobolectricExtension::class.java.name)
      val results = Fixtures.run(NotAnnotatedWithAndroidCompanion::class, parameters = parameters)

      results.failed.shouldBeEmpty()
      results.succeeded.shouldContainExactly("a test")
    }

    test("a static initializer that fails for a spec that Kotest creates fails that spec only") {
      val results = Fixtures.run(WithFailingCompanion::class, WithAndroidCompanion::class)

      results.succeeded.shouldContainExactly("a test")
      val failure = results.failed.getValue("WithFailingCompanion")
      generateSequence(failure) { it.cause }.map { it.message }.toList() shouldContain
        "fails wherever it runs"
    }

    test("an object whose body uses Android fails as Kotest initializes it, with an explanation") {
      val failure = shouldThrowAny { Fixtures.run(Objects.UsingAndroid::class) }

      generateSequence(failure) { it.cause }.joinToString { it.message.orEmpty() } shouldContain
        "is an object, which Kotest creates"
    }

    test("a spec class in a jar can be initialized outside of Android once it is guarded") {
      val jar = jarOf(WithAndroidCompanion::class)
      val selectors =
        listOf(
          DiscoverySelectors.selectClass(JARRED),
          DiscoverySelectors.selectUniqueId("[engine:kotest]/[spec:$JARRED]/[test:a test]"),
          DiscoverySelectors.selectClasspathRoots(setOf(jar)).single(),
        )

      shouldThrow<ExceptionInInitializerError> { initialize(jar, null) }
      for (selector in selectors) {
        initialize(jar, selector)
      }
    }
  })

private const val JARRED = "jarred.WithAndroidCompanion"

/**
 * Initializes the class of the jar in a class loader of its own, as Kotest does when it discovers a
 * spec class, after a discovery was started with the selector if there is one.
 */
private fun initialize(jar: Path, selector: DiscoverySelector?) {
  URLClassLoader(arrayOf(jar.toUri().toURL()), Spec::class.java.classLoader).use { loader ->
    if (selector != null) {
      val request = LauncherDiscoveryRequestBuilder.request().selectors(selector).build()
      StaticInitializers(SpecClassGuard(loader)).launcherDiscoveryStarted(request)
    }
    Class.forName(JARRED, true, loader)
  }
}

/**
 * Returns a jar with the class and the classes that are nested in it, in the package `jarred`,
 * where only a class loader for the jar finds them.
 */
private fun jarOf(fixture: KClass<*>): Path {
  val name = fixture.java.name.replace('.', '/')
  val directory = Paths.get(fixture.java.protectionDomain.codeSource.location.toURI())
  val files =
    Files.list(directory.resolve(name).parent).use { files ->
      files
        .filter {
          it.name == "${fixture.simpleName}.class" || it.name.startsWith("${fixture.simpleName}$")
        }
        .toList()
    }
  val names = files.associate {
    val simpleName = it.name.removeSuffix(".class")
    "${name.substringBeforeLast('/')}/$simpleName" to "jarred/$simpleName"
  }
  val jar = Files.createTempFile("specs", ".jar")
  jar.toFile().deleteOnExit()
  JarOutputStream(Files.newOutputStream(jar)).use { out ->
    for (file in files) {
      val writer = ClassWriter(0)
      ClassReader(Files.readAllBytes(file)).accept(ClassRemapper(writer, SimpleRemapper(names)), 0)
      out.putNextEntry(JarEntry("jarred/${file.name}"))
      out.write(writer.toByteArray())
      out.closeEntry()
    }
  }
  return jar
}

@Fixture
@ApplyExtension(RobolectricExtension::class)
@Config(sdk = [34])
class WithAndroidCompanion :
  FunSpec({ test("a test") { application.packageName.shouldNotBeEmpty() } }) {
  companion object {
    val application: Application = ApplicationProvider.getApplicationContext()
  }
}

@Fixture
@ApplyExtension(RobolectricExtension::class)
@Config(sdk = [34])
class WithLazyAndroidCompanion :
  FunSpec({ test("a test") { application.packageName.shouldNotBeEmpty() } }) {
  companion object {
    val application: Application by lazy { ApplicationProvider.getApplicationContext() }
  }
}

@ApplyExtension(RobolectricExtension::class)
@Config(sdk = [34])
abstract class AppliesExtension(body: FunSpec.() -> Unit) : FunSpec(body)

@Fixture
class InheritingWithAndroidCompanion :
  AppliesExtension({ test("a test") { application.packageName.shouldNotBeEmpty() } }) {
  companion object {
    val application: Application = ApplicationProvider.getApplicationContext()
  }
}

@Fixture
@Config(sdk = [34])
class NotAnnotatedWithAndroidCompanion :
  FunSpec({ test("a test") { application.packageName.shouldNotBeEmpty() } }) {
  companion object {
    val application: Application = ApplicationProvider.getApplicationContext()
  }
}

@Fixture
class WithFailingCompanion : FunSpec({ test("a test that never runs") {} }) {
  companion object {
    val state: String = error("fails wherever it runs")
  }
}

/** Holds a spec that fails the discovery of all specs that are discovered with it. */
class Objects {
  @Fixture
  @ApplyExtension(RobolectricExtension::class)
  @Config(sdk = [34])
  object UsingAndroid :
    FunSpec({
      val application = ApplicationProvider.getApplicationContext<Application>()

      test("a test") { application.packageName.shouldNotBeEmpty() }
    })
}
