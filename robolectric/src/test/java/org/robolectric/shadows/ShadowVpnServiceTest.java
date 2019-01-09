package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.LOLLIPOP_MR1;
import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.content.Context;
import android.content.Intent;
import android.net.Network;
import android.net.VpnService;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Config;

@RunWith(AndroidJUnit4.class)
public class ShadowVpnServiceTest {
  private Context context;

  @Before
  public void setUp() {
    context = ApplicationProvider.getApplicationContext();
  }

  @Test
  public void testPrepare() {
    Intent intent = new Intent("foo");
    ShadowVpnService.setPrepareResult(intent);

    assertThat(VpnService.prepare(context)).isEqualTo(intent);
  }

  @Test
  @Config(minSdk = LOLLIPOP_MR1)
  public void testGetUnderlyingNetworks() {
    VpnService vpnService = Robolectric.setupService(VpnService.class);
    Network[] networks = new Network[0];

    vpnService.setUnderlyingNetworks(networks);

    assertThat(shadowOf(vpnService).getUnderlyingNetworks()).isEqualTo(networks);
  }
}
