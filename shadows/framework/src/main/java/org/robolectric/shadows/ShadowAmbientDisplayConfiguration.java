package org.robolectric.shadows;

import android.os.Build.VERSION_CODES;
import android.os.SystemProperties;
import android.text.TextUtils;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;

/** Shadow for {@code AmbientDisplayConfiguration} class. */
@Implements(
    className = "android.hardware.display.AmbientDisplayConfiguration",
    minSdk = VERSION_CODES.Q,
    isInAndroidSdk = false)
public class ShadowAmbientDisplayConfiguration {

  @SuppressWarnings("NonFinalStaticField")
  private static String dozeComponent;

  @SuppressWarnings("NonFinalStaticField")
  private static boolean dozeAlwaysOnDisplayAvailable;

  @Implementation
  protected String ambientDisplayComponent() {
    return dozeComponent;
  }

  @Implementation
  protected boolean alwaysOnAvailable() {
    return (alwaysOnDisplayDebuggingEnabled() || alwaysOnDisplayAvailable())
        && ambientDisplayAvailable();
  }

  @Implementation
  protected boolean ambientDisplayAvailable() {
    return !TextUtils.isEmpty(ambientDisplayComponent());
  }

  @Implementation
  protected boolean alwaysOnDisplayDebuggingEnabled() {
    return SystemProperties.getBoolean("debug.doze.aod", false)
        && (SystemProperties.getInt("ro.debuggable", 0) == 1);
  }

  @Implementation
  protected boolean alwaysOnDisplayAvailable() {
    return dozeAlwaysOnDisplayAvailable;
  }

  /**
   * Overrides the string format of component for doze mode. See {@link #ambientDisplayComponent()}.
   */
  public static void setDozeComponent(String dozeComponent) {
    ShadowAmbientDisplayConfiguration.dozeComponent = dozeComponent;
  }

  /**
   * Overrides the available state for always on display. See {@link #alwaysOnDisplayAvailable()}.
   */
  public static void setDozeAlwaysOnDisplayAvailable(boolean dozeAlwaysOnDisplayAvailable) {
    ShadowAmbientDisplayConfiguration.dozeAlwaysOnDisplayAvailable = dozeAlwaysOnDisplayAvailable;
  }

  @Resetter
  public static void reset() {
    dozeComponent = null;
    dozeAlwaysOnDisplayAvailable = false;
  }
}
