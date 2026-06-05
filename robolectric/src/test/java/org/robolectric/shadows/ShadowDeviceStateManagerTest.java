package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.VANILLA_ICE_CREAM;
import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.RuntimeEnvironment.getApplication;

import android.hardware.devicestate.DeviceState;
import android.hardware.devicestate.DeviceStateManager;
import android.hardware.devicestate.DeviceStateManager.DeviceStateCallback;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.shadow.api.Shadow;

@RunWith(AndroidJUnit4.class)
@Config(minSdk = VANILLA_ICE_CREAM)
public final class ShadowDeviceStateManagerTest {
  @Test
  public void registerCallback_addProperty_triggersCallback() {
    DeviceStateManager deviceStateManager =
        getApplication().getSystemService(DeviceStateManager.class);
    ShadowDeviceStateManager shadowDeviceStateManager = Shadow.extract(deviceStateManager);
    List<DeviceState> states = new ArrayList<>();
    DeviceStateCallback callback = states::add;

    deviceStateManager.registerCallback(Runnable::run, callback);
    shadowDeviceStateManager.addProperty(1);

    assertThat(states).hasSize(1);
    assertThat(states.get(0).hasProperty(1)).isTrue();
  }

  @Test
  public void unregisterCallback_stopsReceivingUpdates() {
    DeviceStateManager deviceStateManager =
        getApplication().getSystemService(DeviceStateManager.class);
    ShadowDeviceStateManager shadowDeviceStateManager = Shadow.extract(deviceStateManager);
    List<DeviceState> states = new ArrayList<>();
    DeviceStateCallback callback = states::add;

    deviceStateManager.registerCallback(Runnable::run, callback);
    deviceStateManager.unregisterCallback(callback);
    shadowDeviceStateManager.addProperty(1);

    assertThat(states).isEmpty();
  }

  @Test
  public void addProperty_removeProperty_triggersCallback() {
    DeviceStateManager deviceStateManager =
        getApplication().getSystemService(DeviceStateManager.class);
    ShadowDeviceStateManager shadowDeviceStateManager = Shadow.extract(deviceStateManager);
    List<DeviceState> states = new ArrayList<>();
    DeviceStateCallback callback = states::add;

    deviceStateManager.registerCallback(Runnable::run, callback);
    shadowDeviceStateManager.addProperty(1);
    shadowDeviceStateManager.removeProperty(1);

    assertThat(states).hasSize(2);
    assertThat(states.get(0).hasProperty(1)).isTrue();
    assertThat(states.get(1).hasProperty(1)).isFalse();
  }

  @Test
  public void reset_clearsCallbacks() {
    DeviceStateManager deviceStateManager =
        getApplication().getSystemService(DeviceStateManager.class);
    ShadowDeviceStateManager shadowDeviceStateManager = Shadow.extract(deviceStateManager);
    List<DeviceState> states = new ArrayList<>();
    DeviceStateCallback callback = states::add;

    deviceStateManager.registerCallback(Runnable::run, callback);
    ShadowDeviceStateManager.reset();
    shadowDeviceStateManager.addProperty(1);

    assertThat(states).isEmpty();
  }
}
