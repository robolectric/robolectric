package org.robolectric.shadows;

import static android.os.Looper.getMainLooper;
import static com.google.common.truth.Truth.assertThat;
import static java.util.concurrent.Executors.newSingleThreadExecutor;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.robolectric.Shadows.shadowOf;
import static org.robolectric.shadows.ShadowLooper.shadowMainLooper;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.SystemClock;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;
import org.robolectric.annotation.LooperMode;
import org.robolectric.res.android.Ref;
import org.robolectric.shadow.api.Shadow;

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
  public void quitHandlerThread() {
    handlerThread.quit();
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
    SystemClock.setCurrentTimeMillis(SystemClock.uptimeMillis() + 100);
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
    Runnable mockRunnable = mock(Runnable.class);
    handler.post(mockRunnable);
  }
}
