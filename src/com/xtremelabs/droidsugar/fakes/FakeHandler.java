package com.xtremelabs.droidsugar.fakes;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import com.xtremelabs.droidsugar.ProxyDelegatingHandler;
import com.xtremelabs.droidsugar.util.Implements;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(Handler.class)
public class FakeHandler {
    private Handler realHandler;
    private Looper looper;

    public FakeHandler(Handler realHandler) {
        this.realHandler = realHandler;
    }

    public void __constructor__() {
        this.looper = Looper.myLooper();
    }

    public void __constructor__(Looper looper) {
        this.looper = looper;
    }

    public final boolean post(Runnable r) {
        return postDelayed(r, 0);
    }

    public final boolean postDelayed(Runnable r, long delayMillis) {
        proxyFor(looper).post(r, delayMillis);
        return true;
    }

    public final Message obtainMessage(int what, Object obj) {
        Message message = new Message();
        message.what = what;
        message.obj = obj;
        return message;
    }

    public final boolean sendMessage(final Message msg) {
        post(new Runnable() {
            @Override
            public void run() {
                realHandler.handleMessage(msg);
            }
        });
        return true;
    }

    public static void flush() {
        proxyFor(Looper.myLooper()).idle();
    }

    private static FakeLooper proxyFor(Looper looper) {
        return (FakeLooper) ProxyDelegatingHandler.getInstance().proxyFor(looper);
    }
}
