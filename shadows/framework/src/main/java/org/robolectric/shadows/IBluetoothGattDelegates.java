package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.O;
import static android.os.Build.VERSION_CODES.S;
import static android.os.Build.VERSION_CODES.TIRAMISU;
import static android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE;
import static org.robolectric.RuntimeEnvironment.getApiLevel;
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
import com.android.bluetooth.x.com.android.modules.utils.SynchronousResultReceiver;
import java.util.Collections;
import java.util.List;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.reflector.ForType;
import org.robolectric.versioning.AndroidVersions.V;

/** Holds fakes for the IBluetoothGatt system service */
class IBluetoothGattDelegates {

  private IBluetoothGattDelegates() {}

  /**
   * Creates a fake/stub implementation of the IBluetoothGatt system service for the current API
   * level.
   *
   * <p>Its recommended to cache the result
   */
  static IBluetoothGatt createIBluetoothGatt() {
    return ReflectionHelpers.createDelegatingProxy(IBluetoothGatt.class, createDelegate());
  }

  private static Object createDelegate() {
    if (getApiLevel() >= V.SDK_INT) {
      return new BluetoothGattDelegateV();
    } else if (getApiLevel() >= UPSIDE_DOWN_CAKE) {
      return new BluetoothGattDelegateU();
    } else if (getApiLevel() >= TIRAMISU) {
      return new BluetoothGattDelegateT();
    } else if (getApiLevel() >= S) {
      return new BluetoothGattDelegateS();
    } else if (getApiLevel() >= O) {
      return new BluetoothGattDelegateO();
    } else {
      return new Object();
    }
  }

  @SuppressWarnings("unused") // methods called via reflection
  private static class BluetoothGattDelegateV {

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

      reflector(IAdvertisingSetCallbackReflector.class, callback)
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

  @SuppressWarnings("unused") // methods called via reflection
  private static class BluetoothGattDelegateU {
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
        SynchronousResultReceiver recv) {
      try {
        callback.onAdvertisingSetStarted(
            0 /* advertiserId */,
            0 /* tx_power */,
            AdvertisingSetCallback.ADVERTISE_SUCCESS /* status */);
        recv.send(null);
      } catch (RemoteException e) {
        // should never happen
        throw new RuntimeException(e);
      }
    }

    public void stopAdvertisingSet(
        IAdvertisingSetCallback callback,
        AttributionSource attributionSource,
        SynchronousResultReceiver recv) {
      try {
        callback.onAdvertisingSetStopped(0 /* advertiserId */);
        recv.send(null);
      } catch (RemoteException e) {
        // should never happen
        throw new RuntimeException(e);
      }
    }
  }

  @SuppressWarnings("unused") // methods called via reflection
  private static class BluetoothGattDelegateT {
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
        SynchronousResultReceiver recv) {
      try {
        callback.onAdvertisingSetStarted(
            0 /* advertiserId */,
            0 /* tx_power */,
            AdvertisingSetCallback.ADVERTISE_SUCCESS /* status */);
        recv.send(null);
      } catch (RemoteException e) {
        // should never happen
        throw new RuntimeException(e);
      }
    }

    public void stopAdvertisingSet(
        IAdvertisingSetCallback callback,
        AttributionSource attributionSource,
        SynchronousResultReceiver recv) {
      try {
        callback.onAdvertisingSetStopped(0 /* advertiserId */);
        recv.send(null);
      } catch (RemoteException e) {
        // should never happen
        throw new RuntimeException(e);
      }
    }
  }

  @SuppressWarnings("unused") // methods called via reflection
  private static class BluetoothGattDelegateO {

    public void startAdvertisingSet(
        AdvertisingSetParameters parameters,
        AdvertiseData advertiseData,
        AdvertiseData scanResponse,
        PeriodicAdvertisingParameters periodicParameters,
        AdvertiseData periodicData,
        int duration,
        int maxExtAdvEvents,
        IAdvertisingSetCallback callback) {
      try {
        callback.onAdvertisingSetStarted(
            0 /* advertiserId */,
            0 /* tx_power */,
            AdvertisingSetCallback.ADVERTISE_SUCCESS /* status */);
      } catch (RemoteException e) {
        // should never happen
        throw new RuntimeException(e);
      }
    }

    public List<BluetoothDevice> getDevicesMatchingConnectionStates(int[] states) {
      // TODO: implement properly
      return Collections.emptyList();
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

  private static class BluetoothGattDelegateS {

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
      try {
        callback.onAdvertisingSetStarted(
            0 /* advertiserId */,
            0 /* tx_power */,
            AdvertisingSetCallback.ADVERTISE_SUCCESS /* status */);
      } catch (RemoteException e) {
        // should never happen
        throw new RuntimeException(e);
      }
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

  @ForType(IAdvertisingSetCallback.class)
  private interface IAdvertisingSetCallbackReflector {

    // for android V
    void onAdvertisingSetStarted(IBinder binder, int advertiserId, int txPower, int status);
  }
}
