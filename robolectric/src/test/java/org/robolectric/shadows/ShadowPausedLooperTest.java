package org.robolectric.shadows;

import static android.os.Looper.getMainLooper;
import static com.google.common.truth.Truth.assertThat;
import static java.util.concurrent.Executors.newSingleThreadExecutor;
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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
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
  public void mainLooper_andMyLooper_shouldBeSame_onMainThread() {
    assertThat(Looper.myLooper()).isSameAs(getMainLooper());
  }

  @Test
  public void differentThreads_getDifferentLoopers() {
    assertThat(handlerThread.getLooper()).isNotSameAs(getMainLooper());
    handlerThread.quit();
  }

  @Test
  public void mainLooperThread_shouldBeTestThread() {
    assertThat(getMainLooper().getThread()).isSameAs(Thread.currentThread());
  }

  @Test(timeout = 200)
  public void junitTimeoutTestRunsOnMainThread() {
    assertThat(getMainLooper().getThread()).isSameAs(Thread.currentThread());
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

    shadowLooper.idleFor(200, TimeUnit.MILLISECONDS);
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

    shadowMainLooper().idleFor(200, TimeUnit.MILLISECONDS);
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
    assertThat(shadowMainLooper().getNextScheduledTaskTime().toMillis()).isEqualTo(SystemClock.uptimeMillis() + 100);
  }

  @Test
  public void getLastScheduledTime() {
    assertThat(shadowMainLooper().getLastScheduledTaskTime()).isEqualTo(Duration.ZERO);
    Handler mainHandler = new Handler();
    mainHandler.postDelayed(() -> {}, 200);
    mainHandler.postDelayed(() -> {}, 100);
    assertThat(shadowMainLooper().getLastScheduledTaskTime().toMillis()).isEqualTo(SystemClock.uptimeMillis() + 200);
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

  private void postToMainLooper() {
    // just post a runnable and rely on setUp to check
    Handler handler = new Handler(getMainLooper());
    Runnable mockRunnable = mock(Runnable.class);
    handler.post(mockRunnable);
  }
}
