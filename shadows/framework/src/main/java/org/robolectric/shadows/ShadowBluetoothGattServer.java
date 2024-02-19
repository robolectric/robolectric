package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.O;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
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
  private final List<byte[]> writtenBytes = new ArrayList<>();
  private final Set<BluetoothDevice> cancelledDevices = new HashSet<>();
  private boolean isClosed;
  private final Set<BluetoothGattService> services = new HashSet<>();

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
   * Add a service to the GATT server.
   *
   * @param service service to be added to GattServer
   */
  @Implementation
  protected boolean addService(BluetoothGattService service) {
    bluetoothGattServerReflector.addService(service);
    this.services.add(service);
    return true;
  }

  /**
   * Remove a service from the GATT server.
   *
   * @param service service to be removed from GattServer
   */
  @Implementation
  protected boolean removeService(BluetoothGattService service) {
    return this.services.remove(service);
  }

  /** Remove all services from the list of provided services. */
  @Implementation
  protected void clearServices() {
    this.services.clear();
  }

  /** Returns a list of GATT services offered by this device. */
  @Implementation
  protected List<BluetoothGattService> getServices() {
    return ImmutableList.copyOf(this.services);
  }

  /**
   * Returns a {@link BluetoothGattService} from the list of services offered by this device.
   *
   * <p>If multiple instances of the same service (as identified by UUID) exist, the first instance
   * of the service is returned.
   *
   * @param uuid uuid of service
   */
  @Implementation
  protected BluetoothGattService getService(UUID uuid) {
    return this.services.stream().filter(s -> s.getUuid().equals(uuid)).findFirst().orElse(null);
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
   * Simulate a Gatt characteristic write request to the Gatt Server by triggering the server
   * callback.
   *
   * @param device remote device
   * @param requestId id of the request
   * @param characteristic characteristic to be written to
   * @param preparedWrite true, if this write operation should be queued for later execution
   * @param responseNeeded true, if the remote device requires a response
   * @param offset the offset given for the value
   * @param value the value the client wants to assign to the characteristic
   */
  public boolean notifyOnCharacteristicWriteRequest(
      BluetoothDevice device,
      int requestId,
      BluetoothGattCharacteristic characteristic,
      Boolean preparedWrite,
      Boolean responseNeeded,
      int offset,
      byte[] value) {
    if (this.callback == null) {
      return false;
    } else if ((characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_WRITE) == 0
        && (characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE)
            == 0) {
      return false;
    }
    writtenBytes.add(value);
    this.callback.onCharacteristicWriteRequest(
        device, requestId, characteristic, preparedWrite, responseNeeded, offset, value);
    return true;
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

  /** Get a copy of the list of bytes that have been received. */
  public List<byte[]> getWrittenBytes() {
    return Lists.transform(this.writtenBytes, bytes -> bytes != null ? bytes.clone() : null);
  }

  /** Clear the list of written bytes. */
  public void clearWrittenBytes() {
    this.writtenBytes.clear();
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

    @Direct
    boolean addService(BluetoothGattService service);
  }
}
