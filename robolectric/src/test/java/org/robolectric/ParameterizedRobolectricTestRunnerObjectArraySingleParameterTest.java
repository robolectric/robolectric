package org.robolectric;

import static com.google.common.truth.Truth.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.ParameterizedRobolectricTestRunner.Parameter;
import org.robolectric.ParameterizedRobolectricTestRunner.Parameters;

/**
 * Tests for the single parameter test with {@link Object} array as return type.
 *
 * <p>See https://github.com/junit-team/junit4/wiki/parameterized-tests#tests-with-single-parameter.
 */
@RunWith(ParameterizedRobolectricTestRunner.class)
public class ParameterizedRobolectricTestRunnerObjectArraySingleParameterTest {
  @Parameter public int intValue;

  @Test
  public void parameters_shouldHaveValues() {
    assertThat(intValue).isNotEqualTo(0);
  }

  @Parameters
  public static Object[] parameters() {
    return new Object[] {1, 2, 3, 4, 5};
  }
}
