package org.robolectric.annotation.processing.ksp

import com.google.common.truth.Truth.assertThat
import java.io.ByteArrayOutputStream
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class RobolectricKspProcessorTest {

  // ---------------------------------------------------------------------------
  // Helper
  // ---------------------------------------------------------------------------

  private fun generate(
    shadowPackage: String = "org.robolectric.test",
    shouldInstrumentPackages: Boolean = true,
    priority: Int = 0,
    shadowInfos: List<ShadowInfo> = emptyList(),
    resetterInfos: List<ResetterInfo> = emptyList(),
  ): String {
    val out = ByteArrayOutputStream()
    generateShadowProviderContent(
      shadowPackage,
      shouldInstrumentPackages,
      priority,
      shadowInfos,
      resetterInfos,
      out,
    )
    return out.toString(Charsets.UTF_8)
  }

  /** Shorthand for creating a [ShadowInfo] with sensible defaults for tests. */
  @Suppress("LongParameterList")
  private fun shadow(
    actualName: String,
    shadowBinaryName: String,
    shadowPickerBinaryName: String? = null,
    isInAndroidSdk: Boolean = true,
    minSdk: Int = -1,
    maxSdk: Int = -1,
  ) =
    ShadowInfo(actualName, shadowBinaryName, shadowPickerBinaryName, isInAndroidSdk, minSdk, maxSdk)

  // ---------------------------------------------------------------------------
  // Tests
  // ---------------------------------------------------------------------------

  @Test
  fun noShadows_shouldGenerateEmptyProvider() {
    val content = generate()
    assertThat(content).contains("package org.robolectric.test;")
    assertThat(content).contains("public class Shadows implements ShadowProvider")
    assertThat(content).contains("return new String[] {};")
  }

  @Test
  fun basicShadow_shouldGenerateShadowEntry() {
    val content = generate(shadowInfos = listOf(shadow("java.lang.String", "ShadowString")))
    assertThat(content).contains("package org.robolectric.test;")
    assertThat(content).contains("implements ShadowProvider")
    assertThat(content)
      .contains(
        "SHADOWS.add(new AbstractMap.SimpleImmutableEntry<>(\"java.lang.String\", \"ShadowString\"))"
      )
  }

  @Test
  fun shadowWithClassName_shouldGenerateEntry() {
    val content = generate(shadowInfos = listOf(shadow("com.example.Foo", "ShadowFoo")))
    assertThat(content).contains("\"com.example.Foo\"")
    assertThat(content).contains("ShadowFoo")
  }

  @Test
  fun shadowPickerMap_shouldBeEmitted() {
    val content =
      generate(shadowInfos = listOf(shadow("com.example.Foo", "ShadowFoo", "MyShadowPicker")))
    assertThat(content).contains("SHADOW_PICKER_MAP.put(\"com.example.Foo\", \"MyShadowPicker\")")
    // Shadow pickers go to SHADOW_PICKER_MAP, not SHADOWS
    assertThat(content).doesNotContain("SHADOWS.add")
  }

  @Test
  fun shouldInstrumentPackages_byDefault() {
    val content =
      generate(
        shouldInstrumentPackages = true,
        shadowInfos = listOf(shadow("java.lang.String", "ShadowString")),
      )
    assertThat(content).contains("\"java.lang\"")
  }

  @Test
  fun shouldNotInstrumentPackages_whenDisabled() {
    val content =
      generate(
        shouldInstrumentPackages = false,
        shadowInfos = listOf(shadow("java.lang.String", "ShadowString")),
      )
    assertThat(content).contains("return new String[] {};")
    assertThat(content).doesNotContain("\"java.lang\"")
  }

  @Test
  fun packageNames_shouldBeDeduplicated() {
    val content =
      generate(
        shouldInstrumentPackages = true,
        shadowInfos =
          listOf(
            shadow("java.lang.String", "ShadowString"),
            shadow("java.lang.StringBuilder", "ShadowStringBuilder"),
          ),
      )
    val count = content.split("\"java.lang\"").size - 1
    assertThat(count).isEqualTo(1)
  }

  @Test
  fun customPackage_shouldGenerateInPackage() {
    val content =
      generate(
        shadowPackage = "my.custom.pkg",
        shadowInfos = listOf(shadow("com.example.Foo", "ShadowFoo")),
      )
    assertThat(content).contains("package my.custom.pkg;")
  }

  @Test
  fun serviceFileContent_shouldContainFullyQualifiedShadowsClass() {
    assertThat(generateServiceFileContent("org.robolectric.test"))
      .isEqualTo("org.robolectric.test.Shadows\n")
  }

  @Test
  fun serviceFileContent_customPackage() {
    assertThat(generateServiceFileContent("my.custom.pkg")).isEqualTo("my.custom.pkg.Shadows\n")
  }

  @Test
  fun priorityOption_shouldAddPriorityAnnotation() {
    val content =
      generate(priority = 1, shadowInfos = listOf(shadow("java.lang.String", "ShadowString")))
    assertThat(content).contains("@javax.annotation.Priority(1)")
  }

  @Test
  fun noPriorityOption_shouldNotAddPriorityAnnotation() {
    val content = generate(shadowInfos = listOf(shadow("java.lang.String", "ShadowString")))
    assertThat(content).doesNotContain("@javax.annotation.Priority")
  }

  @Test
  fun resetter_withMinAndMaxSdk_shouldGuardCall() {
    val content =
      generate(
        shadowInfos = listOf(shadow("java.lang.String", "ShadowString", minSdk = 19, maxSdk = 28)),
        resetterInfos = listOf(ResetterInfo("ShadowString.reset();", 19, 28)),
      )
    assertThat(content)
      .contains(
        "if (RuntimeEnvironment.getApiLevel() >= 19 " + "&& RuntimeEnvironment.getApiLevel() <= 28)"
      )
  }

  @Test
  fun resetter_withOnlyMinSdk_shouldGuardCall() {
    val content =
      generate(
        shadowInfos = listOf(shadow("java.lang.String", "ShadowString", minSdk = 21)),
        resetterInfos = listOf(ResetterInfo("ShadowString.reset();", 21, -1)),
      )
    assertThat(content).contains("if (RuntimeEnvironment.getApiLevel() >= 21)")
    assertThat(content).doesNotContain("<= ")
  }

  @Test
  fun resetter_withOnlyMaxSdk_shouldGuardCall() {
    val content =
      generate(
        shadowInfos = listOf(shadow("java.lang.String", "ShadowString", maxSdk = 18)),
        resetterInfos = listOf(ResetterInfo("ShadowString.reset();", -1, 18)),
      )
    assertThat(content).contains("if (RuntimeEnvironment.getApiLevel() <= 18)")
    assertThat(content).doesNotContain(">= ")
  }

  @Test
  fun resetter_withNoSdkBounds_shouldCallUnguarded() {
    val content =
      generate(
        shadowInfos = listOf(shadow("java.lang.String", "ShadowString")),
        resetterInfos = listOf(ResetterInfo("ShadowString.reset();", -1, -1)),
      )
    assertThat(content).contains("ShadowString.reset();")
    assertThat(content).doesNotContain("getApiLevel()")
  }

  @Test
  fun multipleShadows_shouldBeSortedAlphabetically() {
    val content =
      generate(
        shadowInfos =
          listOf(shadow("java.util.List", "ShadowList"), shadow("java.lang.String", "ShadowString"))
      )
    val stringIdx = content.indexOf("\"java.lang.String\"")
    val listIdx = content.indexOf("\"java.util.List\"")
    assertThat(stringIdx).isLessThan(listIdx)
  }

  /**
   * The KSP processor reads `isInAndroidSdk` from the annotation and stores it in [ShadowInfo], but
   * does not use it to alter the generated output because the KSP processor never generates
   * `shadowOf()` helper methods (unlike the Java annotation processor). These tests verify that a
   * shadow with `isInAndroidSdk = false` produces the same generated output as one with the default
   * `isInAndroidSdk = true`.
   */
  @Test
  fun shadowWithIsNotInAndroidSdk_outputIdenticalToDefaultShadow() {
    val defaultShadow =
      shadow("com.example.objects.Dummy", "org.robolectric.ShadowDummy", isInAndroidSdk = true)
    val excludedShadow =
      shadow("com.example.objects.Dummy", "org.robolectric.ShadowDummy", isInAndroidSdk = false)
    val contentDefault =
      generate(shouldInstrumentPackages = true, shadowInfos = listOf(defaultShadow))
    val contentExcluded =
      generate(shouldInstrumentPackages = true, shadowInfos = listOf(excludedShadow))
    assertThat(contentDefault).isEqualTo(contentExcluded)
  }

  @Test
  fun shadowWithIsNotInAndroidSdk_stillInShadowsList() {
    val content =
      generate(
        shouldInstrumentPackages = true,
        shadowInfos =
          listOf(
            shadow(
              "com.example.objects.Dummy",
              "org.robolectric.ShadowDummy",
              isInAndroidSdk = false,
            )
          ),
      )
    assertThat(content)
      .contains(
        "SHADOWS.add(new AbstractMap.SimpleImmutableEntry<>(" +
          "\"com.example.objects.Dummy\", \"org.robolectric.ShadowDummy\"))"
      )
  }

  @Test
  fun shadowWithIsNotInAndroidSdk_packageStillInstrumented() {
    val content =
      generate(
        shouldInstrumentPackages = true,
        shadowInfos =
          listOf(
            shadow(
              "com.example.objects.Dummy",
              "org.robolectric.ShadowDummy",
              isInAndroidSdk = false,
            )
          ),
      )
    assertThat(content).contains("\"com.example.objects\"")
  }

  @Test
  fun shadowWithIsNotInAndroidSdk_isInAndroidSdkStoredInModel() {
    val info = shadow("com.example.Foo", "ShadowFoo", isInAndroidSdk = false)
    assertThat(info.isInAndroidSdk).isFalse()
  }

  @Test
  fun shadowWithDefaultIsInAndroidSdk_defaultsToTrue() {
    val info = shadow("com.example.Foo", "ShadowFoo")
    assertThat(info.isInAndroidSdk).isTrue()
  }
}
