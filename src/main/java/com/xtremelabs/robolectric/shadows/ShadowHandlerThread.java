package com.xtremelabs.robolectric.shadows;

import android.os.HandlerThread;
import android.os.Looper;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

@Implements(HandlerThread.class)
public class ShadowHandlerThread {
    private String name;
    private Looper looper;

    public void __constructor__(String name) {
        __constructor__(name, -1);
    }

    public void __constructor__(String name, int priority) {
        this.name = name;
        looper = Looper.getMainLooper();
    }

    @Implementation
    public void run() {
        Looper.prepare();
        synchronized (this) {
            looper = Looper.myLooper();
            notifyAll();
        }
        Looper.loop();
    }

    @Implementation
    public Looper getLooper() {
        // If the thread has been started, wait until the looper has been created.
        synchronized (this) {
            while (looper == null) {
                try {
                    wait();
                } catch (InterruptedException e) {
                }
            }
        }
        return looper;
    }
}
