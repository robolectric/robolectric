package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.LOLLIPOP_MR1;
import static org.robolectric.util.ReflectionHelpers.ClassParameter.from;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.os.Build;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.util.ReflectionHelpers;

/** Adds Robolectric support for BLE advertising. */
@Implements(value = BluetoothLeAdvertiser.class, minSdk = LOLLIPOP)
public class ShadowBluetoothLeAdvertiser {
  private static BluetoothLeAdvertiser bluetoothLeAdvertiser;
  @RealObject private BluetoothLeAdvertiser realBluetoothLeAdvertiser;

  static BluetoothLeAdvertiser getInstance(BluetoothAdapter bluetoothAdapter) {
    if (bluetoothLeAdvertiser == null) {
      bluetoothLeAdvertiser = newInstance(bluetoothAdapter);
    }
    return bluetoothLeAdvertiser;
  }

  @SuppressLint("PrivateApi")
  @SuppressWarnings("unchecked")
  private static BluetoothLeAdvertiser newInstance(BluetoothAdapter bluetoothAdapter) {
    try {
      Class<?> iBluetoothManagerClass =
          Shadow.class.getClassLoader().loadClass("android.bluetooth.IBluetoothManager");

      Object bluetoothManager = ReflectionHelpers.getField(bluetoothAdapter, "mManagerService");

      return Shadow.newInstance(
          BluetoothLeAdvertiser.class,
          new Class<?>[] {iBluetoothManagerClass},
          new Object[] {bluetoothManager});
    } catch (ClassNotFoundException e) {
      return null;
    }
  }

  /**
   * Performs a subset of the checks on advertiseData and scanResponse that a real Android device
   * will preform, such as size checks. On a real device, there are many more checks that have not
   * yet been implemented here (eg. there is a global limit to the number of simultaneous calls to
   * startAdvertising that should return ADVERTISE_FAILED_TOO_MANY_ADVERTISERS).
   *
   * <p>Returns AdvertiseCallback.onStartSuccess() immediately if all checks pass.
   */
  @Implementation
  protected void startAdvertising(
      AdvertiseSettings settings,
      AdvertiseData advertiseData,
      AdvertiseData scanResponse,
      AdvertiseCallback callback) {
    // Check data length, and fail if it's too large. Note that the available size changed between
    // L and L MR1.
    for (byte[] data : advertiseData.getServiceData().values()) {
      if (data.length > 20 && Build.VERSION.SDK_INT == LOLLIPOP) {
        callback.onStartFailure(AdvertiseCallback.ADVERTISE_FAILED_DATA_TOO_LARGE);
        return;
      }
      if (data.length > 23 && Build.VERSION.SDK_INT >= LOLLIPOP_MR1) {
        callback.onStartFailure(AdvertiseCallback.ADVERTISE_FAILED_DATA_TOO_LARGE);
        return;
      }
    }
    for (int i = 0; i < advertiseData.getManufacturerSpecificData().size(); i++) {
      int key = advertiseData.getManufacturerSpecificData().keyAt(i);
      byte[] data = advertiseData.getManufacturerSpecificData().get(key);
      if (data.length > 22 && Build.VERSION.SDK_INT == LOLLIPOP) {
        callback.onStartFailure(AdvertiseCallback.ADVERTISE_FAILED_DATA_TOO_LARGE);
        return;
      }
      if (data.length > 25 && Build.VERSION.SDK_INT >= LOLLIPOP_MR1) {
        callback.onStartFailure(AdvertiseCallback.ADVERTISE_FAILED_DATA_TOO_LARGE);
        return;
      }
    }

    // Attempt to call the BluetoothManager, if it's been set.
    try {
      Object bluetoothManager =
          ReflectionHelpers.getField(realBluetoothLeAdvertiser, "mBluetoothManager");
      if (bluetoothManager != null) {
        Object bluetoothGatt =
            ReflectionHelpers.callInstanceMethod(bluetoothManager, "getBluetoothGatt");
        Class<?> iBluetoothGattClass =
            Shadow.class.getClassLoader().loadClass("android.bluetooth.IBluetoothGatt");
        ReflectionHelpers.callInstanceMethod(
            iBluetoothGattClass,
            bluetoothGatt,
            "startMultiAdvertising",
            from(Integer.TYPE, 0),
            from(AdvertiseData.class, advertiseData),
            from(AdvertiseData.class, scanResponse),
            from(AdvertiseSettings.class, settings));
      }
    } catch (ClassNotFoundException e) {
      // ignored
    }

    // Report advertising as successfully started.
    callback.onStartSuccess(settings);
  }

  @Implementation
  protected void stopAdvertising(AdvertiseCallback callback) {
    // Attempt to call the BluetoothManager, if it's been set.
    try {
      Object bluetoothManager =
          ReflectionHelpers.getField(realBluetoothLeAdvertiser, "mBluetoothManager");
      if (bluetoothManager != null) {
        Object bluetoothGatt =
            ReflectionHelpers.callInstanceMethod(bluetoothManager, "getBluetoothGatt");
        Class<?> iBluetoothGattClass =
            Shadow.class.getClassLoader().loadClass("android.bluetooth.IBluetoothGatt");
        ReflectionHelpers.callInstanceMethod(
            iBluetoothGattClass, bluetoothGatt, "stopMultiAdvertising", from(Integer.TYPE, 0));
      }
    } catch (ClassNotFoundException e) {
      // ignored
    }
  }
}
