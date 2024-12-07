package org.robolectric.shadows;

import static android.os.Looper.getMainLooper;
import static com.google.common.truth.Truth.assertThat;
import static java.util.concurrent.Executors.newSingleThreadExecutor;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.robolectric.Shadows.shadowOf;
import static org.robolectric.shadows.ShadowLooper.shadowMainLooper;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.os.Build.VERSION_CODES;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.MessageQueue.IdleHandler;
import android.os.SystemClock;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.google.common.util.concurrent.SettableFuture;
import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.LooperMode;
import org.robolectric.res.android.Ref;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.util.reflector.Direct;
import org.robolectric.util.reflector.ForType;

@RunWith(AndroidJUnit4.class)
@LooperMode(LooperMode.Mode.PAUSED)
public class ShadowPausedLooperTest {

  // testName is used when creating background threads. Makes it
  // easier to debug exceptions on background threads when you
  // know what test they are associated with.
  @Rule public TestName testName = new TestName();
  private HandlerThread handlerThread;

  @Before
  public void createHandlerThread() {
    handlerThread = new HandlerThread(testName.getMethodName());
    handlerThread.start();
  }

  @After
  public void quitHandlerThread() throws Exception {
    handlerThread.quit();
    handlerThread.join();
  }

  @Test
  public void mainLooper_getAllLoopers_shouldContainMainAndHandlerThread() {
    Looper looper = handlerThread.getLooper();

    assertThat(ShadowLooper.getAllLoopers()).contains(getMainLooper());
    assertThat(ShadowLooper.getAllLoopers()).contains(looper);
  }

  @Test
  public void mainLooper_andMyLooper_shouldBeSame_onMainThread() {
    assertThat(Looper.myLooper()).isSameInstanceAs(getMainLooper());
  }

  @Test
  public void differentThreads_getDifferentLoopers() {
    assertThat(handlerThread.getLooper()).isNotSameInstanceAs(getMainLooper());
    handlerThread.quit();
  }

  @Test
  public void mainLooperThread_shouldBeTestThread() {
    assertThat(getMainLooper().getThread()).isSameInstanceAs(Thread.currentThread());
  }

  @Test(timeout = 200)
  public void junitTimeoutTestRunsOnMainThread() {
    assertThat(getMainLooper().getThread()).isSameInstanceAs(Thread.currentThread());
  }

  @Test
  public void postedMainLooperTasksAreNotExecuted() {
    Runnable mockRunnable = mock(Runnable.class);
    Handler handler = new Handler();
    handler.post(mockRunnable);
    verify(mockRunnable, timeout(20).times(0)).run();
  }

  @Test
  public void postedBackgroundLooperTasksAreExecuted() throws InterruptedException {
    Runnable mockRunnable = mock(Runnable.class);
    Handler handler = new Handler(handlerThread.getLooper());
    handler.post(mockRunnable);
    ShadowPausedLooper shadowLooper = Shadow.extract(handlerThread.getLooper());
    shadowLooper.idle();
    verify(mockRunnable, times(1)).run();
  }

  @Test
  public void postedBackgroundLooperTasksWhenPaused() throws InterruptedException {
    Runnable mockRunnable = mock(Runnable.class);
    shadowOf(handlerThread.getLooper()).pause();
    new Handler(handlerThread.getLooper()).post(mockRunnable);
    verify(mockRunnable, timeout(20).times(0)).run();
    assertThat(shadowOf(handlerThread.getLooper()).isIdle()).isFalse();
    shadowOf(handlerThread.getLooper()).idle();
    verify(mockRunnable, times(1)).run();
  }

  @Test
  public void pause_backgroundLooper() {
    assertThat(shadowOf(handlerThread.getLooper()).isPaused()).isFalse();
    shadowOf(handlerThread.getLooper()).pause();
    assertThat(shadowOf(handlerThread.getLooper()).isPaused()).isTrue();

    shadowOf(handlerThread.getLooper()).unPause();
    assertThat(shadowOf(handlerThread.getLooper()).isPaused()).isFalse();
  }

  @Test
  public void idle_backgroundLooperExecuteInBackgroundThread() {
    Ref<Thread> threadRef = new Ref<>(null);
    new Handler(handlerThread.getLooper()).post(() -> threadRef.set(Thread.currentThread()));
    shadowOf(handlerThread.getLooper()).idle();
    assertThat(handlerThread.getLooper().getThread()).isEqualTo(threadRef.get());
    assertThat(getMainLooper().getThread()).isNotEqualTo(threadRef.get());
  }

  @Test
  public void runOneTask_backgroundLooperExecuteInBackgroundThread() {
    shadowOf(handlerThread.getLooper()).pause();
    Ref<Thread> threadRef = new Ref<>(null);
    new Handler(handlerThread.getLooper()).post(() -> threadRef.set(Thread.currentThread()));
    shadowOf(handlerThread.getLooper()).runOneTask();
    assertThat(handlerThread.getLooper().getThread()).isEqualTo(threadRef.get());
    assertThat(getMainLooper().getThread()).isNotEqualTo(threadRef.get());
  }

  @Test
  public void postedDelayedBackgroundLooperTasksAreExecutedOnlyWhenSystemClockAdvanced()
      throws InterruptedException {
    Runnable mockRunnable = mock(Runnable.class);
    new Handler(handlerThread.getLooper()).postDelayed(mockRunnable, 10);
    ShadowPausedLooper shadowLooper = Shadow.extract(handlerThread.getLooper());
    shadowLooper.idle();
    verify(mockRunnable, times(0)).run();
    ShadowSystemClock.advanceBy(Duration.ofMillis(100));
    shadowLooper.idle();
    verify(mockRunnable, times(1)).run();
  }

  @Test
  public void cannotIdleMainThreadFromBackgroundThread() throws InterruptedException {
    ExecutorService executorService = newSingleThreadExecutor();
    Future<Boolean> result =
        executorService.submit(
            new Callable<Boolean>() {
              @Override
              public Boolean call() throws Exception {
                shadowMainLooper().idle();
                return true;
              }
            });
    try {
      result.get();
      fail("idling main looper from background thread unexpectedly succeeded.");
    } catch (InterruptedException e) {
      throw e;
    } catch (ExecutionException e) {
      assertThat(e.getCause()).isInstanceOf(UnsupportedOperationException.class);
    } finally {
      executorService.shutdown();
    }
  }

  @Test
  public void idle_mainLooper() {
    ShadowPausedLooper shadowLooper = Shadow.extract(getMainLooper());
    shadowLooper.idle();
  }

  @Test
  public void idle_executesTask_mainLooper() {
    ShadowPausedLooper shadowLooper = Shadow.extract(getMainLooper());
    Runnable mockRunnable = mock(Runnable.class);
    Handler mainHandler = new Handler();
    mainHandler.post(mockRunnable);
    verify(mockRunnable, times(0)).run();

    shadowLooper.idle();
    verify(mockRunnable, times(1)).run();
  }

  @Test
  public void idle_executesTask_andIdleHandler() {
    ShadowPausedLooper shadowLooper = Shadow.extract(getMainLooper());
    Runnable mockRunnable = mock(Runnable.class);
    IdleHandler mockIdleHandler = mock(IdleHandler.class);
    getMainLooper().getQueue().addIdleHandler(mockIdleHandler);
    Handler mainHandler = new Handler();
    mainHandler.post(mockRunnable);
    verify(mockRunnable, times(0)).run();
    verify(mockIdleHandler, times(0)).queueIdle();

    shadowLooper.idle();
    verify(mockRunnable, times(1)).run();
    verify(mockIdleHandler, times(1)).queueIdle();
  }

  @Test
  public void idle_executesTask_andIdleHandler_removesIdleHandler() {
    ShadowPausedLooper shadowLooper = Shadow.extract(getMainLooper());
    Runnable mockRunnable = mock(Runnable.class);
    IdleHandler mockIdleHandler = mock(IdleHandler.class);
    getMainLooper().getQueue().addIdleHandler(mockIdleHandler);
    Handler mainHandler = new Handler();
    mainHandler.post(mockRunnable);
    verify(mockRunnable, times(0)).run();
    verify(mockIdleHandler, times(0)).queueIdle();

    shadowLooper.idle();
    verify(mockRunnable, times(1)).run();
    verify(mockIdleHandler, times(1)).queueIdle();

    mainHandler.post(mockRunnable);
    shadowLooper.idle();
    verify(mockRunnable, times(2)).run();
    verify(mockIdleHandler, times(1)).queueIdle(); // It was not kept, does not run again.
  }

  @Test
  public void idle_executesTask_andIdleHandler_keepsIdleHandler() {
    ShadowPausedLooper shadowLooper = Shadow.extract(getMainLooper());
    Runnable mockRunnable = mock(Runnable.class);
    IdleHandler mockIdleHandler = mock(IdleHandler.class);
    when(mockIdleHandler.queueIdle()).thenReturn(true);
    getMainLooper().getQueue().addIdleHandler(mockIdleHandler);
    Handler mainHandler = new Handler();
    mainHandler.post(mockRunnable);
    verify(mockRunnable, times(0)).run();
    verify(mockIdleHandler, times(0)).queueIdle();

    shadowLooper.idle();
    verify(mockRunnable, times(1)).run();
    verify(mockIdleHandler, times(1)).queueIdle();

    mainHandler.post(mockRunnable);
    shadowLooper.idle();
    verify(mockRunnable, times(2)).run();
    verify(mockIdleHandler, times(2)).queueIdle(); // It was kept and runs again
  }

  @Test
  public void runOneTask_executesTask_andIdleHandler() {
    ShadowPausedLooper shadowLooper = Shadow.extract(getMainLooper());
    Runnable mockRunnable = mock(Runnable.class);
    IdleHandler mockIdleHandler = mock(IdleHandler.class);
    getMainLooper().getQueue().addIdleHandler(mockIdleHandler);
    Handler mainHandler = new Handler();
    mainHandler.post(mockRunnable);
    verify(mockRunnable, times(0)).run();
    verify(mockIdleHandler, times(0)).queueIdle();

    shadowLooper.runOneTask();
    verify(mockRunnable, times(1)).run();
    verify(mockIdleHandler, times(1)).queueIdle();
  }

  @Test
  public void runOneTask_executesTwoTasks_thenIdleHandler() {
    ShadowPausedLooper shadowLooper = Shadow.extract(getMainLooper());
    Runnable mockRunnable = mock(Runnable.class);
    IdleHandler mockIdleHandler = mock(IdleHandler.class);
    getMainLooper().getQueue().addIdleHandler(mockIdleHandler);
    Handler mainHandler = new Handler();
    mainHandler.post(mockRunnable);
    mainHandler.post(mockRunnable);
    verify(mockRunnable, times(0)).run();
    verify(mockIdleHandler, times(0)).queueIdle();

    shadowLooper.runOneTask();
    verify(mockRunnable, times(1)).run();
    verify(mockIdleHandler, times(0)).queueIdle();

    shadowLooper.runOneTask();
    verify(mockRunnable, times(2)).run();
    verify(mockIdleHandler, times(1)).queueIdle();
  }

  @Test
  public void runOneTask_executesTask_andIdleHandler_removesIdleHandler() {
    ShadowPausedLooper shadowLooper = Shadow.extract(getMainLooper());
    Runnable mockRunnable = mock(Runnable.class);
    IdleHandler mockIdleHandler = mock(IdleHandler.class);
    getMainLooper().getQueue().addIdleHandler(mockIdleHandler);
    Handler mainHandler = new Handler();
    mainHandler.post(mockRunnable);
    verify(mockRunnable, times(0)).run();
    verify(mockIdleHandler, times(0)).queueIdle();

    shadowLooper.runOneTask();
    verify(mockRunnable, times(1)).run();
    verify(mockIdleHandler, times(1)).queueIdle();

    mainHandler.post(mockRunnable);
    shadowLooper.idle();
    verify(mockRunnable, times(2)).run();
    verify(mockIdleHandler, times(1)).queueIdle(); // It was not kept, does not run again.
  }

  @Test
  public void runOneTask_executesTask_andIdleHandler_keepsIdleHandler() {
    ShadowPausedLooper shadowLooper = Shadow.extract(getMainLooper());
    Runnable mockRunnable = mock(Runnable.class);
    IdleHandler mockIdleHandler = mock(IdleHandler.class);
    when(mockIdleHandler.queueIdle()).thenReturn(true);
    getMainLooper().getQueue().addIdleHandler(mockIdleHandler);
    Handler mainHandler = new Handler();
    mainHandler.post(mockRunnable);
    verify(mockRunnable, times(0)).run();
    verify(mockIdleHandler, times(0)).queueIdle();

    shadowLooper.runOneTask();
    verify(mockRunnable, times(1)).run();
    verify(mockIdleHandler, times(1)).queueIdle();

    mainHandler.post(mockRunnable);
    shadowLooper.runOneTask();
    verify(mockRunnable, times(2)).run();
    verify(mockIdleHandler, times(2)).queueIdle(); // It was kept and runs again
  }

  @Test
  public void idleFor_executesTask_mainLooper() {
    ShadowPausedLooper shadowLooper = Shadow.extract(getMainLooper());
    Runnable mockRunnable = mock(Runnable.class);
    Handler mainHandler = new Handler();
    mainHandler.postDelayed(mockRunnable, 100);
    verify(mockRunnable, times(0)).run();

    shadowLooper.idle();
    verify(mockRunnable, times(0)).run();

    shadowLooper.idleFor(Duration.ofMillis(200));
    verify(mockRunnable, times(1)).run();
  }

  @Test
  public void idleFor_incrementsTimeTaskByTask() {
    final Handler mainHandler = new Handler();

    Runnable mockRunnable = mock(Runnable.class);
    Runnable postingRunnable =
        () -> {
          mainHandler.postDelayed(mockRunnable, 100);
        };
    mainHandler.postDelayed(postingRunnable, 100);

    verify(mockRunnable, times(0)).run();

    shadowMainLooper().idleFor(Duration.ofMillis(200));
    verify(mockRunnable, times(1)).run();
  }

  @Test
  public void idleExecutesPostedRunnables() {
    ShadowPausedLooper shadowLooper = Shadow.extract(getMainLooper());
    Runnable mockRunnable = mock(Runnable.class);
    Runnable postingRunnable =
        () -> {
          Handler mainHandler = new Handler();
          mainHandler.post(mockRunnable);
        };
    Handler mainHandler = new Handler();
    mainHandler.post(postingRunnable);

    verify(mockRunnable, times(0)).run();
    shadowLooper.idle();
    verify(mockRunnable, times(1)).run();
  }

  @Test
  public void getNextScheduledTime() {
    assertThat(shadowMainLooper().getNextScheduledTaskTime()).isEqualTo(Duration.ZERO);
    Handler mainHandler = new Handler();
    mainHandler.postDelayed(() -> {}, 100);
    assertThat(shadowMainLooper().getNextScheduledTaskTime().toMillis())
        .isEqualTo(SystemClock.uptimeMillis() + 100);
  }

  @Test
  public void getLastScheduledTime() {
    assertThat(shadowMainLooper().getLastScheduledTaskTime()).isEqualTo(Duration.ZERO);
    Handler mainHandler = new Handler();
    mainHandler.postDelayed(() -> {}, 200);
    mainHandler.postDelayed(() -> {}, 100);
    assertThat(shadowMainLooper().getLastScheduledTaskTime().toMillis())
        .isEqualTo(SystemClock.uptimeMillis() + 200);
  }

  @Before
  public void assertMainLooperEmpty() {
    ShadowPausedMessageQueue queue = Shadow.extract(getMainLooper().getQueue());
    assertThat(queue.isIdle()).isTrue();
  }

  @Test
  public void mainLooperQueueIsCleared() {
    postToMainLooper();
  }

  @Test
  public void mainLooperQueueIsClearedB() {
    postToMainLooper();
  }

  @Test
  public void isIdle_mainLooper() {
    assertThat(shadowMainLooper().isIdle()).isTrue();
    Handler mainHandler = new Handler();
    mainHandler.post(() -> {});
    assertThat(shadowMainLooper().isIdle()).isFalse();
    shadowMainLooper().idle();
    assertThat(shadowMainLooper().isIdle()).isTrue();
  }

  @Test
  public void isIdle_delayed() {
    assertThat(shadowMainLooper().isIdle()).isTrue();
    Handler mainHandler = new Handler();
    mainHandler.postDelayed(() -> {}, 100);
    assertThat(shadowMainLooper().isIdle()).isTrue();
    ShadowSystemClock.advanceBy(Duration.ofMillis(100));
    assertThat(shadowMainLooper().isIdle()).isFalse();
  }

  @Test
  public void isIdle_taskExecuting() throws InterruptedException {
    BlockingRunnable runnable = new BlockingRunnable();
    Handler handler = new Handler(handlerThread.getLooper());
    handler.post(runnable);
    assertThat(shadowOf(handlerThread.getLooper()).isIdle()).isFalse();
    runnable.latch.countDown();
    // poll for isIdle to be true, since it will take some time for queue to clear
    for (int i = 0; i < 3 && !shadowOf(handlerThread.getLooper()).isIdle(); i++) {
      Thread.sleep(10);
    }
    assertThat(shadowOf(handlerThread.getLooper()).isIdle()).isTrue();
  }

  @Test
  public void isIdle_paused() throws InterruptedException {
    ShadowLooper shadowLooper = shadowOf(handlerThread.getLooper());
    shadowLooper.pause();
    assertThat(shadowLooper.isIdle()).isTrue();
    new Handler(handlerThread.getLooper()).post(mock(Runnable.class));
    assertThat(shadowLooper.isIdle()).isFalse();
    shadowOf(handlerThread.getLooper()).idle();
    assertThat(shadowLooper.isIdle()).isTrue();
  }

  @Test
  public void quitFromSameThread_releasesLooperThread() throws Exception {
    HandlerThread thread = new HandlerThread("WillBeQuit");
    thread.start();
    Looper looper = thread.getLooper();
    new Handler(looper).post(looper::quit);
    thread.join(5_000);
    assertThat(thread.getState()).isEqualTo(Thread.State.TERMINATED);
  }

  @Test
  public void quitPausedFromSameThread_releasesLooperThread() throws Exception {
    HandlerThread thread = new HandlerThread("WillBeQuit");
    thread.start();
    Looper looper = thread.getLooper();
    shadowOf(looper).pause();
    new Handler(looper).post(looper::quit);
    shadowOf(looper).idle();
    thread.join(5_000);
    assertThat(thread.getState()).isEqualTo(Thread.State.TERMINATED);
  }

  @Test
  public void quitPausedFromDifferentThread_releasesLooperThread() throws Exception {
    HandlerThread thread = new HandlerThread("WillBeQuit");
    thread.start();
    Looper looper = thread.getLooper();
    shadowOf(looper).pause();
    looper.quit();
    thread.join(5_000);
    assertThat(thread.getState()).isEqualTo(Thread.State.TERMINATED);
  }

  @Test
  public void idle_failsIfThreadQuit() {
    ShadowLooper shadowLooper = shadowOf(handlerThread.getLooper());
    handlerThread.quit();
    try {
      shadowLooper.idle();
      fail("IllegalStateException not thrown");
    } catch (IllegalStateException e) {
      // expected
    }
  }

  @Test
  public void resetter_allowsStaticHandlerThreadsToBeReused() throws Exception {
    Handler handler = new Handler(handlerThread.getLooper());
    CountDownLatch countDownLatch1 = new CountDownLatch(1);
    handler.post(countDownLatch1::countDown);
    assertThat(countDownLatch1.await(30, SECONDS)).isTrue();
    ShadowPausedLooper.resetLoopers();
    CountDownLatch countDownLatch2 = new CountDownLatch(1);
    handler.post(countDownLatch2::countDown);
    assertThat(countDownLatch2.await(30, SECONDS)).isTrue();
  }

  @Test
  public void idle_looperPaused_idleHandlerThrowsException() throws Exception {
    Looper looper = handlerThread.getLooper();
    shadowOf(looper).pause();
    new Handler(looper)
        .post(
            () -> {
              Looper.myQueue()
                  .addIdleHandler(
                      () -> {
                        throw new IllegalStateException();
                      });
            });
    assertThrows(IllegalStateException.class, () -> shadowOf(looper).idle());
    handlerThread.join(5_000);
    assertThat(handlerThread.getState()).isEqualTo(Thread.State.TERMINATED);
  }

  @Test
  public void idle_looperPaused_runnableThrowsException() throws Exception {
    Looper looper = handlerThread.getLooper();
    shadowOf(looper).pause();
    new Handler(looper)
        .post(
            () -> {
              throw new IllegalStateException();
            });

    assertThrows(IllegalStateException.class, () -> shadowOf(looper).idle());
    handlerThread.join(5_000);
    assertThat(handlerThread.getState()).isEqualTo(Thread.State.TERMINATED);
  }

  @Test
  public void idle_looperRunning_runnableThrowsException() throws Exception {
    Looper looper = handlerThread.getLooper();
    new Handler(looper)
        .post(
            () -> {
              throw new IllegalStateException();
            });

    assertThrows(IllegalStateException.class, () -> shadowOf(looper).idle());
    handlerThread.join(5_000);
    assertThat(handlerThread.getState()).isEqualTo(Thread.State.TERMINATED);
  }

  @Test
  public void post_throws_if_looper_died() throws Exception {
    Looper looper = handlerThread.getLooper();
    new Handler(looper)
        .post(
            () -> {
              throw new IllegalStateException();
            });
    handlerThread.join(5_000);
    assertThat(handlerThread.getState()).isEqualTo(Thread.State.TERMINATED);

    assertThrows(IllegalStateException.class, () -> new Handler(looper).post(() -> {}));
  }

  @Test
  public void idle_throws_if_looper_died() throws Exception {
    Looper looper = handlerThread.getLooper();
    new Handler(looper)
        .post(
            () -> {
              throw new IllegalStateException();
            });
    handlerThread.join(5_000);
    assertThat(handlerThread.getState()).isEqualTo(Thread.State.TERMINATED);

    assertThrows(IllegalStateException.class, () -> shadowOf(looper).idle());
  }

  @Test
  public void poll() {
    ShadowPausedLooper shadowPausedLooper = Shadow.extract(Looper.getMainLooper());
    AtomicBoolean backgroundThreadPosted = new AtomicBoolean();
    AtomicBoolean foregroundThreadReceived = new AtomicBoolean();
    shadowPausedLooper.idle();

    new Handler(handlerThread.getLooper())
        .post(
            () -> {
              backgroundThreadPosted.set(true);
              new Handler(Looper.getMainLooper()).post(() -> foregroundThreadReceived.set(true));
            });
    shadowPausedLooper.poll(0);
    shadowPausedLooper.idle();

    assertThat(backgroundThreadPosted.get()).isTrue();
    assertThat(foregroundThreadReceived.get()).isTrue();
  }

  @Test
  @Config(minSdk = VERSION_CODES.M)
  public void runOneTask_ignoreSyncBarrier() {
    int barrier = Looper.getMainLooper().getQueue().postSyncBarrier();

    final AtomicBoolean wasRun = new AtomicBoolean(false);
    new Handler(Looper.getMainLooper()).post(() -> wasRun.set(true));

    ShadowPausedLooper shadowPausedLooper = Shadow.extract(Looper.getMainLooper());
    shadowPausedLooper.runOneTask();

    // tasks should not be executed when blocked by a sync barrier
    assertThat(wasRun.get()).isFalse();
    // sync barrier will throw if the barrier was not found.
    Looper.getMainLooper().getQueue().removeSyncBarrier(barrier);

    shadowPausedLooper.runOneTask();
    assertThat(wasRun.get()).isTrue();
  }

  /**
   * Tests a race condition that could occur if a paused background Looper was quit but the thread
   * was still alive. The resetter would attempt to unpause it, but the message would never run
   * because the looper was quit. This caused a deadlock.
   */
  @Test
  public void looper_customThread_unPauseAfterQuit() throws Exception {
    for (int i = 0; i < 100; i++) {
      final SettableFuture<Looper> future = SettableFuture.create();
      final CountDownLatch countDownLatch = new CountDownLatch(1);

      Thread t =
          new Thread(
              () -> {
                try {
                  Looper.prepare();
                } finally {
                  future.set(Looper.myLooper());
                }
                Looper.loop();
                try {
                  countDownLatch.await();
                } catch (InterruptedException e) {
                  Thread.currentThread().interrupt();
                }
              });
      t.start();
      Looper looper = future.get();
      shadowOf(looper).pause();
      new Handler(looper).post(() -> looper.quitSafely());
      shadowOf(looper).idle();
      ((ShadowPausedLooper) shadowOf(looper)).resetLooperToInitialState();
      countDownLatch.countDown();
    }
  }

  @Test
  @Config(minSdk = VERSION_CODES.P)
  public void runOneTask_ignoreSyncBarrier_with_async() {
    int barrier = Looper.getMainLooper().getQueue().postSyncBarrier();

    final AtomicBoolean wasRun = new AtomicBoolean(false);
    Handler.createAsync(Looper.getMainLooper()).post(() -> wasRun.set(true));

    ShadowPausedLooper shadowPausedLooper = Shadow.extract(Looper.getMainLooper());
    shadowPausedLooper.runOneTask();

    // tasks should be executed as the handler is async
    assertThat(wasRun.get()).isTrue();
    // sync barrier will throw if the barrier was not found.
    Looper.getMainLooper().getQueue().removeSyncBarrier(barrier);
  }

  /**
   * Verify that calling a control operation like idle while a sync barrier is being held doesn't
   * deadlock the looper
   */
  @Test
  public void idle_paused_onSyncBarrier() {

    Handler handler = new Handler(handlerThread.getLooper());
    Handler asyncHandler = ShadowPausedLooper.createAsyncHandler(handlerThread.getLooper());
    ShadowPausedLooper shadowLooper = Shadow.extract(handlerThread.getLooper());
    shadowLooper.pause();

    AtomicInteger token = new AtomicInteger(-1);
    AtomicBoolean wasRun = new AtomicBoolean(false);
    handler.post(
        () -> {
          token.set(postSyncBarrierCompat(handlerThread.getLooper()));
          handler.post(
              () -> {
                wasRun.set(true);
              });
        });
    shadowLooper.idle();
    assertThat(token.get()).isNotEqualTo(-1);
    assertThat(wasRun.get()).isEqualTo(false);
    // should be effectively a no-op and not deadlock
    shadowLooper.idle();
    // remove sync barriers messages need to get posted as async
    asyncHandler.post(
        () -> {
          removeSyncBarrierCompat(handlerThread.getLooper(), token.get());
        });
    shadowLooper.idle();
    assertThat(wasRun.get()).isEqualTo(true);
  }

  /** Similar to previous test but with a running aka unpaused looper. */
  @Test
  public void idle_running_onSyncBarrier() {
    Handler handler = new Handler(handlerThread.getLooper());
    Handler asyncHandler = ShadowPausedLooper.createAsyncHandler(handlerThread.getLooper());
    ShadowPausedLooper shadowLooper = Shadow.extract(handlerThread.getLooper());

    AtomicInteger token = new AtomicInteger(-1);
    AtomicBoolean wasRun = new AtomicBoolean(false);
    handler.post(
        () -> {
          token.set(postSyncBarrierCompat(handlerThread.getLooper()));
          handler.post(
              () -> {
                wasRun.set(true);
              });
        });
    shadowLooper.idle();
    assertThat(token.get()).isNotEqualTo(-1);
    assertThat(wasRun.get()).isEqualTo(false);
    // should be effectively a no-op and not deadlock
    shadowLooper.idle();
    // remove sync barriers messages need to get posted as async
    asyncHandler.post(
        () -> {
          removeSyncBarrierCompat(handlerThread.getLooper(), token.get());
        });
    shadowLooper.idle();
    assertThat(wasRun.get()).isEqualTo(true);
  }

  private static class BlockingRunnable implements Runnable {
    CountDownLatch latch = new CountDownLatch(1);

    @Override
    public void run() {
      try {
        latch.await();
      } catch (InterruptedException e) {
      }
    }
  }

  private void postToMainLooper() {
    // just post a runnable and rely on setUp to check
    Handler handler = new Handler(getMainLooper());
    handler.post(() -> {});
  }

  private static int postSyncBarrierCompat(Looper looper) {
    if (RuntimeEnvironment.getApiLevel() >= 23) {
      return looper.getQueue().postSyncBarrier();
    } else {
      return reflector(LooperReflector.class, looper).postSyncBarrier();
    }
  }

  private static void removeSyncBarrierCompat(Looper looper, int token) {
    if (RuntimeEnvironment.getApiLevel() >= 23) {
      looper.getQueue().removeSyncBarrier(token);
    } else {
      reflector(LooperReflector.class, looper).removeSyncBarrier(token);
    }
  }

  @ForType(Looper.class)
  private interface LooperReflector {
    @Direct
    int postSyncBarrier();

    @Direct
    void removeSyncBarrier(int token);
  }
}
