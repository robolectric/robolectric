package com.xtremelabs.droidsugar.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Scheduler {
    private List<PostedRunnable> postedRunnables = new ArrayList<PostedRunnable>();
    private long currentTime = 0;

    public void postDelayed(Runnable runnable, long delayMillis) {
        postedRunnables.add(new PostedRunnable(runnable, currentTime + delayMillis));
    }

    public void post(Runnable runnable) {
        postedRunnables.add(new PostedRunnable(runnable, currentTime));
    }

    public void tick(long intervalMs) {
        Collections.sort(postedRunnables);

        long endingTime = currentTime + intervalMs;
        while (postedRunnables.size() > 0 && postedRunnables.get(0).scheduledTime <= endingTime) {
            long timeUntilNext = postedRunnables.get(0).scheduledTime - currentTime;

            if (timeUntilNext <= intervalMs) {
                currentTime += timeUntilNext;
                intervalMs -= timeUntilNext;
            }

            PostedRunnable postedRunnable = postedRunnables.remove(0);
            postedRunnable.run();
        }

        currentTime += intervalMs;
    }

    public int enqueuedTaskCount() {
        return postedRunnables.size();
    }

    class PostedRunnable implements Comparable<PostedRunnable> {
        Runnable runnable;
        long scheduledTime;

        PostedRunnable(Runnable runnable, long scheduledTime) {
            this.runnable = runnable;
            this.scheduledTime = scheduledTime;
        }

        @Override
        public int compareTo(PostedRunnable postedRunnable) {
            return (int) (scheduledTime - postedRunnable.scheduledTime);
        }

        public void run() {
            runnable.run();
        }
    }
}
