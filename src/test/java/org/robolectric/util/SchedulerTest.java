package com.xtremelabs.robolectric.util;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class SchedulerTest {
    private Transcript transcript;
    private Scheduler scheduler;

    @Before
    public void setUp() throws Exception {
        scheduler = new Scheduler();
        scheduler.pause();
        transcript = new Transcript();
    }

    @Test
    public void testTick_ReturnsTrueIffSomeJobWasRun() throws Exception {
        scheduler.postDelayed(new AddToTranscript("one"), 0);
        scheduler.postDelayed(new AddToTranscript("two"), 0);
        scheduler.postDelayed(new AddToTranscript("three"), 1000);

        assertThat(scheduler.advanceBy(0), equalTo(true));
        transcript.assertEventsSoFar("one", "two");

        assertThat(scheduler.advanceBy(0), equalTo(false));
        transcript.assertNoEventsSoFar();

        assertThat(scheduler.advanceBy(1000), equalTo(true));
        transcript.assertEventsSoFar("three");
    }

    @Test
    public void testShadowPostDelayed() throws Exception {
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
    public void testShadowPostAtFrontOfQueue() throws Exception {
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
    public void testShadowPostAtFrontOfQueue_whenUnpaused() throws Exception {
        scheduler.unPause();
        scheduler.postAtFrontOfQueue(new AddToTranscript("three"));
        transcript.assertEventsSoFar("three");
    }

    @Test
    public void testShadowPostDelayed_WhenMoreItemsAreAdded() throws Exception {
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
    public void removeShouldRemoveAllInstancesOfRunnableFromQueue() throws Exception {
        scheduler.post(new TestRunnable());
        TestRunnable runnable = new TestRunnable();
        scheduler.post(runnable);
        scheduler.post(runnable);
        assertThat(scheduler.enqueuedTaskCount(), equalTo(3));
        scheduler.remove(runnable);
        assertThat(scheduler.enqueuedTaskCount(), equalTo(1));
        scheduler.advanceToLastPostedRunnable();
        assertThat(runnable.wasRun, equalTo(false));
    }

    @Test
    public void resetShouldUnPause() throws Exception {
        scheduler.pause();

        TestRunnable runnable = new TestRunnable();
        scheduler.post(runnable);

        assertThat(runnable.wasRun, equalTo(false));

        scheduler.reset();
        scheduler.post(runnable);
        assertThat(runnable.wasRun, equalTo(true));
    }

    @Test
    public void resetShouldClearPendingRunnables() throws Exception {
        scheduler.pause();

        TestRunnable runnable1 = new TestRunnable();
        scheduler.post(runnable1);

        assertThat(runnable1.wasRun, equalTo(false));

        scheduler.reset();

        TestRunnable runnable2 = new TestRunnable();
        scheduler.post(runnable2);

        assertThat(runnable1.wasRun, equalTo(false));
        assertThat(runnable2.wasRun, equalTo(true));
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
