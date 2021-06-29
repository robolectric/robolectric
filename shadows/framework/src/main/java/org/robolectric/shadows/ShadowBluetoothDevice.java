package org.robolectric.shadows;

import static android.bluetooth.BluetoothDevice.BOND_NONE;
import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR2;
import static android.os.Build.VERSION_CODES.M;
import static android.os.Build.VERSION_CODES.O;
import static android.os.Build.VERSION_CODES.Q;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothSocket;
import android.bluetooth.IBluetooth;
import android.content.Context;
import android.os.Handler;
import android.os.ParcelUuid;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.annotation.Resetter;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.reflector.Direct;
import org.robolectric.util.reflector.ForType;
import org.robolectric.util.reflector.Static;

@Implements(BluetoothDevice.class)
public class ShadowBluetoothDevice {
  public static BluetoothDevice newInstance(String address) {
    return ReflectionHelpers.callConstructor(
        BluetoothDevice.class, ReflectionHelpers.ClassParameter.from(String.class, address));
  }

  @Resetter
  public static void reset() {
    bluetoothSocket = null;
  }

  private static BluetoothSocket bluetoothSocket = null;

  @RealObject private BluetoothDevice realBluetoothDevice;
  private String name;
  private ParcelUuid[] uuids;
  private int bondState = BOND_NONE;
  private boolean createdBond = false;
  private boolean fetchUuidsWithSdpResult = false;
  private int fetchUuidsWithSdpCount = 0;
  private int type = BluetoothDevice.DEVICE_TYPE_UNKNOWN;
  private final List<BluetoothGatt> bluetoothGatts = new ArrayList<>();
  private Boolean pairingConfirmation = null;
  private byte[] pin = null;
  private String alias;
  private boolean shouldThrowOnGetAliasName = false;
  private BluetoothClass bluetoothClass = null;

  /**
   * Implements getService() in the same way the original method does, but ignores any Exceptions
   * from invoking {@link android.bluetooth.BluetoothAdapter#getBluetoothService}.
   */
  @Implementation
  protected static IBluetooth getService() {
    // Attempt to call the underlying getService method, but ignore any Exceptions. This allows us
    // to easily create BluetoothDevices for testing purposes without having any actual Bluetooth
    // capability.
    try {
      return reflector(BluetoothDeviceReflector.class).getService();
    } catch (Exception e) {
      // No-op.
    }
    return null;
  }

  public void setName(String name) {
    this.name = name;
  }

  /**
   * Sets the alias name of the device.
   *
   * <p>Alias is the locally modified name of a remote device.
   *
   * <p>Alias Name is not part of the supported SDK, and accessed via reflection.
   *
   * @param alias alias name.
   */
  public void setAlias(String alias) {
    this.alias = alias;
  }

  /**
   * Sets if a runtime exception is thrown when the alias name of the device is accessed.
   *
   * <p>Intended to replicate what may happen if the unsupported SDK is changed.
   *
   * <p>Alias is the locally modified name of a remote device.
   *
   * <p>Alias Name is not part of the supported SDK, and accessed via reflection.
   *
   * @param shouldThrow if getAliasName() should throw when called.
   */
  public void setThrowOnGetAliasName(boolean shouldThrow) {
    shouldThrowOnGetAliasName = shouldThrow;
  }

  @Implementation
  protected String getName() {
    return name;
  }

  @Implementation
  protected String getAlias() {
    return alias;
  }

  @Implementation(maxSdk = Q)
  protected String getAliasName() throws ReflectiveOperationException {
    // Mimicking if the officially supported function is changed.
    if (shouldThrowOnGetAliasName) {
      throw new ReflectiveOperationException("Exception on getAliasName");
    }

    // Matches actual implementation.
    String name = getAlias();
    return name != null ? name : getName();
  }

  /** Sets the return value for {@link BluetoothDevice#getType}. */
  public void setType(int type) {
    this.type = type;
  }

  /**
   * Overrides behavior of {@link BluetoothDevice#getType} to return pre-set result.
   *
   * @return Value set by calling {@link ShadowBluetoothDevice#setType}. If setType has not
   *     previously been called, will return BluetoothDevice.DEVICE_TYPE_UNKNOWN.
   */
  @Implementation(minSdk = JELLY_BEAN_MR2)
  protected int getType() {
    return type;
  }

  /** Sets the return value for {@link BluetoothDevice#getUuids}. */
  public void setUuids(ParcelUuid[] uuids) {
    this.uuids = uuids;
  }

  /**
   * Overrides behavior of {@link BluetoothDevice#getUuids} to return pre-set result.
   *
   * @return Value set by calling {@link ShadowBluetoothDevice#setUuids}. If setUuids has not
   *     previously been called, will return null.
   */
  @Implementation
  protected ParcelUuid[] getUuids() {
    return uuids;
  }

  /** Sets value of bond state for {@link BluetoothDevice#getBondState}. */
  public void setBondState(int bondState) {
    this.bondState = bondState;
  }

  /**
   * Overrides behavior of {@link BluetoothDevice#getBondState} to return pre-set result.
   *
   * @returns Value set by calling {@link ShadowBluetoothDevice#setBondState}. If setBondState has
   *     not previously been called, will return {@link BluetoothDevice#BOND_NONE} to indicate the
   *     device is not bonded.
   */
  @Implementation
  protected int getBondState() {
    return bondState;
  }

  /** Sets whether this device has been bonded with. */
  public void setCreatedBond(boolean createdBond) {
    this.createdBond = createdBond;
  }

  /** Returns whether this device has been bonded with. */
  @Implementation
  protected boolean createBond() {
    return createdBond;
  }

  @Implementation
  protected boolean setPin(byte[] pin) {
    this.pin = pin;
    return true;
  }

  /**
   * Get the PIN previously set with a call to {@link BluetoothDevice#setPin(byte[])}, or null if no
   * PIN has been set.
   */
  public byte[] getPin() {
    return pin;
  }

  @Implementation
  public boolean setPairingConfirmation(boolean confirm) {
    this.pairingConfirmation = confirm;
    return true;
  }

  /**
   * Get the confirmation value previously set with a call to {@link
   * BluetoothDevice#setPairingConfirmation(boolean)}, or null if no value is set.
   */
  public Boolean getPairingConfirmation() {
    return pairingConfirmation;
  }

  @Implementation
  protected BluetoothSocket createRfcommSocketToServiceRecord(UUID uuid) throws IOException {
    synchronized (ShadowBluetoothDevice.class) {
      if (bluetoothSocket == null) {
        bluetoothSocket = Shadow.newInstanceOf(BluetoothSocket.class);
      }
    }
    return bluetoothSocket;
  }

  /** Sets value of the return result for {@link BluetoothDevice#fetchUuidsWithSdp}. */
  public void setFetchUuidsWithSdpResult(boolean fetchUuidsWithSdpResult) {
    this.fetchUuidsWithSdpResult = fetchUuidsWithSdpResult;
  }

  /**
   * Overrides behavior of {@link BluetoothDevice#fetchUuidsWithSdp}. This method updates the
   * counter which counts the number of invocations of this method.
   *
   * @returns Value set by calling {@link ShadowBluetoothDevice#setFetchUuidsWithSdpResult}. If not
   *     previously set, will return false by default.
   */
  @Implementation
  protected boolean fetchUuidsWithSdp() {
    fetchUuidsWithSdpCount++;
    return fetchUuidsWithSdpResult;
  }

  /** Returns the number of times fetchUuidsWithSdp has been called. */
  public int getFetchUuidsWithSdpCount() {
    return fetchUuidsWithSdpCount;
  }

  @Implementation(minSdk = JELLY_BEAN_MR2)
  protected BluetoothGatt connectGatt(
      Context context, boolean autoConnect, BluetoothGattCallback callback) {
    return connectGatt(callback);
  }

  @Implementation(minSdk = M)
  protected BluetoothGatt connectGatt(
      Context context, boolean autoConnect, BluetoothGattCallback callback, int transport) {
    return connectGatt(callback);
  }

  @Implementation(minSdk = O)
  protected BluetoothGatt connectGatt(
      Context context,
      boolean autoConnect,
      BluetoothGattCallback callback,
      int transport,
      int phy,
      Handler handler) {
    return connectGatt(callback);
  }

  private BluetoothGatt connectGatt(BluetoothGattCallback callback) {
    BluetoothGatt bluetoothGatt = ShadowBluetoothGatt.newInstance(realBluetoothDevice);
    bluetoothGatts.add(bluetoothGatt);
    ShadowBluetoothGatt shadowBluetoothGatt = Shadow.extract(bluetoothGatt);
    shadowBluetoothGatt.setGattCallback(callback);
    return bluetoothGatt;
  }

  /**
   * Returns all {@link BluetoothGatt} objects created by calling {@link
   * ShadowBluetoothDevice#connectGatt}.
   */
  public List<BluetoothGatt> getBluetoothGatts() {
    return bluetoothGatts;
  }

  /**
   * Causes {@link BluetoothGattCallback#onConnectionStateChange to be called for every GATT client.
   * @param status Status of the GATT operation
   * @param newState The new state of the GATT profile
   */
  public void simulateGattConnectionChange(int status, int newState) {
    for (BluetoothGatt bluetoothGatt : bluetoothGatts) {
      ShadowBluetoothGatt shadowBluetoothGatt = Shadow.extract(bluetoothGatt);
      BluetoothGattCallback gattCallback = shadowBluetoothGatt.getGattCallback();
      gattCallback.onConnectionStateChange(bluetoothGatt, status, newState);
    }
  }

  /**
   * Overrides behavior of {@link BluetoothDevice#getBluetoothClass} to return pre-set result.
   *
   * @return Value set by calling {@link ShadowBluetoothDevice#setBluetoothClass}. If setType has
   *     not previously been called, will return null.
   */
  @Implementation
  public BluetoothClass getBluetoothClass() {
    return bluetoothClass;
  }

  /** Sets the return value for {@link BluetoothDevice#getBluetoothClass}. */
  public void setBluetoothClass(BluetoothClass bluetoothClass) {
    this.bluetoothClass = bluetoothClass;
  }

  @ForType(BluetoothDevice.class)
  interface BluetoothDeviceReflector {

    @Static
    @Direct
    IBluetooth getService();
  }
}
