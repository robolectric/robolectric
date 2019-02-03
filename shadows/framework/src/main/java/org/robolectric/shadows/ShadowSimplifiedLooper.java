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
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.ForType;
import org.robolectric.util.reflector.Static;

/** A simpler variant of a Looper shadow that is active when ControlledLooper is enabled. */
@Implements(
    value = Looper.class,
    shadowPicker = ShadowBaseLooper.Picker.class,
    // TODO: turn off shadowOf generation. Figure out why this is needed
    isInAndroidSdk = false)
public class ShadowSimplifiedLooper extends ShadowBaseLooper {

  @RealObject private Looper realLooper;

  @Override
  public void idle() {
    ShadowSimplifiedMessageQueue shadowQueue = Shadow.extract(realLooper.getQueue());
    if (Thread.currentThread() == realLooper.getThread()) {
      while (!shadowQueue.isIdle()) {
        Message msg = shadowQueue.getNext();
        msg.getTarget().dispatchMessage(msg);
        ShadowSimplifiedMessage shadowMsg = Shadow.extract(msg);
        shadowMsg.recycleQuietly();
      }
    }
    else {
      throw new UnsupportedOperationException();
    }
  }

  @Override
  public void idleFor(long time, TimeUnit timeUnit) {
    idle();
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

    // TODO: clear all loopers
    ShadowSimplifiedMessageQueue shadowQueue = Shadow.extract(Looper.getMainLooper().getQueue());
    shadowQueue.reset();
    reflector(_Looper_.class).setThreadLocal(new ThreadLocal<>());
    reflector(_Looper_.class).setMainLooper(null);
  }

  @ForType(Looper.class)
  private interface _Looper_ {
    @Static @Accessor("sThreadLocal")
    void setThreadLocal(ThreadLocal<Looper> looper);

    @Static @Accessor("sMainLooper")
    void setMainLooper(Looper looper);

  }
}
