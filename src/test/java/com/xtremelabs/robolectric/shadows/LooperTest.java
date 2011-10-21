package com.xtremelabs.robolectric.shadows;

import android.os.Handler;
import android.os.Looper;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

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

}
