package org.robolectric.shadows;

import android.os.Looper;
import java.util.concurrent.TimeUnit;
import org.robolectric.shadow.api.Shadow;

public abstract class ShadowBaseLooper {

  public static boolean useRealisticLooper() {
    // TODO: get this from configuration
    return true;
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
