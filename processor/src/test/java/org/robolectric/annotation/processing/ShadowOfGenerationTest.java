package org.robolectric.annotation.processing;

import static com.google.testing.compile.CompilationSubject.assertThat;
import static com.google.testing.compile.Compiler.javac;
import static com.google.testing.compile.JavaFileObjects.forResource;
import static org.robolectric.annotation.processing.Utils.DEFAULT_OPTS;
import static org.robolectric.annotation.processing.Utils.SHADOW_EXTRACTOR_SOURCE;
import static org.robolectric.annotation.processing.Utils.SHADOW_PROVIDER_SOURCE;

import com.google.testing.compile.Compilation;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class ShadowOfGenerationTest {
  @Test
  public void shouldNotGenerateShadowOfMethodsForShadowsWithoutPublicMethods() {
    Compilation compilation =
        javac()
            .withProcessors(new RobolectricProcessor(DEFAULT_OPTS))
            .compile(
                SHADOW_PROVIDER_SOURCE,
                SHADOW_EXTRACTOR_SOURCE,
                forResource(
                    "org/robolectric/annotation/processing/shadows/ShadowNoPublicMethods.java"));
    assertThat(compilation).succeeded();
    assertThat(compilation)
        .generatedSourceFile("org.robolectric.Shadows")
        .hasSourceEquivalentTo(
            Utils.sourceResource("org/robolectric/Robolectric_NoShadowOf.java.txt"));
  }

  @Test
  public void shouldGenerateShadowOfMethodsForShadowsWithPublicMethods() {
    Compilation compilation =
        javac()
            .withProcessors(new RobolectricProcessor(DEFAULT_OPTS))
            .compile(
                SHADOW_PROVIDER_SOURCE,
                SHADOW_EXTRACTOR_SOURCE,
                forResource(
                    "org/robolectric/annotation/processing/shadows/ShadowWithPublicMethods.java"));
    assertThat(compilation).succeeded();
    assertThat(compilation)
        .generatedSourceFile("org.robolectric.Shadows")
        .hasSourceEquivalentTo(
            Utils.sourceResource("org/robolectric/Robolectric_WithShadowOf.java.txt"));
  }

  @Test
  public void shouldGenerateShadowOfMethodsForShadowsWithInheritedPublicMethods() {
    Compilation compilation =
        javac()
            .withProcessors(new RobolectricProcessor(DEFAULT_OPTS))
            .compile(
                SHADOW_PROVIDER_SOURCE,
                SHADOW_EXTRACTOR_SOURCE,
                forResource(
                    "org/robolectric/annotation/processing/shadows/ShadowWithPublicMethods.java"),
                forResource(
                    "org/robolectric/annotation/processing/shadows/ShadowSubclassNoPublicMethods.java"));
    assertThat(compilation).succeeded();
    assertThat(compilation)
        .generatedSourceFile("org.robolectric.Shadows")
        .hasSourceEquivalentTo(
            Utils.sourceResource("org/robolectric/Robolectric_WithInheritedShadowOf.java.txt"));
  }
}
