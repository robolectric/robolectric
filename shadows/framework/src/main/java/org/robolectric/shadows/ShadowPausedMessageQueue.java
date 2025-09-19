package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.BAKLAVA;
import static android.os.Build.VERSION_CODES.M;
import static android.os.Build.VERSION_CODES.VANILLA_ICE_CREAM;
import static com.google.common.base.Preconditions.checkState;
import static org.robolectric.RuntimeEnvironment.getApiLevel;
import static org.robolectric.shadows.ShadowPausedLooper.shadowMsg;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.os.Looper;
import android.os.Message;
import android.os.MessageQueue;
import android.os.MessageQueue.IdleHandler;
import android.os.SystemClock;
import java.time.Duration;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import javax.annotation.concurrent.GuardedBy;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.LooperMode;
import org.robolectric.annotation.RealObject;
import org.robolectric.res.android.NativeObjRegistry;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.shadows.ShadowMessage.MessageReflector;
import org.robolectric.util.Scheduler;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.Direct;
import org.robolectric.util.reflector.ForType;

/**
 * The shadow {@link} MessageQueue} for {@link LooperMode.Mode#PAUSED}
 *
 * <p>This class should not be referenced directly. Use {@link ShadowMessageQueue} instead.
 */
@SuppressWarnings("SynchronizeOnNonFinalField")
@Implements(value = MessageQueue.class, isInAndroidSdk = false)
public class ShadowPausedMessageQueue extends ShadowMessageQueue {

  @RealObject private MessageQueue realQueue;

  // just use this class as the native object
  private static final NativeObjRegistry<ShadowPausedMessageQueue> nativeQueueRegistry =
      new NativeObjRegistry<>(ShadowPausedMessageQueue.class);
  private boolean isPolling = false;
  private ShadowPausedSystemClock.Listener clockListener;
  private final AtomicReference<Exception> uncaughtExceptionRef = new AtomicReference<>(null);

  @GuardedBy("realQueue")
  private boolean pendingWake;

  private final AtomicReference<QueueListener> queueListenerRef = new AtomicReference<>(null);

  private interface QueueListener {

    /** Called when there is a new executable message available. */
    void onExecutableMsg();
  }

  // shadow constructor instead of nativeInit because nativeInit signature has changed across SDK
  // versions
  @Implementation
  protected void __constructor__(boolean quitAllowed) {
    reflector(MessageQueueReflector.class, realQueue).__constructor__(quitAllowed);
    long ptr = nativeQueueRegistry.register(this);
    reflector(MessageQueueReflector.class, realQueue).setPtr(ptr);
    clockListener =
        () -> {
          synchronized (realQueue) {
            if (!realQueue.isIdle()) {
              // only wake up the Looper thread if queue is non empty to reduce contention if many
              // Looper threads are active
              nativeWake(ptr);
            }
          }
          updateListener();
        };
    ShadowPausedSystemClock.addStaticListener(clockListener);
  }

  @Implementation
  protected static void nativeDestroy(long ptr) {
    ShadowPausedMessageQueue q = nativeQueueRegistry.unregister(ptr);
    ShadowPausedSystemClock.removeListener(q.clockListener);
  }

  @Implementation(minSdk = M)
  protected void nativePollOnce(long ptr, int timeoutMillis) {
    synchronized (realQueue) {
      isPolling = true;
      try {
        if (pendingWake) {
          // Calling with pending wake returns immediately
        } else if (timeoutMillis == 0) {
          // Calling epoll_wait() with 0 returns immediately
        } else {
          // ignore timeout since clock is not advancing. ClockListener will notify when clock
          // advances
          realQueue.wait();
        }
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
      isPolling = false;
      pendingWake = false;
    }
  }

  @Implementation
  protected static void nativeWake(long ptr) {
    MessageQueue realQueue = nativeQueueRegistry.getNativeObject(ptr).realQueue;
    ShadowPausedMessageQueue shadowPausedMessageQueue = Shadow.extract(realQueue);
    synchronized (shadowPausedMessageQueue.realQueue) {
      shadowPausedMessageQueue.pendingWake = true;
      realQueue.notifyAll();
    }
  }

  @Implementation(minSdk = M)
  protected static boolean nativeIsPolling(long ptr) {
    return nativeQueueRegistry.getNativeObject(ptr).isPolling;
  }

  /** Exposes the API23+_isIdle method to older platforms */
  @Implementation(minSdk = 23)
  public boolean isIdle() {
    return reflector(MessageQueueReflector.class, realQueue).isIdle();
  }

  @Implementation
  protected boolean enqueueMessage(Message msg, long when) {
    checkQueueState();
    boolean result = reflector(MessageQueueReflector.class, realQueue).enqueueMessage(msg, when);
    if (result) {
      updateListener();
    }
    return result;
  }

  void poll(long timeout) {
    checkState(
        Looper.myLooper() == Looper.getMainLooper() && Looper.myLooper().getQueue() == realQueue);
    // need to add a listener before checking executable messages, to avoid race condition where
    // message gets
    // posted immediately after checking state
    CountDownLatch latch = new CountDownLatch(1);
    queueListenerRef.set(latch::countDown);
    if (!hasExecutableMsg()) {
      try {
        if (timeout == 0) {
          latch.await();
        } else {
          latch.await(timeout, TimeUnit.MILLISECONDS);
        }
      } catch (InterruptedException e) {
        // Fall through and unblock with no messages.
      }
    }
    queueListenerRef.set(null);
  }

  private void updateListener() {
    QueueListener listener = queueListenerRef.get();
    // if listener is non null, we know poll() is blocking the Looper thread and its safe for us to
    // call
    // hasExecutableMsg
    if (listener != null && hasExecutableMsg()) {
      listener.onExecutableMsg();
    }
  }

  /**
   * Return true if there is an executable Message currently available for execution.
   *
   * <p>It is the caller's responsibility to ensure this does not race with the Looper thread
   * calling next. Thus it should only be called on the Looper thread, or when its known the Looper
   * thread is blocked (such as from poll())
   */
  private boolean hasExecutableMsg() {
    if (getApiLevel() > BAKLAVA) {
      Long when = reflector(MessageQueueReflector.class, realQueue).peekWhenForTest();
      return when != null && when <= SystemClock.uptimeMillis();
    } else {
      final long now = SystemClock.uptimeMillis();
      synchronized (realQueue) {
        Message msg = getMessages();
        if (msg != null && msg.getTarget() == null) {
          // Stalled by a barrier.  Find the next asynchronous message in the queue.
          do {
            msg = reflector(MessageReflector.class, msg).getNext();
          } while (msg != null && !msg.isAsynchronous());
        }
        return msg != null && shadowMsg(msg).getWhen() <= now;
      }
    }
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

  @Implementation
  protected void quit(boolean allowed) {
    reflector(MessageQueueReflector.class, realQueue).quit(allowed);
    ShadowPausedSystemClock.removeListener(clockListener);
  }

  @Implementation
  protected void removeSyncBarrier(int token) {
    reflector(MessageQueueReflector.class, realQueue).removeSyncBarrier(token);
    updateListener();
  }

  Duration getLastScheduledTaskTime() {
    if (getApiLevel() > BAKLAVA) {
      Message msg = reflector(MessageQueueReflector.class, realQueue).peekLastMessageForTest();
      if (msg == null) {
        return Duration.ZERO;
      }
      return Duration.ofMillis(convertWhenToScheduledTime(shadowOfMsg(msg).getWhen()));
    } else {
      long when = 0;
      synchronized (realQueue) {
        Message next = getMessages();
        if (next == null) {
          return Duration.ZERO;
        }
        while (next != null) {
          if (next.getTarget() != null) {
            when = shadowOfMsg(next).getWhen();
          }
          next = shadowOfMsg(next).internalGetNext();
        }
      }
      return Duration.ofMillis(convertWhenToScheduledTime(when));
    }
  }

  static long convertWhenToScheduledTime(long when) {
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
   * <p>Do not use, will likely be removed in a future release and not supported on SDKs > Baklava
   */
  public int internalGetSize() {
    // The following logic won't work on the new MessageQueue implementation used on SDKs >
    // Baklava.
    checkState(
        getApiLevel() <= BAKLAVA,
        "size() is not supported on SDKs > baklava. Consider using Handler.hasMessages or"
            + " hasCallbacks instead");
    int count = 0;
    synchronized (realQueue) {
      Message next = getMessages();
      while (next != null) {
        if (next.getTarget() != null) {
          count++;
        }
        next = shadowOfMsg(next).internalGetNext();
      }
    }
    return count;
  }

  // TODO: reconsider exposing this as a public API. Only ShadowPausedLooper needs to access this,
  // so it should be package private
  @Override
  public void reset() {
    MessageQueueReflector msgQueue = reflector(MessageQueueReflector.class, realQueue);
    setUncaughtException(null);
    if (getApiLevel() > BAKLAVA) {
      msgQueue.resetForTest();
    } else {
      synchronized (realQueue) {
        Message msg = getMessages();
        while (msg != null) {
          Message next = shadowOfMsg(msg).internalGetNext();
          shadowOfMsg(msg).recycleUnchecked();
          msg = next;
        }
        reflector(MessageQueueReflector.class, realQueue).setMessages(null);
        if (getApiLevel() >= VANILLA_ICE_CREAM) {
          reflector(MessageQueueReflector.class, realQueue).setLast(null);
          reflector(MessageQueueReflector.class, realQueue).setAsyncMessageCount(0);
        }

        msgQueue.setIdleHandlers(new ArrayList<>());
        msgQueue.setNextBarrierToken(0);
      }
    }
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

  /**
   * Called when an uncaught exception occurred in this message queue's Looper thread.
   *
   * <p>In real android, by default an exception handler is installed which kills the entire process
   * when an uncaught exception occurs. We don't want to do this in robolectric to isolate tests, so
   * instead an uncaught exception puts the message queue into an error state, where any future
   * interaction will rethrow the exception.
   */
  void setUncaughtException(Exception e) {
    this.uncaughtExceptionRef.set(e);
  }

  void checkQueueState() {
    Exception uncaughtException = uncaughtExceptionRef.get();
    if (uncaughtException != null) {
      throw new IllegalStateException(
          "Looper thread has died due to an uncaught exception", uncaughtException);
      }
  }

  /** Accessor interface for {@link MessageQueue}'s internals. */
  @ForType(MessageQueue.class)
  private interface MessageQueueReflector {
    @Direct
    void __constructor__(boolean quitAllowed);

    @Direct
    boolean enqueueMessage(Message msg, long when);

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

    @Accessor("mPtr")
    void setPtr(long ptr);

    @Direct
    void quit(boolean b);

    @Accessor("mLast")
    void setLast(Message msg);

    @Accessor("mAsyncMessageCount")
    void setAsyncMessageCount(int asyncMessageCount);

    @Direct
    boolean isIdle();

    // only available on > Baklava
    @Direct
    void resetForTest();

    @Direct
    Message peekLastMessageForTest();

    @Direct
    void removeSyncBarrier(int token);

    @Direct
    Long peekWhenForTest();
  }
}
