package com.xtremelabs.robolectric.shadows;

import android.os.SystemClock;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;


@Implements(SystemClock.class)
public class ShadowSystemClock {
    private static long start = 0;

    static {
        start = System.currentTimeMillis();
    }

    @Implementation
    public static long currentThreadTimeMillis() {
        return uptimeMillis();
    }

    @Implementation
    public static long elapsedRealtime() {
        return uptimeMillis();
    }

    @Implementation
    public static boolean setCurrentTimeMillis(long millis) {
        return false;
    }

    @Implementation
    public static void sleep(long ms) {
        long    start       = uptimeMillis();
        long    duration    = ms;
        boolean interrupted = false;

        do {
            try {
                Thread.sleep(duration);
            } catch(InterruptedException e) {
                interrupted = true;
            }

            duration = (start + ms) - uptimeMillis();
        } while(duration > 0);

        if(interrupted) {
            // Important: we don't want to quietly eat an interrupt() event,
            // so we make sure to re-interrupt the thread so that the next
            // call to Thread.sleep() or Object.wait() will be interrupted.
            Thread.currentThread().interrupt();
        }
    }

    @Implementation
    public static long uptimeMillis() {
        return System.currentTimeMillis() - start;
    }
}
