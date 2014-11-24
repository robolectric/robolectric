package org.robolectric.annotation.processing;

import static org.robolectric.annotation.processing.SingleClassSubject.singleClass;
import static org.truth0.Truth.ASSERT;

import org.junit.Test;

public class ImplementationValidatorTest {
  @Test
  public void implementationWithoutImplements_shouldNotCompile() {
    final String testClass = "org.robolectric.annotation.processing.shadows.ShadowImplementationWithoutImplements";
    ASSERT.about(singleClass())
      .that(testClass)
      .failsToCompile()
      .withErrorContaining("@Implementation without @Implements")
      .onLine(7);
  }
}
