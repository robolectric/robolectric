package com.xtremelabs.robolectric.util;

import java.util.Arrays;
import java.util.Iterator;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

public class Scheduler {
	private final static long DEFAULT_TIMEOUT_MS = 10000; //10sec
    private final LinkedBlockingDeque<PostedRunnable> postedRunnables = new LinkedBlockingDeque<PostedRunnable>();
    private long currentTime = 0;
    private boolean paused = false;
    private final Thread associatedThread = Thread.currentThread();
    private boolean isConstantlyIdling = false;

    public synchronized long getCurrentTime() {
        return currentTime;
    }

    public synchronized void pause() {
        paused = true;
    }

    public synchronized void unPause() {
        paused = false;
        advanceToLastPostedRunnable(DEFAULT_TIMEOUT_MS);
    }

    public synchronized boolean isPaused() {
        return paused;
    }

    public synchronized void postDelayed(final Runnable runnable, final long delayMillis) {
        if ((!isConstantlyIdling && (paused || delayMillis > 0)) || Thread.currentThread() != associatedThread) {
        	postedRunnables.add(new PostedRunnable(runnable, currentTime + delayMillis));
        	
        	PostedRunnable[] tmp = new PostedRunnable[postedRunnables.size()];
        	tmp = postedRunnables.toArray(tmp);
        	Arrays.sort(tmp);
        	
        	postedRunnables.clear();
        	
        	for(final PostedRunnable postedRunnable: tmp){
        		postedRunnables.add(postedRunnable);
        	}
        } else {
            runnable.run();
        }
    }

    public synchronized void post(final Runnable runnable) {
        postDelayed(runnable, 0);
    }

    
    public synchronized void postAtFrontOfQueue(final Runnable runnable) {
        if (paused || Thread.currentThread() != associatedThread) {
        	postedRunnables.addFirst(new PostedRunnable(runnable, currentTime));
        } else {
            runnable.run();
        }
    }

    public synchronized void remove(final Runnable runnable) {
        final Iterator<PostedRunnable> iterator = postedRunnables.iterator();
        while (iterator.hasNext()) {
            final PostedRunnable next = iterator.next();
            if (next.runnable == runnable) {
                iterator.remove();
            }
        }
    }
    
    public synchronized boolean advanceToLastPostedRunnable() {
    	if (enqueuedTaskCount() < 1) {
    		return false;
    	}
    	
    	return advanceToLastPostedRunnable(DEFAULT_TIMEOUT_MS);
    }

    public synchronized boolean advanceToLastPostedRunnable(final long timeoutMs) {
        final int size = postedRunnables.size();
        return runTasks(size, timeoutMs);
    }

    public synchronized boolean advanceToNextPostedRunnable() {
    	if (enqueuedTaskCount() < 1) {
    		return false;
    	}
    	
    	return advanceToNextPostedRunnable(DEFAULT_TIMEOUT_MS);
    }
    
    public synchronized boolean advanceToNextPostedRunnable(final long timeoutMs) {
        try {
			final PostedRunnable postedRunnable = postedRunnables.poll(timeoutMs, TimeUnit.MILLISECONDS);
			if(postedRunnable != null){
				postedRunnables.add(postedRunnable);
				return advanceTo(postedRunnable.scheduledTime, timeoutMs);
			}
		} catch (final InterruptedException e) {
		}

    	return false;
    }

    public synchronized boolean advanceBy(final long intervalMs) {
    	return advanceBy(intervalMs, DEFAULT_TIMEOUT_MS);
    }
    
    public synchronized boolean advanceBy(final long intervalMs, final long timeoutMs) {
        final long endingTime = currentTime + intervalMs;
        return advanceTo(endingTime, timeoutMs);
    }

    public synchronized boolean advanceTo(final long endingTime) {
    	return advanceTo(endingTime, DEFAULT_TIMEOUT_MS);
    }
    
    public synchronized boolean advanceTo(final long endingTime, final long timeoutMs) {
        if (endingTime - currentTime < 0 || enqueuedTaskCount() < 1) {
            return false;
        }

        int runCount = 0;
        while (nextTaskIsScheduledBefore(endingTime)) {
            runOneTask(timeoutMs);
            ++runCount;
        }
        currentTime = endingTime;

        return runCount > 0;
    }

    public synchronized boolean runOneTask() {
    	if (enqueuedTaskCount() < 1) {
    		return false;
    	}
    	
    	return runOneTask(DEFAULT_TIMEOUT_MS);
    }
    
    public synchronized boolean runOneTask(final long timeoutMs) {
        return runTasks(1, timeoutMs);
    }

    public synchronized boolean runTasks(final int howMany) {
    	if (enqueuedTaskCount() < howMany) {
    		return false;
    	}
    	
    	return runTasks(howMany, DEFAULT_TIMEOUT_MS);
    }
    
    public synchronized boolean runTasks(int howMany, final long timeoutMs) {
        try {
			while (howMany > 0) {
			    final PostedRunnable postedRunnable = postedRunnables.poll(timeoutMs, TimeUnit.MILLISECONDS);
			    currentTime = postedRunnable.scheduledTime;
			    postedRunnable.run();
			    howMany--;
			}
		} catch (final InterruptedException e) {
			return false;
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
        isConstantlyIdling = false;
    }

    public synchronized int size() {
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

    private boolean nextTaskIsScheduledBefore(final long endingTime) {
        return enqueuedTaskCount() > 0 && postedRunnables.peek().scheduledTime <= endingTime;
    }
}
