package org.robolectric.shadows;

import org.robolectric.internal.Implementation;
import org.robolectric.internal.Implements;
import org.robolectric.internal.RealObject;

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
