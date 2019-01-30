package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.KITKAT_WATCH;
import static android.os.Build.VERSION_CODES.M;

import android.os.Message;
import android.os.MessageQueue;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.res.android.NativeObjRegistry;
import org.robolectric.util.reflector.ForType;

@Implements(
    value = MessageQueue.class,
    shadowPicker = ShadowBaseMessageQueue.Picker.class,
    // TODO: turn off shadowOf generation. Figure out why this is needed
    isInAndroidSdk = false)
public class ShadowSimplifiedMessageQueue extends ShadowBaseMessageQueue {

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

  /** Accessor interface for {@link MessageQueue}'s internals. */
  @ForType(MessageQueue.class)
  interface _MessageQueue_ {

    void enqueueMessage(Message msg, long when);

    Message next();
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
