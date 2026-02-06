package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.Q;
import static android.os.Build.VERSION_CODES.S_V2;
import static android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE;
import static java.util.Objects.requireNonNull;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.IBluetooth;
import android.bluetooth.IBluetoothGatt;
import android.bluetooth.IBluetoothManager;
import android.bluetooth.IBluetoothManagerCallback;
import android.content.ComponentName;
import android.content.Context;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.ServiceManager;
import javax.annotation.concurrent.GuardedBy;
import org.robolectric.annotation.ClassName;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.util.reflector.ForType;

/** Shadow for the IBluetoothManager stub system service */
@Implements(value = IBluetoothManager.Default.class, isInAndroidSdk = false, minSdk = Q)
public class ShadowIBluetoothManager {

  private IBluetoothGatt iBluetoothGatt;
  private static final Object messengerLock = new Object();

  @GuardedBy("messengerLock")
  private static Messenger serviceMessenger = null;

  @GuardedBy("messengerLock")
  private static HandlerThread messengerThread = null;

  @Implementation(maxSdk = UPSIDE_DOWN_CAKE)
  protected IBluetoothGatt getBluetoothGatt() {
    if (iBluetoothGatt == null) {
      iBluetoothGatt = BluetoothGattProxyDelegate.createBluetoothGattProxy();
    }
    return iBluetoothGatt;
  }

  // For backwards compatibility, only shadow this method on <= S_V2 even though it exists up to U.
  // Shadowing it on U causes a selection of tests to fail
  @Implementation(maxSdk = S_V2)
  protected IBluetooth registerAdapter(IBluetoothManagerCallback callback) {
    IBinder btBinder = requireNonNull(ServiceManager.getService(Context.BLUETOOTH_SERVICE));
    IBluetooth btService = requireNonNull(IBluetooth.Stub.asInterface(btBinder));
    reflector(IBluetoothManagerCallbackReflectorS.class, callback).onBluetoothServiceUp(btService);
    return btService;
  }

  /**
   * Allows the internal BluetoothProfileConnector associated with a {@link BluetoothProfile} to
   * automatically invoke the service connected callback.
   */
  @Implementation(minSdk = UPSIDE_DOWN_CAKE, maxSdk = UPSIDE_DOWN_CAKE)
  protected boolean bindBluetoothProfileService(
      int bluetoothProfile,
      String serviceName,
      @ClassName("android.bluetooth.IBluetoothProfileServiceConnection") Object proxy) {
    if (!BluetoothAdapter.getDefaultAdapter().isEnabled()) {
      return false;
    }
    try {

      reflector(IBluetoothProfileServiceConnectionReflector.class, proxy)
          .onServiceConnected(null, null);
    } catch (RemoteException e) {
      return false;
    }
    return true;
    }

  /**
   * Allows the internal BluetoothProfileConnector associated with a {@link BluetoothProfile} to
   * automatically invoke the service disconnected callback.
   */
  @Implementation(minSdk = UPSIDE_DOWN_CAKE, maxSdk = UPSIDE_DOWN_CAKE)
  protected void unbindBluetoothProfileService(
      int bluetoothProfile,
      @ClassName("android.bluetooth.IBluetoothProfileServiceConnection") Object proxy) {
    try {
      reflector(IBluetoothProfileServiceConnectionReflector.class, proxy)
          .onServiceDisconnected(null);
    } catch (RemoteException e) {
      // nothing to do
    }
  }

  @ForType(className = "android.bluetooth.IBluetoothProfileServiceConnection")
  private interface IBluetoothProfileServiceConnectionReflector {

    void onServiceConnected(ComponentName className, IBinder service) throws RemoteException;

    void onServiceDisconnected(ComponentName className) throws RemoteException;
  }

  @ForType(IBluetoothManagerCallback.class)
  private interface IBluetoothManagerCallbackReflectorS {
    void onBluetoothServiceUp(IBluetooth bluetoothService);
  }
}

