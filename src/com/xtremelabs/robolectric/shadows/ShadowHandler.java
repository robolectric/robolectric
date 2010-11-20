package com.xtremelabs.robolectric.shadows;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import com.xtremelabs.robolectric.util.Implementation;
import com.xtremelabs.robolectric.util.Implements;
import com.xtremelabs.robolectric.util.RealObject;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;

/**
 * Shadow for Handler that puts posted {@link Runnable}s into a queue instead of sending them to be handled on a
 * separate thread.{@link Runnable}s that are scheduled to be executed immediately can be triggered by calling
 * {@link #flush()}.
 * todo: add utility method to advance time and trigger execution of Runnables scheduled for a time in the future
 */
@SuppressWarnings({"UnusedDeclaration"})
@Implements(Handler.class)
public class ShadowHandler {
    @RealObject private Handler realHandler;

    private Looper looper;

    public void __constructor__() {
        this.looper = Looper.myLooper();
    }

    public void __constructor__(Looper looper) {
        this.looper = looper;
    }

    @Implementation
    public boolean post(Runnable r) {
        return postDelayed(r, 0);
    }

    @Implementation
    public boolean postDelayed(Runnable r, long delayMillis) {
        shadowOf(looper).post(r, delayMillis);
        return true;
    }

    @Implementation
    public Message obtainMessage(int what, Object obj) {
        Message message = new Message();
        message.what = what;
        message.obj = obj;
        return message;
    }

    @Implementation
    public final boolean sendMessage(final Message msg) {
        post(new Runnable() {
            @Override
            public void run() {
                realHandler.handleMessage(msg);
            }
        });
        return true;
    }

    @Implementation
    public final boolean sendEmptyMessage(int what) {
        final Message msg = new Message();
        msg.what = what;
        return sendMessage(msg);
    }

    /**
     * Causes any posted {@link Runnable}s that are scheduled to run immediately to actually be run. This gives
     * visibility into the order in which things will happen. An event can be triggered and its immediate effects
     * examined, and then {@code flush()} can be called and the side-effects of the original event can be examined.
     */
    public static void flush() {
        shadowOf(Looper.myLooper()).idle();
    }
}
