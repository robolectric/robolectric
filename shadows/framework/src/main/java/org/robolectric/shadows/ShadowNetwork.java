package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.LOLLIPOP_MR1;
import static android.os.Build.VERSION_CODES.M;

import android.net.Network;
import java.io.FileDescriptor;
import java.net.DatagramSocket;
import java.net.Socket;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.shadow.api.Shadow;

@Implements(value = Network.class, minSdk = LOLLIPOP)
public class ShadowNetwork {
  private int netId;

  /**
   * Creates new instance of {@link Network}, because its constructor is hidden.
   *
   * @param netId The netId.
   * @return The Network instance.
   */
  public static Network newInstance(int netId) {
    return Shadow.newInstance(Network.class, new Class[] {int.class}, new Object[] {netId});
  }

  @Implementation
  protected void __constructor__(int netId) {
    this.netId = netId;
  }

  /**
   * No-ops. We cannot assume that a Network represents a real network interface on the device
   * running this test, so we have nothing to bind the socket to.
   */
  @Implementation(minSdk = LOLLIPOP_MR1)
  protected void bindSocket(DatagramSocket socket) {}

  /**
   * No-ops. We cannot assume that a Network represents a real network interface on the device
   * running this test, so we have nothing to bind the socket to.
   */
  @Implementation
  protected void bindSocket(Socket socket) {}

  /**
   * No-ops. We cannot assume that a Network represents a real network interface on the device
   * running this test, so we have nothing to bind the socket to.
   */
  @Implementation(minSdk = M)
  protected void bindSocket(FileDescriptor fd) {}

  /**
   * Allows to get the stored netId.
   *
   * @return The netId.
   */
  public int getNetId() {
    return netId;
  }
}
