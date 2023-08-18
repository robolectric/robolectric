package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.O;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.ReflectorObject;
import org.robolectric.util.reflector.Direct;
import org.robolectric.util.reflector.ForType;

/** Shadow of {@link BluetoothGattServer}. */
@Implements(value = BluetoothGattServer.class, minSdk = O)
public class ShadowBluetoothGattServer {
  private BluetoothGattServerCallback callback;
  private final List<byte[]> responses = new ArrayList<>();
  private final Set<BluetoothDevice> cancelledDevices = new HashSet<>();
  private boolean isClosed;

  @ReflectorObject protected BluetoothGattServerReflector bluetoothGattServerReflector;

  /** Close this GATT server instance. */
  @Implementation
  protected void close() {
    bluetoothGattServerReflector.close();
    this.isClosed = true;
  }

  /**
   * Disconnects an established connection, or cancels a connection attempt currently in progress.
   *
   * @param device Remote device
   */
  @Implementation
  protected void cancelConnection(BluetoothDevice device) {
    this.bluetoothGattServerReflector.cancelConnection(device);
    this.cancelledDevices.add(device);
  }

  /**
   * Send a response to a read or write request to a remote device.
   *
   * @param device The remote device to send this response to
   * @param requestId The ID of the request that was received with the callback
   * @param status The status of the request to be sent to the remote devices
   * @param offset Value offset for partial read/write response
   * @param value The value of the attribute that was read/written (optional)
   */
  @Implementation
  protected boolean sendResponse(
      BluetoothDevice device, int requestId, int status, int offset, byte[] value) {
    this.responses.add(value);
    return this.bluetoothGattServerReflector.sendResponse(device, requestId, status, offset, value);
  }

  /**
   * Simulate a successful Gatt Server Connection with {@link BluetoothConnectionManager}. Performs
   * a {@link BluetoothGattCallback#onConnectionStateChange} if available.
   *
   * @param device remote device
   */
  public void notifyConnection(BluetoothDevice device) {
    BluetoothConnectionManager.getInstance().registerGattServerConnection(device.getAddress());
    this.cancelledDevices.remove(device);

    if (this.callback != null) {
      this.callback.onConnectionStateChange(
          device, BluetoothGatt.GATT_SUCCESS, BluetoothAdapter.STATE_CONNECTED);
    }
  }

  /**
   * Simulate a successful Gatt Server Disconnection with {@link BluetoothConnectionManager}.
   * Performs a {@link BluetoothGattCallback#onConnectionStateChange} if available, even when device
   * was not connected initially.
   *
   * @param device remote device
   */
  public void notifyDisconnection(BluetoothDevice device) {
    BluetoothConnectionManager.getInstance().unregisterGattServerConnection(device.getAddress());
    this.cancelledDevices.add(device);

    if (this.callback != null) {
      this.callback.onConnectionStateChange(
          device, BluetoothGatt.GATT_SUCCESS, BluetoothAdapter.STATE_DISCONNECTED);
    }
  }

  /**
   * Get whether the device's connection has been cancelled.
   *
   * @param device remote device
   */
  public boolean isConnectionCancelled(BluetoothDevice device) {
    return this.cancelledDevices.contains(device);
  }

  /**
   * Returns true if the connection status of remote device is connected
   *
   * @param device remote device
   */
  public boolean isConnectedToDevice(BluetoothDevice device) {
    return BluetoothConnectionManager.getInstance().hasGattServerConnection(device.getAddress());
  }

  /** Get a copy of the list of responses that have been sent. */
  public List<byte[]> getResponses() {
    List<byte[]> responsesCopy = new ArrayList<>();
    for (byte[] response : this.responses) {
      if (response != null) {
        responsesCopy.add(response.clone());
      } else {
        responsesCopy.add(null);
      }
    }
    return responsesCopy;
  }

  /** Clear the list of responses. */
  public void clearResponses() {
    this.responses.clear();
  }

  /** Get whether server has been closed. */
  public boolean isClosed() {
    return this.isClosed;
  }

  public void setGattServerCallback(BluetoothGattServerCallback callback) {
    this.callback = callback;
  }

  public BluetoothGattServerCallback getGattServerCallback() {
    return this.callback;
  }

  public BluetoothConnectionManager getBluetoothConnectionManager() {
    return BluetoothConnectionManager.getInstance();
  }

  @ForType(BluetoothGattServer.class)
  private interface BluetoothGattServerReflector {

    @Direct
    void close();

    @Direct
    void cancelConnection(BluetoothDevice device);

    @Direct
    boolean sendResponse(
        BluetoothDevice device, int requestId, int status, int offset, byte[] value);
  }
}
