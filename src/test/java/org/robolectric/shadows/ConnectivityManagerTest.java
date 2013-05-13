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
  public void getConnectivityManagerShouldNotBeNull() {
    assertNotNull(connectivityManager);
    assertNotNull(connectivityManager.getActiveNetworkInfo());
  }

  @Test
  public void networkInfoShouldReturnTrueCorrectly() {
    shadowOfActiveNetworkInfo.setConnectionStatus(true);

    assertTrue(connectivityManager.getActiveNetworkInfo().isConnectedOrConnecting());
    assertTrue(connectivityManager.getActiveNetworkInfo().isConnected());
  }

  @Test
  public void getNetworkInfoShouldReturnAssignedValue() throws Exception {
    NetworkInfo networkInfo = ShadowNetworkInfo.newInstance(NetworkInfo.DetailedState.CONNECTING);
    shadowConnectivityManager.setNetworkInfo(ConnectivityManager.TYPE_WIFI, networkInfo);
    NetworkInfo actual = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
    assertThat(actual).isSameAs(networkInfo);
    assertThat(actual.getDetailedState()).isEqualTo(NetworkInfo.DetailedState.CONNECTING);
  }

  @Test
  public void networkInfoShouldReturnFalseCorrectly() {
    shadowOfActiveNetworkInfo.setConnectionStatus(false);

    assertFalse(connectivityManager.getActiveNetworkInfo().isConnectedOrConnecting());
    assertFalse(connectivityManager.getActiveNetworkInfo().isConnected());
  }

  @Test
  public void networkInfoShouldReturnTypeCorrectly(){
    shadowOfActiveNetworkInfo.setConnectionType(ConnectivityManager.TYPE_MOBILE);
    assertEquals(ConnectivityManager.TYPE_MOBILE, shadowOfActiveNetworkInfo.getType());

    shadowOfActiveNetworkInfo.setConnectionType(ConnectivityManager.TYPE_WIFI);
    assertEquals(ConnectivityManager.TYPE_WIFI, shadowOfActiveNetworkInfo.getType());
  }

  @Test
  public void shouldGetAndSetBackgroundDataSetting() throws Exception {
    assertFalse(connectivityManager.getBackgroundDataSetting());
    shadowConnectivityManager.setBackgroundDataSetting(true);
    assertTrue(connectivityManager.getBackgroundDataSetting());
  }

  @Test
  public void shouldSetActiveNetworkInfo() throws Exception {
    shadowConnectivityManager.setActiveNetworkInfo(null);
    assertNull(connectivityManager.getActiveNetworkInfo());
    shadowConnectivityManager.setActiveNetworkInfo(ShadowNetworkInfo.newInstance(null,
        ConnectivityManager.TYPE_MOBILE_HIPRI,
        TelephonyManager.NETWORK_TYPE_EDGE, true, false));

    NetworkInfo info = connectivityManager.getActiveNetworkInfo();

    assertEquals(ConnectivityManager.TYPE_MOBILE_HIPRI, info.getType());
    assertEquals(TelephonyManager.NETWORK_TYPE_EDGE, info.getSubtype());
    assertTrue(info.isAvailable());
    assertFalse(info.isConnected());
  }

  @Test
  public void shouldGetAllNetworkInfo() throws Exception {
    NetworkInfo[] infos = connectivityManager.getAllNetworkInfo();
    assertNotNull(infos);
    assertEquals(1, infos.length);
    assertSame(connectivityManager.getActiveNetworkInfo(), infos[0]);

    shadowConnectivityManager.setActiveNetworkInfo(null);
    assertEquals(0, connectivityManager.getAllNetworkInfo().length);
  }

  @Test
  public void shouldGetDefaultNetworkPreference() throws Exception {
    assertEquals(connectivityManager.getNetworkPreference(), ConnectivityManager.DEFAULT_NETWORK_PREFERENCE);
  }

  @Test
  public void shouldGetSetNetworkPreference() throws Exception {
    connectivityManager.setNetworkPreference(ConnectivityManager.TYPE_MOBILE);
    assertEquals(connectivityManager.getNetworkPreference(), connectivityManager.getNetworkPreference());
    connectivityManager.setNetworkPreference(ConnectivityManager.TYPE_WIFI);
    assertEquals(connectivityManager.getNetworkPreference(), ConnectivityManager.TYPE_WIFI);
  }
}
