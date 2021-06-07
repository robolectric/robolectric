package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.KITKAT;

import android.animation.TimeAnimator;
import android.animation.TimeAnimator.TimeListener;
import java.time.Duration;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;

/** Shadow for the {@link TimeAnimator} class. */
@Implements(value = TimeAnimator.class)
public class ShadowTimeAnimator {

  @RealObject private TimeAnimator realObject;

  private boolean started = false;
  private TimeListener listener;
  private long currentTimeMs = 0;

  @Implementation(minSdk = KITKAT)
  public void start() {
    currentTimeMs = 0;
    started = true;
  }

  @Implementation
  public void setTimeListener(TimeListener listener) {
    this.listener = listener;
  }

  public void progressTimeBy(Duration duration, Duration updatePeriod) {
    if (!started) {
      return;
    }

    long stopTime = currentTimeMs + duration.toMillis();
    while (currentTimeMs + updatePeriod.toMillis() <= stopTime) {
      currentTimeMs += updatePeriod.toMillis();
      listener.onTimeUpdate(realObject, currentTimeMs, updatePeriod.toMillis());
    }
    listener.onTimeUpdate(realObject, stopTime, stopTime - currentTimeMs);
    currentTimeMs = stopTime;
  }
}
