package org.robolectric.shadows;


import static org.robolectric.shadow.api.Shadow.directlyOn;
import static org.robolectric.shadow.api.Shadow.invokeConstructor;
import static org.robolectric.util.ReflectionHelpers.ClassParameter.from;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.os.Looper;
import android.os.Message;
import android.os.MessageQueue;
import android.os.SystemClock;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.TimeUnit;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.annotation.Resetter;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.ForType;
import org.robolectric.util.reflector.Static;

/**
 * A new variant of a Looper shadow that is active when {@link ShadowBaseLooper#useRealisticLooper()} is enabled.
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
public class ShadowRealisticLooper extends ShadowBaseLooper {

  // Keep reference to all created Loopers so they can be torn down after test
  private static Set<Looper> loopingLoopers = Collections.synchronizedSet(Collections.newSetFromMap(
      new WeakHashMap<Looper, Boolean>()));

  @RealObject private Looper realLooper;

  @Implementation
  protected void __constructor__(boolean quitAllowed) {
    invokeConstructor(Looper.class, realLooper, from(boolean.class, quitAllowed));
    loopingLoopers.add(realLooper);
  }

  @Override
  public void idle() {
    ShadowRealisticMessageQueue shadowQueue = Shadow.extract(realLooper.getQueue());
    if (Thread.currentThread() == realLooper.getThread()) {
      while (!shadowQueue.isIdle()) {
        Message msg = shadowQueue.getNext();
        msg.getTarget().dispatchMessage(msg);
        ShadowRealisticMessage shadowMsg = Shadow.extract(msg);
        shadowMsg.recycleQuietly();
      }
    }
    else {
      throw new UnsupportedOperationException();
    }
  }

  @Override
  public void idleFor(long time, TimeUnit timeUnit) {
    ShadowRealisticSystemClock.advanceBy(time, timeUnit);
    idle();
  }

  @Override
  public void runPaused(Runnable runnable) {
    // directly run, looper is always paused
    runnable.run();
  }

  @Override
  public void pause() {
    // ignore, looper is always paused
  }

  @Resetter
  public static synchronized void reset() {
    if (!ShadowBaseLooper.useRealisticLooper()) {
      // ignore if not realistic looper
      return;
    }
    for (Looper looper: loopingLoopers) {
      ShadowRealisticMessageQueue shadowQueue = Shadow.extract(looper.getQueue());
      shadowQueue.setQuitAllowed(true);
      looper.quit();
      shadowQueue.setReset(true);
    }
    reflector(ReflectorLooper.class).getThreadLocal().remove();
    reflector(ReflectorLooper.class).setMainLooper(null);
    loopingLoopers.clear();
  }

  @ForType(Looper.class)
  private interface ReflectorLooper {
    @Static @Accessor("sThreadLocal")
    ThreadLocal<Looper> getThreadLocal();

    @Static @Accessor("sMainLooper")
    void setMainLooper(Looper looper);
  }
}
