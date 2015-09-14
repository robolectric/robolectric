package org.robolectric.shadows;

import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.view.Choreographer;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;
import org.robolectric.internal.Shadow;
import org.robolectric.util.SoftThreadLocal;
import org.robolectric.util.TimeUtils;

/**
 * Shadow for {@link android.view.Choreographer}.
 *
 * <p>This class maintains its own concept of the current time aimed at making animations
 * work correctly. Time starts out at 0 and advances by "frameInterval" every time
 * {@link android.view.Choreographer#getFrameTimeNanos} is called.</p>
 */
@Implements(Choreographer.class)
public class ShadowChoreographer {
  private long nanoTime = 0;
  private static long FRAME_INTERVAL = 10 * TimeUtils.NANOS_PER_MS; // 10ms
  private static final Thread MAIN_THREAD = Thread.currentThread();
  private static SoftThreadLocal<Choreographer> instance = makeThreadLocal();
  private Handler handler = new Handler(Looper.myLooper());

  private static SoftThreadLocal<Choreographer> makeThreadLocal() {
    return new SoftThreadLocal<Choreographer>() {
      @Override
      protected Choreographer create() {
        Looper looper = Looper.myLooper();
        if (looper == null) {
          throw new IllegalStateException("The current thread must have a looper!");
        }
        return Shadow.newInstance(Choreographer.class, new Class[]{Looper.class}, new Object[]{looper});
      }
    };
  }

  @Implementation
  public static Choreographer getInstance() {
    return instance.get();
  }

  @Implementation
  public void postCallbackDelayed(int callbackType, Runnable action, Object token, long delayMillis) {
    handler.postDelayed(action, delayMillis);
  }

  @Implementation
  public void removeCallbacks(int callbackType, Runnable action, Object token) {
    handler.removeCallbacks(action, token);
  }

  @Implementation
  public void postFrameCallbackDelayed(final Choreographer.FrameCallback callback, long delayMillis) {
    handler.postAtTime(new Runnable() {
      @Override public void run() {
        callback.doFrame(getFrameTimeNanos());
      }
    }, callback, SystemClock.uptimeMillis() + delayMillis);
  }

  @Implementation
  public void removeFrameCallback(Choreographer.FrameCallback callback) {
    handler.removeCallbacksAndMessages(callback);
  }

  @Implementation
  public long getFrameTimeNanos() {
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

