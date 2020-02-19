package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;

import android.os.Build.VERSION_CODES;
import android.telephony.ims.ImsException;
import android.telephony.ims.ImsMmTelManager.CapabilityCallback;
import android.telephony.ims.ImsMmTelManager.RegistrationCallback;
import android.telephony.ims.ImsReasonInfo;
import android.telephony.ims.feature.MmTelFeature.MmTelCapabilities;
import android.telephony.ims.stub.ImsRegistrationImplBase;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

/** Tests for {@link ShadowImsMmTelManager} */
@RunWith(RobolectricTestRunner.class)
@Config(minSdk = VERSION_CODES.Q)
public class ShadowImsMmTelManagerTest {

  private ShadowImsMmTelManager shadowImsMmTelManager;

  @Before
  public void setup() {
    shadowImsMmTelManager = new ShadowImsMmTelManager();
  }

  @Test
  public void registerImsRegistrationCallback_imsRegistering_onRegisteringInvoked()
      throws ImsException {
    int[] imsTech = {ImsRegistrationImplBase.REGISTRATION_TECH_NONE}; // effectively final hack
    RegistrationCallback registrationCallback =
        new RegistrationCallback() {
          @Override
          public void onRegistering(int imsTransportType) {
            super.onRegistering(imsTransportType);
            imsTech[0] = imsTransportType;
          }
        };

    shadowImsMmTelManager.registerImsRegistrationCallback(Runnable::run, registrationCallback);
    shadowImsMmTelManager.setImsRegistering(ImsRegistrationImplBase.REGISTRATION_TECH_LTE);

    assertThat(imsTech[0]).isEqualTo(ImsRegistrationImplBase.REGISTRATION_TECH_LTE);

    shadowImsMmTelManager.unregisterImsRegistrationCallback(registrationCallback);
    shadowImsMmTelManager.setImsRegistering(ImsRegistrationImplBase.REGISTRATION_TECH_IWLAN);

    assertThat(imsTech[0]).isEqualTo(ImsRegistrationImplBase.REGISTRATION_TECH_LTE);
  }

  @Test
  public void registerImsRegistrationCallback_imsRegistered_onRegisteredInvoked()
      throws ImsException {
    int[] imsTech = {ImsRegistrationImplBase.REGISTRATION_TECH_NONE}; // effectively final hack
    RegistrationCallback registrationCallback =
        new RegistrationCallback() {
          @Override
          public void onRegistered(int imsTransportType) {
            super.onRegistered(imsTransportType);
            imsTech[0] = imsTransportType;
          }
        };

    shadowImsMmTelManager.registerImsRegistrationCallback(Runnable::run, registrationCallback);
    shadowImsMmTelManager.setImsRegistered(ImsRegistrationImplBase.REGISTRATION_TECH_IWLAN);

    assertThat(imsTech[0]).isEqualTo(ImsRegistrationImplBase.REGISTRATION_TECH_IWLAN);

    shadowImsMmTelManager.unregisterImsRegistrationCallback(registrationCallback);
    shadowImsMmTelManager.setImsRegistered(ImsRegistrationImplBase.REGISTRATION_TECH_LTE);

    assertThat(imsTech[0]).isEqualTo(ImsRegistrationImplBase.REGISTRATION_TECH_IWLAN);
  }

  @Test
  public void registerImsRegistrationCallback_imsUnregistered_onUnregisteredInvoked()
      throws ImsException {
    ImsReasonInfo[] imsReasonInfo = new ImsReasonInfo[1];
    RegistrationCallback registrationCallback =
        new RegistrationCallback() {
          @Override
          public void onUnregistered(ImsReasonInfo info) {
            super.onUnregistered(info);
            imsReasonInfo[0] = info;
          }
        };

    shadowImsMmTelManager.registerImsRegistrationCallback(Runnable::run, registrationCallback);
    ImsReasonInfo imsReasonInfoWithCallbackRegistered = new ImsReasonInfo();
    shadowImsMmTelManager.setImsUnregistered(imsReasonInfoWithCallbackRegistered);

    assertThat(imsReasonInfo[0]).isNotNull();
    assertThat(imsReasonInfo[0]).isEqualTo(imsReasonInfoWithCallbackRegistered);

    ImsReasonInfo imsReasonInfoAfterUnregisteringCallback = new ImsReasonInfo();
    shadowImsMmTelManager.unregisterImsRegistrationCallback(registrationCallback);
    shadowImsMmTelManager.setImsUnregistered(imsReasonInfoAfterUnregisteringCallback);

    assertThat(imsReasonInfoWithCallbackRegistered)
        .isNotEqualTo(imsReasonInfoAfterUnregisteringCallback);
    assertThat(imsReasonInfo[0]).isEqualTo(imsReasonInfoWithCallbackRegistered);
  }

  @Test
  public void registerImsRegistrationCallback_imsNotSupported_imsExceptionThrown() {
    shadowImsMmTelManager.setImsAvailableOnDevice(false);
    try {
      shadowImsMmTelManager.registerImsRegistrationCallback(
          Runnable::run, new RegistrationCallback());
      assertWithMessage("Expected ImsException was not thrown").fail();
    } catch (ImsException e) {
      assertThat(e.getCode()).isEqualTo(ImsException.CODE_ERROR_UNSUPPORTED_OPERATION);
      assertThat(e).hasMessageThat().contains("IMS not available on device.");
    }
  }

  @Test
  public void
      registerMmTelCapabilityCallback_imsRegistered_availabilityChange_onCapabilitiesStatusChangedInvoked()
          throws ImsException {
    MmTelCapabilities[] mmTelCapabilities = new MmTelCapabilities[1];
    CapabilityCallback capabilityCallback =
        new CapabilityCallback() {
          @Override
          public void onCapabilitiesStatusChanged(MmTelCapabilities capabilities) {
            super.onCapabilitiesStatusChanged(capabilities);
            mmTelCapabilities[0] = capabilities;
          }
        };

    shadowImsMmTelManager.setImsRegistered(ImsRegistrationImplBase.REGISTRATION_TECH_LTE);
    shadowImsMmTelManager.registerMmTelCapabilityCallback(Runnable::run, capabilityCallback);

    MmTelCapabilities mmTelCapabilitiesWithCallbackRegistered = new MmTelCapabilities();
    mmTelCapabilitiesWithCallbackRegistered.addCapabilities(
        MmTelCapabilities.CAPABILITY_TYPE_VIDEO);
    mmTelCapabilitiesWithCallbackRegistered.addCapabilities(
        MmTelCapabilities.CAPABILITY_TYPE_VOICE);
    shadowImsMmTelManager.setMmTelCapabilitiesAvailable(mmTelCapabilitiesWithCallbackRegistered);

    assertThat(mmTelCapabilities[0]).isNotNull();
    assertThat(mmTelCapabilities[0]).isEqualTo(mmTelCapabilitiesWithCallbackRegistered);

    shadowImsMmTelManager.unregisterMmTelCapabilityCallback(capabilityCallback);
    shadowImsMmTelManager.setMmTelCapabilitiesAvailable(new MmTelCapabilities());
    assertThat(mmTelCapabilities[0]).isEqualTo(mmTelCapabilitiesWithCallbackRegistered);
  }

  @Test
  public void
      registerMmTelCapabilityCallback_imsNotRegistered_availabilityChange_onCapabilitiesStatusChangedNotInvoked()
          throws ImsException {
    MmTelCapabilities[] mmTelCapabilities = new MmTelCapabilities[1];
    CapabilityCallback capabilityCallback =
        new CapabilityCallback() {
          @Override
          public void onCapabilitiesStatusChanged(MmTelCapabilities capabilities) {
            super.onCapabilitiesStatusChanged(capabilities);
            mmTelCapabilities[0] = capabilities;
          }
        };

    shadowImsMmTelManager.registerMmTelCapabilityCallback(Runnable::run, capabilityCallback);
    shadowImsMmTelManager.setMmTelCapabilitiesAvailable(new MmTelCapabilities());

    assertThat(mmTelCapabilities[0]).isNull();
  }

  @Test
  public void registerMmTelCapabilityCallback_imsNotSupported_imsExceptionThrown() {
    shadowImsMmTelManager.setImsAvailableOnDevice(false);
    try {
      shadowImsMmTelManager.registerMmTelCapabilityCallback(
          Runnable::run, new CapabilityCallback());
      assertWithMessage("Expected ImsException was not thrown").fail();
    } catch (ImsException e) {
      assertThat(e.getCode()).isEqualTo(ImsException.CODE_ERROR_UNSUPPORTED_OPERATION);
      assertThat(e).hasMessageThat().contains("IMS not available on device.");
    }
  }
}
