package org.robolectric.shadows;

import android.os.Build.VERSION_CODES;
import android.os.CancellationSignal;
import android.os.PersistableBundle;
import android.uwb.RangingSession;
import android.uwb.UwbManager;
import java.util.concurrent.Executor;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.shadow.api.Shadow;

/** Adds Robolectric support for UWB ranging. */
@Implements(value = UwbManager.class, minSdk = VERSION_CODES.S, isInAndroidSdk = false)
public class ShadowUwbManager {

  private PersistableBundle specificationInfo = new PersistableBundle();
  private ShadowRangingSession.Adapter adapter =
      new ShadowRangingSession.Adapter() {
        @Override
        public void onOpen(
            RangingSession session, RangingSession.Callback callback, PersistableBundle params) {}

        @Override
        public void onStart(
            RangingSession session, RangingSession.Callback callback, PersistableBundle params) {}

        @Override
        public void onReconfigure(
            RangingSession session, RangingSession.Callback callback, PersistableBundle params) {}

        @Override
        public void onStop(RangingSession session, RangingSession.Callback callback) {}

        @Override
        public void onClose(RangingSession session, RangingSession.Callback callback) {}
      };

  /**
   * Simply returns the bundle provided by {@link ShadowUwbManager#setSpecificationInfo()}, allowing
   * the tester to dictate available features.
   */
  @Implementation
  protected PersistableBundle getSpecificationInfo() {
    return specificationInfo;
  }

  /**
   * Instantiates a {@link ShadowRangingSession} with the adapter provided by {@link
   * ShadowUwbManager#setUwbAdapter()}, allowing the tester dictate the results of ranging attempts.
   */
  @Implementation
  protected CancellationSignal openRangingSession(
      PersistableBundle params, Executor executor, RangingSession.Callback callback) {
    RangingSession session = ShadowRangingSession.newInstance(executor, callback, adapter);
    CancellationSignal cancellationSignal = new CancellationSignal();
    cancellationSignal.setOnCancelListener(session::close);
    Shadow.<ShadowRangingSession>extract(session).open(params);
    return cancellationSignal;
  }

  /** Sets the UWB adapter to use for new {@link ShadowRangingSession}s. */
  public void setUwbAdapter(ShadowRangingSession.Adapter adapter) {
    this.adapter = adapter;
  }

  /** Sets the bundle to be returned by {@link android.uwb.UwbManager#getSpecificationInfo}. */
  public void setSpecificationInfo(PersistableBundle specificationInfo) {
    this.specificationInfo = new PersistableBundle(specificationInfo);
  }
}
