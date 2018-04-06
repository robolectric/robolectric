package org.robolectric.annotation.processing.validator;

import static com.google.common.truth.Truth.assertAbout;
import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.annotation.processing.validator.SingleClassSubject.singleClass;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.robolectric.annotation.processing.DocumentedMethod;

/** Tests for {@link ImplementsValidator */
@RunWith(JUnit4.class)
public class ImplementsValidatorTest {
  @Test
  public void implementsWithoutClassOrClassName_shouldNotCompile() {
    final String testClass = "org.robolectric.annotation.processing.shadows.ShadowImplementsWithoutClass";
    assertAbout(singleClass())
      .that(testClass)
      .failsToCompile()
      .withErrorContaining("@Implements: must specify <value> or <className>")
      .onLine(5);
  }

  @Test
  public void value_withUnresolvableClassNameAndOldMaxSdk_shouldNotCompile() {
    final String testClass =
        "org.robolectric.annotation.processing.shadows.ShadowWithUnresolvableClassNameAndOldMaxSdk";
    assertAbout(singleClass())
        .that(testClass)
        .compilesWithoutError();
  }

  @Test
  public void value_withClassName_shouldNotCompile() {
    final String testClass = "org.robolectric.annotation.processing.shadows.ShadowImplementsDummyWithOuterDummyClassName";
    assertAbout(singleClass())
      .that(testClass)
      .failsToCompile()
      .withErrorContaining("@Implements: cannot specify both <value> and <className> attributes")
      .onLine(6);
  }

  @Test
  public void implementsWithParameterMismatch_shouldNotCompile() {
    final String testClass = "org.robolectric.annotation.processing.shadows.ShadowImplementsWithParameterMismatch";
    assertAbout(singleClass())
      .that(testClass)
      .failsToCompile()
      .withErrorContaining("Shadow type must have same type parameters as its real counterpart: expected <T,N extends java.lang.Number>, was <N extends java.lang.Number,T>")
      .onLine(7);
  }

  @Test
  public void implementsWithMissingParameters_shouldNotCompile() {
    final String testClass = "org.robolectric.annotation.processing.shadows.ShadowImplementsWithMissingParameters";
    assertAbout(singleClass())
      .that(testClass)
      .failsToCompile()
      .withErrorContaining("Shadow type is missing type parameters, expected <T,N extends java.lang.Number>")
      .onLine(7);
  }

  @Test
  public void implementsWithExtraParameters_shouldNotCompile() {
    final String testClass = "org.robolectric.annotation.processing.shadows.ShadowImplementsWithExtraParameters";
    assertAbout(singleClass())
      .that(testClass)
      .failsToCompile()
      .withErrorContaining("Shadow type has type parameters but real type does not")
      .onLine(7);
  }

  @Test
  public void constructorShadowWithoutImplementation_shouldNotCompile() {
    final String testClass =
        "org.robolectric.annotation.processing.shadows.ShadowWithImplementationlessShadowMethods";
    assertAbout(singleClass())
        .that(testClass)
        .failsToCompile()
        .withErrorContaining("Shadow methods must be annotated @Implementation")
        .onLine(8)
        .and()
        .withErrorContaining("Shadow methods must be annotated @Implementation")
        .onLine(10);
  }

  @Test
  public void javadocMarkdownFormatting() throws Exception {
    DocumentedMethod documentedMethod = new DocumentedMethod("name");
    documentedMethod.setDocumentation(
        " First sentence.\n \n Second sentence.\n \n ASCII art:\n   *  *  *\n @return null\n"
    );

    assertThat(documentedMethod.getDocumentation())
        .isEqualTo("First sentence.\n\nSecond sentence.\n\nASCII art:\n  *  *  *\n@return null\n");
  }
}
