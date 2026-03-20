package org.robolectric.shadows;

import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import org.robolectric.annotation.Filter;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;

@Implements(Messenger.class)
public class ShadowMessenger {
  private static Message lastMessageSent = null;

  /** Returns the last {@link Message} sent, or {@code null} if there isn't any message sent. */
  public static Message getLastMessageSent() {
    return lastMessageSent;
  }

  @Filter
  protected void send(Message message) throws RemoteException {
    lastMessageSent = Message.obtain(message);
  }

  @Resetter
  public static void reset() {
    lastMessageSent = null;
  }
}
