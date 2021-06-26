package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.O;
import static java.util.Collections.unmodifiableList;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;

import android.app.PendingIntent;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanSettings;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/** Adds Robolectric support for BLE scanning. */
@Implements(value = BluetoothLeScanner.class, minSdk = LOLLIPOP)
public class ShadowBluetoothLeScanner {

  private static class ScanParams {
    private final List<ScanFilter> filters;
    private final ScanSettings settings;

    private ScanParams(List<ScanFilter> filters, ScanSettings settings) {
      this.filters = filters;
      this.settings = settings;
    }
  }

  private static final ScanParams EMPTY = new ScanParams(null, null);

  private final Map<ScanCallback, ScanParams> scanCallbacks = new HashMap<>();
  private final Map<PendingIntent, ScanParams> pendingIntents = new HashMap<>();

  /**
   * Tracks ongoing scans. Use {@link #getScanCallbacks} to get a list of any currently registered
   * {@link ScanCallback}s.
   */
  @Implementation
  protected void startScan(List<ScanFilter> filters, ScanSettings settings, ScanCallback callback) {
    if (filters != null) {
      filters = unmodifiableList(filters);
    }
    scanCallbacks.put(callback, new ScanParams(filters, settings));
  }

  /**
   * Tracks ongoing scans. Use {@link #getScanCallbacks} to get a list of any currently registered
   * {@link ScanCallback}s.
   */
  @Implementation(minSdk = O)
  protected int startScan(
      List<ScanFilter> filters, ScanSettings settings, PendingIntent pendingIntent) {
    if (filters != null) {
      filters = unmodifiableList(filters);
    }
    pendingIntents.put(pendingIntent, new ScanParams(filters, settings));
    return 0;
  }

  @Implementation
  protected void stopScan(ScanCallback callback) {
    scanCallbacks.remove(callback);
  }

  @Implementation(minSdk = O)
  protected void stopScan(PendingIntent pendingIntent) {
    pendingIntents.remove(pendingIntent);
  }

  /** Returns all currently active {@link ScanCallback}s. */
  public Set<ScanCallback> getScanCallbacks() {
    return Collections.unmodifiableSet(scanCallbacks.keySet());
  }

  /** Returns all currently active {@link PendingIntent}s. */
  public Set<PendingIntent> getPendingIntents() {
    return Collections.unmodifiableSet(pendingIntents.keySet());
  }

  /** Returns filters associated with an active {@link ScanCallback} */
  public Optional<List<ScanFilter>> getScanFilters(ScanCallback callback) {
    return ofNullable(requireNonNull(scanCallbacks.getOrDefault(callback, EMPTY)).filters);
  }

  /** Returns filters associated with an active {@link PendingIntent} */
  public Optional<List<ScanFilter>> getScanFilters(PendingIntent pendingIntent) {
    return ofNullable(requireNonNull(pendingIntents.getOrDefault(pendingIntent, EMPTY)).filters);
  }

  /** Returns filters associated with an active {@link ScanCallback} */
  public Optional<ScanSettings> getScanSettings(ScanCallback callback) {
    return ofNullable(requireNonNull(scanCallbacks.getOrDefault(callback, EMPTY)).settings);
  }

  /** Returns filters associated with an active {@link PendingIntent} */
  public Optional<ScanSettings> getScanSettings(PendingIntent pendingIntent) {
    return ofNullable(requireNonNull(pendingIntents.getOrDefault(pendingIntent, EMPTY)).settings);
  }
}
