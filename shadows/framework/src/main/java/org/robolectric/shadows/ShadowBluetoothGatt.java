package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR2;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.O;
import static android.os.Build.VERSION_CODES.O_MR1;
import static android.os.Build.VERSION_CODES.R;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.annotation.ReflectorObject;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.util.PerfStatsCollector;
import org.robolectric.util.reflector.Direct;
import org.robolectric.util.reflector.ForType;

/** Shadow implementation of {@link BluetoothGatt}. */
@Implements(value = BluetoothGatt.class, minSdk = JELLY_BEAN_MR2)
public class ShadowBluetoothGatt {

  private static final String NULL_CALLBACK_MSG = "BluetoothGattCallback can not be null.";

  private BluetoothGattCallback bluetoothGattCallback;
  private int connectionPriority = BluetoothGatt.CONNECTION_PRIORITY_BALANCED;
  private boolean isConnected = false;
  private boolean isClosed = false;
  private byte[] writtenBytes;
  private byte[] readBytes;
  private final Set<BluetoothGattService> discoverableServices = new HashSet<>();
  private final ArrayList<BluetoothGattService> services = new ArrayList<>();

  @RealObject private BluetoothGatt realBluetoothGatt;
  @ReflectorObject protected BluetoothGattReflector bluetoothGattReflector;

  @SuppressLint("PrivateApi")
  @SuppressWarnings("unchecked")
  public static BluetoothGatt newInstance(BluetoothDevice device) {
    try {
      Class<?> iBluetoothGattClass =
          Shadow.class.getClassLoader().loadClass("android.bluetooth.IBluetoothGatt");

      BluetoothGatt bluetoothGatt;
      int apiLevel = RuntimeEnvironment.getApiLevel();
      if (apiLevel > R) {
        bluetoothGatt =
            Shadow.newInstance(
                BluetoothGatt.class,
                new Class<?>[] {
                  iBluetoothGattClass,
                  BluetoothDevice.class,
                  Integer.TYPE,
                  Boolean.TYPE,
                  Integer.TYPE,
                  Class.forName("android.content.AttributionSource"),
                },
                new Object[] {null, device, 0, false, 0, null});
      } else if (apiLevel >= O_MR1) {
        bluetoothGatt =
            Shadow.newInstance(
                BluetoothGatt.class,
                new Class<?>[] {
                  iBluetoothGattClass,
                  BluetoothDevice.class,
                  Integer.TYPE,
                  Boolean.TYPE,
                  Integer.TYPE
                },
                new Object[] {null, device, 0, false, 0});
      } else if (apiLevel >= O) {
        bluetoothGatt =
            Shadow.newInstance(
                BluetoothGatt.class,
                new Class<?>[] {
                  iBluetoothGattClass, BluetoothDevice.class, Integer.TYPE, Integer.TYPE
                },
                new Object[] {null, device, 0, 0});
      } else if (apiLevel >= LOLLIPOP) {
        bluetoothGatt =
            Shadow.newInstance(
                BluetoothGatt.class,
                new Class<?>[] {
                  Context.class, iBluetoothGattClass, BluetoothDevice.class, Integer.TYPE
                },
                new Object[] {RuntimeEnvironment.getApplication(), null, device, 0});
      } else {
        bluetoothGatt =
            Shadow.newInstance(
                BluetoothGatt.class,
                new Class<?>[] {Context.class, iBluetoothGattClass, BluetoothDevice.class},
                new Object[] {RuntimeEnvironment.getApplication(), null, device});
      }

      PerfStatsCollector.getInstance().incrementCount("constructShadowBluetoothGatt");
      return bluetoothGatt;
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Connect to a remote device, and performs a {@link
   * BluetoothGattCallback#onConnectionStateChange} if a {@link BluetoothGattCallback} has been set
   * by {@link ShadowBluetoothGatt#setGattCallback}
   *
   * @return true, if a {@link BluetoothGattCallback} has been set by {@link
   *     ShadowBluetoothGatt#setGattCallback}
   */
  @Implementation(minSdk = JELLY_BEAN_MR2)
  protected boolean connect() {
    if (this.getGattCallback() != null) {
      this.isConnected = true;
      this.getGattCallback()
          .onConnectionStateChange(
              this.realBluetoothGatt, BluetoothGatt.GATT_SUCCESS, BluetoothProfile.STATE_CONNECTED);
      return true;
    }
    return false;
  }

  /**
   * Disconnects an established connection, or cancels a connection attempt currently in progress.
   */
  @Implementation(minSdk = JELLY_BEAN_MR2)
  protected void disconnect() {
    bluetoothGattReflector.disconnect();
    if (this.getGattCallback() != null && this.isConnected) {
      this.getGattCallback()
          .onConnectionStateChange(
              this.realBluetoothGatt,
              BluetoothGatt.GATT_SUCCESS,
              BluetoothProfile.STATE_DISCONNECTED);
    }
    this.isConnected = false;
  }

  /** Close this Bluetooth GATT client. */
  @Implementation(minSdk = JELLY_BEAN_MR2)
  protected void close() {
    bluetoothGattReflector.close();
    this.isClosed = true;
    this.isConnected = false;
  }

  /**
   * Request a connection parameter update.
   *
   * @param priority Request a specific connection priority. Must be one of {@link
   *     BluetoothGatt#CONNECTION_PRIORITY_BALANCED}, {@link BluetoothGatt#CONNECTION_PRIORITY_HIGH}
   *     or {@link BluetoothGatt#CONNECTION_PRIORITY_LOW_POWER}.
   * @return true if operation is successful.
   * @throws IllegalArgumentException If the parameters are outside of their specified range.
   */
  @Implementation(minSdk = O)
  protected boolean requestConnectionPriority(int priority) {
    if (priority == BluetoothGatt.CONNECTION_PRIORITY_HIGH
        || priority == BluetoothGatt.CONNECTION_PRIORITY_BALANCED
        || priority == BluetoothGatt.CONNECTION_PRIORITY_LOW_POWER) {
      this.connectionPriority = priority;
      return true;
    }
    throw new IllegalArgumentException("connection priority not within valid range");
  }

  /**
   * Overrides {@link BluetoothGatt#discoverServices} to always return false unless there are
   * discoverable services made available by {@link ShadowBluetoothGatt#addDiscoverableService}
   *
   * @return true if discoverable service is available and callback response is possible
   */
  @Implementation(minSdk = O)
  protected boolean discoverServices() {
    this.services.clear();
    if (!this.discoverableServices.isEmpty()) {
      this.services.addAll(this.discoverableServices);

      if (this.getGattCallback() != null) {
        this.getGattCallback()
            .onServicesDiscovered(this.realBluetoothGatt, BluetoothGatt.GATT_SUCCESS);
        return true;
      }
    }
    return false;
  }

  /**
   * Overrides {@link BluetoothGatt#getServices} to always return a list of services discovered.
   *
   * @return list of services that have been discovered through {@link
   *     ShadowBluetoothGatt#discoverServices}, empty if none.
   */
  @Implementation(minSdk = O)
  protected List<BluetoothGattService> getServices() {
    return new ArrayList<>(this.services);
  }

  /**
   * Reads bytes from incoming characteristic if properties are valid and callback is set. Callback
   * responds with {@link BluetoothGattCallback#onCharacteristicWrite} and returns true when
   * successful.
   *
   * @param characteristic Characteristic to read
   * @return true, if the read operation was initiated successfully
   * @throws IllegalStateException if a {@link BluetoothGattCallback} has not been set by {@link
   *     ShadowBluetoothGatt#setGattCallback}
   */
  public boolean writeIncomingCharacteristic(BluetoothGattCharacteristic characteristic) {
    if (this.getGattCallback() == null) {
      throw new IllegalStateException(NULL_CALLBACK_MSG);
    }
    if (characteristic.getService() == null
        || ((characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_WRITE) == 0
            && (characteristic.getProperties()
                    & BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE)
                == 0)) {
      return false;
    }
    this.writtenBytes = characteristic.getValue();
    this.bluetoothGattCallback.onCharacteristicWrite(
        this.realBluetoothGatt, characteristic, BluetoothGatt.GATT_SUCCESS);
    return true;
  }

  /**
   * Writes bytes from incoming characteristic if properties are valid and callback is set. Callback
   * responds with BluetoothGattCallback#onCharacteristicRead and returns true when successful.
   *
   * @param characteristic Characteristic to read
   * @return true, if the read operation was initiated successfully
   * @throws IllegalStateException if a {@link BluetoothGattCallback} has not been set by {@link
   *     ShadowBluetoothGatt#setGattCallback}
   */
  public boolean readIncomingCharacteristic(BluetoothGattCharacteristic characteristic) {
    if (this.getGattCallback() == null) {
      throw new IllegalStateException(NULL_CALLBACK_MSG);
    }
    if ((characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_READ) == 0
        || characteristic.getService() == null) {
      return false;
    }

    this.readBytes = characteristic.getValue();
    this.bluetoothGattCallback.onCharacteristicRead(
        this.realBluetoothGatt, characteristic, BluetoothGatt.GATT_SUCCESS);
    return true;
  }

  public void addDiscoverableService(BluetoothGattService service) {
    this.discoverableServices.add(service);
  }

  public void removeDiscoverableService(BluetoothGattService service) {
    this.discoverableServices.remove(service);
  }

  public BluetoothGattCallback getGattCallback() {
    return this.bluetoothGattCallback;
  }

  public void setGattCallback(BluetoothGattCallback bluetoothGattCallback) {
    this.bluetoothGattCallback = bluetoothGattCallback;
  }

  public boolean isConnected() {
    return this.isConnected;
  }

  public boolean isClosed() {
    return this.isClosed;
  }

  public int getConnectionPriority() {
    return this.connectionPriority;
  }

  public byte[] getLatestWrittenBytes() {
    return this.writtenBytes;
  }

  public byte[] getLatestReadBytes() {
    return this.readBytes;
  }

  @ForType(BluetoothGatt.class)
  private interface BluetoothGattReflector {

    @Direct
    void disconnect();

    @Direct
    void close();
  }
}
