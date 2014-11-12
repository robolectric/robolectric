package org.robolectric.annotation.processing;

import static org.truth0.Truth.ASSERT;
import static com.google.testing.compile.JavaFileObjects.*;
import static com.google.testing.compile.JavaSourceSubjectFactory.javaSource;
import static com.google.testing.compile.JavaSourcesSubjectFactory.javaSources;
import static org.robolectric.annotation.processing.Utils.ROBO_SOURCE;
import static org.robolectric.annotation.processing.Utils.SHADOW_EXTRACTOR_SOURCE;

import org.junit.Ignore;
import org.junit.Test;

import com.google.common.collect.ImmutableList;

public class RoboProcessorTest {

  @Test
  public void unannotatedSource_shouldCompile() {
    ASSERT.about(javaSources())
      .that(ImmutableList.of(
          ROBO_SOURCE,
          SHADOW_EXTRACTOR_SOURCE,
          forSourceString("HelloWorld", "final class HelloWorld {}")))
      .processedWith(new RoboProcessor())
      .compilesWithoutError();
  }

  @Test
  public void generatedFile_shouldHandleInnerClassCollisions() {
    // Because the Generated annotation has a retention of "source", it can't
    // be tested by a unit test - must run a source-level test.
    ASSERT.about(javaSources())
      .that(ImmutableList.of(
          ROBO_SOURCE,
          SHADOW_EXTRACTOR_SOURCE,
          forResource("org/robolectric/annotation/processing/shadows/ShadowDummy.java"),
          forResource("org/robolectric/annotation/processing/shadows/ShadowOuterDummy.java"),
          forResource("org/robolectric/annotation/processing/shadows/ShadowUniqueDummy.java")))
      .processedWith(new RoboProcessor())
      .compilesWithoutError()
      .and()
      .generatesSources(forResource("org/robolectric/Robolectric_InnerClassCollision.java"));
  }

  @Test
  public void generatedFile_shouldSkipNonPublicClasses() {
    ASSERT.about(javaSources())
      .that(ImmutableList.of(
          ROBO_SOURCE,
          SHADOW_EXTRACTOR_SOURCE,
          forResource("org/robolectric/annotation/processing/shadows/ShadowPrivate.java"),
          forResource("org/robolectric/annotation/processing/shadows/ShadowOuterDummy2.java"),
          forResource("org/robolectric/annotation/processing/shadows/ShadowDummy.java")))
      .processedWith(new RoboProcessor())
      .compilesWithoutError()
      .and()
      .generatesSources(forResource("org/robolectric/Robolectric_HiddenClasses.java"));
  }
  
  @Test
  public void generatedFile_shouldHandleAnythingShadows() {
    ASSERT.about(javaSources())
      .that(ImmutableList.of(
          ROBO_SOURCE,
          SHADOW_EXTRACTOR_SOURCE,
          forResource("org/robolectric/annotation/processing/shadows/ShadowAnything.java"),
          forResource("org/robolectric/annotation/processing/shadows/ShadowDummy.java")))
      .processedWith(new RoboProcessor())
      .compilesWithoutError()
      .and()
      .generatesSources(forResource("org/robolectric/Robolectric_Anything.java"));
  }
  
  @Test
  public void generatedFile_shouldHandleClassNameOnlyShadows() {
    ASSERT.about(javaSources())
      .that(ImmutableList.of(
          SHADOW_EXTRACTOR_SOURCE,
          forResource("org/robolectric/annotation/processing/shadows/ShadowClassNameOnly.java"),
          forResource("org/robolectric/annotation/processing/shadows/ShadowDummy.java")))
      .processedWith(new RoboProcessor())
      .compilesWithoutError()
      .and()
      .generatesSources(forResource("org/robolectric/Robolectric_ClassNameOnly.java"));
  }
  
  @Test
  public void shouldGracefullyHandleUnrecognisedAnnotation() {
    ASSERT.about(javaSources())
      .that(ImmutableList.of(
          ROBO_SOURCE,
          SHADOW_EXTRACTOR_SOURCE,
          forResource("org/robolectric/annotation/TestWithUnrecognizedAnnotation.java")))
      .processedWith(new RoboProcessor())
      .compilesWithoutError();
  }

  @Test
  public void shouldGracefullyHandleNoAnythingClass_withNoRealObject() {
    ASSERT.about(javaSource())
      .that(forResource("org/robolectric/annotation/processing/shadows/ShadowAnything.java"))
      .processedWith(new RoboProcessor())
      .failsToCompile();
  }

  @Test
  public void shouldGracefullyHandleNoAnythingClass_withFoundOnImplementsAnnotation() {
    ASSERT.about(javaSources())
      .that(ImmutableList.of(
          SHADOW_EXTRACTOR_SOURCE,
          forResource("org/robolectric/annotation/processing/shadows/ShadowRealObjectWithCorrectAnything.java")))
      .processedWith(new RoboProcessor())
      .failsToCompile();
  }

  @Ignore("feature not yet implemented")
  @Test
  public void shouldGenerateGenericShadowOf() {
    ASSERT.about(javaSources())
      .that(ImmutableList.of(
          ROBO_SOURCE,
          SHADOW_EXTRACTOR_SOURCE,
          forResource("org/robolectric/annotation/processing/shadows/ShadowDummy.java"),
          forResource("org/robolectric/annotation/processing/shadows/ShadowParameterizedDummy.java")))
      .processedWith(new RoboProcessor())
      .compilesWithoutError()
      .and()
      .generatesSources(forResource("org/robolectric/Robolectric_Parameterized.java"));
  }
}
