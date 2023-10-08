package org.robolectric.shadows;

import android.companion.virtual.VirtualDeviceParams;
import android.companion.virtual.sensor.VirtualSensorCallback;
import android.companion.virtual.sensor.VirtualSensorDirectChannelCallback;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.versioning.AndroidVersions.U;

/** Shadow for VirtualDeviceParams. */
@Implements(
    value = VirtualDeviceParams.class,
    minSdk = U.SDK_INT,
    // TODO: remove when minimum supported compileSdk is >= 34
    isInAndroidSdk = false)
public class ShadowVirtualDeviceParams {

  @RealObject VirtualDeviceParams realObject;

  public VirtualSensorCallback getVirtualSensorCallback() {
    return realObject.getVirtualSensorCallback() == null
        ? null
        : ReflectionHelpers.getField(realObject.getVirtualSensorCallback(), "mCallback");
  }

  public VirtualSensorDirectChannelCallback getVirtualSensorDirectChannelCallback() {
    return realObject.getVirtualSensorCallback() == null
        ? null
        : ReflectionHelpers.getField(
            realObject.getVirtualSensorCallback(), "mDirectChannelCallback");
  }
}
