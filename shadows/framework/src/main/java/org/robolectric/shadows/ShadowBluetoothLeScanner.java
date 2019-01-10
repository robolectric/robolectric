package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.LOLLIPOP;

import android.annotation.SuppressLint;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanSettings;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.shadow.api.Shadow;

/** Adds Robolectric support for BLE scanning. */
@Implements(BluetoothLeScanner.class)
public class ShadowBluetoothLeScanner {
  private static BluetoothLeScanner bluetoothLeScanner;

  private Set<ScanCallback> scanCallbacks = new HashSet<ScanCallback>();

  static BluetoothLeScanner getInstance() {
    if (bluetoothLeScanner == null) {
      bluetoothLeScanner = newInstance();
    }
    return bluetoothLeScanner;
  }

  @SuppressLint("PrivateApi")
  @SuppressWarnings("unchecked")
  private static BluetoothLeScanner newInstance() {
    try {
      Class<?> iBluetoothManagerClass =
          Shadow.class.getClassLoader().loadClass("android.bluetooth.IBluetoothManager");

      return Shadow.newInstance(
          BluetoothLeScanner.class, new Class<?>[] {iBluetoothManagerClass}, new Object[] {null});
    } catch (ClassNotFoundException e) {
      return null;
    }
  }

  @Implementation(minSdk = LOLLIPOP)
  protected void startScan(List<ScanFilter> filters, ScanSettings settings, ScanCallback callback) {
    scanCallbacks.add(callback);
  }

  @Implementation(minSdk = LOLLIPOP)
  protected void stopScan(ScanCallback callback) {
    scanCallbacks.remove(callback);
  }

  public Set<ScanCallback> getScanCallbacks() {
    return Collections.unmodifiableSet(scanCallbacks);
  }
}
