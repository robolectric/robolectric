package org.robolectric.shadows;

import android.os.Build;
import java.util.HashMap;
import java.util.Map;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

@Implements(
    className = "android.hardware.display.ColorDisplayManager",
    isInAndroidSdk = false,
    minSdk = Build.VERSION_CODES.Q)
public class ShadowColorDisplayManager {

  private int saturationLevel = 100;
  private int transformCapabilities = 0x4;
  private Map<String, Integer> packagesToSaturation = new HashMap<>();

  @Implementation
  protected void __constructor__() {
    // don't initialize ColorDisplayManagerInternal
  }

  @Implementation
  protected boolean setSaturationLevel(int saturationLevel) {
    this.saturationLevel = saturationLevel;
    return true;
  }

  /** Returns the current display saturation level. */
  public int getSaturationLevel() {
    return saturationLevel;
  }

  @Implementation
  protected int getTransformCapabilities() {
    return transformCapabilities;
  }

  /** Sets the current transform capabilities. */
  public boolean setTransformCapabilities(int transformCapabilities) {
    this.transformCapabilities = transformCapabilities;
    return true;
  }

  @Implementation
  protected boolean setAppSaturationLevel(String packageName, int saturationLevel) {
    packagesToSaturation.put(packageName, saturationLevel);
    return true;
  }

  /** Returns the current display saturation level for the {@code packageName}. */
  public int getAppSaturationLevel(String packageName) {
    return packagesToSaturation.getOrDefault(packageName, 100);
  }
}
