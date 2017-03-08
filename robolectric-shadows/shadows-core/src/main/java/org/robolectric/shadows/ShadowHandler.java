package org.robolectric.shadows;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.util.ReflectionHelpers.ClassParameter;

import static org.robolectric.Shadows.shadowOf;
import static org.robolectric.shadow.api.Shadow.directlyOn;

/**
 * Shadow for Handler that puts posted {@link Runnable}s into a queue instead of sending them to be handled on a
 * separate thread.{@link Runnable}s that are scheduled to be executed immediately can be triggered by calling
 * {@link #idleMainLooper()}.
 * todo: add utility method to advance time and trigger execution of Runnables scheduled for a time in the future
 * 
 * Most of the special handling is all done by {@link ShadowLooper} and
 * {@link ShadowMessageQueue}.
 */
@Implements(Handler.class)
public class ShadowHandler {
  @RealObject
  private Handler realHandler;

  /**
   * @deprecated use {@link ShadowLooper#idleMainLooper()} instead
   */
  @Deprecated
  public static void flush() {
    idleMainLooper();
  }

  /**
   * @deprecated
   * @see org.robolectric.shadows.ShadowLooper#idleMainLooper()
   */
  @Deprecated
  public static void idleMainLooper() {
    ShadowLooper.idleMainLooper();
  }

  /**
   * @deprecated
   * @see ShadowLooper#runUiThreadTasksIncludingDelayedTasks()
   */
  @Deprecated
  public static void runMainLooperToEndOfTasks() {
    ShadowLooper.runUiThreadTasksIncludingDelayedTasks();
  }

  /**
   * @deprecated
   * @see ShadowLooper#runMainLooperOneTask() ()
   */
  @Deprecated
  public static void runMainLooperOneTask() {
    shadowOf(Looper.myLooper()).runOneTask();
  }

  /**
   * @deprecated
   * @see ShadowLooper#runMainLooperToNextTask() ()
   */
  @Deprecated
  public static void runMainLooperToNextTask() {
    shadowOf(Looper.myLooper()).runToNextTask();
  }

  @Implementation
  public final boolean sendMessageDelayed(Message msg, long delayMillis) {
    if (delayMillis < 0L) {
      delayMillis = 0L;
    }
    long time = delayMillis + shadowOf(realHandler.getLooper()).getScheduler().getCurrentTime();
    return directlyOn(realHandler, Handler.class, "sendMessageAtTime",
        ClassParameter.from(Message.class, msg),
        ClassParameter.from(long.class, time));
  }
}
