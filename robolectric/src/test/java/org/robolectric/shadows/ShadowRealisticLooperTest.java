package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.TruthJUnit.assume;
import static java.util.concurrent.Executors.newSingleThreadExecutor;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.robolectric.shadows.ShadowBaseLooper.shadowMainLooper;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.SystemClock;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;
import org.robolectric.shadow.api.Shadow;

@RunWith(AndroidJUnit4.class)
public class ShadowRealisticLooperTest {

  // testName is used when creating background threads. Makes it
  // easier to debug exceptions on background threads when you
  // know what test they are associated with.
  @Rule public TestName testName = new TestName();

  // Helper method that starts the thread with the same name as the
  // current test, so that you will know which test invoked it if
  // it has an exception.
  private HandlerThread getHandlerThread() {
    HandlerThread ht = new HandlerThread(testName.getMethodName());
    ht.start();
    return ht;
  }

  @Before
  public void skipIfLegacyLooper() {
    assume().that(ShadowBaseLooper.useRealisticLooper()).isTrue();
  }

  @Test
  public void mainLooper_andMyLooper_shouldBeSame_onMainThread() {
    assertThat(Looper.myLooper()).isSameAs(Looper.getMainLooper());
  }

  @Test
  public void differentThreads_getDifferentLoopers() {
    HandlerThread ht = getHandlerThread();
    assertThat(ht.getLooper()).isNotSameAs(Looper.getMainLooper());
    ht.quit();
  }

  @Test
  public void mainLooperThread_shouldBeTestThread() {
    assertThat(Looper.getMainLooper().getThread()).isSameAs(Thread.currentThread());
  }

  @Test(timeout = 200)
  public void junitTimeoutTestRunsOnMainThread() {
    assertThat(Looper.getMainLooper().getThread()).isSameAs(Thread.currentThread());
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
    HandlerThread ht = getHandlerThread();
    try {
      Handler handler = new Handler(ht.getLooper());
      handler.post(mockRunnable);
      ShadowRealisticLooper shadowLooper = Shadow.extract(ht.getLooper());
      shadowLooper.idle();
      verify(mockRunnable, times(1)).run();
    } finally {
      ht.quit();
    }
  }

  @Test
  public void postedDelayedBackgroundLooperTasksAreExecutedOnlyWhenSystemClockAdvanced()
      throws InterruptedException {
    Runnable mockRunnable = mock(Runnable.class);
    HandlerThread ht = getHandlerThread();
    try {
      Handler handler = new Handler(ht.getLooper());
      handler.postDelayed(mockRunnable, 10);
      ShadowRealisticLooper shadowLooper = Shadow.extract(ht.getLooper());
      shadowLooper.idle();
      verify(mockRunnable, times(0)).run();
      SystemClock.setCurrentTimeMillis(SystemClock.uptimeMillis() + 100);
      shadowLooper.idle();
      verify(mockRunnable, times(1)).run();
    } finally {
      ht.quit();
    }
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
    ShadowRealisticLooper shadowLooper = Shadow.extract(Looper.getMainLooper());
    shadowLooper.idle();
  }

  @Test
  public void idle_executesTask_mainLooper() {
    ShadowRealisticLooper shadowLooper = Shadow.extract(Looper.getMainLooper());
    Runnable mockRunnable = mock(Runnable.class);
    Handler mainHandler = new Handler();
    mainHandler.post(mockRunnable);
    verify(mockRunnable, times(0)).run();

    shadowLooper.idle();
    verify(mockRunnable, times(1)).run();
  }

  @Test
  public void idleFor_executesTask_mainLooper() {
    ShadowRealisticLooper shadowLooper = Shadow.extract(Looper.getMainLooper());
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
  public void idleExecutesPostedRunnables() {
    ShadowRealisticLooper shadowLooper = Shadow.extract(Looper.getMainLooper());
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
  public void isIdle() {
    assertThat(shadowMainLooper().isIdle()).isTrue();
    Handler mainHandler = new Handler();
    mainHandler.post(() -> {});
    assertThat(shadowMainLooper().isIdle()).isFalse();
    shadowMainLooper().idle();
    assertThat(shadowMainLooper().isIdle()).isTrue();
  }

  @Before
  public void assertMainLooperEmpty() {
    assertThat(ShadowRealisticLooper.isMainLooperIdle()).isTrue();
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
    Handler handler = new Handler(Looper.getMainLooper());
    Runnable mockRunnable = mock(Runnable.class);
    handler.post(mockRunnable);
  }
}
