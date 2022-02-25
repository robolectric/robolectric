package org.robolectric.shadows;

import android.hardware.display.BrightnessChangeEvent;

/** Builder for {@link BrightnessChangeEvent}. */
public class BrightnessChangeEventBuilder {

  private final BrightnessChangeEvent.Builder builderInternal = new BrightnessChangeEvent.Builder();

  public BrightnessChangeEventBuilder setBrightness(float brightness) {
    builderInternal.setBrightness(brightness);
    return this;
  }

  public BrightnessChangeEventBuilder setTimeStamp(long timeStamp) {
    builderInternal.setTimeStamp(timeStamp);
    return this;
  }

  public BrightnessChangeEventBuilder setPackageName(String packageName) {
    builderInternal.setPackageName(packageName);
    return this;
  }

  public BrightnessChangeEventBuilder setUserId(int userId) {
    builderInternal.setUserId(userId);
    return this;
  }

  public BrightnessChangeEventBuilder setLuxValues(float[] luxValues) {
    builderInternal.setLuxValues(luxValues);
    return this;
  }

  public BrightnessChangeEventBuilder setLuxTimestamps(long[] luxTimestamps) {
    builderInternal.setLuxTimestamps(luxTimestamps);
    return this;
  }

  public BrightnessChangeEventBuilder setBatteryLevel(float batteryLevel) {
    builderInternal.setBatteryLevel(batteryLevel);
    return this;
  }

  public BrightnessChangeEventBuilder setPowerBrightnessFactor(float powerBrightnessFactor) {
    builderInternal.setPowerBrightnessFactor(powerBrightnessFactor);
    return this;
  }

  public BrightnessChangeEventBuilder setNightMode(boolean nightMode) {
    builderInternal.setNightMode(nightMode);
    return this;
  }

  public BrightnessChangeEventBuilder setColorTemperature(int colorTemperature) {
    builderInternal.setColorTemperature(colorTemperature);
    return this;
  }

  public BrightnessChangeEventBuilder setLastBrightness(float lastBrightness) {
    builderInternal.setLastBrightness(lastBrightness);
    return this;
  }

  public BrightnessChangeEventBuilder setIsDefaultBrightnessConfig(
      boolean isDefaultBrightnessConfig) {
    builderInternal.setIsDefaultBrightnessConfig(isDefaultBrightnessConfig);
    return this;
  }

  public BrightnessChangeEventBuilder setUserBrightnessPoint(boolean isUserSetBrightness) {
    builderInternal.setUserBrightnessPoint(isUserSetBrightness);
    return this;
  }

  public BrightnessChangeEventBuilder setColorValues(
      long[] colorValueBuckets, long colorSampleDuration) {
    builderInternal.setColorValues(colorValueBuckets, colorSampleDuration);
    return this;
  }

  public BrightnessChangeEventBuilder setUniqueDisplayId(String displayId) {
    builderInternal.setUniqueDisplayId(displayId);
    return this;
  }

  public BrightnessChangeEvent build() {
    return builderInternal.build();
  }
}
