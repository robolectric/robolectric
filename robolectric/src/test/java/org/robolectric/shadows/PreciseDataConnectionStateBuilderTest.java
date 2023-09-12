package org.robolectric.shadows;

import static android.telephony.AccessNetworkConstants.TRANSPORT_TYPE_WWAN;
import static com.google.common.truth.Truth.assertThat;

import android.os.Build;
import android.telephony.DataFailCause;
import android.telephony.PreciseDataConnectionState;
import android.telephony.TelephonyManager;
import android.telephony.data.ApnSetting;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

/** Test for {@link PreciseDataConnectionStateBuilder} */
@RunWith(AndroidJUnit4.class)
@Config(minSdk = Build.VERSION_CODES.S)
public class PreciseDataConnectionStateBuilderTest {
  @Test
  public void build_preciseDataConnectionState() {
    ApnSetting apnSetting = new ApnSetting.Builder().setApnName("apnName").build();
    PreciseDataConnectionState state =
        PreciseDataConnectionStateBuilder.newBuilder()
            .setDataState(TelephonyManager.DATA_DISCONNECTED)
            .setNetworkType(TelephonyManager.NETWORK_TYPE_LTE)
            .setTransportType(TRANSPORT_TYPE_WWAN)
            .setApnSetting(apnSetting)
            .setDataFailCause(DataFailCause.IMEI_NOT_ACCEPTED)
            .build();

    assertThat(state).isNotNull();
    assertThat(state.getState()).isEqualTo(TelephonyManager.DATA_DISCONNECTED);
    assertThat(state.getNetworkType()).isEqualTo(TelephonyManager.NETWORK_TYPE_LTE);
    assertThat(state.getTransportType()).isEqualTo(TRANSPORT_TYPE_WWAN);
    assertThat(state.getLastCauseCode()).isEqualTo(DataFailCause.IMEI_NOT_ACCEPTED);
    assertThat(state.getApnSetting()).isEqualTo(apnSetting);
  }

  @Test
  public void build_preciseDataConnectionState_nullApnSetting() {
    PreciseDataConnectionState state =
        PreciseDataConnectionStateBuilder.newBuilder()
            .setDataState(TelephonyManager.DATA_DISCONNECTED)
            .setNetworkType(TelephonyManager.NETWORK_TYPE_LTE)
            .setTransportType(TRANSPORT_TYPE_WWAN)
            .setDataFailCause(DataFailCause.IMEI_NOT_ACCEPTED)
            .build();

    assertThat(state).isNotNull();
    assertThat(state.getState()).isEqualTo(TelephonyManager.DATA_DISCONNECTED);
    assertThat(state.getNetworkType()).isEqualTo(TelephonyManager.NETWORK_TYPE_LTE);
    assertThat(state.getTransportType()).isEqualTo(TRANSPORT_TYPE_WWAN);
    assertThat(state.getLastCauseCode()).isEqualTo(DataFailCause.IMEI_NOT_ACCEPTED);
    assertThat(state.getApnSetting()).isNull();
  }
}
