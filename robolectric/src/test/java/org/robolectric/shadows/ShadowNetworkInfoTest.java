package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.shadow.api.Shadow;

@RunWith(AndroidJUnit4.class)
public class ShadowNetworkInfoTest {

  @Test
  public void getDetailedState_shouldReturnTheAssignedState() {
    NetworkInfo networkInfo = Shadow.newInstanceOf(NetworkInfo.class);
    shadowOf(networkInfo).setDetailedState(NetworkInfo.DetailedState.SCANNING);
    assertThat(networkInfo.getDetailedState()).isEqualTo(NetworkInfo.DetailedState.SCANNING);
  }

  @Test
  public void getExtraInfo_shouldReturnTheSetValue() {
    NetworkInfo networkInfo = Shadow.newInstanceOf(NetworkInfo.class);
    shadowOf(networkInfo).setExtraInfo("some_extra_info");
    assertThat(networkInfo.getExtraInfo()).isEqualTo("some_extra_info");
  }

  @Test
  public void getTypeName_wifi_isWifiWithEmptySubtypeName() {
    NetworkInfo networkInfo =
        ShadowNetworkInfo.newInstance(
            NetworkInfo.DetailedState.CONNECTED,
            ConnectivityManager.TYPE_WIFI,
            /* subType= */ 0,
            /* isAvailable= */ true,
            NetworkInfo.State.CONNECTED);
    assertThat(networkInfo.getTypeName()).isEqualTo("WIFI");
    assertThat(networkInfo.getSubtypeName()).isEmpty();
  }

  @Test
  public void getTypeName_mobile_namesTheSubtype() {
    NetworkInfo networkInfo =
        ShadowNetworkInfo.newInstance(
            NetworkInfo.DetailedState.CONNECTED,
            ConnectivityManager.TYPE_MOBILE,
            TelephonyManager.NETWORK_TYPE_LTE,
            /* isAvailable= */ true,
            NetworkInfo.State.CONNECTED);
    assertThat(networkInfo.getTypeName()).isEqualTo("MOBILE");
    assertThat(networkInfo.getSubtypeName()).isEqualTo("LTE");
  }

  @Test
  public void getTypeName_followsSetConnectionType() {
    NetworkInfo networkInfo = Shadow.newInstanceOf(NetworkInfo.class);
    shadowOf(networkInfo).setConnectionType(ConnectivityManager.TYPE_ETHERNET);
    assertThat(networkInfo.getTypeName()).isEqualTo("ETHERNET");
  }

  @Test
  public void getTypeName_defaultActiveNetworkInfo_isNamed() {
    ConnectivityManager connectivityManager =
        (ConnectivityManager)
            ApplicationProvider.getApplicationContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
    NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
    assertThat(networkInfo.getTypeName()).isNotNull();
    assertThat(networkInfo.getSubtypeName()).isNotNull();
  }

  @Test
  public void getTypeName_copy_keepsCopiedNames() {
    NetworkInfo source =
        ShadowNetworkInfo.newInstance(
            NetworkInfo.DetailedState.CONNECTED,
            ConnectivityManager.TYPE_MOBILE,
            TelephonyManager.NETWORK_TYPE_LTE,
            /* isAvailable= */ true,
            NetworkInfo.State.CONNECTED);

    NetworkInfo copy = new NetworkInfo(source);

    assertThat(copy.getTypeName()).isEqualTo("MOBILE");
    assertThat(copy.getSubtypeName()).isEqualTo("LTE");
  }

  @Test
  public void getTypeName_constructor_keepsGivenNames() {
    NetworkInfo networkInfo =
        new NetworkInfo(ConnectivityManager.TYPE_WIFI, 0, "customType", "customSubtype");

    assertThat(networkInfo.getTypeName()).isEqualTo("customType");
    assertThat(networkInfo.getSubtypeName()).isEqualTo("customSubtype");
  }
}
