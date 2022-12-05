package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import android.annotation.SuppressLint;
import android.os.Build.VERSION_CODES;
import android.telephony.ims.ImsException;
import android.telephony.ims.ImsMmTelManager;
import android.telephony.ims.ImsMmTelManager.CapabilityCallback;
import android.telephony.ims.ImsReasonInfo;
import android.telephony.ims.ImsRegistrationAttributes;
import android.telephony.ims.RegistrationManager;
import android.telephony.ims.feature.MmTelFeature.MmTelCapabilities;
import android.telephony.ims.stub.ImsRegistrationImplBase;
import android.util.ArraySet;
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
  public void registerImsRegistrationManagerCallback_imsRegistering_onRegisteringInvoked()
      throws ImsException {
    RegistrationManager.RegistrationCallback registrationCallback =
        mock(RegistrationManager.RegistrationCallback.class);

    shadowImsMmTelManager.registerImsRegistrationCallback(Runnable::run, registrationCallback);
    shadowImsMmTelManager.setImsRegistering(ImsRegistrationImplBase.REGISTRATION_TECH_LTE);

    verify(registrationCallback).onRegistering(ImsRegistrationImplBase.REGISTRATION_TECH_LTE);

    shadowImsMmTelManager.unregisterImsRegistrationCallback(registrationCallback);
    shadowImsMmTelManager.setImsRegistering(ImsRegistrationImplBase.REGISTRATION_TECH_LTE);

    verifyNoMoreInteractions(registrationCallback);
  }

  @Test
  @Config(sdk = {VERSION_CODES.S, Config.NEWEST_SDK})
  public void registerImsRegistrationManagerCallbackImsAttrs_imsRegistering_onRegisteringInvoked()
      throws ImsException {
    RegistrationManager.RegistrationCallback registrationCallback =
        mock(RegistrationManager.RegistrationCallback.class);

    int imsRegistrationTech = ImsRegistrationImplBase.REGISTRATION_TECH_IWLAN;
    int imsTransportType = RegistrationManager.getAccessType(imsRegistrationTech);
    int imsAttributeFlags = 0;
    ArraySet<String> featureTags = new ArraySet<>();

    ImsRegistrationAttributes imsRegistrationAttrs =
        new ImsRegistrationAttributes(
            imsRegistrationTech, imsTransportType, imsAttributeFlags, featureTags);

    shadowImsMmTelManager.registerImsRegistrationCallback(Runnable::run, registrationCallback);
    shadowImsMmTelManager.setImsRegistering(imsRegistrationAttrs);

    verify(registrationCallback).onRegistering(imsRegistrationAttrs);

    shadowImsMmTelManager.unregisterImsRegistrationCallback(registrationCallback);
    shadowImsMmTelManager.setImsRegistering(imsRegistrationAttrs);

    verifyNoMoreInteractions(registrationCallback);
  }

  @Test
  public void registerImsRegistrationManagerCallback_imsRegistered_onRegisteredInvoked()
      throws ImsException {
    RegistrationManager.RegistrationCallback registrationCallback =
        mock(RegistrationManager.RegistrationCallback.class);
    shadowImsMmTelManager.registerImsRegistrationCallback(Runnable::run, registrationCallback);
    shadowImsMmTelManager.setImsRegistered(ImsRegistrationImplBase.REGISTRATION_TECH_IWLAN);

    verify(registrationCallback).onRegistered(ImsRegistrationImplBase.REGISTRATION_TECH_IWLAN);

    shadowImsMmTelManager.unregisterImsRegistrationCallback(registrationCallback);
    shadowImsMmTelManager.setImsRegistered(ImsRegistrationImplBase.REGISTRATION_TECH_LTE);

    verifyNoMoreInteractions(registrationCallback);
  }

  @Test
  @Config(sdk = {VERSION_CODES.S, Config.NEWEST_SDK})
  public void registerImsRegistrationManagerCallbackImsAttrs_imsRegistered_onRegisteredInvoked()
      throws ImsException {
    RegistrationManager.RegistrationCallback registrationCallback =
        mock(RegistrationManager.RegistrationCallback.class);

    int imsRegistrationTech = ImsRegistrationImplBase.REGISTRATION_TECH_IWLAN;
    int imsTransportType = RegistrationManager.getAccessType(imsRegistrationTech);
    int imsAttributeFlags = 0;
    ArraySet<String> featureTags = new ArraySet<>();

    ImsRegistrationAttributes imsRegistrationAttrs =
        new ImsRegistrationAttributes(
            imsRegistrationTech, imsTransportType, imsAttributeFlags, featureTags);

    shadowImsMmTelManager.registerImsRegistrationCallback(Runnable::run, registrationCallback);
    shadowImsMmTelManager.setImsRegistered(imsRegistrationAttrs);

    verify(registrationCallback).onRegistered(imsRegistrationAttrs);

    shadowImsMmTelManager.unregisterImsRegistrationCallback(registrationCallback);
    shadowImsMmTelManager.setImsRegistered(imsRegistrationAttrs);

    verifyNoMoreInteractions(registrationCallback);
  }

  @Test
  public void registerImsRegistrationManagerCallback_imsDeregistered_onDeregisteredInvoked()
      throws ImsException {
    RegistrationManager.RegistrationCallback registrationCallback =
        mock(RegistrationManager.RegistrationCallback.class);
    shadowImsMmTelManager.registerImsRegistrationCallback(Runnable::run, registrationCallback);
    ImsReasonInfo imsReasonInfoWithCallbackRegistered = new ImsReasonInfo();
    shadowImsMmTelManager.setImsUnregistered(imsReasonInfoWithCallbackRegistered);

    verify(registrationCallback).onUnregistered(imsReasonInfoWithCallbackRegistered);

    ImsReasonInfo imsReasonInfoAfterUnregisteringCallback = new ImsReasonInfo();
    shadowImsMmTelManager.unregisterImsRegistrationCallback(registrationCallback);
    shadowImsMmTelManager.setImsUnregistered(imsReasonInfoAfterUnregisteringCallback);

    verifyNoMoreInteractions(registrationCallback);
  }

  @Test
  public void
      registerImsRegistrationManagerCallback_imsTechnologyChangeFailed_onTechnologyChangeFailedInvoked()
          throws ImsException {
    RegistrationManager.RegistrationCallback registrationCallback =
        mock(RegistrationManager.RegistrationCallback.class);
    shadowImsMmTelManager.registerImsRegistrationCallback(Runnable::run, registrationCallback);
    ImsReasonInfo imsReasonInfoWithCallbackRegistered = new ImsReasonInfo();
    shadowImsMmTelManager.setOnTechnologyChangeFailed(
        ImsRegistrationImplBase.REGISTRATION_TECH_IWLAN, imsReasonInfoWithCallbackRegistered);

    verify(registrationCallback)
        .onTechnologyChangeFailed(
            ImsRegistrationImplBase.REGISTRATION_TECH_IWLAN, imsReasonInfoWithCallbackRegistered);

    ImsReasonInfo imsReasonInfoAfterUnregisteringCallback = new ImsReasonInfo();
    shadowImsMmTelManager.unregisterImsRegistrationCallback(registrationCallback);
    shadowImsMmTelManager.setOnTechnologyChangeFailed(
        ImsRegistrationImplBase.REGISTRATION_TECH_IWLAN, imsReasonInfoAfterUnregisteringCallback);

    verifyNoMoreInteractions(registrationCallback);
  }

  @Test
  public void
      registerImsMmTelManagerRegistrationManagerCallback_imsNotSupported_imsExceptionThrown() {
    shadowImsMmTelManager.setImsAvailableOnDevice(false);
    try {
      shadowImsMmTelManager.registerImsRegistrationCallback(
          Runnable::run, mock(RegistrationManager.RegistrationCallback.class));
      assertWithMessage("Expected ImsException was not thrown").fail();
    } catch (ImsException e) {
      assertThat(e.getCode()).isEqualTo(ImsException.CODE_ERROR_UNSUPPORTED_OPERATION);
      assertThat(e).hasMessageThat().contains("IMS not available on device.");
    }
  }

  @Test
  public void registerImsMmTelManagerRegistrationCallback_imsRegistering_onRegisteringInvoked()
      throws ImsException {
    ImsMmTelManager.RegistrationCallback registrationCallback =
        mock(ImsMmTelManager.RegistrationCallback.class);
    shadowImsMmTelManager.registerImsRegistrationCallback(Runnable::run, registrationCallback);
    shadowImsMmTelManager.setImsRegistering(ImsRegistrationImplBase.REGISTRATION_TECH_LTE);

    verify(registrationCallback).onRegistering(ImsRegistrationImplBase.REGISTRATION_TECH_LTE);

    shadowImsMmTelManager.unregisterImsRegistrationCallback(registrationCallback);
    shadowImsMmTelManager.setImsRegistering(ImsRegistrationImplBase.REGISTRATION_TECH_IWLAN);

    verifyNoMoreInteractions(registrationCallback);
  }

  @Test
  public void registerImsMmTelManagerRegistrationCallback_imsRegistered_onRegisteredInvoked()
      throws ImsException {
    ImsMmTelManager.RegistrationCallback registrationCallback =
        mock(ImsMmTelManager.RegistrationCallback.class);
    shadowImsMmTelManager.registerImsRegistrationCallback(Runnable::run, registrationCallback);
    shadowImsMmTelManager.setImsRegistered(ImsRegistrationImplBase.REGISTRATION_TECH_IWLAN);

    verify(registrationCallback).onRegistered(ImsRegistrationImplBase.REGISTRATION_TECH_IWLAN);

    shadowImsMmTelManager.unregisterImsRegistrationCallback(registrationCallback);
    shadowImsMmTelManager.setImsRegistered(ImsRegistrationImplBase.REGISTRATION_TECH_LTE);

    verifyNoMoreInteractions(registrationCallback);
  }

  @Test
  public void registerImsMmTelManagerRegistrationCallback_imsUnregistered_onUnregisteredInvoked()
      throws ImsException {
    ImsMmTelManager.RegistrationCallback registrationCallback =
        mock(ImsMmTelManager.RegistrationCallback.class);
    shadowImsMmTelManager.registerImsRegistrationCallback(Runnable::run, registrationCallback);
    ImsReasonInfo imsReasonInfoWithCallbackRegistered = new ImsReasonInfo();
    shadowImsMmTelManager.setImsUnregistered(imsReasonInfoWithCallbackRegistered);

    verify(registrationCallback).onUnregistered(imsReasonInfoWithCallbackRegistered);

    ImsReasonInfo imsReasonInfoAfterUnregisteringCallback = new ImsReasonInfo();
    shadowImsMmTelManager.unregisterImsRegistrationCallback(registrationCallback);
    shadowImsMmTelManager.setImsUnregistered(imsReasonInfoAfterUnregisteringCallback);

    verifyNoMoreInteractions(registrationCallback);
  }

  @Test
  public void registerImsMmTelManagerRegistrationCallback_imsNotSupported_imsExceptionThrown() {
    shadowImsMmTelManager.setImsAvailableOnDevice(false);
    try {
      shadowImsMmTelManager.registerImsRegistrationCallback(
          Runnable::run, mock(ImsMmTelManager.RegistrationCallback.class));
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

  @Test
  public void isAvailable_mmTelCapabilitiesNeverSet_noneAvailable() {
    assertThat(
            shadowImsMmTelManager.isAvailable(
                MmTelCapabilities.CAPABILITY_TYPE_SMS,
                ImsRegistrationImplBase.REGISTRATION_TECH_IWLAN))
        .isFalse();
    assertThat(
            shadowImsMmTelManager.isAvailable(
                MmTelCapabilities.CAPABILITY_TYPE_SMS,
                ImsRegistrationImplBase.REGISTRATION_TECH_LTE))
        .isFalse();
    assertThat(
            shadowImsMmTelManager.isAvailable(
                MmTelCapabilities.CAPABILITY_TYPE_VIDEO,
                ImsRegistrationImplBase.REGISTRATION_TECH_IWLAN))
        .isFalse();
    assertThat(
            shadowImsMmTelManager.isAvailable(
                MmTelCapabilities.CAPABILITY_TYPE_VIDEO,
                ImsRegistrationImplBase.REGISTRATION_TECH_LTE))
        .isFalse();
    assertThat(
            shadowImsMmTelManager.isAvailable(
                MmTelCapabilities.CAPABILITY_TYPE_UT,
                ImsRegistrationImplBase.REGISTRATION_TECH_IWLAN))
        .isFalse();
    assertThat(
            shadowImsMmTelManager.isAvailable(
                MmTelCapabilities.CAPABILITY_TYPE_UT,
                ImsRegistrationImplBase.REGISTRATION_TECH_LTE))
        .isFalse();
    assertThat(
            shadowImsMmTelManager.isAvailable(
                MmTelCapabilities.CAPABILITY_TYPE_VOICE,
                ImsRegistrationImplBase.REGISTRATION_TECH_IWLAN))
        .isFalse();
    assertThat(
            shadowImsMmTelManager.isAvailable(
                MmTelCapabilities.CAPABILITY_TYPE_VOICE,
                ImsRegistrationImplBase.REGISTRATION_TECH_LTE))
        .isFalse();
  }

  @Test
  public void
      isAvailable_imsRegisteredWifi_voiceAndVideoMmTelCapabilitiesSet_voiceAndVideoOverWifiAvailable() {
    shadowImsMmTelManager.setImsRegistered(ImsRegistrationImplBase.REGISTRATION_TECH_IWLAN);

    MmTelCapabilities voiceAndVideoMmTelCapabilities = new MmTelCapabilities();
    voiceAndVideoMmTelCapabilities.addCapabilities(MmTelCapabilities.CAPABILITY_TYPE_VOICE);
    voiceAndVideoMmTelCapabilities.addCapabilities(MmTelCapabilities.CAPABILITY_TYPE_VIDEO);

    shadowImsMmTelManager.setMmTelCapabilitiesAvailable(voiceAndVideoMmTelCapabilities);

    assertThat(
            shadowImsMmTelManager.isAvailable(
                MmTelCapabilities.CAPABILITY_TYPE_VIDEO,
                ImsRegistrationImplBase.REGISTRATION_TECH_IWLAN))
        .isTrue();
    assertThat(
            shadowImsMmTelManager.isAvailable(
                MmTelCapabilities.CAPABILITY_TYPE_VOICE,
                ImsRegistrationImplBase.REGISTRATION_TECH_IWLAN))
        .isTrue();

    assertThat(
            shadowImsMmTelManager.isAvailable(
                MmTelCapabilities.CAPABILITY_TYPE_SMS,
                ImsRegistrationImplBase.REGISTRATION_TECH_IWLAN))
        .isFalse();
    assertThat(
            shadowImsMmTelManager.isAvailable(
                MmTelCapabilities.CAPABILITY_TYPE_SMS,
                ImsRegistrationImplBase.REGISTRATION_TECH_LTE))
        .isFalse();
    assertThat(
            shadowImsMmTelManager.isAvailable(
                MmTelCapabilities.CAPABILITY_TYPE_VIDEO,
                ImsRegistrationImplBase.REGISTRATION_TECH_LTE))
        .isFalse();
    assertThat(
            shadowImsMmTelManager.isAvailable(
                MmTelCapabilities.CAPABILITY_TYPE_UT,
                ImsRegistrationImplBase.REGISTRATION_TECH_IWLAN))
        .isFalse();
    assertThat(
            shadowImsMmTelManager.isAvailable(
                MmTelCapabilities.CAPABILITY_TYPE_UT,
                ImsRegistrationImplBase.REGISTRATION_TECH_LTE))
        .isFalse();
    assertThat(
            shadowImsMmTelManager.isAvailable(
                MmTelCapabilities.CAPABILITY_TYPE_VOICE,
                ImsRegistrationImplBase.REGISTRATION_TECH_LTE))
        .isFalse();
  }

  @Test
  public void isAvailable_imsNotRegistered_voiceAndVideoMmTelCapabilitiesSet_noneAvailable() {
    MmTelCapabilities voiceAndVideoMmTelCapabilities = new MmTelCapabilities();
    voiceAndVideoMmTelCapabilities.addCapabilities(MmTelCapabilities.CAPABILITY_TYPE_VOICE);
    voiceAndVideoMmTelCapabilities.addCapabilities(MmTelCapabilities.CAPABILITY_TYPE_VIDEO);

    shadowImsMmTelManager.setMmTelCapabilitiesAvailable(voiceAndVideoMmTelCapabilities);

    assertThat(
            shadowImsMmTelManager.isAvailable(
                MmTelCapabilities.CAPABILITY_TYPE_SMS,
                ImsRegistrationImplBase.REGISTRATION_TECH_IWLAN))
        .isFalse();
    assertThat(
            shadowImsMmTelManager.isAvailable(
                MmTelCapabilities.CAPABILITY_TYPE_SMS,
                ImsRegistrationImplBase.REGISTRATION_TECH_LTE))
        .isFalse();
    assertThat(
            shadowImsMmTelManager.isAvailable(
                MmTelCapabilities.CAPABILITY_TYPE_VIDEO,
                ImsRegistrationImplBase.REGISTRATION_TECH_IWLAN))
        .isFalse();
    assertThat(
            shadowImsMmTelManager.isAvailable(
                MmTelCapabilities.CAPABILITY_TYPE_VIDEO,
                ImsRegistrationImplBase.REGISTRATION_TECH_LTE))
        .isFalse();
    assertThat(
            shadowImsMmTelManager.isAvailable(
                MmTelCapabilities.CAPABILITY_TYPE_UT,
                ImsRegistrationImplBase.REGISTRATION_TECH_IWLAN))
        .isFalse();
    assertThat(
            shadowImsMmTelManager.isAvailable(
                MmTelCapabilities.CAPABILITY_TYPE_UT,
                ImsRegistrationImplBase.REGISTRATION_TECH_LTE))
        .isFalse();
    assertThat(
            shadowImsMmTelManager.isAvailable(
                MmTelCapabilities.CAPABILITY_TYPE_VOICE,
                ImsRegistrationImplBase.REGISTRATION_TECH_IWLAN))
        .isFalse();
    assertThat(
            shadowImsMmTelManager.isAvailable(
                MmTelCapabilities.CAPABILITY_TYPE_VOICE,
                ImsRegistrationImplBase.REGISTRATION_TECH_LTE))
        .isFalse();
  }

  @Test
  @SuppressLint("NewApi")
  public void createForSubscriptionId_invalidSubscriptionId_throwsIllegalArgumentException() {
    try {
      ShadowImsMmTelManager.createForSubscriptionId(-5);
      assertWithMessage("Expected IllegalArgumentException was not thrown").fail();
    } catch (IllegalArgumentException e) {
      assertThat(e).hasMessageThat().contains("Invalid subscription ID");
    }
  }

  @Test
  @SuppressLint("NewApi")
  public void createForSubscriptionId_multipleValidSubscriptionIds_sharesInstances() {
    ImsMmTelManager imsMmTelManager1 = ShadowImsMmTelManager.createForSubscriptionId(1);
    ImsMmTelManager imsMmTelManager2 = ShadowImsMmTelManager.createForSubscriptionId(2);

    assertThat(imsMmTelManager1).isNotEqualTo(imsMmTelManager2);
    assertThat(imsMmTelManager1).isEqualTo(ShadowImsMmTelManager.createForSubscriptionId(1));
    assertThat(imsMmTelManager2).isEqualTo(ShadowImsMmTelManager.createForSubscriptionId(2));

    ShadowImsMmTelManager.clearExistingInstances();

    assertThat(imsMmTelManager1).isNotEqualTo(ShadowImsMmTelManager.createForSubscriptionId(1));
    assertThat(imsMmTelManager2).isNotEqualTo(ShadowImsMmTelManager.createForSubscriptionId(2));
  }

  @Test
  public void getSubscriptionId() {
    shadowImsMmTelManager.__constructor__(5);
    assertThat(shadowImsMmTelManager.getSubscriptionId()).isEqualTo(5);
  }
}
