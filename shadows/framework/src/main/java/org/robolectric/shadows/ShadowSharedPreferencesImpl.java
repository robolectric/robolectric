package org.robolectric.shadows;

import static org.robolectric.shadow.api.Shadow.directlyOn;

import android.os.Build;
import android.os.Build.VERSION_CODES;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;

@Implements(className = "android.app.SharedPreferencesImpl",
    isInAndroidSdk = false)
public class ShadowSharedPreferencesImpl {
  @RealObject private Object realSharedPreferencesImpl;


  /**
   * Override to load from disk synchronously, to prevent cross-test collisions where one test has
   * finished but thread to load from disk is still active.
   */
  @Implementation
  public void startLoadFromDisk() {
    if (RuntimeEnvironment.getApiLevel() >= VERSION_CODES.N) {
      directlyOn(realSharedPreferencesImpl, "android.app.SharedPreferencesImpl", "loadFromDisk");
    } else {
      synchronized (realSharedPreferencesImpl) {
        directlyOn(realSharedPreferencesImpl, "android.app.SharedPreferencesImpl",
            "loadFromDiskLocked");
      }
    }
  }
}
