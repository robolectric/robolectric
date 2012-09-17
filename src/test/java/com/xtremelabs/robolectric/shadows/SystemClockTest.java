package com.xtremelabs.robolectric.shadows;

import static junit.framework.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import android.os.SystemClock;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;


@RunWith(WithTestDefaultsRunner.class)
public class SystemClockTest {
    @Test
    public void shouldUptimeMilisBeNonNegative() {
        assertTrue(SystemClock.uptimeMillis() >= 0);
    }

    @Test
    public void shouldUptimeMilisCounting() throws Exception {
        long       startTime = SystemClock.uptimeMillis();
        final long DELTA     = 100;

        Thread.sleep(DELTA);

        assertTrue((SystemClock.uptimeMillis() - startTime) >= DELTA);
    }

    @Test
    public void shouldSleepThread() {
        long       startTime = SystemClock.uptimeMillis();
        final long DELTA     = 200;

        SystemClock.sleep(DELTA);

        assertTrue((SystemClock.uptimeMillis() - startTime) >= DELTA);
    }
}
