package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR1;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Build;
import android.os.ParcelUuid;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.util.ReflectionHelpers;

@Implements(value = BluetoothServerSocket.class)
public class ShadowBluetoothServerSocket {
  private final BlockingQueue<BluetoothSocket> sockets = new LinkedBlockingQueue<>();
  private boolean closed;

  @SuppressLint("PrivateApi")
  @SuppressWarnings("unchecked")
  public static BluetoothServerSocket newInstance(
      int type, boolean auth, boolean encrypt, ParcelUuid uuid) {
    if (Build.VERSION.SDK_INT >= JELLY_BEAN_MR1) {
      return Shadow.newInstance(
          BluetoothServerSocket.class,
          new Class<?>[] {Integer.TYPE, Boolean.TYPE, Boolean.TYPE, ParcelUuid.class},
          new Object[] {type, auth, encrypt, uuid});
    } else {
      return Shadow.newInstance(
          BluetoothServerSocket.class,
          new Class<?>[] {Integer.TYPE, Boolean.TYPE, Boolean.TYPE, Integer.TYPE},
          new Object[] {type, auth, encrypt, getPort(uuid)});
    }
  }

  // Port ranges are valid from 1 to MAX_RFCOMM_CHANNEL.
  private static int getPort(ParcelUuid uuid) {
    return Math.abs(uuid.hashCode() % BluetoothSocket.MAX_RFCOMM_CHANNEL) + 1;
  }

  /**
   * May block the current thread and wait until {@link BluetoothDevice} is offered via
   * {@link #deviceConnected(BluetoothDevice)} method or timeout occurred.
   *
   * @return socket of the connected bluetooth device
   * @throws IOException if socket has been closed, thread interrupted while waiting or timeout has
   *         occurred.
   */
  @Implementation
  protected BluetoothSocket accept(int timeout) throws IOException {
    if (closed) {
      throw new IOException("Socket closed");
    }

    BluetoothSocket socket;
    try {
      socket = timeout == -1
              ? sockets.take() : sockets.poll(timeout, TimeUnit.MILLISECONDS);
    } catch (InterruptedException e) {
      throw new IOException(e);
    }

    if (socket == null) {
      throw new IOException("Timeout occurred");
    }
    socket.connect();
    return socket;
  }

  @Implementation
  protected void close() throws IOException {
    closed = true;
  }

  /** Creates {@link BluetoothSocket} for the given device and makes this socket available
   * immediately in the {@link #accept(int)} method. */
  public BluetoothSocket deviceConnected(BluetoothDevice device) {
    BluetoothSocket socket = Shadow.newInstanceOf(BluetoothSocket.class);
    ReflectionHelpers.setField(socket, "mDevice", device);
    sockets.offer(socket);
    return socket;
  }
}
