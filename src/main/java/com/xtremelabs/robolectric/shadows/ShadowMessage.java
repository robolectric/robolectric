package com.xtremelabs.robolectric.shadows;


import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;
import com.xtremelabs.robolectric.internal.RealObject;

import android.os.Bundle;
import android.os.Message;

@Implements(Message.class)
public class ShadowMessage {
    private Bundle data;

    @RealObject
    private Message message;

    @Implementation
    public void setData(Bundle data) {
        this.data = data;
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
    public void copyFrom(Message m) {
        message.arg1 = m.arg1;
        message.arg2 = m.arg2;
        message.obj = m.obj;
        message.setData(m.getData());
    }
}
