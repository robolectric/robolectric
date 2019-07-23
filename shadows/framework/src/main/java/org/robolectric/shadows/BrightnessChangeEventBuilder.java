package org.robolectric.shadows;

import android.hardware.display.BrightnessChangeEvent;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;

/** Builder for {@link BrightnessChangeEvent}. */
public class BrightnessChangeEventBuilder {
  private float brightness;
  private long timeStamp;
  private String packageName;
  private int userId;
  private float[] luxValues;
  private long[] luxTimestamps;
  private float batteryLevel;
  private float powerBrightnessFactor;
  private boolean nightMode;
  private int colorTemperature;
  private float lastBrightness;
  private boolean isDefaultBrightnessConfig;
  private boolean isUserSetBrightness;
  private long[] colorValueBuckets;
  private long colorSampleDuration;

  public BrightnessChangeEventBuilder setBrightness(float brightness) {
    this.brightness = brightness;
    return this;
  }

  public BrightnessChangeEventBuilder setTimeStamp(long timeStamp) {
    this.timeStamp = timeStamp;
    return this;
  }

  public BrightnessChangeEventBuilder setPackageName(String packageName) {
    this.packageName = packageName;
    return this;
  }

  public BrightnessChangeEventBuilder setUserId(int userId) {
    this.userId = userId;
    return this;
  }

  public BrightnessChangeEventBuilder setLuxValues(float[] luxValues) {
    this.luxValues = luxValues;
    return this;
  }

  public BrightnessChangeEventBuilder setLuxTimestamps(long[] luxTimestamps) {
    this.luxTimestamps = luxTimestamps;
    return this;
  }

  public BrightnessChangeEventBuilder setBatteryLevel(float batteryLevel) {
    this.batteryLevel = batteryLevel;
    return this;
  }

  public BrightnessChangeEventBuilder setPowerBrightnessFactor(float powerBrightnessFactor) {
    this.powerBrightnessFactor = powerBrightnessFactor;
    return this;
  }

  public BrightnessChangeEventBuilder setNightMode(boolean nightMode) {
    this.nightMode = nightMode;
    return this;
  }

  public BrightnessChangeEventBuilder setColorTemperature(int colorTemperature) {
    this.colorTemperature = colorTemperature;
    return this;
  }

  public BrightnessChangeEventBuilder setLastBrightness(float lastBrightness) {
    this.lastBrightness = lastBrightness;
    return this;
  }

  public BrightnessChangeEventBuilder setIsDefaultBrightnessConfig(
      boolean isDefaultBrightnessConfig) {
    this.isDefaultBrightnessConfig = isDefaultBrightnessConfig;
    return this;
  }

  public BrightnessChangeEventBuilder setUserBrightnessPoint(boolean isUserSetBrightness) {
    this.isUserSetBrightness = isUserSetBrightness;
    return this;
  }

  public BrightnessChangeEventBuilder setColorValues(
      long[] colorValueBuckets, long colorSampleDuration) {
    this.colorValueBuckets = colorValueBuckets;
    this.colorSampleDuration = colorSampleDuration;
    return this;
  }

  public BrightnessChangeEvent build() {
    return ReflectionHelpers.callConstructor(
        BrightnessChangeEvent.class,
        ClassParameter.from(float.class, brightness),
        ClassParameter.from(long.class, timeStamp),
        ClassParameter.from(String.class, packageName),
        ClassParameter.from(int.class, userId),
        ClassParameter.from(float[].class, luxValues),
        ClassParameter.from(long[].class, luxTimestamps),
        ClassParameter.from(float.class, batteryLevel),
        ClassParameter.from(float.class, powerBrightnessFactor),
        ClassParameter.from(boolean.class, nightMode),
        ClassParameter.from(int.class, colorTemperature),
        ClassParameter.from(float.class, lastBrightness),
        ClassParameter.from(boolean.class, isDefaultBrightnessConfig),
        ClassParameter.from(boolean.class, isUserSetBrightness),
        ClassParameter.from(long[].class, colorValueBuckets),
        ClassParameter.from(long.class, colorSampleDuration));
  }
}
