package org.robolectric.shadows;

import static android.os.Looper.getMainLooper;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.os.Handler;
import android.os.HandlerThread;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runner.Runner;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.JUnit4;
import org.junit.runners.model.InitializationError;
import org.robolectric.RobolectricTestRunner;

/** A specialized test for verifying that looper state is cleared properly between tests. */
@RunWith(JUnit4.class)
public class ShadowLooperResetterTest {
  private final RunNotifier runNotifier = new RunNotifier();

  @Before
  public void setup() {
    runNotifier.addListener(
        new RunListener() {
          @Override
          public void testFailure(Failure failure) throws Exception {
            throw new AssertionError("Unexpected test failure: " + failure, failure.getException());
          }
        });
  }

  /**
   * Basic test class that interacts with Looper in two different tests, to ensure Looper remains
   * functional after reset.
   */
  public static class BasicLooperTest {

    private void doPostToLooperTest() {
      checkNotNull(getMainLooper());

      AtomicBoolean didRun = new AtomicBoolean(false);
      new Handler(getMainLooper()).post(() -> didRun.set(true));

      assertThat(didRun.get()).isFalse();
      shadowOf(getMainLooper()).idle();
      assertThat(didRun.get()).isTrue();
    }

    @Test
    public void postToLooperTest() {
      doPostToLooperTest();
    }

    @Test
    public void anotherPostToLooperTest() {
      doPostToLooperTest();
    }
  }

  @Test
  public void basicPostAndRun() throws InitializationError {
    Runner runner = new RobolectricTestRunner(BasicLooperTest.class);

    // run and assert no failures
    runner.run(runNotifier);
  }

  /** Test that leaves an unexecuted runnable on Looper and verifies it is removed between tests. */
  public static class PendingLooperTest {

    private void doPostToLooperTest() {
      checkState(shadowOf(getMainLooper()).isIdle());

      AtomicBoolean didRun = new AtomicBoolean(false);
      new Handler(getMainLooper()).post(() -> didRun.set(true));

      assertThat(didRun.get()).isFalse();
      assertThat(shadowOf(getMainLooper()).isIdle()).isFalse();
    }

    @Test
    public void postToLooperTest() {
      doPostToLooperTest();
    }

    @Test
    public void anotherPostToLooperTest() {
      doPostToLooperTest();
    }
  }

  @Test
  public void pendingTasksClearer() throws InitializationError {
    Runner runner = new RobolectricTestRunner(PendingLooperTest.class);

    // run and assert no failures
    runner.run(runNotifier);
  }

  /** Test that uses delayed tasks */
  public static class DelayedTaskTest {

    private void doDelayedPostToLooperTest() {
      checkState(shadowOf(getMainLooper()).isIdle());

      AtomicBoolean didRun = new AtomicBoolean(false);
      new Handler(getMainLooper()).postDelayed(() -> didRun.set(true), 100);
      shadowOf(getMainLooper()).idle();
      assertThat(didRun.get()).isFalse();
      shadowOf(getMainLooper()).idleFor(Duration.ofMillis(100));
      assertThat(didRun.get()).isTrue();
    }

    @Test
    public void postToLooperTest() {
      doDelayedPostToLooperTest();
    }

    @Test
    public void anotherPostToLooperTest() {
      doDelayedPostToLooperTest();
    }
  }

  @Test
  public void delayedTask() throws InitializationError {
    Runner runner = new RobolectricTestRunner(DelayedTaskTest.class);

    // run and assert no failures
    runner.run(runNotifier);
  }

  /** Test that uses delayed tasks on a running looper */
  public static class DelayedTaskRunningLooperTest {

    // use a static thread so both tests share the same looper
    static HandlerThread handlerThread;

    @Before
    public void init() {
      if (handlerThread == null) {
        handlerThread = new HandlerThread("DelayedTaskRunningLooperTest");
        handlerThread.start();
      }
    }

    @AfterClass
    public static void shutDown() throws InterruptedException {
      handlerThread.quit();
      handlerThread.join();
    }

    private void doDelayedPostToLooperTest() {
      checkNotNull(handlerThread.getLooper());

      AtomicBoolean didRun = new AtomicBoolean(false);
      new Handler(handlerThread.getLooper()).postDelayed(() -> didRun.set(true), 100);
      shadowOf(handlerThread.getLooper()).idle();
      assertThat(didRun.get()).isFalse();
      shadowOf(handlerThread.getLooper()).idleFor(Duration.ofMillis(100));
      assertThat(didRun.get()).isTrue();
    }

    @Test
    public void postToLooperTest() {
      doDelayedPostToLooperTest();
    }

    @Test
    public void anotherPostToLooperTest() {
      doDelayedPostToLooperTest();
    }
  }

  @Test
  public void delayedTaskRunningLooper() throws InitializationError {
    Runner runner = new RobolectricTestRunner(DelayedTaskRunningLooperTest.class);

    // run and assert no failures
    runner.run(runNotifier);
  }
}
