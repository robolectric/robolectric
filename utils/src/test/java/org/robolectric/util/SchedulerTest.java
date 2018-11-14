package org.robolectric.util;

import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.util.Scheduler.IdleState.CONSTANT_IDLE;
import static org.robolectric.util.Scheduler.IdleState.PAUSED;
import static org.robolectric.util.Scheduler.IdleState.UNPAUSED;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class SchedulerTest {
  private final Scheduler scheduler = new Scheduler();
  private final List<String> transcript = new ArrayList<>();

  private long startTime;
  
  @Before
  public void setUp() throws Exception {
    scheduler.pause();
    startTime = scheduler.getCurrentTime();
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
    assertThat(transcript).isEmpty();
    final long time = scheduler.getCurrentTime();
    scheduler.setIdleState(UNPAUSED);
    assertThat(transcript).containsExactly("one", "two");
    assertThat(scheduler.getCurrentTime()).named("time").isEqualTo(time);
  }

  @Test
  public void setIdleStateToConstantIdle_shouldRunAllTasks() {
    scheduler.postDelayed(new AddToTranscript("one"), 0);
    scheduler.postDelayed(new AddToTranscript("two"), 0);
    scheduler.postDelayed(new AddToTranscript("three"), 1000);
    assertThat(transcript).isEmpty();
    final long time = scheduler.getCurrentTime();
    scheduler.setIdleState(CONSTANT_IDLE);
    assertThat(transcript).containsExactly("one", "two", "three");
    assertThat(scheduler.getCurrentTime()).named("time").isEqualTo(time + 1000);
  }

  @Test
  public void unPause_shouldRunPendingTasks() {
    scheduler.postDelayed(new AddToTranscript("one"), 0);
    scheduler.postDelayed(new AddToTranscript("two"), 0);
    scheduler.postDelayed(new AddToTranscript("three"), 1000);
    assertThat(transcript).isEmpty();
    final long time = scheduler.getCurrentTime();
    scheduler.unPause();
    assertThat(transcript).containsExactly("one", "two");
    assertThat(scheduler.getCurrentTime()).named("time").isEqualTo(time);
  }

  @Test
  @SuppressWarnings("deprecation")
  public void idleConstantlyTrue_shouldRunAllTasks() {
    scheduler.postDelayed(new AddToTranscript("one"), 0);
    scheduler.postDelayed(new AddToTranscript("two"), 0);
    scheduler.postDelayed(new AddToTranscript("three"), 1000);
    assertThat(transcript).isEmpty();
    final long time = scheduler.getCurrentTime();
    scheduler.idleConstantly(true);
    assertThat(transcript).containsExactly("one", "two", "three");
    assertThat(scheduler.getCurrentTime()).named("time").isEqualTo(time + 1000);
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
    assertThat(transcript).containsExactly("one", "two");
    transcript.clear();

    assertThat(scheduler.advanceBy(0)).isFalse();
    assertThat(transcript).isEmpty();

    assertThat(scheduler.advanceBy(1000)).isTrue();
    assertThat(transcript).containsExactly("three");
  }

  @Test
  public void postDelayed_addsAJobToBeRunInTheFuture() throws Exception {
    scheduler.postDelayed(new AddToTranscript("one"), 1000);
    scheduler.postDelayed(new AddToTranscript("two"), 2000);
    scheduler.postDelayed(new AddToTranscript("three"), 3000);

    scheduler.advanceBy(1000);
    assertThat(transcript).containsExactly("one");
    transcript.clear();

    scheduler.advanceBy(500);
    assertThat(transcript).isEmpty();

    scheduler.advanceBy(501);
    assertThat(transcript).containsExactly("two");
    transcript.clear();

    scheduler.advanceBy(999);
    assertThat(transcript).containsExactly("three");
  }

  @Test
  public void postDelayed_whileIdlingConstantly_executesImmediately() {
    scheduler.setIdleState(CONSTANT_IDLE);
    scheduler.postDelayed(new AddToTranscript("one"), 1000);

    assertThat(transcript).containsExactly("one");
  }
  
  @Test
  public void postDelayed_whileIdlingConstantly_advancesTime() {
    scheduler.setIdleState(CONSTANT_IDLE);
    scheduler.postDelayed(new AddToTranscript("one"), 1000);

    assertThat(scheduler.getCurrentTime()).isEqualTo(1000 + startTime);
  }
  
  @Test
  public void postAtFrontOfQueue_addsJobAtFrontOfQueue() throws Exception {
    scheduler.post(new AddToTranscript("one"));
    scheduler.post(new AddToTranscript("two"));
    scheduler.postAtFrontOfQueue(new AddToTranscript("three"));

    scheduler.runOneTask();
    assertThat(transcript).containsExactly("three");
    transcript.clear();

    scheduler.runOneTask();
    assertThat(transcript).containsExactly("one");
    transcript.clear();

    scheduler.runOneTask();
    assertThat(transcript).containsExactly("two");
  }

  @Test
  public void postAtFrontOfQueue_whenUnpaused_runsJobs() throws Exception {
    scheduler.unPause();
    scheduler.postAtFrontOfQueue(new AddToTranscript("three"));
    assertThat(transcript).containsExactly("three");
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
    assertThat(transcript).containsExactly("one");
    transcript.clear();

    scheduler.advanceBy(500);
    assertThat(transcript).isEmpty();

    scheduler.advanceBy(501);
    assertThat(transcript).containsExactly("two");
    transcript.clear();

    scheduler.advanceBy(999);
    assertThat(transcript).containsExactly("three");
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
    
    assertThat(order).named("order:first run").containsExactly(1, 2);
    assertThat(scheduler.size()).named("size:first run").isEqualTo(2);
    scheduler.runOneTask();
    assertThat(order).named("order:second run").containsExactly(1, 2, 3);
    assertThat(scheduler.size()).named("size:second run").isEqualTo(1);
    scheduler.runOneTask();
    assertThat(order).named("order:third run").containsExactly(1, 2, 3, 4);
    assertThat(scheduler.size()).named("size:second run").isEqualTo(0);
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
    
    assertThat(order).named("order").containsExactly(1, 2, 3);
    assertThat(scheduler.size()).named("size").isEqualTo(0);
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
    assertThat(order).named("order").containsExactly(1, 2, 3, 4);
    assertThat(scheduler.size()).named("size").isEqualTo(0);
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
    assertThat(order).named("order").containsExactly(1, 2, 3);
    assertThat(scheduler.size()).named("size").isEqualTo(0);
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
    
    assertThat(order).named("order:before").containsExactly(1, 2);
    assertThat(scheduler.size()).named("size:before").isEqualTo(1);
    scheduler.advanceToLastPostedRunnable();
    assertThat(order).named("order:after").containsExactly(1, 2, 3);
    assertThat(scheduler.size()).named("size:after").isEqualTo(0);
    assertThat(scheduler.getCurrentTime()).named("time:after").isEqualTo(1 + startTime);
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

    assertThat(order).named("order").containsExactly(1, 2, 3);
    assertThat(scheduler.size()).named("size").isEqualTo(0);
    assertThat(scheduler.getCurrentTime()).named("time").isEqualTo(1 + startTime);
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
  public void testTimeNotChangedByNegativeDelay() throws Exception {
    long currentTime = scheduler.getCurrentTime();
    long[] observedTime = new long[1];
    scheduler.postDelayed(
        new Runnable() {
          @Override
          public void run() {
            observedTime[0] = scheduler.getCurrentTime();
          }
        },
        -1000);
    scheduler.advanceToLastPostedRunnable();
    assertThat(observedTime[0]).isEqualTo(currentTime);
    assertThat(scheduler.getCurrentTime()).isEqualTo(currentTime);
  }

  /** Tests for quadractic or exponential behavior in the scheduler, and stable sorting */
  @Test(timeout = 1000)
  public void schedulerWithManyRunnables() {
    Random random = new Random(0);
    Map<Integer, List<Integer>> orderCheck = new TreeMap<>();
    List<Integer> actualOrder = new ArrayList<>();
    for (int i = 0; i < 20_000; i++) {
      int delay = random.nextInt(10);
      List<Integer> list = orderCheck.get(delay);
      if (list == null) {
        list = new ArrayList<>();
        orderCheck.put(delay, list);
      }
      list.add(i);
      final int localI = i;
      scheduler.postDelayed(
          new Runnable() {
            @Override
            public void run() {
              actualOrder.add(localI);
            }
          },
          delay);
    }
    assertThat(actualOrder).isEmpty();
    scheduler.advanceToLastPostedRunnable();
    assertThat(actualOrder).isEqualTo(ImmutableList.copyOf(Iterables.concat(orderCheck.values())));
  }

  @Test(timeout=1000)
  public void schedulerAllowsConcurrentTimeRead_whileLockIsHeld() throws InterruptedException {
    final AtomicLong l = new AtomicLong();
    Thread t = new Thread("schedulerAllowsConcurrentTimeRead") {
      @Override
      public void run() {
        l.set(scheduler.getCurrentTime());
      }
    };
    // Grab the lock and then start a thread that tries to get the current time. The other thread
    // should not deadlock.
    synchronized (scheduler) {
      t.start();
      t.join();
    }
  }

  @Test(timeout = 1000)
  public void schedulerAllowsConcurrentStateRead_whileLockIsHeld() throws InterruptedException {
    Thread t = new Thread("schedulerAllowsConcurrentStateRead") {
      @Override
      public void run() {
        scheduler.getIdleState();
      }
    };
    // Grab the lock and then start a thread that tries to get the idle state. The other thread
    // should not deadlock.
    synchronized (scheduler) {
      t.start();
      t.join();
    }
  }

  @Test(timeout = 1000)
  public void schedulerAllowsConcurrentIsPaused_whileLockIsHeld() throws InterruptedException {
    Thread t = new Thread("schedulerAllowsConcurrentIsPaused") {
      @Override
      public void run() {
        scheduler.isPaused();
      }
    };
    // Grab the lock and then start a thread that tries to get the paused state. The other thread
    // should not deadlock.
    synchronized (scheduler) {
      t.start();
      t.join();
    }
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
