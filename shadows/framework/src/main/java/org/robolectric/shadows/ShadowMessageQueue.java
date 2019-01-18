package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR2;
import static android.os.Build.VERSION_CODES.KITKAT;
import static android.os.Build.VERSION_CODES.KITKAT_WATCH;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.LOLLIPOP_MR1;
import static org.robolectric.RuntimeEnvironment.getApiLevel;
import static org.robolectric.shadow.api.Shadow.directlyOn;
import static org.robolectric.util.ReflectionHelpers.ClassParameter.from;
import static org.robolectric.util.ReflectionHelpers.getField;
import static org.robolectric.util.ReflectionHelpers.setField;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.os.Handler;
import android.os.Message;
import android.os.MessageQueue;
import java.util.ArrayList;
import org.robolectric.annotation.HiddenApi;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.shadows.ShadowMessage._Message_;
import org.robolectric.util.Logger;
import org.robolectric.util.Scheduler;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.ForType;

/**
 * Robolectric puts {@link android.os.Message}s into the scheduler queue instead of sending
 * them to be handled on a separate thread. {@link android.os.Message}s that are scheduled to
 * be dispatched can be triggered by calling {@link ShadowLooper#idleMainLooper}.
 *
 * @see ShadowLooper
 */
@Implements(MessageQueue.class)
public class ShadowMessageQueue {

  @RealObject
  private MessageQueue realQueue;

  private Scheduler scheduler;

  // Stub out the native peer - scheduling
  // is handled by the Scheduler class which is user-driven
  // rather than automatic.
  @HiddenApi
  @Implementation
  @SuppressWarnings("robolectric.ShadowReturnTypeMismatch")
  public static Number nativeInit() {
    return 1;
  }

  @HiddenApi
  @Implementation(minSdk = JELLY_BEAN_MR2, maxSdk = KITKAT_WATCH)
  public static void nativeDestroy(int ptr) {
    nativeDestroy((long) ptr);
  }

  @Implementation(minSdk = LOLLIPOP)
  protected static void nativeDestroy(long ptr) {}

  @HiddenApi
  @Implementation(minSdk = KITKAT, maxSdk = KITKAT_WATCH)
  public static boolean nativeIsIdling(int ptr) {
    return nativeIsIdling((long) ptr);
  }

  @Implementation(minSdk = LOLLIPOP, maxSdk = LOLLIPOP_MR1)
  protected static boolean nativeIsIdling(long ptr) {
    return false;
  }

  public Scheduler getScheduler() {
    return scheduler;
  }

  public void setScheduler(Scheduler scheduler) {
    this.scheduler = scheduler;
  }

  public Message getHead() {
    return getField(realQueue, "mMessages");
  }

  public void setHead(Message msg) {
    reflector(_MessageQueue_.class, realQueue).setMessages(msg);
  }

  public void reset() {
    setHead(null);
    setField(realQueue, "mIdleHandlers", new ArrayList<>());
    setField(realQueue, "mNextBarrierToken", 0);
  }

  @Implementation
  @SuppressWarnings("SynchronizeOnNonFinalField")
  protected boolean enqueueMessage(final Message msg, long when) {
    final boolean retval = directlyOn(realQueue, MessageQueue.class, "enqueueMessage", from(Message.class, msg), from(long.class, when));
    if (retval) {
      final Runnable callback = new Runnable() {
        @Override
        public void run() {
          synchronized (realQueue) {
            Message m = getHead();
            if (m == null) {
              return;
            }

            Message n = shadowOf(m).getNext();
            if (m == msg) {
              setHead(n);
            } else {
              while (n != null) {
                if (n == msg) {
                  n = shadowOf(n).getNext();
                  shadowOf(m).setNext(n);
                  break;
                }
                m = n;
                n = shadowOf(m).getNext();
              }
            }
          }
          dispatchMessage(msg);
        }
      };
      shadowOf(msg).setScheduledRunnable(callback);
      if (when == 0) {
        scheduler.postAtFrontOfQueue(callback);
      } else {
        scheduler.postDelayed(callback, when - scheduler.getCurrentTime());
      }
    }
    return retval;
  }

  private static void dispatchMessage(Message msg) {
    final Handler target = msg.getTarget();

    shadowOf(msg).setNext(null);
    // If target is null it means the message has been removed
    // from the queue prior to being dispatched by the scheduler.
    if (target != null) {
      _Message_ msgProxy = reflector(_Message_.class, msg);
      msgProxy.markInUse();
      target.dispatchMessage(msg);

      if (getApiLevel() >= LOLLIPOP) {
        msgProxy.recycleUnchecked();
      } else {
        msgProxy.recycle();
      }
    }
  }

  @Implementation
  @HiddenApi
  protected void removeSyncBarrier(int token) {
    // TODO(b/74402484): workaround scheduler corruption of message queue
    try {
      directlyOn(realQueue, MessageQueue.class, "removeSyncBarrier", from(int.class, token));
    } catch (IllegalStateException e) {
      Logger.warn("removeSyncBarrier failed! Could not find token %d", token);
    }
  }

  private static ShadowMessage shadowOf(Message actual) {
    return (ShadowMessage) Shadow.extract(actual);
  }

  /** Accessor interface for {@link MessageQueue}'s internals. */
  @ForType(MessageQueue.class)
  interface _MessageQueue_ {

    @Accessor("mMessages")
    void setMessages(Message msg);
  }
}
