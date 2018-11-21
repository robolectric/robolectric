package org.robolectric.shadows;

import static org.robolectric.shadow.api.Shadow.directlyOn;

import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.annotation.Resetter;

@Implements(Messenger.class)
public class ShadowMessenger {
  private static Message lastMessageSent = null;

  /** Returns the last {@link Message} sent, or {@code null} if there isn't any message sent. */
  public static Message getLastMessageSent() {
    return lastMessageSent;
  }

  @RealObject private Messenger messenger;

  @Implementation
  protected void send(Message message) throws RemoteException {
    lastMessageSent = Message.obtain(message);
    directlyOn(messenger, Messenger.class).send(message);
  }

  @Resetter
  public static void reset() {
    lastMessageSent = null;
  }
}
