package org.robolectric.shadows;

import static org.robolectric.util.reflector.Reflector.reflector;

import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertisingSetCallback;
import android.bluetooth.le.AdvertisingSetParameters;
import android.bluetooth.le.IAdvertisingSetCallback;
import android.bluetooth.le.PeriodicAdvertisingParameters;
import android.content.AttributionSource;
import android.os.IBinder;
import android.os.RemoteException;
import org.robolectric.shadows.BluetoothGattProxyDelegate.IAdvertisingSetCallbackReflectorV;
import org.robolectric.util.ReflectionHelpers;

/** Delegating proxy for the IBluetoothAdvertise system service */
@SuppressWarnings("unused") // methods called via reflection
class BluetoothAdvertiseProxyDelegate {

  private BluetoothAdvertiseProxyDelegate() {}

  /**
   * Creates a fake/stub implementation of the IBluetoothAdvertise system service for the current
   * API level.
   *
   * <p>It is recommended to cache the result
   */
  static /* IBluetoothAdvertise */ Object createBluetoothAdvertiseProxy() {
    try {
      return ReflectionHelpers.createDelegatingProxy(
          Class.forName("android.bluetooth.IBluetoothAdvertise"),
          new BluetoothAdvertiseProxyDelegate());
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  public void startAdvertisingSet(
      AdvertisingSetParameters parameters,
      AdvertiseData advertiseData,
      AdvertiseData scanResponse,
      PeriodicAdvertisingParameters periodicParameters,
      AdvertiseData periodicData,
      int duration,
      int maxExtAdvEvents,
      int gattServerIf,
      IAdvertisingSetCallback callback,
      AttributionSource attributionSource) {

    reflector(IAdvertisingSetCallbackReflectorV.class, callback)
        .onAdvertisingSetStarted(
            ReflectionHelpers.createNullProxy(IBinder.class),
            0,
            parameters.getTxPowerLevel(),
            AdvertisingSetCallback.ADVERTISE_SUCCESS);
  }

  public void stopAdvertisingSet(
      IAdvertisingSetCallback callback, AttributionSource attributionSource) {
    try {
      callback.onAdvertisingSetStopped(0 /* advertiserId */);
    } catch (RemoteException e) {
      // should never happen
      throw new RuntimeException(e);
    }
  }
}
