package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.KITKAT_WATCH;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static org.robolectric.RuntimeEnvironment.getApiLevel;
import static org.robolectric.shadow.api.Shadow.directlyOn;
import static org.robolectric.util.ReflectionHelpers.ClassParameter.from;
import static org.robolectric.util.ReflectionHelpers.callInstanceMethod;
import static org.robolectric.util.ReflectionHelpers.getField;
import static org.robolectric.util.ReflectionHelpers.setField;

import android.os.Handler;
import android.os.Message;
import android.os.MessageQueue;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.HiddenApi;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.util.Logger;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.Scheduler;
import org.robolectric.util.TaskManager.Listener;

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
  private final List<Listener> listeners = new CopyOnWriteArrayList<>();

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
  public static boolean nativeIsIdling(int ptr) {
    return nativeIsIdling((long) ptr);
  }

  @Implementation(minSdk = LOLLIPOP)
  public static boolean nativeIsIdling(long ptr) {
    return false;
  }

  /**
   * @deprecated use ShadowApplication.get*Scheduler methods instead. Will be removed in a future Robolectric release
   */
  @Deprecated
  public Scheduler getScheduler() {
    return scheduler;
  }

  /**
   * @deprecated effectively unused. will be removed in a future Robolectric release
   */
  @Deprecated
  public void setScheduler(Scheduler scheduler) {
    this.scheduler = scheduler;
  }

  public Message getHead() {
    return getField(realQueue, "mMessages");
  }

  /**
   * @depecated will be removed in a future Robolectric release
   */
  @Deprecated
  public void setHead(Message msg) {
    setField(realQueue, "mMessages", msg);
  }

  public void reset() {
    setHead(null);
    setField(realQueue, "mIdleHandlers", new ArrayList<>());
    setField(realQueue, "mNextBarrierToken", 0);
  }

  @Implementation
  @SuppressWarnings("SynchronizeOnNonFinalField")
  public boolean enqueueMessage(final Message msg, long when) {
    final boolean retval = directlyOn(realQueue, MessageQueue.class, "enqueueMessage", from(Message.class, msg), from(long.class, when));
    if (retval) {
      notifyListeners();
    }
    return retval;
  }

  private static ShadowMessage shadowOf(Message actual) {
    return (ShadowMessage) Shadow.extract(actual);
  }

  void addListener(Listener listener) {
    listeners.add(listener);
  }

  private void notifyListeners() {
    for (Listener listener : listeners) {
      listener.newTaskPosted();
    }
  }

  Message peekNextMessage() {
    synchronized (realQueue) {
      Message msg = getHead();
      if (msg != null && msg.getTarget() == null) {
        // Stalled by a barrier.  Find the next asynchronous message in the queue.
        do {
          msg = shadowOf(msg).getNext();
        } while (msg != null && !msg.isAsynchronous());
      }
      return msg;
    }
  }

  Message getNextMessage() {
    synchronized (realQueue) {
      Message prevMsg = null;
      Message msg = getHead();
      if (msg != null && msg.getTarget() == null) {
        // Stalled by a barrier.  Find the next asynchronous message in the queue.
        do {
          prevMsg = msg;
          msg = shadowOf(msg).getNext();
        } while (msg != null && !msg.isAsynchronous());
      }
      if (msg != null) {
        // got a message, adjust queue
        if (prevMsg != null) {
          shadowOf(prevMsg).setNext(shadowOf(msg).getNext());
        } else {
          setHead(shadowOf(msg).getNext());
        }
        shadowOf(msg).setNext(null);
        shadowOf(msg).setMarkInUse();
      }
      return msg;
    }
  }

  void removeAll() {
    synchronized (realQueue) {
      if (RuntimeEnvironment.getApiLevel() > 17) {
        ReflectionHelpers.callInstanceMethod(realQueue, "removeAllMessagesLocked");
      } else {
        Message msg = getHead();
        while (msg != null) {
          Message next = shadowOf(msg).getNext();
          msg.recycle();
          msg = next;
        }
      }
    }
  }

  int size() {
    synchronized (realQueue) {
      int count = 0;
      Message msg = getHead();
      while (msg != null) {
        // ignore sync barriers
        if (msg.getTarget() != null) {
          count++;
        }
        msg = shadowOf(msg).getNext();
      }
      return count;
    }
  }
}
