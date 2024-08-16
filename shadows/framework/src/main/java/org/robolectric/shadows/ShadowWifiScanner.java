package org.robolectric.shadows;

import static com.google.common.util.concurrent.MoreExecutors.directExecutor;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.net.wifi.IWifiScannerListener;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiScanner;
import android.net.wifi.WifiScanner.ScanData;
import android.net.wifi.WifiScanner.ScanListener;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.RemoteException;
import android.util.SparseArray;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.ForType;

/** Shadow for {@link android.net.wifi.WifiScanner}. */
@Implements(value = WifiScanner.class, isInAndroidSdk = false)
public class ShadowWifiScanner {
  @RealObject protected WifiScanner realWifiScanner;

  private List<ScanResult> scanResults = new ArrayList<>();

  /**
   * This method sets the scanResults that are immediately passed to the listeners, or returned by
   * getSingleScanResults().
   *
   * <p>All registered listeners are notified immediately after this is called.
   */
  public void setScanResults(List<ScanResult> scanResults) {
    this.scanResults = scanResults;
    if (VERSION.SDK_INT >= VERSION_CODES.UPSIDE_DOWN_CAKE) {
      notifyScanListenersU();
    } else {
      notifyScanListeners();
    }
  }

  @Implementation(minSdk = VERSION_CODES.R)
  protected List<ScanResult> getSingleScanResults() {
    return scanResults;
  }

  private void notifyScanListenersU() {
    Object listenerMapLock =
        reflector(WifiScannerReflector.class, realWifiScanner).getListenerMapLock();

    Map<WifiScanner.ActionListener, IWifiScannerListener.Stub> listenerMap =
        reflector(WifiScannerReflector.class, realWifiScanner).getListenerMap();

    List<IWifiScannerListener.Stub> listeners = null;
    synchronized (listenerMapLock) {
      listeners = new ArrayList<>(listenerMap.values());
    }

    for (IWifiScannerListener.Stub listener : listeners) {
      try {
        listener.onResults(buildScanData());
      } catch (RemoteException e) {
        throw new RuntimeException("Failed to notify scan listeners", e);
      }
    }
  }

  private void notifyScanListeners() {
    Object listenerMapLock =
        reflector(WifiScannerReflector.class, realWifiScanner).getListenerMapLock();
    SparseArray<Object> listenerMap = null;
    SparseArray<Executor> executorMap = null;

    synchronized (listenerMapLock) {
      listenerMap =
          reflector(WifiScannerReflector.class, realWifiScanner).getListenerSparseArray().clone();

      if (VERSION.SDK_INT >= VERSION_CODES.R) {
        executorMap =
            reflector(WifiScannerReflector.class, realWifiScanner).getExecutorSparseArray().clone();
      }
    }

    // Iterate over keys
    for (int i = 0; i < listenerMap.size(); i++) {
      int key = listenerMap.keyAt(i);
      Object value = listenerMap.valueAt(i);
      Executor executor = null;

      if (executorMap != null) {
        executor = executorMap.get(key);
      }

      if (executor == null) {
        executor = directExecutor();
      }

      if (value instanceof ScanListener) {
        ScanListener listener = (ScanListener) value;
        executor.execute(() -> listener.onResults(buildScanData()));
      }
    }
  }

  @ForType(WifiScanner.class)
  interface WifiScannerReflector {
    @Accessor("mListenerMapLock")
    Object getListenerMapLock();

    @Accessor("mListenerMap")
    Map<WifiScanner.ActionListener, IWifiScannerListener.Stub> getListenerMap();

    @Accessor("mListenerMap")
    SparseArray<Object> getListenerSparseArray();

    @Accessor("mExecutorMap")
    SparseArray<Executor> getExecutorSparseArray();
  }

  private ScanData[] buildScanData() {
    return new ScanData[] {
      new WifiScanner.ScanData(/* id= */ 0, /* flags= */ 0, scanResults.toArray(new ScanResult[0]))
    };
  }
}
