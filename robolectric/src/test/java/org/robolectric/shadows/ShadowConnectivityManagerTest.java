package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.KITKAT;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.M;
import static android.os.Build.VERSION_CODES.N;
import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.robolectric.Shadows.shadowOf;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;

@RunWith(AndroidJUnit4.class)
public class ShadowConnectivityManagerTest {
  private ConnectivityManager connectivityManager;
  private ShadowNetworkInfo shadowOfActiveNetworkInfo;

  @Before
  public void setUp() throws Exception {
    connectivityManager =
        (ConnectivityManager)
            getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
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
    shadowOf(connectivityManager).addNetwork(vpnNetwork, vpnNetworkInfo);

    NetworkInfo returnedNetworkInfo = connectivityManager.getNetworkInfo(vpnNetwork);
    assertThat(returnedNetworkInfo).isSameAs(vpnNetworkInfo);
  }

  @Test @Config(minSdk = LOLLIPOP)
  public void getNetworkInfo_shouldNotReturnRemovedNetwork() throws Exception {
    Network wifiNetwork = ShadowNetwork.newInstance(ShadowConnectivityManager.NET_ID_WIFI);
    shadowOf(connectivityManager).removeNetwork(wifiNetwork);

    NetworkInfo returnedNetworkInfo = connectivityManager.getNetworkInfo(wifiNetwork);
    assertThat(returnedNetworkInfo).isNull();
  }

  @Test
  public void setConnectionType_shouldReturnTypeCorrectly() {
    shadowOfActiveNetworkInfo.setConnectionType(ConnectivityManager.TYPE_MOBILE);
    assertThat(shadowOfActiveNetworkInfo.getType()).isEqualTo(ConnectivityManager.TYPE_MOBILE);

    shadowOfActiveNetworkInfo.setConnectionType(ConnectivityManager.TYPE_WIFI);
    assertThat(shadowOfActiveNetworkInfo.getType()).isEqualTo(ConnectivityManager.TYPE_WIFI);
  }

  @Test
  public void shouldGetAndSetBackgroundDataSetting() throws Exception {
    assertThat(connectivityManager.getBackgroundDataSetting()).isFalse();
    shadowOf(connectivityManager).setBackgroundDataSetting(true);
    assertThat(connectivityManager.getBackgroundDataSetting()).isTrue();
  }

  @Test
  public void setActiveNetworkInfo_shouldSetActiveNetworkInfo() throws Exception {
    shadowOf(connectivityManager).setActiveNetworkInfo(null);
    assertThat(connectivityManager.getActiveNetworkInfo()).isNull();
    shadowOf(connectivityManager)
        .setActiveNetworkInfo(
            ShadowNetworkInfo.newInstance(
                null,
                ConnectivityManager.TYPE_MOBILE_HIPRI,
                TelephonyManager.NETWORK_TYPE_EDGE,
                true,
                NetworkInfo.State.DISCONNECTED));

    NetworkInfo info = connectivityManager.getActiveNetworkInfo();

    assertThat(info.getType()).isEqualTo(ConnectivityManager.TYPE_MOBILE_HIPRI);
    assertThat(info.getSubtype()).isEqualTo(TelephonyManager.NETWORK_TYPE_EDGE);
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
    shadowOf(connectivityManager).setDefaultNetworkActive(false);
    assertThat(connectivityManager.getActiveNetwork()).isNull();
  }

  @Test
  @Config(minSdk = M)
  public void setActiveNetworkInfo_shouldSetActiveNetwork() throws Exception {
    shadowOf(connectivityManager).setActiveNetworkInfo(null);
    assertThat(connectivityManager.getActiveNetworkInfo()).isNull();
    shadowOf(connectivityManager)
        .setActiveNetworkInfo(
            ShadowNetworkInfo.newInstance(
                null,
                ConnectivityManager.TYPE_MOBILE_HIPRI,
                TelephonyManager.NETWORK_TYPE_EDGE,
                true,
                NetworkInfo.State.DISCONNECTED));

    NetworkInfo info = connectivityManager.getActiveNetworkInfo();

    assertThat(info.getType()).isEqualTo(ConnectivityManager.TYPE_MOBILE_HIPRI);
    assertThat(info.getSubtype()).isEqualTo(TelephonyManager.NETWORK_TYPE_EDGE);
    assertThat(info.isAvailable()).isTrue();
    assertThat(info.isConnected()).isFalse();
    assertThat(shadowOf(connectivityManager.getActiveNetwork()).getNetId()).isEqualTo(info.getType());
  }

  @Test
  public void getAllNetworkInfo_shouldReturnAllNetworkInterfaces() throws Exception {
    NetworkInfo[] infos = connectivityManager.getAllNetworkInfo();
    assertThat(infos).asList().hasSize(2);
    assertThat(infos).asList().contains(connectivityManager.getActiveNetworkInfo());

    shadowOf(connectivityManager).setActiveNetworkInfo(null);
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
            true /* isConnected */);
    shadowOf(connectivityManager).setActiveNetworkInfo(networkInfo);

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
    shadowOf(connectivityManager).setDefaultNetworkActive(false);
    assertThat(connectivityManager.getAllNetworkInfo()).isNull();
  }

  @Test @Config(minSdk = LOLLIPOP)
  public void getAllNetworks_shouldReturnAllNetworks() throws Exception {
    Network[] networks = connectivityManager.getAllNetworks();
    assertThat(networks).asList().hasSize(2);
  }

  @Test @Config(minSdk = LOLLIPOP)
  public void getAllNetworks_shouldReturnNoNetworksWhenCleared() throws Exception {
    shadowOf(connectivityManager).clearAllNetworks();
    Network[] networks = connectivityManager.getAllNetworks();
    assertThat(networks).isEmpty();
  }

  @Test @Config(minSdk = LOLLIPOP)
  public void getAllNetworks_shouldReturnAddedNetworks() throws Exception {
    // Let's start clear.
    shadowOf(connectivityManager).clearAllNetworks();

    // Add a "VPN network".
    Network vpnNetwork = ShadowNetwork.newInstance(123);
    NetworkInfo vpnNetworkInfo =
        ShadowNetworkInfo.newInstance(
            NetworkInfo.DetailedState.CONNECTED,
            ConnectivityManager.TYPE_VPN,
            0,
            true,
            NetworkInfo.State.CONNECTED);
    shadowOf(connectivityManager).addNetwork(vpnNetwork, vpnNetworkInfo);

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
    shadowOf(connectivityManager).removeNetwork(wifiNetwork);

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
        shadowOf(connectivityManager).getReportedNetworkConnectivity();
    assertThat(reportedNetworks.size()).isEqualTo(1);
    assertThat(reportedNetworks.get(wifiNetwork)).isTrue();

    // Update the status.
    connectivityManager.reportNetworkConnectivity(wifiNetwork, false);
    reportedNetworks = shadowOf(connectivityManager).getReportedNetworkConnectivity();
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
    assertThat(shadowOf(connectivityManager).getNetworkCallbacks()).isEmpty();
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
    assertThat(shadowOf(connectivityManager).getNetworkCallbacks()).hasSize(1);
  }

  @Test @Config(minSdk = LOLLIPOP)
  public void registerCallback_shouldAddCallback() throws Exception {
    NetworkRequest.Builder builder = new NetworkRequest.Builder();
    ConnectivityManager.NetworkCallback callback = createSimpleCallback();
    connectivityManager.registerNetworkCallback(builder.build(), callback);
    assertThat(shadowOf(connectivityManager).getNetworkCallbacks()).hasSize(1);
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
    assertThat(shadowOf(connectivityManager).getNetworkCallbacks()).hasSize(2);
    connectivityManager.unregisterNetworkCallback(callback2);
    assertThat(shadowOf(connectivityManager).getNetworkCallbacks()).hasSize(1);
    connectivityManager.unregisterNetworkCallback(callback1);
    assertThat(shadowOf(connectivityManager).getNetworkCallbacks()).isEmpty();
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
    shadowOf(connectivityManager)
        .setActiveNetworkInfo(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE));
    assertThat(connectivityManager.isActiveNetworkMetered()).isTrue();
  }

  @Test
  public void isActiveNetworkMetered_nonMobileIsUnmetered() {
    shadowOf(connectivityManager)
        .setActiveNetworkInfo(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI));
    assertThat(connectivityManager.isActiveNetworkMetered()).isFalse();
  }

  @Test
  public void isActiveNetworkMetered_noActiveNetwork() {
    shadowOf(connectivityManager).setActiveNetworkInfo(null);
    assertThat(connectivityManager.isActiveNetworkMetered()).isFalse();
  }

  @Test
  public void isActiveNetworkMetered_noDefaultNetworkActive() {
    shadowOf(connectivityManager).setDefaultNetworkActive(false);
    assertThat(connectivityManager.isActiveNetworkMetered()).isFalse();
  }

  @Test
  @Config(minSdk = M)
  public void bindProcessToNetwork_shouldGetBoundNetworkForProcess() {
    Network network = ShadowNetwork.newInstance(789);
    connectivityManager.bindProcessToNetwork(network);
    assertThat(connectivityManager.getBoundNetworkForProcess()).isSameAs(network);
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void isDefaultNetworkActive_defaultActive() {
    assertThat(shadowOf(connectivityManager).isDefaultNetworkActive()).isTrue();
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void isDefaultNetworkActive_notActive() {
    shadowOf(connectivityManager).setDefaultNetworkActive(false);
    assertThat(shadowOf(connectivityManager).isDefaultNetworkActive()).isFalse();
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

    shadowOf(connectivityManager).setDefaultNetworkActive(true);

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

    shadowOf(connectivityManager).setDefaultNetworkActive(true);

    verify(listener1).onNetworkActive();
    verify(listener2).onNetworkActive();
    // Remove one at the time.
    connectivityManager.removeDefaultNetworkActiveListener(listener2);

    shadowOf(connectivityManager).setDefaultNetworkActive(true);

    verify(listener1, times(2)).onNetworkActive();
    verify(listener2).onNetworkActive();

    connectivityManager.removeDefaultNetworkActiveListener(listener1);

    shadowOf(connectivityManager).setDefaultNetworkActive(true);

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

  @Test
  @Config(minSdk = N)
  public void getCaptivePortalServerUrl_shouldReturnAddedUrl() {
    assertThat(connectivityManager.getCaptivePortalServerUrl()).isEqualTo("http://10.0.0.2");

    shadowOf(connectivityManager).setCaptivePortalServerUrl("http://10.0.0.1");
    assertThat(connectivityManager.getCaptivePortalServerUrl()).isEqualTo("http://10.0.0.1");

    shadowOf(connectivityManager).setCaptivePortalServerUrl("http://10.0.0.2");
    assertThat(connectivityManager.getCaptivePortalServerUrl()).isEqualTo("http://10.0.0.2");
  }

  @Test
  @Config(minSdk = KITKAT)
  public void setAirplaneMode() {
    connectivityManager.setAirplaneMode(false);
    assertThat(
            Settings.Global.getInt(
                getApplicationContext().getContentResolver(), Settings.Global.AIRPLANE_MODE_ON, -1))
        .isEqualTo(0);
    connectivityManager.setAirplaneMode(true);
    assertThat(
            Settings.Global.getInt(
                getApplicationContext().getContentResolver(), Settings.Global.AIRPLANE_MODE_ON, -1))
        .isEqualTo(1);
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void getLinkProperties() {
    Network network = shadowOf(connectivityManager).getActiveNetwork();
    LinkProperties lp = ReflectionHelpers.callConstructor(LinkProperties.class);
    shadowOf(connectivityManager).setLinkProperties(network, lp);

    assertThat(connectivityManager.getLinkProperties(network)).isEqualTo(lp);
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void getLinkProperties_shouldReturnNull() {
    Network network = shadowOf(connectivityManager).getActiveNetwork();
    shadowOf(connectivityManager).setLinkProperties(network, null);

    assertThat(connectivityManager.getLinkProperties(network)).isNull();
  }
}
