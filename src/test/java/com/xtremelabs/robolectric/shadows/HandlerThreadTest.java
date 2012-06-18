package com.xtremelabs.robolectric.shadows;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;
import static org.junit.Assert.*;

import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.os.HandlerThread;
import android.os.Looper;

@RunWith(WithTestDefaultsRunner.class)
public class HandlerThreadTest {

    @Test
    public void shouldReturnLooper() throws Exception {
        HandlerThread handlerThread = new HandlerThread("test");
        handlerThread.start();
        assertNotNull(handlerThread.getLooper());
        assertNotSame(handlerThread.getLooper(), Robolectric.application.getMainLooper());
    }

    @Test
    public void shouldReturnNullIfThreadHasNotBeenStarted() throws Exception {
        HandlerThread handlerThread = new HandlerThread("test");
        assertNull(handlerThread.getLooper());
    }

    @Test
    public void shouldQuitLooperAndThread() throws Exception {
        HandlerThread handlerThread = new HandlerThread("test");
        handlerThread.start();
        assertTrue(handlerThread.isAlive());
        assertTrue(handlerThread.quit());
        handlerThread.join();
        assertFalse(handlerThread.isAlive());
    }

    @Test
    public void shouldStopThreadIfLooperIsQuit() throws Exception {
        HandlerThread ht = new HandlerThread("test1");
        ht.start();
        Looper looper = ht.getLooper();
        assertFalse(shadowOf(looper).quit);
        looper.quit();
        ht.join();
        assertFalse(ht.isAlive());
        assertTrue(shadowOf(looper).quit);
    }

    @Test
    public void shouldCallOnLooperPrepared() throws Exception {
        final Boolean[] wasCalled = new Boolean[] { false };
        HandlerThread t = new HandlerThread("test") {
            @Override
            protected void onLooperPrepared() {
                wasCalled[0] = true;
            }
        };
        t.start();
        assertNotNull(t.getLooper());
        assertTrue(wasCalled[0]);
    }
}
