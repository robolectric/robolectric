package org.robolectric.annotation.processing.validator;

import static com.google.common.truth.Truth.assertAbout;
import static org.robolectric.annotation.processing.validator.SingleClassSubject.singleClass;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests for {@link ImplementationValidator} */
@RunWith(JUnit4.class)
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

  @Test
  public void implementationWithIncorrectVisibility_shouldNotCompile() {
    final String testClass = "org.robolectric.annotation.processing.shadows.ShadowImplementationWithIncorrectVisibility";
    assertAbout(singleClass())
        .that(testClass)
        .failsToCompile()
        .withErrorContaining("@Implementation methods should be protected (preferred) or public (deprecated)")
        .onLine(17)
        .and()
        .withErrorContaining("@Implementation methods should be protected (preferred) or public (deprecated)")
        .onLine(21)
        .and()
        .withErrorContaining("@Implementation methods should be protected (preferred) or public (deprecated)")
        .onLine(31)
        .and()
        .withErrorContaining("@Implementation methods should be protected (preferred) or public (deprecated)")
        .onLine(34)
    ;
  }
}
