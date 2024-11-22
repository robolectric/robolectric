package org.robolectric.shadows;

import static com.google.common.base.Preconditions.checkArgument;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import javax.annotation.Nonnull;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;

/** Builder for {@link SensorEvent}. */
public class SensorEventBuilder {
  private float[] values;
  private int accuracy = 0;
  private long timestampNs = 0;
  private Sensor sourceSensor = null;

  private SensorEventBuilder() {}

  public static SensorEventBuilder newBuilder() {
    return new SensorEventBuilder();
  }

  public SensorEventBuilder setValues(@Nonnull float[] value) {
    values = value;
    return this;
  }

  /**
   * If the 'type' property of Sensor is all that is important to your use case, an instance of from
   * ShadowSensor.newInstance(sensorType) should suffice.
   */
  public SensorEventBuilder setSensor(@Nonnull Sensor value) {
    sourceSensor = value;
    return this;
  }

  public SensorEventBuilder setTimestamp(long value) {
    timestampNs = value;
    return this;
  }

  public SensorEventBuilder setAccuracy(int value) {
    accuracy = value;
    return this;
  }

  public SensorEvent build() {
    // SensorEvent values and a source Sensor object need be set.
    checkArgument(values != null && values.length > 0);
    checkArgument(sourceSensor != null);

    SensorEvent sensorEvent =
        ReflectionHelpers.callConstructor(
            SensorEvent.class, ClassParameter.from(int.class, values.length));

    System.arraycopy(values, 0, sensorEvent.values, 0, values.length);
    sensorEvent.accuracy = accuracy;
    sensorEvent.timestamp = timestampNs;
    sensorEvent.sensor = sourceSensor;

    return sensorEvent;
  }
}
