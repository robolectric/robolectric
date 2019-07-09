package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.Q;

import android.Manifest;
import android.annotation.RequiresPermission;
import android.annotation.SystemApi;
import android.hardware.display.ColorDisplayManager.AutoMode;
import java.util.HashMap;
import java.util.Map;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

@Implements(
    className = "android.hardware.display.ColorDisplayManager",
    isInAndroidSdk = false,
    minSdk = Q)
public class ShadowColorDisplayManager {

  private boolean isNightDisplayActivated;
  private int nightDisplayTemperature;
  private int nightDisplayAutoMode;
  private final Map<String, Integer> packagesToSaturation = new HashMap<>();

  // Full saturation by default
  private int saturationLevel = 100;
  // No capabilities by default
  private int transformCapabilities = 0x0;

  @Implementation
  protected void __constructor__() {
    // Don't initialize ColorDisplayManagerInternal.
  }

  @Implementation
  @SystemApi
  @RequiresPermission(Manifest.permission.CONTROL_DISPLAY_COLOR_TRANSFORMS)
  protected int getTransformCapabilities() {
    return transformCapabilities;
  }

  @Implementation
  @SystemApi
  @RequiresPermission(Manifest.permission.CONTROL_DISPLAY_COLOR_TRANSFORMS)
  protected boolean setNightDisplayActivated(boolean activated) {
    isNightDisplayActivated = activated;
    return true;
  }

  @Implementation
  @SystemApi
  @RequiresPermission(Manifest.permission.CONTROL_DISPLAY_COLOR_TRANSFORMS)
  protected boolean isNightDisplayActivated() {
    return isNightDisplayActivated;
  }

  @Implementation
  @SystemApi
  @RequiresPermission(Manifest.permission.CONTROL_DISPLAY_COLOR_TRANSFORMS)
  protected boolean setNightDisplayColorTemperature(int temperature) {
    nightDisplayTemperature = temperature;
    return true;
  }

  @Implementation
  @SystemApi
  @RequiresPermission(Manifest.permission.CONTROL_DISPLAY_COLOR_TRANSFORMS)
  protected int getNightDisplayColorTemperature() {
    return nightDisplayTemperature;
  }

  @Implementation
  @SystemApi
  @RequiresPermission(Manifest.permission.CONTROL_DISPLAY_COLOR_TRANSFORMS)
  protected boolean setNightDisplayAutoMode(@AutoMode int autoMode) {
    nightDisplayAutoMode = autoMode;
    return true;
  }

  @Implementation
  @SystemApi
  @RequiresPermission(Manifest.permission.CONTROL_DISPLAY_COLOR_TRANSFORMS)
  @AutoMode
  protected int getNightDisplayAutoMode() {
    return nightDisplayAutoMode;
  }

  @Implementation
  @SystemApi
  @RequiresPermission(Manifest.permission.CONTROL_DISPLAY_COLOR_TRANSFORMS)
  protected boolean setSaturationLevel(int saturationLevel) {
    this.saturationLevel = saturationLevel;
    return true;
  }

  @Implementation
  @SystemApi
  @RequiresPermission(Manifest.permission.CONTROL_DISPLAY_COLOR_TRANSFORMS)
  protected boolean setAppSaturationLevel(String packageName, int saturationLevel) {
    packagesToSaturation.put(packageName, saturationLevel);
    return true;
  }

  /** Sets the current transform capabilities. */
  public boolean setTransformCapabilities(int transformCapabilities) {
    this.transformCapabilities = transformCapabilities;
    return true;
  }

  /** Returns the current display saturation level for the {@code packageName}. */
  public int getAppSaturationLevel(String packageName) {
    return packagesToSaturation.getOrDefault(packageName, 100);
  }

  /** Returns the current display saturation level. */
  public int getSaturationLevel() {
    return saturationLevel;
  }
}
