package org.robolectric.annotation.processing;

import static org.truth0.Truth.ASSERT;
import static com.google.testing.compile.JavaFileObjects.*;
import static com.google.testing.compile.JavaSourceSubjectFactory.javaSource;
import static com.google.testing.compile.JavaSourcesSubjectFactory.javaSources;
import static org.robolectric.annotation.processing.RobolectricProcessor.PACKAGE_OPT;
import static org.robolectric.annotation.processing.validator.Utils.ROBO_SOURCE;
import static org.robolectric.annotation.processing.validator.Utils.SHADOW_PROVIDER_SOURCE;
import static org.robolectric.annotation.processing.validator.Utils.SHADOW_EXTRACTOR_SOURCE;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.google.common.collect.ImmutableList;

public class RobolectricProcessorTest {
  private static final Map<String,String> DEFAULT_OPTS = new HashMap<>();
  
  static {
    DEFAULT_OPTS.put(PACKAGE_OPT, "org.robolectric");
  }
	
  @Test
  public void roboProcessor_supportsPackageOption() {
    ASSERT.that(new RobolectricProcessor().getSupportedOptions()).contains(PACKAGE_OPT);
  }
  
  @Test
  public void unannotatedSource_shouldCompile() {
    ASSERT.about(javaSources())
      .that(ImmutableList.of(
          ROBO_SOURCE,
          SHADOW_PROVIDER_SOURCE,
          SHADOW_EXTRACTOR_SOURCE,
          forSourceString("HelloWorld", "final class HelloWorld {}")))
      .processedWith(new RobolectricProcessor())
      .compilesWithoutError();
    //.and().generatesNoSources(); Should add this assertion onces
    // it becomes available in compile-testing
  }

  @Test
  public void generatedFile_shouldHandleInnerClassCollisions() {
    // Because the Generated annotation has a retention of "source", it can't
    // be tested by a unit test - must run a source-level test.
    ASSERT.about(javaSources())
      .that(ImmutableList.of(
          ROBO_SOURCE,
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
  public void generatedFile_shouldSkipNonPublicClasses() {
    ASSERT.about(javaSources())
      .that(ImmutableList.of(
          ROBO_SOURCE,
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
  public void generatedFile_shouldHandleAnythingShadows() {
    ASSERT.about(javaSources())
      .that(ImmutableList.of(
          ROBO_SOURCE,
          SHADOW_PROVIDER_SOURCE,
          SHADOW_EXTRACTOR_SOURCE,
          forResource("org/robolectric/annotation/processing/shadows/ShadowAnything.java"),
          forResource("org/robolectric/annotation/processing/shadows/ShadowDummy.java")))
      .processedWith(new RobolectricProcessor(DEFAULT_OPTS))
      .compilesWithoutError()
      .and()
      .generatesSources(forResource("org/robolectric/Robolectric_Anything.java"));
  }
  
  @Test
  public void generatedFile_shouldHandleClassNameOnlyShadows() {
    ASSERT.about(javaSources())
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
    ASSERT.about(javaSources())
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
    StringBuffer expected = new StringBuffer();
    BufferedReader reader = new BufferedReader(new InputStreamReader(RobolectricProcessorTest.class.getClassLoader().getResourceAsStream("org/robolectric/Robolectric_ClassNameOnly.java")));

    String line;
    while ((line = reader.readLine()) != null) {
      expected.append(line).append("\n");
    }
    line = expected.toString();
    line = line.replace("package org.robolectric", "package my.test.pkg");

    Map<String,String> opts = new HashMap<>();
    opts.put(PACKAGE_OPT, "my.test.pkg");
    
    ASSERT.about(javaSources())
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
    ASSERT.about(javaSources())
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
    ASSERT.about(javaSources())
      .that(ImmutableList.of(
          ROBO_SOURCE,
          SHADOW_PROVIDER_SOURCE,
          SHADOW_EXTRACTOR_SOURCE,
          forResource("org/robolectric/annotation/TestWithUnrecognizedAnnotation.java")))
      .processedWith(new RobolectricProcessor())
      .compilesWithoutError();
  }

  @Test
  public void shouldGracefullyHandleNoAnythingClass_withNoRealObject() {
    ASSERT.about(javaSource())
      .that(forResource("org/robolectric/annotation/processing/shadows/ShadowAnything.java"))
      .processedWith(new RobolectricProcessor())
      .failsToCompile();
  }

  @Test
  public void shouldGracefullyHandleNoAnythingClass_withFoundOnImplementsAnnotation() {
    ASSERT.about(javaSources())
      .that(ImmutableList.of(
          SHADOW_PROVIDER_SOURCE,
          SHADOW_EXTRACTOR_SOURCE,
          forResource("org/robolectric/annotation/processing/shadows/ShadowRealObjectWithCorrectAnything.java")))
      .processedWith(new RobolectricProcessor())
      .failsToCompile();
  }

  @Test
  public void shouldGenerateGenericShadowOf() {
    ASSERT.about(javaSources())
      .that(ImmutableList.of(
          ROBO_SOURCE,
          SHADOW_PROVIDER_SOURCE,
          SHADOW_EXTRACTOR_SOURCE,
          forResource("org/robolectric/annotation/processing/shadows/ShadowDummy.java"),
          forResource("org/robolectric/annotation/processing/shadows/ShadowParameterizedDummy.java")))
      .processedWith(new RobolectricProcessor(DEFAULT_OPTS))
      .compilesWithoutError()
      .and()
      .generatesSources(forResource("org/robolectric/Robolectric_Parameterized.java"));
  }
}
