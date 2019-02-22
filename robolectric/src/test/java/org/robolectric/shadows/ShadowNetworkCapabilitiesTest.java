package org.robolectric.shadows;

import static android.net.NetworkCapabilities.TRANSPORT_CELLULAR;
import static android.net.NetworkCapabilities.TRANSPORT_WIFI;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.net.NetworkCapabilities;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

@RunWith(AndroidJUnit4.class)
@Config(minSdk = LOLLIPOP)
public class ShadowNetworkCapabilitiesTest {

  @Test
  public void hasTransport_shouldReturnAsPerAssignedTransportTypes() throws Exception {
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
}
