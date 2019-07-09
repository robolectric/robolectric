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
    reflector(_Sensor_.class, sensor).setTypeCompat(type);
    return sensor;
  }

  /** Controls the return value of {@link Sensor#isWakeUpSensor()}. */
  public void setWakeUpFlag(boolean wakeup) {
    int wakeUpSensorMask = reflector(_Sensor_.class).getWakeUpSensorFlag();

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

  private void setMask(int mask) {
    _Sensor_ _sensor_ = reflector(_Sensor_.class, realSensor);
    _sensor_.setFlags(_sensor_.getFlags() | mask);
  }

  private void clearMask(int mask) {
    _Sensor_ _sensor_ = reflector(_Sensor_.class, realSensor);
    _sensor_.setFlags(_sensor_.getFlags() & ~mask);
  }

  /** Accessor interface for {@link Sensor}'s internals. */
  @ForType(Sensor.class)
  interface _Sensor_ {

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

    @Static
    @Accessor("SENSOR_FLAG_WAKE_UP_SENSOR")
    int getWakeUpSensorFlag();
  }
}
