package org.robolectric;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import org.junit.runner.notification.RunNotifier;
import org.robolectric.annotation.Config;
import org.robolectric.internal.ParallelUniverse;
import org.robolectric.internal.ParallelUniverseInterface;
import org.robolectric.internal.SdkEnvironment;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.robolectric.util.ReflectionHelpers.ClassParameter.from;
import static org.robolectric.util.ReflectionHelpers.callConstructor;

public class RobolectricTestRunnerTest {
  @Test
  public void failureInResetterDoesntBreakAllTests() throws Exception {
    RobolectricTestRunner runner = new RobolectricTestRunner(TestWithTwoMethods.class) {
      @Override
      ParallelUniverseInterface getHooksInterface(SdkEnvironment sdkEnvironment) {
        Class<? extends ParallelUniverseInterface> clazz = sdkEnvironment.bootstrappedClass(MyParallelUniverse.class);
        return callConstructor(clazz, from(RobolectricTestRunner.class, this));
      }
    };
    RunNotifier notifier = new RunNotifier();
    final List<String> failures = new ArrayList<>();
    notifier.addListener(new RunListener() {
      @Override
      public void testFailure(Failure failure) throws Exception {
        failures.add(failure.getMessage());
      }
    });
    runner.run(notifier);
    assertThat(failures).containsExactly(
        "java.lang.RuntimeException: fake error in resetStaticState",
        "java.lang.RuntimeException: fake error in resetStaticState"
    );
  }

  /////////////////////////////

  public static class MyParallelUniverse extends ParallelUniverse {
    public MyParallelUniverse(RobolectricTestRunner robolectricTestRunner) {
      super(robolectricTestRunner);
    }

    @Override
    public void resetStaticState(Config config) {
      throw new RuntimeException("fake error in resetStaticState");
    }
  }

  @Ignore
  public static class TestWithTwoMethods {
    @Test
    public void first() throws Exception {
    }

    @Test
    public void second() throws Exception {
    }
  }
}