package org.robolectric.shadows;

import android.os.SystemClock;
import org.robolectric.internal.HiddenApi;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

@Implements(value = SystemClock.class, callThroughByDefault = true)
public class ShadowSystemClock {
  private static long bootedAt = now();

  private static long now() {
    return System.currentTimeMillis();
  }

  @Implementation
  public static boolean setCurrentTimeMillis(long millis) {
    return false;
  }

  @Implementation
  public static long uptimeMillis() {
    return now() - bootedAt;
  }

  @Implementation
  public static long elapsedRealtime() {
    return uptimeMillis();
  }

  @Implementation
  public static long currentThreadTimeMillis() {
    return uptimeMillis();
  }

  @HiddenApi @Implementation
  public static long currentThreadTimeMicro() {
    return uptimeMillis() * 1000;
  }

  @HiddenApi @Implementation
  public static long currentTimeMicro() {
    return now() * 1000;
  }
}
