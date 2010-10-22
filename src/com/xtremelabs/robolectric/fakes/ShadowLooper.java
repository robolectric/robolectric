package com.xtremelabs.robolectric.fakes;

import android.os.Looper;
import com.xtremelabs.robolectric.util.Implementation;
import com.xtremelabs.robolectric.util.Implements;
import com.xtremelabs.robolectric.util.Scheduler;

import static com.xtremelabs.robolectric.Robolectric.newInstanceOf;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(Looper.class)
public class ShadowLooper {
    private static ThreadLocal<Looper> sThreadLocal;
    private static Looper MAIN_LOOPER;

    static {
        resetAll();
    }

    public static void resetAll() {
        sThreadLocal = new ThreadLocal<Looper>() {
            @Override
            protected Looper initialValue() {
                return newInstanceOf(Looper.class);
            }
        };

        MAIN_LOOPER = sThreadLocal.get();
    }

    @Implementation
    public static Looper getMainLooper() {
        return MAIN_LOOPER;
    }

    @Implementation
    public static Looper myLooper() {
        return sThreadLocal.get();
    }

    public Scheduler scheduler = new Scheduler();

    public void idle() {
        scheduler.tick(0);
    }

    public void post(Runnable r, long delayMillis) {
        scheduler.postDelayed(r, delayMillis);
    }

    public void reset() {
        scheduler.reset();
    }
}
