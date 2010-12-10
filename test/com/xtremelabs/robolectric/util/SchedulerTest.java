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
