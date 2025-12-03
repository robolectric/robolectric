package org.robolectric.shadows;

import static java.util.Objects.requireNonNull;

import android.bluetooth.IBluetooth;
import android.bluetooth.IBluetoothGatt;
import android.bluetooth.IBluetoothManagerCallback;
import android.content.Context;
import android.os.IBinder;
import android.os.ServiceManager;
import org.robolectric.util.reflector.ForType;
import org.robolectric.util.reflector.Reflector;

/** Holds fakes for the IBluetoothManager system service used for SDKs < Q. */
public class IBluetoothManagerDelegateS {

  private IBluetoothGatt iBluetoothGatt;

  public IBluetoothGatt getBluetoothGatt() {
    if (iBluetoothGatt == null) {
      iBluetoothGatt = BluetoothGattProxyDelegate.createBluetoothGattProxy();
    }
    return iBluetoothGatt;
  }

  public IBluetooth registerAdapter(IBluetoothManagerCallback callback) {
    IBinder btBinder = requireNonNull(ServiceManager.getService(Context.BLUETOOTH_SERVICE));
    IBluetooth btService = requireNonNull(IBluetooth.Stub.asInterface(btBinder));
    Reflector.reflector(IBluetoothManagerCallbackReflectorS.class, callback)
        .onBluetoothServiceUp(btService);
    return btService;
  }

  @ForType(IBluetoothManagerCallback.class)
  private interface IBluetoothManagerCallbackReflectorS {
    void onBluetoothServiceUp(IBluetooth bluetoothService);
  }
}
