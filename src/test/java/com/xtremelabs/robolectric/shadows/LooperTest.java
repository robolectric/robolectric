package com.xtremelabs.robolectric.shadows;

import android.os.Handler;
import android.os.Looper;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

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

}
