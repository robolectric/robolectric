package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.O;
import static com.google.common.truth.Truth.assertThat;

import android.net.wifi.aware.DiscoverySession;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

/** Tests for {@link ShadowDiscoverySession}. */
@RunWith(AndroidJUnit4.class)
@Config(minSdk = O)
public class ShadowDiscoverySessionTest {

  @Test
  public void canCreateDiscoverySessionViaNewInstance() throws Exception {
    DiscoverySession discoverySession = ShadowDiscoverySession.newInstance();
    assertThat(discoverySession).isNotNull();
  }
}
