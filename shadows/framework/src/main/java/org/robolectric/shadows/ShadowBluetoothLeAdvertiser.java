package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.O;
import static android.os.Build.VERSION_CODES.R;
import static android.os.Build.VERSION_CODES.S;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothUuid;
import android.bluetooth.IBluetoothManager;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.os.ParcelUuid;
import java.util.HashSet;
import java.util.Set;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.ReflectorObject;
import org.robolectric.util.PerfStatsCollector;
import org.robolectric.util.reflector.Direct;
import org.robolectric.util.reflector.ForType;

/** Shadow implementation of {@link BluetoothLeAdvertiser}. */
@Implements(value = BluetoothLeAdvertiser.class, minSdk = O)
public class ShadowBluetoothLeAdvertiser {

  private static final String CALLBACK_NULL_MESSAGE = "callback cannot be null.";
  private static final int MAX_LEGACY_ADVERTISING_DATA_BYTES = 31;
  private static final int OVERHEAD_BYTES_PER_FIELD = 2;
  private static final int FLAGS_FIELD_BYTES = 3;
  private static final int MANUFACTURER_SPECIFIC_DATA_LENGTH = 2;
  private static final int SERVICE_DATA_UUID_LENGTH = 2;

  private BluetoothAdapter bluetoothAdapter;
  private final Set<AdvertiseCallback> advertisements = new HashSet<>();
  @ReflectorObject protected BluetoothLeAdvertiserReflector bluetoothLeAdvertiserReflector;

  @Implementation(maxSdk = R)
  protected void __constructor__(IBluetoothManager bluetoothManager) {
    bluetoothLeAdvertiserReflector.__constructor__(bluetoothManager);
    PerfStatsCollector.getInstance().incrementCount("constructShadowBluetoothLeAdvertiser");
    this.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
  }

  @Implementation(minSdk = S)
  protected void __constructor__(BluetoothAdapter bluetoothAdapter) {
    bluetoothLeAdvertiserReflector.__constructor__(bluetoothAdapter);
    PerfStatsCollector.getInstance().incrementCount("constructShadowBluetoothLeAdvertiser");
    this.bluetoothAdapter = bluetoothAdapter;
  }

  /**
   * Start Bluetooth LE Advertising. This method returns immediately, the operation status is
   * delivered through {@code callback}.
   *
   * @param settings Settings for Bluetooth LE advertising.
   * @param advertiseData Advertisement data to be broadcasted.
   * @param callback Callback for advertising status.
   */
  @Implementation
  protected void startAdvertising(
      AdvertiseSettings settings, AdvertiseData advertiseData, AdvertiseCallback callback) {
    startAdvertising(settings, advertiseData, null, callback);
  }

  /**
   * Start Bluetooth LE Advertising. This method returns immediately, the operation status is
   * delivered through {@code callback}.
   *
   * @param settings Settings for Bluetooth LE advertising.
   * @param advertiseData Advertisement data to be broadcasted.
   * @param scanResponse Scan response associated with the advertisement data.
   * @param callback Callback for advertising status.
   * @throws IllegalArgumentException When {@code callback} is not present.
   */
  @Implementation
  protected void startAdvertising(
      AdvertiseSettings settings,
      AdvertiseData advertiseData,
      AdvertiseData scanResponse,
      AdvertiseCallback callback) {

    if (callback == null) {
      throw new IllegalArgumentException(CALLBACK_NULL_MESSAGE);
    }

    boolean isConnectable = settings.isConnectable();

    if (this.getTotalBytes(advertiseData, isConnectable) > MAX_LEGACY_ADVERTISING_DATA_BYTES
        || this.getTotalBytes(scanResponse, false) > MAX_LEGACY_ADVERTISING_DATA_BYTES) {
      callback.onStartFailure(AdvertiseCallback.ADVERTISE_FAILED_DATA_TOO_LARGE);
      return;
    }

    if (advertisements.contains(callback)) {
      callback.onStartFailure(AdvertiseCallback.ADVERTISE_FAILED_ALREADY_STARTED);
      return;
    }

    this.advertisements.add(callback);
    callback.onStartSuccess(settings);
  }

  /**
   * Stop Bluetooth LE advertising. The {@code callback} must be the same one use in {@link
   * ShadowBluetoothLeAdvertiser#startAdvertising}.
   *
   * @param callback {@link AdvertiseCallback} identifies the advertising instance to stop.
   * @throws IllegalArgumentException When the {@code callback} is not a key present in {@code
   *     advertisements}.
   */
  @Implementation
  protected void stopAdvertising(AdvertiseCallback callback) {
    if (callback == null) {
      throw new IllegalArgumentException(CALLBACK_NULL_MESSAGE);
    }
    this.advertisements.remove(callback);
  }

  /** Returns the count of current ongoing Bluetooth LE advertising requests. */
  public int getAdvertisementRequestCount() {
    return this.advertisements.size();
  }

  private int getTotalBytes(AdvertiseData data, boolean isConnectable) {
    if (data == null) {
      return 0;
    }
    // Flags field is omitted if the advertising is not connectable.
    int size = isConnectable ? FLAGS_FIELD_BYTES : 0;
    if (data.getServiceUuids() != null) {
      int num16BitUuids = 0;
      int num32BitUuids = 0;
      int num128BitUuids = 0;
      for (ParcelUuid uuid : data.getServiceUuids()) {
        if (BluetoothUuid.is16BitUuid(uuid)) {
          ++num16BitUuids;
        } else if (BluetoothUuid.is32BitUuid(uuid)) {
          ++num32BitUuids;
        } else {
          ++num128BitUuids;
        }
      }
      // 16 bit service uuids are grouped into one field when doing advertising.
      if (num16BitUuids != 0) {
        size += OVERHEAD_BYTES_PER_FIELD + num16BitUuids * BluetoothUuid.UUID_BYTES_16_BIT;
      }
      // 32 bit service uuids are grouped into one field when doing advertising.
      if (num32BitUuids != 0) {
        size += OVERHEAD_BYTES_PER_FIELD + num32BitUuids * BluetoothUuid.UUID_BYTES_32_BIT;
      }
      // 128 bit service uuids are grouped into one field when doing advertising.
      if (num128BitUuids != 0) {
        size += OVERHEAD_BYTES_PER_FIELD + num128BitUuids * BluetoothUuid.UUID_BYTES_128_BIT;
      }
    }

    for (byte[] value : data.getServiceData().values()) {
      size += OVERHEAD_BYTES_PER_FIELD + SERVICE_DATA_UUID_LENGTH + getByteLength(value);
    }
    for (int i = 0; i < data.getManufacturerSpecificData().size(); ++i) {
      size +=
          OVERHEAD_BYTES_PER_FIELD
              + MANUFACTURER_SPECIFIC_DATA_LENGTH
              + getByteLength(data.getManufacturerSpecificData().valueAt(i));
    }
    if (data.getIncludeTxPowerLevel()) {
      size += OVERHEAD_BYTES_PER_FIELD + 1; // tx power level value is one byte.
    }
    if (data.getIncludeDeviceName() && bluetoothAdapter.getName() != null) {
      size += OVERHEAD_BYTES_PER_FIELD + bluetoothAdapter.getName().length();
    }
    return size;
  }

  private static int getByteLength(byte[] array) {
    return array == null ? 0 : array.length;
  }

  @ForType(BluetoothLeAdvertiser.class)
  private interface BluetoothLeAdvertiserReflector {
    @Direct
    void __constructor__(IBluetoothManager bluetoothManager);

    @Direct
    void __constructor__(BluetoothAdapter bluetoothAdapter);
  }
}
