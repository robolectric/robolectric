package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE;

import android.companion.virtual.sensor.VirtualSensor;
import android.companion.virtual.sensor.VirtualSensorEvent;
import java.util.ArrayList;
import java.util.List;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/** Shadow for VirtualSensor. */
@Implements(
    value = VirtualSensor.class,
    minSdk = UPSIDE_DOWN_CAKE,
    // TODO: remove when minimum supported compileSdk is >= 34
    isInAndroidSdk = false)
public class ShadowVirtualSensor {

  private int deviceId = 0;
  private final List<VirtualSensorEvent> sentEvents = new ArrayList<>();

  @Implementation
  protected int getDeviceId() {
    return deviceId;
  }

  @Implementation
  protected void sendEvent(VirtualSensorEvent event) {
    sentEvents.add(event);
  }

  public List<VirtualSensorEvent> getSentEvents() {
    return sentEvents;
  }

  void setDeviceId(int deviceId) {
    this.deviceId = deviceId;
  }
}
