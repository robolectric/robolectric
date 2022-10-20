package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.P;

import android.net.wifi.rtt.RangingRequest;
import android.net.wifi.rtt.RangingResult;
import android.net.wifi.rtt.RangingResultCallback;
import android.net.wifi.rtt.WifiRttManager;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/** Shadow for {@link android.net.wifi.rtt.WifiRttManager}. */
@Implements(value = WifiRttManager.class, minSdk = P)
public class ShadowWifiRttManager {
  private List<RangingResult> rangingResults = new ArrayList<>();

  /**
   * If there are RangingResults set by the setRangeResults method of this shadow class, this method
   * will call the onRangingResults method of the callback on the executor thread and pass the list
   * of RangingResults. If there are no ranging results set, it will pass
   * RangingResultCallback.STATUS_CODE_FAIL to the onRangingFailure method of the callback, also
   * called on the executor thread.
   */
  @Implementation(minSdk = P)
  protected void startRanging(
      RangingRequest request, Executor executor, RangingResultCallback callback) {
    if (!rangingResults.isEmpty()) {
      executor.execute(() -> callback.onRangingResults(this.rangingResults));
    } else {
      executor.execute(() -> callback.onRangingFailure(RangingResultCallback.STATUS_CODE_FAIL));
    }
  }

  /** Assumes the WifiRttManager is always available. */
  @Implementation(minSdk = P)
  protected boolean isAvailable() {
    return true;
  }

  /**
   * This method sets the RangingResults that are passed to the RangingResultCallback when the
   * shadow startRanging method is called.
   */
  public void setRangeResults(List<RangingResult> rangingResults) {
    this.rangingResults = rangingResults;
  }
}
