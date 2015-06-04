package org.robolectric.shadows;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.telephony.TelephonyManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.TestRunners;
import org.robolectric.annotation.Config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;
import static org.robolectric.Shadows.shadowOf;

@RunWith(TestRunners.MultiApiWithDefaults.class)
public class ShadowConnectivityManagerTest {
  private ConnectivityManager connectivityManager;
  private ShadowNetworkInfo shadowOfActiveNetworkInfo;
  private ShadowConnectivityManager shadowConnectivityManager;

  @Before
  public void setUp() throws Exception {
    connectivityManager = (ConnectivityManager) RuntimeEnvironment.application.getSystemService(Context.CONNECTIVITY_SERVICE);
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

  @Test @Config(sdk = 21)
  public void getNetworkCallbacks_shouldHaveEmptyDefault() throws Exception {
    assertEquals(0, shadowConnectivityManager.getNetworkCallbacks().size());
  }

  private static ConnectivityManager.NetworkCallback createSimpleCallback() {
    return new ConnectivityManager.NetworkCallback() {
      @Override
      public void onAvailable(Network network) {}
      @Override
      public void onLost(Network network) {}
    };
  }

  @Test @Config(sdk = 21)
  public void registerCallback_shouldAddCallback() throws Exception {
    NetworkRequest.Builder builder = new NetworkRequest.Builder();
    ConnectivityManager.NetworkCallback callback = createSimpleCallback();
    connectivityManager.registerNetworkCallback(builder.build(), callback);
    assertEquals(1, shadowConnectivityManager.getNetworkCallbacks().size());
  }

  @Test @Config(sdk = 21)
  public void unregisterCallback_shouldRemoveCallbacks() throws Exception {
    NetworkRequest.Builder builder = new NetworkRequest.Builder();
    // Add two different callbacks.
    ConnectivityManager.NetworkCallback callback1 = createSimpleCallback();
    ConnectivityManager.NetworkCallback callback2 = createSimpleCallback();
    connectivityManager.registerNetworkCallback(builder.build(), callback1);
    connectivityManager.registerNetworkCallback(builder.build(), callback2);
    // Remove one at the time.
    assertEquals(2, shadowConnectivityManager.getNetworkCallbacks().size());
    connectivityManager.unregisterNetworkCallback(callback2);
    assertEquals(1, shadowConnectivityManager.getNetworkCallbacks().size());
    connectivityManager.unregisterNetworkCallback(callback1);
    assertEquals(0, shadowConnectivityManager.getNetworkCallbacks().size());
  }

  @Test(expected=IllegalArgumentException.class) @Config(sdk = 21)
  public void unregisterCallback_shouldNotAllowNullCallback() throws Exception {
    // Verify that exception is thrown.
    connectivityManager.unregisterNetworkCallback(null);
  }
}
