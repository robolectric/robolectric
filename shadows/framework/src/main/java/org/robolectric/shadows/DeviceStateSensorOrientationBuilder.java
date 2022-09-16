package org.robolectric.shadows;

import android.hardware.camera2.params.DeviceStateSensorOrientationMap;
import android.os.Build.VERSION_CODES;
import androidx.annotation.RequiresApi;
import com.google.errorprone.annotations.CanIgnoreReturnValue;

/** Builder for {@link DeviceStateSensorOrientationMap} which was introduced in Android T. */
@RequiresApi(VERSION_CODES.TIRAMISU)
public class DeviceStateSensorOrientationBuilder {
  private long[] sensorOrientationMap;

  private DeviceStateSensorOrientationBuilder() {}

  public static DeviceStateSensorOrientationBuilder newBuilder() {
    return new DeviceStateSensorOrientationBuilder();
  }

  @CanIgnoreReturnValue
  public DeviceStateSensorOrientationBuilder addSensorOrientationMap(long[] sensorOrientationMap) {
    this.sensorOrientationMap = sensorOrientationMap;
    return this;
  }

  public DeviceStateSensorOrientationMap build() {
    return new DeviceStateSensorOrientationMap(sensorOrientationMap);
  }
}
