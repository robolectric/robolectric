package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.BAKLAVA;
import static android.os.Build.VERSION_CODES.VANILLA_ICE_CREAM;
import static org.robolectric.RuntimeEnvironment.getApiLevel;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.os.Looper;
import android.os.Message;
import android.os.MessageQueue;
import android.os.TestLooperManager;
import java.util.Objects;
import javax.annotation.Nullable;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.shadows.ShadowMessage.MessageReflector;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.Direct;
import org.robolectric.util.reflector.ForType;

/**
 * Compat class that supports the {@link TestLooperManager} Baklava+ functionality on older Android
 * SDKs.
 */
@SuppressWarnings("NonFinalStaticField")
final class TestLooperManagerCompat implements AutoCloseable {

  private final MessageQueue queue;

  // the TestLooperManager to defer to. Will only be non-null if running
  // on an Android API level that supports it
  private final TestLooperManager delegate;

  TestLooperManagerCompat(MessageQueue queue) {
    this.queue = queue;
    this.delegate = null;
  }

  private TestLooperManagerCompat(TestLooperManager testLooperManager) {
    this.queue = null;
    this.delegate = testLooperManager;
  }

  static TestLooperManagerCompat acquire(Looper looper) {
    if (getApiLevel() >= BAKLAVA) {
      TestLooperManager testLooperManager = new TestLooperManager(looper);
      return new TestLooperManagerCompat(testLooperManager);
    } else {
      return new TestLooperManagerCompat(looper.getQueue());
    }
  }

  @Nullable
  Long peekWhen() {
    try {
      if (delegate != null) {
        return delegate.peekWhen();
      } else {
        Message msg = legacyPeek();
        if (msg != null && msg.getTarget() == null) {
          return null;
        }
        return msg == null ? null : msg.getWhen();
      }
    } catch (ReflectiveOperationException e) {
      throw new RuntimeException(e);
    }
  }

  @Nullable
  private Message legacyPeek() throws IllegalAccessException {
    // the legacy MessageQueue implementation synchronizes on itself,
    // so this uses the same lock
    LegacyMessageQueueReflector queueReflector =
        reflector(LegacyMessageQueueReflector.class, queue);
    synchronized (Objects.requireNonNull(queue)) {
      Message msg = queueReflector.getMessages();
      // Head is blocked on synchronization barrier, find next asynchronous message.
      if (msg != null && msg.getTarget() == null) {
        do {
          msg = shadowOfMsg(msg).internalGetNext();
        } while (msg != null && !msg.isAsynchronous());
      }
      return msg;
    }
  }

  Message poll() {
    if (delegate != null) {
      return delegate.poll();
    } else {
      return legacyPoll(queue);
    }
  }

  private Message legacyPoll(MessageQueue realQueue) {
    // fork of implementation from Baklava's MessageQueue#legacyPeekOrPoll
    LegacyMessageQueueReflector queueReflector =
        reflector(LegacyMessageQueueReflector.class, realQueue);
    synchronized (realQueue) {
      Message prevMsg = null;
      Message msg = queueReflector.getMessages();
      // Head is blocked on synchronization barrier, find next asynchronous message.
      if (msg != null && msg.getTarget() == null) {
        do {
          prevMsg = msg;
          msg = shadowOfMsg(msg).internalGetNext();
        } while (msg != null && !msg.isAsynchronous());
      }
      if (msg != null) {
        Message nextMsg = reflector(MessageReflector.class, msg).getNext();
        if (prevMsg != null) {
          reflector(MessageReflector.class, prevMsg).setNext(nextMsg);
          if (reflector(MessageReflector.class, prevMsg).getNext() == null
              && getApiLevel() >= VANILLA_ICE_CREAM) {
            queueReflector.setLast(prevMsg);
          }
        } else {
          queueReflector.setMessages(nextMsg);
          if (nextMsg == null && getApiLevel() >= VANILLA_ICE_CREAM) {
            queueReflector.setLast(null);
          }
        }
        if (msg.isAsynchronous() && getApiLevel() >= VANILLA_ICE_CREAM) {
          queueReflector.setAsyncMessageCount(queueReflector.getAsyncMessageCount() - 1);
        }
      }
      return msg;
    }
  }

  private static ShadowPausedMessage shadowOfMsg(Message msg) {
    return Shadow.extract(msg);
  }

  @Override
  public void close() {
    if (delegate != null) {
      delegate.release();
    }
  }

  @Nullable
  Long peekTailWhen() {
    if (getApiLevel() > BAKLAVA) {
      // TestLooperManager does not yet expose a public API for the last message's when, so fall
      // back to the hidden MessageQueue#peekLastMessageForTest() method.
      Message msg =
          reflector(LegacyMessageQueueReflector.class, getQueue()).peekLastMessageForTest();
      // DeliQueue's peekLastMessageForTest() does not filter out sync barriers (target == null).
      if (msg == null || msg.getTarget() == null) {
        return null;
      }
      return shadowOfMsg(msg).getWhen();
    } else {
      return legacyPeekTailWhen(getQueue());
    }
  }

  private MessageQueue getQueue() {
    if (delegate != null) {
      return delegate.getMessageQueue();
    } else {
      return queue;
    }
  }

  @Nullable
  private static Long legacyPeekTailWhen(MessageQueue queue) {
    LegacyMessageQueueReflector queueReflector =
        reflector(LegacyMessageQueueReflector.class, queue);
    Long lastWhen = null;

    synchronized (queue) {
      Message next = queueReflector.getMessages();
      while (next != null) {
        if (next.getTarget() != null) {
          long when = shadowOfMsg(next).getWhen();
          if (lastWhen == null || when > lastWhen) {
            lastWhen = when;
          }
        }
        next = shadowOfMsg(next).internalGetNext();
      }
    }
    return lastWhen;
  }

  @ForType(MessageQueue.class)
  private interface LegacyMessageQueueReflector {
    @Accessor("mMessages")
    Message getMessages();

    @Accessor("mLast")
    void setLast(Message msg);

    @Accessor("mAsyncMessageCount")
    int getAsyncMessageCount();

    @Accessor("mAsyncMessageCount")
    void setAsyncMessageCount(int asyncMessageCount);

    @Accessor("mBlocked")
    void setBlocked(boolean blocked);

    @Accessor("mMessages")
    void setMessages(Message nextMsg);

    // Only available on > Baklava; used until TestLooperManager exposes a public equivalent.
    @Direct
    Message peekLastMessageForTest();
  }
}
