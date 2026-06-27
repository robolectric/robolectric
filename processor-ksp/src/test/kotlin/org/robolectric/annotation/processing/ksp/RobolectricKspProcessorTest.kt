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
    actualIsPublic: Boolean = true,
  ) =
    ShadowInfo(
      actualName,
      shadowBinaryName,
      shadowPickerBinaryName,
      isInAndroidSdk,
      minSdk,
      maxSdk,
      actualIsPublic,
    )

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
        resetterInfos =
          listOf(ResetterInfo("ShadowString.reset();", 19, 28, "ShadowString", "java.lang.String")),
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
        resetterInfos =
          listOf(ResetterInfo("ShadowString.reset();", 21, -1, "ShadowString", "java.lang.String")),
      )
    assertThat(content).contains("if (RuntimeEnvironment.getApiLevel() >= 21)")
    assertThat(content).doesNotContain("<= ")
  }

  @Test
  fun resetter_withOnlyMaxSdk_shouldGuardCall() {
    val content =
      generate(
        shadowInfos = listOf(shadow("java.lang.String", "ShadowString", maxSdk = 18)),
        resetterInfos =
          listOf(ResetterInfo("ShadowString.reset();", -1, 18, "ShadowString", "java.lang.String")),
      )
    assertThat(content).contains("if (RuntimeEnvironment.getApiLevel() <= 18)")
    assertThat(content).doesNotContain(">= ")
  }

  @Test
  fun resetter_withNoSdkBounds_shouldCallUnguarded() {
    val content =
      generate(
        shadowInfos = listOf(shadow("java.lang.String", "ShadowString")),
        resetterInfos =
          listOf(ResetterInfo("ShadowString.reset();", -1, -1, "ShadowString", "java.lang.String")),
      )
    assertThat(content).contains("ShadowString.reset();")
    assertThat(content).doesNotContain("getApiLevel()")
  }

  @Test
  fun resetter_shouldBeGuardedByClassTracker() {
    val content =
      generate(
        shadowInfos = listOf(shadow("java.lang.String", "ShadowString")),
        resetterInfos =
          listOf(ResetterInfo("ShadowString.reset();", -1, -1, "ShadowString", "java.lang.String")),
      )
    assertThat(content)
      .contains("public void reset(org.robolectric.internal.ClassTracker classTracker) {")
    assertThat(content)
      .contains(
        "if (classTracker.isClassLoaded(\"ShadowString\") " +
          "|| classTracker.isClassLoaded(\"java.lang.String\")) {"
      )
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
   * A `shadowOf()` helper is emitted for SDK-visible shadows of a public class (mirroring the javac
   * processor), but not when `isInAndroidSdk = false`. The shadow is still registered in `SHADOWS`
   * either way (see [shadowWithIsNotInAndroidSdk_stillInShadowsList]).
   */
  @Test
  fun shadowOf_emittedForSdkVisibleShadow_butNotWhenExcluded() {
    val visibleShadow =
      shadow("com.example.objects.Dummy", "org.robolectric.ShadowDummy", isInAndroidSdk = true)
    val excludedShadow =
      shadow("com.example.objects.Dummy", "org.robolectric.ShadowDummy", isInAndroidSdk = false)
    val contentVisible = generate(shadowInfos = listOf(visibleShadow))
    val contentExcluded = generate(shadowInfos = listOf(excludedShadow))
    assertThat(contentVisible)
      .contains(
        "public static org.robolectric.ShadowDummy shadowOf(com.example.objects.Dummy actual)"
      )
    assertThat(contentVisible).contains("return org.robolectric.shadow.api.Shadow.extract(actual);")
    assertThat(contentExcluded).doesNotContain("shadowOf(")
  }

  /** No `shadowOf()` is emitted when the shadowed class could not be resolved as a public class. */
  @Test
  fun shadowOf_notEmittedWhenActualNotPublic() {
    val content =
      generate(
        shadowInfos =
          listOf(
            shadow(
              "com.example.objects.Dummy",
              "org.robolectric.ShadowDummy",
              isInAndroidSdk = true,
              actualIsPublic = false,
            )
          )
      )
    assertThat(content).doesNotContain("shadowOf(")
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
