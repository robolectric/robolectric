package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.O;
import static android.os.Build.VERSION_CODES.R;
import static android.os.Build.VERSION_CODES.S;
import static android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothUuid;
import android.bluetooth.IBluetoothGatt;
import android.bluetooth.IBluetoothManager;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.AdvertisingSet;
import android.bluetooth.le.AdvertisingSetCallback;
import android.bluetooth.le.AdvertisingSetParameters;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.bluetooth.le.PeriodicAdvertisingParameters;
import android.content.AttributionSource;
import android.os.Handler;
import android.os.ParcelUuid;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.ReflectorObject;
import org.robolectric.util.PerfStatsCollector;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.reflector.Constructor;
import org.robolectric.util.reflector.Direct;
import org.robolectric.util.reflector.ForType;
import org.robolectric.versioning.AndroidVersions.V;

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
  private final Map<AdvertisingSetCallback, AdvertisingSet> advertisingSetMap = new HashMap<>();
  private final AtomicInteger advertiserId = new AtomicInteger(0);
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

  /**
   * Start Bluetooth LE Advertising Set. This method returns immediately, the operation status is
   * delivered through {@code callback}.
   *
   * @param parameters Advertising set parameters.
   * @param advertiseData Advertisement data to be broadcasted.
   * @param scanResponse Scan response associated with the advertisement data.
   * @param periodicParameters Periodic advertisng parameters.
   * @param periodicData Periodic advertising data.
   * @param duration Advertising duration, in 10ms unit.
   * @param maxExtendedAdvertisingEvents Maximum number of extended advertising events the
   *     controller shall attempt to send prior to terminating the extended advertising, even if the
   *     duration has not expired.
   * @param gattServer GattServer the GATT server that will "own" connections derived from this
   *     advertising.
   * @param callback Callback for advertising set.
   * @param handler Thread upon which the callbacks will be invoked.
   * @throws IllegalArgumentException When {@code callback} is not present.
   */
  @Implementation(minSdk = UPSIDE_DOWN_CAKE)
  protected void startAdvertisingSet(
      AdvertisingSetParameters parameters,
      AdvertiseData advertiseData,
      AdvertiseData scanResponse,
      PeriodicAdvertisingParameters periodicParameters,
      AdvertiseData periodicData,
      int duration,
      int maxExtendedAdvertisingEvents,
      BluetoothGattServer gattServer,
      AdvertisingSetCallback callback,
      Handler handler) {
    if (callback == null) {
      throw new IllegalArgumentException("callback cannot be null");
    }

    boolean isConnectable = parameters.isConnectable();
    boolean isDiscoverable = parameters.isDiscoverable();
    boolean hasFlags = isConnectable && isDiscoverable;
    if (parameters.isLegacy()) {
      if (getTotalBytes(advertiseData, hasFlags) > MAX_LEGACY_ADVERTISING_DATA_BYTES) {
        throw new IllegalArgumentException("Legacy advertising data too big");
      }

      if (getTotalBytes(scanResponse, false) > MAX_LEGACY_ADVERTISING_DATA_BYTES) {
        throw new IllegalArgumentException("Legacy scan response data too big");
      }
    } else {
      boolean supportCodedPhy = bluetoothAdapter.isLeCodedPhySupported();
      boolean support2MPhy = bluetoothAdapter.isLe2MPhySupported();
      int pphy = parameters.getPrimaryPhy();
      int sphy = parameters.getSecondaryPhy();
      if (pphy == BluetoothDevice.PHY_LE_CODED && !supportCodedPhy) {
        throw new IllegalArgumentException("Unsupported primary PHY selected");
      }

      if ((sphy == BluetoothDevice.PHY_LE_CODED && !supportCodedPhy)
          || (sphy == BluetoothDevice.PHY_LE_2M && !support2MPhy)) {
        throw new IllegalArgumentException("Unsupported secondary PHY selected");
      }

      int maxData = bluetoothAdapter.getLeMaximumAdvertisingDataLength();
      if (getTotalBytes(advertiseData, hasFlags) > maxData) {
        throw new IllegalArgumentException("Advertising data too big");
      }

      if (getTotalBytes(scanResponse, false) > maxData) {
        throw new IllegalArgumentException("Scan response data too big");
      }

      if (getTotalBytes(periodicData, false) > maxData) {
        throw new IllegalArgumentException("Periodic advertising data too big");
      }
    }

    if (maxExtendedAdvertisingEvents < 0 || maxExtendedAdvertisingEvents > 255) {
      throw new IllegalArgumentException(
          "maxExtendedAdvertisingEvents out of range: " + maxExtendedAdvertisingEvents);
    }

    if (maxExtendedAdvertisingEvents != 0 && !bluetoothAdapter.isLePeriodicAdvertisingSupported()) {
      throw new IllegalArgumentException(
          "Can't use maxExtendedAdvertisingEvents with controller that don't support "
              + "LE Extended Advertising");
    }

    if (duration < 0 || duration > 65535) {
      throw new IllegalArgumentException("duration out of range: " + duration);
    }

    if (advertisingSetMap.containsKey(callback)) {
      callback.onAdvertisingSetStarted(
          /* advertisingSet= */ null,
          parameters.getTxPowerLevel(),
          AdvertisingSetCallback.ADVERTISE_FAILED_ALREADY_STARTED);
      return;
    }

    AdvertisingSet advertisingSet;
    if (RuntimeEnvironment.getApiLevel() >= V.SDK_INT) {
      IBluetoothGatt gatt =
          ReflectionHelpers.callInstanceMethod(bluetoothAdapter, "getBluetoothGatt");

      advertisingSet =
          reflector(AdvertisingSetReflector.class)
              .__constructor__(
                  gatt == null ? ReflectionHelpers.createNullProxy(IBluetoothGatt.class) : gatt,
                  advertiserId.getAndAdd(1),
                  bluetoothAdapter,
                  bluetoothAdapter.getAttributionSource());
    } else {
      advertisingSet =
          reflector(AdvertisingSetReflector.class)
              .__constructor__(
                  advertiserId.getAndAdd(1),
                  ReflectionHelpers.createNullProxy(IBluetoothManager.class),
                  (AttributionSource)
                      ReflectionHelpers.callInstanceMethod(
                          bluetoothAdapter, "getAttributionSource"));
    }

    callback.onAdvertisingSetStarted(
        advertisingSet, parameters.getTxPowerLevel(), AdvertisingSetCallback.ADVERTISE_SUCCESS);

    advertisingSetMap.put(callback, advertisingSet);
  }

  /**
   * Used to dispose of a {@link AdvertisingSet} object, obtained with {@link
   * BluetoothLeAdvertiser#startAdvertisingSet}.
   *
   * @param callback Callback for advertising set.
   * @throws IllegalArgumentException When {@code callback} is not present.
   */
  @Implementation(minSdk = UPSIDE_DOWN_CAKE)
  protected void stopAdvertisingSet(AdvertisingSetCallback callback) {
    if (callback == null) {
      throw new IllegalArgumentException("callback cannot be null");
    }

    if (!advertisingSetMap.containsKey(callback)) {
      throw new IllegalArgumentException("callback not found");
    }

    callback.onAdvertisingSetStopped(advertisingSetMap.get(callback));

    advertisingSetMap.remove(callback);
  }

  /** Returns the count of current ongoing Bluetooth LE advertising requests. */
  public int getAdvertisementRequestCount() {
    return this.advertisements.size();
  }

  /** Returns the count of current ongoing Bluetooth LE advertising set requests. */
  public int getAdvertisingSetRequestCount() {
    return this.advertisingSetMap.size();
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

  @ForType(AdvertisingSet.class)
  interface AdvertisingSetReflector {
    @Constructor
    AdvertisingSet __constructor__(
        IBluetoothGatt bluetoothGatt,
        int advertiserId,
        BluetoothAdapter bluetoothAdapter,
        AttributionSource attributionSource);

    @Constructor
    AdvertisingSet __constructor__(
        int advertiserId, IBluetoothManager bluetoothManager, AttributionSource attributionSource);
  }
}
