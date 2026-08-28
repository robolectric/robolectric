package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.CINNAMON_BUN;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;
import static org.robolectric.Shadows.shadowOf;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.SystemClock;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.LooperMode;
import org.robolectric.shadow.api.Shadow;

@RunWith(AndroidJUnit4.class)
@LooperMode(LooperMode.Mode.RUNNING)
@Config(minSdk = CINNAMON_BUN)
public class ShadowRunningLooperTest {

  private static final long POST_DELAYED_TIME = 10;
  private static final long SLEEP_TIME = 20;

  // testName is used when creating background threads. Makes it
  // easier to debug exceptions on background threads when you
  // know what test they are associated with.
  @Rule public TestName testName = new TestName();
  private ExecutorService backgroundExecutor;

  // for tests that need to run on a Looper thread
  private Looper looper;
  private ExecutorService looperExecutor;

  @Before
  public void createHandlerThread() {
    backgroundExecutor =
        Executors.newSingleThreadExecutor(r -> new Thread(r, "bg-" + testName.getMethodName()));
    looper = Looper.getMainLooper();
    looperExecutor = new HandlerExecutor(Looper.getMainLooper());
  }

  @After
  public void quitHandlerThread() throws Exception {
    backgroundExecutor.shutdown();
  }

  @Test
  public void looperMode_isRunning() {
    assertThat(ShadowLooper.looperMode()).isEqualTo(LooperMode.Mode.RUNNING);
    assertThat(ShadowLooper.hasTestThread()).isTrue();
  }

  @Test
  public void isPaused_returnsFalseByDefault() {
    ShadowLooper shadowMainLooper = shadowOf(Looper.getMainLooper());
    assertThat(shadowMainLooper.isPaused()).isFalse();
  }

  @Test
  public void unPause_succeeds() {
    ShadowLooper shadowMainLooper = shadowOf(Looper.getMainLooper());
    // unPause is expected to succeed as a no-op because the looper is already unpaused (running)
    shadowMainLooper.unPause();
    assertThat(shadowMainLooper.isPaused()).isFalse();
  }

  @Test
  public void pause_and_control_and_unpause() throws Exception {
    ShadowLooper shadowMainLooper = shadowOf(Looper.getMainLooper());
    Handler mainHandler = new Handler(Looper.getMainLooper());
    AtomicBoolean executed = new AtomicBoolean(false);

    shadowMainLooper.pause();
    assertThat(shadowMainLooper.isPaused()).isTrue();

    mainHandler.post(() -> executed.set(true));
    Thread.sleep(SLEEP_TIME);
    // Task should not have executed automatically while paused
    assertThat(executed.get()).isFalse();

    // Explicitly idle to process the queue while paused
    shadowMainLooper.idle();
    assertThat(executed.get()).isTrue();

    // Unpause and verify tasks execute automatically again
    shadowMainLooper.unPause();
    assertThat(shadowMainLooper.isPaused()).isFalse();

    AtomicBoolean unpausedExecuted = new AtomicBoolean(false);
    CountDownLatch latch = new CountDownLatch(1);
    mainHandler.post(
        () -> {
          unpausedExecuted.set(true);
          latch.countDown();
        });
    assertThat(latch.await(2, TimeUnit.SECONDS)).isTrue();
    assertThat(unpausedExecuted.get()).isTrue();
  }

  @Test
  public void pauseLooper_and_unPauseLooper_static() throws Exception {
    Looper mainLooper = Looper.getMainLooper();
    ShadowLooper shadowMainLooper = shadowOf(mainLooper);

    ShadowLooper.pauseLooper(mainLooper);
    assertThat(shadowMainLooper.isPaused()).isTrue();

    ShadowLooper.unPauseLooper(mainLooper);
    assertThat(shadowMainLooper.isPaused()).isFalse();
  }

  @Test
  public void idle_drainsPendingTasks() {
    ShadowLooper shadowMainLooper = shadowOf(Looper.getMainLooper());
    Handler mainHandler = new Handler(Looper.getMainLooper());
    AtomicBoolean executed = new AtomicBoolean(false);

    mainHandler.post(() -> executed.set(true));
    shadowMainLooper.idle();

    assertThat(executed.get()).isTrue();
  }

  @Test
  public void idleMainLooper_static_drainsPendingTasks() {
    Handler mainHandler = new Handler(Looper.getMainLooper());
    AtomicBoolean executed = new AtomicBoolean(false);

    mainHandler.post(() -> executed.set(true));
    ShadowLooper.idleMainLooper();

    assertThat(executed.get()).isTrue();
  }

  @Test
  public void idleFor_advancesTimeAndDrainsLooper() {
    ShadowLooper shadowMainLooper = shadowOf(Looper.getMainLooper());
    Handler mainHandler = new Handler(Looper.getMainLooper());
    AtomicBoolean executed = new AtomicBoolean(false);

    mainHandler.postDelayed(() -> executed.set(true), POST_DELAYED_TIME);

    long start = SystemClock.uptimeMillis();
    shadowMainLooper.idleFor(Duration.ofMillis(SLEEP_TIME));
    long elapsed = SystemClock.uptimeMillis() - start;

    assertThat(elapsed).isAtLeast(SLEEP_TIME);
    assertThat(executed.get()).isTrue();
  }

  @Test
  public void idleFor_withTimeUnit_advancesTimeAndDrainsLooper() {
    ShadowLooper shadowMainLooper = shadowOf(Looper.getMainLooper());
    Handler mainHandler = new Handler(Looper.getMainLooper());
    AtomicBoolean executed = new AtomicBoolean(false);

    mainHandler.postDelayed(() -> executed.set(true), POST_DELAYED_TIME);

    long start = SystemClock.uptimeMillis();
    shadowMainLooper.idleFor(SLEEP_TIME, TimeUnit.MILLISECONDS);
    long elapsed = SystemClock.uptimeMillis() - start;

    assertThat(elapsed).isAtLeast(SLEEP_TIME);
    assertThat(executed.get()).isTrue();
  }

  @Test
  public void idleMainLooper_static_withTimeUnit_advancesTimeAndDrainsLooper() {
    Handler mainHandler = new Handler(Looper.getMainLooper());
    AtomicBoolean executed = new AtomicBoolean(false);

    mainHandler.postDelayed(() -> executed.set(true), POST_DELAYED_TIME);

    long start = SystemClock.uptimeMillis();
    ShadowLooper.idleMainLooper(SLEEP_TIME, TimeUnit.MILLISECONDS);
    long elapsed = SystemClock.uptimeMillis() - start;

    assertThat(elapsed).isAtLeast(SLEEP_TIME);
    assertThat(executed.get()).isTrue();
  }

  @Test
  public void scheduledTaskTimes_reflectQueueState() {
    HandlerThread handlerThread = new HandlerThread("testHandlerThread");
    handlerThread.start();
    try {
      Looper looper = handlerThread.getLooper();
      ShadowLooper shadowLooper = shadowOf(looper);
      Handler handler = new Handler(looper);

      long now = SystemClock.uptimeMillis();
      handler.postAtTime(() -> {}, now + 1000);
      handler.postAtTime(() -> {}, now + 2000);

      Duration nextScheduledTime = shadowLooper.getNextScheduledTaskTime();
      Duration lastScheduledTime = shadowLooper.getLastScheduledTaskTime();

      assertThat(nextScheduledTime).isNotNull();
      assertThat(lastScheduledTime).isNotNull();
      assertThat(lastScheduledTime.toMillis()).isEqualTo(nextScheduledTime.toMillis() + 1000);
    } finally {
      handlerThread.quitSafely();
    }
  }

  @Test
  public void postSync_runsTaskSynchronouslyOnMainLooper() {
    ShadowPausedLooper shadowMainLooper = (ShadowPausedLooper) shadowOf(Looper.getMainLooper());
    AtomicBoolean executed = new AtomicBoolean(false);

    shadowMainLooper.postSync(() -> executed.set(true));

    assertThat(executed.get()).isTrue();
  }

  @Test
  public void runToEndOfTasks_drainsAllTasks() {
    ShadowLooper shadowMainLooper = shadowOf(Looper.getMainLooper());
    Handler mainHandler = new Handler(Looper.getMainLooper());
    AtomicBoolean executed = new AtomicBoolean(false);

    mainHandler.postDelayed(() -> executed.set(true), POST_DELAYED_TIME);
    shadowMainLooper.runToEndOfTasks();

    assertThat(executed.get()).isTrue();
  }

  @Test
  public void runToNextTask_advancesToNextTask() {
    ShadowLooper shadowMainLooper = shadowOf(Looper.getMainLooper());
    Handler mainHandler = new Handler(Looper.getMainLooper());
    AtomicBoolean firstExecuted = new AtomicBoolean(false);
    AtomicBoolean secondExecuted = new AtomicBoolean(false);

    mainHandler.postDelayed(() -> firstExecuted.set(true), 10);
    mainHandler.postDelayed(() -> secondExecuted.set(true), 20);

    shadowMainLooper.runToNextTask();

    assertThat(firstExecuted.get()).isTrue();
    shadowMainLooper.idle();
  }

  @Test
  public void poll() throws Exception {
    runOnLooper(
        () -> {
          ShadowPausedLooper shadowLooper = Shadow.extract(looper);
          AtomicBoolean backgroundThreadPosted = new AtomicBoolean();
          AtomicBoolean foregroundThreadReceived = new AtomicBoolean();
          shadowLooper.idle();

          backgroundExecutor.execute(
              () -> {
                backgroundThreadPosted.set(true);
                new Handler(looper).post(() -> foregroundThreadReceived.set(true));
              });
          shadowLooper.poll(0);
          shadowLooper.idle();

          assertThat(backgroundThreadPosted.get()).isTrue();
          assertThat(foregroundThreadReceived.get()).isTrue();
        });
  }

  @Test
  public void poll_with_syncBarrier() throws Exception {
    runOnLooper(
        () -> {
          int barrier = looper.getQueue().postSyncBarrier();
          ShadowPausedLooper shadowLooper = Shadow.extract(looper);
          long startTime = System.nanoTime();
          shadowLooper.poll(10);
          Duration elapsedTime = Duration.ofNanos(System.nanoTime() - startTime);
          assertThat(elapsedTime.toMillis()).isAtLeast(10);
          looper.getQueue().removeSyncBarrier(barrier);
        });
  }

  @Test
  public void poll_notIdle() throws Exception {
    runOnLooper(
        () -> {
          ShadowPausedLooper shadowLooper = Shadow.extract(looper);
          new Handler(looper).post(() -> {});
          // should return immediately. Checking elapsed time here would be flaky
          shadowLooper.poll(0);
        });
  }

  @Test
  public void poll_future_msg() throws Exception {
    runOnLooper(
        () -> {
          ShadowPausedLooper shadowLooper = Shadow.extract(looper);
          new Handler(looper).postDelayed(() -> {}, 10);
          long startTime = System.nanoTime();
          // poll should wait the full 10 ms, as the posted message is not executable yet
          shadowLooper.poll(10);
          Duration elapsedTime = Duration.ofNanos(System.nanoTime() - startTime);
          assertThat(elapsedTime.toMillis()).isAtLeast(10);
        });
  }

  @Test
  public void poll_removeSyncBarrier() throws Exception {
    runOnLooper(
        () -> {
          int barrier = looper.getQueue().postSyncBarrier();
          // post a message blocked by a sync barrier
          new Handler(looper).post(() -> {});
          ShadowPausedLooper shadowLooper = Shadow.extract(looper);
          backgroundExecutor.execute(
              () -> {
                try {
                  // give time for poll to block
                  Thread.sleep(10);
                } catch (InterruptedException e) {
                  // ignore
                }
                looper.getQueue().removeSyncBarrier(barrier);
              });

          // should not block forever
          shadowLooper.poll(0);
        });
  }

  @Test
  public void poll_new_message_blocked_by_sync() throws Exception {
    runOnLooper(
        () -> {
          ShadowPausedLooper shadowPausedLooper = Shadow.extract(looper);
          int token = looper.getQueue().postSyncBarrier();
          backgroundExecutor.execute(
              () -> {
                try {
                  // try to make poll block first
                  Thread.sleep(1);
                } catch (InterruptedException e) {
                  // ignore
                }
                new Handler(looper).post(() -> {});
              });

          // should wait the entire time
          long startTime = System.nanoTime();
          shadowPausedLooper.poll(20);
          long elapsedMs = Duration.ofNanos(System.nanoTime() - startTime).toMillis();
          assertThat(elapsedMs).isAtLeast(20);
          looper.getQueue().removeSyncBarrier(token);
        });
  }

  @Test
  public void resetScheduler_throwsUnsupportedOperationException() {
    ShadowLooper shadowMainLooper = shadowOf(Looper.getMainLooper());
    assertThrows(UnsupportedOperationException.class, shadowMainLooper::resetScheduler);
  }

  @Test
  public void reset_throwsUnsupportedOperationException() {
    ShadowLooper shadowMainLooper = shadowOf(Looper.getMainLooper());
    assertThrows(UnsupportedOperationException.class, shadowMainLooper::reset);
  }

  @Test
  public void idleConstantly_throwsUnsupportedOperationException() {
    ShadowLooper shadowMainLooper = shadowOf(Looper.getMainLooper());
    assertThrows(UnsupportedOperationException.class, () -> shadowMainLooper.idleConstantly(true));
  }

  private void runOnLooper(Runnable runnable) throws Exception {
    looperExecutor.submit(runnable).get(5, TimeUnit.SECONDS);
  }

  private static class HandlerExecutor extends AbstractExecutorService {
    private final Handler handler;

    HandlerExecutor(Handler handler) {
      this.handler = handler;
    }

    HandlerExecutor(Looper looper) {
      this(new Handler(looper));
    }

    @Override
    public void execute(Runnable command) {
      handler.post(command);
    }

    @Override
    public void shutdown() {
      throw new UnsupportedOperationException();
    }

    @Override
    public List<Runnable> shutdownNow() {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean isShutdown() {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean isTerminated() {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) {
      throw new UnsupportedOperationException();
    }
  }
}
