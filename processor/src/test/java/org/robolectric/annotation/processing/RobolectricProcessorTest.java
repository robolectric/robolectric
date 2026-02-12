package org.robolectric.annotation.processing;

import static com.google.common.truth.Truth.assertThat;
import static com.google.testing.compile.CompilationSubject.assertThat;
import static com.google.testing.compile.Compiler.javac;
import static com.google.testing.compile.JavaFileObjects.forResource;
import static com.google.testing.compile.JavaFileObjects.forSourceString;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.robolectric.annotation.processing.RobolectricProcessor.JSON_DOCS_DIR;
import static org.robolectric.annotation.processing.RobolectricProcessor.PACKAGE_OPT;
import static org.robolectric.annotation.processing.RobolectricProcessor.SHOULD_INSTRUMENT_PKG_OPT;
import static org.robolectric.annotation.processing.Utils.DEFAULT_OPTS;
import static org.robolectric.annotation.processing.Utils.SHADOW_EXTRACTOR_SOURCE;
import static org.robolectric.annotation.processing.Utils.SHADOW_PROVIDER_SOURCE;

import com.google.common.collect.ImmutableMap;
import com.google.common.io.Files;
import com.google.testing.compile.Compilation;
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
    assertThat(new RobolectricProcessor(DEFAULT_OPTS).getSupportedOptions())
        .contains(SHOULD_INSTRUMENT_PKG_OPT);
  }

  @Test
  public void unannotatedSource_shouldCompile() {
    Compilation compilation =
        javac()
            .withProcessors(new RobolectricProcessor(DEFAULT_OPTS))
            .compile(
                SHADOW_PROVIDER_SOURCE,
                SHADOW_EXTRACTOR_SOURCE,
                forSourceString("HelloWorld", "final class HelloWorld {}"));
    assertThat(compilation).succeeded();
  }

  @Test
  public void generatedFile_shouldHandleInnerClassCollisions() {
    // Because the Generated annotation has a retention of "source", it can't
    // be tested by a unit test - must run a source-level test.
    Compilation compilation =
        javac()
            .withProcessors(new RobolectricProcessor(DEFAULT_OPTS))
            .compile(
                SHADOW_PROVIDER_SOURCE,
                SHADOW_EXTRACTOR_SOURCE,
                forResource("org/robolectric/annotation/processing/shadows/ShadowDummy.java"),
                forResource("org/robolectric/annotation/processing/shadows/ShadowOuterDummy.java"),
                forResource(
                    "org/robolectric/annotation/processing/shadows/ShadowUniqueDummy.java"));
    assertThat(compilation).succeeded();
    assertThat(compilation)
        .generatedSourceFile("org.robolectric.Shadows")
        .hasSourceEquivalentTo(
            Utils.sourceResource("org/robolectric/Robolectric_InnerClassCollision.java.txt"));
  }

  @Test
  public void generatedFile_shouldHandleNonPublicClasses() {
    Compilation compilation =
        javac()
            .withProcessors(new RobolectricProcessor(DEFAULT_OPTS))
            .compile(
                SHADOW_PROVIDER_SOURCE,
                SHADOW_EXTRACTOR_SOURCE,
                forResource("org/robolectric/annotation/processing/shadows/ShadowPrivate.java"),
                forResource("org/robolectric/annotation/processing/shadows/ShadowOuterDummy2.java"),
                forResource("org/robolectric/annotation/processing/shadows/ShadowDummy.java"));
    assertThat(compilation).succeeded();
    assertThat(compilation)
        .generatedSourceFile("org.robolectric.Shadows")
        .hasSourceEquivalentTo(
            Utils.sourceResource("org/robolectric/Robolectric_HiddenClasses.java.txt"));
  }

  @Test
  public void generatedFile_shouldComplainAboutNonStaticInnerClasses() {
    Compilation compilation =
        javac()
            .withProcessors(new RobolectricProcessor(DEFAULT_OPTS))
            .compile(
                SHADOW_PROVIDER_SOURCE,
                SHADOW_EXTRACTOR_SOURCE,
                forResource(
                    "org/robolectric/annotation/processing/shadows/ShadowOuterDummyWithErrs.java"));
    assertThat(compilation).failed();
    assertThat(compilation).hadErrorContaining("inner shadow classes must be static");
  }

  @Test
  public void generatedFile_shouldHandleClassNameOnlyShadows() {
    Compilation compilation =
        javac()
            .withProcessors(new RobolectricProcessor(DEFAULT_OPTS))
            .compile(
                SHADOW_PROVIDER_SOURCE,
                SHADOW_EXTRACTOR_SOURCE,
                forResource(
                    "org/robolectric/annotation/processing/shadows/ShadowClassNameOnly.java"),
                forResource("org/robolectric/annotation/processing/shadows/ShadowDummy.java"));
    assertThat(compilation).succeeded();
    assertThat(compilation)
        .generatedSourceFile("org.robolectric.Shadows")
        .hasSourceEquivalentTo(
            Utils.sourceResource("org/robolectric/Robolectric_ClassNameOnly.java.txt"));
  }

  @Test
  public void generatedFile_shouldNotGenerateShadowOfMethodsForExcludedClasses() {
    Compilation compilation =
        javac()
            .withProcessors(new RobolectricProcessor(DEFAULT_OPTS))
            .compile(
                SHADOW_PROVIDER_SOURCE,
                SHADOW_EXTRACTOR_SOURCE,
                forResource(
                    "org/robolectric/annotation/processing/shadows/ShadowExcludedFromAndroidSdk.java"));
    assertThat(compilation).succeeded();
    assertThat(compilation)
        .generatedSourceFile("org.robolectric.Shadows")
        .hasSourceEquivalentTo(
            Utils.sourceResource("org/robolectric/Robolectric_NoExcludedTypes.java.txt"));
  }

  @Test
  public void generatedFile_shouldUseSpecifiedPackage() throws IOException {
    StringBuilder expected = new StringBuilder();
    InputStream in =
        RobolectricProcessorTest.class
            .getClassLoader()
            .getResourceAsStream("org/robolectric/Robolectric_ClassNameOnly.java.txt");
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

    Compilation compilation =
        javac()
            .withProcessors(new RobolectricProcessor(opts))
            .compile(
                SHADOW_PROVIDER_SOURCE,
                SHADOW_EXTRACTOR_SOURCE,
                forResource(
                    "org/robolectric/annotation/processing/shadows/ShadowClassNameOnly.java"),
                forResource("org/robolectric/annotation/processing/shadows/ShadowDummy.java"));
    assertThat(compilation).succeeded();
    assertThat(compilation)
        .generatedSourceFile("my.test.pkg.Shadows")
        .hasSourceEquivalentTo(forSourceString("my.test.pkg.Shadows", line));
  }

  @Test
  public void shouldGenerateMetaInfServicesFile() {
    Compilation compilation =
        javac()
            .withProcessors(new RobolectricProcessor(DEFAULT_OPTS))
            .compile(
                SHADOW_PROVIDER_SOURCE,
                SHADOW_EXTRACTOR_SOURCE,
                forResource(
                    "org/robolectric/annotation/processing/shadows/ShadowClassNameOnly.java"),
                forResource("org/robolectric/annotation/processing/shadows/ShadowDummy.java"));
    assertThat(compilation).succeeded();
    assertThat(compilation)
        .generatedFile(
            javax.tools.StandardLocation.CLASS_OUTPUT,
            "META-INF/services/org.robolectric.internal.ShadowProvider")
        .hasContents(
            com.google.common.io.Resources.asByteSource(
                com.google.common.io.Resources.getResource(
                    "META-INF/services/org.robolectric.internal.ShadowProvider")));
  }

  @Test
  public void shouldGracefullyHandleUnrecognisedAnnotation() {
    Compilation compilation =
        javac()
            .withProcessors(new RobolectricProcessor(DEFAULT_OPTS))
            .compile(
                SHADOW_PROVIDER_SOURCE,
                SHADOW_EXTRACTOR_SOURCE,
                forResource("org/robolectric/annotation/TestWithUnrecognizedAnnotation.java"));
    assertThat(compilation).succeeded();
  }

  @Test
  public void shouldGenerateGenericShadowOf() {
    Compilation compilation =
        javac()
            .withProcessors(new RobolectricProcessor(DEFAULT_OPTS))
            .compile(
                SHADOW_PROVIDER_SOURCE,
                SHADOW_EXTRACTOR_SOURCE,
                forResource("org/robolectric/annotation/processing/shadows/ShadowDummy.java"),
                forResource(
                    "org/robolectric/annotation/processing/shadows/ShadowParameterizedDummy.java"));
    assertThat(compilation).succeeded();
    assertThat(compilation)
        .generatedSourceFile("org.robolectric.Shadows")
        .hasSourceEquivalentTo(
            Utils.sourceResource("org/robolectric/Robolectric_Parameterized.java.txt"));
  }

  @Test
  public void generatedShadowProvider_canConfigureInstrumentingPackages() {
    Map<String, String> options = new HashMap<>(DEFAULT_OPTS);
    options.put(SHOULD_INSTRUMENT_PKG_OPT, "false");

    Compilation compilation =
        javac()
            .withProcessors(new RobolectricProcessor(options))
            .compile(
                SHADOW_PROVIDER_SOURCE,
                SHADOW_EXTRACTOR_SOURCE,
                forResource("org/robolectric/annotation/processing/shadows/ShadowDummy.java"));
    assertThat(compilation).succeeded();
    assertThat(compilation)
        .generatedSourceFile("org.robolectric.Shadows")
        .hasSourceEquivalentTo(
            Utils.sourceResource("org/robolectric/Robolectric_EmptyProvidedPackageNames.java.txt"));
  }

  @Test
  public void generatedShadowProvider_minimalPackageNames() {
    Map<String, String> options = new HashMap<>(DEFAULT_OPTS);
    options.put(SHOULD_INSTRUMENT_PKG_OPT, "true");

    Compilation compilation =
        javac()
            .withProcessors(new RobolectricProcessor(options))
            .compile(
                SHADOW_PROVIDER_SOURCE,
                SHADOW_EXTRACTOR_SOURCE,
                forResource("org/robolectric/annotation/processing/shadows/ShadowDummy.java"),
                forResource("org/robolectric/annotation/processing/shadows/ShadowDummy2.java"),
                forResource(
                    "org/robolectric/annotation/processing/shadows/ShadowInnerPackageDummy.java"),
                forResource(
                    "org/robolectric/annotation/processing/shadows/ShadowClassNameOnly.java"));
    assertThat(compilation).succeeded();
    assertThat(compilation)
        .generatedSourceFile("org.robolectric.Shadows")
        .hasSourceEquivalentTo(
            Utils.sourceResource("org/robolectric/Robolectric_MinimalPackages.java.txt"));
  }

  @Test
  public void shouldEmitShadowPickerMapForShadowedInnerClasses() {
    Map<String, String> options = new HashMap<>(DEFAULT_OPTS);
    options.put(SHOULD_INSTRUMENT_PKG_OPT, "true");

    Compilation compilation =
        javac()
            .withProcessors(new RobolectricProcessor(options))
            .compile(
                SHADOW_PROVIDER_SOURCE,
                SHADOW_EXTRACTOR_SOURCE,
                forResource(
                    "org/robolectric/annotation/processing/shadows/ShadowInnerDummyWithPicker.java"));
    assertThat(compilation).succeeded();
    assertThat(compilation)
        .generatedSourceFile("org.robolectric.Shadows")
        .hasSourceEquivalentTo(
            Utils.sourceResource("org/robolectric/Robolectric_ShadowPickers.java.txt"));
  }
}
