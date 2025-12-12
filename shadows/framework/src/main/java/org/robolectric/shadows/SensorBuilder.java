package org.robolectric.shadows;

import android.hardware.Sensor;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import org.robolectric.util.ReflectionHelpers;

/** Builder for {@link Sensor}. */
public class SensorBuilder {

  private static final int SENSOR_FLAG_WAKE_UP_SENSOR = 1;

  private int type;
  private int flags;
  private float maximumRange;
  private int minDelay;
  private String name;

  private SensorBuilder() {}

  public static SensorBuilder newBuilder() {
    return new SensorBuilder();
  }

  @CanIgnoreReturnValue
  public SensorBuilder setType(int type) {
    this.type = type;
    return this;
  }

  @CanIgnoreReturnValue
  public SensorBuilder setWakeUpFlag(boolean wakeUpFlag) {
    if (wakeUpFlag) {
      this.flags |= SENSOR_FLAG_WAKE_UP_SENSOR;
    } else {
      this.flags &= ~SENSOR_FLAG_WAKE_UP_SENSOR;
    }
    return this;
  }

  @CanIgnoreReturnValue
  public SensorBuilder setMaximumRange(float maximumRange) {
    this.maximumRange = maximumRange;
    return this;
  }

  @CanIgnoreReturnValue
  public SensorBuilder setMinDelay(int minDelay) {
    this.minDelay = minDelay;
    return this;
  }

  @CanIgnoreReturnValue
  public SensorBuilder setName(String name) {
    this.name = name;
    return this;
  }

  public Sensor build() {
    Sensor sensor = ReflectionHelpers.callConstructor(Sensor.class);
    ReflectionHelpers.setField(sensor, "mType", type);
    ReflectionHelpers.setField(sensor, "mMaxRange", maximumRange);
    ReflectionHelpers.setField(sensor, "mMinDelay", minDelay);
    ReflectionHelpers.setField(sensor, "mName", name);
    ReflectionHelpers.setField(sensor, "mFlags", flags);
    return sensor;
  }
}
