package com.xtremelabs.robolectric.shadows;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;
import com.xtremelabs.robolectric.internal.RealObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;

/**
 * Shadow for Handler that puts posted {@link Runnable}s into a queue instead of sending them to be handled on a
 * separate thread.{@link Runnable}s that are scheduled to be executed immediately can be triggered by calling
 * {@link #idleMainLooper()}.
 * todo: add utility method to advance time and trigger execution of Runnables scheduled for a time in the future
 */
@SuppressWarnings({"UnusedDeclaration"})
@Implements(Handler.class)
public class ShadowHandler {
    @RealObject
    private Handler realHandler;
    private Looper looper = Looper.myLooper();
    private List<Message> messages = new ArrayList<Message>();

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
    public final boolean postAtFrontOfQueue(Runnable runnable) {
        shadowOf(looper).postAtFrontOfQueue(runnable);
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
        return sendMessageDelayed(msg, 0L);
    }

    @Implementation
    public final boolean sendMessageDelayed(final Message msg, long delayMillis) {
        messages.add(msg);
        postDelayed(new Runnable() {
            @Override
            public void run() {
                if (messages.contains(msg)) {
                    realHandler.handleMessage(msg);
                    messages.remove(msg);
                }
            }
        }, delayMillis);
        return true;
    }

    @Implementation
    public final boolean sendEmptyMessage(int what) {
        return sendEmptyMessageDelayed(what, 0L);
    }

    @Implementation
    public final boolean sendEmptyMessageDelayed(int what, long delayMillis) {
        final Message msg = new Message();
        msg.what = what;
        return sendMessageDelayed(msg, delayMillis);
    }
    
    @Implementation
    public final Looper getLooper() {
    	return looper;
    }

    @Implementation
    public final void removeCallbacks(java.lang.Runnable r) {
        shadowOf(looper).getScheduler().remove(r);
    }

    @Implementation
    public final boolean hasMessages(int what) {
        for (Message message : messages) {
            if (message.what == what) {
                return true;
            }
        }
        return false;
    }


    @Implementation
    public final void removeMessages(int what) {
        for (Iterator<Message> iterator = messages.iterator(); iterator.hasNext(); ) {
            Message message = iterator.next();
            if (message.what == what) {
                iterator.remove();
            }
        }
    }

    /**
     * @deprecated use {@link #idleMainLooper()} instead
     */
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
