package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.BAKLAVA;
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
import android.telephony.satellite.SatelliteSubscriberInfo;
import android.telephony.satellite.SatelliteSubscriberProvisionStatus;
import androidx.test.core.app.ApplicationProvider;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadow.api.Shadow;

@RunWith(RobolectricTestRunner.class)
@Config(minSdk = BAKLAVA)
public class ShadowSatelliteManagerTest {

  private static final int UNSET = -1;

  private int currentModemState = UNSET;
  private final Context context = ApplicationProvider.getApplicationContext();

  static class TestOutcomeReceiver<T> implements OutcomeReceiver<T, SatelliteException> {
    final AtomicReference<T> result = new AtomicReference<>();
    final AtomicReference<SatelliteException> error = new AtomicReference<>();

    @Override
    public void onResult(T result) {
      this.result.set(result);
    }

    @Override
    public void onError(SatelliteException error) {
      this.error.set(error);
    }
  }

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
    assertThat(requestIsSupported().result.get()).isFalse();
  }

  @Test
  public void requestIsSupported_whenSetToTrue_returnsTrue() throws Exception {
    getShadowSatelliteManager().setIsSupportedResponse(true, null);

    assertThat(requestIsSupported().result.get()).isTrue();
  }

  @Test
  public void requestIsSupported_whenSetToFalse_returnsFalse() throws Exception {
    getShadowSatelliteManager().setIsSupportedResponse(false, null);

    assertThat(requestIsSupported().result.get()).isFalse();
  }

  @Test
  public void requestIsSupported_whenSetToError_throws() throws Exception {
    getShadowSatelliteManager().setIsSupportedResponse(true, new SatelliteException(123));

    TestOutcomeReceiver<Boolean> receiver = requestIsSupported();

    assertThat(receiver.error.get()).isNotNull();
  }

  private TestOutcomeReceiver<Boolean> requestIsSupported() throws Exception {
    TestOutcomeReceiver<Boolean> receiver = new TestOutcomeReceiver<>();
    SatelliteManager satelliteManager = context.getSystemService(SatelliteManager.class);
    satelliteManager.requestIsSupported(directExecutor(), receiver);
    return receiver;
  }

  @Test
  public void requestIsCommunicationAllowedForCurrentLocation_returnsFalseByDefault()
      throws Exception {
    assertThat(requestIsCommunicationAllowedForCurrentLocation().result.get()).isFalse();
  }

  @Test
  public void requestIsCommunicationAllowedForCurrentLocation_whenSetToTrue_returnsTrue()
      throws Exception {
    getShadowSatelliteManager().setIsCommunicationAllowedForCurrentLocation(true, null);

    assertThat(requestIsCommunicationAllowedForCurrentLocation().result.get()).isTrue();
  }

  @Test
  public void requestIsCommunicationAllowedForCurrentLocation_whenSetToFalse_returnsFalse()
      throws Exception {
    getShadowSatelliteManager().setIsCommunicationAllowedForCurrentLocation(false, null);

    assertThat(requestIsCommunicationAllowedForCurrentLocation().result.get()).isFalse();
  }

  @Test
  public void requestIsCommunicationAllowedForCurrentLocation_whenSetToError_throws()
      throws Exception {
    getShadowSatelliteManager()
        .setIsCommunicationAllowedForCurrentLocation(true, new SatelliteException(123));

    TestOutcomeReceiver<Boolean> receiver = requestIsCommunicationAllowedForCurrentLocation();
    assertThat(receiver.error.get()).isNotNull();
  }

  @Test
  public void registerForSupportedStateChanged_callbackIsTriggered() {
    SatelliteManager satelliteManager = context.getSystemService(SatelliteManager.class);
    AtomicReference<Boolean> isSupported = new AtomicReference<>();
    Consumer<Boolean> callback = isSupported::set;

    int result = satelliteManager.registerForSupportedStateChanged(directExecutor(), callback);
    assertThat(result).isEqualTo(SATELLITE_RESULT_SUCCESS);

    getShadowSatelliteManager().triggerOnSupportedStateChanged(true);
    assertThat(isSupported.get()).isTrue();
    getShadowSatelliteManager().triggerOnSupportedStateChanged(false);
    assertThat(isSupported.get()).isFalse();
  }

  @Test
  public void unregisterForSupportedStateChanged_unregistersCallback() {
    SatelliteManager satelliteManager = context.getSystemService(SatelliteManager.class);
    AtomicBoolean wasCalled = new AtomicBoolean(false);
    Consumer<Boolean> callback = supported -> wasCalled.set(true);
    int unused = satelliteManager.registerForSupportedStateChanged(directExecutor(), callback);

    satelliteManager.unregisterForSupportedStateChanged(callback);
    getShadowSatelliteManager().triggerOnSupportedStateChanged(true);

    assertThat(wasCalled.get()).isFalse();
  }

  @Test
  public void triggerOnSupportedStateChanged_triggersAllCallbacks() {
    SatelliteManager satelliteManager = context.getSystemService(SatelliteManager.class);
    AtomicReference<Boolean> callback1Result = new AtomicReference<>();
    AtomicReference<Boolean> callback2Result = new AtomicReference<>();
    Consumer<Boolean> callback1 = callback1Result::set;
    Consumer<Boolean> callback2 = callback2Result::set;
    int unused = satelliteManager.registerForSupportedStateChanged(directExecutor(), callback1);
    unused = satelliteManager.registerForSupportedStateChanged(directExecutor(), callback2);

    getShadowSatelliteManager().triggerOnSupportedStateChanged(true);

    assertThat(callback1Result.get()).isTrue();
    assertThat(callback2Result.get()).isTrue();
  }

  @Test
  public void requestSatelliteSubscriberProvisionStatus_returnsNullByDefault() throws Exception {
    assertThat(requestSatelliteSubscriberProvisionStatus().result.get()).isNull();
  }

  @Test
  public void requestSatelliteSubscriberProvisionStatus_whenSet_returnsStatus() throws Exception {
    List<SatelliteSubscriberProvisionStatus> provisionStatus = new ArrayList<>();
    provisionStatus.add(
        new SatelliteSubscriberProvisionStatus.Builder()
            .setSatelliteSubscriberInfo(
                new SatelliteSubscriberInfo.Builder()
                    .setSubscriptionId(1)
                    .setSubscriberId("test-subscriber-id")
                    .setNiddApn("test-nidd-apn")
                    .build())
            .setProvisioned(true)
            .build());

    getShadowSatelliteManager().setSatelliteSubscriberProvisionStatus(provisionStatus, null);

    assertThat(requestSatelliteSubscriberProvisionStatus().result.get()).isEqualTo(provisionStatus);
  }

  @Test
  public void requestSatelliteSubscriberProvisionStatus_whenSetToError_reportsError() {
    SatelliteException exception = new SatelliteException(123);
    getShadowSatelliteManager().setSatelliteSubscriberProvisionStatus(null, exception);

    TestOutcomeReceiver<List<SatelliteSubscriberProvisionStatus>> receiver =
        requestSatelliteSubscriberProvisionStatus();

    assertThat(receiver.error.get()).isEqualTo(exception);
  }

  @Test
  public void getAttachRestrictionReasonsForCarrier_returnsEmptySetByDefault() {
    SatelliteManager satelliteManager = context.getSystemService(SatelliteManager.class);

    assertThat(satelliteManager.getAttachRestrictionReasonsForCarrier(1)).isEmpty();
  }

  @Test
  public void getAttachRestrictionReasonsForCarrier_whenSet_returnsReasons() {
    SatelliteManager satelliteManager = context.getSystemService(SatelliteManager.class);
    ShadowSatelliteManager shadowSatelliteManager = Shadow.extract(satelliteManager);
    Set<Integer> reasons = new HashSet<>();
    reasons.add(1);
    reasons.add(2);

    shadowSatelliteManager.setAttachRestrictionReasonsForCarrier(1, reasons);

    assertThat(satelliteManager.getAttachRestrictionReasonsForCarrier(1)).isEqualTo(reasons);
    assertThat(satelliteManager.getAttachRestrictionReasonsForCarrier(2)).isEmpty();
  }

  @Test
  public void getSatelliteDataOptimizedApps_returnsEmptyListByDefault() {
    SatelliteManager satelliteManager = context.getSystemService(SatelliteManager.class);

    assertThat(satelliteManager.getSatelliteDataOptimizedApps()).isEmpty();
  }

  @Test
  public void getSatelliteDataOptimizedApps_whenSet_returnsApps() {
    SatelliteManager satelliteManager = context.getSystemService(SatelliteManager.class);
    ShadowSatelliteManager shadowSatelliteManager = Shadow.extract(satelliteManager);
    List<String> apps = new ArrayList<>();
    apps.add("com.google.android.apps.messaging");
    shadowSatelliteManager.setSatelliteDataOptimizedApps(apps);

    assertThat(satelliteManager.getSatelliteDataOptimizedApps()).isEqualTo(apps);
  }

  private TestOutcomeReceiver<List<SatelliteSubscriberProvisionStatus>>
      requestSatelliteSubscriberProvisionStatus() {
    TestOutcomeReceiver<List<SatelliteSubscriberProvisionStatus>> receiver =
        new TestOutcomeReceiver<>();
    SatelliteManager satelliteManager = context.getSystemService(SatelliteManager.class);
    satelliteManager.requestSatelliteSubscriberProvisionStatus(directExecutor(), receiver);
    return receiver;
  }

  private TestOutcomeReceiver<Boolean> requestIsCommunicationAllowedForCurrentLocation() {
    TestOutcomeReceiver<Boolean> receiver = new TestOutcomeReceiver<>();
    SatelliteManager satelliteManager = context.getSystemService(SatelliteManager.class);
    satelliteManager.requestIsCommunicationAllowedForCurrentLocation(directExecutor(), receiver);
    return receiver;
  }

  private ShadowSatelliteManager getShadowSatelliteManager() {
    return Shadow.extract(context.getSystemService(SatelliteManager.class));
  }
}
