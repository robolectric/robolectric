package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.BAKLAVA;
import static android.os.Build.VERSION_CODES.O;
import static android.os.Build.VERSION_CODES.O_MR1;
import static android.os.Build.VERSION_CODES.R;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothStatusCodes;
import android.bluetooth.IBluetoothGatt;
import android.content.AttributionSource;
import android.content.Context;
import android.os.Build;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;
import java.util.function.BooleanSupplier;
import java.util.function.IntSupplier;
import javax.annotation.Nullable;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Filter;
import org.robolectric.annotation.Filter.Order;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.annotation.ReflectorObject;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.util.PerfStatsCollector;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.reflector.Constructor;
import org.robolectric.util.reflector.ForType;
import org.robolectric.util.reflector.WithType;

/** Shadow implementation of {@link BluetoothGatt}. */
@Implements(BluetoothGatt.class)
public class ShadowBluetoothGatt {

  private static final String NULL_CALLBACK_MSG = "BluetoothGattCallback can not be null.";

  private volatile BluetoothGattCallback bluetoothGattCallback;
  private volatile int connectionPriority = BluetoothGatt.CONNECTION_PRIORITY_BALANCED;
  private volatile boolean isConnected = false;
  private volatile boolean isClosed = false;
  private volatile byte[] writtenBytes;
  private volatile BluetoothGattWriteListener writeListener;
  private volatile byte[] readBytes;
  private int rssi = -50;
  private int txPhy = BluetoothDevice.PHY_LE_1M;
  private int rxPhy = BluetoothDevice.PHY_LE_1M;
  private boolean beginReliableWriteResult = true;

  private volatile BooleanSupplier discoverServicesResult = () -> true;
  private volatile IntSupplier discoverServicesStatus = () -> BluetoothGatt.GATT_SUCCESS;

  private volatile BooleanSupplier readCharacteristicResult = () -> true;
  private volatile IntSupplier readCharacteristicStatus = () -> BluetoothGatt.GATT_SUCCESS;

  private volatile BooleanSupplier writeCharacteristicResult = () -> true;
  private volatile IntSupplier writeCharacteristicStatus = () -> BluetoothGatt.GATT_SUCCESS;

  private volatile BooleanSupplier readDescriptorResult = () -> true;
  private volatile IntSupplier readDescriptorStatus = () -> BluetoothGatt.GATT_SUCCESS;

  private volatile BooleanSupplier writeDescriptorResult = () -> true;
  private volatile IntSupplier writeDescriptorStatus = () -> BluetoothGatt.GATT_SUCCESS;

  private volatile BooleanSupplier requestMtuResult = () -> true;
  private volatile IntSupplier requestMtuStatus = () -> BluetoothGatt.GATT_SUCCESS;

  private volatile BooleanSupplier readRemoteRssiResult = () -> true;
  private volatile IntSupplier readRemoteRssiStatus = () -> BluetoothGatt.GATT_SUCCESS;

  private volatile IntSupplier readPhyStatus = () -> BluetoothGatt.GATT_SUCCESS;
  private volatile IntSupplier setPreferredPhyStatus = () -> BluetoothGatt.GATT_SUCCESS;

  private volatile BooleanSupplier requestConnectionPriorityResult = () -> true;
  private volatile IntSupplier requestConnectionPriorityStatus = () -> BluetoothGatt.GATT_SUCCESS;

  private volatile IntSupplier connectStatus = () -> BluetoothGatt.GATT_SUCCESS;
  private volatile IntSupplier disconnectStatus = () -> BluetoothGatt.GATT_SUCCESS;

  private volatile BooleanSupplier requestSubrateModeResult = () -> true;
  private volatile IntSupplier requestSubrateModeStatus = () -> BluetoothGatt.GATT_SUCCESS;

  public void setDiscoverServicesResult(BooleanSupplier result) {
    this.discoverServicesResult = result == null ? () -> true : result;
  }

  public void setDiscoverServicesResult(boolean result) {
    this.discoverServicesResult = () -> result;
  }

  public void setDiscoverServicesStatus(IntSupplier status) {
    this.discoverServicesStatus = status == null ? () -> BluetoothGatt.GATT_SUCCESS : status;
  }

  public void setDiscoverServicesStatus(int status) {
    this.discoverServicesStatus = () -> status;
  }

  public void setReadCharacteristicResult(BooleanSupplier result) {
    this.readCharacteristicResult = result == null ? () -> true : result;
  }

  public void setReadCharacteristicResult(boolean result) {
    this.readCharacteristicResult = () -> result;
  }

  public void setReadCharacteristicStatus(IntSupplier status) {
    this.readCharacteristicStatus = status == null ? () -> BluetoothGatt.GATT_SUCCESS : status;
  }

  public void setReadCharacteristicStatus(int status) {
    this.readCharacteristicStatus = () -> status;
  }

  public void setWriteCharacteristicResult(BooleanSupplier result) {
    this.writeCharacteristicResult = result == null ? () -> true : result;
  }

  public void setWriteCharacteristicResult(boolean result) {
    this.writeCharacteristicResult = () -> result;
  }

  public void setWriteCharacteristicStatus(IntSupplier status) {
    this.writeCharacteristicStatus = status == null ? () -> BluetoothGatt.GATT_SUCCESS : status;
  }

  public void setWriteCharacteristicStatus(int status) {
    this.writeCharacteristicStatus = () -> status;
  }

  public void setReadDescriptorResult(BooleanSupplier result) {
    this.readDescriptorResult = result == null ? () -> true : result;
  }

  public void setReadDescriptorResult(boolean result) {
    this.readDescriptorResult = () -> result;
  }

  public void setReadDescriptorStatus(IntSupplier status) {
    this.readDescriptorStatus = status == null ? () -> BluetoothGatt.GATT_SUCCESS : status;
  }

  public void setReadDescriptorStatus(int status) {
    this.readDescriptorStatus = () -> status;
  }

  public void setWriteDescriptorResult(BooleanSupplier result) {
    this.writeDescriptorResult = result == null ? () -> true : result;
  }

  public void setWriteDescriptorResult(boolean result) {
    this.writeDescriptorResult = () -> result;
  }

  public void setWriteDescriptorStatus(IntSupplier status) {
    this.writeDescriptorStatus = status == null ? () -> BluetoothGatt.GATT_SUCCESS : status;
  }

  public void setWriteDescriptorStatus(int status) {
    this.writeDescriptorStatus = () -> status;
  }

  public void setRequestMtuResult(BooleanSupplier result) {
    this.requestMtuResult = result == null ? () -> true : result;
  }

  public void setRequestMtuResult(boolean result) {
    this.requestMtuResult = () -> result;
  }

  public void setRequestMtuStatus(IntSupplier status) {
    this.requestMtuStatus = status == null ? () -> BluetoothGatt.GATT_SUCCESS : status;
  }

  public void setRequestMtuStatus(int status) {
    this.requestMtuStatus = () -> status;
  }

  public void setReadRemoteRssiResult(BooleanSupplier result) {
    this.readRemoteRssiResult = result == null ? () -> true : result;
  }

  public void setReadRemoteRssiResult(boolean result) {
    this.readRemoteRssiResult = () -> result;
  }

  public void setReadRemoteRssiStatus(IntSupplier status) {
    this.readRemoteRssiStatus = status == null ? () -> BluetoothGatt.GATT_SUCCESS : status;
  }

  public void setReadRemoteRssiStatus(int status) {
    this.readRemoteRssiStatus = () -> status;
  }

  public void setReadPhyStatus(IntSupplier status) {
    this.readPhyStatus = status == null ? () -> BluetoothGatt.GATT_SUCCESS : status;
  }

  public void setReadPhyStatus(int status) {
    this.readPhyStatus = () -> status;
  }

  public void setPreferredPhyStatus(IntSupplier status) {
    this.setPreferredPhyStatus = status == null ? () -> BluetoothGatt.GATT_SUCCESS : status;
  }

  public void setPreferredPhyStatus(int status) {
    this.setPreferredPhyStatus = () -> status;
  }

  public void setRequestConnectionPriorityResult(BooleanSupplier result) {
    this.requestConnectionPriorityResult = result == null ? () -> true : result;
  }

  public void setRequestConnectionPriorityResult(boolean result) {
    this.requestConnectionPriorityResult = () -> result;
  }

  public void setRequestConnectionPriorityStatus(IntSupplier status) {
    this.requestConnectionPriorityStatus =
        status == null ? () -> BluetoothGatt.GATT_SUCCESS : status;
  }

  public void setRequestConnectionPriorityStatus(int status) {
    this.requestConnectionPriorityStatus = () -> status;
  }

  public void setConnectStatus(IntSupplier status) {
    this.connectStatus = status == null ? () -> BluetoothGatt.GATT_SUCCESS : status;
  }

  public void setConnectStatus(int status) {
    this.connectStatus = () -> status;
  }

  public void setDisconnectStatus(IntSupplier status) {
    this.disconnectStatus = status == null ? () -> BluetoothGatt.GATT_SUCCESS : status;
  }

  public void setDisconnectStatus(int status) {
    this.disconnectStatus = () -> status;
  }

  public void setRequestSubrateModeResult(BooleanSupplier result) {
    this.requestSubrateModeResult = result == null ? () -> true : result;
  }

  public void setRequestSubrateModeResult(boolean result) {
    this.requestSubrateModeResult = () -> result;
  }

  public void setRequestSubrateModeStatus(IntSupplier status) {
    this.requestSubrateModeStatus = status == null ? () -> BluetoothGatt.GATT_SUCCESS : status;
  }

  public void setRequestSubrateModeStatus(int status) {
    this.requestSubrateModeStatus = () -> status;
  }

  // TODO: ShadowBluetoothGatt.services should be removed in favor of just using the real
  // BluetoothGatt.mServices.
  private final Set<BluetoothGattService> discoverableServices = ConcurrentHashMap.newKeySet();
  private final List<BluetoothGattService> services = new CopyOnWriteArrayList<>();
  private final Set<BluetoothGattCharacteristic> characteristicNotificationEnableSet =
      ConcurrentHashMap.newKeySet();

  @RealObject private BluetoothGatt realBluetoothGatt;
  @ReflectorObject protected BluetoothGattReflector bluetoothGattReflector;

  @SuppressLint("PrivateApi")
  public static BluetoothGatt newInstance(BluetoothDevice device) {
    try {
      Class<?> iBluetoothGattClass =
          Shadow.class.getClassLoader().loadClass("android.bluetooth.IBluetoothGatt");

      BluetoothGatt bluetoothGatt;
      int apiLevel = RuntimeEnvironment.getApiLevel();
      if (apiLevel > BAKLAVA) {
        Object gattConnectionSettingsBuilder =
            reflector(BluetoothGattConnectionSettingsBuilderReflector.class).newInstance();
        Object gattConnectionSettings =
            reflector(
                    BluetoothGattConnectionSettingsBuilderReflector.class,
                    gattConnectionSettingsBuilder)
                .build();
        bluetoothGatt =
            reflector(BluetoothGattReflector.class)
                .newInstance(
                    ReflectionHelpers.createNullProxy(IBluetoothGatt.class),
                    device,
                    null,
                    gattConnectionSettings,
                    null,
                    null);
      } else if (apiLevel > R) {
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
      } else if (apiLevel == O) {
        bluetoothGatt =
            Shadow.newInstance(
                BluetoothGatt.class,
                new Class<?>[] {
                  iBluetoothGattClass, BluetoothDevice.class, Integer.TYPE, Integer.TYPE
                },
                new Object[] {null, device, 0, 0});
      } else {
        bluetoothGatt =
            Shadow.newInstance(
                BluetoothGatt.class,
                new Class<?>[] {
                  Context.class, iBluetoothGattClass, BluetoothDevice.class, Integer.TYPE
                },
                new Object[] {RuntimeEnvironment.getApplication(), null, device, 0});
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
  @Implementation
  protected boolean connect() {
    if (bluetoothGattCallback != null) {
      int status = connectStatus.getAsInt();
      this.isConnected = (status == BluetoothGatt.GATT_SUCCESS);
      int state =
          this.isConnected ? BluetoothProfile.STATE_CONNECTED : BluetoothProfile.STATE_DISCONNECTED;
      bluetoothGattCallback.onConnectionStateChange(this.realBluetoothGatt, status, state);
      return true;
    }
    return false;
  }

  /**
   * Disconnects an established connection, or cancels a connection attempt currently in progress.
   */
  @Filter(order = Order.AFTER)
  protected void disconnect() {
    boolean callbackAppropriate = this.isCallbackAppropriate();
    this.isConnected = false;
    if (callbackAppropriate) {
      int status = disconnectStatus.getAsInt();
      bluetoothGattCallback.onConnectionStateChange(
          this.realBluetoothGatt, status, BluetoothProfile.STATE_DISCONNECTED);
    }
  }

  /** Close this Bluetooth GATT client. */
  @Filter(order = Order.AFTER)
  protected void close() {
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
    if (!requestConnectionPriorityResult.getAsBoolean()) {
      return false;
    }
    if (priority == BluetoothGatt.CONNECTION_PRIORITY_HIGH
        || priority == BluetoothGatt.CONNECTION_PRIORITY_BALANCED
        || priority == BluetoothGatt.CONNECTION_PRIORITY_LOW_POWER) {
      this.connectionPriority = priority;
      if (bluetoothGattCallback != null) {
        // Interval, latency, timeout values are dummies as they depend on the priority and
        // negotiation
        int interval = 0;
        int latency = 0;
        int timeout = 0;
        int status = requestConnectionPriorityStatus.getAsInt();
        bluetoothGattCallback.onConnectionUpdated(
            this.realBluetoothGatt, interval, latency, timeout, status);
      }
      return true;
    }
    throw new IllegalArgumentException("connection priority not within valid range");
  }

  /**
   * Overrides {@link BluetoothGatt#requestMtu} to always fail before {@link
   * ShadowBluetoothGatt#setGattCallback} is called, and always succeed after.
   */
  @Implementation(minSdk = O)
  protected boolean requestMtu(int mtu) {
    if (!requestMtuResult.getAsBoolean()) {
      return false;
    }
    if (bluetoothGattCallback == null) {
      return false;
    }

    int status = requestMtuStatus.getAsInt();
    bluetoothGattCallback.onMtuChanged(this.realBluetoothGatt, mtu, status);
    return true;
  }

  /**
   * Overrides {@link BluetoothGatt#setPreferredPhy} to always fail before {@link
   * ShadowBluetoothGatt#setGattCallback} is called, and always succeed after.
   */
  @Implementation(minSdk = O)
  protected void setPreferredPhy(int txPhy, int rxPhy, int phyOptions) {
    if (bluetoothGattCallback == null) {
      return;
    }

    int status = setPreferredPhyStatus.getAsInt();
    bluetoothGattCallback.onPhyUpdate(this.realBluetoothGatt, txPhy, rxPhy, status);
  }

  /**
   * Overrides {@link BluetoothGatt#readPhy} to always fail before {@link
   * ShadowBluetoothGatt#setGattCallback} is called, and always succeed after.
   */
  @Implementation(minSdk = O)
  protected void readPhy() {
    if (bluetoothGattCallback == null) {
      return;
    }

    int status = readPhyStatus.getAsInt();
    bluetoothGattCallback.onPhyRead(this.realBluetoothGatt, txPhy, rxPhy, status);
  }

  /** Sets the values to be returned by {@link BluetoothGatt#readPhy()} */
  public void setPhy(int txPhy, int rxPhy) {
    this.txPhy = txPhy;
    this.rxPhy = rxPhy;
  }

  /**
   * Overrides {@link BluetoothGatt#discoverServices} to always return false unless there are
   * discoverable services made available by {@link ShadowBluetoothGatt#addDiscoverableService}
   *
   * @return true if discoverable service is available and callback response is possible
   */
  @Implementation(minSdk = O)
  protected boolean discoverServices() {
    if (!discoverServicesResult.getAsBoolean()) {
      return false;
    }
    this.services.clear();
    if (!this.discoverableServices.isEmpty()) {
      int status = discoverServicesStatus.getAsInt();
      if (status == BluetoothGatt.GATT_SUCCESS) {
        this.services.addAll(this.discoverableServices);
      }
      if (bluetoothGattCallback != null) {
        bluetoothGattCallback.onServicesDiscovered(this.realBluetoothGatt, status);
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
    // TODO: Remove this method when real BluetoothGatt#getServices() works.
    return new ArrayList<>(this.services);
  }

  /**
   * Overrides {@link BluetoothGatt#getService(UUID)} to return a service with given UUID.
   *
   * @return a service with given UUID that have been discovered through {@link
   *     ShadowBluetoothGatt#discoverServices}.
   */
  @Implementation(minSdk = O)
  @Nullable
  protected BluetoothGattService getService(UUID uuid) {
    // TODO: Remove this method when real BluetoothGatt#getService() works.
    for (BluetoothGattService service : this.services) {
      if (service.getUuid().equals(uuid)) {
        return service;
      }
    }
    return null;
  }

  /**
   * Overrides {@link BluetoothGatt#setCharacteristicNotification} so it returns true (false) if
   * allowCharacteristicNotification (disallowCharacteristicNotification) is called.
   */
  @Implementation(minSdk = O)
  protected boolean setCharacteristicNotification(
      BluetoothGattCharacteristic characteristic, boolean enable) {
    return characteristicNotificationEnableSet.contains(characteristic);
  }

  @Implementation(minSdk = O)
  protected boolean readDescriptor(BluetoothGattDescriptor descriptor) {
    if (!readDescriptorResult.getAsBoolean()) {
      return false;
    }
    if (bluetoothGattCallback == null) {
      throw new IllegalStateException(NULL_CALLBACK_MSG);
    }
    if (descriptor.getCharacteristic() == null
        || descriptor.getCharacteristic().getService() == null) {
      return false;
    }
    if ((descriptor.getPermissions() & BluetoothGattDescriptor.PERMISSION_READ) == 0) {
      return false;
    }
    this.readBytes = descriptor.getValue();
    if (this.readBytes == null) {
      return false;
    }
    int status = readDescriptorStatus.getAsInt();
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      bluetoothGattCallback.onDescriptorRead(
          this.realBluetoothGatt, descriptor, status, this.readBytes);
    } else {
      bluetoothGattCallback.onDescriptorRead(this.realBluetoothGatt, descriptor, status);
    }
    return true;
  }

  /**
   * Sets the RSSI value to be returned by {@link #readRemoteRssi()}.
   *
   * @param rssi The RSSI value.
   */
  public void setRssi(int rssi) {
    this.rssi = rssi;
  }

  @Implementation(minSdk = O)
  protected boolean readRemoteRssi() {
    if (!readRemoteRssiResult.getAsBoolean()) {
      return false;
    }
    if (bluetoothGattCallback == null) {
      return false;
    }
    int status = readRemoteRssiStatus.getAsInt();
    bluetoothGattCallback.onReadRemoteRssi(this.realBluetoothGatt, rssi, status);
    return true;
  }

  public void setBeginReliableWriteResult(boolean beginReliableWriteResult) {
    this.beginReliableWriteResult = beginReliableWriteResult;
  }

  @Implementation(minSdk = O)
  protected boolean beginReliableWrite() {
    return beginReliableWriteResult;
  }

  @Implementation(minSdk = O)
  protected boolean executeReliableWrite() {
    if (bluetoothGattCallback == null) {
      return false;
    }
    bluetoothGattCallback.onReliableWriteCompleted(
        this.realBluetoothGatt, BluetoothGatt.GATT_SUCCESS);
    return true;
  }

  @Implementation(minSdk = O)
  protected void abortReliableWrite() {
    // No-op
  }

  @Implementation(minSdk = BAKLAVA)
  protected int requestSubrateMode(int subrateMode) {
    if (!requestSubrateModeResult.getAsBoolean()) {
      return BluetoothStatusCodes.ERROR_UNKNOWN;
    }
    if (bluetoothGattCallback == null) {
      return BluetoothStatusCodes.ERROR_UNKNOWN;
    }
    int latency = 0;
    int contNum = 0;
    int timeout = 0;
    int status = requestSubrateModeStatus.getAsInt();
    try {
      reflector(BluetoothGattCallbackReflector.class, bluetoothGattCallback)
          .onSubrateChange(realBluetoothGatt, subrateMode, status);
    } catch (IllegalStateException | AssertionError e) {
      // This method was overloaded before being made public. Calling the above method on previous
      // versions of Robolectric will throw an exception, so we rely on this one instead. Do not
      // rely on this method in production code as it is deprecated, not public, and will not be
      // supported by client applications in the future.
      reflector(BluetoothGattCallbackReflector.class, bluetoothGattCallback)
          .onSubrateChange(realBluetoothGatt, subrateMode, latency, contNum, timeout, status);
    }
    return BluetoothStatusCodes.SUCCESS;
  }

  @Implementation(minSdk = O)
  protected boolean writeDescriptor(BluetoothGattDescriptor descriptor) {
    return writeDescriptorInternal(descriptor);
  }

  @Implementation(minSdk = Build.VERSION_CODES.TIRAMISU)
  protected int writeDescriptor(BluetoothGattDescriptor descriptor, byte[] value) {
    descriptor.setValue(value);
    return writeDescriptor(descriptor)
        ? BluetoothGatt.GATT_SUCCESS
        : BluetoothStatusCodes.ERROR_UNKNOWN;
  }

  private boolean writeDescriptorInternal(BluetoothGattDescriptor descriptor) {
    if (!writeDescriptorResult.getAsBoolean()) {
      return false;
    }
    if (bluetoothGattCallback == null) {
      throw new IllegalStateException(NULL_CALLBACK_MSG);
    }
    if (descriptor.getCharacteristic() == null
        || descriptor.getCharacteristic().getService() == null) {
      return false;
    }
    writtenBytes = descriptor.getValue();
    int status = writeDescriptorStatus.getAsInt();
    bluetoothGattCallback.onDescriptorWrite(realBluetoothGatt, descriptor, status);
    return true;
  }

  @Implementation(minSdk = O)
  protected boolean writeCharacteristic(BluetoothGattCharacteristic characteristic) {
    return writeIncomingCharacteristic(characteristic);
  }

  @Implementation(minSdk = Build.VERSION_CODES.TIRAMISU)
  protected int writeCharacteristic(
      BluetoothGattCharacteristic characteristic, byte[] value, int writeType) {
    characteristic.setValue(value);
    boolean writeSuccessCode = writeIncomingCharacteristic(characteristic);
    if (writeSuccessCode) {
      return BluetoothGatt.GATT_SUCCESS;
    }
    return BluetoothStatusCodes.ERROR_UNKNOWN;
  }

  @Implementation(minSdk = O)
  protected boolean readCharacteristic(BluetoothGattCharacteristic characteristic) {
    return readIncomingCharacteristic(characteristic);
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
    if (!writeCharacteristicResult.getAsBoolean()) {
      return false;
    }
    if (bluetoothGattCallback == null) {
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
    BluetoothGattWriteListener listener = this.writeListener;
    if (listener != null) {
      listener.onCharacteristicWrite(characteristic, this.writtenBytes);
    }
    int status = writeCharacteristicStatus.getAsInt();
    bluetoothGattCallback.onCharacteristicWrite(this.realBluetoothGatt, characteristic, status);
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
    if (!readCharacteristicResult.getAsBoolean()) {
      return false;
    }
    if (!isCharacteristicValidForRead(characteristic)) {
      return false;
    }

    this.readBytes = characteristic.getValue();
    if (this.readBytes == null) {
      return true;
    }

    int status = readCharacteristicStatus.getAsInt();
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      bluetoothGattCallback.onCharacteristicRead(
          this.realBluetoothGatt, characteristic, this.readBytes, status);
    } else {
      bluetoothGattCallback.onCharacteristicRead(this.realBluetoothGatt, characteristic, status);
    }

    return true;
  }

  /** Allows the incoming characteristic to be set to enable notification. */
  public void allowCharacteristicNotification(BluetoothGattCharacteristic characteristic) {
    characteristicNotificationEnableSet.add(characteristic);
  }

  /** Disallows the incoming characteristic to be set to enable notification. */
  public void disallowCharacteristicNotification(BluetoothGattCharacteristic characteristic) {
    characteristicNotificationEnableSet.remove(characteristic);
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

  public BluetoothConnectionManager getBluetoothConnectionManager() {
    return BluetoothConnectionManager.getInstance();
  }

  /**
   * Simulate a successful Gatt Client Connection with {@link BluetoothConnectionManager}. Performs
   * a {@link BluetoothGattCallback#onConnectionStateChange} if available.
   *
   * @param remoteAddress address of Gatt client
   */
  public void notifyConnection(String remoteAddress) {
    int status = connectStatus.getAsInt();
    this.isConnected = (status == BluetoothGatt.GATT_SUCCESS);
    if (this.isConnected) {
      BluetoothConnectionManager.getInstance().registerGattClientConnection(remoteAddress);
    }
    if (bluetoothGattCallback != null) {
      int state =
          this.isConnected ? BluetoothProfile.STATE_CONNECTED : BluetoothProfile.STATE_DISCONNECTED;
      bluetoothGattCallback.onConnectionStateChange(this.realBluetoothGatt, status, state);
    }
  }

  /**
   * Simulate a successful Gatt Client Disconnection with {@link BluetoothConnectionManager}.
   * Performs a {@link BluetoothGattCallback#onConnectionStateChange} if available.
   *
   * @param remoteAddress address of Gatt client
   */
  public void notifyDisconnection(String remoteAddress) {
    BluetoothConnectionManager.getInstance().unregisterGattClientConnection(remoteAddress);
    boolean callbackAppropriate = this.isCallbackAppropriate();
    this.isConnected = false;
    if (callbackAppropriate) {
      int status = disconnectStatus.getAsInt();
      bluetoothGattCallback.onConnectionStateChange(
          this.realBluetoothGatt, status, BluetoothProfile.STATE_DISCONNECTED);
    }
  }

  private boolean isCallbackAppropriate() {
    return bluetoothGattCallback != null && this.isConnected;
  }

  private boolean isCharacteristicValidForRead(BluetoothGattCharacteristic characteristic) {
    if (bluetoothGattCallback == null) {
      throw new IllegalStateException(NULL_CALLBACK_MSG);
    }
    return (characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_READ) != 0
        && characteristic.getService() != null;
  }

  @ForType(BluetoothGatt.class)
  private interface BluetoothGattReflector {

    @Constructor
    BluetoothGatt newInstance(
        IBluetoothGatt iGatt,
        BluetoothDevice device,
        AttributionSource source,
        @WithType("android.bluetooth.BluetoothGattConnectionSettings")
            Object gattConnectionSettings,
        @WithType("android.bluetooth.BluetoothGattCallback") Object callback,
        Executor executor);
  }

  @ForType(className = "android.bluetooth.BluetoothGattConnectionSettings$Builder")
  private interface BluetoothGattConnectionSettingsBuilderReflector {
    @Constructor
    @SuppressWarnings("unused")
    Object newInstance();

    Object build();
  }

  @ForType(BluetoothGattCallback.class)
  private interface BluetoothGattCallbackReflector {
    void onSubrateChange(BluetoothGatt gatt, int subrateFactor, int status);

    /**
     * @deprecated Use {@link #onSubrateChange(BluetoothGatt, int, int)} instead.
     */
    @Deprecated(since = "Baklava")
    void onSubrateChange(
        BluetoothGatt gatt, int subrateFactor, int latency, int contNum, int timeout, int status);
  }

  /** Listener for characteristic write events. */
  public interface BluetoothGattWriteListener {
    void onCharacteristicWrite(BluetoothGattCharacteristic characteristic, byte[] value);
  }

  public void setWriteListener(BluetoothGattWriteListener listener) {
    this.writeListener = listener;
  }

  public void notifyCharacteristicChanged(BluetoothGattCharacteristic characteristic) {
    if (bluetoothGattCallback != null) {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        bluetoothGattCallback.onCharacteristicChanged(
            this.realBluetoothGatt, characteristic, characteristic.getValue());
      } else {
        bluetoothGattCallback.onCharacteristicChanged(this.realBluetoothGatt, characteristic);
      }
    }
  }
}
