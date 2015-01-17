package org.robolectric.shadows;

import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.TestRunners;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.Scheduler;

import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;
import static org.robolectric.Shadows.shadowOf;

@RunWith(TestRunners.WithDefaults.class)
public class ShadowLooperTest {

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
    HandlerThread ht = new HandlerThread("shouldThrowawayRunnableQueueIfLooperQuits");
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
    assertThat(shadowOf(looper.getQueue()).getHead()).as("queue").isNull();
  }

  @Test
  public void shouldResetQueue_whenLooperIsReset() {
    HandlerThread ht = new HandlerThread("shouldResetQueue_whenLooperIsReset");
    ht.start();
    Looper looper = ht.getLooper();
    Handler h = new Handler(looper);
    ShadowLooper sLooper = shadowOf(looper);
    sLooper.pause();
    h.post(new Runnable() {
      @Override
      public void run() {
      }
    });
    assertThat(shadowOf(looper.getQueue()).getHead()).as("queue").isNotNull();
    sLooper.reset();
    assertFalse(sLooper.getScheduler().areAnyRunnable());
    assertThat(shadowOf(looper.getQueue()).getHead()).as("queue").isNull();
  }
  
  @Test
  public void testLoopThread() {
    assertTrue(shadowOf(Looper.getMainLooper()).getThread() == Thread.currentThread());
  }

  @Test public void soStaticRefsToLoopersInAppWorksAcrossTests_shouldRetainSameLooperForMainThreadBetweenResetsButGiveItAFreshScheduler() throws Exception {
    Looper mainLooper = Looper.getMainLooper();
    Scheduler scheduler = shadowOf(mainLooper).getScheduler();
    shadowOf(mainLooper).quit = true;
    assertThat(RuntimeEnvironment.application.getMainLooper()).isSameAs(mainLooper);

    ShadowLooper.resetThreadLoopers();
    Application application = new Application();
    ReflectionHelpers.callInstanceMethod(application, "attach", ReflectionHelpers.ClassParameter.from(Context.class, RuntimeEnvironment.application.getBaseContext()));

    assertThat(Looper.getMainLooper()).isSameAs(mainLooper);
    assertThat(application.getMainLooper()).isSameAs(mainLooper);
    assertThat(shadowOf(mainLooper).getScheduler()).isNotSameAs(scheduler);
    assertThat(shadowOf(mainLooper).hasQuit()).isFalse();
  }

  @Test
  public void getMainLooperReturnsNonNullOnMainThreadWhenRobolectricApplicationIsNull() {
      RuntimeEnvironment.application = null;
      assertNotNull(Looper.getMainLooper());
  }

  @Test
  public void getMainLooperThrowsNullPointerExceptionOnBackgroundThreadWhenRobolectricApplicationIsNull() throws Exception {
      RuntimeEnvironment.application = null;
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
