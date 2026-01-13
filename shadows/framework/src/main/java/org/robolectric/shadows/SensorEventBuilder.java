package org.robolectric.shadows;


import android.hardware.Sensor;
import android.hardware.SensorEvent;
import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import javax.annotation.Nonnull;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;

/** Builder for {@link SensorEvent}. */
public class SensorEventBuilder {
  private float[] values;
  private int accuracy = 0;
  private long timestampNs = 0;
  private Sensor sourceSensor = null;

  private SensorEventBuilder(Sensor sourceSensor, float[] values) {
    this.sourceSensor = sourceSensor;
    this.values = values;
  }

  /**
   * @deprecated Use {@link #newBuilder(Sensor, float[])} instead.
   */
  @Deprecated
  public static SensorEventBuilder newBuilder() {
    return new SensorEventBuilder(null, null);
  }

  public static SensorEventBuilder newBuilder(Sensor sensor, float[] values) {
    Preconditions.checkNotNull(sensor, "sensor cannot be null");
    Preconditions.checkNotNull(values, "values cannot be null");
    Preconditions.checkArgument(values.length > 0, "values cannot be empty");
    return new SensorEventBuilder(sensor, values);
  }

  @CanIgnoreReturnValue
  public SensorEventBuilder setValues(@Nonnull float[] values) {
    this.values = values;
    return this;
  }

  @CanIgnoreReturnValue
  public SensorEventBuilder setSensor(@Nonnull Sensor value) {
    sourceSensor = value;
    return this;
  }

  @CanIgnoreReturnValue
  public SensorEventBuilder setTimestamp(long value) {
    timestampNs = value;
    return this;
  }

  @CanIgnoreReturnValue
  public SensorEventBuilder setAccuracy(int value) {
    accuracy = value;
    return this;
  }

  public SensorEvent build() {
    // SensorEvent values and a source Sensor object need be set.
    Preconditions.checkArgument(
        values != null && values.length > 0, "values cannot be null or empty");
    Preconditions.checkArgument(sourceSensor != null, "sensor cannot be null");

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
