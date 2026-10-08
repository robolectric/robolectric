package org.robolectric.runner.common

import com.google.common.truth.Truth.assertThat
import java.nio.file.Files
import java.nio.file.Path
import kotlin.jvm.internal.Intrinsics
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.commons.ClassRemapper
import org.objectweb.asm.commons.SimpleRemapper
import org.robolectric.annotation.Config
import org.robolectric.internal.dependency.DependencyResolver
import org.robolectric.pluginapi.Sdk
import org.robolectric.pluginapi.SdkProvider
import org.robolectric.plugins.DefaultSdkProvider

/** Tests the class path that a session can have of its own. */
@OptIn(ExperimentalRunnerApi::class)
class RobolectricSessionClasspathTest {
  @Test
  fun `a session uses the classes of its class path before those of the JVM`(@TempDir entry: Path) {
    // A class that is only there, and one that the JVM has too, which says where it is from.
    write(entry, AndroidProbe::class.java, renamedTo = ONLY_THERE)
    write(entry, Origin::class.java, replacing = Origin().name to "the entry")

    open(entry) { android ->
      val origin = android.run {
        val onlyThere = android.classLoader.loadClass(ONLY_THERE.replace('/', '.'))
        val sdk = onlyThere.getMethod("sdkInt").invoke(onlyThere.getConstructor().newInstance())
        val both = android.loadClass(Origin::class.java)
        "${both.getMethod("getName").invoke(both.getConstructor().newInstance())} on SDK $sdk"
      }

      assertThat(origin).isEqualTo("the entry on SDK 34")
    }
  }

  @Test
  fun `a session finds the plugins of its class path`(@TempDir entry: Path) {
    // A provider of SDKs that is a plugin there only.
    write(entry, RecordingSdkProvider::class.java, renamedTo = ONLY_THERE)
    val services = Files.createDirectories(entry.resolve("META-INF/services"))
    Files.write(
      services.resolve(SdkProvider::class.java.name),
      listOf(ONLY_THERE.replace('/', '.')),
    )

    try {
      open(entry) { android ->
        assertThat(AndroidProbe.read<Int>(android, "sdkInt")).isEqualTo(34)
      }

      assertThat(System.getProperty(SDKS_PROVIDED)).isEqualTo("true")
    } finally {
      System.clearProperty(SDKS_PROVIDED)
    }
  }

  @Test
  fun `a session loads Kotlin's standard library from a class path that has it`(
    @TempDir withKotlin: Path,
    @TempDir withoutKotlin: Path,
  ) {
    write(withKotlin, Intrinsics::class.java)
    write(withoutKotlin, Origin::class.java)

    assertThat(kotlinIn(withoutKotlin)).isSameInstanceAs(Unit::class.java)
    assertThat(kotlinIn(withKotlin)).isNotSameInstanceAs(Unit::class.java)
  }

  /** Returns a class of Kotlin's standard library as an environment with the entry loads it. */
  private fun kotlinIn(entry: Path): Class<*> =
    open(entry) { android ->
      // Kotlin code runs in it either way.
      assertThat(AndroidProbe.read<Int>(android, "sdkInt")).isEqualTo(34)
      android.loadClass(Unit::class.java)
    }

  /** Runs the action in an environment of a session that has the entry on its class path. */
  private fun <T> open(entry: Path, action: (RobolectricEnvironment) -> T): T =
    RobolectricSession.builder().properties(testProperties()).classpath(entry).build().use { session
      ->
      session.open(session.plan(Config.Builder().setSdk(34).build()).single()).use(action)
    }

  /** Writes the class into the directory, under another name or with another string constant. */
  private fun write(
    directory: Path,
    type: Class<*>,
    renamedTo: String = type.name.replace('.', '/'),
    replacing: Pair<String, String>? = null,
  ) {
    val name = type.name.replace('.', '/')
    val writer = ClassWriter(0)
    val constants =
      object : ClassVisitor(Opcodes.ASM9, ClassRemapper(writer, SimpleRemapper(name, renamedTo))) {
        override fun visitMethod(
          access: Int,
          method: String,
          descriptor: String,
          signature: String?,
          exceptions: Array<String>?,
        ): MethodVisitor =
          object :
            MethodVisitor(
              Opcodes.ASM9,
              super.visitMethod(access, method, descriptor, signature, exceptions),
            ) {
            override fun visitLdcInsn(value: Any) =
              super.visitLdcInsn(if (value == replacing?.first) replacing?.second else value)
          }
      }
    type.getResourceAsStream("/$name.class")!!.use { ClassReader(it).accept(constants, 0) }
    val file = directory.resolve("$renamedTo.class")
    Files.createDirectories(file.parent)
    Files.write(file, writer.toByteArray())
  }

  private companion object {
    const val ONLY_THERE = "only/there/Probe"
  }
}

/** A class that says where it is from. */
class Origin {
  val name: String = "the class path"
}

/** The system property that tells that a [RecordingSdkProvider] provided SDKs. */
const val SDKS_PROVIDED = "org.robolectric.runner.common.sdksProvided"

/** Provides the SDKs that Robolectric has, and records that it did. */
class RecordingSdkProvider(resolver: DependencyResolver) : SdkProvider {
  private val delegate = DefaultSdkProvider(resolver)

  override fun getSdks(): Collection<Sdk> {
    System.setProperty(SDKS_PROVIDED, "true")
    return delegate.sdks
  }
}
