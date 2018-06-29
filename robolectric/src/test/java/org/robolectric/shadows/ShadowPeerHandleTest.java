package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.O;
import static com.google.common.truth.Truth.assertThat;

import android.net.wifi.aware.PeerHandle;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

/** Tests for {@link ShadowPeerHandle}. */
@RunWith(RobolectricTestRunner.class)
@Config(minSdk = O)
public class ShadowPeerHandleTest {

  @Test
  public void canCreatePeerHandleViaNewInstance() throws Exception {
    PeerHandle peerHandle = ShadowPeerHandle.newInstance();
    assertThat(peerHandle).isNotNull();
  }
}
