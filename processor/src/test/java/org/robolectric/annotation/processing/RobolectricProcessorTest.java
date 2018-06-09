package org.robolectric.annotation.processing;

import static com.google.common.truth.Truth.assertAbout;
import static com.google.common.truth.Truth.assertThat;
import static com.google.testing.compile.JavaFileObjects.forResource;
import static com.google.testing.compile.JavaFileObjects.forSourceString;
import static com.google.testing.compile.JavaSourcesSubjectFactory.javaSources;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.robolectric.annotation.processing.RobolectricProcessor.JSON_DOCS_DIR;
import static org.robolectric.annotation.processing.RobolectricProcessor.PACKAGE_OPT;
import static org.robolectric.annotation.processing.RobolectricProcessor.SHOULD_INSTRUMENT_PKG_OPT;
import static org.robolectric.annotation.processing.Utils.DEFAULT_OPTS;
import static org.robolectric.annotation.processing.Utils.SHADOW_EXTRACTOR_SOURCE;
import static org.robolectric.annotation.processing.Utils.SHADOW_PROVIDER_SOURCE;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.Files;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class RobolectricProcessorTest {
  @Test
  public void robolectricProcessor_supportsPackageOption() {
    assertThat(new RobolectricProcessor(DEFAULT_OPTS).getSupportedOptions()).contains(PACKAGE_OPT);
  }

  @Test
  public void robolectricProcessor_supportsShouldInstrumentPackageOption() {
    assertThat(
        new RobolectricProcessor(DEFAULT_OPTS).getSupportedOptions()).contains(SHOULD_INSTRUMENT_PKG_OPT);
  }

  @Test
  public void unannotatedSource_shouldCompile() {
    assertAbout(javaSources())
      .that(ImmutableList.of(
          SHADOW_PROVIDER_SOURCE,
          SHADOW_EXTRACTOR_SOURCE,
          forSourceString("HelloWorld", "final class HelloWorld {}")))
      .processedWith(new RobolectricProcessor(DEFAULT_OPTS))
      .compilesWithoutError();
    //.and().generatesNoSources(); Should add this assertion onces
    // it becomes available in compile-testing
  }

  @Test
  public void generatedFile_shouldHandleInnerClassCollisions() {
    // Because the Generated annotation has a retention of "source", it can't
    // be tested by a unit test - must run a source-level test.
    assertAbout(javaSources())
      .that(ImmutableList.of(
          SHADOW_PROVIDER_SOURCE,
          SHADOW_EXTRACTOR_SOURCE,
          forResource("org/robolectric/annotation/processing/shadows/ShadowDummy.java"),
          forResource("org/robolectric/annotation/processing/shadows/ShadowOuterDummy.java"),
          forResource("org/robolectric/annotation/processing/shadows/ShadowUniqueDummy.java")))
      .processedWith(new RobolectricProcessor(DEFAULT_OPTS))
      .compilesWithoutError()
      .and()
      .generatesSources(forResource("org/robolectric/Robolectric_InnerClassCollision.java"));
  }

  @Test
  public void generatedFile_shouldHandleNonPublicClasses() {
    assertAbout(javaSources())
      .that(ImmutableList.of(
          SHADOW_PROVIDER_SOURCE,
          SHADOW_EXTRACTOR_SOURCE,
          forResource("org/robolectric/annotation/processing/shadows/ShadowPrivate.java"),
          forResource("org/robolectric/annotation/processing/shadows/ShadowOuterDummy2.java"),
          forResource("org/robolectric/annotation/processing/shadows/ShadowDummy.java")))
      .processedWith(new RobolectricProcessor(DEFAULT_OPTS))
      .compilesWithoutError()
      .and()
      .generatesSources(forResource("org/robolectric/Robolectric_HiddenClasses.java"));
  }

  @Test
  public void generatedFile_shouldComplainAboutNonStaticInnerClasses() {
    assertAbout(javaSources())
      .that(ImmutableList.of(
          SHADOW_PROVIDER_SOURCE,
          SHADOW_EXTRACTOR_SOURCE,
          forResource("org/robolectric/annotation/processing/shadows/ShadowOuterDummyWithErrs.java")))
      .processedWith(new RobolectricProcessor(DEFAULT_OPTS))
      .failsToCompile()
      .withErrorContaining("inner shadow classes must be static");
  }

  @Test
  public void generatedFile_shouldHandleClassNameOnlyShadows() {
    assertAbout(javaSources())
      .that(ImmutableList.of(
          SHADOW_PROVIDER_SOURCE,
          SHADOW_EXTRACTOR_SOURCE,
          forResource("org/robolectric/annotation/processing/shadows/ShadowClassNameOnly.java"),
          forResource("org/robolectric/annotation/processing/shadows/ShadowDummy.java")))
      .processedWith(new RobolectricProcessor(DEFAULT_OPTS))
      .compilesWithoutError()
      .and()
      .generatesSources(forResource("org/robolectric/Robolectric_ClassNameOnly.java"));
  }

  @Test
  public void generatedFile_shouldNotGenerateShadowOfMethodsForExcludedClasses() {
    assertAbout(javaSources())
        .that(ImmutableList.of(
            SHADOW_PROVIDER_SOURCE,
            SHADOW_EXTRACTOR_SOURCE,
            forResource("org/robolectric/annotation/processing/shadows/ShadowExcludedFromAndroidSdk.java")))
        .processedWith(new RobolectricProcessor(DEFAULT_OPTS))
        .compilesWithoutError()
        .and()
        .generatesSources(forResource("org/robolectric/Robolectric_NoExcludedTypes.java"));
  }

  @Test
  public void generatedFile_shouldUseSpecifiedPackage() throws IOException {
    StringBuilder expected = new StringBuilder();
    InputStream in = RobolectricProcessorTest.class.getClassLoader()
        .getResourceAsStream("org/robolectric/Robolectric_ClassNameOnly.java");
    BufferedReader reader = new BufferedReader(new InputStreamReader(in, UTF_8));

    String line;
    while ((line = reader.readLine()) != null) {
      expected.append(line).append("\n");
    }
    line = expected.toString();
    line = line.replace("package org.robolectric", "package my.test.pkg");

    ImmutableMap<String, String> opts =
        ImmutableMap.of(
            PACKAGE_OPT, "my.test.pkg", JSON_DOCS_DIR, Files.createTempDir().toString());

    assertAbout(javaSources())
      .that(ImmutableList.of(
          SHADOW_PROVIDER_SOURCE,
          SHADOW_EXTRACTOR_SOURCE,
          forResource("org/robolectric/annotation/processing/shadows/ShadowClassNameOnly.java"),
          forResource("org/robolectric/annotation/processing/shadows/ShadowDummy.java")))
      .processedWith(new RobolectricProcessor(opts))
      .compilesWithoutError()
      .and()
      .generatesSources(forSourceString("Shadows", line));
  }

  @Test
  public void shouldGenerateMetaInfServicesFile() {
    assertAbout(javaSources())
        .that(ImmutableList.of(
            SHADOW_PROVIDER_SOURCE,
            SHADOW_EXTRACTOR_SOURCE,
            forResource("org/robolectric/annotation/processing/shadows/ShadowClassNameOnly.java"),
            forResource("org/robolectric/annotation/processing/shadows/ShadowDummy.java")))
        .processedWith(new RobolectricProcessor(DEFAULT_OPTS))
        .compilesWithoutError()
        .and()
        .generatesFiles(forResource("META-INF/services/org.robolectric.internal.ShadowProvider"));
  }

  @Test
  public void shouldGracefullyHandleUnrecognisedAnnotation() {
    assertAbout(javaSources())
      .that(ImmutableList.of(
          SHADOW_PROVIDER_SOURCE,
          SHADOW_EXTRACTOR_SOURCE,
          forResource("org/robolectric/annotation/TestWithUnrecognizedAnnotation.java")))
      .processedWith(new RobolectricProcessor(DEFAULT_OPTS))
      .compilesWithoutError();
  }

  @Test
  public void shouldGenerateGenericShadowOf() {
    assertAbout(javaSources())
      .that(ImmutableList.of(
          SHADOW_PROVIDER_SOURCE,
          SHADOW_EXTRACTOR_SOURCE,
          forResource("org/robolectric/annotation/processing/shadows/ShadowDummy.java"),
          forResource("org/robolectric/annotation/processing/shadows/ShadowParameterizedDummy.java")))
      .processedWith(new RobolectricProcessor(DEFAULT_OPTS))
      .compilesWithoutError()
      .and()
      .generatesSources(forResource("org/robolectric/Robolectric_Parameterized.java"));
  }

  @Test
  public void generatedShadowProvider_canConfigureInstrumentingPackages() {
    Map<String, String> options = new HashMap<>(DEFAULT_OPTS);
    options.put(SHOULD_INSTRUMENT_PKG_OPT, "false");

    assertAbout(javaSources())
    .that(ImmutableList.of(
        SHADOW_PROVIDER_SOURCE,
        SHADOW_EXTRACTOR_SOURCE,
        forResource("org/robolectric/annotation/processing/shadows/ShadowDummy.java")))
    .processedWith(new RobolectricProcessor(options))
    .compilesWithoutError()
    .and()
    .generatesSources(forResource("org/robolectric/Robolectric_EmptyProvidedPackageNames.java"));
  }
}
