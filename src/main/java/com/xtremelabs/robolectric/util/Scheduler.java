package com.xtremelabs.robolectric.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

public class Scheduler {
    private List<PostedRunnable> postedRunnables = new ArrayList<PostedRunnable>();
    private long currentTime = 0;
    private boolean paused = false;
    private Thread associatedThread = Thread.currentThread();

    public synchronized long getCurrentTime() {
        return currentTime;
    }

    public synchronized void pause() {
        paused = true;
    }

    public synchronized void unPause() {
        paused = false;
        advanceToLastPostedRunnable();
    }

    public synchronized boolean isPaused() {
        return paused;
    }

    public synchronized void postDelayed(Runnable runnable, long delayMillis) {
        if (paused || delayMillis > 0 || Thread.currentThread() != associatedThread) {
	        postedRunnables.add(new PostedRunnable(runnable, currentTime + delayMillis));
	        Collections.sort(postedRunnables);
        } else {
            runnable.run();
        }
    }

    public synchronized void post(Runnable runnable) {
        postDelayed(runnable, 0);
    }

    public synchronized void postAtFrontOfQueue(Runnable runnable) {
        if (paused || Thread.currentThread() != associatedThread) {
        	postedRunnables.add(0, new PostedRunnable(runnable, currentTime));
        } else {
            runnable.run();
        }
    }

    public synchronized void remove(Runnable runnable) {
        ListIterator<PostedRunnable> iterator = postedRunnables.listIterator();
        while (iterator.hasNext()) {
            PostedRunnable next = iterator.next();
            if (next.runnable == runnable) {
                iterator.remove();
            }
        }
    }

    public synchronized boolean advanceToLastPostedRunnable() {
        if (enqueuedTaskCount() < 1) {
            return false;
        }

        return advanceTo(postedRunnables.get(postedRunnables.size() - 1).scheduledTime);
    }

    public synchronized boolean advanceToNextPostedRunnable() {
        if (enqueuedTaskCount() < 1) {
            return false;
        }

        return advanceTo(postedRunnables.get(0).scheduledTime);
    }

    public synchronized boolean advanceBy(long intervalMs) {
        long endingTime = currentTime + intervalMs;
        return advanceTo(endingTime);
    }

    public synchronized boolean advanceTo(long endingTime) {
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

    public synchronized boolean runOneTask() {
        if (enqueuedTaskCount() < 1) {
            return false;
        }

        PostedRunnable postedRunnable = postedRunnables.remove(0);
        currentTime = postedRunnable.scheduledTime;
        postedRunnable.run();
        return true;
    }

    public synchronized boolean runTasks(int howMany) {
        if (enqueuedTaskCount() < howMany) {
            return false;
        }

        while (howMany > 0) {
            PostedRunnable postedRunnable = postedRunnables.remove(0);
            currentTime = postedRunnable.scheduledTime;
            postedRunnable.run();
            howMany--;
        }
        return true;
    }

    public synchronized int enqueuedTaskCount() {
        return postedRunnables.size();
    }

    public synchronized boolean areAnyRunnable() {
        return nextTaskIsScheduledBefore(currentTime);
    }

    public synchronized void reset() {
        postedRunnables.clear();
        paused = false;
    }

    public synchronized int size() {
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

    private boolean nextTaskIsScheduledBefore(long endingTime) {
        return enqueuedTaskCount() > 0 && postedRunnables.get(0).scheduledTime <= endingTime;
    }
}
