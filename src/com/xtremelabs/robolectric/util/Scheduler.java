package com.xtremelabs.robolectric.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Scheduler {
    private List<PostedRunnable> postedRunnables = new ArrayList<PostedRunnable>();
    private long currentTime = 0;
    private boolean paused = false;


    public void pause() {
        paused = true;
    }

    public void unPause() {
        paused = false;
        advanceToLastPostedRunnable();
    }

    public void postDelayed(Runnable runnable, long delayMillis) {
        if (paused || delayMillis > 0) {
            postedRunnables.add(new PostedRunnable(runnable, currentTime + delayMillis));
            Collections.sort(postedRunnables);
        } else {
            runnable.run();
        }
    }

    public void post(Runnable runnable) {
        postDelayed(runnable, 0);
    }

    public boolean advanceToLastPostedRunnable() {
        if (enqueuedTaskCount() < 1) {
            return false;
        }

        return advanceTo(postedRunnables.get(postedRunnables.size() - 1).scheduledTime);
    }

    public boolean advanceToNextPostedRunnable() {
        if (enqueuedTaskCount() < 1) {
            return false;
        }

        return advanceTo(postedRunnables.get(0).scheduledTime);
    }

    public boolean advanceBy(long intervalMs) {
        long endingTime = currentTime + intervalMs;
        return advanceTo(endingTime);
    }

    public boolean advanceTo(long endingTime) {
        if (endingTime - currentTime < 0 || enqueuedTaskCount() < 1) {
            return false;
        }

        int runCount = 0;
        while (nextTaskIsScheduledBefore(endingTime)) {
            runOneTask();
            ++runCount;
        }
        currentTime = endingTime;

        return runCount > 0;
    }

    public boolean runOneTask() {
        if (enqueuedTaskCount() < 1) {
            return false;
        }

        PostedRunnable postedRunnable = postedRunnables.remove(0);
        currentTime = postedRunnable.scheduledTime;
        postedRunnable.run();
        return true;
    }

    public int enqueuedTaskCount() {
        return postedRunnables.size();
    }

    public boolean areAnyRunnable() {
        return nextTaskIsScheduledBefore(currentTime);
    }

    public void reset() {
        postedRunnables.clear();
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

    private boolean nextTaskIsScheduledBefore(long endingTime) {
        return enqueuedTaskCount() > 0 && postedRunnables.get(0).scheduledTime <= endingTime;
    }
}
