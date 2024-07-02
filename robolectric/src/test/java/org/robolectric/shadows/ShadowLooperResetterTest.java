package org.robolectric.shadows;

import static android.os.Looper.getMainLooper;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.SystemClock;
import android.view.Choreographer;
import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.After;
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
import org.robolectric.util.TimeUtils;

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

  /** Test that uses Choreographer. ShadowLooper should clear Choreographer state between tests */
  public static class ChoreographerResetTest {

    // use a static thread so both tests share the same Looper + Choreographer
    static HandlerThread handlerThread;

    @Before
    public void init() {
      if (handlerThread == null) {
        handlerThread = new HandlerThread("ChoreographerResetTest");
        handlerThread.start();
      }
    }

    @AfterClass
    public static void shutDown() throws InterruptedException {
      handlerThread.quit();
      handlerThread.join();
    }

    private void doPostToChoreographerTest() {
      checkNotNull(handlerThread.getLooper());
      Handler handler = new Handler(handlerThread.getLooper());

      AtomicLong frameTimeNanosResult = new AtomicLong(-1);
      // you can only access Choreographer from Looper thread
      handler.post(() -> Choreographer.getInstance().postFrameCallback(frameTimeNanosResult::set));
      shadowOf(handlerThread.getLooper()).idle();

      // ensure callback happened and that clock is consistent with Choreographer's frame time
      // tracking
      assertThat(frameTimeNanosResult.get())
          .isEqualTo(SystemClock.uptimeMillis() * TimeUtils.NANOS_PER_MS);

      // Now set Choreographer so it expects there is a pending vsync+frame callback.
      // Choreographer will ignore vsync+frame requests if there is already one pending.
      // If Choreographer state isn't clearly properly between tests the next test will fail.

      // Setting Choreographer into paused mode makes this test deterministic, as vsync callbacks
      // won't occur until clock has been incremented.
      ShadowChoreographer.setPaused(true);
      ShadowChoreographer.setFrameDelay(Duration.ofMillis(16));

      handler.post(() -> Choreographer.getInstance().postFrameCallback(frameTimeNanos -> {}));
      shadowOf(handlerThread.getLooper()).idle();
    }

    @Test
    public void postToChoreographerTest() {
      doPostToChoreographerTest();
    }

    @Test
    public void anotherPostToChoreographerTest() {
      doPostToChoreographerTest();
    }
  }

  @Test
  public void choreographerPost() throws InitializationError {
    Runner runner = new RobolectricTestRunner(ChoreographerResetTest.class);

    // run and assert no failures
    runner.run(runNotifier);
  }

  /** Test that uses Choreographer and asynchronously kills thread between tests */
  public static class ChoreographerResetQuitTest {

    // use a static thread so both tests share the same Looper + Choreographer
    private HandlerThread handlerThread;
    private final Executor executor = Executors.newSingleThreadExecutor();

    @Before
    public void init() {
      handlerThread = new HandlerThread("ChoreographerResetQuitTest");
      handlerThread.start();
    }

    @After
    public void shutDown() throws InterruptedException {
      // asynchronously quit handler thread to try to expose race conditions
      executor.execute(
          new Runnable() {
            @Override
            public void run() {
              handlerThread.quit();
            }
          });
    }

    private void doPostToChoreographerTest() {
      checkNotNull(handlerThread.getLooper());
      Handler handler = new Handler(handlerThread.getLooper());

      AtomicLong frameTimeNanosResult = new AtomicLong(-1);
      // you can only access Choreographer from Looper thread
      handler.post(() -> Choreographer.getInstance().postFrameCallback(frameTimeNanosResult::set));
      shadowOf(handlerThread.getLooper()).idle();

      // ensure callback happened and that clock is consistent with Choreographer's frame time
      // tracking
      assertThat(frameTimeNanosResult.get())
          .isEqualTo(SystemClock.uptimeMillis() * TimeUtils.NANOS_PER_MS);

      // Now set Choreographer so it expects there is a pending vsync+frame callback.
      // Choreographer will ignore vsync+frame requests if there is already one pending.
      // If Choreographer state isn't clearly properly between tests the next test will fail.

      // Setting Choreographer into paused mode makes this test deterministic, as vsync callbacks
      // won't occur until clock has been incremented.
      ShadowChoreographer.setPaused(true);
      ShadowChoreographer.setFrameDelay(Duration.ofMillis(16));

      handler.post(() -> Choreographer.getInstance().postFrameCallback(frameTimeNanos -> {}));
      shadowOf(handlerThread.getLooper()).idle();
    }

    @Test
    public void postToChoreographerTest() {
      doPostToChoreographerTest();
    }

    @Test
    public void anotherPostToChoreographerTest() {
      doPostToChoreographerTest();
    }
  }

  /** Tests for potentially race conditions where Looper is quit asynchrounously at end of test */
  @Test
  public void choreographerQuitPost() throws InitializationError {
    Runner runner = new RobolectricTestRunner(ChoreographerResetQuitTest.class);

    // run and assert no failures
    runner.run(runNotifier);
  }

  /**
   * Test that holds static reference to Choreographer. Robolectric should clear Choreographer state
   * between tests
   */
  public static class StaticChoreographerResetTest {

    // use a static thread so both tests share the same Looper + Choreographer
    @SuppressWarnings("NonFinalStaticField")
    static HandlerThread handlerThread;

    @SuppressWarnings("NonFinalStaticField")
    static Choreographer choreographer;

    @Before
    public void init() throws InterruptedException {
      if (handlerThread == null) {
        handlerThread = new HandlerThread("ChoreographerResetTest");
        handlerThread.start();
        CountDownLatch latch = new CountDownLatch(1);
        new Handler(handlerThread.getLooper())
            .post(
                new Runnable() {
                  @Override
                  public void run() {
                    choreographer = Choreographer.getInstance();
                    latch.countDown();
                  }
                });
        latch.await();
      }
    }

    @AfterClass
    public static void shutDown() throws InterruptedException {
      handlerThread.quit();
      handlerThread.join();
    }

    private void doPostToChoreographerTest() {
      checkNotNull(handlerThread.getLooper());
      Handler handler = new Handler(handlerThread.getLooper());

      AtomicLong frameTimeNanosResult = new AtomicLong(-1);
      // you can only access Choreographer from Looper thread
      handler.post(() -> choreographer.postFrameCallback(frameTimeNanosResult::set));
      shadowOf(handlerThread.getLooper()).idle();

      // ensure callback happened and that clock is consistent with Choreographer's frame time
      // tracking
      assertThat(frameTimeNanosResult.get())
          .isEqualTo(SystemClock.uptimeMillis() * TimeUtils.NANOS_PER_MS);

      // Now set Choreographer so it expects there is a pending vsync+frame callback.
      // Choreographer will ignore vsync+frame requests if there is already one pending.
      // If Choreographer state isn't clearly properly between tests the next test will fail.

      // Setting Choreographer into paused mode makes this test deterministic, as vsync callbacks
      // won't occur until clock has been incremented.
      ShadowChoreographer.setPaused(true);
      ShadowChoreographer.setFrameDelay(Duration.ofMillis(16));

      handler.post(() -> choreographer.postFrameCallback(frameTimeNanos -> {}));
      shadowOf(handlerThread.getLooper()).idle();
    }

    @Test
    public void postToChoreographerTest() {
      doPostToChoreographerTest();
    }

    @Test
    public void anotherPostToChoreographerTest() {
      doPostToChoreographerTest();
    }
  }

  @Test
  public void staticChoreographerPost() throws InitializationError {
    Runner runner = new RobolectricTestRunner(StaticChoreographerResetTest.class);

    // run and assert no failures
    runner.run(runNotifier);
  }
}
