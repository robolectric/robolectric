package org.robolectric;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.RunWith;
import org.junit.runner.notification.Failure;
import org.junit.runners.JUnit4;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.experimental.LazyApplication;
import org.robolectric.annotation.experimental.LazyApplication.LazyLoad;
import org.robolectric.internal.bytecode.InstrumentationConfiguration;

@RunWith(JUnit4.class)
public final class BeforeAfterClassInvocationTest {
  public static final class StateHolder {

    public static final AtomicBoolean beforeClassCalled = new AtomicBoolean(false);
    public static final AtomicBoolean afterClassCalled = new AtomicBoolean(false);
    public static final AtomicInteger testCounter = new AtomicInteger(0);
    public static final AtomicInteger beforeClassCounter = new AtomicInteger(0);
    public static final AtomicInteger afterClassCounter = new AtomicInteger(0);

    public static void reset() {
      beforeClassCalled.set(false);
      afterClassCalled.set(false);
      testCounter.set(0);
      beforeClassCounter.set(0);
      afterClassCounter.set(0);
    }

    private StateHolder() {}
  }

  @Before
  public void setup() {
    StateHolder.reset();
  }

  @Test
  public void testBeforeAfterClassCalled() throws Exception {
    assertThat(StateHolder.beforeClassCalled.get()).isFalse();
    assertThat(StateHolder.afterClassCalled.get()).isFalse();
    // run SimpleTest
    assertNoFailures(run(new Runner(SimpleTest.class)));

    assertThat(StateHolder.beforeClassCalled.get()).isTrue();
    assertThat(StateHolder.afterClassCalled.get()).isTrue();
  }

  @Test
  public void testInvocationCount() throws Exception {
    assertThat(StateHolder.beforeClassCounter.get()).isEqualTo(0);
    assertThat(StateHolder.afterClassCounter.get()).isEqualTo(0);
    assertThat(StateHolder.testCounter.get()).isEqualTo(0);
    // run SimpleTest
    assertNoFailures(run(new Runner(SimpleTest.class)));

    assertThat(StateHolder.beforeClassCounter.get()).isEqualTo(1);
    assertThat(StateHolder.afterClassCounter.get()).isEqualTo(1);
    assertThat(StateHolder.testCounter.get()).isEqualTo(2);
  }

  private Result run(RobolectricTestRunner runner) {
    JUnitCore runnerCore = new JUnitCore();
    return runnerCore.run(runner);
  }

  private void assertNoFailures(Result result) {
    if (!result.wasSuccessful()) {
      for (Failure failure : result.getFailures()) {
        fail(failure.getMessage() + failure.getException());
      }
    }
  }

  // This is a test that only has @BeforeClass and @AfterClass to avoid any tanglement with
  // @ClassRule
  @LazyApplication(LazyLoad.OFF)
  @Config(sdk = Config.NEWEST_SDK)
  public static class SimpleTest {
    @BeforeClass
    public static void beforeClass() {
      StateHolder.beforeClassCalled.set(true);
      StateHolder.beforeClassCounter.incrementAndGet();
    }

    @AfterClass
    public static void afterClass() throws Exception {
      StateHolder.afterClassCalled.set(true);
      StateHolder.afterClassCounter.incrementAndGet();
    }

    @Test
    public void shouldDoNothingMuch() throws Exception {
      StateHolder.testCounter.incrementAndGet();
    }

    @Test
    public void shouldDoNothingMuchMore() throws Exception {
      StateHolder.testCounter.incrementAndGet();
    }
  }

  public static class Runner extends RobolectricTestRunner {
    public Runner(Class<?> testClass) throws InitializationError {
      super(testClass);
    }

    @Override
    protected InstrumentationConfiguration createClassLoaderConfig(FrameworkMethod method) {
      InstrumentationConfiguration.Builder builder =
          new InstrumentationConfiguration.Builder(super.createClassLoaderConfig(method));
      // This is to make sure that the StateHolder class is not acquired in the sandbox.
      builder.doNotAcquireClass(StateHolder.class);
      return builder.build();
    }
  }
}
