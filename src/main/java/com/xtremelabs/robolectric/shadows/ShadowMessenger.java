package com.xtremelabs.robolectric.shadows;

import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;
import com.xtremelabs.robolectric.internal.RealObject;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

@Implements(Messenger.class)
public class ShadowMessenger {

    private Handler handler;

    public void __constructor__(Handler handler) {
        this.handler = handler;
    }

    @Implementation
    public void send(Message message) throws RemoteException {
        message.setTarget(handler);
        message.sendToTarget();
    }
}
