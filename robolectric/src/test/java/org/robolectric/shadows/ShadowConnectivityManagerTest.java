package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.M;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.robolectric.Shadows.shadowOf;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.net.NetworkRequest;
import android.telephony.TelephonyManager;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;

@RunWith(RobolectricTestRunner.class)
public class ShadowConnectivityManagerTest {
  private ConnectivityManager connectivityManager;
  private ShadowNetworkInfo shadowOfActiveNetworkInfo;
  private ShadowConnectivityManager shadowConnectivityManager;

  @Before
  public void setUp() throws Exception {
    connectivityManager =
        (ConnectivityManager)
            RuntimeEnvironment.application.getSystemService(Context.CONNECTIVITY_SERVICE);
    shadowConnectivityManager = shadowOf(connectivityManager);
    shadowOfActiveNetworkInfo = shadowOf(connectivityManager.getActiveNetworkInfo());
  }

  @Test
  public void getActiveNetworkInfo_shouldInitializeItself() {
    assertThat(connectivityManager.getActiveNetworkInfo()).isNotNull();
  }

  @Test
  public void getActiveNetworkInfo_shouldReturnTrueCorrectly() {
    shadowOfActiveNetworkInfo.setConnectionStatus(NetworkInfo.State.CONNECTED);
    assertThat(connectivityManager.getActiveNetworkInfo().isConnectedOrConnecting()).isTrue();
    assertThat(connectivityManager.getActiveNetworkInfo().isConnected()).isTrue();

    shadowOfActiveNetworkInfo.setConnectionStatus(NetworkInfo.State.CONNECTING);
    assertThat(connectivityManager.getActiveNetworkInfo().isConnectedOrConnecting()).isTrue();
    assertThat(connectivityManager.getActiveNetworkInfo().isConnected()).isFalse();

    shadowOfActiveNetworkInfo.setConnectionStatus(NetworkInfo.State.DISCONNECTED);
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

  @Test @Config(minSdk = LOLLIPOP)
  public void getNetworkInfo_shouldReturnSomeForAllNetworks() throws Exception {
    Network[] allNetworks = connectivityManager.getAllNetworks();
    for (Network network: allNetworks) {
      NetworkInfo networkInfo = connectivityManager.getNetworkInfo(network);
      assertThat(networkInfo).isNotNull();
    }
  }

  @Test @Config(minSdk = LOLLIPOP)
  public void getNetworkInfo_shouldReturnAddedNetwork() throws Exception {
    Network vpnNetwork = ShadowNetwork.newInstance(123);
    NetworkInfo vpnNetworkInfo =
        ShadowNetworkInfo.newInstance(
            NetworkInfo.DetailedState.CONNECTED,
            ConnectivityManager.TYPE_VPN,
            0,
            true,
            NetworkInfo.State.CONNECTED);
    shadowConnectivityManager.addNetwork(vpnNetwork, vpnNetworkInfo);

    NetworkInfo returnedNetworkInfo = connectivityManager.getNetworkInfo(vpnNetwork);
    assertThat(returnedNetworkInfo).isSameAs(vpnNetworkInfo);
  }

  @Test @Config(minSdk = LOLLIPOP)
  public void getNetworkInfo_shouldNotReturnRemovedNetwork() throws Exception {
    Network wifiNetwork = ShadowNetwork.newInstance(ShadowConnectivityManager.NET_ID_WIFI);
    shadowConnectivityManager.removeNetwork(wifiNetwork);

    NetworkInfo returnedNetworkInfo = connectivityManager.getNetworkInfo(wifiNetwork);
    assertThat(returnedNetworkInfo).isNull();
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
    shadowConnectivityManager.setActiveNetworkInfo(
        ShadowNetworkInfo.newInstance(
            null,
            ConnectivityManager.TYPE_MOBILE_HIPRI,
            TelephonyManager.NETWORK_TYPE_EDGE,
            true,
            NetworkInfo.State.DISCONNECTED));

    NetworkInfo info = connectivityManager.getActiveNetworkInfo();

    assertThat(ConnectivityManager.TYPE_MOBILE_HIPRI).isEqualTo(info.getType());
    assertThat(TelephonyManager.NETWORK_TYPE_EDGE).isEqualTo(info.getSubtype());
    assertThat(info.isAvailable()).isTrue();
    assertThat(info.isConnected()).isFalse();
  }

  @Test
  @Config(minSdk = M)
  public void getActiveNetwork_shouldInitializeItself() {
    assertThat(connectivityManager.getActiveNetwork()).isNotNull();
  }

  @Test
  @Config(minSdk = M)
  public void getActiveNetwork_nullIfNetworkNotActive() {
    shadowConnectivityManager.setDefaultNetworkActive(false);
    assertThat(connectivityManager.getActiveNetwork()).isNull();
  }

  @Test
  @Config(minSdk = M)
  public void setActiveNetworkInfo_shouldSetActiveNetwork() throws Exception {
    shadowConnectivityManager.setActiveNetworkInfo(null);
    assertThat(connectivityManager.getActiveNetworkInfo()).isNull();
    shadowConnectivityManager.setActiveNetworkInfo(
        ShadowNetworkInfo.newInstance(
            null,
            ConnectivityManager.TYPE_MOBILE_HIPRI,
            TelephonyManager.NETWORK_TYPE_EDGE,
            true,
            NetworkInfo.State.DISCONNECTED));

    NetworkInfo info = connectivityManager.getActiveNetworkInfo();

    assertThat(ConnectivityManager.TYPE_MOBILE_HIPRI).isEqualTo(info.getType());
    assertThat(TelephonyManager.NETWORK_TYPE_EDGE).isEqualTo(info.getSubtype());
    assertThat(info.isAvailable()).isTrue();
    assertThat(info.isConnected()).isFalse();
    assertThat(shadowOf(connectivityManager.getActiveNetwork()).getNetId()).isEqualTo(info.getType());
  }

  @Test
  public void getAllNetworkInfo_shouldReturnAllNetworkInterfaces() throws Exception {
    NetworkInfo[] infos = connectivityManager.getAllNetworkInfo();
    assertThat(infos).asList().hasSize(2);
    assertThat(infos).asList().contains(connectivityManager.getActiveNetworkInfo());

    shadowConnectivityManager.setActiveNetworkInfo(null);
    assertThat(connectivityManager.getAllNetworkInfo()).isEmpty();
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void getAllNetworkInfo_shouldEqualGetAllNetworks() throws Exception {
    // Update the active network so that we're no longer in the default state.
    NetworkInfo networkInfo =
        ShadowNetworkInfo.newInstance(
            NetworkInfo.DetailedState.CONNECTED,
            ConnectivityManager.TYPE_WIFI,
            0 /* subType */,
            true /* isAvailable */,
            State.CONNECTED);
    shadowConnectivityManager.setActiveNetworkInfo(networkInfo);

    // Verify that getAllNetworks and getAllNetworkInfo match.
    Network[] networks = connectivityManager.getAllNetworks();
    NetworkInfo[] networkInfos = new NetworkInfo[networks.length];
    for (int i = 0; i < networks.length; i++) {
      networkInfos[i] = connectivityManager.getNetworkInfo(networks[i]);
      assertThat(connectivityManager.getAllNetworkInfo()).asList().contains(networkInfos[i]);
    }
    assertThat(networkInfos).hasLength(connectivityManager.getAllNetworkInfo().length);
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void getAllNetworkInfo_nullIfNetworkNotActive() {
    shadowConnectivityManager.setDefaultNetworkActive(false);
    assertThat(connectivityManager.getAllNetworkInfo()).isNull();
  }

  @Test @Config(minSdk = LOLLIPOP)
  public void getAllNetworks_shouldReturnAllNetworks() throws Exception {
    Network[] networks = connectivityManager.getAllNetworks();
    assertThat(networks).asList().hasSize(2);
  }

  @Test @Config(minSdk = LOLLIPOP)
  public void getAllNetworks_shouldReturnNoNetworksWhenCleared() throws Exception {
    shadowConnectivityManager.clearAllNetworks();
    Network[] networks = connectivityManager.getAllNetworks();
    assertThat(networks).isEmpty();
  }

  @Test @Config(minSdk = LOLLIPOP)
  public void getAllNetworks_shouldReturnAddedNetworks() throws Exception {
    // Let's start clear.
    shadowConnectivityManager.clearAllNetworks();

    // Add a "VPN network".
    Network vpnNetwork = ShadowNetwork.newInstance(123);
    NetworkInfo vpnNetworkInfo =
        ShadowNetworkInfo.newInstance(
            NetworkInfo.DetailedState.CONNECTED,
            ConnectivityManager.TYPE_VPN,
            0,
            true,
            NetworkInfo.State.CONNECTED);
    shadowConnectivityManager.addNetwork(vpnNetwork, vpnNetworkInfo);

    Network[] networks = connectivityManager.getAllNetworks();
    assertThat(networks).asList().hasSize(1);

    Network returnedNetwork = networks[0];
    assertThat(returnedNetwork).isSameAs(vpnNetwork);

    NetworkInfo returnedNetworkInfo = connectivityManager.getNetworkInfo(returnedNetwork);
    assertThat(returnedNetworkInfo).isSameAs(vpnNetworkInfo);
  }

  @Test @Config(minSdk = LOLLIPOP)
  public void getAllNetworks_shouldNotReturnRemovedNetworks() throws Exception {
    Network wifiNetwork = ShadowNetwork.newInstance(ShadowConnectivityManager.NET_ID_WIFI);
    shadowConnectivityManager.removeNetwork(wifiNetwork);

    Network[] networks = connectivityManager.getAllNetworks();
    assertThat(networks).asList().hasSize(1);

    Network returnedNetwork = networks[0];
    ShadowNetwork shadowReturnedNetwork = shadowOf(returnedNetwork);
    assertThat(shadowReturnedNetwork.getNetId()).isNotEqualTo(ShadowConnectivityManager.NET_ID_WIFI);
  }

  @Test
  public void getNetworkPreference_shouldGetDefaultValue() throws Exception {
    assertThat(connectivityManager.getNetworkPreference()).isEqualTo(ConnectivityManager.DEFAULT_NETWORK_PREFERENCE);
  }

  @Test
  @Config(minSdk = M)
  public void getReportedNetworkConnectivity() throws Exception {
    Network wifiNetwork = ShadowNetwork.newInstance(ShadowConnectivityManager.NET_ID_WIFI);
    connectivityManager.reportNetworkConnectivity(wifiNetwork, true);

    Map<Network, Boolean> reportedNetworks =
        shadowConnectivityManager.getReportedNetworkConnectivity();
    assertThat(reportedNetworks.size()).isEqualTo(1);
    assertThat(reportedNetworks.get(wifiNetwork)).isTrue();

    // Update the status.
    connectivityManager.reportNetworkConnectivity(wifiNetwork, false);
    reportedNetworks = shadowConnectivityManager.getReportedNetworkConnectivity();
    assertThat(reportedNetworks.size()).isEqualTo(1);
    assertThat(reportedNetworks.get(wifiNetwork)).isFalse();
  }

  @Test
  public void setNetworkPreference_shouldSetDefaultValue() throws Exception {
    connectivityManager.setNetworkPreference(ConnectivityManager.TYPE_MOBILE);
    assertThat(connectivityManager.getNetworkPreference()).isEqualTo(connectivityManager.getNetworkPreference());
    connectivityManager.setNetworkPreference(ConnectivityManager.TYPE_WIFI);
    assertThat(connectivityManager.getNetworkPreference()).isEqualTo(ConnectivityManager.TYPE_WIFI);
  }

  @Test @Config(minSdk = LOLLIPOP)
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

  @Test
  @Config(minSdk = LOLLIPOP)
  public void requestNetwork_shouldAddCallback() throws Exception {
    NetworkRequest.Builder builder = new NetworkRequest.Builder();
    ConnectivityManager.NetworkCallback callback = createSimpleCallback();
    connectivityManager.requestNetwork(builder.build(), callback);
    assertThat(shadowConnectivityManager.getNetworkCallbacks()).hasSize(1);
  }

  @Test @Config(minSdk = LOLLIPOP)
  public void registerCallback_shouldAddCallback() throws Exception {
    NetworkRequest.Builder builder = new NetworkRequest.Builder();
    ConnectivityManager.NetworkCallback callback = createSimpleCallback();
    connectivityManager.registerNetworkCallback(builder.build(), callback);
    assertEquals(1, shadowConnectivityManager.getNetworkCallbacks().size());
  }

  @Test @Config(minSdk = LOLLIPOP)
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

  @Test(expected=IllegalArgumentException.class) @Config(minSdk = LOLLIPOP)
  public void unregisterCallback_shouldNotAllowNullCallback() throws Exception {
    // Verify that exception is thrown.
    connectivityManager.unregisterNetworkCallback((ConnectivityManager.NetworkCallback) null);
  }

  @Test
  public void isActiveNetworkMetered_defaultsToTrue() {
    assertThat(connectivityManager.isActiveNetworkMetered()).isTrue();
  }

  @Test
  public void isActiveNetworkMetered_mobileIsMetered() {
    shadowConnectivityManager.setActiveNetworkInfo(
        connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE));
    assertThat(connectivityManager.isActiveNetworkMetered()).isTrue();
  }

  @Test
  public void isActiveNetworkMetered_nonMobileIsUnmetered() {
    shadowConnectivityManager.setActiveNetworkInfo(
        connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI));
    assertThat(connectivityManager.isActiveNetworkMetered()).isFalse();
  }

  @Test
  public void isActiveNetworkMetered_noActiveNetwork() {
    shadowConnectivityManager.setActiveNetworkInfo(null);
    assertThat(connectivityManager.isActiveNetworkMetered()).isFalse();
  }

  @Test
  public void isActiveNetworkMetered_noDefaultNetworkActive() {
    shadowConnectivityManager.setDefaultNetworkActive(false);
    assertThat(connectivityManager.isActiveNetworkMetered()).isFalse();
  }

  @Test
  @Config(minSdk = M)
  public void bindProcessToNetwork_should() {
    Network network = ShadowNetwork.newInstance(789);
    connectivityManager.bindProcessToNetwork(network);
    assertThat(connectivityManager.getBoundNetworkForProcess()).isSameAs(network);
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void isDefaultNetworkActive_defaultActive() {
    assertThat(shadowConnectivityManager.isDefaultNetworkActive()).isTrue();
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void isDefaultNetworkActive_notActive() {
    shadowConnectivityManager.setDefaultNetworkActive(false);
    assertThat(shadowConnectivityManager.isDefaultNetworkActive()).isFalse();
  }

  private static ConnectivityManager.OnNetworkActiveListener createSimpleOnNetworkActiveListener() {
    return new ConnectivityManager.OnNetworkActiveListener() {
      @Override
      public void onNetworkActive() {}
    };
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void addDefaultNetworkActiveListener_shouldAddListener() throws Exception {
    ConnectivityManager.OnNetworkActiveListener listener1 =
        spy(createSimpleOnNetworkActiveListener());
    ConnectivityManager.OnNetworkActiveListener listener2 =
        spy(createSimpleOnNetworkActiveListener());
    connectivityManager.addDefaultNetworkActiveListener(listener1);
    connectivityManager.addDefaultNetworkActiveListener(listener2);

    shadowConnectivityManager.setDefaultNetworkActive(true);

    verify(listener1).onNetworkActive();
    verify(listener2).onNetworkActive();
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void removeDefaultNetworkActiveListener_shouldRemoveListeners() throws Exception {
    // Add two different callbacks.
    ConnectivityManager.OnNetworkActiveListener listener1 =
        spy(createSimpleOnNetworkActiveListener());
    ConnectivityManager.OnNetworkActiveListener listener2 =
        spy(createSimpleOnNetworkActiveListener());
    connectivityManager.addDefaultNetworkActiveListener(listener1);
    connectivityManager.addDefaultNetworkActiveListener(listener2);

    shadowConnectivityManager.setDefaultNetworkActive(true);

    verify(listener1).onNetworkActive();
    verify(listener2).onNetworkActive();
    // Remove one at the time.
    connectivityManager.removeDefaultNetworkActiveListener(listener2);

    shadowConnectivityManager.setDefaultNetworkActive(true);

    verify(listener1, times(2)).onNetworkActive();
    verify(listener2).onNetworkActive();

    connectivityManager.removeDefaultNetworkActiveListener(listener1);

    shadowConnectivityManager.setDefaultNetworkActive(true);

    verify(listener1, times(2)).onNetworkActive();
    verify(listener2).onNetworkActive();
  }

  @Test(expected = IllegalArgumentException.class)
  @Config(minSdk = LOLLIPOP)
  public void removeDefaultNetworkActiveListener_shouldNotAllowNullListener() throws Exception {
    // Verify that exception is thrown.
    connectivityManager.removeDefaultNetworkActiveListener(null);
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void getNetworkCapabilities() throws Exception {
    NetworkCapabilities nc = new NetworkCapabilities(null);
    ReflectionHelpers.callInstanceMethod(
        nc,
        "addCapability",
        ClassParameter.from(int.class, NetworkCapabilities.NET_CAPABILITY_MMS));

    shadowOf(connectivityManager).setNetworkCapabilities(
        shadowOf(connectivityManager).getActiveNetwork(), nc);

    assertThat(
            shadowOf(connectivityManager)
                .getNetworkCapabilities(shadowOf(connectivityManager).getActiveNetwork())
                .hasCapability(NetworkCapabilities.NET_CAPABILITY_MMS))
        .isTrue();
  }
}

