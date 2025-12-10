package org.robolectric.shadows;

import android.content.AttributionSource;
import android.os.RemoteException;
import android.ranging.IRangingAdapter;
import android.ranging.IRangingCallbacks;
import android.ranging.IRangingCapabilitiesCallback;
import android.ranging.RangingCapabilities;
import android.ranging.RangingPreference;
import android.ranging.SessionHandle;
import org.robolectric.shadows.ShadowServiceManager.ResettableService;

/** Fake implementation of {@link IRangingAdapter} for testing. */
class FakeRangingAdapter extends IRangingAdapter.Default implements ResettableService {

  private RangingCapabilities rangingCapabilities;
  private IRangingCapabilitiesCallback rangingCapabilitiesCallback;
  private RangingPreference openSessionRangingPreference;

  @Override
  public void registerCapabilitiesCallback(IRangingCapabilitiesCallback rangingCapabilitiesCallback)
      throws RemoteException {
    this.rangingCapabilitiesCallback = rangingCapabilitiesCallback;
    if (rangingCapabilities != null) {
      rangingCapabilitiesCallback.onRangingCapabilities(rangingCapabilities);
    }
  }

  @Override
  public void unregisterCapabilitiesCallback(
      IRangingCapabilitiesCallback rangingCapabilitiesCallback) {
    if (this.rangingCapabilitiesCallback == rangingCapabilitiesCallback) {
      this.rangingCapabilitiesCallback = null;
    }
  }

  @Override
  public void startRanging(
      AttributionSource attributionSource,
      SessionHandle sessionHandle,
      RangingPreference rangingPreference,
      IRangingCallbacks cbs)
      throws RemoteException {
    openSessionRangingPreference = rangingPreference;
  }

  RangingPreference getOpenSessionRangingPreference() throws RemoteException {
    return openSessionRangingPreference;
  }

  void updateRangingCapabilities(RangingCapabilities rangingCapabilities) throws RemoteException {
    this.rangingCapabilities = rangingCapabilities;
    if (rangingCapabilitiesCallback != null) {
      rangingCapabilitiesCallback.onRangingCapabilities(rangingCapabilities);
    }
  }

  @Override
  public void reset() {
    rangingCapabilitiesCallback = null;
    rangingCapabilities = null;
    openSessionRangingPreference = null;
  }
}
