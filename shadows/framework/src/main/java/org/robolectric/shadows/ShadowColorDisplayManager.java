package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.Q;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.Manifest;
import android.annotation.RequiresPermission;
import android.annotation.SystemApi;
import android.hardware.display.ColorDisplayManager.AutoMode;
import java.util.HashMap;
import java.util.Map;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.ForType;
import org.robolectric.util.reflector.WithType;

/** Shadow for {@link ColorDisplayManager}. */
@Implements(
    className = "android.hardware.display.ColorDisplayManager",
    isInAndroidSdk = false,
    minSdk = Q)
public class ShadowColorDisplayManager {

  // These member variables do not need to be static, ColorDisplayManager uses a static instance.
  private boolean isNightDisplayActivated;
  private int nightDisplayTemperature;
  private int nightDisplayAutoMode;
  private final Map<String, Integer> packagesToSaturation = new HashMap<>();

  // Full saturation by default
  private int saturationLevel = 100;
  // No capabilities by default
  private int transformCapabilities = 0x0;

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

  @Resetter
  public static void reset() {
    reflector(ColorDisplayManagerInternalReflector.class).setInstance(null);
  }

  @ForType(className = "android.hardware.display.ColorDisplayManager$ColorDisplayManagerInternal")
  interface ColorDisplayManagerInternalReflector {
    @Accessor("sInstance")
    void setInstance(
        @WithType("android.hardware.display.ColorDisplayManager$ColorDisplayManagerInternal")
            Object instance);
  }
}
