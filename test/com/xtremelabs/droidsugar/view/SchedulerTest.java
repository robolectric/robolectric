package com.xtremelabs.droidsugar.view;

import org.junit.Test;

public class SchedulerTest {
    private Transcript transcript;

    @Test
    public void testFakePostDelayed() throws Exception {
        transcript = new Transcript();
        Scheduler Scheduler = new Scheduler();
        Scheduler.postDelayed(new AddToTranscript("one"), 1000);
        Scheduler.postDelayed(new AddToTranscript("two"), 2000);
        Scheduler.postDelayed(new AddToTranscript("three"), 3000);

        Scheduler.tick(1000);
        transcript.assertEventsSoFar("one");

        Scheduler.tick(500);
        transcript.assertNoEventsSoFar();

        Scheduler.tick(501);
        transcript.assertEventsSoFar("two");

        Scheduler.tick(999);
        transcript.assertEventsSoFar("three");
    }

    @Test
    public void testFakePostDelayed_WhenMoreItemsAreAdded() throws Exception {
        transcript = new Transcript();
        final Scheduler Scheduler = new Scheduler();
        Scheduler.postDelayed(new Runnable() {
            @Override
            public void run() {
                transcript.add("one");
                Scheduler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        transcript.add("two");
                        Scheduler.postDelayed(new AddToTranscript("three"), 1000);
                    }
                }, 1000);
            }
        }, 1000);

        Scheduler.tick(1000);
        transcript.assertEventsSoFar("one");

        Scheduler.tick(500);
        transcript.assertNoEventsSoFar();

        Scheduler.tick(501);
        transcript.assertEventsSoFar("two");

        Scheduler.tick(999);
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
