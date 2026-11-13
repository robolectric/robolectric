package org.robolectric;

import static com.google.common.truth.Truth.assertThat;

import java.util.Arrays;
import java.util.List;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.robolectric.ParameterizedRobolectricTestRunner.Parameter;
import org.robolectric.ParameterizedRobolectricTestRunner.Parameters;
import org.robolectric.annotation.Config;
import org.robolectric.junit.rules.SetSystemPropertyRule;

@RunWith(JUnit4.class)
public class ParameterizedRobolectricTestRunnerTest {
  private static final String PROVIDER_CALLS =
      "ParameterizedRobolectricTestRunnerTest.providerCalls";

  @Rule public SetSystemPropertyRule properties = new SetSystemPropertyRule();

  @Test
  public void constructorParametersAreComputedOncePerSandbox() throws Throwable {
    runFixture(ConstructorFixture.class, 4);
  }

  @Test
  public void fieldParametersAreComputedOncePerSandbox() throws Throwable {
    runFixture(FieldFixture.class, 2);
  }

  @Test
  public void ignoredMethodsStillHaveParameters() throws Throwable {
    runFixture(IgnoredFixture.class, 0);
  }

  @Test
  public void parametersAreRecomputedWhenRunnerRunsAgain() throws Throwable {
    properties.set(PROVIDER_CALLS, "0");
    ParameterizedRobolectricTestRunner runner =
        new ParameterizedRobolectricTestRunner(ConstructorFixture.class);

    for (int run = 1; run <= 2; run++) {
      Result result = new JUnitCore().run(runner);
      assertThat(result.getFailures()).isEmpty();
      assertThat(result.getRunCount()).isAtLeast(4);
      assertThat(System.getProperty(PROVIDER_CALLS))
          .isEqualTo(Integer.toString(1 + run * result.getRunCount() / 4));
    }
  }

  private void runFixture(Class<?> fixture, int testsPerSandbox) throws Throwable {
    properties.set(PROVIDER_CALLS, "0");
    ParameterizedRobolectricTestRunner runner = new ParameterizedRobolectricTestRunner(fixture);
    assertThat(runner.getDescription().getChildren()).hasSize(2);
    Result result = new JUnitCore().run(runner);
    assertThat(result.getFailures()).isEmpty();
    assertThat(result.getRunCount()).isAtLeast(testsPerSandbox);
    int sandboxCount = testsPerSandbox == 0 ? 0 : result.getRunCount() / testsPerSandbox;
    assertThat(result.getRunCount()).isEqualTo(testsPerSandbox * sandboxCount);
    assertThat(System.getProperty(PROVIDER_CALLS)).isEqualTo(Integer.toString(1 + sandboxCount));
  }

  private static List<Value> values() {
    System.setProperty(
        PROVIDER_CALLS, Integer.toString(Integer.parseInt(System.getProperty(PROVIDER_CALLS)) + 1));
    return Arrays.asList(new Value(1), new Value(2));
  }

  public static class Value {
    final int number;

    Value(int number) {
      this.number = number;
    }

    @Override
    public String toString() {
      return Integer.toString(number);
    }
  }

  @Ignore
  @Config(sdk = Config.ALL_SDKS)
  public static class ConstructorFixture {
    private final Value value;

    public ConstructorFixture(Value value) {
      this.value = value;
    }

    @Parameters(name = "value={0}")
    public static List<Value> parameters() {
      return values();
    }

    @Test
    public void first() {
      assertThat(value.number).isAnyOf(1, 2);
    }

    @Test
    public void second() {
      assertThat(value).isInstanceOf(Value.class);
    }
  }

  @Ignore
  @Config(sdk = Config.ALL_SDKS)
  public static class FieldFixture {
    @Parameter public Value value;

    @Parameters(name = "value={0}")
    public static List<Value> parameters() {
      return values();
    }

    @Test
    public void test() {
      assertThat(value.number).isAnyOf(1, 2);
    }
  }

  @Ignore
  public static class IgnoredFixture extends FieldFixture {
    @Ignore
    @Test
    @Override
    public void test() {}
  }
}
