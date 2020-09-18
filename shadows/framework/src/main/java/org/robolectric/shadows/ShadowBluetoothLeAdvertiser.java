package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.O;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.AdvertisingSet;
import android.bluetooth.le.AdvertisingSetCallback;
import android.bluetooth.le.AdvertisingSetParameters;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.bluetooth.le.PeriodicAdvertisingParameters;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelUuid;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.UUID;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.util.ReflectionHelpers;

/** Adds robolectric support for BLE advertising. */
@Implements(value = BluetoothLeAdvertiser.class, minSdk = LOLLIPOP)
public class ShadowBluetoothLeAdvertiser {

  private static BluetoothLeAdvertiser bluetoothLeAdvertiser;
  private static BluetoothAdapter bluetoothAdapter;
  private static final int MAX_LEGACY_ADVERTISING_DATA_BYTES = 31;
  private static final int MANUFACTURER_SPECIFIC_DATA_LENGTH = 2;
  // Each fields need one byte for field length and another byte for field type.
  private static final int OVERHEAD_BYTES_PER_FIELD = 2;
  private static final int MOCK_TX_POWER = -15;
  public static final ParcelUuid BASE_UUID =
      ParcelUuid.fromString("00000000-0000-1000-8000-00805F9B34FB");

  /** Length of bytes for 16 bit UUID */
  public static final int UUID_BYTES_16_BIT = 2;
  /** Length of bytes for 32 bit UUID */
  public static final int UUID_BYTES_32_BIT = 4;
  /** Length of bytes for 128 bit UUID */
  public static final int UUID_BYTES_128_BIT = 16;

  static BluetoothLeAdvertiser getInstance() {
    if (bluetoothLeAdvertiser == null) {
      bluetoothLeAdvertiser = newInstance();
    }
    return bluetoothLeAdvertiser;
  }

  private final ArrayList<AdvertiseCallback> advertisers = new ArrayList<>();

  @SuppressLint("PrivateApi")
  private static BluetoothLeAdvertiser newInstance() {
    try {
      Class<?> iBluetoothManagerClass =
          Shadow.class.getClassLoader().loadClass("android.bluetooth.IBluetoothManager");
      return Shadow.newInstance(
          BluetoothLeAdvertiser.class,
          new Class<?>[] {iBluetoothManagerClass},
          new Object[] {null});

    } catch (ClassNotFoundException e) {
      return null;
    }
  }

  @Implementation
  public void startAdvertising(
      AdvertiseSettings settings, AdvertiseData advertiseData, final AdvertiseCallback callback) {
    startAdvertising(settings, advertiseData, null, callback);
  }

  @Implementation
  public void startAdvertising(
      AdvertiseSettings settings,
      AdvertiseData advertiseData,
      AdvertiseData scanResponse,
      final AdvertiseCallback callback) {
    boolean connectable = settings.isConnectable();
    if (totalBytes(advertiseData, connectable) > MAX_LEGACY_ADVERTISING_DATA_BYTES) {
      callback.onStartFailure(AdvertiseCallback.ADVERTISE_FAILED_DATA_TOO_LARGE);
    } else if (advertisers.contains(callback)) {
      callback.onStartFailure(AdvertiseCallback.ADVERTISE_FAILED_ALREADY_STARTED);
    } else {
      advertisers.add(callback);
      callback.onStartSuccess(settings);
    }
  }

  @Implementation
  public void stopAdvertising(final AdvertiseCallback callback) {
    advertisers.remove(callback);
  }

  @Implementation(minSdk = O)
  public void startAdvertisingSet(
      AdvertisingSetParameters parameters,
      AdvertiseData advertiseData,
      AdvertiseData scanResponse,
      PeriodicAdvertisingParameters periodicParameters,
      AdvertiseData periodicData,
      AdvertisingSetCallback callback) {
    startAdvertisingSet(
        parameters,
        advertiseData,
        scanResponse,
        periodicParameters,
        periodicData,
        0,
        0,
        callback,
        new Handler(Looper.getMainLooper()));
  }

  @Implementation(minSdk = O)
  public void startAdvertisingSet(
      AdvertisingSetParameters parameters,
      AdvertiseData advertiseData,
      AdvertiseData scanResponse,
      PeriodicAdvertisingParameters periodicParameters,
      AdvertiseData periodicData,
      int duration,
      int maxExtendedAdvertisingEvents,
      AdvertisingSetCallback callback,
      Handler handler) {
    boolean connectable = parameters.isConnectable();
    if (totalBytes(advertiseData, connectable) > MAX_LEGACY_ADVERTISING_DATA_BYTES
        || totalBytes(scanResponse, false) > MAX_LEGACY_ADVERTISING_DATA_BYTES) {
      callback.onAdvertisingSetStarted(
          null, MOCK_TX_POWER, AdvertisingSetCallback.ADVERTISE_FAILED_DATA_TOO_LARGE);
    } else {
      callback.onAdvertisingSetStarted(
          newAdvertisingSet(), MOCK_TX_POWER, AdvertisingSetCallback.ADVERTISE_SUCCESS);
    }
  }

  @Implementation(minSdk = O)
  public void stopAdvertisingSet(AdvertisingSetCallback callback) {
    callback.onAdvertisingSetStopped(newAdvertisingSet());
  }

  /** Sets the bluetooth adapter object. */
  public void setBluetoothAdapter(BluetoothAdapter adapter) {
    bluetoothAdapter = adapter;
  }

  /** Returns a new instance of AdvertisingSet. */
  public static AdvertisingSet newAdvertisingSet() {
    return ReflectionHelpers.callConstructor(AdvertisingSet.class);
  }

  /** Calculates the total number of bytes in the advertise data. */
  private static int totalBytes(AdvertiseData data, boolean connectable) {
    if (data == null) {
      return 0;
    }
    int size = 0;
    int flagsBytes = 3;
    if (connectable) {
      size += flagsBytes;
    }
    if (data.getServiceUuids() != null) {
      int num16BitUuids = 0;
      int num32BitUuids = 0;
      int num128BitUuids = 0;
      for (ParcelUuid uuid : data.getServiceUuids()) {
        if (is16BitUuid(uuid)) {
          ++num16BitUuids;
        } else if (is32BitUuid(uuid)) {
          ++num32BitUuids;
        } else {
          ++num128BitUuids;
        }
      }
      // 16 bit service uuids are grouped into one field when doing advertising.
      if (num16BitUuids != 0) {
        size += OVERHEAD_BYTES_PER_FIELD + num16BitUuids * UUID_BYTES_16_BIT;
      }
      // 32 bit service uuids are grouped into one field when doing advertising.
      if (num32BitUuids != 0) {
        size += OVERHEAD_BYTES_PER_FIELD + num32BitUuids * UUID_BYTES_32_BIT;
      }
      // 128 bit service uuids are grouped into one field when doing advertising.
      if (num128BitUuids != 0) {
        size += OVERHEAD_BYTES_PER_FIELD + num128BitUuids * UUID_BYTES_128_BIT;
      }
    }

    for (ParcelUuid uuid : data.getServiceData().keySet()) {
      int uuidLen = uuidToBytes(uuid).length;
      size += OVERHEAD_BYTES_PER_FIELD + uuidLen + byteLength(data.getServiceData().get(uuid));
    }
    for (int i = 0; i < data.getManufacturerSpecificData().size(); ++i) {
      size +=
          OVERHEAD_BYTES_PER_FIELD
              + MANUFACTURER_SPECIFIC_DATA_LENGTH
              + byteLength(data.getManufacturerSpecificData().valueAt(i));
    }
    if (data.getIncludeTxPowerLevel()) {
      size += OVERHEAD_BYTES_PER_FIELD + 1; // tx power level value is one byte.
    }
    if (data.getIncludeDeviceName() && bluetoothAdapter.getName() != null) {
      size += OVERHEAD_BYTES_PER_FIELD + bluetoothAdapter.getName().length();
    }
    return size;
  }

  /** Returns the length of a given array and 0 if null. */
  private static int byteLength(byte[] array) {
    return array == null ? 0 : array.length;
  }

  /**
   * Parse UUID to bytes. The returned value is shortest representation, a 16-bit, 32-bit or 128-bit
   * UUID, Note returned value is little endian (Bluetooth).
   *
   * @param uuid uuid to parse.
   * @return shortest representation of {@code uuid} as bytes.
   * @throws IllegalArgumentException If the {@code uuid} is null.
   */
  public static byte[] uuidToBytes(ParcelUuid uuid) {
    if (uuid == null) {
      throw new IllegalArgumentException("uuid cannot be null");
    }

    if (is16BitUuid(uuid)) {
      byte[] uuidBytes = new byte[UUID_BYTES_16_BIT];
      int uuidVal = getServiceIdentifierFromParcelUuid(uuid);
      uuidBytes[0] = (byte) (uuidVal & 0xFF);
      uuidBytes[1] = (byte) ((uuidVal & 0xFF00) >> 8);
      return uuidBytes;
    }

    if (is32BitUuid(uuid)) {
      byte[] uuidBytes = new byte[UUID_BYTES_32_BIT];
      int uuidVal = getServiceIdentifierFromParcelUuid(uuid);
      uuidBytes[0] = (byte) (uuidVal & 0xFF);
      uuidBytes[1] = (byte) ((uuidVal & 0xFF00) >> 8);
      uuidBytes[2] = (byte) ((uuidVal & 0xFF0000) >> 16);
      uuidBytes[3] = (byte) ((uuidVal & 0xFF000000) >> 24);
      return uuidBytes;
    }

    // Construct a 128 bit UUID.
    long msb = uuid.getUuid().getMostSignificantBits();
    long lsb = uuid.getUuid().getLeastSignificantBits();

    byte[] uuidBytes = new byte[UUID_BYTES_128_BIT];
    ByteBuffer buf = ByteBuffer.wrap(uuidBytes).order(ByteOrder.LITTLE_ENDIAN);
    buf.putLong(8, msb);
    buf.putLong(0, lsb);
    return uuidBytes;
  }

  /**
   * Check whether the given parcelUuid can be converted to 16 bit bluetooth uuid.
   *
   * @param parcelUuid to be converted
   * @return true if the parcelUuid can be converted to 16 bit uuid, false otherwise.
   */
  public static boolean is16BitUuid(ParcelUuid parcelUuid) {
    UUID uuid = parcelUuid.getUuid();
    if (uuid.getLeastSignificantBits() != BASE_UUID.getUuid().getLeastSignificantBits()) {
      return false;
    }
    return ((uuid.getMostSignificantBits() & 0xFFFF0000FFFFFFFFL) == 0x1000L);
  }

  /**
   * Check whether the given parcelUuid can be converted to 32 bit bluetooth uuid.
   *
   * @param parcelUuid to be converted
   * @return true if the parcelUuid can be converted to 32 bit uuid, false otherwise.
   */
  public static boolean is32BitUuid(ParcelUuid parcelUuid) {
    UUID uuid = parcelUuid.getUuid();
    if (uuid.getLeastSignificantBits() != BASE_UUID.getUuid().getLeastSignificantBits()) {
      return false;
    }
    if (is16BitUuid(parcelUuid)) {
      return false;
    }
    return ((uuid.getMostSignificantBits() & 0xFFFFFFFFL) == 0x1000L);
  }

  public static int getServiceIdentifierFromParcelUuid(ParcelUuid parcelUuid) {
    UUID uuid = parcelUuid.getUuid();
    long value = (uuid.getMostSignificantBits() & 0xFFFFFFFF00000000L) >>> 32;
    return (int) value;
  }

  public ShadowBluetoothLeAdvertiser() {}
}
