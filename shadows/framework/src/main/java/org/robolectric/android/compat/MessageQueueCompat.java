package org.robolectric.android.compat;

import static android.os.Build.VERSION_CODES.M;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.os.Build;
import android.os.Message;
import android.os.MessageQueue;
import android.os.SystemClock;
import org.robolectric.android.compat.MessageCompat._Message_;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.ForType;

/** */
public final class MessageQueueCompat {
  private MessageQueueCompat() {}

  public static boolean isIdle(MessageQueue messageQueue) {
    if (Build.VERSION.SDK_INT >= M) {
      return messageQueue.isIdle();
    } else {
      _MessageQueue_ internalQueue = reflector(_MessageQueue_.class, messageQueue);
      // this is a copy of the implementation from P
      synchronized (messageQueue) {
        final long now = SystemClock.uptimeMillis();
        Message headMsg = internalQueue.getMessages();
        if (headMsg == null) {
          return true;
        }
        long when = reflector(_Message_.class, headMsg).getWhen();
        return now < when;
      }
    }
  }

  /** Accessor interface for {@link MessageQueue}'s internals. */
  @ForType(MessageQueue.class)
  interface _MessageQueue_ {

    @Accessor("mMessages")
    Message getMessages();
  }
}
