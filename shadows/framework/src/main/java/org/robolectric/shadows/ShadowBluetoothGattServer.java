package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR2;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.O;
import static android.os.Build.VERSION_CODES.R;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.shadow.api.Shadow;

/** Shadow for android.bluetooth.BluetoothGattServer. */
@Implements(value = BluetoothGattServer.class, minSdk = JELLY_BEAN_MR2)
public class ShadowBluetoothGattServer {
  public final Map<UUID, BluetoothGattService> gattServices = new HashMap<>();
  private BluetoothGattServerCallback bluetoothGattServerCallback;

  @SuppressLint("PrivateApi")
  @SuppressWarnings("unchecked")
  public static BluetoothGattServer newInstance() {
    try {
      Class<?> iBluetoothGattClass =
          Shadow.class.getClassLoader().loadClass("android.bluetooth.IBluetoothGatt");
      BluetoothGattServer bluetoothGattServer;
      int apiLevel = RuntimeEnvironment.getApiLevel();
      if (apiLevel > R) {
        bluetoothGattServer =
            Shadow.newInstance(
                BluetoothGattServer.class,
                new Class<?>[] {iBluetoothGattClass, Integer.TYPE, BluetoothAdapter.class},
                new Object[] {null, 0, BluetoothAdapter.getDefaultAdapter()});
      } else if (apiLevel >= O) {
        bluetoothGattServer =
            Shadow.newInstance(
                BluetoothGattServer.class,
                new Class<?>[] {
                  iBluetoothGattClass, Integer.TYPE,
                },
                new Object[] {null, 0});
      } else if (apiLevel >= LOLLIPOP) {
        bluetoothGattServer =
            Shadow.newInstance(
                BluetoothGattServer.class,
                new Class<?>[] {
                  Context.class, iBluetoothGattClass, Integer.TYPE,
                },
                new Object[] {RuntimeEnvironment.getApplication(), null, 0});
      } else {
        bluetoothGattServer =
            Shadow.newInstance(
                BluetoothGattServer.class,
                new Class<?>[] {
                  Context.class, iBluetoothGattClass,
                },
                new Object[] {RuntimeEnvironment.getApplication(), null});
      }
      return bluetoothGattServer;
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  @Implementation
  protected boolean addService(BluetoothGattService gattService) {
    gattServices.put(gattService.getUuid(), gattService);
    return true;
  }

  @Implementation
  protected BluetoothGattService getService(UUID uuid) {
    return gattServices.get(uuid);
  }

  @Implementation
  protected List<BluetoothGattService> getServices() {
    return new ArrayList<>(gattServices.values());
  }

  @Implementation
  protected boolean removeService(BluetoothGattService service) {
    if (!gattServices.containsKey(service.getUuid())) {
      return false;
    }
    gattServices.remove(service.getUuid());
    return true;
  }

  @Implementation
  protected void clearServices() {
    gattServices.clear();
  }

  /* package */ BluetoothGattServerCallback getGattServerCallback() {
    return bluetoothGattServerCallback;
  }

  /* package */ void setGattServerCallback(
      BluetoothGattServerCallback bluetoothGattServerCallback) {
    this.bluetoothGattServerCallback = bluetoothGattServerCallback;
  }
}
