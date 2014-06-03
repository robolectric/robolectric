package org.robolectric.shadows;

import android.app.Application;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.TestRunners;
import org.robolectric.util.Scheduler;

import java.util.concurrent.atomic.AtomicReference;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.*;
import static org.robolectric.Robolectric.shadowOf;

@RunWith(TestRunners.WithDefaults.class)
public class LooperTest {

  @Test
  public void testMainLooperAndMyLooperAreTheSameInstanceOnMainThread() throws Exception {
    assertSame(Looper.myLooper(), Looper.getMainLooper());
  }

  @Test
  public void idleMainLooper_executesScheduledTasks() {
    final boolean[] wasRun = new boolean[]{false};
    new Handler().postDelayed(new Runnable() {
      @Override
      public void run() {
        wasRun[0] = true;
      }
    }, 2000);

    assertFalse(wasRun[0]);
    ShadowLooper.idleMainLooper(1999);
    assertFalse(wasRun[0]);
    ShadowLooper.idleMainLooper(1);
    assertTrue(wasRun[0]);
  }

  @Test
  public void idleConstantly_runsPostDelayedTasksImmediately() {
    ShadowLooper.idleMainLooperConstantly(true);
    final boolean[] wasRun = new boolean[]{false};
    new Handler().postDelayed(new Runnable() {
      @Override
      public void run() {
        wasRun[0] = true;
      }
    }, 2000);

    assertTrue(wasRun[0]);
  }

  @Test
  public void differentThreadsGetDifferentLoopers() {
    Looper mainLooper = Looper.getMainLooper();
    Looper thisThreadsLooper = Looper.myLooper();

    assertSame("junit test's thread should use the main looper", mainLooper, thisThreadsLooper);

    final Looper[] thread1Looper = new Looper[1];
    new Thread() {
      @Override
      public void run() {
        Looper.prepare();
        thread1Looper[0] = Looper.myLooper();
      }
    }.start();

    while(thread1Looper[0] == null) {
      Thread.yield();
    }

    assertNotSame(mainLooper, thread1Looper[0]);
  }

  @Test(expected = RuntimeException.class)
  public void shouldThrowRuntimeExceptionIfTryingToQuitMainLooper() throws Exception {
    Looper.getMainLooper().quit();
  }

  @Test
  public void shouldNotQueueMessagesIfLooperIsQuit() throws Exception {
    HandlerThread ht = new HandlerThread("test1");
    ht.start();
    Looper looper = ht.getLooper();
    looper.quit();
    assertTrue(shadowOf(looper).hasQuit());
    assertFalse(shadowOf(looper).post(new Runnable() {
      @Override public void run() { }
    }, 0));

    assertFalse(shadowOf(looper).postAtFrontOfQueue(new Runnable() {
      @Override
      public void run() {
      }
    }));
    assertFalse(shadowOf(looper).getScheduler().areAnyRunnable());
  }

  @Test
  public void shouldThrowawayRunnableQueueIfLooperQuits() throws Exception {
    HandlerThread ht = new HandlerThread("test1");
    ht.start();
    Looper looper = ht.getLooper();
    shadowOf(looper).pause();
    shadowOf(looper).post(new Runnable() {
      @Override
      public void run() {
      }
    }, 0);
    looper.quit();
    assertTrue(shadowOf(looper).hasQuit());
    assertFalse(shadowOf(looper).getScheduler().areAnyRunnable());
  }

  @Test
  public void testLoopThread() {
    assertTrue(shadowOf(Looper.getMainLooper()).getThread() == Thread.currentThread());
  }

  @Test public void soStaticRefsToLoopersInAppWorksAcrossTests_shouldRetainSameLooperForMainThreadBetweenResetsButGiveItAFreshScheduler() throws Exception {
    Looper mainLooper = Looper.getMainLooper();
    Scheduler scheduler = shadowOf(mainLooper).getScheduler();
    shadowOf(mainLooper).quit = true;
    assertThat(Robolectric.application.getMainLooper()).isSameAs(mainLooper);

    ShadowLooper.resetThreadLoopers();
    Robolectric.application = new Application();

    assertThat(Looper.getMainLooper()).isSameAs(mainLooper);
    assertThat(Robolectric.application.getMainLooper()).isSameAs(mainLooper);
    assertThat(shadowOf(mainLooper).getScheduler()).isNotSameAs(scheduler);
    assertThat(shadowOf(mainLooper).hasQuit()).isFalse();
  }

  @Test
  public void getMainLooperReturnsNonNullOnMainThreadWhenRobolectricApplicationIsNull() {
      Robolectric.application = null;
      assertNotNull(Looper.getMainLooper());
  }

  @Test
  public void getMainLooperThrowsNullPointerExceptionOnBackgroundThreadWhenRobolectricApplicationIsNull() throws Exception {
      Robolectric.application = null;
      final AtomicReference<Looper> mainLooperAtomicReference = new AtomicReference<Looper>();
      final AtomicReference<NullPointerException> nullPointerExceptionAtomicReference = new AtomicReference<NullPointerException>();

      Thread backgroundThread = new Thread(new Runnable() {
          @Override
          public void run() {
              try {
                  Looper mainLooper = Looper.getMainLooper();
                  mainLooperAtomicReference.set(mainLooper);
              } catch (NullPointerException nullPointerException) {
                  nullPointerExceptionAtomicReference.set(nullPointerException);
              }
          }
      });
      backgroundThread.start();
      backgroundThread.join();

      assertThat(nullPointerExceptionAtomicReference.get()).isInstanceOf(NullPointerException.class);
      assertNull(mainLooperAtomicReference.get());
  }
}
