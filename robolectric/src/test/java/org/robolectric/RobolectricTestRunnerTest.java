package org.robolectric;

import android.os.Build;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import org.junit.runner.notification.RunNotifier;
import org.robolectric.annotation.Config;
import org.robolectric.internal.ParallelUniverse;
import org.robolectric.internal.ParallelUniverseInterface;
import org.robolectric.internal.VirtualEnvironment;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.robolectric.util.ReflectionHelpers.ClassParameter.from;
import static org.robolectric.util.ReflectionHelpers.callConstructor;

public class RobolectricTestRunnerTest {

  private RunNotifier notifier;
  private List<String> events;

  @Before
  public void setUp() throws Exception {
    notifier = new RunNotifier();
    events = new ArrayList<>();
    notifier.addListener(new RunListener() {
      @Override
      public void testIgnored(Description description) throws Exception {
        events.add("ignored: " + description.getDisplayName());
      }

      @Override
      public void testFailure(Failure failure) throws Exception {
        events.add("failure: " + failure.getMessage());
      }
    });
  }

  @Test public void ignoredTestCanSpecifyUnsupportedSdkWithoutExploding() throws Exception {
    RobolectricTestRunner runner = new RobolectricTestRunner(TestWithOldSdk.class);
    runner.run(notifier);
    assertThat(events).containsOnly(
        "failure: Robolectric does not support API level 11.",
        "ignored: ignoredOldSdkMethod(org.robolectric.RobolectricTestRunnerTest$TestWithOldSdk)"
    );
  }

  @Test
  public void failureInResetterDoesntBreakAllTests() throws Exception {
    RobolectricTestRunner runner = new RobolectricTestRunner(TestWithTwoMethods.class) {
      @Override
      ParallelUniverseInterface getHooksInterface(VirtualEnvironment virtualEnvironment) {
        Class<? extends ParallelUniverseInterface> clazz = virtualEnvironment.bootstrappedClass(MyParallelUniverse.class);
        return callConstructor(clazz, from(RobolectricTestRunner.class, this));
      }
    };
    runner.run(notifier);
    assertThat(events).containsExactly(
        "failure: java.lang.RuntimeException: fake error in resetStaticState",
        "failure: java.lang.RuntimeException: fake error in resetStaticState"
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
  public static class TestWithOldSdk {
    @Config(sdk = Build.VERSION_CODES.HONEYCOMB)
    @Test
    public void oldSdkMethod() throws Exception {
      fail("I should not be run!");
    }

    @Ignore("This test shouldn't run, and shouldn't cause the test runner to fail")
    @Config(sdk = Build.VERSION_CODES.HONEYCOMB)
    @Test
    public void ignoredOldSdkMethod() throws Exception {
      fail("I should not be run!");
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