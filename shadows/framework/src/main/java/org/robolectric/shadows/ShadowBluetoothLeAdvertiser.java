package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.O;

import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.AdvertisingSetCallback;
import android.bluetooth.le.AdvertisingSetParameters;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.bluetooth.le.PeriodicAdvertisingParameters;
import java.util.Map;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.ReflectorObject;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.Direct;
import org.robolectric.util.reflector.ForType;

/** Shadow implementation of {@link BluetoothLeAdvertiser}. */
@Implements(value = BluetoothLeAdvertiser.class, minSdk = O)
public class ShadowBluetoothLeAdvertiser {
  @ReflectorObject protected BluetoothLeAdvertiserReflector bluetoothLeAdvertiserReflector;

  private AdvertiseData lastAdvertiseData;

  /** Returns the count of current ongoing Bluetooth LE advertising requests. */
  public int getAdvertisementRequestCount() {
    return bluetoothLeAdvertiserReflector.getLegacyAdvertisers().size();
  }

  /** Returns the count of current ongoing Bluetooth LE advertising set requests. */
  public int getAdvertisingSetRequestCount() {
    return bluetoothLeAdvertiserReflector.getAdvertisingSets().size();
  }

  @Implementation
  protected void startAdvertising(
      AdvertiseSettings settings, AdvertiseData advertiseData, AdvertiseCallback callback) {
    bluetoothLeAdvertiserReflector.startAdvertising(settings, advertiseData, callback);
    this.lastAdvertiseData = advertiseData;
  }

  @Implementation
  protected void startAdvertisingSet(
      AdvertisingSetParameters parameters,
      AdvertiseData advertiseData,
      AdvertiseData scanResponse,
      PeriodicAdvertisingParameters periodicParameters,
      AdvertiseData periodicData,
      AdvertisingSetCallback callback) {
    bluetoothLeAdvertiserReflector.startAdvertisingSet(
        parameters, advertiseData, scanResponse, periodicParameters, periodicData, callback);
    this.lastAdvertiseData = advertiseData;
  }

  public AdvertiseData getLastAdvertisingData() {
    return lastAdvertiseData;
  }

  @ForType(BluetoothLeAdvertiser.class)
  private interface BluetoothLeAdvertiserReflector {
    @Accessor("mLegacyAdvertisers")
    Map<AdvertiseCallback, AdvertisingSetCallback> getLegacyAdvertisers();

    @Accessor("mAdvertisingSets")
    Map<?, ?> getAdvertisingSets();

    @Direct
    void startAdvertisingSet(
        AdvertisingSetParameters parameters,
        AdvertiseData advertiseData,
        AdvertiseData scanResponse,
        PeriodicAdvertisingParameters periodicParameters,
        AdvertiseData periodicData,
        AdvertisingSetCallback callback);

    @Direct
    void startAdvertising(
        AdvertiseSettings settings, AdvertiseData advertiseData, AdvertiseCallback callback);
  }
}
