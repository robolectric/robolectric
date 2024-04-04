package org.robolectric.shadows;

import android.companion.virtual.sensor.VirtualSensor;
import android.companion.virtual.sensor.VirtualSensorEvent;
import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.List;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.versioning.AndroidVersions.U;

/** Shadow for VirtualSensor. */
@Implements(value = VirtualSensor.class, minSdk = U.SDK_INT)
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
    return ImmutableList.copyOf(sentEvents);
  }

  public void clearSentEvents() {
    sentEvents.clear();
  }

  void setDeviceId(int deviceId) {
    this.deviceId = deviceId;
  }
}
