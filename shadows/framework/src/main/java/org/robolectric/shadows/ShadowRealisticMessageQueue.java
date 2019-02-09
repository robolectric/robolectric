package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.KITKAT_WATCH;
import static android.os.Build.VERSION_CODES.M;
import static org.robolectric.shadow.api.Shadow.directlyOn;
import static org.robolectric.util.ReflectionHelpers.setField;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.os.Build;
import android.os.Message;
import android.os.MessageQueue;
import android.os.SystemClock;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.res.android.NativeObjRegistry;
import org.robolectric.shadow.api.Shadow;
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

  public void reset() {
    _MessageQueue_ internalQueue = reflector(_MessageQueue_.class, realQueue);
    internalQueue.setIdleHandlers(new ArrayList<>());
    internalQueue.setNextBarrierToken(0);
    internalQueue.setMessages(null);
  }

  /**
   *
   * Exposes the API23+_isIdle method to older platforms
   */
  @Implementation(minSdk = 23)
  public boolean isIdle() {
    if (Build.VERSION.SDK_INT >= M) {
      return directlyOn( realQueue, MessageQueue.class).isIdle();
    } else {
      _MessageQueue_ internalQueue = reflector(_MessageQueue_.class, realQueue);
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

  Message getNext() {
    return reflector(_MessageQueue_.class, realQueue).next();
  }

  /** Accessor interface for {@link MessageQueue}'s internals. */
  @ForType(MessageQueue.class)
  interface _MessageQueue_ {

    void enqueueMessage(Message msg, long when);

    Message next();

    void quit(boolean safe);

    void quit();

    @Accessor("mQuitAllowed")
    void setQuitAllowed(boolean val);

    @Accessor("mMessages")
    void setMessages(Message msg);

    @Accessor("mMessages")
    Message getMessages();

    @Accessor("mIdleHandlers")
    void setIdleHandlers(ArrayList<Object> objects);

    @Accessor("mNextBarrierToken")
    void setNextBarrierToken(int token);
  }

  /**
   * A simplistic implementation of the native code backing a MessageQueue.
   *
   * <p>Currently only supports the signaling aspect aka nativeWake and nativePoll
   */
  private static class NativeQueue {

    private boolean isPolling = false;

    public synchronized void pollOnce(int timeoutMillis) {
      isPolling = true;
      try {
        if (timeoutMillis > 0) {
          wait(timeoutMillis);
        } else if (timeoutMillis < 0) {
          wait();
        }
      } catch (InterruptedException e) {
        // ignore
      }
      isPolling = false;
    }

    public synchronized void wake() {
      notifyAll();
    }

    public synchronized boolean isPolling() {
      return isPolling;
    }
  }

}
