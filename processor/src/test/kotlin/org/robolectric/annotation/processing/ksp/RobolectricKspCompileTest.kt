package org.robolectric.annotation.processing.ksp

import com.google.common.truth.Truth.assertThat
import com.tschuchort.compiletesting.JvmCompilationResult
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.configureKsp
import com.tschuchort.compiletesting.kspProcessorOptions
import com.tschuchort.compiletesting.sourcesGeneratedBySymbolProcessor
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

/**
 * Full-pipeline compile tests for the KSP processor using kctfork. These tests feed actual
 * Kotlin/Java source through the KSP processor and verify the generated output, covering annotation
 * parsing, resetter collection, error handling, and edge cases that cannot be tested by the
 * unit-level [RobolectricKspProcessorTest].
 */
@OptIn(ExperimentalCompilerApi::class)
@RunWith(JUnit4::class)
class RobolectricKspCompileTest {

  // ---------------------------------------------------------------------------
  // Helpers
  // ---------------------------------------------------------------------------

  /**
   * Stub for [org.robolectric.RuntimeEnvironment] so that the generated Shadows.java with
   * SDK-guarded resetters can compile without pulling in the full shadows/framework module.
   */
  private val runtimeEnvironmentStub =
    SourceFile.java(
      "RuntimeEnvironment.java",
      """
      package org.robolectric;
      public class RuntimeEnvironment {
        public static int getApiLevel() { return 0; }
      }
      """,
    )

  private fun compile(
    vararg sources: SourceFile,
    shadowPackage: String = "com.test",
    shouldInstrumentPackages: Boolean = true,
    priority: Int = 0,
  ): JvmCompilationResult {
    val options =
      mutableMapOf("org.robolectric.annotation.processing.shadowPackage" to shadowPackage)
    if (!shouldInstrumentPackages) {
      options["org.robolectric.annotation.processing.shouldInstrumentPackage"] = "false"
    }
    if (priority != 0) {
      options["org.robolectric.annotation.processing.priority"] = priority.toString()
    }
    val compilation =
      KotlinCompilation().apply {
        this.sources = sources.toList() + runtimeEnvironmentStub
        configureKsp { symbolProcessorProviders += RobolectricKspProcessorProvider() }
        kspProcessorOptions = options
        inheritClassPath = true
      }
    return compilation.compile()
  }

  /** Returns the generated `Shadows.java` content from a successful compilation result. */
  private fun JvmCompilationResult.shadowsJavaContent(): String {
    val shadowsFile =
      sourcesGeneratedBySymbolProcessor.firstOrNull { it.name == "Shadows.java" }
        ?: error(
          "Shadows.java not found in generated files: " +
            "${sourcesGeneratedBySymbolProcessor.map { it.name }.toList()}"
        )
    return shadowsFile.readText()
  }

  /** Returns the generated service file content from a successful compilation result. */
  private fun JvmCompilationResult.serviceFileContent(): String {
    // Check in KSP-generated sources first (includes resource files)
    val fromSources =
      sourcesGeneratedBySymbolProcessor.firstOrNull {
        it.path.contains("META-INF/services") ||
          it.name == "org.robolectric.internal.ShadowProvider"
      }
    if (fromSources != null) return fromSources.readText()

    // Fall back to compiled output files
    val fromGenerated =
      generatedFiles.firstOrNull {
        it.path.contains("META-INF/services") ||
          it.name == "org.robolectric.internal.ShadowProvider"
      }
    if (fromGenerated != null) return fromGenerated.readText()

    error(
      "Service file not found.\ngenerated: ${generatedFiles.map { it.path }}\n" +
        "kspSources: ${sourcesGeneratedBySymbolProcessor.map { it.path }.toList()}"
    )
  }

  // ---------------------------------------------------------------------------
  // Basic compilation tests
  // ---------------------------------------------------------------------------

  @Test
  fun basicKotlinShadow_shouldCompileAndGenerate() {
    val source =
      SourceFile.kotlin(
        "ShadowFoo.kt",
        """
        package com.test.shadows
        import org.robolectric.annotation.Implements

        class Foo
        @Implements(Foo::class)
        class ShadowFoo
        """,
      )

    val result = compile(source)
    assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.OK)

    val content = result.shadowsJavaContent()
    assertThat(content).contains("com.test.shadows.Foo")
    assertThat(content).contains("com.test.shadows.ShadowFoo")
  }

  @Test
  fun basicJavaShadow_shouldCompileAndGenerate() {
    val targetSource =
      SourceFile.java(
        "Bar.java",
        """
        package com.test.shadows;
        public class Bar {}
        """,
      )
    val shadowSource =
      SourceFile.java(
        "ShadowBar.java",
        """
        package com.test.shadows;
        import org.robolectric.annotation.Implements;

        @Implements(Bar.class)
        public class ShadowBar {}
        """,
      )

    val result = compile(targetSource, shadowSource)
    assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.OK)

    val content = result.shadowsJavaContent()
    assertThat(content).contains("com.test.shadows.Bar")
    assertThat(content).contains("com.test.shadows.ShadowBar")
  }

  // ---------------------------------------------------------------------------
  // Annotation attribute parsing
  // ---------------------------------------------------------------------------

  @Test
  fun classNameAttribute_shouldResolveActualName() {
    val source =
      SourceFile.kotlin(
        "ShadowMyClass.kt",
        """
        package com.test.shadows
        import org.robolectric.annotation.Implements

        @Implements(className = "com.hidden.HiddenClass")
        class ShadowMyClass
        """,
      )

    val result = compile(source)
    assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.OK)

    val content = result.shadowsJavaContent()
    assertThat(content).contains("com.hidden.HiddenClass")
    assertThat(content).contains("com.test.shadows.ShadowMyClass")
  }

  @Test
  fun minSdkAndMaxSdk_shouldGuardResetter() {
    val source =
      SourceFile.kotlin(
        "ShadowBounded.kt",
        """
        package com.test.shadows
        import org.robolectric.annotation.Implements
        import org.robolectric.annotation.Resetter

        class Bounded
        @Implements(value = Bounded::class, minSdk = 21, maxSdk = 30)
        class ShadowBounded {
          companion object {
            @JvmStatic @Resetter fun reset() {}
          }
        }
        """,
      )

    val result = compile(source)
    assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.OK)

    val content = result.shadowsJavaContent()
    assertThat(content).contains("getApiLevel() >= 21")
    assertThat(content).contains("getApiLevel() <= 30")
    assertThat(content).contains("ShadowBounded.reset();")
  }

  @Test
  fun isInAndroidSdk_shouldBeResolvedButNotAffectOutput() {
    val source =
      SourceFile.kotlin(
        "ShadowHidden.kt",
        """
        package com.test.shadows
        import org.robolectric.annotation.Implements

        @Implements(className = "com.hidden.InternalClass", isInAndroidSdk = false)
        class ShadowHidden
        """,
      )

    val result = compile(source)
    assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.OK)

    val content = result.shadowsJavaContent()
    // Shadow is still registered despite isInAndroidSdk = false
    assertThat(content).contains("com.hidden.InternalClass")
    assertThat(content).contains("com.test.shadows.ShadowHidden")
  }

  @Test
  fun shadowPicker_shouldGoToPickerMap() {
    val source =
      SourceFile.kotlin(
        "ShadowWithPicker.kt",
        """
        package com.test.shadows
        import org.robolectric.annotation.Implements
        import org.robolectric.shadow.api.ShadowPicker

        class Target
        class MyPicker : ShadowPicker<ShadowWithPicker> {
          override fun pickShadowClass(): Class<ShadowWithPicker> = ShadowWithPicker::class.java
        }
        @Implements(value = Target::class, shadowPicker = MyPicker::class)
        class ShadowWithPicker
        """,
      )

    val result = compile(source)
    assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.OK)

    val content = result.shadowsJavaContent()
    // Should go to SHADOW_PICKER_MAP, not SHADOWS
    assertThat(content).contains("SHADOW_PICKER_MAP.put(")
    assertThat(content).contains("com.test.shadows.Target")
    assertThat(content).contains("com.test.shadows.MyPicker")
    // Should NOT be in SHADOWS list
    assertThat(content).doesNotContain("SHADOWS.add")
  }

  // ---------------------------------------------------------------------------
  // Resetter collection
  // ---------------------------------------------------------------------------

  @Test
  fun javaStaticResetter_shouldBeCollected() {
    val targetSource =
      SourceFile.java(
        "HasResetter.java",
        """
        package com.test.shadows;
        public class HasResetter {}
        """,
      )
    val shadowSource =
      SourceFile.java(
        "ShadowWithResetter.java",
        """
        package com.test.shadows;
        import org.robolectric.annotation.Implements;
        import org.robolectric.annotation.Resetter;

        @Implements(HasResetter.class)
        public class ShadowWithResetter {
          @Resetter
          public static void reset() {}
        }
        """,
      )

    val result = compile(targetSource, shadowSource)
    assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.OK)

    val content = result.shadowsJavaContent()
    assertThat(content).contains("ShadowWithResetter.reset();")
  }

  @Test
  fun companionObjectResetter_withJvmStatic_shouldBeCollected() {
    val source =
      SourceFile.kotlin(
        "ShadowCompanion.kt",
        """
        package com.test.shadows
        import org.robolectric.annotation.Implements
        import org.robolectric.annotation.Resetter

        class CompanionTarget
        @Implements(CompanionTarget::class)
        class ShadowCompanion {
          companion object {
            @JvmStatic @Resetter fun resetState() {}
          }
        }
        """,
      )

    val result = compile(source)
    assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.OK)

    val content = result.shadowsJavaContent()
    assertThat(content).contains("ShadowCompanion.resetState();")
  }

  @Test
  fun resetterWithoutSdkBounds_shouldBeUnguarded() {
    val source =
      SourceFile.kotlin(
        "ShadowUnguarded.kt",
        """
        package com.test.shadows
        import org.robolectric.annotation.Implements
        import org.robolectric.annotation.Resetter

        class UnguardedTarget
        @Implements(UnguardedTarget::class)
        class ShadowUnguarded {
          companion object {
            @JvmStatic @Resetter fun reset() {}
          }
        }
        """,
      )

    val result = compile(source)
    assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.OK)

    val content = result.shadowsJavaContent()
    assertThat(content).contains("ShadowUnguarded.reset();")
    assertThat(content).doesNotContain("getApiLevel()")
  }

  // ---------------------------------------------------------------------------
  // Processor options
  // ---------------------------------------------------------------------------

  @Test
  fun priorityOption_shouldEmitAnnotation() {
    val source =
      SourceFile.kotlin(
        "ShadowP.kt",
        """
        package com.test.shadows
        import org.robolectric.annotation.Implements

        class P
        @Implements(P::class)
        class ShadowP
        """,
      )

    val result = compile(source, priority = 1)
    assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.OK)

    val content = result.shadowsJavaContent()
    assertThat(content).contains("@javax.annotation.Priority(1)")
  }

  @Test
  fun shouldInstrumentPackages_false_shouldReturnEmptyArray() {
    val source =
      SourceFile.kotlin(
        "ShadowNoInstr.kt",
        """
        package com.test.shadows
        import org.robolectric.annotation.Implements

        class NoInstr
        @Implements(NoInstr::class)
        class ShadowNoInstr
        """,
      )

    val result = compile(source, shouldInstrumentPackages = false)
    assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.OK)

    val content = result.shadowsJavaContent()
    assertThat(content).contains("return new String[] {};")
  }

  @Test
  fun serviceFile_shouldContainQualifiedShadowsClass() {
    val source =
      SourceFile.kotlin(
        "ShadowSvc.kt",
        """
        package com.test.shadows
        import org.robolectric.annotation.Implements

        class Svc
        @Implements(Svc::class)
        class ShadowSvc
        """,
      )

    val result = compile(source, shadowPackage = "my.custom.pkg")
    assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.OK)

    val content = result.serviceFileContent()
    assertThat(content.trim()).isEqualTo("my.custom.pkg.Shadows")
  }

  // ---------------------------------------------------------------------------
  // Edge cases
  // ---------------------------------------------------------------------------

  @Test
  fun nestedClass_shouldUseBinaryName() {
    val source =
      SourceFile.kotlin(
        "Outer.kt",
        """
        package com.test.shadows
        import org.robolectric.annotation.Implements

        class Outer {
          class Inner
        }
        @Implements(Outer.Inner::class)
        class ShadowInner
        """,
      )

    val result = compile(source)
    assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.OK)

    val content = result.shadowsJavaContent()
    // Actual class name should use $ for nested classes
    assertThat(content).contains("com.test.shadows.Outer\$Inner")
  }

  @Test
  fun mixedKotlinAndJava_shouldProcessBoth() {
    val ktSource =
      SourceFile.kotlin(
        "ShadowKt.kt",
        """
        package com.test.shadows
        import org.robolectric.annotation.Implements
        import org.robolectric.annotation.Resetter

        class KtTarget
        @Implements(KtTarget::class)
        class ShadowKt {
          companion object {
            @JvmStatic @Resetter fun reset() {}
          }
        }
        """,
      )

    val javaTargetSource =
      SourceFile.java(
        "JvTarget.java",
        """
        package com.test.shadows;
        public class JvTarget {}
        """,
      )

    val javaShadowSource =
      SourceFile.java(
        "ShadowJv.java",
        """
        package com.test.shadows;
        import org.robolectric.annotation.Implements;
        import org.robolectric.annotation.Resetter;

        @Implements(JvTarget.class)
        public class ShadowJv {
          @Resetter
          public static void reset() {}
        }
        """,
      )

    val result = compile(ktSource, javaTargetSource, javaShadowSource)
    assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.OK)

    val content = result.shadowsJavaContent()
    // Both shadows should be registered
    assertThat(content).contains("com.test.shadows.JvTarget")
    assertThat(content).contains("com.test.shadows.ShadowJv")
    assertThat(content).contains("com.test.shadows.KtTarget")
    assertThat(content).contains("com.test.shadows.ShadowKt")
    // Both resetters should be present
    assertThat(content).contains("ShadowJv.reset();")
    assertThat(content).contains("ShadowKt.reset();")
  }

  @Test
  fun multipleShadows_shouldBeSortedByActualName() {
    val source =
      SourceFile.kotlin(
        "MultipleShadows.kt",
        """
        package com.test.shadows
        import org.robolectric.annotation.Implements

        class Zebra
        class Apple
        @Implements(Zebra::class) class ShadowZebra
        @Implements(Apple::class) class ShadowApple
        """,
      )

    val result = compile(source)
    assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.OK)

    val content = result.shadowsJavaContent()
    val appleIdx = content.indexOf("com.test.shadows.Apple")
    val zebraIdx = content.indexOf("com.test.shadows.Zebra")
    assertThat(appleIdx).isLessThan(zebraIdx)
  }

  @Test
  fun implementsOnInterface_shouldBeIgnored() {
    val source =
      SourceFile.kotlin(
        "InterfaceShadow.kt",
        """
        package com.test.shadows
        import org.robolectric.annotation.Implements

        class RealClass

        @Implements(RealClass::class)
        interface ShadowInterface

        @Implements(RealClass::class)
        class ShadowClass
        """,
      )

    val result = compile(source)
    assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.OK)

    val content = result.shadowsJavaContent()
    // The interface should not appear, only the class
    assertThat(content).contains("ShadowClass")
    assertThat(content).doesNotContain("ShadowInterface")
  }

  @Test
  fun providedPackageNames_shouldDeduplicatePackages() {
    val source =
      SourceFile.kotlin(
        "DupPackages.kt",
        """
        package com.test.shadows
        import org.robolectric.annotation.Implements

        class One
        class Two
        @Implements(One::class) class ShadowOne
        @Implements(Two::class) class ShadowTwo
        """,
      )

    val result = compile(source)
    assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.OK)

    val content = result.shadowsJavaContent()
    // Package should appear only once
    val count = content.split("\"com.test.shadows\"").size - 1
    assertThat(count).isEqualTo(1)
  }

  @Test
  fun implementsWithBlankClassName_shouldFailCompilation() {
    val source =
      SourceFile.kotlin(
        "ShadowVoid.kt",
        """
        package com.test.shadows
        import org.robolectric.annotation.Implements

        @Implements(className = "")
        class ShadowVoid
        """,
      )

    val result = compile(source)
    assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.COMPILATION_ERROR)
  }

  @Test
  fun implementsWithVoidValue_shouldBeSkipped() {
    val source =
      SourceFile.java(
        "ShadowVoidJava.java",
        """
        package com.test.shadows;
        import org.robolectric.annotation.Implements;

        @Implements(Void.class)
        public class ShadowVoidJava {}
        """,
      )

    val result = compile(source)
    // Void.class is filtered to null, causing an error for the unresolvable target
    assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.COMPILATION_ERROR)
  }

  @Test
  fun invalidTarget_shouldNotBlockValidShadows() {
    val validSource =
      SourceFile.kotlin(
        "ShadowValid.kt",
        """
        package com.test.shadows
        import org.robolectric.annotation.Implements

        class ValidTarget
        @Implements(ValidTarget::class)
        class ShadowValid
        """,
      )
    val invalidSource =
      SourceFile.kotlin(
        "ShadowInvalid.kt",
        """
        package com.test.shadows
        import org.robolectric.annotation.Implements

        @Implements(className = "")
        class ShadowInvalid
        """,
      )

    val result = compile(validSource, invalidSource)
    // Compilation fails due to the invalid target, but the valid shadow should still be generated.
    assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.COMPILATION_ERROR)

    val content = result.shadowsJavaContent()
    assertThat(content).contains("com.test.shadows.ValidTarget")
    assertThat(content).contains("com.test.shadows.ShadowValid")
    assertThat(content).doesNotContain("ShadowInvalid")
  }
}
