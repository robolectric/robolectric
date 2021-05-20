package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.O;
import static java.util.Collections.unmodifiableList;

import android.app.PendingIntent;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanSettings;
import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nullable;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/** Adds Robolectric support for BLE scanning. */
@Implements(value = BluetoothLeScanner.class, minSdk = LOLLIPOP)
public class ShadowBluetoothLeScanner {

  private List<ScanParams> activeScanParams = new ArrayList<>();

  /**
   * Encapsulates scan params passed to {@link android.bluetooth.BluetoothAdapter} startScan
   * methods.
   */
  @AutoValue
  public abstract static class ScanParams {
    public abstract ImmutableList<ScanFilter> scanFilters();

    @Nullable
    public abstract ScanSettings scanSettings();

    @Nullable
    public abstract PendingIntent pendingIntent();

    @Nullable
    public abstract ScanCallback scanCallback();

    static ScanParams create(
        List<ScanFilter> filters, ScanSettings settings, ScanCallback scanCallback) {
      ImmutableList<ScanFilter> filtersCopy =
          (filters == null) ? ImmutableList.of() : ImmutableList.copyOf(filters);
      return new AutoValue_ShadowBluetoothLeScanner_ScanParams(
          filtersCopy, settings, null, scanCallback);
    }

    static ScanParams create(
        List<ScanFilter> filters, ScanSettings settings, PendingIntent pendingIntent) {
      ImmutableList<ScanFilter> filtersCopy =
          (filters == null) ? ImmutableList.of() : ImmutableList.copyOf(filters);
      return new AutoValue_ShadowBluetoothLeScanner_ScanParams(
          filtersCopy, settings, pendingIntent, null);
    }
  }

  /**
   * Tracks ongoing scans. Use {@link #getScanCallbacks} to get a list of any currently registered
   * {@link ScanCallback}s.
   */
  @Implementation
  protected void startScan(List<ScanFilter> filters, ScanSettings settings, ScanCallback callback) {
    if (filters != null) {
      filters = unmodifiableList(filters);
    }

    activeScanParams.add(ScanParams.create(filters, settings, callback));
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
    activeScanParams.add(ScanParams.create(filters, settings, pendingIntent));
    return 0;
  }

  @Implementation
  protected void stopScan(ScanCallback callback) {
    activeScanParams =
        Lists.newArrayList(
            Iterables.filter(
                activeScanParams, input -> !Objects.equals(callback, input.scanCallback())));
  }

  @Implementation(minSdk = O)
  protected void stopScan(PendingIntent pendingIntent) {
    activeScanParams =
        Lists.newArrayList(
            Iterables.filter(
                activeScanParams, input -> !Objects.equals(pendingIntent, input.pendingIntent())));
  }

  /** Returns all currently active {@link ScanCallback}s. */
  public Set<ScanCallback> getScanCallbacks() {
    ArrayList<ScanCallback> scanCallbacks = new ArrayList<>();

    for (ScanParams scanParams : activeScanParams) {
      if (scanParams.scanCallback() != null) {
        scanCallbacks.add(scanParams.scanCallback());
      }
    }
    return Collections.unmodifiableSet(new HashSet<>(scanCallbacks));
  }

  /** Returns all {@link ScanParams}s representing active scans. */
  public List<ScanParams> getActiveScans() {
    return Collections.unmodifiableList(activeScanParams);
  }
}
