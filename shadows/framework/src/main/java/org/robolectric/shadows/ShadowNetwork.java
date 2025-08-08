package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.M;
import static android.os.Build.VERSION_CODES.R;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.net.Network;
import java.io.FileDescriptor;
import java.net.DatagramSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.Nullable;
import javax.net.SocketFactory;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.reflector.Direct;
import org.robolectric.util.reflector.ForType;

@Implements(Network.class)
public class ShadowNetwork {

  @RealObject private Network realObject;

  private final Set<Socket> boundSockets = new HashSet<>();
  private final Set<DatagramSocket> boundDatagramSockets = new HashSet<>();
  private final Set<FileDescriptor> boundFileDescriptors = new HashSet<>();

  @Nullable private SocketFactory socketFactoryOverride;

  /**
   * Creates new instance of {@link Network}, because its constructor is hidden.
   *
   * @param netId The netId.
   * @return The Network instance.
   */
  public static Network newInstance(int netId) {
    return Shadow.newInstance(Network.class, new Class[] {int.class}, new Object[] {netId});
  }

  /** Checks if the {@code socket} was previously bound to this network. */
  public boolean isSocketBound(Socket socket) {
    return boundSockets.contains(socket);
  }

  /** Checks if the {@code datagramSocket} was previously bound to this network. */
  public boolean isSocketBound(DatagramSocket socket) {
    return boundDatagramSockets.contains(socket);
  }

  /** Checks if the {@code fileDescriptor} was previously bound to this network. */
  public boolean isSocketBound(FileDescriptor fd) {
    return boundFileDescriptors.contains(fd);
  }

  /** Returns the total number of sockets bound to this network interface. */
  public int boundSocketCount() {
    return boundSockets.size() + boundDatagramSockets.size() + boundFileDescriptors.size();
  }

  /**
   * Simulates a socket bind. isSocketBound can be called to verify that the socket was bound to
   * this network interface, and boundSocketCount() will increment for any unique socket.
   */
  @Implementation
  protected void bindSocket(DatagramSocket socket) {
    boundDatagramSockets.add(socket);
  }

  /**
   * Simulates a socket bind. isSocketBound can be called to verify that the socket was bound to
   * this network interface, and boundSocketCount() will increment for any unique socket.
   */
  @Implementation
  protected void bindSocket(Socket socket) {
    boundSockets.add(socket);
  }

  /**
   * Simulates a socket bind. isSocketBound can be called to verify that the fd was bound to this
   * network interface, and boundSocketCount() will increment for any unique socket.
   */
  @Implementation(minSdk = M)
  protected void bindSocket(FileDescriptor fd) {
    boundFileDescriptors.add(fd);
  }

  /**
   * Allows to get the stored netId.
   *
   * @return The netId.
   */
  @Implementation(minSdk = R)
  public int getNetId() {
    return ReflectionHelpers.getField(realObject, "netId");
  }

  /** Sets the socket factory to be provided by {@link #getSocketFactory()} */
  public void setSocketFactory(SocketFactory socketFactory) {
    socketFactoryOverride = socketFactory;
  }

  @Implementation
  protected SocketFactory getSocketFactory() {
    if (socketFactoryOverride != null) {
      return socketFactoryOverride;
    }
    return reflector(NetworkReflector.class, realObject).getSocketFactory();
  }

  @ForType(Network.class)
  private interface NetworkReflector {
    @Direct
    SocketFactory getSocketFactory();
  }
}
