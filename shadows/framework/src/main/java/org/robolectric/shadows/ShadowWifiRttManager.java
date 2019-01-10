package org.robolectric.shadows;

import android.net.wifi.rtt.RangingRequest;
import android.net.wifi.rtt.RangingResult;
import android.net.wifi.rtt.RangingResultCallback;
import android.net.wifi.rtt.WifiRttManager;
import java.util.List;
import java.util.concurrent.Executor;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;

/** Shadow for {@link android.net.wifi.rtt.WifiRttManager}. */
@Implements(WifiRttManager.class)
public class ShadowWifiRttManager {
  private List<RangingResult> rangeResults;
  @RealObject WifiRttManager wifiRttManager;

  public void startRanging(
      RangingRequest request, Executor executor, RangingResultCallback callback) {
    if (rangeResults.size() > 0) {
      callback.onRangingResults(this.rangeResults);
    } else {
      callback.onRangingFailure(RangingResult.STATUS_FAIL);
    }
  }

  public void setRangeResults(List<RangingResult> rangeResults) {
    this.rangeResults = rangeResults;
  }
}
