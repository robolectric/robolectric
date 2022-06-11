package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;
import static org.robolectric.Shadows.shadowOf;
import static org.robolectric.annotation.LooperMode.Mode.LEGACY;
import static org.robolectric.shadows.ShadowLooper.shadowMainLooper;

import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.SystemClock;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.time.Duration;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;
import org.robolectric.RoboSettings;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.LooperMode;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.Scheduler;

@RunWith(AndroidJUnit4.class)
@LooperMode(LEGACY)
public class ShadowLegacyLooperTest {

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
    assertThat(Looper.myLooper()).isSameInstanceAs(Looper.getMainLooper());
  }

  @Test
  public void differentThreads_getDifferentLoopers() {
    HandlerThread ht = getHandlerThread();
    assertThat(ht.getLooper()).isNotSameInstanceAs(Looper.getMainLooper());
  }

  @Test
  public void mainLooperThread_shouldBeTestThread() {
    assertThat(Looper.getMainLooper().getThread()).isSameInstanceAs(Thread.currentThread());
  }

  @Test
  public void shadowMainLooper_shouldBeShadowOfMainLooper() {
    assertThat(ShadowLooper.getShadowMainLooper())
        .isSameInstanceAs(shadowOf(Looper.getMainLooper()));
  }

  @Test
  public void getLooperForThread_returnsLooperForAThreadThatHasOne() throws InterruptedException {
    QuitThread qt = getQuitThread();
    assertThat(ShadowLooper.getLooperForThread(qt)).isSameInstanceAs(qt.looper);
  }

  @Test
  public void getLooperForThread_returnsLooperForMainThread() {
    assertThat(ShadowLooper.getLooperForThread(Thread.currentThread()))
        .isSameInstanceAs(Looper.getMainLooper());
  }

  @Test
  public void getAllLoopers_shouldContainMainAndHandlerThread() throws InterruptedException {
    Looper looper = getQuitThread().looper;

    assertThat(ShadowLooper.getAllLoopers()).contains(Looper.getMainLooper());
    assertThat(ShadowLooper.getAllLoopers()).contains(looper);
  }

  @Test
  public void idleMainLooper_executesScheduledTasks() {
    final boolean[] wasRun = new boolean[] {false};
    new Handler()
        .postDelayed(
            new Runnable() {
              @Override
              public void run() {
                wasRun[0] = true;
              }
            },
            2000);

    assertWithMessage("first").that(wasRun[0]).isFalse();
    ShadowLooper.idleMainLooper(1999, TimeUnit.MILLISECONDS);
    assertWithMessage("second").that(wasRun[0]).isFalse();
    ShadowLooper.idleMainLooper(1, TimeUnit.MILLISECONDS);
    assertWithMessage("last").that(wasRun[0]).isTrue();
  }

  @Test
  public void idleConstantly_runsPostDelayedTasksImmediately() {
    ShadowLooper.idleMainLooperConstantly(true);
    final boolean[] wasRun = new boolean[] {false};
    new Handler()
        .postDelayed(
            new Runnable() {
              @Override
              public void run() {
                wasRun[0] = true;
              }
            },
            2000);

    assertThat(wasRun[0]).isTrue();
  }

  @Test(expected = RuntimeException.class)
  public void shouldThrowRuntimeExceptionIfTryingToQuitMainLooper() {
    Looper.getMainLooper().quit();
  }

  @Test
  public void shouldNotQueueMessagesIfLooperIsQuit() {
    HandlerThread ht = getHandlerThread();
    Looper looper = ht.getLooper();
    looper.quit();
    assertWithMessage("hasQuit").that(shadowOf(looper).hasQuit()).isTrue();
    assertWithMessage("post")
        .that(
            shadowOf(looper)
                .post(
                    new Runnable() {
                      @Override
                      public void run() {}
                    },
                    0))
        .isFalse();

    assertWithMessage("postAtFrontOfQueue")
        .that(
            shadowOf(looper)
                .postAtFrontOfQueue(
                    new Runnable() {
                      @Override
                      public void run() {}
                    }))
        .isFalse();
    assertWithMessage("areAnyRunnable")
        .that(shadowOf(looper).getScheduler().areAnyRunnable())
        .isFalse();
  }

  @Test
  public void shouldThrowawayRunnableQueueIfLooperQuits() {
    HandlerThread ht = getHandlerThread();
    Looper looper = ht.getLooper();
    shadowOf(looper).pause();
    shadowOf(looper)
        .post(
            new Runnable() {
              @Override
              public void run() {}
            },
            0);
    looper.quit();
    assertWithMessage("hasQuit").that(shadowOf(looper).hasQuit()).isTrue();
    assertWithMessage("areAnyRunnable")
        .that(shadowOf(looper).getScheduler().areAnyRunnable())
        .isFalse();
    assertWithMessage("queue").that(shadowOf(looper.getQueue()).getHead()).isNull();
  }

  @Test
  public void threadShouldContinue_whenLooperQuits() throws InterruptedException {
    QuitThread test = getQuitThread();
    assertWithMessage("beforeJoin").that(test.hasContinued).isFalse();
    test.looper.quit();
    test.join(5000);
    assertWithMessage("afterJoin").that(test.hasContinued).isTrue();
  }

  @Test
  public void shouldResetQueue_whenLooperIsReset() {
    HandlerThread ht = getHandlerThread();
    Looper looper = ht.getLooper();
    Handler h = new Handler(looper);
    ShadowLooper sLooper = shadowOf(looper);
    sLooper.pause();
    h.post(
        new Runnable() {
          @Override
          public void run() {}
        });
    assertWithMessage("queue").that(shadowOf(looper.getQueue()).getHead()).isNotNull();
    sLooper.reset();
    assertWithMessage("areAnyRunnable").that(sLooper.getScheduler().areAnyRunnable()).isFalse();
    assertWithMessage("queue").that(shadowOf(looper.getQueue()).getHead()).isNull();
  }

  @Test
  public void shouldSetNewScheduler_whenLooperIsReset() {
    HandlerThread ht = getHandlerThread();
    Looper looper = ht.getLooper();
    ShadowLooper sLooper = shadowOf(looper);
    Scheduler old = sLooper.getScheduler();
    sLooper.reset();
    assertThat(old).isNotSameInstanceAs(sLooper.getScheduler());
  }

  @Test
  public void resetThreadLoopers_shouldQuitAllNonMainLoopers() throws InterruptedException {
    QuitThread test = getQuitThread();
    assertWithMessage("hasContinued:before").that(test.hasContinued).isFalse();
    ShadowLooper.resetThreadLoopers();
    test.join(5000);
    assertWithMessage("hasContinued:after").that(test.hasContinued).isTrue();
  }

  @Test(timeout = 1000)
  public void whenTestHarnessUsesDifferentThread_shouldStillHaveMainLooper() {
    assertThat(Looper.myLooper()).isSameInstanceAs(Looper.getMainLooper());
  }

  @Test
  public void resetThreadLoopers_fromNonMainThread_shouldThrowISE() throws InterruptedException {
    final AtomicReference<Throwable> ex = new AtomicReference<>();
    Thread t =
        new Thread() {
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
  public void
      soStaticRefsToLoopersInAppWorksAcrossTests_shouldRetainSameLooperForMainThreadBetweenResetsButGiveItAFreshScheduler() {
    Looper mainLooper = Looper.getMainLooper();
    Scheduler scheduler = shadowOf(mainLooper).getScheduler();
    ShadowLegacyLooper shadowLooper = Shadow.extract(mainLooper);
    shadowLooper.quit = true;

    assertThat(ApplicationProvider.getApplicationContext().getMainLooper())
        .isSameInstanceAs(mainLooper);
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

    assertWithMessage("Looper.getMainLooper()")
        .that(Looper.getMainLooper())
        .isSameInstanceAs(mainLooper);
    assertWithMessage("app.getMainLooper()")
        .that(application.getMainLooper())
        .isSameInstanceAs(mainLooper);
    assertWithMessage("scheduler")
        .that(shadowOf(mainLooper).getScheduler())
        .isNotSameInstanceAs(scheduler);
    assertWithMessage("scheduler").that(shadowOf(mainLooper).getScheduler()).isSameInstanceAs(s);
    assertWithMessage("quit").that(shadowOf(mainLooper).hasQuit()).isFalse();
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
    assertThat(sMainLooper.getScheduler()).isSameInstanceAs(s);
  }

  @Test
  public void reset_setsGlobalScheduler_forMainLooper_withAdvancedScheduling() {
    setAdvancedScheduling();
    ShadowLooper sMainLooper = ShadowLooper.getShadowMainLooper();
    Scheduler s = new Scheduler();
    RuntimeEnvironment.setMasterScheduler(s);
    sMainLooper.reset();
    assertThat(sMainLooper.getScheduler()).isSameInstanceAs(s);
  }

  @Test
  public void reset_setsNewScheduler_forNonMainLooper_byDefault() {
    HandlerThread ht = getHandlerThread();
    ShadowLooper sLooper = shadowOf(ht.getLooper());
    Scheduler old = sLooper.getScheduler();
    sLooper.reset();
    assertThat(sLooper.getScheduler()).isNotSameInstanceAs(old);
    assertThat(sLooper.getScheduler()).isNotSameInstanceAs(RuntimeEnvironment.getMasterScheduler());
  }

  @Test
  public void reset_setsSchedulerToMaster_forNonMainLooper_withAdvancedScheduling() {
    HandlerThread ht = getHandlerThread();
    ShadowLooper sLooper = shadowOf(ht.getLooper());
    Scheduler s = new Scheduler();
    RuntimeEnvironment.setMasterScheduler(s);
    setAdvancedScheduling();
    sLooper.reset();
    assertThat(sLooper.getScheduler()).isSameInstanceAs(s);
  }

  @Test
  public void resetThreadLoopers_resets_background_thread_schedulers() {
    HandlerThread backgroundThread = new HandlerThread("resetTest");
    backgroundThread.start();
    Looper backgroundLooper = backgroundThread.getLooper();
    Handler handler = new Handler(backgroundLooper);
    Runnable empty =
        new Runnable() {
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
    Thread t =
        new Thread(testName.getMethodName()) {
          @Override
          public void run() {
            myLooper.set(Looper.myLooper());
          }
        };
    RuntimeEnvironment.setMainThread(t);
    t.start();
    try {
      t.join(1000);
      assertThat(myLooper.get()).isSameInstanceAs(Looper.getMainLooper());
    } finally {
      RuntimeEnvironment.setMainThread(Thread.currentThread());
    }
  }

  @Test
  public void
      getMainLooper_shouldBeInitialized_onBackgroundThread_evenWhenRobolectricApplicationIsNull()
          throws Exception {
    RuntimeEnvironment.application = null;
    final AtomicReference<Looper> mainLooperAtomicReference = new AtomicReference<>();

    Thread backgroundThread =
        new Thread(
            new Runnable() {
              @Override
              public void run() {
                Looper mainLooper = Looper.getMainLooper();
                mainLooperAtomicReference.set(mainLooper);
              }
            },
            testName.getMethodName());
    backgroundThread.start();
    backgroundThread.join();

    assertWithMessage("mainLooper")
        .that(mainLooperAtomicReference.get())
        .isSameInstanceAs(Looper.getMainLooper());
  }

  @Test
  public void schedulerOnAnotherLooper_shouldNotBeMaster_byDefault() {
    HandlerThread ht = getHandlerThread();
    assertThat(shadowOf(ht.getLooper()).getScheduler())
        .isNotSameInstanceAs(RuntimeEnvironment.getMasterScheduler());
  }

  @Test
  public void schedulerOnAnotherLooper_shouldBeMaster_ifAdvancedSchedulingEnabled() {
    setAdvancedScheduling();
    HandlerThread ht = getHandlerThread();
    assertThat(shadowOf(ht.getLooper()).getScheduler())
        .isSameInstanceAs(RuntimeEnvironment.getMasterScheduler());
  }

  @Test
  public void
      withAdvancedScheduling_shouldDispatchMessagesOnBothLoopers_whenAdvancingForegroundThread() {
    setAdvancedScheduling();
    ShadowLooper.pauseMainLooper();
    HandlerThread ht = getHandlerThread();
    Handler handler1 = new Handler(ht.getLooper());
    Handler handler2 = new Handler();
    final ArrayList<String> events = new ArrayList<>();
    handler1.postDelayed(
        new Runnable() {
          @Override
          public void run() {
            events.add("handler1");
          }
        },
        100);
    handler2.postDelayed(
        new Runnable() {
          @Override
          public void run() {
            events.add("handler2");
          }
        },
        200);
    assertWithMessage("start").that(events).isEmpty();
    Scheduler s = ShadowLooper.getShadowMainLooper().getScheduler();
    assertThat(s).isSameInstanceAs(RuntimeEnvironment.getMasterScheduler());
    assertThat(s).isSameInstanceAs(shadowOf(ht.getLooper()).getScheduler());
    final long startTime = s.getCurrentTime();
    s.runOneTask();
    assertWithMessage("firstEvent").that(events).containsExactly("handler1");
    assertWithMessage("firstEvent:time").that(s.getCurrentTime()).isEqualTo(100 + startTime);
    s.runOneTask();
    assertWithMessage("secondEvent").that(events).containsExactly("handler1", "handler2");
    assertWithMessage("secondEvent:time").that(s.getCurrentTime()).isEqualTo(200 + startTime);
  }

  @Test
  public void resetThreadLoopers_clears_messages() {
    HandlerThread backgroundThread = new HandlerThread("resetTest");
    backgroundThread.start();
    Looper backgroundLooper = backgroundThread.getLooper();
    Handler handler = new Handler(backgroundLooper);
    for (int i = 0; i < 5; i++) {
      handler.sendEmptyMessageDelayed(1, 100);
      ShadowLegacyLooper.resetThreadLoopers();
      assertThat(handler.hasMessages(1)).isFalse();
    }
  }

  @Test
  public void isIdle() {
    ShadowLooper.pauseMainLooper();
    assertThat(shadowMainLooper().isIdle()).isTrue();
    Handler mainHandler = new Handler();
    mainHandler.post(() -> {});
    assertThat(shadowMainLooper().isIdle()).isFalse();
    shadowMainLooper().idle();
    assertThat(shadowMainLooper().isIdle()).isTrue();
  }

  @Test
  public void getNextScheduledTime() {
    ShadowLooper.pauseMainLooper();
    assertThat(shadowMainLooper().getNextScheduledTaskTime()).isEqualTo(Duration.ZERO);
    Handler mainHandler = new Handler();
    mainHandler.postDelayed(() -> {}, 100);
    assertThat(shadowMainLooper().getNextScheduledTaskTime().toMillis())
        .isEqualTo(SystemClock.uptimeMillis() + 100);
  }

  @Test
  public void getLastScheduledTime() {
    ShadowLooper.pauseMainLooper();
    assertThat(shadowMainLooper().getLastScheduledTaskTime()).isEqualTo(Duration.ZERO);
    Handler mainHandler = new Handler();
    mainHandler.postDelayed(() -> {}, 200);
    mainHandler.postDelayed(() -> {}, 100);
    assertThat(shadowMainLooper().getLastScheduledTaskTime().toMillis())
        .isEqualTo(SystemClock.uptimeMillis() + 200);
  }

  @Test
  public void backgroundSchedulerInBackgroundThread_isDeferred() throws Exception {
    ExecutorService executorService = Executors.newSingleThreadExecutor();
    AtomicBoolean ran = new AtomicBoolean(false);
    executorService
        .submit(() -> ShadowLegacyLooper.getBackgroundThreadScheduler().post(() -> ran.set(true)))
        .get();

    assertThat(ran.get()).isFalse();
    Robolectric.flushBackgroundThreadScheduler();
    assertThat(ran.get()).isTrue();
  }

  @After
  public void tearDown() {
    RoboSettings.setUseGlobalScheduler(false);
  }
}
