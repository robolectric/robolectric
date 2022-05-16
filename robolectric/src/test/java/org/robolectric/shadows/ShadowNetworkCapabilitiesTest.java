package org.robolectric.shadows;

import static android.net.NetworkCapabilities.NET_CAPABILITY_MMS;
import static android.net.NetworkCapabilities.NET_CAPABILITY_NOT_RESTRICTED;
import static android.net.NetworkCapabilities.NET_CAPABILITY_NOT_VPN;
import static android.net.NetworkCapabilities.NET_CAPABILITY_TRUSTED;
import static android.net.NetworkCapabilities.TRANSPORT_CELLULAR;
import static android.net.NetworkCapabilities.TRANSPORT_WIFI;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.N;
import static android.os.Build.VERSION_CODES.N_MR1;
import static android.os.Build.VERSION_CODES.O;
import static android.os.Build.VERSION_CODES.S;
import static com.google.common.truth.Truth.assertThat;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.mockito.Mockito.mock;
import static org.robolectric.Shadows.shadowOf;

import android.net.NetworkCapabilities;
import android.net.NetworkSpecifier;
import android.net.wifi.WifiInfo;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.util.ReflectionHelpers;

@RunWith(AndroidJUnit4.class)
@Config(minSdk = LOLLIPOP)
public class ShadowNetworkCapabilitiesTest {

  @Test
  public void hasTransport_shouldReturnAsPerAssignedTransportTypes() {
    NetworkCapabilities networkCapabilities = ShadowNetworkCapabilities.newInstance();

    // Assert default false state.
    assertThat(networkCapabilities.hasTransport(TRANSPORT_WIFI)).isFalse();

    shadowOf(networkCapabilities).addTransportType(TRANSPORT_WIFI);
    shadowOf(networkCapabilities).addTransportType(TRANSPORT_CELLULAR);
    assertThat(networkCapabilities.hasTransport(TRANSPORT_WIFI)).isTrue();
    assertThat(networkCapabilities.hasTransport(TRANSPORT_CELLULAR)).isTrue();

    shadowOf(networkCapabilities).removeTransportType(TRANSPORT_WIFI);
    assertThat(networkCapabilities.hasTransport(TRANSPORT_WIFI)).isFalse();
    assertThat(networkCapabilities.hasTransport(TRANSPORT_CELLULAR)).isTrue();
  }

  @Test
  public void hasCapability_shouldReturnAsPerAssignedCapabilities() {
    NetworkCapabilities networkCapabilities = ShadowNetworkCapabilities.newInstance();

    // Assert default capabilities
    assertThat(networkCapabilities.hasCapability(NET_CAPABILITY_NOT_RESTRICTED)).isTrue();
    assertThat(networkCapabilities.hasCapability(NET_CAPABILITY_TRUSTED)).isTrue();
    assertThat(networkCapabilities.hasCapability(NET_CAPABILITY_NOT_VPN)).isTrue();

    shadowOf(networkCapabilities).addCapability(NET_CAPABILITY_MMS);

    assertThat(networkCapabilities.hasCapability(NET_CAPABILITY_MMS)).isTrue();

    shadowOf(networkCapabilities).removeCapability(NET_CAPABILITY_MMS);

    assertThat(networkCapabilities.hasCapability(NET_CAPABILITY_MMS)).isFalse();
    assertThat(networkCapabilities.hasCapability(NET_CAPABILITY_NOT_RESTRICTED)).isTrue();
    assertThat(networkCapabilities.hasCapability(NET_CAPABILITY_TRUSTED)).isTrue();
    assertThat(networkCapabilities.hasCapability(NET_CAPABILITY_NOT_VPN)).isTrue();
  }

  @Test
  @Config(minSdk = O)
  public void getNetworkSpecifier_shouldReturnTheSpecifiedValue_fromO() {
    NetworkCapabilities networkCapabilities = ShadowNetworkCapabilities.newInstance();
    // Required to set NetworkSpecifier
    shadowOf(networkCapabilities).addTransportType(TRANSPORT_WIFI);

    NetworkSpecifier testNetworkSpecifier = mock(NetworkSpecifier.class);
    shadowOf(networkCapabilities).setNetworkSpecifier(testNetworkSpecifier);
    assertThat(networkCapabilities.getNetworkSpecifier()).isEqualTo(testNetworkSpecifier);
  }

  @Test
  @Config(minSdk = N, maxSdk = N_MR1)
  public void getNetworkSpecifier_shouldReturnTheSpecifiedValue_beforeO() {
    NetworkCapabilities networkCapabilities = ShadowNetworkCapabilities.newInstance();
    // Required to set NetworkSpecifier
    shadowOf(networkCapabilities).addTransportType(TRANSPORT_WIFI);

    String testNetworkSpecifier = "testNetworkSpecifier";
    shadowOf(networkCapabilities).setNetworkSpecifier(testNetworkSpecifier);
    String checkedNetworkSpecifier =
        ReflectionHelpers.callInstanceMethod(networkCapabilities, "getNetworkSpecifier");
    assertThat(checkedNetworkSpecifier).isEqualTo(testNetworkSpecifier);
  }

  @Config(minSdk = S)
  public void setTransportInfo_shouldSetTransportInfo() {
    NetworkCapabilities networkCapabilities = ShadowNetworkCapabilities.newInstance();

    String fakeBssid = "00:00:00:00:00:00";
    String fakeSsid = "test wifi";
    shadowOf(networkCapabilities)
        .setTransportInfo(
            new WifiInfo.Builder().setSsid(fakeSsid.getBytes(UTF_8)).setBssid(fakeBssid).build());

    WifiInfo wifiInfo = (WifiInfo) networkCapabilities.getTransportInfo();
    assertThat(wifiInfo.getSSID()).isEqualTo(String.format("\"%s\"", fakeSsid));
    assertThat(wifiInfo.getBSSID()).isEqualTo(fakeBssid);
  }
}
