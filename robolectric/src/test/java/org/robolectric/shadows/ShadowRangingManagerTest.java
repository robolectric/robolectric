package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.BAKLAVA;
import static android.ranging.RangingCapabilities.ENABLED;
import static android.ranging.RangingManager.UWB;
import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.robolectric.RuntimeEnvironment.getApplication;

import android.ranging.DataNotificationConfig;
import android.ranging.RangingCapabilities;
import android.ranging.RangingDevice;
import android.ranging.RangingManager;
import android.ranging.RangingManager.RangingCapabilitiesCallback;
import android.ranging.RangingPreference;
import android.ranging.RangingSession;
import android.ranging.SensorFusionParams;
import android.ranging.SessionConfig;
import android.ranging.oob.DeviceHandle;
import android.ranging.oob.OobInitiatorRangingConfig;
import android.ranging.oob.TransportHandle;
import com.google.common.util.concurrent.MoreExecutors;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadow.api.Shadow;

@RunWith(RobolectricTestRunner.class)
@Config(minSdk = BAKLAVA)
public final class ShadowRangingManagerTest {

  @Rule public final MockitoRule mockito = MockitoJUnit.rule();

  @Mock RangingCapabilitiesCallback rangingCapabilitiesCallback;
  @Mock RangingSession.Callback rangingSessionCallback;

  private static final RangingCapabilities RANGING_CAPABILITIES =
      new RangingCapabilitiesBuilder().addAvailability(UWB, ENABLED).build();

  @Test
  public void setCapabilities_capabilitiesReportedAfterRegisteringCallback() throws Exception {
    RangingManager rangingManager = getApplication().getSystemService(RangingManager.class);
    ShadowRangingManager shadowRangingManager = Shadow.extract(rangingManager);

    shadowRangingManager.updateCapabilities(RANGING_CAPABILITIES);
    rangingManager.registerCapabilitiesCallback(
        MoreExecutors.directExecutor(), rangingCapabilitiesCallback);

    verify(rangingCapabilitiesCallback).onRangingCapabilities(RANGING_CAPABILITIES);
  }

  @Test
  public void triggerCapabilitiesCallback_capabilitiesCallbackTriggeredTwice() throws Exception {
    RangingManager rangingManager = getApplication().getSystemService(RangingManager.class);
    ShadowRangingManager shadowRangingManager = Shadow.extract(rangingManager);

    shadowRangingManager.updateCapabilities(RANGING_CAPABILITIES);
    rangingManager.registerCapabilitiesCallback(
        MoreExecutors.directExecutor(), rangingCapabilitiesCallback);
    shadowRangingManager.updateCapabilities(RANGING_CAPABILITIES);

    verify(rangingCapabilitiesCallback, times(2)).onRangingCapabilities(RANGING_CAPABILITIES);
  }

  @Test
  public void getOpenSessionRangingPreference_returnsCorrectPreference() throws Exception {
    RangingManager rangingManager = getApplication().getSystemService(RangingManager.class);
    ShadowRangingManager shadowRangingManager = Shadow.extract(rangingManager);

    RangingSession session =
        rangingManager.createRangingSession(MoreExecutors.directExecutor(), rangingSessionCallback);
    assertThat(session).isNotNull();
    RangingDevice rangingDevice = new RangingDevice.Builder().build();
    DeviceHandle deviceHandle =
        new DeviceHandle.Builder(rangingDevice, mock(TransportHandle.class)).build();
    OobInitiatorRangingConfig oobInitiatorRangingConfig =
        new OobInitiatorRangingConfig.Builder().addDeviceHandle(deviceHandle).build();
    DataNotificationConfig dataNotificationConfig = new DataNotificationConfig.Builder().build();
    SensorFusionParams sensorFusionParams =
        new SensorFusionParams.Builder().setSensorFusionEnabled(false).build();
    SessionConfig sessionConfig =
        new SessionConfig.Builder()
            .setAngleOfArrivalNeeded(true)
            .setDataNotificationConfig(dataNotificationConfig)
            .setSensorFusionParams(sensorFusionParams)
            .build();
    RangingPreference rangingPreference =
        new RangingPreference.Builder(
                RangingPreference.DEVICE_ROLE_INITIATOR, oobInitiatorRangingConfig)
            .setSessionConfig(sessionConfig)
            .build();
    session.start(rangingPreference);

    RangingPreference retrievedRangingPreference =
        shadowRangingManager.getOpenSessionRangingPreference();
    assertThat(retrievedRangingPreference.getDeviceRole())
        .isEqualTo(rangingPreference.getDeviceRole());
  }
}
