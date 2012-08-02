package com.xtremelabs.robolectric.shadows;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;
import static org.junit.Assert.*;

import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.os.HandlerThread;
import android.os.Looper;

@RunWith(WithTestDefaultsRunner.class)
public class HandlerThreadTest {
	
	private HandlerThread handlerThread;
	
	@After
	public void tearDown() throws Exception {
		// Try to ensure we've exited the thread at the end of each test
		if ( handlerThread != null ) {
			handlerThread.quit();
			handlerThread.join();
		}
	}

    @Test
    public void shouldReturnLooper() throws Exception {
        handlerThread = new HandlerThread("test");
        handlerThread.start();
        assertNotNull(handlerThread.getLooper());
        assertNotSame(handlerThread.getLooper(), Robolectric.application.getMainLooper());
    }

    @Test
    public void shouldReturnNullIfThreadHasNotBeenStarted() throws Exception {
        handlerThread = new HandlerThread("test");
        assertNull(handlerThread.getLooper());
    }

    @Test
    public void shouldQuitLooperAndThread() throws Exception {
        handlerThread = new HandlerThread("test");
        handlerThread.start();
        assertTrue(handlerThread.isAlive());
        assertTrue(handlerThread.quit());
        handlerThread.join();
        assertFalse(handlerThread.isAlive());
        handlerThread = null;
    }

    @Test
    public void shouldStopThreadIfLooperIsQuit() throws Exception {
        handlerThread = new HandlerThread("test1");
        handlerThread.start();
        Looper looper = handlerThread.getLooper();
        assertFalse(shadowOf(looper).quit);
        looper.quit();
        handlerThread.join();
        assertFalse(handlerThread.isAlive());
        assertTrue(shadowOf(looper).quit);
        handlerThread = null;
    }

    @Test
    public void shouldCallOnLooperPrepared() throws Exception {
        final Boolean[] wasCalled = new Boolean[] { false };
        handlerThread = new HandlerThread("test") {
            @Override
            protected void onLooperPrepared() {
                wasCalled[0] = true;
            }
        };
        handlerThread.start();
        assertNotNull(handlerThread.getLooper());
        assertTrue(wasCalled[0]);
    }
}
