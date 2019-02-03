package org.robolectric.shadows;

import static com.google.common.base.Preconditions.checkState;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.app.Instrumentation;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import org.robolectric.android.compat.InstrumentationCompat;
import org.robolectric.android.compat.MessageCompat;
import org.robolectric.android.compat.MessageQueueCompat;
import org.robolectric.android.compat.TestLooperManagerCompat;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.shadows.ShadowSimplifiedMessageQueue._MessageQueue_;

/**
 * A test API for deterministically executing tasks posted to a {@link Looper}.
 *
 * <p>{@link #useControlledLooper()} must be true to use this API.
 *
 * <p>When the controlledLooper feature is enabled, tasks posted to the main Looper are not
 * automatically executed, and the {@link SystemClock} time is frozen in place.
 *
 * <p>The {@link #register()} call can be used to put any background/non-main Looper into a
 * controlled state.
 */
public class ControlledLooper {

  private static boolean useControlledLooper = true;

  // TODO: get this from a Configuration
  public static boolean useControlledLooper() {
    return useControlledLooper;
  }

  private final Looper looper;
  private final LooperAccessor accessor;

  private static final List<ControlledLooper> controlledLoopers = new ArrayList<>();

  private ControlledLooper(LooperAccessor accessor, Looper looper) {
    this.accessor = accessor;
    this.looper = looper;
  }

  /**
   * Register a Looper to be put into controlled state.
   *
   * <p>If this is called from the looper's current thread, all future access must be from the same
   * thread.
   */
  public static ControlledLooper register(Looper looper) {
    LooperAccessor looperAccessor;
    if (get(looper) != null) {
      throw new IllegalStateException("Looper is already registered " + looper);
    }
    if (Thread.currentThread() == looper.getThread()) {
      // TestLooperManager deadlocks if you try to acquire and then advance from the looper thread.
      // So just use a simplified variant that directly executes messages.
      looperAccessor = new ShadowLooperAccessor(looper);
    } else {
      // TODO: use androidx.test InstrumentationRegistry
      Instrumentation instrumentation = ShadowInstrumentation.getInstrumentation();
      TestLooperManagerCompat looperManager =
          InstrumentationCompat.acquireLooperManager(instrumentation, looper);
      looperAccessor = new TestLooperManagerAccessor(looperManager);
    }
    ControlledLooper controlledLooper = new ControlledLooper(looperAccessor, looper);
    controlledLoopers.add(controlledLooper);
    return controlledLooper;
  }

  /**
   * Retrieve the ControlledLooper for the given Looper.
   *
   * <p>Returns null if there is no ControlledLooper registered.
   */
  public static ControlledLooper get(Looper looper) {
    for (ControlledLooper controlledLooper : controlledLoopers) {
      if (controlledLooper.getLooper().equals(looper)) {
        return controlledLooper;
      }
    }
    return null;
  }

  /** A convenience method for get(Looper.getMainLooper()) */
  public static ControlledLooper getMainControllerLooper() {
    return get(Looper.getMainLooper());
  }

  /**
   * Release the current looper from controlled state.
   *
   * <p>Tests should not call this directly for the main thread.
   */
  public void release() {
    accessor.release();
    controlledLoopers.remove(this);
  }

  public static void reset() {
    for (ControlledLooper controlledLooper : controlledLoopers) {
      controlledLooper.accessor.release();
      controlledLooper.accessor.reset();
    }
    controlledLoopers.clear();
  }

  private Looper getLooper() {
    return looper;
  }

  /**
   * Execute all queued looper tasks due to execute before or at the current {@link
   * SystemClock#uptimeMillis()}
   *
   * <p>If the current ControlledLooper is controlling the main looper, (or it was registered from
   * the looper's thread), this method can only be called from the same thread.
   */
  public void idle() {
    while (!accessor.isIdle()) {
      Message nextMessage = accessor.next();
      accessor.execute(nextMessage);
      accessor.recycle(nextMessage);
    }
  }

  /** Advances the SystemClock by given time, and execute all runnables due before that time. */
  public void idleFor(long time, TimeUnit timeUnit) {
    SystemClock.setCurrentTimeMillis(SystemClock.uptimeMillis() + timeUnit.toMillis(time));
    idle();
  }

  private interface LooperAccessor {
    Message next();

    void execute(Message msg);

    void recycle(Message msg);

    void release();

    boolean isIdle();

    void reset();
  }

  private static class ShadowLooperAccessor implements LooperAccessor {
    private final Looper looper;

    ShadowLooperAccessor(Looper looper) {
      this.looper = looper;
    }

    public boolean isIdle() {
      return MessageQueueCompat.isIdle(looper.getQueue());
    }

    public Message next() {
      checkLooperThread();
      return reflector(_MessageQueue_.class, looper.getQueue()).next();
    }

    public void execute(Message msg) {
      checkLooperThread();
      msg.getTarget().dispatchMessage(msg);
    };

    public void recycle(Message msg) {
      checkLooperThread();
      MessageCompat.recycleUnchecked(msg);
    }

    public void release() {
    }

    public void reset() {
      ShadowSimplifiedMessageQueue shadowQueue = Shadow.extract(looper.getQueue());
      shadowQueue.reset();
    }

    private void checkLooperThread() {
      checkState(
          looper.getThread() == Thread.currentThread(),
          "Can only be called from this looper's thread");
    }
  }

  private static class TestLooperManagerAccessor implements LooperAccessor {
    private final TestLooperManagerCompat looperManager;

    TestLooperManagerAccessor(TestLooperManagerCompat looperManager) {
      this.looperManager = looperManager;
    }

    public boolean isIdle() {
      return MessageQueueCompat.isIdle(looperManager.getMessageQueue());
    }

    public Message next() {
      return looperManager.next();
    }

    public void execute(Message msg) {
      looperManager.execute(msg);
    }

    public void recycle(Message msg) {
      looperManager.recycle(msg);
    }

    public void release() {
      looperManager.release();
    }

    public void reset() {
      ShadowSimplifiedMessageQueue shadowQueue = Shadow.extract(looperManager.getMessageQueue());
      shadowQueue.reset();
    }
  }
}
