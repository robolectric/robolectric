package org.robolectric.shadows;

import static org.robolectric.util.reflector.Reflector.reflector;

import android.hardware.Sensor;
import android.os.Build.VERSION_CODES;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.ForType;
import org.robolectric.util.reflector.Static;

@Implements(Sensor.class)
public class ShadowSensor {

  @RealObject private Sensor realSensor;

  private float maximumRange = 0;

  /** Constructs a {@link Sensor} with a given type. */
  public static Sensor newInstance(int type) {
    Sensor sensor = Shadow.newInstanceOf(Sensor.class);
    reflector(SensorReflector.class, sensor).setTypeCompat(type);
    return sensor;
  }

  /** Controls the return value of {@link Sensor#isWakeUpSensor()}. */
  public void setWakeUpFlag(boolean wakeup) {
    int wakeUpSensorMask = reflector(SensorReflector.class).getWakeUpSensorFlag();

    if (wakeup) {
      setMask(wakeUpSensorMask);
    } else {
      clearMask(wakeUpSensorMask);
    }
  }

  /** Sets the return value for {@link Sensor#getMaximumRange}. */
  public void setMaximumRange(float range) {
    maximumRange = range;
  }

  @Implementation
  protected float getMaximumRange() {
    return maximumRange;
  }

  /** Sets the return value for {@link Sensor#getMinDelay}. */
  public void setMinDelay(int delay) {
    reflector(SensorReflector.class, realSensor).setMinDelay(delay);
  }

  private void setMask(int mask) {
    SensorReflector sensorReflector = reflector(SensorReflector.class, realSensor);
    sensorReflector.setFlags(sensorReflector.getFlags() | mask);
  }

  private void clearMask(int mask) {
    SensorReflector sensorReflector = reflector(SensorReflector.class, realSensor);
    sensorReflector.setFlags(sensorReflector.getFlags() & ~mask);
  }

  /** Sets the return value for {@link Sensor#getName}. */
  public void setName(String name) {
    reflector(SensorReflector.class, realSensor).setName(name);
  }

  /** Accessor interface for {@link Sensor}'s internals. */
  @ForType(Sensor.class)
  interface SensorReflector {

    @Accessor("mType")
    void setTypeField(int type);

    void setType(int type);

    default void setTypeCompat(int type) {
      if (RuntimeEnvironment.getApiLevel() >= VERSION_CODES.M) {
        setType(type);
      } else {
        setTypeField(type);
      }
    }

    @Accessor("mFlags")
    int getFlags();

    @Accessor("mFlags")
    void setFlags(int flags);

    @Accessor("mName")
    void setName(String name);

    @Accessor("mMinDelay")
    void setMinDelay(int minDelay);

    @Static
    @Accessor("SENSOR_FLAG_WAKE_UP_SENSOR")
    int getWakeUpSensorFlag();
  }
}
