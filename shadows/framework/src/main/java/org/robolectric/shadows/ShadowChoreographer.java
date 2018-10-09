package org.robolectric.shadows;

import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.view.Choreographer;
import android.view.Choreographer.FrameCallback;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.util.SoftThreadLocal;
import org.robolectric.util.TimeUtils;

/**
 * Robolectric maintains its own concept of the current time from the Choreographer's
 * point of view, aimed at making animations work correctly. Time starts out at {@code 0}
 * and advances by {@code frameInterval} every time
 * {@link Choreographer#getFrameTimeNanos()} is called.
 */
@Implements(Choreographer.class)
public class ShadowChoreographer {
  private long nanoTime = 0;
  private static long FRAME_INTERVAL = 10 * TimeUtils.NANOS_PER_MS; // 10ms
  private static final Thread MAIN_THREAD = Thread.currentThread();
  private static SoftThreadLocal<Choreographer> instance = makeThreadLocal();
  private Handler handler = new Handler(Looper.myLooper());
  private static volatile int postCallbackDelayMillis = 0;
  private static volatile int postFrameCallbackDelayMillis = 0;

  private static SoftThreadLocal<Choreographer> makeThreadLocal() {
    return new SoftThreadLocal<Choreographer>() {
      @Override
      protected Choreographer create() {
        Looper looper = Looper.myLooper();
        if (looper == null) {
          throw new IllegalStateException("The current thread must have a looper!");
        }

        // Choreographer's constructor changes somewhere in Android O...
        try {
          Choreographer.class.getDeclaredConstructor(Looper.class);
          return Shadow.newInstance(Choreographer.class, new Class[]{Looper.class}, new Object[]{looper});
        } catch (NoSuchMethodException e) {
          return Shadow.newInstance(Choreographer.class, new Class[]{Looper.class, int.class}, new Object[]{looper, 0});
        }
      }
    };
  }

  /**
   * Allows application to specify a fixed amount of delay when {@link #postCallback(int, Runnable,
   * Object)} is invoked. The default delay value is `0`. This can be used to avoid infinite
   * animation tasks to be spawned when the Robolectric {@link org.robolectric.util.Scheduler} is in
   * {@link org.robolectric.util.Scheduler.IdleState#PAUSED} mode.
   */
  public static void setPostCallbackDelay(int delayMillis) {
    postCallbackDelayMillis = delayMillis;
  }

  /**
   * Allows application to specify a fixed amount of delay when {@link
   * #postFrameCallback(FrameCallback)} is invoked. The default delay value is `0`. This can be used
   * to avoid infinite animation tasks to be spawned when the Robolectric {@link
   * org.robolectric.util.Scheduler} is in {@link org.robolectric.util.Scheduler.IdleState#PAUSED}
   * mode.
   */
  public static void setPostFrameCallbackDelay(int delayMillis) {
    postFrameCallbackDelayMillis = delayMillis;
  }

  @Implementation
  protected static Choreographer getInstance() {
    return instance.get();
  }

  /**
   * The default implementation will call {@link #postCallbackDelayed(int, Runnable, Object, long)}
   * with no delay. {@link android.animation.AnimationHandler} calls this method to schedule
   * animation updates infinitely. Because during a Robolectric test the system time is paused and
   * execution of the event loop is invoked for each test instruction, the behavior of
   * AnimationHandler would result in endless looping (the execution of the task results in a new
   * animation task created and scheduled to the front of the event loop queue).
   *
   * <p>To prevent endless looping, a test may call {@link #setPostCallbackDelay(int)} to specify a
   * small delay when animation is scheduled.
   *
   * @see #setPostCallbackDelay(int)
   */
  @Implementation
  protected void postCallback(int callbackType, Runnable action, Object token) {
    postCallbackDelayed(callbackType, action, token, postCallbackDelayMillis);
  }

  @Implementation
  protected void postCallbackDelayed(
      int callbackType, Runnable action, Object token, long delayMillis) {
    handler.postDelayed(action, delayMillis);
  }

  @Implementation
  protected void removeCallbacks(int callbackType, Runnable action, Object token) {
    handler.removeCallbacks(action, token);
  }

  /**
   * The default implementation will call {@link #postFrameCallbackDelayed(FrameCallback, long)}
   * with no delay. {@link android.animation.AnimationHandler} calls this method to schedule
   * animation updates infinitely. Because during a Robolectric test the system time is paused and
   * execution of the event loop is invoked for each test instruction, the behavior of
   * AnimationHandler would result in endless looping (the execution of the task results in a new
   * animation task created and scheduled to the front of the event loop queue).
   *
   * <p>To prevent endless looping, a test may call {@link #setPostFrameCallbackDelay(int)} to
   * specify a small delay when animation is scheduled.
   *
   * @see #setPostCallbackDelay(int)
   */
  @Implementation
  protected void postFrameCallback(final FrameCallback callback) {
    postFrameCallbackDelayed(callback, postFrameCallbackDelayMillis);
  }

  @Implementation
  protected void postFrameCallbackDelayed(final FrameCallback callback, long delayMillis) {
    handler.postAtTime(new Runnable() {
      @Override public void run() {
        callback.doFrame(getFrameTimeNanos());
      }
    }, callback, SystemClock.uptimeMillis() + delayMillis);
  }

  @Implementation
  protected void removeFrameCallback(FrameCallback callback) {
    handler.removeCallbacksAndMessages(callback);
  }

  @Implementation
  protected long getFrameTimeNanos() {
    final long now = nanoTime;
    nanoTime += ShadowChoreographer.FRAME_INTERVAL;
    return now;
  }

  /**
   * Return the current inter-frame interval.
   *
   * @return  Inter-frame interval.
   */
  public static long getFrameInterval() {
    return ShadowChoreographer.FRAME_INTERVAL;
  }

  /**
   * Set the inter-frame interval used to advance the clock. By default, this is set to 1ms.
   *
   * @param frameInterval  Inter-frame interval.
   */
  public static void setFrameInterval(long frameInterval) {
    ShadowChoreographer.FRAME_INTERVAL = frameInterval;
  }

  @Resetter
  public static synchronized void reset() {
    // Blech. We need to share the main looper because somebody might refer to it in a static
    // field. We also need to keep it in a soft reference so we don't max out permgen.
    if (Thread.currentThread() != MAIN_THREAD) {
      throw new RuntimeException("You should only call this from the main thread!");
    }
    instance = makeThreadLocal();
    FRAME_INTERVAL = 10 * TimeUtils.NANOS_PER_MS; // 10ms
  }
}

