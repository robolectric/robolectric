package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.O;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.IBluetoothGatt;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertisingSetCallback;
import android.bluetooth.le.AdvertisingSetParameters;
import android.bluetooth.le.IAdvertisingSetCallback;
import android.bluetooth.le.PeriodicAdvertisingParameters;
import android.content.AttributionSource;
import android.os.IBinder;
import android.os.RemoteException;
import java.util.Collections;
import java.util.List;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.ClassName;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.reflector.ForType;

/** Delegating proxy for the IBluetoothGatt system service */
@SuppressWarnings("unused") // methods called via reflection
class BluetoothGattProxyDelegate {

  private BluetoothGattProxyDelegate() {}

  /**
   * Creates a fake/stub implementation of the IBluetoothGatt system service for the current API
   * level.
   *
   * <p>Its recommended to cache the result
   */
  static IBluetoothGatt createBluetoothGattProxy() {
    // Currently only APIs >= O are supported
    // Using BluetoothGattProxyDelegate on older APIs currently fails with `NoClassDefFoundError:
    // IAdvertisingSetCallback`
    Object delegate =
        (RuntimeEnvironment.getApiLevel() >= O) ? new BluetoothGattProxyDelegate() : new Object();
    return ReflectionHelpers.createDelegatingProxy(IBluetoothGatt.class, delegate);
  }

  private void invokeOnAdvertisingSetStarted(IAdvertisingSetCallback callback) {
    reflector(IAdvertisingSetCallbackReflectorU.class, callback)
        .onAdvertisingSetStarted(0, 0, AdvertisingSetCallback.ADVERTISE_SUCCESS);
  }

  // for android V
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

  // for android U
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
      AttributionSource attributionSource,
      @ClassName("com.android.bluetooth.x.com.android.modules.utils.SynchronousResultReceiver")
          Object recv) {
    invokeOnAdvertisingSetStarted(callback);
    reflector(SynchronousResultReceiverReflector.class, recv).send(null);
  }

  // for android T
  public void startAdvertisingSet(
      AdvertisingSetParameters parameters,
      AdvertiseData advertiseData,
      AdvertiseData scanResponse,
      PeriodicAdvertisingParameters periodicParameters,
      AdvertiseData periodicData,
      int duration,
      int maxExtAdvEvents,
      IAdvertisingSetCallback callback,
      AttributionSource attributionSource,
      @ClassName("com.android.bluetooth.x.com.android.modules.utils.SynchronousResultReceiver")
          Object recv) {
    invokeOnAdvertisingSetStarted(callback);
    reflector(SynchronousResultReceiverReflector.class, recv).send(null);
  }

  // for android S*
  public void startAdvertisingSet(
      AdvertisingSetParameters parameters,
      AdvertiseData advertiseData,
      AdvertiseData scanResponse,
      PeriodicAdvertisingParameters periodicParameters,
      AdvertiseData periodicData,
      int duration,
      int maxExtAdvEvents,
      IAdvertisingSetCallback callback,
      AttributionSource attributionSource) {
    invokeOnAdvertisingSetStarted(callback);
  }

  // for android O to R
  public void startAdvertisingSet(
      AdvertisingSetParameters parameters,
      AdvertiseData advertiseData,
      AdvertiseData scanResponse,
      PeriodicAdvertisingParameters periodicParameters,
      AdvertiseData periodicData,
      int duration,
      int maxExtAdvEvents,
      IAdvertisingSetCallback callback) {
    invokeOnAdvertisingSetStarted(callback);
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

  // for android U
  public void stopAdvertisingSet(
      IAdvertisingSetCallback callback,
      AttributionSource attributionSource,
      @ClassName("com.android.bluetooth.x.com.android.modules.utils.SynchronousResultReceiver")
          Object recv) {
    stopAdvertisingSet(callback, attributionSource);
    reflector(SynchronousResultReceiverReflector.class, recv).send(null);
  }

  public List<BluetoothDevice> getDevicesMatchingConnectionStates(int[] states) {
    // TODO: implement properly
    return Collections.emptyList();
  }

  @ForType(IAdvertisingSetCallback.class)
  private interface IAdvertisingSetCallbackReflectorU {
    // for android U
    void onAdvertisingSetStarted(int advertiserId, int txPower, int status);
  }

  @ForType(IAdvertisingSetCallback.class)
  interface IAdvertisingSetCallbackReflectorV {
    // for android V
    void onAdvertisingSetStarted(IBinder binder, int advertiserId, int txPower, int status);
  }

  @ForType(
      className = "com.android.bluetooth.x.com.android.modules.utils.SynchronousResultReceiver")
  interface SynchronousResultReceiverReflector {
    void send(Object value);
  }
}
