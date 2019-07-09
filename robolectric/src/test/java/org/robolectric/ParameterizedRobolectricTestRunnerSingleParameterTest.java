package org.robolectric;

import static com.google.common.truth.Truth.assertThat;

import java.util.Arrays;
import java.util.Collection;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.ParameterizedRobolectricTestRunner.Parameter;
import org.robolectric.ParameterizedRobolectricTestRunner.Parameters;

/** Tests for the single parameter test. */
@RunWith(ParameterizedRobolectricTestRunner.class)
public final class ParameterizedRobolectricTestRunnerSingleParameterTest {

  @Parameter public int intValue;

  @Test
  public void parameters_shouldHaveValues() {
    assertThat(intValue).isNotEqualTo(0);
  }

  @Parameters
  public static Collection<Integer> parameters() {
    return Arrays.asList(1, 2, 3, 4, 5);
  }
}
