package org.robolectric.shadows;

import com.android.server.power.PowerManagerService;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

@Implements(value = PowerManagerService.class, isInAndroidSdk = false)
public class ShadowPowerManagerService {
  @Implementation
  public static void nativeAcquireSuspendBlocker(String var0) {
  }

  @Implementation
  private static void nativeReleaseSuspendBlocker(String var0) {
  }
}
