package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.Q;
import static android.os.Build.VERSION_CODES.S_V2;
import static android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE;
import static java.util.Objects.requireNonNull;
import static org.robolectric.util.reflector.Reflector.reflector;
import static org.robolectric.versioning.VersionCalculator.POST_BAKLAVA;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.IBluetooth;
import android.bluetooth.IBluetoothGatt;
import android.bluetooth.IBluetoothManager;
import android.bluetooth.IBluetoothManagerCallback;
import android.content.ComponentName;
import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import javax.annotation.concurrent.GuardedBy;
import org.robolectric.annotation.ClassName;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;
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

  @Implementation(minSdk = POST_BAKLAVA)
  protected android.os.Messenger getServiceMessenger() {
    synchronized (messengerLock) {
      if (serviceMessenger == null) {
        messengerThread = new HandlerThread("BluetoothSystemServerMessenger");
        messengerThread.start();
        Looper looper = messengerThread.getLooper();

        Handler handler =
            new Handler(
                looper,
                msg -> {
                  if (msg.replyTo == null) {
                    return true;
                  }
                  try {
                    Object data = msg.obj;
                    String requestClassName = data.getClass().getName();
                    // SystemServiceMessage classes are not public, so we can't import them.
                    // Instead, we rely on the naming convention of the reply class.
                    Class<?> replyClass = Class.forName(requestClassName + "$Reply");
                    Object replyData = replyClass.getConstructor().newInstance();

                    // The default constructor will leave the IBluetooth field as null,
                    // which simulates the Bluetooth-off state.

                    android.os.Message replyMsg = android.os.Message.obtain();
                    replyMsg.obj = replyData;
                    msg.replyTo.send(replyMsg);
                  } catch (ReflectiveOperationException | RemoteException e) {
                    // Don't crash the test host.
                    Log.w("ShadowIBluetoothManager", "failed serviceMessenger transaction", e);
                  }
                  return true;
                });
        serviceMessenger = new Messenger(handler);
      }
      return serviceMessenger;
    }
  }

  @Resetter
  public static void reset() {
    synchronized (messengerLock) {
      if (messengerThread != null) {
        messengerThread.quit();
        messengerThread = null;
        serviceMessenger = null;
      }
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

