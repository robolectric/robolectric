package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.Q;

import android.annotation.SuppressLint;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanSettings;
import android.os.Build;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.shadow.api.Shadow;

/** Adds Robolectric support for BLE scanning. */
@Implements(value = BluetoothLeScanner.class, minSdk = LOLLIPOP)
public class ShadowBluetoothLeScanner {
  private static BluetoothLeScanner bluetoothLeScanner;

  private final Set<ScanCallback> scanCallbacks = new HashSet<ScanCallback>();

  static BluetoothLeScanner getInstance() {
    if (bluetoothLeScanner == null) {
      bluetoothLeScanner = newInstance();
    }
    return bluetoothLeScanner;
  }

  @SuppressLint("PrivateApi")
  private static BluetoothLeScanner newInstance() {
    try {
      Class<?> iBluetoothManagerClass =
          Shadow.class.getClassLoader().loadClass("android.bluetooth.IBluetoothManager");

      if (Build.VERSION.SDK_INT > Q) {
        return Shadow.newInstance(
            BluetoothLeScanner.class,
            new Class<?>[] {iBluetoothManagerClass, String.class, String.class},
            new Object[] {null, "packageName", /*featureId=*/ null});
      } else {
        return Shadow.newInstance(
            BluetoothLeScanner.class, new Class<?>[] {iBluetoothManagerClass}, new Object[] {null});
      }
    } catch (ClassNotFoundException e) {
      return null;
    }
  }

  /**
   * Tracks ongoing scans. Use {@link #getScanCallbacks} to get a list of any currently registered
   * {@link ScanCallback}s.
   */
  @Implementation
  protected void startScan(List<ScanFilter> filters, ScanSettings settings, ScanCallback callback) {
    scanCallbacks.add(callback);
  }

  @Implementation
  protected void stopScan(ScanCallback callback) {
    scanCallbacks.remove(callback);
  }

  /** Returns all currently active {@link ScanCallback}s. */
  public Set<ScanCallback> getScanCallbacks() {
    return Collections.unmodifiableSet(scanCallbacks);
  }
}
