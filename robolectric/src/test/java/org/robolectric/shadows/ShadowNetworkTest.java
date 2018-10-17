package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.LOLLIPOP_MR1;
import static android.os.Build.VERSION_CODES.M;
import static com.google.common.truth.Truth.assertThat;

import android.net.Network;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.io.FileDescriptor;
import java.net.DatagramSocket;
import java.net.Socket;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;

@RunWith(AndroidJUnit4.class)
@Config(minSdk = LOLLIPOP)
public class ShadowNetworkTest {
  @Test
  public void getNetId_shouldReturnConstructorNetId() {
    final int netId = 123;

    Network network = ShadowNetwork.newInstance(netId);
    ShadowNetwork shadowNetwork = Shadows.shadowOf(network);
    assertThat(shadowNetwork.getNetId()).isEqualTo(netId);
  }

  @Test
  @Config(minSdk = LOLLIPOP_MR1)
  public void bindSocketDatagramSocket_shouldNotCrash() throws Exception {
    Network network = ShadowNetwork.newInstance(0);
    network.bindSocket(new DatagramSocket());
  }

  @Test
  public void bindSocketSocket_shouldNotCrash() throws Exception {
    Network network = ShadowNetwork.newInstance(0);
    network.bindSocket(new Socket());
  }

  @Test
  @Config(minSdk = M)
  public void bindSocketFileDescriptor_shouldNotCrash() throws Exception {
    Network network = ShadowNetwork.newInstance(0);
    network.bindSocket(new FileDescriptor());
  }
}
