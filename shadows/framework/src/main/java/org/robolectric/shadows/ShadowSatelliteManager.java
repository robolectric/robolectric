package org.robolectric.shadows;

import static android.telephony.satellite.SatelliteManager.SATELLITE_MODEM_STATE_OFF;
import static android.telephony.satellite.SatelliteManager.SATELLITE_RESULT_ERROR;
import static android.telephony.satellite.SatelliteManager.SATELLITE_RESULT_SUCCESS;

import android.annotation.CallbackExecutor;
import android.annotation.NonNull;
import android.annotation.Nullable;
import android.annotation.SystemApi;
import android.content.Context;
import android.os.Build.VERSION_CODES;
import android.os.OutcomeReceiver;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.telephony.satellite.ISatelliteModemStateCallback;
import android.telephony.satellite.SatelliteManager;
import android.telephony.satellite.SatelliteManager.SatelliteException;
import android.telephony.satellite.SatelliteSubscriberProvisionStatus;
import com.android.internal.telephony.ITelephony;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;

/** Shadow for {@link SatelliteManager} system service. */
@Implements(
    value = SatelliteManager.class,
    minSdk = VERSION_CODES.VANILLA_ICE_CREAM,
    isInAndroidSdk = false)
public class ShadowSatelliteManager {

  private static final FakeTelephony fakeTelephony = new FakeTelephony();
  private static final AtomicBoolean isSupported = new AtomicBoolean(false);
  private static final AtomicReference<SatelliteException> isSupportedError =
      new AtomicReference<>();
  private static final AtomicBoolean isCommunicationAllowedForCurrentLocation =
      new AtomicBoolean(false);
  private static final AtomicReference<SatelliteException>
      isCommunicationAllowedForCurrentLocationError = new AtomicReference<>();
  private static final AtomicReference<List<SatelliteSubscriberProvisionStatus>>
      satelliteSubscriberProvisionStatus = new AtomicReference<>();
  private static final AtomicReference<SatelliteException> satelliteSubscriberProvisionStatusError =
      new AtomicReference<>();
  private static final Map<Integer, Set<Integer>> attachRestrictionReasons = new HashMap<>();
  private static final List<String> satelliteDataOptimizedApps = new ArrayList<>();
  private static final Map<Consumer<Boolean>, Executor> supportedStateChangedCallbacks =
      new HashMap<>();

  /** Test helper: set the provision status that will be returned by requests. */
  public void setSatelliteSubscriberProvisionStatus(
      List<SatelliteSubscriberProvisionStatus> status, @Nullable SatelliteException error) {
    satelliteSubscriberProvisionStatus.set(status);
    satelliteSubscriberProvisionStatusError.set(error);
  }

  /** Test helper: set attach restriction reasons for a carrier. */
  public void setAttachRestrictionReasonsForCarrier(int carrierId, Set<Integer> reasons) {
    Preconditions.checkNotNull(reasons);
    attachRestrictionReasons.put(carrierId, new HashSet<>(reasons));
  }

  /** Test helper: set satellite data optimized apps list. */
  public void setSatelliteDataOptimizedApps(List<String> apps) {
    Preconditions.checkNotNull(apps);
    satelliteDataOptimizedApps.clear();
    satelliteDataOptimizedApps.addAll(apps);
  }

  /** Updates the current state of the satellite modem, and notifies all listeners. */
  public void triggerSatelliteModemStateChange(int state) throws RemoteException {
    fakeTelephony.triggerSatelliteModemStateChange(state);
  }

  /** Sets the result of {@link SatelliteManager#registerForModemStateChanged} calls. */
  public void setRegistrationSatelliteResult(int result) {
    fakeTelephony.setNextRegistrationResult(result);
  }

  /**
   * Sets the result of {@link SatelliteManager#requestIsSupported} calls.
   *
   * <p>Accepts both the boolean result indicating whether satellite is supported or not, and an
   * optional error which will be returned instead, when non-null.
   */
  public void setIsSupportedResponse(boolean newValue, @Nullable SatelliteException error) {
    isSupported.set(newValue);
    isSupportedError.set(error);
  }

  @Implementation(minSdk = VERSION_CODES.VANILLA_ICE_CREAM)
  @SystemApi
  protected void requestIsSupported(
      @NonNull @CallbackExecutor Executor executor,
      @NonNull OutcomeReceiver<Boolean, SatelliteException> callback) {
    executor.execute(
        () -> {
          if (isSupportedError.get() != null) {
            callback.onError(isSupportedError.get());
          } else {
            callback.onResult(isSupported.get());
          }
        });
  }

  /**
   * Sets the result of {@link SatelliteManager#requestIsCommunicationAllowedForCurrentLocation}
   * calls.
   *
   * <p>Accepts both the boolean result indicating whether satellite is allowed or not, and an
   * optional error which will be returned instead, when non-null.
   */
  public void setIsCommunicationAllowedForCurrentLocation(
      boolean newValue, @Nullable SatelliteException error) {
    isCommunicationAllowedForCurrentLocation.set(newValue);
    isCommunicationAllowedForCurrentLocationError.set(error);
  }

  @Implementation(minSdk = VERSION_CODES.VANILLA_ICE_CREAM)
  protected void requestIsCommunicationAllowedForCurrentLocation(
      @NonNull @CallbackExecutor Executor executor,
      @NonNull OutcomeReceiver<Boolean, SatelliteException> callback) {
    executor.execute(
        () -> {
          if (isCommunicationAllowedForCurrentLocationError.get() != null) {
            callback.onError(isCommunicationAllowedForCurrentLocationError.get());
          } else {
            callback.onResult(isCommunicationAllowedForCurrentLocation.get());
          }
        });
  }

  @Implementation
  protected static ITelephony getITelephony() {
    if (ServiceManager.checkService(Context.TELEPHONY_SERVICE) == null) {
      return null;
    }
    return fakeTelephony;
  }

  @Implementation(minSdk = VERSION_CODES.BAKLAVA)
  @CanIgnoreReturnValue
  protected int registerForSupportedStateChanged(
      @NonNull @CallbackExecutor Executor executor, @NonNull Consumer<Boolean> callback) {
    supportedStateChangedCallbacks.put(callback, executor);
    return SATELLITE_RESULT_SUCCESS;
  }

  @Implementation(minSdk = VERSION_CODES.BAKLAVA)
  protected void unregisterForSupportedStateChanged(@NonNull Consumer<Boolean> callback) {
    supportedStateChangedCallbacks.remove(callback);
  }

  /** Triggers the callback for satellite supported state changes. */
  public void triggerOnSupportedStateChanged(boolean isSupported) {
    for (Map.Entry<Consumer<Boolean>, Executor> entry : supportedStateChangedCallbacks.entrySet()) {
      entry.getValue().execute(() -> entry.getKey().accept(isSupported));
    }
  }

  @Implementation(minSdk = VERSION_CODES.BAKLAVA)
  protected void requestSatelliteSubscriberProvisionStatus(
      @NonNull @CallbackExecutor Executor executor,
      @NonNull
          OutcomeReceiver<List<SatelliteSubscriberProvisionStatus>, SatelliteException> callback) {
    executor.execute(
        () -> {
          if (satelliteSubscriberProvisionStatusError.get() != null) {
            callback.onError(satelliteSubscriberProvisionStatusError.get());
          } else {
            callback.onResult(satelliteSubscriberProvisionStatus.get());
          }
        });
  }

  @Implementation(minSdk = VERSION_CODES.BAKLAVA)
  protected Set<Integer> getAttachRestrictionReasonsForCarrier(int carrierId) {
    return ImmutableSet.copyOf(attachRestrictionReasons.getOrDefault(carrierId, new HashSet<>()));
  }

  @Implementation(minSdk = VERSION_CODES.BAKLAVA)
  protected List<String> getSatelliteDataOptimizedApps() {
    return ImmutableList.copyOf(satelliteDataOptimizedApps);
  }

  private static class FakeTelephony extends ITelephony.Default {

    private final Set<ISatelliteModemStateCallback> satelliteCallbacks = new HashSet<>();

    private int nextRegistrationResult = SatelliteManager.SATELLITE_RESULT_SUCCESS;
    private int currentSatelliteModeState = SATELLITE_MODEM_STATE_OFF;

    public void reset() {
      nextRegistrationResult = SATELLITE_RESULT_SUCCESS;
      currentSatelliteModeState = SATELLITE_MODEM_STATE_OFF;
      satelliteCallbacks.clear();
    }

    // @Override - it's an override, but robolectric complains if declared
    public int registerForSatelliteModemStateChanged(
        int unused, ISatelliteModemStateCallback callback) throws RemoteException {
      if (nextRegistrationResult != SATELLITE_RESULT_ERROR) {
        satelliteCallbacks.add(callback);
        callback.onSatelliteModemStateChanged(currentSatelliteModeState);
      }
      return nextRegistrationResult;
    }

    // For Android B
    public int registerForSatelliteModemStateChanged(ISatelliteModemStateCallback callback)
        throws RemoteException {
      return registerForSatelliteModemStateChanged(0, callback);
    }

    // @Override - it's an override, but robolectric complains if declared
    public void unregisterForModemStateChanged(int unused, ISatelliteModemStateCallback callback) {
      satelliteCallbacks.remove(callback);
    }

    // For Android B
    public void unregisterForModemStateChanged(ISatelliteModemStateCallback callback) {
      unregisterForModemStateChanged(0, callback);
    }

    public void triggerSatelliteModemStateChange(int state) throws RemoteException {
      currentSatelliteModeState = state;
      for (ISatelliteModemStateCallback callback : satelliteCallbacks) {
        callback.onSatelliteModemStateChanged(state);
      }
    }

    public void setNextRegistrationResult(int nextRegistrationResult) {
      this.nextRegistrationResult = nextRegistrationResult;
    }
  }

  @Resetter
  public static void reset() {
    fakeTelephony.reset();
    isSupported.set(false);
    isSupportedError.set(null);
    isCommunicationAllowedForCurrentLocation.set(false);
    isCommunicationAllowedForCurrentLocationError.set(null);
    satelliteSubscriberProvisionStatus.set(null);
    satelliteSubscriberProvisionStatusError.set(null);
    attachRestrictionReasons.clear();
    satelliteDataOptimizedApps.clear();
    supportedStateChangedCallbacks.clear();
  }
}
