package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class ControlledLooperTest {

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

  private ControlledLooper controlledLooper;
  private HandlerThread handlerThread;
  private Handler handler;
  private List<String> events;

  @Before
  public void acquire() {
    handlerThread = getHandlerThread();
    handler = new Handler(handlerThread.getLooper());
    controlledLooper = ControlledLooper.register(handlerThread.getLooper());
    assertThat(controlledLooper).isNotNull();
    events = new ArrayList<>();
  }

  @After
  public void release() {
    handlerThread.quit();
  }

  @Test
  public void idle_initial() {
    controlledLooper.idle();
  }

  @Test
  public void idle_executesTask() {
    Runnable mockRunnable = mock(Runnable.class);
    handler.post(mockRunnable);
    verify(mockRunnable, times(0)).run();

    controlledLooper.idle();
    verify(mockRunnable, times(1)).run();
  }

  @Test
  public void idle_futureTask() {
    Runnable mockRunnable = mock(Runnable.class);
    handler.postDelayed(mockRunnable, 100);
    controlledLooper.idle();
    verify(mockRunnable, times(0)).run();

    controlledLooper.idleFor(100L, TimeUnit.MILLISECONDS);
    verify(mockRunnable, times(1)).run();
  }

  @Test
  public void taskCanAdvanceCurrentLooper() {
    handler.post(new IdlingRunnable("task1"));
    handler.postDelayed(new IdlingRunnable("task2"), 100);
    controlledLooper.idle();
    assertThat(events).containsExactlyElementsIn(Arrays.asList("task1", "task2")).inOrder();
  }

  @Test
  public void idle_mainLooper() {
    ControlledLooper controlledMainLooper = ControlledLooper.get(Looper.getMainLooper());
    controlledMainLooper.idle();
  }

  @Test
  public void idle_executesTask_mainLooper() {
    ControlledLooper controlledMainLooper = ControlledLooper.get(Looper.getMainLooper());
    Runnable mockRunnable = mock(Runnable.class);
    Handler mainHandler = new Handler();
    mainHandler.post(mockRunnable);
    verify(mockRunnable, times(0)).run();

    controlledMainLooper.idle();
    verify(mockRunnable, times(1)).run();
  }

  private class IdlingRunnable implements Runnable {
    private final String tag;

    IdlingRunnable(String tag) {
      this.tag = tag;
    }

    @Override
    public void run() {
      events.add(tag);
      controlledLooper.idleFor(100L, TimeUnit.MILLISECONDS);
    }
  }
}
