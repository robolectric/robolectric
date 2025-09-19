package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.VANILLA_ICE_CREAM;
import static android.telephony.satellite.SatelliteManager.SATELLITE_MODEM_STATE_OFF;
import static android.telephony.satellite.SatelliteManager.SATELLITE_MODEM_STATE_UNAVAILABLE;
import static android.telephony.satellite.SatelliteManager.SATELLITE_RESULT_ERROR;
import static android.telephony.satellite.SatelliteManager.SATELLITE_RESULT_SUCCESS;
import static com.google.common.truth.Truth.assertThat;
import static com.google.common.util.concurrent.MoreExecutors.directExecutor;
import static org.junit.Assert.assertThrows;

import android.content.Context;
import android.os.OutcomeReceiver;
import android.os.RemoteException;
import android.telephony.satellite.SatelliteManager;
import android.telephony.satellite.SatelliteManager.SatelliteException;
import android.telephony.satellite.SatelliteModemStateCallback;
import androidx.test.core.app.ApplicationProvider;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadow.api.Shadow;

@RunWith(RobolectricTestRunner.class)
@Config(minSdk = VANILLA_ICE_CREAM)
public class ShadowSatelliteManagerTest {

  private static final int UNSET = -1;

  private int currentModemState = UNSET;
  private final Context context = ApplicationProvider.getApplicationContext();

  @Test
  public void registerForModemStateChanged_returnsSuccess() {
    SatelliteManager satelliteManager = context.getSystemService(SatelliteManager.class);
    SatelliteModemStateCallback callback = state -> currentModemState = state;
    int result = satelliteManager.registerForModemStateChanged(directExecutor(), callback);

    assertThat(result).isEqualTo(SATELLITE_RESULT_SUCCESS);
  }

  @Test
  public void registerForModemStateChanged_whenSetToFail_returnsFailure() {
    SatelliteManager satelliteManager = context.getSystemService(SatelliteManager.class);
    SatelliteModemStateCallback callback = state -> currentModemState = state;
    ShadowSatelliteManager shadowSatelliteManager = Shadow.extract(satelliteManager);
    shadowSatelliteManager.setRegistrationSatelliteResult(SATELLITE_RESULT_ERROR);

    int result = satelliteManager.registerForModemStateChanged(directExecutor(), callback);

    assertThat(result).isEqualTo(SATELLITE_RESULT_ERROR);
  }

  @Test
  public void registerForModemStateChanged_whenSetToFail_doesntRegisterCallback()
      throws RemoteException {
    SatelliteManager satelliteManager = context.getSystemService(SatelliteManager.class);
    SatelliteModemStateCallback callback = state -> currentModemState = state;
    ShadowSatelliteManager shadowSatelliteManager = Shadow.extract(satelliteManager);
    shadowSatelliteManager.setRegistrationSatelliteResult(SATELLITE_RESULT_ERROR);
    satelliteManager.registerForModemStateChanged(directExecutor(), callback);

    shadowSatelliteManager.triggerSatelliteModemStateChange(SATELLITE_MODEM_STATE_UNAVAILABLE);

    assertThat(currentModemState).isEqualTo(UNSET);
  }

  @Test
  public void registerForModemStateChanged_registersCallback() {
    SatelliteManager satelliteManager = context.getSystemService(SatelliteManager.class);
    SatelliteModemStateCallback callback = state -> currentModemState = state;
    satelliteManager.registerForModemStateChanged(directExecutor(), callback);

    assertThat(currentModemState).isEqualTo(SATELLITE_MODEM_STATE_OFF);
  }

  @Test
  public void registerForModemStateChanged_whenStateChanges_stateUpdated() throws RemoteException {
    SatelliteManager satelliteManager = context.getSystemService(SatelliteManager.class);
    SatelliteModemStateCallback callback = state -> currentModemState = state;
    satelliteManager.registerForModemStateChanged(directExecutor(), callback);
    ShadowSatelliteManager shadowSatelliteManager = Shadow.extract(satelliteManager);

    shadowSatelliteManager.triggerSatelliteModemStateChange(SATELLITE_MODEM_STATE_UNAVAILABLE);

    assertThat(currentModemState).isEqualTo(SATELLITE_MODEM_STATE_UNAVAILABLE);
  }

  @Test
  public void registerForModemStateChanged_whenTelephonyUnavailable_throws() {
    SatelliteModemStateCallback callback = state -> currentModemState = state;
    Object callbackObject = callback;
    ShadowServiceManager.setServiceAvailability(Context.TELEPHONY_SERVICE, false);

    assertThrows(
        IllegalStateException.class,
        () ->
            context
                .getSystemService(SatelliteManager.class)
                .registerForModemStateChanged(
                    directExecutor(), (SatelliteModemStateCallback) callbackObject));
  }

  @Test
  public void unregisterForModemStateChanged_unregistersCallback() throws RemoteException {
    SatelliteManager satelliteManager = context.getSystemService(SatelliteManager.class);
    SatelliteModemStateCallback callback = state -> currentModemState = state;
    satelliteManager.registerForModemStateChanged(directExecutor(), callback);

    satelliteManager.unregisterForModemStateChanged(callback);
    ShadowSatelliteManager shadowSatelliteManager = Shadow.extract(satelliteManager);
    shadowSatelliteManager.triggerSatelliteModemStateChange(SATELLITE_MODEM_STATE_UNAVAILABLE);

    assertThat(currentModemState).isEqualTo(SATELLITE_MODEM_STATE_OFF);
  }

  @Test
  public void unregisterForModemStateChanged_whenTelephonyUnavailable_throws() {
    SatelliteModemStateCallback callback = state -> currentModemState = state;
    Object callbackObject = callback;
    ShadowServiceManager.setServiceAvailability(Context.TELEPHONY_SERVICE, false);

    assertThrows(
        IllegalStateException.class,
        () ->
            context
                .getSystemService(SatelliteManager.class)
                .unregisterForModemStateChanged((SatelliteModemStateCallback) callbackObject));
  }

  @Test
  public void requestIsSupported_returnsFalseByDefault() throws Exception {
    assertThat(requestIsSupported()).isFalse();
  }

  @Test
  public void requestIsSupported_whenSetToTrue_returnsTrue() throws Exception {
    getShadowSatelliteManager().setIsSupportedResponse(true, null);

    assertThat(requestIsSupported()).isTrue();
  }

  @Test
  public void requestIsSupported_whenSetToFalse_returnsFalse() throws Exception {
    getShadowSatelliteManager().setIsSupportedResponse(false, null);

    assertThat(requestIsSupported()).isFalse();
  }

  @Test
  public void requestIsSupported_whenSetToError_throws() throws Exception {
    getShadowSatelliteManager().setIsSupportedResponse(true, new SatelliteException(123));

    assertThrows(Exception.class, this::requestIsSupported);
  }

  private boolean requestIsSupported() throws Exception {
    AtomicBoolean isSupported = new AtomicBoolean(false);
    // Declared as Exception to work around NoClassDefFoundError relating to SatelliteException's
    // definition being flagged.
    AtomicReference<Exception> error = new AtomicReference<>();
    OutcomeReceiver<Boolean, SatelliteException> callback =
        new OutcomeReceiver<Boolean, SatelliteException>() {
          @Override
          public void onResult(Boolean result) {
            isSupported.set(result);
          }

          @Override
          public void onError(SatelliteException e) {
            error.set(e); // Still receives SatelliteException, but stored as Exception
          }
        };

    getShadowSatelliteManager().requestIsSupported(directExecutor(), callback);
    if (error.get() != null) {
      throw error.get();
    }
    return isSupported.get();
  }

  @Test
  public void requestIsCommunicationAllowedForCurrentLocation_returnsFalseByDefault()
      throws Exception {
    assertThat(requestIsCommunicationAllowedForCurrentLocation()).isFalse();
  }

  @Test
  public void requestIsCommunicationAllowedForCurrentLocation_whenSetToTrue_returnsTrue()
      throws Exception {
    getShadowSatelliteManager().setIsCommunicationAllowedForCurrentLocation(true, null);

    assertThat(requestIsCommunicationAllowedForCurrentLocation()).isTrue();
  }

  @Test
  public void requestIsCommunicationAllowedForCurrentLocation_whenSetToFalse_returnsFalse()
      throws Exception {
    getShadowSatelliteManager().setIsCommunicationAllowedForCurrentLocation(false, null);

    assertThat(requestIsCommunicationAllowedForCurrentLocation()).isFalse();
  }

  @Test
  public void requestIsCommunicationAllowedForCurrentLocation_whenSetToError_throws()
      throws Exception {
    getShadowSatelliteManager()
        .setIsCommunicationAllowedForCurrentLocation(true, new SatelliteException(123));

    assertThrows(Exception.class, this::requestIsCommunicationAllowedForCurrentLocation);
  }

  private boolean requestIsCommunicationAllowedForCurrentLocation() throws Exception {
    AtomicBoolean isCommunicationAllowedForCurrentLocation = new AtomicBoolean(false);
    // Declared as Exception to work around NoClassDefFoundError relating to SatelliteException's
    // definition being flagged.
    AtomicReference<Exception> error = new AtomicReference<>();
    OutcomeReceiver<Boolean, SatelliteException> callback =
        new OutcomeReceiver<Boolean, SatelliteException>() {
          @Override
          public void onResult(Boolean result) {
            isCommunicationAllowedForCurrentLocation.set(result);
          }

          @Override
          public void onError(SatelliteException e) {
            error.set(e); // Still receives SatelliteException, but stored as Exception
          }
        };

    getShadowSatelliteManager()
        .requestIsCommunicationAllowedForCurrentLocation(directExecutor(), callback);
    if (error.get() != null) {
      throw error.get();
    }
    return isCommunicationAllowedForCurrentLocation.get();
  }

  private ShadowSatelliteManager getShadowSatelliteManager() {
    return Shadow.extract(context.getSystemService(SatelliteManager.class));
  }
}
