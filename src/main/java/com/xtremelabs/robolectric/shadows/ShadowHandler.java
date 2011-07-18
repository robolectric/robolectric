package com.xtremelabs.robolectric.shadows;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Handler.Callback;

import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;
import com.xtremelabs.robolectric.internal.RealObject;

/**
 * Shadow for Handler that puts posted {@link Runnable}s into a queue instead of sending them to be handled on a
 * separate thread.{@link Runnable}s that are scheduled to be executed immediately can be triggered by calling
 * {@link #idleMainLooper()}.
 * todo: add utility method to advance time and trigger execution of Runnables scheduled for a time in the future
 */
@SuppressWarnings({"UnusedDeclaration"})
@Implements(Handler.class)
public class ShadowHandler {
    @RealObject private Handler realHandler;

    private Looper looper = Looper.myLooper();

    private Callback callback;

    public void __constructor__() {
        this.looper = Looper.myLooper();
    }
    
    public void __constructor__(final Handler.Callback callback) {
        __constructor__();
        this.callback = callback;
    }

    public void __constructor__(final Looper looper) {
        this.looper = looper;
    }

    @Implementation
    public boolean post(final Runnable r) {
        return postDelayed(r, 0);
    }

    @Implementation
    public boolean postDelayed(final Runnable r, final long delayMillis) {
        shadowOf(looper).post(r, delayMillis);
        return true;
    }

    @Implementation
    public Message obtainMessage(final int what, final Object obj) {
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
                if (callback != null) {
                    callback.handleMessage(msg);
                } else {
                    realHandler.handleMessage(msg);
                }
            }
        });
        return true;
    }

    @Implementation
    public final boolean sendEmptyMessage(final int what) {
        final Message msg = new Message();
        msg.what = what;
        return sendMessage(msg);
    }

    /**
     * @deprecated use {@link #idleMainLooper()} instead
     */
    @Deprecated
    public static void flush() {
        idleMainLooper();
    }

    /**
     * @see com.xtremelabs.robolectric.shadows.ShadowLooper#idle()
     */
    public static void idleMainLooper() {
        shadowOf(Looper.myLooper()).idle();
    }

    /**
     * @see ShadowLooper#runToEndOfTasks() ()
     */
    public static void runMainLooperToEndOfTasks() {
        shadowOf(Looper.myLooper()).runToEndOfTasks();
    }


    /**
     * @see ShadowLooper#runOneTask() ()
     */
    public static void runMainLooperOneTask() {
        shadowOf(Looper.myLooper()).runOneTask();
    }

    /**
     * @see ShadowLooper#runToNextTask() ()
     */
    public static void runMainLooperToNextTask() {
        shadowOf(Looper.myLooper()).runToNextTask();
    }
}
