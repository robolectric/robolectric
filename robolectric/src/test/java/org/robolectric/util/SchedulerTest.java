package org.robolectric.util;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class SchedulerTest {
  private final Scheduler scheduler = new Scheduler();
  private final Transcript transcript = new Transcript();

  @Before
  public void setUp() throws Exception {
    scheduler.pause();
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
  public void postDelayed_whenAnotherPostDelayedIsEnqueued_runsInCorrectSequence() {
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
        }, 0);
        order.add(2);
      }
    }, 0);

    assertThat(order).containsExactly(1, 2);
    assertThat(scheduler.size()).isEqualTo(1);
  }

  @Test
  public void postDelayed_whenAnotherPostDelayedIsEnqueued_runsInCorrectSequence2() {
    final List<Integer> order = new ArrayList<>();
    scheduler.postDelayed(new Runnable() {
      @Override
      public void run() {
        order.add(1);
        scheduler.postDelayed(new Runnable() {
          @Override
          public void run() {
            order.add(3);
          }
        }, 0);
        order.add(2);
      }
    }, 0);
    scheduler.unPause();

    assertThat(order).containsExactly(1, 2, 3);
    assertThat(scheduler.size()).isEqualTo(0);
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
    } catch (RuntimeException e) { }

    scheduler.post(new Runnable() {
      @Override
      public void run() {
        runnablesThatWereRun.add(2);
      }
    });

    assertThat(runnablesThatWereRun).containsExactly(1, 2);
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
