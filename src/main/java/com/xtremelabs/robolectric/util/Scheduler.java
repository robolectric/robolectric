package com.xtremelabs.robolectric.util;

import java.util.Arrays;
import java.util.Iterator;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

public class Scheduler {
	private final static long DEFAULT_TIMEOUT_MS = 1000; // 1sec
    private final LinkedBlockingDeque<PostedRunnable> postedRunnables = new LinkedBlockingDeque<PostedRunnable>();
    private long currentTime = 0;
    private boolean paused = false;
    private final Thread associatedThread = Thread.currentThread();
    private boolean isConstantlyIdling = false;

	public long getCurrentTime() {
        return currentTime;
    }

	public void pause() {
        paused = true;
    }

	public void unPause() {
        paused = false;
        advanceToLastPostedRunnable(DEFAULT_TIMEOUT_MS);
    }

	public boolean isPaused() {
        return paused;
    }

	public void postDelayed(final Runnable runnable, final long delayMillis) {
        if ((!isConstantlyIdling && (paused || delayMillis > 0)) || Thread.currentThread() != associatedThread) {
        	postedRunnables.add(new PostedRunnable(runnable, currentTime + delayMillis));
        	sortQueue();
        } else {
            runnable.run();
        }
    }
	
	private synchronized void sortQueue(){
		PostedRunnable[] tmp = new PostedRunnable[postedRunnables.size()];
    	tmp = postedRunnables.toArray(tmp);
    	Arrays.sort(tmp);

    	postedRunnables.clear();

    	for(final PostedRunnable postedRunnable: tmp){
    		postedRunnables.add(postedRunnable);
    	}
	}

	public void post(final Runnable runnable) {
        postDelayed(runnable, 0);
    }


	public void postAtFrontOfQueue(final Runnable runnable) {
        if (paused || Thread.currentThread() != associatedThread) {
        	synchronized(this){
        		postedRunnables.addFirst(new PostedRunnable(runnable, currentTime));
        	}
        } else {
            runnable.run();
        }
    }

	public void remove(final Runnable runnable) {
        final Iterator<PostedRunnable> iterator = postedRunnables.iterator();
        while (iterator.hasNext()) {
            final PostedRunnable next = iterator.next();
            if (next.runnable == runnable) {
                iterator.remove();
            }
        }
    }

	public boolean advanceToLastPostedRunnable() {
    	return advanceToLastPostedRunnable(DEFAULT_TIMEOUT_MS);
    }

	public boolean advanceToLastPostedRunnable(final long timeoutMs) {
        final int size = postedRunnables.size();
        return runTasks(size, timeoutMs);
    }

	public boolean advanceToNextPostedRunnable() {
    	return advanceToNextPostedRunnable(DEFAULT_TIMEOUT_MS);
    }

	public boolean advanceToNextPostedRunnable(final long timeoutMs) {
    	final long scheduledTime = getScheduledTimeOfFirstTask(timeoutMs);
    	if(scheduledTime == -1){
    		return false;
    	}
    	
		return advanceTo(scheduledTime, timeoutMs);
    }

	public boolean advanceBy(final long intervalMs) {
    	return advanceBy(intervalMs, DEFAULT_TIMEOUT_MS);
    }

	public boolean advanceBy(final long intervalMs, final long timeoutMs) {
        final long endingTime = currentTime + intervalMs;
        return advanceTo(endingTime, timeoutMs);
    }

	public boolean advanceTo(final long endingTime) {
    	return advanceTo(endingTime, DEFAULT_TIMEOUT_MS);
    }

	public boolean advanceTo(final long endingTime, final long timeoutMs) {
		if (endingTime - currentTime < 0) {
            return false;
        }

        int runCount = 0;
		while (nextTaskIsScheduledBefore(endingTime, timeoutMs)) {
            runOneTask(timeoutMs);
            ++runCount;
        }
        currentTime = endingTime;

        return runCount > 0;
    }

	/**
	 * Same as {@link #runOneTask(long)}, only with a default timeout of {@value #DEFAULT_TIMEOUT_MS} milliseconds.
	 * @return <code>true</code> when task runs
	 */
	public boolean runOneTask() {
    	return runOneTask(DEFAULT_TIMEOUT_MS);
    }

	/**
	 * Runs one task that is (or will be) posted in the queue, waiting up to the specified
	 * wait time for the task to become available. 
	 * @param timeoutMs the time to wait
	 * @return <code>true</code> when task runs
	 */
	public boolean runOneTask(final long timeoutMs) {
        return runTasks(1, timeoutMs);
    }

	public boolean runTasks(int howMany, final long timeoutMs) {
		synchronized(this){
	        try {
				while (howMany > 0) {
				    final PostedRunnable postedRunnable = postedRunnables.poll(timeoutMs, TimeUnit.MILLISECONDS);
					if (postedRunnable == null) {
						return false;
					}
				    currentTime = postedRunnable.scheduledTime;
				    postedRunnable.run();
				    howMany--;
				}
			} catch (final InterruptedException e) {
				return false;
			}
	        return true;
		}
    }

	public int enqueuedTaskCount() {
        return postedRunnables.size();
    }

	public boolean areAnyRunnable() {
		return nextTaskIsScheduledBefore(currentTime, DEFAULT_TIMEOUT_MS);
    }

	public void reset() {
        postedRunnables.clear();
        paused = false;
        isConstantlyIdling = false;
    }

	public int size() {
        return postedRunnables.size();
    }

    public void idleConstantly(final boolean shouldIdleConstantly) {
        isConstantlyIdling = shouldIdleConstantly;
    }

    class PostedRunnable implements Comparable<PostedRunnable> {
        Runnable runnable;
        long scheduledTime;

        PostedRunnable(final Runnable runnable, final long scheduledTime) {
            this.runnable = runnable;
            this.scheduledTime = scheduledTime;
        }

        @Override
        public int compareTo(final PostedRunnable postedRunnable) {
            return (int) (scheduledTime - postedRunnable.scheduledTime);
        }

        public void run() {
            runnable.run();
        }
    }

	private boolean nextTaskIsScheduledBefore(final long endingTime, final long timeoutMs) {
		final long scheduledTime = getScheduledTimeOfFirstTask(timeoutMs);
		if(scheduledTime == -1){
			return false;
		}
		return scheduledTime <= endingTime;
    }
	
	private synchronized long getScheduledTimeOfFirstTask(final long timeoutMs) {
		PostedRunnable postedRunnable = null;
		try {
			postedRunnable = postedRunnables.poll(timeoutMs, TimeUnit.MILLISECONDS);
		} catch (final InterruptedException e) {
		}
		if(postedRunnable == null){
			return -1;
		}
		postedRunnables.addFirst(postedRunnable);
		return postedRunnable.scheduledTime;
	}
}
