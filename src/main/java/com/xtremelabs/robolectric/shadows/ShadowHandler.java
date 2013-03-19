package com.xtremelabs.robolectric.shadows;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.xtremelabs.robolectric.Robolectric;
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
    @RealObject
    private Handler realHandler;
    private Looper looper = Looper.myLooper();
    private final List<Message> messages = new ArrayList<Message>();
    private Handler.Callback callback;

    public void __constructor__() {
        this.looper = Looper.myLooper();
    }

    public void __constructor__(final Looper looper) {
        this.looper = looper;
    }

    public void __constructor__(final Handler.Callback callback) {
        this.callback = callback;
    }

    @Implementation
    public boolean post(final Runnable r) {
        return postDelayed(r, 0);
    }

    @Implementation
    public boolean postDelayed(final Runnable r, final long delayMillis) {
        return shadowOf(looper).post(r, delayMillis);
    }

    @Implementation
    public final boolean postAtFrontOfQueue(final Runnable runnable) {
        return shadowOf(looper).postAtFrontOfQueue(runnable);
    }

    @Implementation
    public Message obtainMessage() {
        return obtainMessage(0);
    }

    @Implementation
    public Message obtainMessage(final int what) {
        return obtainMessage(what, null);
    }

    @Implementation
    public Message obtainMessage(final int what, final Object obj) {
        return obtainMessage(what, 0, 0, obj);
    }

    @Implementation
    public Message obtainMessage(final int what, final int arg1, final int arg2) {
        return obtainMessage(what, arg1, arg2, null);
    }

    @Implementation
    public Message obtainMessage(final int what, final int arg1, final int arg2, final Object obj) {
        final Message message = new Message();
        message.what = what;
        message.arg1 = arg1;
        message.arg2 = arg2;
        message.obj = obj;
        message.setTarget(realHandler);
        return message;
    }

    @Implementation
    public final boolean sendMessage(final Message msg) {
        return sendMessageDelayed(msg, 0L);
    }

    @Implementation
    public final boolean sendMessageDelayed(final Message msg, final long delayMillis) {
        Robolectric.shadowOf(msg).setWhen(Robolectric.shadowOf(looper).getScheduler().getCurrentTime()+delayMillis);
        messages.add(msg);
        postDelayed(new Runnable() {
            @Override
            public void run() {
                if (messages.contains(msg)) {
                    messages.remove(msg);
                    routeMessage(msg);
                }
            }
        }, delayMillis);
        return true;
    }

    private void routeMessage(final Message msg) {
        if(callback != null) {
            callback.handleMessage(msg);
        } else {
            realHandler.handleMessage(msg);
        }
    }

    @Implementation
    public final boolean sendEmptyMessage(final int what) {
        return sendEmptyMessageDelayed(what, 0L);
    }

    @Implementation
    public final boolean sendEmptyMessageDelayed(final int what, final long delayMillis) {
        final Message msg = new Message();
        msg.what = what;
        return sendMessageDelayed(msg, delayMillis);
    }

    @Implementation
    public final boolean sendMessageAtFrontOfQueue(final Message msg) {
        Robolectric.shadowOf(msg).setWhen(Robolectric.shadowOf(looper).getScheduler().getCurrentTime());
        messages.add(0, msg);
        postAtFrontOfQueue(new Runnable() {
            @Override
            public void run() {
                if (messages.contains(msg)) {
                    messages.remove(msg);
                    routeMessage(msg);
                }
            }
        });
        return true;
    }

    @Implementation
    public final Looper getLooper() {
        return looper;
    }

    @Implementation
    public final void removeCallbacks(final java.lang.Runnable r) {
        shadowOf(looper).getScheduler().remove(r);
    }

    @Implementation
    public final boolean hasMessages(final int what) {
        for (final Message message : messages) {
            if (message.what == what) {
                return true;
            }
        }
        return false;
    }

    @Implementation
    public final boolean hasMessages(final int what, final Object object) {
        for (final Message message : messages) {
            if(message.what == what && message.obj == object) {
                return true;
            }
        }
        return false;
    }


    @Implementation
    public final void removeMessages(final int what) {
        removeMessages(what, null);
    }

    @Implementation
    public final void removeMessages(final int what, final Object object) {
        for (final Iterator<Message> iterator = messages.iterator(); iterator.hasNext(); ) {
            final Message message = iterator.next();
            if (message.what == what && (object == null || object.equals(message.obj))) {
                iterator.remove();
            }
        }
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
     * @see ShadowLooper#runOneTask() ()
     * @param timeoutMs time in milliseconds to wait for task to become available
     */
    public static void runMainLooperOneTask(final long timeoutMs) {
    	shadowOf(Looper.myLooper()).runOneTask();
    }

    /**
     * @see ShadowLooper#runToNextTask() ()
     */
    public static void runMainLooperToNextTask() {
        shadowOf(Looper.myLooper()).runToNextTask();
    }
}
