package org.robolectric.util;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import static java.util.concurrent.TimeUnit.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.robolectric.util.Scheduler.IdleState.*;

public class SchedulerTest {
  private final Scheduler scheduler = new Scheduler();
  private final Transcript transcript = new Transcript();

  private long startTime;
  
  @Before
  public void setUp() throws Exception {
    // Normally RobolectricTestRunner would do this for us.
    Scheduler.setMainThread(Thread.currentThread());
    scheduler.pause();
    startTime = scheduler.getCurrentTime(NANOSECONDS);
  }

  @Test
  public void postDelayed_worksWithDifferentTimeUnits() {
    Runnable r = new AddToTranscript("dummy");
    scheduler.postDelayed(r, 12, NANOSECONDS);
    scheduler.advanceToNextPostedRunnable();
    startTime += 12;
    assertThat(scheduler.getCurrentTime(NANOSECONDS)).as("nano step").isEqualTo(startTime);
    scheduler.postDelayed(r, 12, MICROSECONDS);
    scheduler.advanceToNextPostedRunnable();
    startTime += 12000;
    assertThat(scheduler.getCurrentTime(NANOSECONDS)).as("micro step").isEqualTo(startTime);
    scheduler.postDelayed(r, 12, MILLISECONDS);
    scheduler.advanceToNextPostedRunnable();
    startTime += 12000000;
    assertThat(scheduler.getCurrentTime(NANOSECONDS)).as("milli step").isEqualTo(startTime);
    scheduler.postDelayed(r, 12, SECONDS);
    scheduler.advanceToNextPostedRunnable();
    startTime += 12000000000L;
    assertThat(scheduler.getCurrentTime(NANOSECONDS)).as("full step").isEqualTo(startTime);
  }

  @Test
  public void getCurrentTime_worksWithDifferentTimeUnits() {
    scheduler.advanceTo(1234);
    assertThat(scheduler.getCurrentTime(MILLISECONDS)).as("millis").isEqualTo(1234);
    assertThat(scheduler.getCurrentTime(MICROSECONDS)).as("micros").isEqualTo(1234000);
    assertThat(scheduler.getCurrentTime(NANOSECONDS)).as("nanos").isEqualTo(1234000000);
    assertThat(scheduler.getCurrentTime(SECONDS)).as("seconds").isEqualTo(1);
  }

  @Test
  public void advanceTo_worksWithDifferentTimeUnits() {
    scheduler.advanceTo(1234567, NANOSECONDS);
    assertThat(scheduler.getCurrentTime()).as("millis").isEqualTo(1);
    scheduler.advanceTo(1234123, MICROSECONDS);
    assertThat(scheduler.getCurrentTime()).as("micros").isEqualTo(1234);
    scheduler.advanceTo(5678, MILLISECONDS);
    assertThat(scheduler.getCurrentTime()).as("millis").isEqualTo(5678);
    scheduler.advanceTo(1234, SECONDS);
    assertThat(scheduler.getCurrentTime()).as("millis").isEqualTo(1234000);
  }

  @Test
  public void advanceBy_worksWithDifferentTimeUnits() {
    scheduler.advanceBy(12, NANOSECONDS);
    startTime += 12;
    assertThat(scheduler.getCurrentTime(NANOSECONDS)).as("nano step").isEqualTo(startTime);
    scheduler.advanceBy(12, MICROSECONDS);
    startTime += 12000;
    assertThat(scheduler.getCurrentTime(NANOSECONDS)).as("micro step").isEqualTo(startTime);
    scheduler.advanceBy(12, MILLISECONDS);
    startTime += 12000000;
    assertThat(scheduler.getCurrentTime(NANOSECONDS)).as("milli step").isEqualTo(startTime);
    scheduler.advanceBy(12, SECONDS);
    startTime += 12000000000L;
    assertThat(scheduler.getCurrentTime(NANOSECONDS)).as("full step").isEqualTo(startTime);
  }

  @Test
  public void whenIdleStateIsConstantIdle_isPausedReturnsFalse() {
    scheduler.setIdleState(CONSTANT_IDLE);
    assertThat(scheduler.isPaused()).isFalse();
  }

  @Test
  public void whenIdleStateIsUnPaused_isPausedReturnsFalse() {
    scheduler.setIdleState(UNPAUSED);
    assertThat(scheduler.isPaused()).isFalse();
  }

  @Test
  public void whenIdleStateIsPaused_isPausedReturnsTrue() {
    scheduler.setIdleState(PAUSED);
    assertThat(scheduler.isPaused()).isTrue();
  }

  @Test
  public void pause_setsIdleState() {
    scheduler.setIdleState(UNPAUSED);
    scheduler.pause();
    assertThat(scheduler.getIdleState()).isSameAs(PAUSED);
  }

  @Test
  @SuppressWarnings("deprecation")
  public void idleConstantly_setsIdleState() {
    scheduler.setIdleState(UNPAUSED);
    scheduler.idleConstantly(true);
    assertThat(scheduler.getIdleState()).isSameAs(CONSTANT_IDLE);
    scheduler.idleConstantly(false);
    assertThat(scheduler.getIdleState()).isSameAs(UNPAUSED);
  }

  @Test
  public void unPause_setsIdleState() {
    scheduler.setIdleState(PAUSED);
    scheduler.unPause();
    assertThat(scheduler.getIdleState()).isSameAs(UNPAUSED);
  }

  @Test
  public void setIdleStateToUnPause_shouldRunPendingTasks() {
    scheduler.postDelayed(new AddToTranscript("one"), 0);
    scheduler.postDelayed(new AddToTranscript("two"), 0);
    scheduler.postDelayed(new AddToTranscript("three"), 1000);
    transcript.assertNoEventsSoFar();
    scheduler.setIdleState(UNPAUSED);
    transcript.assertEventsSoFar("one", "two");
    assertThat(scheduler.getCurrentTime(NANOSECONDS)).as("time").isEqualTo(startTime);
  }

  @Test
  public void setIdleStateToConstantIdle_shouldRunAllTasks() {
    scheduler.postDelayed(new AddToTranscript("one"), 0);
    scheduler.postDelayed(new AddToTranscript("two"), 0);
    scheduler.postDelayed(new AddToTranscript("three"), 1000);
    transcript.assertNoEventsSoFar();
    scheduler.setIdleState(CONSTANT_IDLE);
    transcript.assertEventsSoFar("one", "two", "three");
    assertThat(scheduler.getCurrentTime(NANOSECONDS)).as("time")
        .isEqualTo(startTime + MILLISECONDS.toNanos(1000));
  }

  @Test
  public void unPause_shouldRunPendingTasks() {
    scheduler.postDelayed(new AddToTranscript("one"), 0);
    scheduler.postDelayed(new AddToTranscript("two"), 0);
    scheduler.postDelayed(new AddToTranscript("three"), 1000);
    transcript.assertNoEventsSoFar();
    scheduler.unPause();
    transcript.assertEventsSoFar("one", "two");
    assertThat(scheduler.getCurrentTime(NANOSECONDS)).as("time").isEqualTo(startTime);
  }

  @Test
  @SuppressWarnings("deprecation")
  public void idleConstantlyTrue_shouldRunAllTasks() {
    scheduler.postDelayed(new AddToTranscript("one"), 0);
    scheduler.postDelayed(new AddToTranscript("two"), 0);
    scheduler.postDelayed(new AddToTranscript("three"), 1000);
    transcript.assertNoEventsSoFar();
    scheduler.idleConstantly(true);
    transcript.assertEventsSoFar("one", "two", "three");
    assertThat(scheduler.getCurrentTime(NANOSECONDS)).as("time")
        .isEqualTo(startTime + MILLISECONDS.toNanos(1000));
  }

  @Test
  public void advanceTo_shouldAdvanceTimeEvenIfThereIsNoWork() throws Exception {
    scheduler.advanceTo(1000);
    assertThat(scheduler.getCurrentTime()).isEqualTo(1000);
  }

  @Test
  public void advanceBy_returnsTrueIffSomeJobWasRun() throws Exception {
    scheduler.postDelayed(new AddToTranscript("one"), 0);
    scheduler.postDelayed(new AddToTranscript("two"), 0);
    scheduler.postDelayed(new AddToTranscript("three"), 1000);

    assertThat(scheduler.advanceBy(0)).isTrue();
    transcript.assertEventsSoFar("one", "two");

    assertThat(scheduler.advanceBy(0)).isFalse();
    transcript.assertNoEventsSoFar();

    assertThat(scheduler.advanceBy(1000)).isTrue();
    transcript.assertEventsSoFar("three");
  }

  @Test
  public void postDelayed_addsAJobToBeRunInTheFuture() throws Exception {
    scheduler.postDelayed(new AddToTranscript("one"), 1000);
    scheduler.postDelayed(new AddToTranscript("two"), 2000);
    scheduler.postDelayed(new AddToTranscript("three"), 3000);

    scheduler.advanceBy(1000);
    transcript.assertEventsSoFar("one");

    scheduler.advanceBy(500);
    transcript.assertNoEventsSoFar();

    scheduler.advanceBy(501);
    transcript.assertEventsSoFar("two");

    scheduler.advanceBy(999);
    transcript.assertEventsSoFar("three");
  }

  @Test
  public void postDelayed_whileIdlingConstantly_executesImmediately() {
    scheduler.setIdleState(CONSTANT_IDLE);
    scheduler.postDelayed(new AddToTranscript("one"), 1000);

    transcript.assertEventsSoFar("one");
  }
  
  @Test
  public void postDelayed_whileIdlingConstantly_advancesTime() {
    scheduler.setIdleState(CONSTANT_IDLE);
    scheduler.postDelayed(new AddToTranscript("one"), 1000);

    assertThat(scheduler.getCurrentTime(NANOSECONDS)).isEqualTo(startTime + MILLISECONDS.toNanos(1000));
  }
  
  @Test
  public void postAtFrontOfQueue_addsJobAtFrontOfQueue() throws Exception {
    scheduler.post(new AddToTranscript("one"));
    scheduler.post(new AddToTranscript("two"));
    scheduler.postAtFrontOfQueue(new AddToTranscript("three"));

    scheduler.runOneTask();
    transcript.assertEventsSoFar("three");

    scheduler.runOneTask();
    transcript.assertEventsSoFar("one");

    scheduler.runOneTask();
    transcript.assertEventsSoFar("two");
  }

  @Test
  public void postAtFrontOfQueue_whenUnpaused_runsJobs() throws Exception {
    scheduler.unPause();
    scheduler.postAtFrontOfQueue(new AddToTranscript("three"));
    transcript.assertEventsSoFar("three");
  }

  @Test
  public void postDelayed_whenMoreItemsAreAdded_runsJobs() throws Exception {
    scheduler.postDelayed(new Runnable() {
      @Override
      public void run() {
        transcript.add("one");
        scheduler.postDelayed(new Runnable() {
          @Override
          public void run() {
            transcript.add("two");
            scheduler.postDelayed(new AddToTranscript("three"), 1000);
          }
        }, 1000);
      }
    }, 1000);

    scheduler.advanceBy(1000);
    transcript.assertEventsSoFar("one");

    scheduler.advanceBy(500);
    transcript.assertNoEventsSoFar();

    scheduler.advanceBy(501);
    transcript.assertEventsSoFar("two");

    scheduler.advanceBy(999);
    transcript.assertEventsSoFar("three");
  }

  @Test
  public void remove_ShouldRemoveAllInstancesOfRunnableFromQueue() throws Exception {
    scheduler.post(new TestRunnable());
    TestRunnable runnable = new TestRunnable();
    scheduler.post(runnable);
    scheduler.post(runnable);
    assertThat(scheduler.size()).isEqualTo(3);
    scheduler.remove(runnable);
    assertThat(scheduler.size()).isEqualTo(1);
    scheduler.advanceToLastPostedRunnable();
    assertThat(runnable.wasRun).isFalse();
  }

  @Test
  public void reset_shouldUnPause() throws Exception {
    scheduler.pause();

    TestRunnable runnable = new TestRunnable();
    scheduler.post(runnable);

    assertThat(runnable.wasRun).isFalse();

    scheduler.reset();
    scheduler.post(runnable);
    assertThat(runnable.wasRun).isTrue();
  }

  @Test
  public void reset_shouldClearPendingRunnables() throws Exception {
    scheduler.pause();

    TestRunnable runnable1 = new TestRunnable();
    scheduler.post(runnable1);

    assertThat(runnable1.wasRun).isFalse();

    scheduler.reset();

    TestRunnable runnable2 = new TestRunnable();
    scheduler.post(runnable2);

    assertThat(runnable1.wasRun).isFalse();
    assertThat(runnable2.wasRun).isTrue();
  }

  @Test
  public void nestedPost_whilePaused_doesntAutomaticallyExecute() {
    final List<Integer> order = new ArrayList<>();
    scheduler.postDelayed(new Runnable() {
      @Override
      public void run() {
        order.add(1);
        scheduler.post(new Runnable() {
          @Override
          public void run() {
            order.add(4);
          }
        });
        order.add(2);
      }
    }, 0);
    scheduler.postDelayed(new Runnable() {
      @Override
      public void run() {
        order.add(3);
      }
    }, 0);
    scheduler.runOneTask();
    
    assertThat(order).as("order:first run").containsExactly(1, 2);
    assertThat(scheduler.size()).as("size:first run").isEqualTo(2);
    scheduler.runOneTask();
    assertThat(order).as("order:second run").containsExactly(1, 2, 3);
    assertThat(scheduler.size()).as("size:second run").isEqualTo(1);
    scheduler.runOneTask();
    assertThat(order).as("order:third run").containsExactly(1, 2, 3, 4);
    assertThat(scheduler.size()).as("size:second run").isEqualTo(0);
  }

  @Test
  public void nestedPost_whileUnpaused_automaticallyExecutes3After() {
    final List<Integer> order = new ArrayList<>();
    scheduler.unPause();
    scheduler.postDelayed(new Runnable() {
      @Override
      public void run() {
        order.add(1);
        scheduler.post(new Runnable() {
          @Override
          public void run() {
            order.add(3);
          }
        });
        order.add(2);
      }
    }, 0);
    
    assertThat(order).as("order").containsExactly(1, 2, 3);
    assertThat(scheduler.size()).as("size").isEqualTo(0);
  }

  @Test
  public void nestedPostInFuture_whileUnpaused_automaticallyExecutes3After() {
    final List<Integer> order = new ArrayList<>();
    scheduler.unPause();
    scheduler.postDelayed(new Runnable() {
      @Override
      public void run() {
        order.add(1);
        scheduler.post(new Runnable() {
          @Override
          public void run() {
            order.add(3);
          }
        });
        order.add(2);
      }
    }, 1000);
    assertThat(order).as("before advance").isEmpty();
    scheduler.advanceToNextPostedRunnable();
    assertThat(order).as("order").containsExactly(1, 2, 3);
    assertThat(scheduler.size()).as("size").isEqualTo(0);
  }

  @Test
  public void nestedPostAtFront_whilePaused_runsBeforeSubsequentPost() {
    final List<Integer> order = new ArrayList<>();
    scheduler.postDelayed(new Runnable() {
      @Override
      public void run() {
        order.add(1);
        scheduler.postAtFrontOfQueue(new Runnable() {
          @Override
          public void run() {
            order.add(3);
          }
        });
        order.add(2);
      }
    }, 0);
    scheduler.postDelayed(new Runnable() {
      @Override
      public void run() {
        order.add(4);
      }
    }, 0);
    scheduler.advanceToLastPostedRunnable();
    assertThat(order).as("order").containsExactly(1, 2, 3, 4);
    assertThat(scheduler.size()).as("size").isEqualTo(0);
  }

  @Test
  public void nestedPostAtFront_whileUnpaused_runsAfter() {
    final List<Integer> order = new ArrayList<>();
    scheduler.unPause();
    scheduler.postDelayed(new Runnable() {
      @Override
      public void run() {
        order.add(1);
        scheduler.postAtFrontOfQueue(new Runnable() {
          @Override
          public void run() {
            order.add(3);
          }
        });
        order.add(2);
      }
    }, 0);
    assertThat(order).as("order").containsExactly(1, 2, 3);
    assertThat(scheduler.size()).as("size").isEqualTo(0);
  }

  @Test
  public void nestedPostDelayed_whileUnpaused_doesntAutomaticallyExecute3() {
    final List<Integer> order = new ArrayList<>();
    scheduler.unPause();
    scheduler.postDelayed(new Runnable() {
      @Override
      public void run() {
        order.add(1);
        scheduler.postDelayed(new Runnable() {
          @Override
          public void run() {
            order.add(3);
          }
        }, 1);
        order.add(2);
      }
    }, 0);
    
    assertThat(order).as("order:before").containsExactly(1, 2);
    assertThat(scheduler.size()).as("size:before").isEqualTo(1);
    scheduler.advanceToLastPostedRunnable();
    assertThat(order).as("order:after").containsExactly(1, 2, 3);
    assertThat(scheduler.size()).as("size:after").isEqualTo(0);    
    assertThat(scheduler.getCurrentTime(NANOSECONDS)).as("time:after")
        .isEqualTo(startTime + MILLISECONDS.toNanos(1));
  }

  @Test
  public void nestedPostDelayed_whenIdlingConstantly_automaticallyExecutes3After() {
    final List<Integer> order = new ArrayList<>();
    scheduler.setIdleState(CONSTANT_IDLE);
    scheduler.postDelayed(new Runnable() {
      @Override
      public void run() {
        order.add(1);
        scheduler.postDelayed(new Runnable() {
          @Override
          public void run() {
            order.add(3);
          }
        }, 1);
        order.add(2);
      }
    }, 0);

    assertThat(order).as("order").containsExactly(1, 2, 3);
    assertThat(scheduler.size()).as("size").isEqualTo(0);
    assertThat(scheduler.getCurrentTime(NANOSECONDS)).as("time")
        .isEqualTo(startTime + MILLISECONDS.toNanos(1));
  }

  @Test
  public void post_whenTheRunnableThrows_executesSubsequentRunnables() throws Exception {
    final List<Integer> runnablesThatWereRun = new ArrayList<>();
    scheduler.post(new Runnable() {
      @Override
      public void run() {
        runnablesThatWereRun.add(1);
        throw new RuntimeException("foo");
      }
    });

    try {
      scheduler.unPause();
    } catch (RuntimeException ignored) { }

    scheduler.post(new Runnable() {
      @Override
      public void run() {
        runnablesThatWereRun.add(2);
      }
    });

    assertThat(runnablesThatWereRun).containsExactly(1, 2);
  }

  // Regression test for #2115
  @Test(timeout=1000)
  public void schedulerAllowsConcurrentTimeRead_whileLockIsHeld() throws InterruptedException {
    final AtomicLong l = new AtomicLong();
    Thread t = createTestThread(new Runnable() {
      @Override
      public void run() {
        l.set(scheduler.getCurrentTime());
      }
    });
    synchronized (scheduler) {
      t.start();
      t.join();
    }
  }

  @Test(timeout=1000)
  public void schedulerAllowsConcurrentStateRead_whileLockIsHeld() throws InterruptedException {
    Thread t = createTestThread(new Runnable() {
      @Override
      public void run() {
        scheduler.getIdleState();
      }
    });
    synchronized (scheduler) {
      t.start();
      t.join();
    }
  }

  @Test(timeout=1000)
  public void schedulerAllowsConcurrentIsPaused_whileLockIsHeld() throws InterruptedException {
    Thread t = createTestThread(new Runnable() {
      @Override
      public void run() {
        scheduler.isPaused();
      }
    });
    synchronized (scheduler) {
      t.start();
      t.join();
    }
  }

  @Test(timeout=1000)
  public void schedulerAllowsConcurrentPostWithinPost() throws InterruptedException {
    // This would normally be done for us by RobolectricTestRunner but we're not running in that
    // context.
    Scheduler.setMainThread(Thread.currentThread());
    final Thread t = createTestThread(new Runnable() {
      @Override
      public void run() {
        scheduler.post(new Runnable() {
          @Override
          public void run() {
          }
        });
      }
    });
    scheduler.post(new Runnable() {
      @Override
      public void run() {
        t.start();
        try {
          t.join();
        } catch (InterruptedException e) {
          throw new RuntimeException(e);
        }
      }
    });
    scheduler.advanceBy(0);
  }

  @Test
  public void scheduledRunnableCompareTo_handlesLargeDifferences() {
    // Found an overflow bug in the original implementation of compareTo() when casting diff to int -
    // if the diff is > INT_MAX the result of casing to INT will be negative, which results in it
    // sorting the opposite of what we want.
    // ScheduledRunnable is private; cannot create directly. Test indirectly using postDelayed().
    TestRunnable r1 = new TestRunnable();
    TestRunnable r2 = new TestRunnable();
    scheduler.postDelayed(r1, 100);
    scheduler.postDelayed(r2, 60, SECONDS); // Difference between 60s and 100ms in nanos is > INT_MAX

    scheduler.runOneTask();
    assertThat(r1.wasRun).as("first task run first").isTrue();
    assertThat(r2.wasRun).as("second task not run yet").isFalse();
    scheduler.runOneTask();
    assertThat(r2.wasRun).as("second task run second").isTrue();
  }

  @Rule
  public TestName testName = new TestName();

  private Thread createTestThread(Runnable r) {
    return new Thread(r, testName.getMethodName());
  }

  @Test
  public void advanceTo_shouldThrowISE_whenNotCalledFromMain() throws InterruptedException {
    testMethodThrowsISE(new Runnable() {
      @Override
      public void run() {
        scheduler.advanceTo(12300, MILLISECONDS);
      }
    });
  }

  @Test
  public void advanceToLastPostedRunnable_shouldThrowISE_whenNotCalledFromMain() throws InterruptedException {
    testMethodThrowsISE(new Runnable() {
      @Override
      public void run() {
        scheduler.advanceToLastPostedRunnable();
      }
    });
  }

  @Test
  public void advanceToNextPostedRunnable_shouldThrowISE_whenNotCalledFromMain() throws InterruptedException {
    testMethodThrowsISE(new Runnable() {
      @Override
      public void run() {
        scheduler.advanceToNextPostedRunnable();
      }
    });
  }

  @Test
  public void runOneTask_shouldThrowISE_whenNotCalledFromMain() throws InterruptedException {
    testMethodThrowsISE(new Runnable() {
      @Override
      public void run() {
        scheduler.runOneTask();
      }
    });
  }

  @Test
  public void setIdleState_shouldThrowISE_whenNotCalledFromMain() throws InterruptedException {
    testMethodThrowsISE(new Runnable() {
      @Override
      public void run() {
        scheduler.setIdleState(Scheduler.IdleState.CONSTANT_IDLE);
      }
    });
  }

  private void testMethodThrowsISE(final Runnable r) throws InterruptedException {
    final AtomicReference<Throwable> t = new AtomicReference<>();
    Thread thread = createTestThread(new Runnable() {
      @Override
      public void run() {
        try {
          r.run();
        } catch (RuntimeException e) {
          t.set(e);
        }
      }
    });
    thread.start();
    thread.join();
    assertThat(t.get()).isInstanceOf(IllegalStateException.class);
  }

  @Test
  public void whenMainThreadIsSwitched_andSchedulerUnpaused_postRunsImmediately() throws InterruptedException {
    scheduler.unPause();
    Thread t = createTestThread(new Runnable() {
      @Override
      public void run() {
        Scheduler.setMainThread(Thread.currentThread());
        scheduler.post(new AddToTranscript("post"));
      }
    });
    t.start();
    t.join();
    transcript.assertEventsSoFar("post");
  }

  @Test
  public void whenMainThreadIsSwitched_andSchedulerUnpaused_postAtFrontRunsImmediately() throws InterruptedException {
    scheduler.unPause();
    Thread t = createTestThread(new Runnable() {
      @Override
      public void run() {
        Scheduler.setMainThread(Thread.currentThread());
        scheduler.postAtFrontOfQueue(new AddToTranscript("post"));
      }
    });
    t.start();
    t.join();
    transcript.assertEventsSoFar("post");
  }

  @Test
  public void newScheduler_shouldNotBeBlocked() {
    assertThat(scheduler.isBlocked).isEqualTo(0);
  }

  @Test
  public void block_shouldIncrementBlockCounter() {
    for (int i = 1; i < 4; i++) {
      scheduler.block();
      assertThat(scheduler.isBlocked).as("iteration " + i).isEqualTo(i);
    }
  }

  @Test
  public void unBlock_shouldDecrementBlockCounter() {
    scheduler.isBlocked = 4;
    for (int i = 3; i >= 0; i--) {
      scheduler.unBlock();
      assertThat(scheduler.isBlocked).as("iteration " + i).isEqualTo(i);
    }
  }

  @Test
  public void unBlock_whenNotBlocked_shouldThrowISE() {
    try {
      scheduler.unBlock();
      Assertions.failBecauseExceptionWasNotThrown(IllegalStateException.class);
    } catch (IllegalStateException e) {
      assertThat(e.getMessage()).contains("not blocked");
    }
  }

  @Test
  public void lastUnBlock_shouldCallRunPendingTasks_afterDecrement() {
    scheduler.isBlocked = 1;
    final AtomicInteger wasBlocked = new AtomicInteger();
    final Scheduler spy = spy(scheduler);
    doAnswer(new Answer() {
      @Override
      public Object answer(InvocationOnMock invocation) {
        wasBlocked.set(spy.isBlocked);
        return null;
      }
    }).when(spy).runPendingTasks();
    spy.unBlock();
    verify(spy).runPendingTasks();
    assertThat(wasBlocked.get()).as("isBlocked").isEqualTo(1);
  }

  @Test
  public void unBlock_shouldNotCallRunPendingTasks_whenOtherBlocksActive() {
    Scheduler spy = spy(scheduler);
    for (int i = 2; i < 5; i++) {
      spy.isBlocked = i;
      spy.unBlock();
      verify(spy, never()).runPendingTasks();
      spy.reset();
    }
  }

  @Test
  public void post_fromBackgroundThread_shouldNotBlockOrUnblock() throws InterruptedException {
    final Scheduler spy = spy(scheduler);
    Thread t = createTestThread(new Runnable() {
      @Override
      public void run() {
        spy.post(new TestRunnable());
      }
    });
    synchronized (scheduler) {
      t.start();
      t.join();
    }
    verify(spy, never()).block();
    verify(spy, never()).unBlock();
  }

  @Test
  public void post_fromMainThread_shouldBlock_beforePosting() {
    final Scheduler spy = spy(scheduler);
    final AtomicInteger size = new AtomicInteger();
    doAnswer(new Answer() {
      @Override
      public Object answer(InvocationOnMock invocation) {
        size.set(spy.size());
        return null;
      }
    }).when(spy).block();
    // This is to prevent ISE because block() hasn't run properly.
    doNothing().when(spy).unBlock();
    spy.post(new TestRunnable());
    verify(spy).block();
    assertThat(size.get()).as("sizeDuringBlock").isEqualTo(0);
  }

  @Test
  public void post_fromMainThread_shouldUnBlock_afterPosting() {
    final Scheduler spy = spy(scheduler);
    final AtomicInteger size = new AtomicInteger();
    doAnswer(new Answer() {
      @Override
      public Object answer(InvocationOnMock invocation) {
        size.set(spy.size());
        return null;
      }
    }).when(spy).unBlock();
    // This is to prevent ISE because block() hasn't run properly.
    doNothing().when(spy).block();
    spy.post(new TestRunnable());
    verify(spy).unBlock();
    assertThat(size.get()).as("sizeDuringUnBlock").isEqualTo(1);
  }

  @Test
  public void postAtFront_fromBackgroundThread_shouldNotBlockOrUnblock() throws InterruptedException {
    final Scheduler spy = spy(scheduler);
    Thread t = createTestThread(new Runnable() {
      @Override
      public void run() {
        spy.postAtFrontOfQueue(new TestRunnable());
      }
    });
    synchronized (scheduler) {
      t.start();
      t.join();
    }
    verify(spy, never()).block();
    verify(spy, never()).unBlock();
  }

  @Test
  public void postAtFront_fromMainThread_shouldBlock_beforePosting() {
    final Scheduler spy = spy(scheduler);
    final AtomicInteger size = new AtomicInteger();
    doAnswer(new Answer() {
      @Override
      public Object answer(InvocationOnMock invocation) {
        size.set(spy.size());
        return null;
      }
    }).when(spy).block();
    // This is to prevent ISE because block() hasn't run properly.
    doNothing().when(spy).unBlock();
    spy.postAtFrontOfQueue(new TestRunnable());
    verify(spy).block();
    assertThat(size.get()).as("sizeDuringBlock").isEqualTo(0);
  }

  @Test
  public void postAtFront_fromMainThread_shouldUnBlock_afterPosting() {
    final Scheduler spy = spy(scheduler);
    final AtomicInteger size = new AtomicInteger();
    doAnswer(new Answer() {
      @Override
      public Object answer(InvocationOnMock invocation) {
        size.set(spy.size());
        return null;
      }
    }).when(spy).unBlock();
    // This is to prevent ISE because block() hasn't run properly.
    doNothing().when(spy).block();
    spy.postAtFrontOfQueue(new TestRunnable());
    verify(spy).unBlock();
    assertThat(size.get()).as("sizeDuringUnBlock").isEqualTo(1);
  }

  @Test
  public void runOneTask_whilePaused_shouldOnlyRunOneTask_withSimultaneousTasks() {
    TestRunnable task1 = new TestRunnable();
    TestRunnable task2 = new TestRunnable();

    scheduler.postDelayed(task1, 1);
    scheduler.postDelayed(task2, 1);
    scheduler.runOneTask();
    assertThat(task1.wasRun).isTrue();
    assertThat(task2.wasRun).isFalse();
  }

  @Test
  public void setMainThread_forCurrentThread() {
    Scheduler.setMainThread(Thread.currentThread());
    assertThat(Scheduler.getMainThread()).isSameAs(Thread.currentThread());
  }

  @Test
  public void setMainThread_forNewThread() {
    Thread t = new Thread();
    Scheduler.setMainThread(t);
    assertThat(Scheduler.getMainThread()).isSameAs(t);
  }

  @Test
  public void isMainThread_forNewThread_withoutSwitch() throws InterruptedException {
    final AtomicBoolean res = new AtomicBoolean();
    final CountDownLatch finished = new CountDownLatch(1);
    Thread t = new Thread() {
      @Override
      public void run() {
        res.set(Scheduler.isMainThread());
        finished.countDown();
      }
    };
    Scheduler.setMainThread(Thread.currentThread());
    t.start();
    if (!finished.await(1000, MILLISECONDS)) {
      throw new InterruptedException("Thread " + t + " didn't finish timely");
    }
    assertThat(Scheduler.isMainThread()).as("testThread").isTrue();
    assertThat(res.get()).as("thread t").isFalse();
  }

  @Test
  public void isMainThread_forNewThread_withSwitch() throws InterruptedException {
    final AtomicBoolean res = new AtomicBoolean();
    final CountDownLatch finished = new CountDownLatch(1);
    Thread t = new Thread() {
      @Override
      public void run() {
        res.set(Scheduler.isMainThread());
        finished.countDown();
      }
    };
    Scheduler.setMainThread(t);
    t.start();
    if (!finished.await(1000, MILLISECONDS)) {
      throw new InterruptedException("Thread " + t + " didn't finish timely");
    }
    assertThat(Scheduler.isMainThread()).as("testThread").isFalse();
    assertThat(res.get()).as("thread t").isTrue();
  }

  @Test
  public void isMainThread_withArg_forNewThread_withSwitch() throws InterruptedException {
    Thread t = new Thread();
    Scheduler.setMainThread(t);
    assertThat(Scheduler.isMainThread(t)).isTrue();
  }

  @Test
  public void getSetMasterScheduler() {
    Scheduler s = new Scheduler();
    Scheduler.setMasterScheduler(s);
    assertThat(Scheduler.getMasterScheduler()).isSameAs(s);
  }

  private class AddToTranscript implements Runnable {
    private String event;

    public AddToTranscript(String event) {
      this.event = event;
    }

    @Override
    public void run() {
      transcript.add(event);
    }
  }
}
