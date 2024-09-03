package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.TIRAMISU;

import android.os.Build.VERSION_CODES;
import android.os.CancellationSignal;
import android.os.PersistableBundle;
import android.uwb.AdapterState;
import android.uwb.RangingSession;
import android.uwb.StateChangeReason;
import android.uwb.UwbManager;
import android.uwb.UwbManager.AdapterStateCallback;
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

  private AdapterStateCallback callback;

  private int adapterState = AdapterStateCallback.STATE_ENABLED_INACTIVE;

  private int stateChangedReason = AdapterStateCallback.STATE_CHANGED_REASON_SYSTEM_POLICY;

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

  @Implementation
  protected void registerAdapterStateCallback(Executor executor, AdapterStateCallback callback) {
    this.callback = callback;
    callback.onStateChanged(adapterState, stateChangedReason);
  }

  /**
   * Simulates adapter state change by invoking a callback registered by {@link
   * ShadowUwbManager#registerAdapterStateCallback(Executor executor, AdapterStateCallback
   * callback)}.
   *
   * @param state A state that should be passed to the callback.
   * @param reason A reason that should be passed to the callback.
   * @throws IllegalArgumentException if the callback is missing.
   */
  public void simulateAdapterStateChange(@AdapterState int state, @StateChangeReason int reason) {
    if (this.callback == null) {
      throw new IllegalArgumentException("AdapterStateCallback should not be null");
    }

    adapterState = state;
    stateChangedReason = reason;

    this.callback.onStateChanged(state, reason);
  }

  /**
   * Simply returns the bundle provided by {@link ShadowUwbManager#setSpecificationInfo()}, allowing
   * the tester to dictate available features.
   */
  @Implementation
  protected PersistableBundle getSpecificationInfo() {
    return specificationInfo;
  }

  /**
   * Returns the adapter state provided by {@link ShadowUwbManager#simulateAdapterStateChange()}.
   */
  @Implementation
  @AdapterState
  protected int getAdapterState() {
    return adapterState;
  }

  /**
   * Instantiates a {@link ShadowRangingSession} with the adapter provided by {@link
   * ShadowUwbManager#setUwbAdapter()}, allowing the tester dictate the results of ranging attempts.
   *
   * @throws IllegalArgumentException if UWB is disabled.
   */
  @Implementation
  protected CancellationSignal openRangingSession(
      PersistableBundle params, Executor executor, RangingSession.Callback callback) {
    if (!isUwbEnabled()) {
      throw new IllegalStateException("Uwb is not enabled");
    }
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

  /** Returns whether UWB is enabled or disabled. */
  @Implementation(minSdk = TIRAMISU)
  protected boolean isUwbEnabled() {
    return adapterState != AdapterStateCallback.STATE_DISABLED;
  }

  /**
   * Disables or enables UWB by the user.
   *
   * @param enabled value representing intent to disable or enable UWB.
   */
  @Implementation
  protected void setUwbEnabled(boolean enabled) {
    boolean stateChanged = false;

    if (enabled && adapterState == AdapterStateCallback.STATE_DISABLED) {
      adapterState = AdapterStateCallback.STATE_ENABLED_INACTIVE;
      stateChanged = true;
    } else if (!enabled && adapterState != AdapterStateCallback.STATE_DISABLED) {
      adapterState = AdapterStateCallback.STATE_DISABLED;
      stateChanged = true;
    }

    if (this.callback != null && stateChanged) {
      this.callback.onStateChanged(
          adapterState, AdapterStateCallback.STATE_CHANGED_REASON_SYSTEM_POLICY);
    }
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
