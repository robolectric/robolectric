package org.robolectric.annotation.processing.validator;

import static com.google.common.truth.Truth.assertAbout;
import static org.robolectric.annotation.processing.validator.SingleClassSubject.singleClass;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests for {@link FilterValidator} */
@RunWith(JUnit4.class)
public class FilterValidatorTest {

  @Test
  public void filterWithNonVoidReturn_shouldNotCompile() {
    final String testClass =
        "org.robolectric.annotation.processing.shadows.ShadowFilterWithNonVoidReturn";
    assertAbout(singleClass())
        .that(testClass)
        .failsToCompile()
        .withErrorContaining("@Filter methods must have a void return type")
        .onLine(8);
  }

  @Test
  public void filterWithIncorrectVisibility_shouldNotCompile() {
    final String testClass =
        "org.robolectric.annotation.processing.shadows.ShadowFilterWithIncorrectVisibility";
    assertAbout(singleClass())
        .that(testClass)
        .failsToCompile()
        .withErrorContaining("@Filter methods must be protected")
        .onLine(8);
  }
}
