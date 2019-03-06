package org.robolectric.shadows;

import android.os.Looper;
import java.util.concurrent.TimeUnit;
import org.robolectric.shadow.api.Shadow;

public abstract class ShadowBaseLooper {

  // DO NOT SUBMIT. Tmp, do not use
  public static final boolean USE_REALISTIC_LOOPER = true;

  public static boolean useRealisticLooper() {
    // TODO: get this from configuration
    return USE_REALISTIC_LOOPER;
  }

  public abstract void idle();
  public abstract void idleFor(long time, TimeUnit timeUnit);
  public abstract void runPaused(Runnable run);
  public abstract void pause();

  public static ShadowBaseLooper shadowMainLooper() {
    return Shadow.extract(Looper.getMainLooper());
  }

  public static class Picker extends LooperShadowPicker<ShadowBaseLooper> {

    public Picker() {
      super(ShadowLooper.class, ShadowRealisticLooper.class);
    }
  }
}
