package org.robolectric.annotation.processing.validator;

import static com.google.common.truth.Truth.assertAbout;
import static org.robolectric.annotation.processing.validator.SingleClassSubject.singleClass;

import org.junit.Test;

public class ImplementationValidatorTest {
  @Test
  public void implementationWithoutImplements_shouldNotCompile() {
    final String testClass = "org.robolectric.annotation.processing.shadows.ShadowImplementationWithoutImplements";
    assertAbout(singleClass())
      .that(testClass)
      .failsToCompile()
      .withErrorContaining("@Implementation without @Implements")
      .onLine(7);
  }
}
