package com.xtremelabs.robolectric.shadows;

import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;
import com.xtremelabs.robolectric.internal.RealObject;

import android.os.HandlerThread;
import android.os.Looper;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@Implements(HandlerThread.class)
public class ShadowHandlerThread {
    private Looper looper;

    @RealObject
    private HandlerThread thread;

    public void __constructor__(String name) {
        __constructor__(name, -1);
    }

    @SuppressWarnings("UnusedParameters")
    public void __constructor__(String name, int priority) {
    }

    @Implementation
    public void run() {
        Looper.prepare();
        synchronized (this) {
            looper = Looper.myLooper();
            onLooperPrepared();
            notifyAll();
        }
        Looper.loop();
    }

    @Implementation
    public Looper getLooper() {
        if (!thread.isAlive()) {
            return null;
        }

        // If the thread has been started, wait until the looper has been created.
        synchronized (this) {
            while (thread.isAlive() && looper == null) {
                try {
                    wait();
                } catch (InterruptedException ignored) {
                }
            }
        }
        return looper;
    }

    @Implementation
    public boolean quit() {
        Looper looper = getLooper();
        if (looper != null) {
            looper.quit();
            return true;
        }
        return false;
    }

    @Implementation
    public void onLooperPrepared() {
        Method prepared;
        try {
            prepared = HandlerThread.class.getDeclaredMethod("onLooperPrepared");
            prepared.setAccessible(true);
            prepared.invoke(thread);
        } catch (NoSuchMethodException ignored) {
        } catch (InvocationTargetException ignored) {
        } catch (IllegalAccessException ignored) {
        }
    }

}
