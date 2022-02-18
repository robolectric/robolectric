package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR1;
import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR2;
import static android.os.Build.VERSION_CODES.KITKAT;
import static android.os.Build.VERSION_CODES.KITKAT_WATCH;
import static android.os.Build.VERSION_CODES.LOLLIPOP_MR1;
import static android.os.Build.VERSION_CODES.M;
import static com.google.common.base.Preconditions.checkState;
import static org.robolectric.shadow.api.Shadow.invokeConstructor;
import static org.robolectric.util.ReflectionHelpers.ClassParameter.from;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.os.Looper;
import android.os.Message;
import android.os.MessageQueue;
import android.os.MessageQueue.IdleHandler;
import android.os.SystemClock;
import java.time.Duration;
import java.util.ArrayList;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.LooperMode;
import org.robolectric.annotation.RealObject;
import org.robolectric.res.android.NativeObjRegistry;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.Scheduler;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.Direct;
import org.robolectric.util.reflector.ForType;

/**
 * The shadow {@link} MessageQueue} for {@link LooperMode.Mode.PAUSED}
 *
 * <p>This class should not be referenced directly. Use {@link ShadowMessageQueue} instead.
 */
@SuppressWarnings("SynchronizeOnNonFinalField")
@Implements(value = MessageQueue.class, isInAndroidSdk = false, looseSignatures = true)
public class ShadowPausedMessageQueue extends ShadowMessageQueue {

  @RealObject private MessageQueue realQueue;

  // just use this class as the native object
  private static NativeObjRegistry<ShadowPausedMessageQueue> nativeQueueRegistry =
      new NativeObjRegistry<ShadowPausedMessageQueue>(ShadowPausedMessageQueue.class);
  private boolean isPolling = false;
  private ShadowPausedSystemClock.Listener clockListener;

  // shadow constructor instead of nativeInit because nativeInit signature has changed across SDK
  // versions
  @Implementation
  protected void __constructor__(boolean quitAllowed) {
    invokeConstructor(MessageQueue.class, realQueue, from(boolean.class, quitAllowed));
    int ptr = (int) nativeQueueRegistry.register(this);
    reflector(MessageQueueReflector.class, realQueue).setPtr(ptr);
    clockListener = () -> nativeWake(ptr);
    ShadowPausedSystemClock.addListener(clockListener);
  }

  @Implementation(maxSdk = JELLY_BEAN_MR1)
  protected void nativeDestroy() {
    nativeDestroy(reflector(MessageQueueReflector.class, realQueue).getPtr());
  }

  @Implementation(minSdk = JELLY_BEAN_MR2, maxSdk = KITKAT)
  protected static void nativeDestroy(int ptr) {
    nativeDestroy((long) ptr);
  }

  @Implementation(minSdk = KITKAT_WATCH)
  protected static void nativeDestroy(long ptr) {
    ShadowPausedMessageQueue q = nativeQueueRegistry.unregister(ptr);
    ShadowPausedSystemClock.removeListener(q.clockListener);
  }

  @Implementation(maxSdk = JELLY_BEAN_MR1)
  protected void nativePollOnce(int ptr, int timeoutMillis) {
    nativePollOnce((long) ptr, timeoutMillis);
  }

  // use the generic Object parameter types here, to avoid conflicts with the non-static
  // nativePollOnce
  @Implementation(minSdk = JELLY_BEAN_MR2, maxSdk = LOLLIPOP_MR1)
  protected static void nativePollOnce(Object ptr, Object timeoutMillis) {
    long ptrLong = getLong(ptr);
    nativeQueueRegistry.getNativeObject(ptrLong).nativePollOnce(ptrLong, (int) timeoutMillis);
  }

  @Implementation(minSdk = M)
  protected void nativePollOnce(long ptr, int timeoutMillis) {
    if (timeoutMillis == 0) {
      return;
    }
    synchronized (realQueue) {
      // only block if queue is empty
      // ignore timeout since clock is not advancing. ClockListener will notify when clock advances
      while (isIdle() && !isQuitting()) {
        isPolling = true;
        try {
          realQueue.wait();
        } catch (InterruptedException e) {
          // ignore
        }
      }
      isPolling = false;
    }
  }

  /**
   * Polls the message queue waiting until a message is posted to the head of the queue. This will
   * suspend the thread until a new message becomes available. Returns immediately if the queue is
   * not idle. There's no guarantee that the message queue will not still be idle when returning,
   * but if the message queue becomes not idle it will return immediately.
   *
   * <p>See {@link ShadowPausedLooper#poll(long)} for more information.
   *
   * @param timeout Timeout in milliseconds, the maximum time to wait before returning, or 0 to wait
   *     indefinitely,
   */
  void poll(long timeout) {
    checkState(Looper.myLooper() == Looper.getMainLooper() && Looper.myQueue() == realQueue);
    // Message queue typically expects the looper to loop calling next() which returns current
    // messages from the head of the queue. If no messages are current it will mark itself blocked
    // and call nativePollOnce (see above) which suspends the thread until the next message's time.
    // When messages are posted to the queue, if a new message is posted to the head and the queue
    // is marked as blocked, then the enqueue function will notify and resume next(), allowing it
    // return the next message. To simulate this behavior check if the queue is idle and if it is
    // mark the queue as blocked and wait on a new message.
    synchronized (realQueue) {
      if (isIdle()) {
        ReflectionHelpers.setField(realQueue, "mBlocked", true);
        try {
          realQueue.wait(timeout);
        } catch (InterruptedException ignored) {
          // Fall through and unblock with no messages.
        } finally {
          ReflectionHelpers.setField(realQueue, "mBlocked", false);
        }
      }
    }
  }

  @Implementation(maxSdk = JELLY_BEAN_MR1)
  protected void nativeWake(int ptr) {
    synchronized (realQueue) {
      realQueue.notifyAll();
    }
  }

  // use the generic Object parameter types here, to avoid conflicts with the non-static
  // nativeWake
  @Implementation(minSdk = JELLY_BEAN_MR2, maxSdk = KITKAT)
  protected static void nativeWake(Object ptr) {
    // JELLY_BEAN_MR2 has a bug where nativeWake can get called when pointer has already been
    // destroyed. See here where nativeWake is called outside the synchronized block
    // https://android.googlesource.com/platform/frameworks/base/+/refs/heads/jb-mr2-release/core/java/android/os/MessageQueue.java#239
    // So check to see if native object exists first
    ShadowPausedMessageQueue q = nativeQueueRegistry.peekNativeObject(getLong(ptr));
    if (q != null) {
      q.nativeWake(getInt(ptr));
    }
  }

  @Implementation(minSdk = KITKAT_WATCH)
  protected static void nativeWake(long ptr) {
    nativeQueueRegistry.getNativeObject(ptr).nativeWake((int) ptr);
  }

  @Implementation(minSdk = M)
  protected static boolean nativeIsPolling(long ptr) {
    return nativeQueueRegistry.getNativeObject(ptr).isPolling;
  }

  /** Exposes the API23+_isIdle method to older platforms */
  @Implementation(minSdk = 23)
  public boolean isIdle() {
    synchronized (realQueue) {
      Message msg = peekNextExecutableMessage();
      if (msg == null) {
          return true;
      }

      long now = SystemClock.uptimeMillis();
      long when = shadowOfMsg(msg).getWhen();
      return now < when;
    }
  }

  Message peekNextExecutableMessage() {
    MessageQueueReflector internalQueue = reflector(MessageQueueReflector.class, realQueue);
    Message msg = internalQueue.getMessages();

    if (msg != null && shadowOfMsg(msg).getTarget() == null) {
      // Stalled by a barrier.  Find the next asynchronous message in the queue.
      do {
        msg = shadowOfMsg(msg).internalGetNext();
      } while (msg != null && !msg.isAsynchronous());
    }

    return msg;
  }

  Message getNext() {
    return reflector(MessageQueueReflector.class, realQueue).next();
  }

  boolean isQuitAllowed() {
    return reflector(MessageQueueReflector.class, realQueue).getQuitAllowed();
  }

  void doEnqueueMessage(Message msg, long when) {
    reflector(MessageQueueReflector.class, realQueue).enqueueMessage(msg, when);
  }

  Message getMessages() {
    return reflector(MessageQueueReflector.class, realQueue).getMessages();
  }

  @Implementation(minSdk = M)
  protected boolean isPolling() {
    synchronized (realQueue) {
      return isPolling;
    }
  }

  @Implementation(maxSdk = JELLY_BEAN_MR1)
  protected void quit() {
    if (RuntimeEnvironment.getApiLevel() >= JELLY_BEAN_MR2) {
      reflector(MessageQueueReflector.class, realQueue).quit(false);
    } else {
      reflector(MessageQueueReflector.class, realQueue).quit();
    }
  }

  private boolean isQuitting() {
    if (RuntimeEnvironment.getApiLevel() >= KITKAT) {
      return reflector(MessageQueueReflector.class, realQueue).getQuitting();
    } else {
      return reflector(MessageQueueReflector.class, realQueue).getQuiting();
    }
  }

  private static long getLong(Object intOrLongObj) {
    if (intOrLongObj instanceof Long) {
      return (long) intOrLongObj;
    } else {
      Integer intObj = (Integer) intOrLongObj;
      return intObj.longValue();
    }
  }

  private static int getInt(Object intOrLongObj) {
    if (intOrLongObj instanceof Integer) {
      return (int) intOrLongObj;
    } else {
      Long longObj = (Long) intOrLongObj;
      return longObj.intValue();
    }
  }

  Duration getNextScheduledTaskTime() {
    Message next = peekNextExecutableMessage();

    if (next == null) {
      return Duration.ZERO;
    }
    return Duration.ofMillis(convertWhenToScheduledTime(shadowOfMsg(next).getWhen()));
  }

  Duration getLastScheduledTaskTime() {
    long when = 0;
    synchronized (realQueue) {
      Message next = getMessages();
      if (next == null) {
        return Duration.ZERO;
      }
      while (next != null) {
        when = shadowOfMsg(next).getWhen();
        next = shadowOfMsg(next).internalGetNext();
      }
    }
    return Duration.ofMillis(convertWhenToScheduledTime(when));
  }

  private static long convertWhenToScheduledTime(long when) {
    // in some situations, when can be 0 or less than uptimeMillis. Always floor it to at least
    // convertWhenToUptime
    if (when < SystemClock.uptimeMillis()) {
      when = SystemClock.uptimeMillis();
    }
    return when;
  }

  /**
   * Internal method to get the number of entries in the MessageQueue.
   *
   * <p>Do not use, will likely be removed in a future release.
   */
  public int internalGetSize() {
    int count = 0;
    synchronized (realQueue) {
      Message next = getMessages();
      while (next != null) {
        count++;
        next = shadowOfMsg(next).internalGetNext();
      }
    }
    return count;
  }

  /**
   * Returns the message at the head of the queue immediately, regardless of its scheduled time.
   * Compare to {@link #getNext()} which will only return the next message if the system clock is
   * advanced to its scheduled time.
   */
  Message getNextIgnoringWhen() {
    synchronized (realQueue) {
      Message head = getMessages();
      if (head != null) {
        Message next = shadowOfMsg(head).internalGetNext();
        reflector(MessageQueueReflector.class, realQueue).setMessages(next);
      }
      return head;
    }
  }

  // TODO: reconsider exposing this as a public API. Only ShadowPausedLooper needs to access this,
  // so it should be package private
  @Override
  public void reset() {
    MessageQueueReflector msgQueue = reflector(MessageQueueReflector.class, realQueue);
    msgQueue.setMessages(null);
    msgQueue.setIdleHandlers(new ArrayList<>());
    msgQueue.setNextBarrierToken(0);
  }

  private static ShadowPausedMessage shadowOfMsg(Message head) {
    return Shadow.extract(head);
  }

  @Override
  public Scheduler getScheduler() {
    throw new UnsupportedOperationException("Not supported in PAUSED LooperMode.");
  }

  @Override
  public void setScheduler(Scheduler scheduler) {
    throw new UnsupportedOperationException("Not supported in PAUSED LooperMode.");
  }

  // intentionally do not support direct access to MessageQueue internals

  @Override
  public Message getHead() {
    throw new UnsupportedOperationException("Not supported in PAUSED LooperMode.");
  }

  @Override
  public void setHead(Message msg) {
    throw new UnsupportedOperationException("Not supported in PAUSED LooperMode.");
  }

  /**
   * Retrieves a copy of the current list of idle handlers. Idle handlers are read with
   * synchronization on the real queue.
   */
  ArrayList<IdleHandler> getIdleHandlersCopy() {
    synchronized (realQueue) {
      return new ArrayList<>(reflector(MessageQueueReflector.class, realQueue).getIdleHandlers());
    }
  }

  /** Accessor interface for {@link MessageQueue}'s internals. */
  @ForType(MessageQueue.class)
  private interface MessageQueueReflector {

    boolean enqueueMessage(Message msg, long when);

    Message next();

    @Accessor("mMessages")
    void setMessages(Message msg);

    @Accessor("mMessages")
    Message getMessages();

    @Accessor("mIdleHandlers")
    void setIdleHandlers(ArrayList<IdleHandler> list);

    @Accessor("mIdleHandlers")
    ArrayList<IdleHandler> getIdleHandlers();

    @Accessor("mNextBarrierToken")
    void setNextBarrierToken(int token);

    @Accessor("mQuitAllowed")
    boolean getQuitAllowed();

    @Accessor("mPtr")
    void setPtr(int ptr);

    @Accessor("mPtr")
    int getPtr();

    // for APIs < JELLYBEAN_MR2
    @Direct
    void quit();

    @Direct
    void quit(boolean b);

    // for APIs < KITKAT
    @Accessor("mQuiting")
    boolean getQuiting();

    @Accessor("mQuitting")
    boolean getQuitting();
  }
}
