package org.robolectric.shadows;


import static org.robolectric.shadow.api.Shadow.directlyOn;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import java.util.concurrent.TimeUnit;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.annotation.Resetter;
import org.robolectric.shadow.api.Shadow;

/**
 * A new variant of a Looper shadow that is active when {@link ShadowBaseLooper#useNewLooper()} is enabled.
 *
 * This shadow differs from the legacy {@link ShadowLooper} in the following ways:
 * - Has no connection to {@link org.robolectric.util.Scheduler}. Its APIs are standalone
 * - The main looper is always paused. Posted messages are not executed unless {@link #idle()} is called.
 * - Just like in real Android, each looper has its own thread, and posted tasks get executed in that thread.
 * - There is only a single {@link SystemClock} value that all loopers read from. Unlike legacy behavior where each {@link org.robolectric.util.Scheduler} kept their own clock value.
 */
@Implements(
    value = Looper.class,
    shadowPicker = ShadowBaseLooper.Picker.class,
    // TODO: turn off shadowOf generation. Figure out why this is needed
    isInAndroidSdk = false)
public class ShadowNewLooper extends ShadowBaseLooper {

  @RealObject private Looper realLooper;

  @Override
  public void idle() {
    ShadowNewMessageQueue shadowQueue = Shadow.extract(realLooper.getQueue());
    if (Thread.currentThread() == realLooper.getThread()) {
      while (!shadowQueue.isIdle()) {
        Message msg = shadowQueue.getNext();
        msg.getTarget().dispatchMessage(msg);
        ShadowNewMessage shadowMsg = Shadow.extract(msg);
        shadowMsg.recycleQuietly();
      }
    }
    else {
      throw new UnsupportedOperationException();
    }
  }

  @Override
  public void idleFor(long time, TimeUnit timeUnit) {
    SystemClock.setCurrentTimeMillis(SystemClock.currentThreadTimeMillis() + timeUnit.toMillis(time));
    idle();
  }

  @Override
  public void runPaused(Runnable runnable) {
    // directly run, looper is always paused
    runnable.run();
  }

  @Resetter
  public static synchronized void reset() {
    // Classes may have static references to main Looper, like Choreographer.
    // So for now, don't tear down main looper references, and just clear the queue instead
    // TODO: clear all loopers
    ShadowNewMessageQueue shadowQueue = Shadow.extract(Looper.getMainLooper().getQueue());
    shadowQueue.reset();
  }
}
