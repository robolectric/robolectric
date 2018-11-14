package org.robolectric.shadows;


import android.hardware.Sensor;
import android.os.Build.VERSION_CODES;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implements;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;

@Implements(Sensor.class)
public class ShadowSensor {

  /**
   * Constructs a {@link Sensor} with a given type.
   */
  public static Sensor newInstance(int type) {
    Sensor sensor = Shadow.newInstanceOf(Sensor.class);
    if (RuntimeEnvironment.getApiLevel() >= VERSION_CODES.M) {
      Shadow.directlyOn(sensor, Sensor.class, "setType", ClassParameter.from(int.class, type));
    } else {
      ReflectionHelpers.setField(Sensor.class, sensor, "mType", type);
    }
    return sensor;
  }
}
