package org.robolectric.annotation.processing.validator;

import static com.google.common.truth.Truth.assertAbout;
import static com.google.testing.compile.JavaFileObjects.forResource;
import static com.google.testing.compile.JavaSourcesSubjectFactory.javaSources;
import static org.robolectric.annotation.processing.validator.SingleClassSubject.singleClass;
import static org.robolectric.annotation.processing.validator.Utils.SHADOW_EXTRACTOR_SOURCE;

import com.google.common.collect.ImmutableList;
import org.junit.Test;
import org.robolectric.annotation.processing.RobolectricProcessor;

public class RealObjectValidatorTest {
  @Test
  public void realObjectWithoutImplements_shouldNotCompile() {
    final String testClass = "org.robolectric.annotation.processing.shadows.ShadowRealObjectWithoutImplements";
    assertAbout(singleClass())
      .that(testClass)
      .failsToCompile()
      .withErrorContaining("@RealObject without @Implements")
      .onLine(7);
  }

  @Test
  public void realObjectParameterizedMissingParameters_shouldNotCompile() {
    final String testClass = "org.robolectric.annotation.processing.shadows.ShadowRealObjectParameterizedMissingParameters";
    assertAbout(singleClass())
      .that(testClass)
      .failsToCompile()
      .withErrorContaining("@RealObject is missing type parameters")
      .onLine(11);
  }

  @Test
  public void realObjectParameterizedMismatch_shouldNotCompile() {
    final String testClass = "org.robolectric.annotation.processing.shadows.ShadowRealObjectParameterizedMismatch";
    assertAbout(singleClass())
      .that(testClass)
      .failsToCompile()
      .withErrorContaining("Parameter type mismatch: expecting <T,S>, was <S,T>")
      .onLine(11);
  }

  @Test
  public void realObjectWithEmptyImplements_shouldNotRaiseOwnError() {
    final String testClass = "org.robolectric.annotation.processing.shadows.ShadowRealObjectWithEmptyImplements";
    assertAbout(singleClass())
      .that(testClass)
      .failsToCompile()
      .withNoErrorContaining("@RealObject");
  }

  @Test
  public void realObjectWithMissingClassName_shouldNotRaiseOwnError() {
    final String testClass = "org.robolectric.annotation.processing.shadows.ShadowRealObjectWithMissingClassName";
    assertAbout(singleClass())
      .that(testClass)
      .failsToCompile()
      .withNoErrorContaining("@RealObject");
  }

  @Test
  public void realObjectWithEmptyClassNameNoAnything_shouldNotRaiseOwnError() {
    final String testClass = "org.robolectric.annotation.processing.shadows.ShadowRealObjectWithEmptyClassNameNoAnything";
    assertAbout(singleClass())
      .that(testClass)
      .failsToCompile()
      .withNoErrorContaining("@RealObject");
  }

  @Test
  public void realObjectWithTypeMismatch_shouldNotCompile() {
    final String testClass = "org.robolectric.annotation.processing.shadows.ShadowRealObjectWithWrongType";
    assertAbout(singleClass())
      .that(testClass)
      .failsToCompile()
      .withErrorContaining("@RealObject with type <com.example.objects.UniqueDummy>; expected <com.example.objects.Dummy>")
      .onLine(11);
  }

  @Test
  public void realObjectWithClassName_typeMismatch_shouldNotCompile() {
    final String testClass = "org.robolectric.annotation.processing.shadows.ShadowRealObjectWithIncorrectClassName";
    assertAbout(singleClass())
      .that(testClass)
      .failsToCompile()
      .withErrorContaining("@RealObject with type <com.example.objects.UniqueDummy>; expected <com.example.objects.Dummy>")
      .onLine(10);
  }

  @Test
  public void realObjectWithCorrectType_shouldCompile() {
    final String testClass = "org.robolectric.annotation.processing.shadows.ShadowRealObjectWithCorrectType";
    assertAbout(singleClass())
      .that(testClass)
      .compilesWithoutError();
  }

  @Test
  public void realObjectWithCorrectType_withoutAnything_shouldCompile() {
    assertAbout(javaSources())
    .that(ImmutableList.of(
        SHADOW_EXTRACTOR_SOURCE,
        forResource("org/robolectric/annotation/processing/shadows/ShadowRealObjectWithCorrectType.java")))
    .processedWith(new RobolectricProcessor())
      .compilesWithoutError();
  }

  @Test
  public void realObjectWithCorrectAnything_shouldCompile() {
    final String testClass = "org.robolectric.annotation.processing.shadows.ShadowRealObjectWithCorrectAnything";
    assertAbout(singleClass())
      .that(testClass)
      .compilesWithoutError();
  }

  @Test
  public void realObjectWithCorrectClassName_shouldCompile() {
    assertAbout(javaSources())
      .that(ImmutableList.of(
          SHADOW_EXTRACTOR_SOURCE,
          forResource("org/robolectric/annotation/processing/shadows/ShadowRealObjectWithCorrectClassName.java")))
      .processedWith(new RobolectricProcessor())
      .compilesWithoutError();
  }
  
  @Test
  public void realObjectWithNestedClassName_shouldCompile() {
    final String testClass = "org.robolectric.annotation.processing.shadows.ShadowRealObjectWithNestedClassName";
    assertAbout(singleClass())
      .that(testClass)
      .compilesWithoutError();
  }
}
