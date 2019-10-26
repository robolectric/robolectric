package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.O;
import static org.robolectric.shadow.api.Shadow.directlyOn;

import android.annotation.NonNull;
import android.annotation.Nullable;
import android.app.PendingIntent;
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
import org.robolectric.annotation.RealObject;

/** Shadow for {@link BluetoothLeScanner}. */
@SuppressWarnings({"UnusedDeclaration"})
@Implements(value = BluetoothLeScanner.class, minSdk = LOLLIPOP)
public class ShadowBluetoothLeScanner {
  @RealObject private BluetoothLeScanner realScanner;

  private final Set<ScanCallback> scanCallbacks = new HashSet<>();
  private final Set<ScanCallback> flushedPendingScanResults = new HashSet<>();
  private final Set<PendingIntent> callbackIntents = new HashSet<>();

  /**
   * Flush pending scan results for a pending scan.
   *
   * @param callback Callback for the scan
   */
  @Implementation
  protected void flushPendingScanResults(ScanCallback callback) {
    flushedPendingScanResults.add(callback);
  }

  /**
   * Starts a scan.
   *
   * @param callback Callback for the scan
   */
  @Implementation
  protected void startScan(final ScanCallback callback) {
    startScan(/* filters = */ null, new ScanSettings.Builder().build(), callback);
  }

  /**
   * Starts a scan.
   *
   * @param filters Filters to apply to the scan
   * @param settings Settings to apply to the scan
   * @param callback Callback for the scan
   */
  @Implementation
  protected void startScan(
      List<ScanFilter> filters, ScanSettings settings, final ScanCallback callback) {
    if (callback == null) {
      throw new IllegalArgumentException("callback is null");
    }

    if (scanCallbacks.contains(callback)) {
      callback.onScanFailed(ScanCallback.SCAN_FAILED_ALREADY_STARTED);
    } else {
      // filters and settings ignored for now
      scanCallbacks.add(callback);
    }
  }

  /**
   * Starts a scan.
   *
   * @param filters Filters to apply to the scan
   * @param settings Settings to apply to the scan
   * @param callbackIntent Callback for the scan
   * @return {@link ScanCallback#NO_ERROR} or an error constant from {@link ScanCallback}
   */
  @Implementation(minSdk = O)
  protected int startScan(
      @Nullable List<ScanFilter> filters,
      @Nullable ScanSettings settings,
      @NonNull PendingIntent callbackIntent) {
    if (callbackIntent == null) {
      throw new IllegalArgumentException("callback is null");
    }

    if (callbackIntents.contains(callbackIntent)) {
      return ScanCallback.SCAN_FAILED_ALREADY_STARTED;
    }

    // filters and settings ignored for now
    callbackIntents.add(callbackIntent);

    return directlyOn(realScanner, BluetoothLeScanner.class)
        .startScan(filters, settings, callbackIntent);
  }

  /** Stops a pending scan for a given {@link ScanCallback}. */
  @Implementation
  protected void stopScan(ScanCallback callback) {
    scanCallbacks.remove(callback);
    directlyOn(realScanner, BluetoothLeScanner.class).stopScan(callback);
  }

  /** Stops a pending scan given for a given {@link PendingIntent}. */
  @Implementation(minSdk = O)
  protected void stopScan(PendingIntent callbackIntent) {
    callbackIntents.remove(callbackIntent);
    directlyOn(realScanner, BluetoothLeScanner.class).stopScan(callbackIntent);
  }

  /** The set {@link ScanCallback}s for pending scans. */
  public Set<ScanCallback> getScanCallbacks() {
    return Collections.unmodifiableSet(scanCallbacks);
  }

  /** The set {@link ScanCallback}s for {@link #flushPendingScanResults(ScanCallback)} requests. */
  public Set<ScanCallback> getFlushedPendingScanResults() {
    return Collections.unmodifiableSet(flushedPendingScanResults);
  }

  /** The set {@link PendingIntent}s fo pending scans. */
  public Set<PendingIntent> getCallbackIntents() {
    return Collections.unmodifiableSet(callbackIntents);
  }
}
