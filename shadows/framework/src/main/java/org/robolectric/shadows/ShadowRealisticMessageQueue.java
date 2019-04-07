package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR1;
import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR2;
import static android.os.Build.VERSION_CODES.KITKAT;
import static android.os.Build.VERSION_CODES.KITKAT_WATCH;
import static android.os.Build.VERSION_CODES.LOLLIPOP_MR1;
import static android.os.Build.VERSION_CODES.M;
import static org.robolectric.shadow.api.Shadow.directlyOn;
import static org.robolectric.shadow.api.Shadow.invokeConstructor;
import static org.robolectric.util.ReflectionHelpers.ClassParameter.from;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.os.Build;
import android.os.Message;
import android.os.MessageQueue;
import android.os.MessageQueue.IdleHandler;
import android.os.SystemClock;
import androidx.test.annotation.Beta;
import java.time.Duration;
import java.util.ArrayList;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.res.android.NativeObjRegistry;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.ForType;

/**
 *  * A new variant of a MessageQueue shadow that is active when {@link
 *  * ShadowBaseLooper#useRealisticLooper()} is enabled.
 *
 * This is beta API, and will very likely be renamed in a future Robolectric release.
 */
@Implements(
    value = MessageQueue.class,
    shadowPicker = ShadowBaseMessageQueue.Picker.class,
    // TODO: turn off shadowOf generation. Figure out why this is needed
    isInAndroidSdk = false,
    looseSignatures = true)
@Beta
public class ShadowRealisticMessageQueue extends ShadowBaseMessageQueue {

  @RealObject private MessageQueue realQueue;

  // just use this class as the native object
  private static NativeObjRegistry<ShadowRealisticMessageQueue> nativeQueueRegistry =
      new NativeObjRegistry<ShadowRealisticMessageQueue>(ShadowRealisticMessageQueue.class);
  private boolean isPolling = false;
  private ShadowPausedSystemClock.Listener clockListener;

  // shadow constructor instead of nativeInit because nativeInit signature has changed across SDK
  // versions
  @Implementation
  protected void __constructor__(boolean quitAllowed) {
    invokeConstructor(MessageQueue.class, realQueue, from(boolean.class, quitAllowed));
    int ptr = (int) nativeQueueRegistry.register(this);
    reflector(ReflectorMessageQueue.class, realQueue).setPtr(ptr);
    clockListener =
        newCurrentTimeMillis -> nativeWake(ptr);
    ShadowPausedSystemClock.addListener(clockListener);
  }

  @Implementation(maxSdk = JELLY_BEAN_MR1)
  protected void nativeDestroy() {
    nativeDestroy(reflector(ReflectorMessageQueue.class, realQueue).getPtr());
  }

  @Implementation(minSdk = JELLY_BEAN_MR2, maxSdk = KITKAT)
  protected static void nativeDestroy(int ptr) {
    nativeDestroy((long) ptr);
  }

  @Implementation(minSdk = KITKAT_WATCH)
  protected static void nativeDestroy(long ptr) {
    ShadowRealisticMessageQueue q = nativeQueueRegistry.unregister(ptr);
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
    nativeQueueRegistry.getNativeObject(getLong(ptr)).nativeWake(getInt(ptr));
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
    if (Build.VERSION.SDK_INT >= M) {
      return directlyOn(realQueue, MessageQueue.class).isIdle();
    } else {
      ReflectorMessageQueue internalQueue = reflector(ReflectorMessageQueue.class, realQueue);
      // this is a copy of the implementation from P
      synchronized (realQueue) {
        final long now = SystemClock.uptimeMillis();
        Message headMsg = internalQueue.getMessages();
        if (headMsg == null) {
          return true;
        }
        long when = shadowMsg(headMsg).getWhen();
        return now < when;
      }
    }
  }

  Message getNext() {
    return reflector(ReflectorMessageQueue.class, realQueue).next();
  }

  void reset() {
    ReflectorMessageQueue msgQueue = reflector(ReflectorMessageQueue.class, realQueue);
    msgQueue.setMessages(null);
    msgQueue.setIdleHandlers(new ArrayList<>());
    msgQueue.setNextBarrierToken(0);
  }

  boolean isQuitAllowed() {
    return reflector(ReflectorMessageQueue.class, realQueue).getQuitAllowed();
  }

  void doEnqueueMessage(Message msg, long when) {
    reflector(ReflectorMessageQueue.class, realQueue).enqueueMessage(msg, when);
  }

  Message getMessages() {
    return reflector(ReflectorMessageQueue.class, realQueue).getMessages();
  }

  boolean isPolling() {
    synchronized (realQueue) {
      return isPolling;
    }
  }

  void quit() {
    if (RuntimeEnvironment.getApiLevel() >= JELLY_BEAN_MR2) {
      reflector(ReflectorMessageQueue.class, realQueue).quit(false);
    } else {
      reflector(ReflectorMessageQueue.class, realQueue).quit();
    }
  }

  private boolean isQuitting() {
    if (RuntimeEnvironment.getApiLevel() >= KITKAT) {
      return reflector(ReflectorMessageQueue.class, realQueue).getQuitting();
    } else {
      return reflector(ReflectorMessageQueue.class, realQueue).getQuiting();
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
    Message head = getMessages();
    if (head == null) {
      return Duration.ZERO;
    }
    return Duration.ofMillis(shadowMsg(head).getWhen());
  }

  Duration getLastScheduledTaskTime() {
    long when = 0;
    synchronized (realQueue) {
      Message next = getMessages();
      while (next != null) {
        when = shadowMsg(next).getWhen();
        next = shadowMsg(next).getNext();
      }
    }
    return Duration.ofMillis(when);
  }

  private static ShadowRealisticMessage shadowMsg(Message head) {
    return Shadow.extract(head);
  }

  Message poll() {
    synchronized (realQueue) {
      Message head = getMessages();
      if (head != null) {
        Message next = shadowMsg(head).getNext();
        reflector(ReflectorMessageQueue.class, realQueue).setMessages(next);
      }
      return head;
    }
  }

  /** Accessor interface for {@link MessageQueue}'s internals. */
  @ForType(MessageQueue.class)
  private interface ReflectorMessageQueue {

    void enqueueMessage(Message msg, long when);

    Message next();

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

    @Accessor("mPtr")
    void setPtr(int ptr);

    @Accessor("mPtr")
    int getPtr();

    // for APIs < JELLYBEAN_MR2
    void quit();

    void quit(boolean b);

    // for APIs < KITKAT
    @Accessor("mQuiting")
    boolean getQuiting();

    @Accessor("mQuitting")
    boolean getQuitting();
  }
}
