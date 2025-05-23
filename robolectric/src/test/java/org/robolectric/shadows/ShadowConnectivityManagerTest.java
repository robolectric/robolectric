package org.robolectric.shadows;

import static android.net.ConnectivityManager.RESTRICT_BACKGROUND_STATUS_DISABLED;
import static android.net.ConnectivityManager.RESTRICT_BACKGROUND_STATUS_ENABLED;
import static android.net.ConnectivityManager.RESTRICT_BACKGROUND_STATUS_WHITELISTED;
import static android.os.Build.VERSION_CODES.M;
import static android.os.Build.VERSION_CODES.N;
import static android.os.Build.VERSION_CODES.O;
import static android.os.Build.VERSION_CODES.S;
import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.robolectric.Shadows.shadowOf;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.net.ProxyInfo;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.Handler;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.Arrays;
import java.util.Map;
import javax.annotation.Nonnull;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;
import org.robolectric.junit.rules.SetSystemPropertyRule;
import org.robolectric.util.ReflectionHelpers;

@RunWith(AndroidJUnit4.class)
public class ShadowConnectivityManagerTest {
  @Rule public SetSystemPropertyRule setSystemPropertyRule = new SetSystemPropertyRule();

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
  public void getNetworkInfo_shouldReturnDefaultNetworks() {
    NetworkInfo wifi = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
    assertThat(wifi.getDetailedState()).isEqualTo(NetworkInfo.DetailedState.DISCONNECTED);

    NetworkInfo mobile = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
    assertThat(mobile.getDetailedState()).isEqualTo(NetworkInfo.DetailedState.CONNECTED);
  }

  @Test
  public void getNetworkInfo_shouldReturnSomeForAllNetworks() {
    Network[] allNetworks = connectivityManager.getAllNetworks();
    for (Network network : allNetworks) {
      NetworkInfo networkInfo = connectivityManager.getNetworkInfo(network);
      assertThat(networkInfo).isNotNull();
    }
  }

  @Test
  public void getNetworkInfo_shouldReturnAddedNetwork() {
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
    assertThat(returnedNetworkInfo).isSameInstanceAs(vpnNetworkInfo);
  }

  @Test
  public void getNetworkInfo_shouldNotReturnRemovedNetwork() {
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
  public void shouldGetAndSetBackgroundDataSetting() {
    assertThat(connectivityManager.getBackgroundDataSetting()).isFalse();
    shadowOf(connectivityManager).setBackgroundDataSetting(true);
    assertThat(connectivityManager.getBackgroundDataSetting()).isTrue();
  }

  @Test
  public void setActiveNetworkInfo_shouldSetActiveNetworkInfo() {
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
  public void setActiveNetworkInfo_shouldSetActiveNetwork() {
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
    assertThat(shadowOf(connectivityManager.getActiveNetwork()).getNetId())
        .isEqualTo(info.getType());
  }

  @Test
  public void getAllNetworkInfo_shouldReturnAllNetworkInterfaces() {
    NetworkInfo[] infos = connectivityManager.getAllNetworkInfo();
    assertThat(infos).asList().hasSize(2);
    assertThat(infos).asList().contains(connectivityManager.getActiveNetworkInfo());

    shadowOf(connectivityManager).setActiveNetworkInfo(null);
    assertThat(connectivityManager.getAllNetworkInfo()).isEmpty();
  }

  @Test
  public void getAllNetworkInfo_shouldEqualGetAllNetworks() {
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
  public void getAllNetworkInfo_nullIfNetworkNotActive() {
    shadowOf(connectivityManager).setDefaultNetworkActive(false);
    assertThat(connectivityManager.getAllNetworkInfo()).isNull();
  }

  @Test
  public void getAllNetworks_shouldReturnAllNetworks() {
    Network[] networks = connectivityManager.getAllNetworks();
    assertThat(networks).asList().hasSize(2);
  }

  @Test
  public void getAllNetworks_shouldReturnNoNetworksWhenCleared() {
    shadowOf(connectivityManager).clearAllNetworks();
    Network[] networks = connectivityManager.getAllNetworks();
    assertThat(networks).isEmpty();
  }

  @Test
  public void getAllNetworks_shouldReturnAddedNetworks() {
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
    assertThat(returnedNetwork).isSameInstanceAs(vpnNetwork);

    NetworkInfo returnedNetworkInfo = connectivityManager.getNetworkInfo(returnedNetwork);
    assertThat(returnedNetworkInfo).isSameInstanceAs(vpnNetworkInfo);
  }

  @Test
  public void getAllNetworks_shouldNotReturnRemovedNetworks() {
    Network wifiNetwork = ShadowNetwork.newInstance(ShadowConnectivityManager.NET_ID_WIFI);
    shadowOf(connectivityManager).removeNetwork(wifiNetwork);

    Network[] networks = connectivityManager.getAllNetworks();
    assertThat(networks).asList().hasSize(1);

    Network returnedNetwork = networks[0];
    ShadowNetwork shadowReturnedNetwork = shadowOf(returnedNetwork);
    assertThat(shadowReturnedNetwork.getNetId())
        .isNotEqualTo(ShadowConnectivityManager.NET_ID_WIFI);
  }

  @Test
  public void getNetworkPreference_shouldGetDefaultValue() {
    assertThat(connectivityManager.getNetworkPreference())
        .isEqualTo(ConnectivityManager.DEFAULT_NETWORK_PREFERENCE);
  }

  @Test
  @Config(minSdk = M)
  public void getReportedNetworkConnectivity() {
    Network wifiNetwork = ShadowNetwork.newInstance(ShadowConnectivityManager.NET_ID_WIFI);
    connectivityManager.reportNetworkConnectivity(wifiNetwork, true);

    Map<Network, Boolean> reportedNetworks =
        shadowOf(connectivityManager).getReportedNetworkConnectivity();
    assertThat(reportedNetworks).hasSize(1);
    assertThat(reportedNetworks.get(wifiNetwork)).isTrue();

    // Update the status.
    connectivityManager.reportNetworkConnectivity(wifiNetwork, false);
    reportedNetworks = shadowOf(connectivityManager).getReportedNetworkConnectivity();
    assertThat(reportedNetworks).hasSize(1);
    assertThat(reportedNetworks.get(wifiNetwork)).isFalse();
  }

  @Test
  public void setNetworkPreference_shouldSetDefaultValue() {
    connectivityManager.setNetworkPreference(ConnectivityManager.TYPE_MOBILE);
    assertThat(connectivityManager.getNetworkPreference())
        .isEqualTo(connectivityManager.getNetworkPreference());
    connectivityManager.setNetworkPreference(ConnectivityManager.TYPE_WIFI);
    assertThat(connectivityManager.getNetworkPreference()).isEqualTo(ConnectivityManager.TYPE_WIFI);
  }

  @Test
  public void getNetworkCallbacks_shouldHaveEmptyDefault() {
    assertThat(shadowOf(connectivityManager).getNetworkCallbacks()).isEmpty();
  }

  private static ConnectivityManager.NetworkCallback createSimpleCallback() {
    return new ConnectivityManager.NetworkCallback() {
      @Override
      public void onAvailable(@Nonnull Network network) {}

      @Override
      public void onLost(@Nonnull Network network) {}
    };
  }

  private static PendingIntent createSimplePendingIntent() {
    return PendingIntent.getActivity(getApplicationContext(), 0, new Intent(), 0);
  }

  @Test
  public void requestNetwork_shouldAddCallback() {
    NetworkRequest.Builder builder = new NetworkRequest.Builder();
    ConnectivityManager.NetworkCallback callback = createSimpleCallback();
    connectivityManager.requestNetwork(builder.build(), callback);
    assertThat(shadowOf(connectivityManager).getNetworkCallbacks()).hasSize(1);
  }

  @Test
  public void registerCallback_shouldAddCallback() {
    NetworkRequest.Builder builder = new NetworkRequest.Builder();
    ConnectivityManager.NetworkCallback callback = createSimpleCallback();
    connectivityManager.registerNetworkCallback(builder.build(), callback);
    assertThat(shadowOf(connectivityManager).getNetworkCallbacks()).hasSize(1);
  }

  @Test
  @Config(minSdk = M)
  public void registerCallback_withPendingIntent_shouldAddCallback() {
    NetworkRequest.Builder builder = new NetworkRequest.Builder();
    PendingIntent pendingIntent = createSimplePendingIntent();
    connectivityManager.registerNetworkCallback(builder.build(), pendingIntent);
    assertThat(shadowOf(connectivityManager).getNetworkCallbackPendingIntents()).hasSize(1);
  }

  @Test
  @Config(minSdk = O)
  public void requestNetwork_withTimeout_shouldAddCallback() {
    NetworkRequest.Builder builder = new NetworkRequest.Builder();
    ConnectivityManager.NetworkCallback callback = createSimpleCallback();
    connectivityManager.requestNetwork(builder.build(), callback, 0);
    assertThat(shadowOf(connectivityManager).getNetworkCallbacks()).hasSize(1);
  }

  @Test
  @Config(minSdk = O)
  public void requestNetwork_withHandler_shouldAddCallback() {
    NetworkRequest.Builder builder = new NetworkRequest.Builder();
    ConnectivityManager.NetworkCallback callback = createSimpleCallback();
    connectivityManager.requestNetwork(builder.build(), callback, new Handler());
    assertThat(shadowOf(connectivityManager).getNetworkCallbacks()).hasSize(1);
  }

  @Test
  @Config(minSdk = O)
  public void requestNetwork_withHandlerAndTimer_shouldAddCallback() {
    NetworkRequest.Builder builder = new NetworkRequest.Builder();
    ConnectivityManager.NetworkCallback callback = createSimpleCallback();
    connectivityManager.requestNetwork(builder.build(), callback, new Handler(), 0);
    assertThat(shadowOf(connectivityManager).getNetworkCallbacks()).hasSize(1);
  }

  @Test
  @Config(minSdk = N)
  public void registerDefaultCallback_shouldAddCallback() {
    ConnectivityManager.NetworkCallback callback = createSimpleCallback();
    connectivityManager.registerDefaultNetworkCallback(callback);
    assertThat(shadowOf(connectivityManager).getNetworkCallbacks()).hasSize(1);
  }

  @Test
  @Config(minSdk = O)
  public void registerDefaultCallback_withHandler_shouldAddCallback() {
    ConnectivityManager.NetworkCallback callback = createSimpleCallback();
    connectivityManager.registerDefaultNetworkCallback(callback, new Handler());
    assertThat(shadowOf(connectivityManager).getNetworkCallbacks()).hasSize(1);
  }

  @Test
  @Config(minSdk = S)
  public void registerBestMatchingNetworkCallback_shouldAddCallback() {
    NetworkRequest.Builder builder = new NetworkRequest.Builder();
    ConnectivityManager.NetworkCallback callback = createSimpleCallback();
    connectivityManager.registerBestMatchingNetworkCallback(
        builder.build(), callback, new Handler());
    assertThat(shadowOf(connectivityManager).getNetworkCallbacks()).hasSize(1);
  }

  @Test
  public void unregisterCallback_shouldRemoveCallbacks() {
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

  @Test
  @Config(minSdk = M)
  public void unregisterCallback_withPendingIntent_shouldRemoveCallbacks() {
    NetworkRequest.Builder builder = new NetworkRequest.Builder();
    // Add two pendingIntents, should treat them as equal based on Intent#filterEquals
    PendingIntent pendingIntent1 = createSimplePendingIntent();
    PendingIntent pendingIntent2 = createSimplePendingIntent();
    connectivityManager.registerNetworkCallback(builder.build(), pendingIntent1);
    connectivityManager.registerNetworkCallback(builder.build(), pendingIntent2);

    assertThat(shadowOf(connectivityManager).getNetworkCallbackPendingIntents()).hasSize(1);
    connectivityManager.unregisterNetworkCallback(pendingIntent2);
    assertThat(shadowOf(connectivityManager).getNetworkCallbackPendingIntents()).isEmpty();
  }

  @Test
  public void unregisterCallback_shouldNotAllowNullCallback() {
    // Verify that exception is thrown.
    assertThrows(
        IllegalArgumentException.class,
        () ->
            connectivityManager.unregisterNetworkCallback(
                (ConnectivityManager.NetworkCallback) null));
  }

  @Config(minSdk = M)
  @Test
  public void unregisterCallback_withPendingIntent_shouldNotAllowNullCallback() {
    // Verify that exception is thrown.
    assertThrows(
        IllegalArgumentException.class,
        () -> connectivityManager.unregisterNetworkCallback((PendingIntent) null));
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
    assertThat(connectivityManager.getBoundNetworkForProcess()).isSameInstanceAs(network);
  }

  @Test
  public void isDefaultNetworkActive_defaultActive() {
    assertThat(shadowOf(connectivityManager).isDefaultNetworkActive()).isTrue();
  }

  @Test
  public void isDefaultNetworkActive_notActive() {
    shadowOf(connectivityManager).setDefaultNetworkActive(false);
    assertThat(shadowOf(connectivityManager).isDefaultNetworkActive()).isFalse();
  }

  private static ConnectivityManager.OnNetworkActiveListener createSimpleOnNetworkActiveListener() {
    //noinspection Convert2Lambda
    return new ConnectivityManager.OnNetworkActiveListener() {
      @Override
      public void onNetworkActive() {}
    };
  }

  @Test
  public void addDefaultNetworkActiveListener_shouldAddListener() {
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
  public void removeDefaultNetworkActiveListener_shouldRemoveListeners() {
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

  @Test
  public void removeDefaultNetworkActiveListener_shouldNotAllowNullListener() {
    // Verify that exception is thrown.
    assertThrows(
        IllegalArgumentException.class,
        () -> connectivityManager.removeDefaultNetworkActiveListener(null));
  }

  @Test
  public void getNetworkCapabilities() {
    NetworkCapabilities nc = ShadowNetworkCapabilities.newInstance();
    shadowOf(nc).addCapability(NetworkCapabilities.NET_CAPABILITY_MMS);

    shadowOf(connectivityManager)
        .setNetworkCapabilities(shadowOf(connectivityManager).getActiveNetwork(), nc);

    assertThat(
            shadowOf(connectivityManager)
                .getNetworkCapabilities(shadowOf(connectivityManager).getActiveNetwork())
                .hasCapability(NetworkCapabilities.NET_CAPABILITY_MMS))
        .isTrue();
  }

  @Test
  public void getNetworkCapabilities_shouldReturnDefaultCapabilities() {
    for (Network network : connectivityManager.getAllNetworks()) {
      NetworkCapabilities nc = connectivityManager.getNetworkCapabilities(network);
      assertThat(nc).isNotNull();

      int netId = shadowOf(network).getNetId();
      if (netId == ShadowConnectivityManager.NET_ID_WIFI) {
        assertThat(nc.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)).isTrue();
      }
      if (netId == ShadowConnectivityManager.NET_ID_MOBILE) {
        assertThat(nc.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)).isTrue();
      }
    }
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
  public void getLinkProperties() {
    Network network = shadowOf(connectivityManager).getActiveNetwork();
    LinkProperties lp = ReflectionHelpers.callConstructor(LinkProperties.class);
    shadowOf(connectivityManager).setLinkProperties(network, lp);

    assertThat(connectivityManager.getLinkProperties(network)).isEqualTo(lp);
  }

  @Test
  public void getLinkProperties_shouldReturnNull() {
    Network network = shadowOf(connectivityManager).getActiveNetwork();
    shadowOf(connectivityManager).setLinkProperties(network, null);

    assertThat(connectivityManager.getLinkProperties(network)).isNull();
  }

  @Test
  @Config(minSdk = N)
  public void setRestrictBackgroundStatus() {
    shadowOf(connectivityManager).setRestrictBackgroundStatus(1);
    assertThat(connectivityManager.getRestrictBackgroundStatus())
        .isEqualTo(RESTRICT_BACKGROUND_STATUS_DISABLED);

    shadowOf(connectivityManager).setRestrictBackgroundStatus(2);
    assertThat(connectivityManager.getRestrictBackgroundStatus())
        .isEqualTo(RESTRICT_BACKGROUND_STATUS_WHITELISTED);

    shadowOf(connectivityManager).setRestrictBackgroundStatus(3);
    assertThat(connectivityManager.getRestrictBackgroundStatus())
        .isEqualTo(RESTRICT_BACKGROUND_STATUS_ENABLED);
  }

  @Test
  @Config(minSdk = N)
  public void setRestrictBackgroundStatus_defaultValueIsDisabled() {
    assertThat(connectivityManager.getRestrictBackgroundStatus())
        .isEqualTo(RESTRICT_BACKGROUND_STATUS_DISABLED);
  }

  @Config(minSdk = N)
  @Test
  public void setRestrictBackgroundStatus_throwsExceptionOnIncorrectStatus0() {
    assertThrows(
        IllegalArgumentException.class,
        () -> shadowOf(connectivityManager).setRestrictBackgroundStatus(0));
  }

  @Config(minSdk = N)
  @Test
  public void setRestrictBackgroundStatus_throwsExceptionOnIncorrectStatus4() {
    assertThrows(
        IllegalArgumentException.class,
        () -> shadowOf(connectivityManager).setRestrictBackgroundStatus(4));
  }

  @Test
  public void checkPollingTetherThreadNotCreated() {
    for (StackTraceElement[] elements : Thread.getAllStackTraces().values()) {
      for (StackTraceElement element : elements) {
        if (element.toString().contains("android.net.TetheringManager")) {
          throw new RuntimeException("Found polling thread " + Arrays.toString(elements));
        }
      }
    }
  }

  @Test
  @Config(minSdk = M)
  public void getProxyForNetwork() {
    Network network = connectivityManager.getActiveNetwork();
    connectivityManager.bindProcessToNetwork(network);
    ProxyInfo proxyInfo = ProxyInfo.buildDirectProxy("10.11.12.13", 1234);

    shadowOf(connectivityManager).setProxyForNetwork(network, proxyInfo);

    assertThat(connectivityManager.getProxyForNetwork(network)).isEqualTo(proxyInfo);
    assertThat(connectivityManager.getDefaultProxy()).isEqualTo(proxyInfo);
  }

  @Test
  @Config(minSdk = M)
  public void getProxyForNetwork_shouldReturnNullByDefaultWithBoundProcess() {
    Network network = connectivityManager.getActiveNetwork();
    connectivityManager.bindProcessToNetwork(network);

    assertThat(connectivityManager.getProxyForNetwork(network)).isNull();
    assertThat(connectivityManager.getDefaultProxy()).isNull();
  }

  @Test
  @Config(minSdk = M)
  public void getProxyForNetwork_shouldReturnNullByDefaultNoBoundProcess() {
    Network network = connectivityManager.getActiveNetwork();

    assertThat(connectivityManager.getProxyForNetwork(network)).isNull();
    assertThat(connectivityManager.getDefaultProxy()).isNull();
  }

  @Test
  @Config(minSdk = VERSION_CODES.O)
  public void connectivityManager_instanceBasedOnSdkVersion() {
    setSystemPropertyRule.set("robolectric.createActivityContexts", "true");

    try (ActivityController<Activity> controller =
        Robolectric.buildActivity(Activity.class).setup()) {
      Activity activity = controller.get();
      ConnectivityManager activityConnectivityManager =
          (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);

      if (Build.VERSION.SDK_INT >= S) {
        assertThat(connectivityManager).isNotSameInstanceAs(activityConnectivityManager);
      } else {
        assertThat(connectivityManager).isSameInstanceAs(activityConnectivityManager);
      }
      Network applicationActiveNetwork = connectivityManager.getActiveNetwork();
      Network activityActiveNetwork = activityConnectivityManager.getActiveNetwork();

      assertThat(activityActiveNetwork).isEqualTo(applicationActiveNetwork);
    }
  }

  @Test
  public void setDefaultNetworkActive_isActiveTrue_triggersOnAvailableInCallbacks() {
    NetworkRequest.Builder builder = new NetworkRequest.Builder();
    ConnectivityManager.NetworkCallback callback1 = spy(createSimpleCallback());
    ConnectivityManager.NetworkCallback callback2 = spy(createSimpleCallback());
    ShadowConnectivityManager shadowConnectivityManager = shadowOf(connectivityManager);
    shadowConnectivityManager.registerDefaultNetworkCallback(callback1);
    shadowConnectivityManager.registerNetworkCallback(builder.build(), callback2);
    shadowConnectivityManager.setActiveNetworkInfo(
        ShadowNetworkInfo.newInstance(
            null,
            ConnectivityManager.TYPE_MOBILE,
            TelephonyManager.NETWORK_TYPE_LTE,
            true,
            NetworkInfo.State.CONNECTED));

    shadowConnectivityManager.setDefaultNetworkActive(true);

    verify(callback1).onAvailable(shadowConnectivityManager.getActiveNetwork());
    verify(callback2).onAvailable(shadowConnectivityManager.getActiveNetwork());
  }

  @Test
  public void setDefaultNetworkActive_isActiveFalse_triggersOnLostInCallbacks() {
    NetworkRequest.Builder builder = new NetworkRequest.Builder();
    ConnectivityManager.NetworkCallback callback1 = spy(createSimpleCallback());
    ConnectivityManager.NetworkCallback callback2 = spy(createSimpleCallback());
    ShadowConnectivityManager shadowConnectivityManager = shadowOf(connectivityManager);
    shadowConnectivityManager.registerDefaultNetworkCallback(callback1);
    shadowConnectivityManager.registerNetworkCallback(builder.build(), callback2);
    shadowConnectivityManager.setActiveNetworkInfo(
        ShadowNetworkInfo.newInstance(
            null,
            ConnectivityManager.TYPE_MOBILE,
            TelephonyManager.NETWORK_TYPE_LTE,
            true,
            NetworkInfo.State.CONNECTED));
    Network previouslyActiveNetwork = shadowConnectivityManager.getActiveNetwork();

    shadowConnectivityManager.setDefaultNetworkActive(false);

    verify(callback1).onLost(previouslyActiveNetwork);
    verify(callback2).onLost(previouslyActiveNetwork);
  }

  @Test
  public void
      setDefaultNetworkActive_withSetNetworkCallbacksEnabledFalse_doesNotTriggerCallbacks() {
    NetworkRequest.Builder builder = new NetworkRequest.Builder();
    ConnectivityManager.NetworkCallback callback1 = spy(createSimpleCallback());
    ConnectivityManager.NetworkCallback callback2 = spy(createSimpleCallback());
    ShadowConnectivityManager shadowConnectivityManager = shadowOf(connectivityManager);
    shadowConnectivityManager.registerDefaultNetworkCallback(callback1);
    shadowConnectivityManager.registerNetworkCallback(builder.build(), callback2);
    shadowConnectivityManager.setActiveNetworkInfo(
        ShadowNetworkInfo.newInstance(
            null,
            ConnectivityManager.TYPE_MOBILE,
            TelephonyManager.NETWORK_TYPE_LTE,
            true,
            NetworkInfo.State.CONNECTED));
    shadowConnectivityManager.setNetworkCallbacksEnabled(false);

    shadowConnectivityManager.setDefaultNetworkActive(true);
    shadowConnectivityManager.setDefaultNetworkActive(false);

    verify(callback1, never()).onAvailable(shadowConnectivityManager.getActiveNetwork());
    verify(callback2, never()).onAvailable(shadowConnectivityManager.getActiveNetwork());
  }

  @Test
  public void defaults_afterReset() {
    ShadowConnectivityManager.reset();
    assertThat(connectivityManager.getAllNetworkInfo()).hasLength(2);
    assertThat(connectivityManager.getAllNetworks()).hasLength(2);
    assertThat(connectivityManager.isDefaultNetworkActive()).isTrue();
    if (RuntimeEnvironment.getApiLevel() >= M) {
      assertThat(connectivityManager.getActiveNetwork()).isNotNull();
    }
  }

  @Config(minSdk = M)
  @Test
  public void getActiveNetwork_afterSetActiveNetworkInfoNull() {
    shadowOf(connectivityManager).setActiveNetworkInfo(null);
    assertThat(connectivityManager.getActiveNetwork()).isNull();
  }
}
