package com.xtremelabs.robolectric.shadows;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;
import static org.junit.Assert.*;

@RunWith(WithTestDefaultsRunner.class)
public class LooperTest {

    @Test
    public void testMainLooperAndMyLooperAreTheSameInstanceOnMainThread() throws Exception {
        assertSame(Looper.myLooper(), Looper.getMainLooper());
    }

    @Test
    public void idleMainLooper_executesScheduledTasks() {
        final boolean[] wasRun = new boolean[]{false};
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                wasRun[0] = true;
            }
        }, 2000);

        assertFalse(wasRun[0]);
        ShadowLooper.idleMainLooper(1999);
        assertFalse(wasRun[0]);
        ShadowLooper.idleMainLooper(1);
        assertTrue(wasRun[0]);
    }

    @Test
    public void idleConstantly_runsPostDelayedTasksImmediately() {
        ShadowLooper.idleMainLooperConstantly(true);
        final boolean[] wasRun = new boolean[]{false};
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                wasRun[0] = true;
            }
        }, 2000);

        assertTrue(wasRun[0]);
    }

    @Test
    public void differentThreadsGetDifferentLoopers() {
        Looper mainLooper = Looper.getMainLooper();
        Looper thisThreadsLooper = Looper.myLooper();

        assertSame("junit test's thread should use the main looper", mainLooper, thisThreadsLooper);

        final Looper[] thread1Looper = new Looper[1];
        new Thread() {
            @Override
            public void run() {
                Looper.prepare();
                thread1Looper[0] = Looper.myLooper();
            }
        }.start();

        while(thread1Looper[0] == null) {
            Thread.yield();
        }

        assertNotSame(mainLooper, thread1Looper[0]);
    }

    @Test(expected = RuntimeException.class)
    public void shouldThrowRuntimeExceptionIfTryingToQuitMainLooper() throws Exception {
        Looper.getMainLooper().quit();
    }

    @Test
    public void shouldNotQueueMessagesIfLooperIsQuit() throws Exception {
        HandlerThread ht = new HandlerThread("test1");
        ht.start();
        Looper looper = ht.getLooper();
        looper.quit();
        assertTrue(shadowOf(looper).hasQuit());
        assertFalse(shadowOf(looper).post(new Runnable() {
            @Override public void run() { }
        }, 0));

        assertFalse(shadowOf(looper).postAtFrontOfQueue(new Runnable() {
            @Override
            public void run() {
            }
        }));
        assertFalse(shadowOf(looper).getScheduler().areAnyRunnable());
    }

    @Test
    public void shouldThrowawayRunnableQueueIfLooperQuits() throws Exception {
        HandlerThread ht = new HandlerThread("test1");
        ht.start();
        Looper looper = ht.getLooper();
        shadowOf(looper).pause();
        shadowOf(looper).post(new Runnable() {
            @Override
            public void run() {
            }
        }, 0);
        looper.quit();
        assertTrue(shadowOf(looper).hasQuit());
        assertFalse(shadowOf(looper).getScheduler().areAnyRunnable());
    }
    
    @Test
    public void testLoopThread() {
    	assertTrue(shadowOf(Looper.getMainLooper()).getThread() == Thread.currentThread());
    }
}
