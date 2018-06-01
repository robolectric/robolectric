package org.robolectric;

import static com.google.common.truth.Truth.assertThat;

import java.util.Arrays;
import java.util.Collection;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.ParameterizedRobolectricTestRunner.Parameter;
import org.robolectric.ParameterizedRobolectricTestRunner.Parameters;
import org.robolectric.annotation.Config;

/** Tests for the {@link Parameter} annotation */
@RunWith(ParameterizedRobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public final class ParameterizedRobolectricTestRunnerParameterTest {

  @Parameter(value = 0)
  public boolean booleanValue;

  @Parameter(value = 1)
  public int intValue;

  @Parameter(value = 2)
  public String stringValue;

  @Parameter(value = 3)
  public String expected;

  @Test
  public void parameters_shouldHaveValues() {
    assertThat("" + booleanValue + intValue + stringValue).isEqualTo(expected);
  }

  @Parameters(name = "{index}: booleanValue = {0}, intValue = {1}, stringValue = {2}")
  public static Collection<Object[]> parameters() {
    return Arrays.asList(
        new Object[][] {
          {true, 1, "hello", "true1hello"},
          {false, 2, "robo", "false2robo"},
        });
  }
}
