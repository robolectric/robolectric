package org.robolectric.shadows;

import android.os.RemoteException;
import android.ranging.IRangingAdapter;
import android.ranging.IRangingCapabilitiesCallback;
import android.ranging.RangingCapabilities;
import org.robolectric.shadows.ShadowServiceManager.ResettableService;

/** Fake implementation of {@link IRangingAdapter} for testing. */
class FakeRangingAdapter extends IRangingAdapter.Default implements ResettableService {

  private RangingCapabilities rangingCapabilities;
  private IRangingCapabilitiesCallback rangingCapabilitiesCallback;

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
  }
}
