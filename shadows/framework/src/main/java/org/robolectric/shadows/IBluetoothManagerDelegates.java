package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.TIRAMISU;
import static com.google.common.base.Preconditions.checkNotNull;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.IBluetooth;
import android.bluetooth.IBluetoothGatt;
import android.bluetooth.IBluetoothManagerCallback;
import android.bluetooth.IBluetoothProfileServiceConnection;
import android.content.Context;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceManager;
import org.robolectric.RuntimeEnvironment;

/** Holds fakes for the IBluetoothManager system service */
class IBluetoothManagerDelegates {

  private IBluetoothManagerDelegates() {}

  /** Creates the appropriate delegate to use based on API level. */
  static Object createDelegate() {
    if (RuntimeEnvironment.getApiLevel() < TIRAMISU) {
      // android T introduces new classes which result in NoClassDefFoundErrors if loaded in older
      // API levels
      return new IBluetoothManagerDelegateS();
    } else {
      return new IBluetoothManagerDelegate();
    }
  }

  private static class IBluetoothManagerDelegateBase {
    private IBluetoothGatt iBluetoothGatt;

    public IBluetoothGatt getBluetoothGatt() {
      if (iBluetoothGatt == null) {
        iBluetoothGatt = BluetoothGattProxyDelegate.createBluetoothGattProxy();
      }
      return iBluetoothGatt;
    }
  }

  private static class IBluetoothManagerDelegateS extends IBluetoothManagerDelegateBase {

    public IBluetooth registerAdapter(IBluetoothManagerCallback callback) {
      IBinder btBinder = checkNotNull(ServiceManager.getService(Context.BLUETOOTH_SERVICE));
      IBluetooth btService = checkNotNull(IBluetooth.Stub.asInterface(btBinder));
      try {
        callback.onBluetoothServiceUp(btService);
        return btService;
      } catch (RemoteException e) {
        // should never happen - this is a local call in Robolectric
        throw new RuntimeException(e);
      }
    }
  }

  // Any BluetoothAdapter calls which need to invoke BluetoothManager methods can delegate those
  // calls to this class. The default behavior for any methods not defined in this class is a no-op.
  @SuppressWarnings("unused")
  private static class IBluetoothManagerDelegate extends IBluetoothManagerDelegateBase {

    private IBluetoothManagerDelegate() {}

    /**
     * Allows the internal BluetoothProfileConnector associated with a {@link BluetoothProfile} to
     * automatically invoke the service connected callback.
     */
    public boolean bindBluetoothProfileService(
        int bluetoothProfile, String serviceName, IBluetoothProfileServiceConnection proxy) {
      if (!BluetoothAdapter.getDefaultAdapter().isEnabled()) {
        return false;
      }
      try {
        proxy.onServiceConnected(null, null);
      } catch (RemoteException e) {
        return false;
      }
      return true;
    }

    /**
     * Allows the internal BluetoothProfileConnector associated with a {@link BluetoothProfile} to
     * automatically invoke the service disconnected callback.
     */
    public void unbindBluetoothProfileService(
        int bluetoothProfile, IBluetoothProfileServiceConnection proxy) {
      try {
        proxy.onServiceDisconnected(null);
      } catch (RemoteException e) {
        // nothing to do
      }
    }
  }
}
