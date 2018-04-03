package org.robolectric.annotation.processing.validator;

import static com.google.common.truth.Truth.assertAbout;
import static org.robolectric.annotation.processing.validator.SingleClassSubject.singleClass;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests for {@link ResetterValidator} */
@RunWith(JUnit4.class)
public class ResetterValidatorTest {
  @Test
  public void resetterWithoutImplements_shouldNotCompile() {
    final String testClass = "org.robolectric.annotation.processing.shadows.ShadowResetterWithoutImplements";
    assertAbout(singleClass())
      .that(testClass)
      .failsToCompile()
      .withErrorContaining("@Resetter without @Implements")
      .onLine(7);
  }

  @Test
  public void nonStaticResetter_shouldNotCompile() {
    final String testClass = "org.robolectric.annotation.processing.shadows.ShadowResetterNonStatic";
    assertAbout(singleClass())
      .that(testClass)
      .failsToCompile()
      .withErrorContaining("@Resetter methods must be static")
      .onLine(10);
  }

  @Test
  public void nonPublicResetter_shouldNotCompile() {
    final String testClass = "org.robolectric.annotation.processing.shadows.ShadowResetterNonPublic";
    assertAbout(singleClass())
      .that(testClass)
      .failsToCompile()
      .withErrorContaining("@Resetter methods must be public")
      .onLine(10);
  }

  @Test
  public void resetterWithParameters_shouldNotCompile() {
    final String testClass = "org.robolectric.annotation.processing.shadows.ShadowResetterWithParameters";
    assertAbout(singleClass())
      .that(testClass)
      .failsToCompile()
      .withErrorContaining("@Resetter methods must not have parameters")
      .onLine(11);
  }

  @Test
  public void goodResetter_shouldCompile() {
    final String testClass = "org.robolectric.annotation.processing.shadows.ShadowDummy";
    assertAbout(singleClass())
      .that(testClass)
      .compilesWithoutError();
  }
}
