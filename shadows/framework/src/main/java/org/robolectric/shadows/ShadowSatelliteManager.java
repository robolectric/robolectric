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
import com.android.internal.telephony.ITelephony;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
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
  private final AtomicBoolean isSupported = new AtomicBoolean(false);
  private final AtomicReference<SatelliteException> isSupportedError = new AtomicReference<>();

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

  @Implementation
  protected static ITelephony getITelephony() {
    if (ServiceManager.checkService(Context.TELEPHONY_SERVICE) == null) {
      return null;
    }
    return fakeTelephony;
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
  }
}
