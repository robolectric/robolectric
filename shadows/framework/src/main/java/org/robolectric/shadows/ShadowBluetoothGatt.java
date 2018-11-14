package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR2;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.O;
import static android.os.Build.VERSION_CODES.O_MR1;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.content.Context;
import android.os.Build;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implements;
import org.robolectric.shadow.api.Shadow;

@Implements(value = BluetoothGatt.class, minSdk = JELLY_BEAN_MR2)
public class ShadowBluetoothGatt {

  @SuppressLint("PrivateApi")
  @SuppressWarnings("unchecked")
  public static BluetoothGatt newInstance(BluetoothDevice device) {
    try {
      Class<?> iBluetoothGattClass =
          Shadow.class.getClassLoader().loadClass("android.bluetooth.IBluetoothGatt");

      BluetoothGatt bluetoothGatt;
      if (Build.VERSION.SDK_INT >= O_MR1) {
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
      } else if (Build.VERSION.SDK_INT >= O) {
        bluetoothGatt =
            Shadow.newInstance(
                BluetoothGatt.class,
                new Class<?>[] {
                  iBluetoothGattClass, BluetoothDevice.class, Integer.TYPE, Integer.TYPE
                },
                new Object[] {null, device, 0, 0});
      } else if (Build.VERSION.SDK_INT >= LOLLIPOP) {
        bluetoothGatt =
            Shadow.newInstance(
                BluetoothGatt.class,
                new Class<?>[] {
                  Context.class, iBluetoothGattClass, BluetoothDevice.class, Integer.TYPE
                },
                new Object[] {RuntimeEnvironment.application, null, device, 0});
      } else {
        bluetoothGatt =
            Shadow.newInstance(
                BluetoothGatt.class,
                new Class<?>[] {Context.class, iBluetoothGattClass, BluetoothDevice.class},
                new Object[] {RuntimeEnvironment.application, null, device});
      }
      return bluetoothGatt;
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
  }
}
