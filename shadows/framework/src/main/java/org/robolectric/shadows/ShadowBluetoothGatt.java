package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR2;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.O;
import static android.os.Build.VERSION_CODES.O_MR1;
import static android.os.Build.VERSION_CODES.R;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.content.Context;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.shadow.api.Shadow;

@Implements(value = BluetoothGatt.class, minSdk = JELLY_BEAN_MR2)
public class ShadowBluetoothGatt {
  private BluetoothGattCallback bluetoothGattCallback;

  @SuppressLint("PrivateApi")
  @SuppressWarnings("unchecked")
  public static BluetoothGatt newInstance(BluetoothDevice device) {
    try {
      Class<?> iBluetoothGattClass =
          Shadow.class.getClassLoader().loadClass("android.bluetooth.IBluetoothGatt");

      BluetoothGatt bluetoothGatt;
      int apiLevel = RuntimeEnvironment.getApiLevel();
      if (apiLevel > R) {
        bluetoothGatt =
            Shadow.newInstance(
                BluetoothGatt.class,
                new Class<?>[] {
                  iBluetoothGattClass,
                  BluetoothDevice.class,
                  Integer.TYPE,
                  Boolean.TYPE,
                  Integer.TYPE,
                  Class.forName("android.content.AttributionSource"),
                },
                new Object[] {null, device, 0, false, 0, null});
      } else if (apiLevel >= O_MR1) {
        bluetoothGatt =
            Shadow.newInstance(
                BluetoothGatt.class,
                new Class<?>[] {
                  iBluetoothGattClass,
                  BluetoothDevice.class,
                  Integer.TYPE,
                  Boolean.TYPE,
                  Integer.TYPE
                },
                new Object[] {null, device, 0, false, 0});
      } else if (apiLevel >= O) {
        bluetoothGatt =
            Shadow.newInstance(
                BluetoothGatt.class,
                new Class<?>[] {
                  iBluetoothGattClass, BluetoothDevice.class, Integer.TYPE, Integer.TYPE
                },
                new Object[] {null, device, 0, 0});
      } else if (apiLevel >= LOLLIPOP) {
        bluetoothGatt =
            Shadow.newInstance(
                BluetoothGatt.class,
                new Class<?>[] {
                  Context.class, iBluetoothGattClass, BluetoothDevice.class, Integer.TYPE
                },
                new Object[] {RuntimeEnvironment.getApplication(), null, device, 0});
      } else {
        bluetoothGatt =
            Shadow.newInstance(
                BluetoothGatt.class,
                new Class<?>[] {Context.class, iBluetoothGattClass, BluetoothDevice.class},
                new Object[] {RuntimeEnvironment.getApplication(), null, device});
      }
      return bluetoothGatt;
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  /* package */ BluetoothGattCallback getGattCallback() {
    return bluetoothGattCallback;
  }

  /* package */ void setGattCallback(BluetoothGattCallback bluetoothGattCallback) {
    this.bluetoothGattCallback = bluetoothGattCallback;
  }

  /**
   * Overrides behavior of {@link BluetoothGatt#connect()} to always return true.
   *
   * @return true, unconditionally
   */
  @Implementation(minSdk = JELLY_BEAN_MR2)
  protected boolean connect() {
    return true;
  }
}
