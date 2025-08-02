package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.M;
import static com.google.common.truth.Truth.assertThat;

import android.net.Network;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.io.FileDescriptor;
import java.net.DatagramSocket;
import java.net.Socket;
import javax.net.SocketFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;

@RunWith(AndroidJUnit4.class)
public class ShadowNetworkTest {
  @Test
  public void getNetId_shouldReturnConstructorNetId() {
    final int netId = 123;

    Network network = ShadowNetwork.newInstance(netId);
    ShadowNetwork shadowNetwork = Shadows.shadowOf(network);
    assertThat(shadowNetwork.getNetId()).isEqualTo(netId);
  }

  @Test
  @Config(minSdk = M)
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

  @Test
  @Config(minSdk = M)
  public void isSocketBoundSocketDatagramSocket() throws Exception {
    Network network = ShadowNetwork.newInstance(0);
    DatagramSocket datagramSocket = new DatagramSocket();

    assertThat(Shadows.shadowOf(network).boundSocketCount()).isEqualTo(0);

    network.bindSocket(datagramSocket);
    assertThat(Shadows.shadowOf(network).isSocketBound(datagramSocket)).isTrue();
    assertThat(Shadows.shadowOf(network).boundSocketCount()).isEqualTo(1);
  }

  @Test
  public void isSocketBoundSocket() throws Exception {
    Network network = ShadowNetwork.newInstance(0);
    Socket socket = new Socket();

    assertThat(Shadows.shadowOf(network).boundSocketCount()).isEqualTo(0);

    network.bindSocket(socket);
    assertThat(Shadows.shadowOf(network).isSocketBound(socket)).isTrue();
    assertThat(Shadows.shadowOf(network).boundSocketCount()).isEqualTo(1);
  }

  @Test
  @Config(minSdk = M)
  public void isSocketBoundFileDescriptor() throws Exception {
    Network network = ShadowNetwork.newInstance(0);
    FileDescriptor fileDescriptor = new FileDescriptor();

    assertThat(Shadows.shadowOf(network).boundSocketCount()).isEqualTo(0);

    network.bindSocket(fileDescriptor);
    assertThat(Shadows.shadowOf(network).isSocketBound(fileDescriptor)).isTrue();
    assertThat(Shadows.shadowOf(network).boundSocketCount()).isEqualTo(1);
  }

  @Test
  @Config(minSdk = M)
  public void setSocketFactory_shouldOverrideSocketFactory() {
    Network network = ShadowNetwork.newInstance(0);
    ShadowNetwork shadowNetwork = Shadows.shadowOf(network);
    SocketFactory customSocketFactory = SocketFactory.getDefault();

    shadowNetwork.setSocketFactory(customSocketFactory);

    assertThat(shadowNetwork.getSocketFactory()).isSameInstanceAs(customSocketFactory);
  }

  @Test
  @Config(minSdk = M)
  public void getSocketFactory_withoutOverride_shouldReturnSocketFactory() {
    Network network = ShadowNetwork.newInstance(0);
    ShadowNetwork shadowNetwork = Shadows.shadowOf(network);

    assertThat(shadowNetwork.getSocketFactory()).isNotNull();
  }
}
