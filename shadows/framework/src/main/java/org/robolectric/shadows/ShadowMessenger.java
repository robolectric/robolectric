package org.robolectric.shadows;

import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

@Implements(Messenger.class)
public class ShadowMessenger {
  private Handler handler;

  @Implementation
  protected void __constructor__(Handler handler) {
    this.handler = handler;
  }

  @Implementation
  protected void send(Message message) throws RemoteException {
    message.setTarget(handler);
    message.sendToTarget();
  }
}
