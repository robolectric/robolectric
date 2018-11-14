package org.robolectric.shadows;

import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.util.ReflectionHelpers;

@Implements(Messenger.class)
public class ShadowMessenger {
  private static Message lastMessageSent = null;

  /** Returns the last {@link Message} sent, or {@code null} if there isn't any message sent. */
  public static Message getLastMessageSent() {
    return lastMessageSent;
  }

  /** Clears the last {@link Message} sent. */
  public static void clearLastMessageSent() {
    lastMessageSent = null;
  }

  @RealObject private Messenger messenger;
  private Handler handler;

  @Implementation
  protected void __constructor__(Handler handler) {
    this.handler = handler;
    Object target = ReflectionHelpers.callInstanceMethod(handler, "getIMessenger");
    ReflectionHelpers.setField(messenger, "mTarget", target);
  }

  @Implementation
  protected void __constructor__(IBinder target) {
    if (target != null && target instanceof FakeBinder) {
      handler = ((FakeBinder) target).handler;
    }
  }

  @Implementation
  protected void send(Message message) throws RemoteException {
    lastMessageSent = message;
    message.setTarget(handler);
    message.sendToTarget();
  }

  @Implementation
  protected IBinder getBinder() {
    return new FakeBinder(handler);
  }

  private static class FakeBinder extends Binder {
    final Handler handler;

    public FakeBinder(Handler handler) {
      this.handler = handler;
    }
  }
}
