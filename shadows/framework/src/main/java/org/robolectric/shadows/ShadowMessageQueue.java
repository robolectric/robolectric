package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.KITKAT_WATCH;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static org.robolectric.RuntimeEnvironment.getApiLevel;
import static org.robolectric.Shadows.shadowOf;
import static org.robolectric.shadow.api.Shadow.directlyOn;
import static org.robolectric.util.ReflectionHelpers.ClassParameter.from;
import static org.robolectric.util.ReflectionHelpers.callInstanceMethod;
import static org.robolectric.util.ReflectionHelpers.getField;
import static org.robolectric.util.ReflectionHelpers.setField;

import android.os.Handler;
import android.os.Message;
import android.os.MessageQueue;
import org.robolectric.annotation.HiddenApi;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.util.Scheduler;

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
  public static Number nativeInit() {
    return 1;
  }

  @HiddenApi
  @Implementation(maxSdk = KITKAT_WATCH)
  public static void nativeDestroy(int ptr) {
    nativeDestroy((long) ptr);
  }

  @Implementation(minSdk = LOLLIPOP)
  public static void nativeDestroy(long ptr) {
  }

  @HiddenApi
  @Implementation(maxSdk = KITKAT_WATCH)
  public static void nativePollOnce(int ptr, int timeoutMillis) {
    nativePollOnce((long) ptr, timeoutMillis);
  }

  @Implementation(minSdk = LOLLIPOP)
  public static void nativePollOnce(long ptr, int timeoutMillis) {
    throw new AssertionError("Should not be called");
  }

  @HiddenApi
  @Implementation(maxSdk = KITKAT_WATCH)
  public static void nativeWake(int ptr) {
    nativeWake((long) ptr);
  }

  @Implementation(minSdk = LOLLIPOP)
  public static void nativeWake(long ptr) {
    throw new AssertionError("Should not be called");
  }

  @HiddenApi
  @Implementation(maxSdk = KITKAT_WATCH)
  public static boolean nativeIsIdling(int ptr) {
    return nativeIsIdling((long) ptr);
  }

  @Implementation(minSdk = LOLLIPOP)
  public static boolean nativeIsIdling(long ptr) {
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
    setField(realQueue, "mMessages", msg);
  }

  public void reset() {
    setHead(null);
  }

  @Implementation
  public boolean enqueueMessage(final Message msg, long when) {
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

  @HiddenApi
  @Implementation
  public void removeSyncBarrier(int token) {
  }

  private static void dispatchMessage(Message msg) {
    final Handler target = msg.getTarget();

    shadowOf(msg).setNext(null);
    // If target is null it means the message has been removed
    // from the queue prior to being dispatched by the scheduler.
    if (target != null) {
      callInstanceMethod(msg, "markInUse");
      target.dispatchMessage(msg);

      if (getApiLevel() >= LOLLIPOP) {
        callInstanceMethod(msg, "recycleUnchecked");
      } else {
        callInstanceMethod(msg, "recycle");
      }
    }
  }
}
