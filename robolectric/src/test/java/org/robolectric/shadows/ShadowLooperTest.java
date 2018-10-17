package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;
import org.robolectric.RoboSettings;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.Scheduler;

@RunWith(AndroidJUnit4.class)
public class ShadowLooperTest {

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

  // Useful class for checking that a thread's loop() has exited.
  private class QuitThread extends Thread {
    private boolean hasContinued = false;
    private Looper looper;
    private CountDownLatch started = new CountDownLatch(1);
    
    public QuitThread() {
      super(testName.getMethodName());
    }
    
    @Override
    public void run() {
      Looper.prepare();
      looper = Looper.myLooper();
      started.countDown();
      Looper.loop();
      hasContinued = true;
    }
  }
  
  private QuitThread getQuitThread() throws InterruptedException {
    QuitThread qt = new QuitThread();
    qt.start();
    qt.started.await();
    return qt;
  }
  
  @Test
  public void mainLooper_andMyLooper_shouldBeSame_onMainThread() {
    assertThat(Looper.myLooper()).isSameAs(Looper.getMainLooper());
  }

  @Test
  public void differentThreads_getDifferentLoopers() {
    HandlerThread ht = getHandlerThread();
    assertThat(ht.getLooper()).isNotSameAs(Looper.getMainLooper());
  }

  @Test
  public void mainLooperThread_shouldBeTestThread() {
    assertThat(Looper.getMainLooper().getThread()).isSameAs(Thread.currentThread());
  }

  @Test
  public void shadowMainLooper_shouldBeShadowOfMainLooper() {
    assertThat(ShadowLooper.getShadowMainLooper()).isSameAs(shadowOf(Looper.getMainLooper()));
  }
  
  @Test
  public void getLooperForThread_returnsLooperForAThreadThatHasOne() throws InterruptedException {
    QuitThread qt = getQuitThread();
    assertThat(ShadowLooper.getLooperForThread(qt)).isSameAs(qt.looper);
  }
  
  @Test
  public void getLooperForThread_returnsLooperForMainThread() {
    assertThat(ShadowLooper.getLooperForThread(Thread.currentThread())).isSameAs(Looper.getMainLooper());
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

    assertThat(wasRun[0]).named("first").isFalse();
    ShadowLooper.idleMainLooper(1999);
    assertThat(wasRun[0]).named("second").isFalse();
    ShadowLooper.idleMainLooper(1);
    assertThat(wasRun[0]).named("last").isTrue();
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

    assertThat(wasRun[0]).isTrue();
  }

  @Test(expected = RuntimeException.class)
  public void shouldThrowRuntimeExceptionIfTryingToQuitMainLooper() throws Exception {
    Looper.getMainLooper().quit();
  }

  @Test
  public void shouldNotQueueMessagesIfLooperIsQuit() throws Exception {
    HandlerThread ht = getHandlerThread();
    Looper looper = ht.getLooper();
    looper.quit();
    assertThat(shadowOf(looper).hasQuit()).named("hasQuit").isTrue();
    assertThat(shadowOf(looper).post(new Runnable() {
      @Override
      public void run() {
      }
    }, 0)).named("post").isFalse();

    assertThat(shadowOf(looper).postAtFrontOfQueue(new Runnable() {
      @Override
      public void run() {
      }
    })).named("postAtFrontOfQueue").isFalse();
    assertThat(shadowOf(looper).getScheduler().areAnyRunnable()).named("areAnyRunnable").isFalse();
  }

  @Test
  public void shouldThrowawayRunnableQueueIfLooperQuits() throws Exception {
    HandlerThread ht = getHandlerThread();
    Looper looper = ht.getLooper();
    shadowOf(looper).pause();
    shadowOf(looper).post(new Runnable() {
      @Override
      public void run() {
      }
    }, 0);
    looper.quit();
    assertThat(shadowOf(looper).hasQuit()).named("hasQuit").isTrue();
    assertThat(shadowOf(looper).getScheduler().areAnyRunnable()).named("areAnyRunnable").isFalse();
    assertThat(shadowOf(looper.getQueue()).getHead()).named("queue").isNull();
  }

  @Test
  public void threadShouldContinue_whenLooperQuits() throws InterruptedException {
    QuitThread test = getQuitThread();
    assertThat(test.hasContinued).named("beforeJoin").isFalse();
    test.looper.quit();
    test.join(5000);
    assertThat(test.hasContinued).named("afterJoin").isTrue();
  }

  @Test
  public void shouldResetQueue_whenLooperIsReset() {
    HandlerThread ht = getHandlerThread();
    Looper looper = ht.getLooper();
    Handler h = new Handler(looper);
    ShadowLooper sLooper = shadowOf(looper);
    sLooper.pause();
    h.post(new Runnable() {
      @Override
      public void run() {
      }
    });
    assertThat(shadowOf(looper.getQueue()).getHead()).named("queue").isNotNull();
    sLooper.reset();
    assertThat(sLooper.getScheduler().areAnyRunnable()).named("areAnyRunnable").isFalse();
    assertThat(shadowOf(looper.getQueue()).getHead()).named("queue").isNull();
  }

  @Test
  public void shouldSetNewScheduler_whenLooperIsReset() {
    HandlerThread ht = getHandlerThread();
    Looper looper = ht.getLooper();
    ShadowLooper sLooper = shadowOf(looper);
    Scheduler old = sLooper.getScheduler();
    sLooper.reset();
    assertThat(old).isNotSameAs(sLooper.getScheduler());
  }

  @Test
  public void resetThreadLoopers_shouldQuitAllNonMainLoopers() throws InterruptedException {
    QuitThread test = getQuitThread();
    assertThat(test.hasContinued).named("hasContinued:before").isFalse();
    ShadowLooper.resetThreadLoopers();
    test.join(5000);
    assertThat(test.hasContinued).named("hasContinued:after").isTrue();
  }
 
  @Test(timeout = 1000)
  public void whenTestHarnessUsesDifferentThread_shouldStillHaveMainLooper() {
    assertThat(Looper.myLooper()).isSameAs(Looper.getMainLooper());
  }
  
  @Test
  public void resetThreadLoopers_fromNonMainThread_shouldThrowISE() throws InterruptedException {
    final AtomicReference<Throwable> ex = new AtomicReference<>();
    Thread t = new Thread() {
      @Override
      public void run() {
        try {
          ShadowLooper.resetThreadLoopers();
        } catch (Throwable t) {
          ex.set(t);
        }
      }
    };
    t.start();
    t.join();
    assertThat(ex.get()).isInstanceOf(IllegalStateException.class);
  }
  
  @Test
  public void soStaticRefsToLoopersInAppWorksAcrossTests_shouldRetainSameLooperForMainThreadBetweenResetsButGiveItAFreshScheduler() throws Exception {
    Looper mainLooper = Looper.getMainLooper();
    Scheduler scheduler = shadowOf(mainLooper).getScheduler();
    shadowOf(mainLooper).quit = true;
    assertThat(ApplicationProvider.getApplicationContext().getMainLooper()).isSameAs(mainLooper);
    Scheduler s = new Scheduler();
    RuntimeEnvironment.setMasterScheduler(s);
    ShadowLooper.resetThreadLoopers();
    Application application = new Application();
    ReflectionHelpers.callInstanceMethod(
        application,
        "attach",
        ReflectionHelpers.ClassParameter.from(
            Context.class,
            ((Application) ApplicationProvider.getApplicationContext()).getBaseContext()));

    assertThat(Looper.getMainLooper()).named("Looper.getMainLooper()").isSameAs(mainLooper);
    assertThat(application.getMainLooper()).named("app.getMainLooper()").isSameAs(mainLooper);
    assertThat(shadowOf(mainLooper).getScheduler()).named("scheduler").isNotSameAs(scheduler);
    assertThat(shadowOf(mainLooper).getScheduler()).named("scheduler").isSameAs(s);
    assertThat(shadowOf(mainLooper).hasQuit()).named("quit").isFalse();
  }

  @Test
  public void getMainLooperReturnsNonNullOnMainThreadWhenRobolectricApplicationIsNull() {
    RuntimeEnvironment.application = null;
    assertThat(Looper.getMainLooper()).isNotNull();
  }

  private void setAdvancedScheduling() {
    RoboSettings.setUseGlobalScheduler(true);
  }

  @Test
  public void reset_setsGlobalScheduler_forMainLooper_byDefault() {
    ShadowLooper sMainLooper = ShadowLooper.getShadowMainLooper();
    Scheduler s = new Scheduler();
    RuntimeEnvironment.setMasterScheduler(s);
    sMainLooper.reset();
    assertThat(sMainLooper.getScheduler()).isSameAs(s);
  }

  @Test
  public void reset_setsGlobalScheduler_forMainLooper_withAdvancedScheduling() {
    setAdvancedScheduling();
    ShadowLooper sMainLooper = ShadowLooper.getShadowMainLooper();
    Scheduler s = new Scheduler();
    RuntimeEnvironment.setMasterScheduler(s);
    sMainLooper.reset();
    assertThat(sMainLooper.getScheduler()).isSameAs(s);
  }

  @Test
  public void reset_setsNewScheduler_forNonMainLooper_byDefault() {
    HandlerThread ht = getHandlerThread();
    ShadowLooper sLooper = shadowOf(ht.getLooper());
    Scheduler old = sLooper.getScheduler();
    sLooper.reset();
    assertThat(sLooper.getScheduler()).isNotSameAs(old);
    assertThat(sLooper.getScheduler()).isNotSameAs(RuntimeEnvironment.getMasterScheduler());
  }

  @Test
  public void reset_setsSchedulerToMaster_forNonMainLooper_withAdvancedScheduling() {
    HandlerThread ht = getHandlerThread();
    ShadowLooper sLooper = shadowOf(ht.getLooper());
    Scheduler s = new Scheduler();
    RuntimeEnvironment.setMasterScheduler(s);
    setAdvancedScheduling();
    sLooper.reset();
    assertThat(sLooper.getScheduler()).isSameAs(s);
  }

  @Test
  public void resetThreadLoopers_resets_background_thread_schedulers() {
    HandlerThread backgroundThread = new HandlerThread("resetTest");
    backgroundThread.start();
    Looper backgroundLooper = backgroundThread.getLooper();
    Handler handler = new Handler(backgroundLooper);
    Runnable empty = new Runnable() {
      @Override
      public void run() {}
    };
    // There should be at least two iterations of this loop because resetThreadLoopers calls
    // 'quit' on background loopers once, which also resets the scheduler.
    for (int i = 0; i < 5; i++) {
      assertThat(shadowOf(backgroundLooper).getScheduler().size()).isEqualTo(0);
      assertThat(shadowOf(backgroundLooper).getScheduler().getCurrentTime()).isEqualTo(100L);
      handler.post(empty);
      handler.postDelayed(empty, 5000);
      // increment scheduler's time by 5000
      shadowOf(backgroundLooper).runToEndOfTasks();
      assertThat(shadowOf(backgroundLooper).getScheduler().getCurrentTime()).isEqualTo(5100L);
      ShadowLooper.resetThreadLoopers();
    }
  }

  @Test
  public void myLooper_returnsMainLooper_ifMainThreadIsSwitched() throws InterruptedException {
    final AtomicReference<Looper> myLooper = new AtomicReference<>();
    Thread t = new Thread(testName.getMethodName()) {
      @Override
      public void run() {
        myLooper.set(Looper.myLooper());
      }
    };
    RuntimeEnvironment.setMainThread(t);
    t.start();
    try {
      t.join(1000);
      assertThat(myLooper.get()).isSameAs(Looper.getMainLooper());
    } finally {
      RuntimeEnvironment.setMainThread(Thread.currentThread());
    }
  }

  @Test
  public void getMainLooper_shouldBeInitialized_onBackgroundThread_evenWhenRobolectricApplicationIsNull() throws Exception {
    RuntimeEnvironment.application = null;
    final AtomicReference<Looper> mainLooperAtomicReference = new AtomicReference<>();

    Thread backgroundThread = new Thread(new Runnable() {
      @Override
      public void run() {
        Looper mainLooper = Looper.getMainLooper();
        mainLooperAtomicReference.set(mainLooper);
      }
    }, testName.getMethodName());
    backgroundThread.start();
    backgroundThread.join();

    assertThat(mainLooperAtomicReference.get()).named("mainLooper").isSameAs(Looper.getMainLooper());
  }

  @Test
  public void schedulerOnAnotherLooper_shouldNotBeMaster_byDefault() {
    HandlerThread ht = getHandlerThread();
    assertThat(shadowOf(ht.getLooper()).getScheduler()).isNotSameAs(RuntimeEnvironment.getMasterScheduler());
  }

  @Test
  public void schedulerOnAnotherLooper_shouldBeMaster_ifAdvancedSchedulingEnabled() {
    setAdvancedScheduling();
    HandlerThread ht = getHandlerThread();
    assertThat(shadowOf(ht.getLooper()).getScheduler()).isSameAs(RuntimeEnvironment.getMasterScheduler());
  }

  @Test
  public void withAdvancedScheduling_shouldDispatchMessagesOnBothLoopers_whenAdvancingForegroundThread() {
    setAdvancedScheduling();
    ShadowLooper.pauseMainLooper();
    HandlerThread ht = getHandlerThread();
    Handler handler1 = new Handler(ht.getLooper());
    Handler handler2 = new Handler();
    final ArrayList<String> events = new ArrayList<>();
    handler1.postDelayed(new Runnable() {
      @Override
      public void run() {
        events.add("handler1");
      }
    }, 100);
    handler2.postDelayed(new Runnable() {
      @Override
      public void run() {
        events.add("handler2");
      }
    }, 200);
    assertThat(events).named("start").isEmpty();
    Scheduler s = ShadowLooper.getShadowMainLooper().getScheduler();
    assertThat(s).isSameAs(RuntimeEnvironment.getMasterScheduler());
    assertThat(s).isSameAs(shadowOf(ht.getLooper()).getScheduler());
    final long startTime = s.getCurrentTime();
    s.runOneTask();
    assertThat(events).named("firstEvent").containsExactly("handler1");
    assertThat(s.getCurrentTime()).named("firstEvent:time").isEqualTo(100 + startTime);
    s.runOneTask();
    assertThat(events).named("secondEvent").containsExactly("handler1", "handler2");
    assertThat(s.getCurrentTime()).named("secondEvent:time").isEqualTo(200 + startTime);
  }

  @Test
  public void resetThreadLoopers_clears_messages() {
    HandlerThread backgroundThread = new HandlerThread("resetTest");
    backgroundThread.start();
    Looper backgroundLooper = backgroundThread.getLooper();
    Handler handler = new Handler(backgroundLooper);
    for (int i = 0; i < 5; i++) {
      handler.sendEmptyMessageDelayed(1, 100);
      ShadowLooper.resetThreadLoopers();
      assertThat(handler.hasMessages(1)).isFalse();
    }
  }

  @After
  public void tearDown() {
    RoboSettings.setUseGlobalScheduler(false);
  }
}
