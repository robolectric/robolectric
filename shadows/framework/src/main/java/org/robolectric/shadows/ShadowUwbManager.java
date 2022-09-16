package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.TIRAMISU;

import android.os.Build.VERSION_CODES;
import android.os.CancellationSignal;
import android.os.PersistableBundle;
import android.uwb.RangingSession;
import android.uwb.UwbManager;
import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.shadow.api.Shadow;

/** Adds Robolectric support for UWB ranging. */
@Implements(value = UwbManager.class, minSdk = VERSION_CODES.S, isInAndroidSdk = false)
public class ShadowUwbManager {

  private PersistableBundle specificationInfo = new PersistableBundle();

  private List<PersistableBundle> chipInfos = new ArrayList<>();

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

  /**
   * Instantiates a {@link ShadowRangingSession} with the multi-chip API call. {@code chipId} is
   * unused in the shadow implementation, so this is equivalent to {@link
   * ShadowUwbManager#openRangingSession(PersistableBundle, Executor, RangingSession.Callback)}
   */
  @Implementation(minSdk = TIRAMISU)
  protected CancellationSignal openRangingSession(
      PersistableBundle params,
      Executor executor,
      RangingSession.Callback callback,
      String chipId) {
    return openRangingSession(params, executor, callback);
  }

  /**
   * Simply returns the List of bundles provided by {@link ShadowUwbManager#setChipInfos(List)} ,
   * allowing the tester to set multi-chip configuration.
   */
  @Implementation(minSdk = TIRAMISU)
  protected List<PersistableBundle> getChipInfos() {
    return ImmutableList.copyOf(chipInfos);
  }

  /** Sets the list of bundles to be returned by {@link android.uwb.UwbManager#getChipInfos}. */
  public void setChipInfos(List<PersistableBundle> chipInfos) {
    this.chipInfos = new ArrayList<>(chipInfos);
  }
}
