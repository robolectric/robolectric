package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.KITKAT_WATCH;
import static android.os.Build.VERSION_CODES.M;
import static org.robolectric.shadow.api.Shadow.directlyOn;
import static org.robolectric.util.ReflectionHelpers.setField;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.os.Build;
import android.os.Message;
import android.os.MessageQueue;
import android.os.MessageQueue.IdleHandler;
import android.os.SystemClock;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.res.android.NativeObjRegistry;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.util.ReflectionHelpers.ClassParameter;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.ForType;

import java.util.ArrayList;

@Implements(
    value = MessageQueue.class,
    shadowPicker = ShadowBaseMessageQueue.Picker.class,
    // TODO: turn off shadowOf generation. Figure out why this is needed
    isInAndroidSdk = false)
public class ShadowRealisticMessageQueue extends ShadowBaseMessageQueue {

  @RealObject private MessageQueue realQueue;

  private static NativeObjRegistry<NativeQueue> nativeQueueRegistry =
      new NativeObjRegistry<NativeQueue>(NativeQueue.class);

  private boolean wasReset = false;

  @Implementation(minSdk = KITKAT_WATCH)
  protected static long nativeInit() {
    return nativeQueueRegistry.register(new NativeQueue());
  }

  @Implementation(minSdk = KITKAT_WATCH)
  protected static void nativeDestroy(long ptr) {
    nativeQueueRegistry.unregister(ptr);
  }

  @Implementation(minSdk = M)
  protected void nativePollOnce(long ptr, int timeoutMillis) {
    nativeQueueRegistry.getNativeObject(ptr).pollOnce(timeoutMillis);
  }

  @Implementation(minSdk = KITKAT_WATCH)
  protected static void nativeWake(long ptr) {
    nativeQueueRegistry.getNativeObject(ptr).wake();
  }

  @Implementation(minSdk = M)
  protected static boolean nativeIsPolling(long ptr) {
    return nativeQueueRegistry.getNativeObject(ptr).isPolling();
  }

  /**
   *
   * Exposes the API23+_isIdle method to older platforms
   */
  @Implementation(minSdk = 23)
  public boolean isIdle() {
    checkWasReset();
    if (Build.VERSION.SDK_INT >= M) {
      return directlyOn( realQueue, MessageQueue.class).isIdle();
    } else {
      ReflectorMessageQueue internalQueue = reflector(ReflectorMessageQueue.class, realQueue);
      // this is a copy of the implementation from P
      synchronized (realQueue) {
        final long now = SystemClock.uptimeMillis();
        Message headMsg = internalQueue.getMessages();
        if (headMsg == null) {
          return true;
        }
        ShadowRealisticMessage shadowMsg = Shadow.extract(headMsg);
        long when = shadowMsg.getWhen();
        return now < when;
      }
    }
  }

  private void checkWasReset() {
    Preconditions.checkState(!wasReset, "This MessageQueue reference has leaked between test " +
        "runs! Ensure your code is not holding onto static Looper and MessageQueue references " +
        "between tests.");
  }

  @Implementation
  protected boolean enqueueMessage(Message msg, long when) {
    checkWasReset();
    return directlyOn(realQueue, MessageQueue.class, "enqueueMessage", ClassParameter.from(Message.class, msg),
        ClassParameter.from(long.class, when));
  }

  Message getNext() {
    return reflector(ReflectorMessageQueue.class, realQueue).next();
  }

  void setQuitAllowed(boolean b) {
    reflector(ReflectorMessageQueue.class, realQueue).setQuitAllowed(b);
  }

  /**
   * Indicate that this messageQueue instance was reset at test teardown, and should no longer be
   * used.
   */
  void setReset(boolean isReset) {
    this.wasReset = isReset;
  }

  public void reset() {
    ReflectorMessageQueue msgQueue = reflector(ReflectorMessageQueue.class, realQueue);
    msgQueue.setMessages(null);
    msgQueue.setIdleHandlers(new ArrayList<>());
    msgQueue.setNextBarrierToken(0);
  }

  boolean isQuitAllowed() {
    return reflector(ReflectorMessageQueue.class, realQueue).getQuitAllowed();
  }

  /** Accessor interface for {@link MessageQueue}'s internals. */
  @ForType(MessageQueue.class)
  interface ReflectorMessageQueue {

    void enqueueMessage(Message msg, long when);

    Message next();

    @Accessor("mQuitAllowed")
    void setQuitAllowed(boolean val);

    @Accessor("mMessages")
    void setMessages(Message msg);

    @Accessor("mMessages")
    Message getMessages();

    @Accessor("mIdleHandlers")
    void setIdleHandlers(ArrayList<IdleHandler> list);

    @Accessor("mNextBarrierToken")
    void setNextBarrierToken(int token);

    @Accessor("mQuitAllowed")
    boolean getQuitAllowed();
  }

  /**
   * A simplistic implementation of the native code backing a MessageQueue.
   *
   * <p>Currently only supports the signaling aspect aka nativeWake and nativePoll
   */
  private static class NativeQueue {

    private static final int MAX_TIMEOUT_MS = 100;

    private Semaphore shouldWait = new Semaphore(0);
    private boolean isPolling;

    public synchronized void pollOnce(int timeoutMillis) {
      try {
        isPolling = true;
        try {
          if (timeoutMillis > 0) {
            shouldWait.tryAcquire(timeoutMillis, TimeUnit.MILLISECONDS);
          } else if (timeoutMillis < 0) {
            // TODO: this is lame, try to find a solution that doesn't involve polling
            shouldWait.tryAcquire(MAX_TIMEOUT_MS, TimeUnit.MILLISECONDS);
          }
        } catch (InterruptedException e) {
          // ignore
        }
      } finally {
        isPolling = false;
      }
    }

    public synchronized void wake() {
      shouldWait.release();
    }

    public synchronized boolean isPolling() {
      return isPolling;
    }
  }
}
