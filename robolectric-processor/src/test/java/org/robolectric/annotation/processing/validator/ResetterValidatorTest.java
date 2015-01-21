package org.robolectric.annotation.processing.validator;

import static org.truth0.Truth.ASSERT;
import static org.robolectric.annotation.processing.validator.SingleClassSubject.singleClass;

import org.junit.Test;

public class ResetterValidatorTest {
  @Test
  public void resetterWithoutImplements_shouldNotCompile() {
    final String testClass = "org.robolectric.annotation.processing.shadows.ShadowResetterWithoutImplements";
    ASSERT.about(singleClass())
      .that(testClass)
      .failsToCompile()
      .withErrorContaining("@Resetter without @Implements")
      .onLine(7);
  }

  @Test
  public void nonStaticResetter_shouldNotCompile() {
    final String testClass = "org.robolectric.annotation.processing.shadows.ShadowResetterNonStatic";
    ASSERT.about(singleClass())
      .that(testClass)
      .failsToCompile()
      .withErrorContaining("@Resetter methods must be static")
      .onLine(10);
  }

  @Test
  public void nonPublicResetter_shouldNotCompile() {
    final String testClass = "org.robolectric.annotation.processing.shadows.ShadowResetterNonPublic";
    ASSERT.about(singleClass())
      .that(testClass)
      .failsToCompile()
      .withErrorContaining("@Resetter methods must be public")
      .onLine(10);
  }

  @Test
  public void resetterWithParameters_shouldNotCompile() {
    final String testClass = "org.robolectric.annotation.processing.shadows.ShadowResetterWithParameters";
    ASSERT.about(singleClass())
      .that(testClass)
      .failsToCompile()
      .withErrorContaining("@Resetter methods must not have parameters")
      .onLine(11);
  }

  @Test
  public void goodResetter_shouldCompile() {
    final String testClass = "org.robolectric.annotation.processing.shadows.ShadowDummy";
    ASSERT.about(singleClass())
      .that(testClass)
      .compilesWithoutError();
  }
}
