package org.robolectric.shadows;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.TestRunners;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.*;
import static org.robolectric.Robolectric.shadowOf;

@RunWith(TestRunners.WithDefaults.class)
public class ConnectivityManagerTest {
  private ConnectivityManager connectivityManager;
  private ShadowNetworkInfo shadowOfActiveNetworkInfo;
  private ShadowConnectivityManager shadowConnectivityManager;

  @Before
  public void setUp() throws Exception {
    connectivityManager = (ConnectivityManager) Robolectric.application.getSystemService(Context.CONNECTIVITY_SERVICE);
    shadowConnectivityManager = shadowOf(connectivityManager);
    shadowOfActiveNetworkInfo = shadowOf(connectivityManager.getActiveNetworkInfo());
  }

  @Test
  public void getActiveNetworkInfo_shouldInitializeItself() {
    assertThat(connectivityManager.getActiveNetworkInfo()).isNotNull();
  }

  @Test
  public void getActiveNetworkInfo_shouldReturnTrueCorrectly() {
    shadowOfActiveNetworkInfo.setConnectionStatus(true);
    assertThat(connectivityManager.getActiveNetworkInfo().isConnectedOrConnecting()).isTrue();
    assertTrue(connectivityManager.getActiveNetworkInfo().isConnected());

    shadowOfActiveNetworkInfo.setConnectionStatus(false);
    assertThat(connectivityManager.getActiveNetworkInfo().isConnectedOrConnecting()).isFalse();
    assertThat(connectivityManager.getActiveNetworkInfo().isConnected()).isFalse();
  }

  @Test
  public void getNetworkInfo_shouldReturnDefaultNetworks() throws Exception {
    NetworkInfo wifi = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
    assertThat(wifi.getDetailedState()).isEqualTo(NetworkInfo.DetailedState.DISCONNECTED);

    NetworkInfo mobile = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
    assertThat(mobile.getDetailedState()).isEqualTo(NetworkInfo.DetailedState.CONNECTED);
  }

  @Test
  public void setConnectionType_shouldReturnTypeCorrectly() {
    shadowOfActiveNetworkInfo.setConnectionType(ConnectivityManager.TYPE_MOBILE);
    assertThat(ConnectivityManager.TYPE_MOBILE).isEqualTo(shadowOfActiveNetworkInfo.getType());

    shadowOfActiveNetworkInfo.setConnectionType(ConnectivityManager.TYPE_WIFI);
    assertThat(ConnectivityManager.TYPE_WIFI).isEqualTo(shadowOfActiveNetworkInfo.getType());
  }

  @Test
  public void shouldGetAndSetBackgroundDataSetting() throws Exception {
    assertThat(connectivityManager.getBackgroundDataSetting()).isFalse();
    shadowConnectivityManager.setBackgroundDataSetting(true);
    assertThat(connectivityManager.getBackgroundDataSetting()).isTrue();
  }

  @Test
  public void setActiveNetworkInfo_shouldSetActiveNetworkInfo() throws Exception {
    shadowConnectivityManager.setActiveNetworkInfo(null);
    assertThat(connectivityManager.getActiveNetworkInfo()).isNull();
    shadowConnectivityManager.setActiveNetworkInfo(ShadowNetworkInfo.newInstance(null,
        ConnectivityManager.TYPE_MOBILE_HIPRI,
        TelephonyManager.NETWORK_TYPE_EDGE, true, false));

    NetworkInfo info = connectivityManager.getActiveNetworkInfo();

    assertThat(ConnectivityManager.TYPE_MOBILE_HIPRI).isEqualTo(info.getType());
    assertThat(TelephonyManager.NETWORK_TYPE_EDGE).isEqualTo(info.getSubtype());
    assertThat(info.isAvailable()).isTrue();
    assertThat(info.isConnected()).isFalse();
  }

  @Test
  public void getAllNetworkInfo_shouldReturnAllNetworkInterfaces() throws Exception {
    NetworkInfo[] infos = connectivityManager.getAllNetworkInfo();
    assertThat(infos).hasSize(2);
    assertThat(connectivityManager.getActiveNetworkInfo()).isSameAs(infos[0]);

    shadowConnectivityManager.setActiveNetworkInfo(null);
    assertThat(connectivityManager.getAllNetworkInfo()).isEmpty();
  }

  @Test
  public void getNetworkPreference_shouldGetDefaultValue() throws Exception {
    assertThat(connectivityManager.getNetworkPreference()).isEqualTo(ConnectivityManager.DEFAULT_NETWORK_PREFERENCE);
  }

  @Test
  public void setNetworkPreference_shouldSetDefaultValue() throws Exception {
    connectivityManager.setNetworkPreference(ConnectivityManager.TYPE_MOBILE);
    assertThat(connectivityManager.getNetworkPreference()).isEqualTo(connectivityManager.getNetworkPreference());
    connectivityManager.setNetworkPreference(ConnectivityManager.TYPE_WIFI);
    assertThat(connectivityManager.getNetworkPreference()).isEqualTo(ConnectivityManager.TYPE_WIFI);
  }
}
