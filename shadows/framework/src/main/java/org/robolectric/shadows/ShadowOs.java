package org.robolectric.shadows;

import android.system.Os;
import java.util.HashMap;
import java.util.Map;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/** A Shadow for android.system.Os */
@Implements(value = Os.class, minSdk = 21)
public final class ShadowOs {

  private ShadowOs() {}

  private static final Map<Integer, Long> sysconfValues = new HashMap<>();

  /** Configures values to be returned by sysconf. */
  public static void setSysconfValue(int name, long value) {
    sysconfValues.put(name, value);
  }

  /** Returns the value configured via setSysconfValue, or -1 if one hasn't been configured. */
  @Implementation
  protected static long sysconf(int name) {
    return sysconfValues.getOrDefault(name, -1L);
  }
}
