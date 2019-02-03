package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.KITKAT_WATCH;
import static android.os.Build.VERSION_CODES.M;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.os.Message;
import android.os.MessageQueue;
import java.util.ArrayList;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.annotation.Resetter;
import org.robolectric.res.android.NativeObjRegistry;
import org.robolectric.util.reflector.Accessor;
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

  public void reset() {
    reflector(_MessageQueue_.class, realQueue).setQuitAllowed(true);
    reflector(_MessageQueue_.class, realQueue).quit();
    reflector(_MessageQueue_.class, realQueue).setMessages(null);
  }

  /** Accessor interface for {@link MessageQueue}'s internals. */
  @ForType(MessageQueue.class)
  interface _MessageQueue_ {

    void enqueueMessage(Message msg, long when);

    Message next();

    void quit();

    @Accessor("mQuitAllowed")
    void setQuitAllowed(boolean val);

    @Accessor("mMessages")
    void setMessages(Message msg);
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
