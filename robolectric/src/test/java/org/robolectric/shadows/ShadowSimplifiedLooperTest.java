package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.SystemClock;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.concurrent.TimeUnit;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;
import org.robolectric.shadow.api.Shadow;

@RunWith(AndroidJUnit4.class)
public class ShadowSimplifiedLooperTest {

  // testName is used when creating background threads. Makes it
  // easier to debug exceptions on background threads when you
  // know what test they are associated with.
  @Rule
  public TestName testName = new TestName();

  // Helper method that starts the thread with the same name as the
  // current test, so that you will know which test invoked it if
  // it has an exception.
  private HandlerThread getHandlerThread() {
    HandlerThread ht = new HandlerThread(testName.getMethodName());
    ht.start();
    return ht;
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
    Handler handler = new Handler(ht.getLooper());
    handler.post(mockRunnable);
    verify(mockRunnable, timeout(100).times(1)).run();
    ht.quit();
  }

  @Test
  public void postedDelayedBackgroundLooperTasksAreExecutedOnlyWhenSystemClockAdvanced()
      throws InterruptedException {
    Runnable mockRunnable = mock(Runnable.class);
    HandlerThread ht = getHandlerThread();
    Handler handler = new Handler(ht.getLooper());
    handler.postDelayed(mockRunnable, 10);
    verify(mockRunnable, timeout(20).times(0)).run();
    SystemClock.setCurrentTimeMillis(SystemClock.uptimeMillis() + 100);
    verify(mockRunnable, timeout(100).times(1)).run();
    ht.quit();
  }

  @Test
  public void idle_mainLooper() {
    ShadowSimplifiedLooper shadowLooper = Shadow.extract(Looper.getMainLooper());
    shadowLooper.idle();
  }

  @Test
  public void idle_executesTask_mainLooper() {
    ShadowSimplifiedLooper shadowLooper = Shadow.extract(Looper.getMainLooper());
    Runnable mockRunnable = mock(Runnable.class);
    Handler mainHandler = new Handler();
    mainHandler.post(mockRunnable);
    verify(mockRunnable, times(0)).run();

    shadowLooper.idle();
    verify(mockRunnable, times(1)).run();
  }

  @Test
  public void idleFor_executesTask_mainLooper() {
    ShadowSimplifiedLooper shadowLooper = Shadow.extract(Looper.getMainLooper());
    Runnable mockRunnable = mock(Runnable.class);
    Handler mainHandler = new Handler();
    mainHandler.postDelayed(mockRunnable, 100);
    verify(mockRunnable, times(0)).run();

    shadowLooper.idle();
    verify(mockRunnable, times(0)).run();

    shadowLooper.idleFor(100, TimeUnit.MILLISECONDS);
    verify(mockRunnable, times(1)).run();
  }
}
