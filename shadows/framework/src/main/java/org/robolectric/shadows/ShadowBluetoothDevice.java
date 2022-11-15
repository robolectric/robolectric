package org.robolectric.shadows;

import static android.bluetooth.BluetoothDevice.BOND_NONE;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR2;
import static android.os.Build.VERSION_CODES.M;
import static android.os.Build.VERSION_CODES.O;
import static android.os.Build.VERSION_CODES.O_MR1;
import static android.os.Build.VERSION_CODES.Q;
import static android.os.Build.VERSION_CODES.S;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.annotation.IntRange;
import android.app.ActivityThread;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothSocket;
import android.bluetooth.BluetoothStatusCodes;
import android.bluetooth.IBluetooth;
import android.content.Context;
import android.os.Build.VERSION;
import android.os.Handler;
import android.os.ParcelUuid;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.annotation.Resetter;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.reflector.Direct;
import org.robolectric.util.reflector.ForType;
import org.robolectric.util.reflector.Static;

/** Shadow for {@link BluetoothDevice}. */
@Implements(value = BluetoothDevice.class, looseSignatures = true)
public class ShadowBluetoothDevice {
  @Deprecated // Prefer {@link android.bluetooth.BluetoothAdapter#getRemoteDevice}
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
  private boolean shouldThrowSecurityExceptions = false;
  private final Map<Integer, byte[]> metadataMap = new HashMap<>();
  private int batteryLevel = BluetoothDevice.BATTERY_LEVEL_BLUETOOTH_OFF;
  private boolean isInSilenceMode = false;

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
  @Implementation
  public Object setAlias(Object alias) {
    this.alias = (String) alias;
    if (RuntimeEnvironment.getApiLevel() >= S) {
      return BluetoothStatusCodes.SUCCESS;
    } else {
      return true;
    }
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

  /**
   * Sets if a runtime exception is thrown when bluetooth methods with BLUETOOTH_CONNECT permission
   * pre-requisites are accessed.
   *
   * <p>Intended to replicate what may happen if user has not enabled nearby device permissions.
   *
   * @param shouldThrow if methods should throw SecurityExceptions without enabled permissions when
   *     called.
   */
  public void setShouldThrowSecurityExceptions(boolean shouldThrow) {
    shouldThrowSecurityExceptions = shouldThrow;
  }

  @Implementation
  protected String getName() {
    checkForBluetoothConnectPermission();
    return name;
  }

  @Implementation
  protected String getAlias() {
    checkForBluetoothConnectPermission();
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
    checkForBluetoothConnectPermission();
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
    checkForBluetoothConnectPermission();
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
    checkForBluetoothConnectPermission();
    return bondState;
  }

  /** Sets whether this device has been bonded with. */
  public void setCreatedBond(boolean createdBond) {
    this.createdBond = createdBond;
  }

  /** Returns whether this device has been bonded with. */
  @Implementation
  protected boolean createBond() {
    checkForBluetoothConnectPermission();
    return createdBond;
  }

  @Implementation(minSdk = Q)
  protected BluetoothSocket createInsecureL2capChannel(int psm) throws IOException {
    checkForBluetoothConnectPermission();
    return reflector(BluetoothDeviceReflector.class, realBluetoothDevice)
        .createInsecureL2capChannel(psm);
  }

  @Implementation(minSdk = Q)
  protected BluetoothSocket createL2capChannel(int psm) throws IOException {
    checkForBluetoothConnectPermission();
    return reflector(BluetoothDeviceReflector.class, realBluetoothDevice).createL2capChannel(psm);
  }

  @Implementation
  protected boolean removeBond() {
    checkForBluetoothConnectPermission();
    boolean result = createdBond;
    createdBond = false;
    return result;
  }

  @Implementation
  protected boolean setPin(byte[] pin) {
    checkForBluetoothConnectPermission();
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
    checkForBluetoothConnectPermission();
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
    checkForBluetoothConnectPermission();
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
    checkForBluetoothConnectPermission();
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
    checkForBluetoothConnectPermission();
    return connectGatt(callback);
  }

  @Implementation(minSdk = M)
  protected BluetoothGatt connectGatt(
      Context context, boolean autoConnect, BluetoothGattCallback callback, int transport) {
    checkForBluetoothConnectPermission();
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
    checkForBluetoothConnectPermission();
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
    checkForBluetoothConnectPermission();
    return bluetoothClass;
  }

  /** Sets the return value for {@link BluetoothDevice#getBluetoothClass}. */
  public void setBluetoothClass(BluetoothClass bluetoothClass) {
    this.bluetoothClass = bluetoothClass;
  }

  @Implementation(minSdk = Q)
  protected boolean setMetadata(int key, byte[] value) {
    checkForBluetoothConnectPermission();
    metadataMap.put(key, value);
    return true;
  }

  @Implementation(minSdk = Q)
  protected byte[] getMetadata(int key) {
    checkForBluetoothConnectPermission();
    return metadataMap.get(key);
  }

  public void setBatteryLevel(@IntRange(from = -100, to = 100) int batteryLevel) {
    this.batteryLevel = batteryLevel;
  }

  @Implementation(minSdk = O_MR1)
  protected int getBatteryLevel() {
    checkForBluetoothConnectPermission();
    return batteryLevel;
  }

  @Implementation(minSdk = Q)
  public boolean setSilenceMode(boolean isInSilenceMode) {
    checkForBluetoothConnectPermission();
    this.isInSilenceMode = isInSilenceMode;
    return true;
  }

  @Implementation(minSdk = Q)
  protected boolean isInSilenceMode() {
    checkForBluetoothConnectPermission();
    return isInSilenceMode;
  }

  @ForType(BluetoothDevice.class)
  interface BluetoothDeviceReflector {

    @Static
    @Direct
    IBluetooth getService();

    @Direct
    BluetoothSocket createInsecureL2capChannel(int psm);

    @Direct
    BluetoothSocket createL2capChannel(int psm);
  }

  static ShadowInstrumentation getShadowInstrumentation() {
    ActivityThread activityThread = (ActivityThread) RuntimeEnvironment.getActivityThread();
    return Shadow.extract(activityThread.getInstrumentation());
  }

  private void checkForBluetoothConnectPermission() {
    if (shouldThrowSecurityExceptions
        && VERSION.SDK_INT >= S
        && !checkPermission(android.Manifest.permission.BLUETOOTH_CONNECT)) {
      throw new SecurityException("Bluetooth connect permission required.");
    }
  }

  static boolean checkPermission(String permission) {
    return getShadowInstrumentation()
            .checkPermission(permission, android.os.Process.myPid(), android.os.Process.myUid())
        == PERMISSION_GRANTED;
  }
}
