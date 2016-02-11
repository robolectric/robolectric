package org.robolectric.util;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static java.util.concurrent.TimeUnit.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.robolectric.util.Scheduler.IdleState.*;

public class SchedulerTest {
  private final Scheduler scheduler = new Scheduler();
  private final Transcript transcript = new Transcript();

  private long startTime;
  
  @Before
  public void setUp() throws Exception {
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
