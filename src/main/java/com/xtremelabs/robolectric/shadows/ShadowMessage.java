package com.xtremelabs.robolectric.shadows;


import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;
import com.xtremelabs.robolectric.internal.RealObject;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

@Implements(Message.class)
public class ShadowMessage {
    private Bundle data;
    private Handler target;
    private long when;

    @RealObject
    private Message message;

    @Implementation
    public void setData(Bundle data) {
        this.data = data;
    }

    @Implementation
    public void setTarget(Handler target) {
        this.target = target;
    }

    @Implementation
    public Bundle peekData() {
        return data;
    }

    @Implementation
    public Bundle getData() {
        if (data == null) {
            data = new Bundle();
        }
        return data;
    }

    @Implementation
    public Handler getTarget() {
        return target;
    }

    @Implementation
    public void copyFrom(Message m) {
        message.arg1 = m.arg1;
        message.arg2 = m.arg2;
        message.obj = m.obj;
        message.what = m.what;
        message.setData(m.getData());
    }

    @Implementation
    public static Message obtain() {
        return new Message();
    }

    @Implementation
    public static Message obtain(Handler h) {
        Message m = new Message();
        m.setTarget(h);
        return m;
    }

    @Implementation
    public static Message obtain(Handler h, int what) {
        Message m = obtain(h);
        m.what = what;
        return m;
    }

    @Implementation
    public static Message obtain(Handler h, int what, Object obj) {
        Message m = obtain(h, what);
        m.obj = obj;
        return m;
    }

    @Implementation
    public static Message obtain(Handler h, int what, int arg1, int arg2) {
        Message m = obtain(h, what);
        m.arg1 = arg1;
        m.arg2 = arg2;
        return m;
    }

    @Implementation
    public static Message obtain(Handler h, int what, int arg1, int arg2, Object obj) {
        Message m = obtain(h, what, arg1, arg2);
        m.obj = obj;
        return m;
    }

    @Implementation
    public static Message obtain(Message msg) {
        Message m = new Message();
        m.copyFrom(msg);
        m.setTarget(msg.getTarget());
        return m;
    }

    @Implementation
    public void sendToTarget() {
        target.sendMessage(message);
    }

    @Implementation
    public long getWhen() {
        return when;
    }

    public void setWhen(long when) {
        this.when = when;
    }
}
