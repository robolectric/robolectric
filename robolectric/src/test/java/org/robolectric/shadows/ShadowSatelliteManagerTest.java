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
import android.telephony.satellite.SatelliteSubscriberInfo;
import android.telephony.satellite.SatelliteSubscriberProvisionStatus;
import androidx.test.core.app.ApplicationProvider;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.Test;
import org.junit.Assume;
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

  @Test
  public void registerForSupportedStateChanged_callbackIsTriggered() {
    Assume.assumeTrue(
      "requires SatelliteManager.registerForSupportedStateChanged",
      hasMethod(
        "android.telephony.satellite.SatelliteManager",
        "registerForSupportedStateChanged",
        java.util.concurrent.Executor.class,
        java.util.function.Consumer.class));
    SatelliteManager satelliteManager = context.getSystemService(SatelliteManager.class);
    AtomicReference<Boolean> isSupported = new AtomicReference<>();
    java.util.function.Consumer<Boolean> callback = isSupported::set;

    int result = satelliteManager.registerForSupportedStateChanged(directExecutor(), callback);
    assertThat(result).isEqualTo(SATELLITE_RESULT_SUCCESS);

    getShadowSatelliteManager().triggerOnSupportedStateChanged(true);
    assertThat(isSupported.get()).isTrue();
    getShadowSatelliteManager().triggerOnSupportedStateChanged(false);
    assertThat(isSupported.get()).isFalse();
  }

  @Test
  public void unregisterForSupportedStateChanged_unregistersCallback() {
    Assume.assumeTrue(
      "requires SatelliteManager.registerForSupportedStateChanged",
      hasMethod(
        "android.telephony.satellite.SatelliteManager",
        "registerForSupportedStateChanged",
        java.util.concurrent.Executor.class,
        java.util.function.Consumer.class));
    SatelliteManager satelliteManager = context.getSystemService(SatelliteManager.class);
    AtomicBoolean wasCalled = new AtomicBoolean(false);
    java.util.function.Consumer<Boolean> callback = supported -> wasCalled.set(true);
    satelliteManager.registerForSupportedStateChanged(directExecutor(), callback);

    satelliteManager.unregisterForSupportedStateChanged(callback);
    getShadowSatelliteManager().triggerOnSupportedStateChanged(true);

    assertThat(wasCalled.get()).isFalse();
  }

  @Test
  public void triggerOnSupportedStateChanged_triggersAllCallbacks() {
    Assume.assumeTrue(
      "requires SatelliteManager.registerForSupportedStateChanged",
      hasMethod(
        "android.telephony.satellite.SatelliteManager",
        "registerForSupportedStateChanged",
        java.util.concurrent.Executor.class,
        java.util.function.Consumer.class));
    SatelliteManager satelliteManager = context.getSystemService(SatelliteManager.class);
    AtomicReference<Boolean> callback1Result = new AtomicReference<>();
    AtomicReference<Boolean> callback2Result = new AtomicReference<>();
    java.util.function.Consumer<Boolean> callback1 = callback1Result::set;
    java.util.function.Consumer<Boolean> callback2 = callback2Result::set;
    satelliteManager.registerForSupportedStateChanged(directExecutor(), callback1);
    satelliteManager.registerForSupportedStateChanged(directExecutor(), callback2);

    getShadowSatelliteManager().triggerOnSupportedStateChanged(true);

    assertThat(callback1Result.get()).isTrue();
    assertThat(callback2Result.get()).isTrue();
  }

  @Test
  public void requestSatelliteSubscriberProvisionStatus_returnsNullByDefault() throws Exception {
    Assume.assumeTrue(
        "requires android.telephony.satellite.SatelliteSubscriberProvisionStatus",
        isClassPresent("android.telephony.satellite.SatelliteSubscriberProvisionStatus"));

    assertThat(requestSatelliteSubscriberProvisionStatus()).isNull();
  }

  @Test
  public void requestSatelliteSubscriberProvisionStatus_whenSet_returnsStatus() throws Exception {
    Assume.assumeTrue(
        "requires android.telephony.satellite.SatelliteSubscriberProvisionStatus",
        isClassPresent("android.telephony.satellite.SatelliteSubscriberProvisionStatus"));
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

    assertThat(requestSatelliteSubscriberProvisionStatus()).isEqualTo(provisionStatus);
  }

  @Test
  public void requestSatelliteSubscriberProvisionStatus_whenSetToError_throws() {
    Assume.assumeTrue(
      "requires android.telephony.satellite.SatelliteSubscriberProvisionStatus",
      isClassPresent("android.telephony.satellite.SatelliteSubscriberProvisionStatus"));

    getShadowSatelliteManager()
      .setSatelliteSubscriberProvisionStatus(null, new SatelliteException(123));

    assertThrows(Exception.class, this::requestSatelliteSubscriberProvisionStatus);
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
    Assume.assumeTrue(
        "requires SatelliteManager.getSatelliteDataOptimizedApps",
        hasMethod(
            "android.telephony.satellite.SatelliteManager", "getSatelliteDataOptimizedApps"));
    SatelliteManager satelliteManager = context.getSystemService(SatelliteManager.class);

    assertThat(satelliteManager.getSatelliteDataOptimizedApps()).isEmpty();
  }

  @Test
  public void getSatelliteDataOptimizedApps_whenSet_returnsApps() {
    Assume.assumeTrue(
        "requires SatelliteManager.getSatelliteDataOptimizedApps",
        hasMethod(
            "android.telephony.satellite.SatelliteManager", "getSatelliteDataOptimizedApps"));

    SatelliteManager satelliteManager = context.getSystemService(SatelliteManager.class);
    ShadowSatelliteManager shadowSatelliteManager = Shadow.extract(satelliteManager);
    List<String> apps = new ArrayList<>();
    apps.add("com.google.android.apps.messaging");
    shadowSatelliteManager.setSatelliteDataOptimizedApps(apps);

    assertThat(satelliteManager.getSatelliteDataOptimizedApps()).isEqualTo(apps);
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

  private List<SatelliteSubscriberProvisionStatus> requestSatelliteSubscriberProvisionStatus()
      throws Exception {
    AtomicReference<List<SatelliteSubscriberProvisionStatus>> status = new AtomicReference<>();
    AtomicReference<Exception> error = new AtomicReference<>();
    OutcomeReceiver<List<SatelliteSubscriberProvisionStatus>, SatelliteException> callback =
        new OutcomeReceiver<List<SatelliteSubscriberProvisionStatus>, SatelliteException>() {
          @Override
          public void onResult(List<SatelliteSubscriberProvisionStatus> result) {
            status.set(result);
          }

          @Override
          public void onError(SatelliteException e) {
            error.set(e);
          }
        };

    getShadowSatelliteManager()
        .requestSatelliteSubscriberProvisionStatus(directExecutor(), callback);
    if (error.get() != null) {
      throw error.get();
    }
    return status.get();
  }

  private static boolean isClassPresent(String className) {
    try {
      Class.forName(className, false, ShadowSatelliteManagerTest.class.getClassLoader());
      return true;
    } catch (Throwable t) {
      return false;
    }
  }

  private static boolean hasMethod(String className, String methodName, Class<?>... params) {
    try {
      Class<?> c = Class.forName(className);
      c.getMethod(methodName, params);
      return true;
    } catch (Throwable t) {
      return false;
    }
  }

  private ShadowSatelliteManager getShadowSatelliteManager() {
    return Shadow.extract(context.getSystemService(SatelliteManager.class));
  }
}
