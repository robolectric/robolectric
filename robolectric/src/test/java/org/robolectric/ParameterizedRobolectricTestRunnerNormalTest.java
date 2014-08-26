package org.robolectric;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

import java.util.Arrays;
import java.util.Collection;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * Parameterized tests using basic java classes.
 *
 * @author John Ferlisi
 */
@RunWith(ParameterizedRobolectricTestRunner.class)
public final class ParameterizedRobolectricTestRunnerNormalTest {

  private final int first;
  private final int second;
  private final int expectedSum;
  private final int expectedDifference;
  private final int expectedProduct;
  private final int expectedQuotient;

  public ParameterizedRobolectricTestRunnerNormalTest(int first,
                                                      int second,
                                                      int expectedSum,
                                                      int expectedDifference,
                                                      int expectedProduct,
                                                      int expectedQuotient) {
    this.first = first;
    this.second = second;
    this.expectedSum = expectedSum;
    this.expectedDifference = expectedDifference;
    this.expectedProduct = expectedProduct;
    this.expectedQuotient = expectedQuotient;
  }

  @Test
  @Config(manifest = Config.NONE)
  public void add() {
    assertThat(first + second).isEqualTo(expectedSum);
  }

  @Test
  @Config(manifest = Config.NONE)
  public void subtract() {
    assertThat(first - second).isEqualTo(expectedDifference);
  }

  @Test
  @Config(manifest = Config.NONE)
  public void multiple() {
    assertThat(first * second).isEqualTo(expectedProduct);
  }

  @Test
  @Config(manifest = Config.NONE)
  public void divide() {
    assertThat(first / second).isEqualTo(expectedQuotient);
  }

  @ParameterizedRobolectricTestRunner.Parameters(name = "Java Math Test: {0}, {1}")
  public static Collection getTestData() {
    Object[][] data = {
        { 1, 1, 2, 0, 1, 1 },
        { 2, 1, 3, 1, 2, 2 },
        { 2, 2, 4, 0, 4, 1 },
        { 4, 4, 8, 0, 16, 1 }
    };
    return Arrays.asList(data);
  }
}
