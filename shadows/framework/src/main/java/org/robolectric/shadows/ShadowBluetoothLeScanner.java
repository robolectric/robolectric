package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.LOLLIPOP;

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

/** Adds Robolectric support for BLE scanning. */
@Implements(value = BluetoothLeScanner.class, minSdk = LOLLIPOP)
public class ShadowBluetoothLeScanner {

  private final Set<ScanCallback> scanCallbacks = new HashSet<ScanCallback>();

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
