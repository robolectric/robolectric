package org.robolectric.shadows;

import static android.hardware.Sensor.TYPE_ACCELEROMETER;
import static android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE;
import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static com.google.common.truth.Truth.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import android.app.PendingIntent;
import android.companion.virtual.VirtualDeviceManager;
import android.companion.virtual.VirtualDeviceManager.VirtualDevice;
import android.companion.virtual.VirtualDeviceParams;
import android.companion.virtual.sensor.VirtualSensorCallback;
import android.companion.virtual.sensor.VirtualSensorConfig;
import android.content.Context;
import android.content.Intent;
import java.time.Duration;
import java.util.function.IntConsumer;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.shadows.ShadowVirtualDeviceManager.ShadowVirtualDevice;

/** Unit test for ShadowVirtualDeviceManager and ShadowVirtualDevice. */
@Config(minSdk = UPSIDE_DOWN_CAKE)
@RunWith(RobolectricTestRunner.class)
public class ShadowVirtualDeviceManagerTest {

  @Rule public final MockitoRule mockito = MockitoJUnit.rule();

  private Context context;
  private VirtualDeviceManager virtualDeviceManager;
  @Mock private IntConsumer mockCallback;

  @Before
  public void setUp() throws Exception {
    virtualDeviceManager =
        (VirtualDeviceManager)
            getApplicationContext().getSystemService(Context.VIRTUAL_DEVICE_SERVICE);
    context = getApplicationContext();
  }

  @Test
  public void testCreateVirtualDevice() {
    assertThat(virtualDeviceManager.getVirtualDevices()).isEmpty();
    virtualDeviceManager.createVirtualDevice(
        0, new VirtualDeviceParams.Builder().setName("foo").build());

    assertThat(virtualDeviceManager.getVirtualDevices()).hasSize(1);
    assertThat(virtualDeviceManager.getVirtualDevices().get(0).getName()).isEqualTo("foo");
  }

  @Test
  public void testIsValidVirtualDeviceId() {
    VirtualDevice virtualDevice =
        virtualDeviceManager.createVirtualDevice(
            0, new VirtualDeviceParams.Builder().setName("foo").build());

    assertThat(virtualDeviceManager.isValidVirtualDeviceId(virtualDevice.getDeviceId())).isTrue();

    // Random virtual device id should be false
    assertThat(virtualDeviceManager.isValidVirtualDeviceId(999)).isFalse();
  }

  @Test
  public void testGetDevicePolicy() {
    VirtualDevice virtualDevice =
        virtualDeviceManager.createVirtualDevice(
            0,
            new VirtualDeviceParams.Builder()
                .setDevicePolicy(
                    VirtualDeviceParams.POLICY_TYPE_SENSORS,
                    VirtualDeviceParams.DEVICE_POLICY_CUSTOM)
                .build());

    assertThat(
            virtualDeviceManager.getDevicePolicy(
                virtualDevice.getDeviceId(), VirtualDeviceParams.POLICY_TYPE_SENSORS))
        .isEqualTo(VirtualDeviceParams.DEVICE_POLICY_CUSTOM);
  }

  @Test
  public void testLaunchIntentOnDevice() {
    PendingIntent pendingIntent =
        PendingIntent.getActivity(context, 0, new Intent(), PendingIntent.FLAG_IMMUTABLE);
    VirtualDevice virtualDevice =
        virtualDeviceManager.createVirtualDevice(0, new VirtualDeviceParams.Builder().build());
    ShadowVirtualDevice shadowVirtualDevice = Shadow.extract(virtualDevice);
    shadowVirtualDevice.setPendingIntentCallbackResultCode(
        VirtualDeviceManager.LAUNCH_FAILURE_NO_ACTIVITY);

    virtualDevice.launchPendingIntent(0, pendingIntent, context.getMainExecutor(), mockCallback);
    Robolectric.flushForegroundThreadScheduler();

    verify(mockCallback).accept(VirtualDeviceManager.LAUNCH_FAILURE_NO_ACTIVITY);
    assertThat(shadowVirtualDevice.getLastLaunchedPendingIntent()).isEqualTo(pendingIntent);
  }

  @Test
  public void testGetVirtualSensorList() {
    VirtualDevice virtualDevice =
        virtualDeviceManager.createVirtualDevice(
            0,
            new VirtualDeviceParams.Builder()
                .setDevicePolicy(
                    VirtualDeviceParams.POLICY_TYPE_SENSORS,
                    VirtualDeviceParams.DEVICE_POLICY_CUSTOM)
                .addVirtualSensorConfig(
                    new VirtualSensorConfig.Builder(TYPE_ACCELEROMETER, "accel").build())
                .setVirtualSensorCallback(
                    context.getMainExecutor(), mock(VirtualSensorCallback.class))
                .build());

    assertThat(virtualDevice.getVirtualSensorList()).hasSize(1);
    assertThat(virtualDevice.getVirtualSensorList().get(0).getName()).isEqualTo("accel");
    assertThat(virtualDevice.getVirtualSensorList().get(0).getType()).isEqualTo(TYPE_ACCELEROMETER);
    assertThat(virtualDevice.getVirtualSensorList().get(0).getDeviceId())
        .isEqualTo(virtualDevice.getDeviceId());
  }

  @Test
  public void testGetSensorCallbacks() {
    VirtualSensorCallback mockVirtualSensorCallback = mock(VirtualSensorCallback.class);
    VirtualDevice virtualDevice =
        virtualDeviceManager.createVirtualDevice(
            0,
            new VirtualDeviceParams.Builder()
                .setDevicePolicy(
                    VirtualDeviceParams.POLICY_TYPE_SENSORS,
                    VirtualDeviceParams.DEVICE_POLICY_CUSTOM)
                .addVirtualSensorConfig(
                    new VirtualSensorConfig.Builder(TYPE_ACCELEROMETER, "accel").build())
                .setVirtualSensorCallback(context.getMainExecutor(), mockVirtualSensorCallback)
                .build());

    ShadowVirtualDevice shadowDevice = Shadow.extract(virtualDevice);
    VirtualSensorCallback retrievedCallback = shadowDevice.getVirtualSensorCallback();

    retrievedCallback.onConfigurationChanged(
        virtualDevice.getVirtualSensorList().get(0), true, Duration.ZERO, Duration.ZERO);

    assertThat(retrievedCallback).isNotNull();
    verify(mockVirtualSensorCallback).onConfigurationChanged(any(), eq(true), any(), any());
  }
}
