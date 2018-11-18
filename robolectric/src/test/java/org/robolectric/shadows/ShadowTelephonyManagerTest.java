package org.robolectric.shadows;

import static android.content.Context.TELEPHONY_SERVICE;
import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR1;
import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR2;
import static android.os.Build.VERSION_CODES.M;
import static android.os.Build.VERSION_CODES.N;
import static android.os.Build.VERSION_CODES.O;
import static android.os.Build.VERSION_CODES.P;
import static android.telephony.PhoneStateListener.LISTEN_CALL_STATE;
import static android.telephony.PhoneStateListener.LISTEN_CELL_INFO;
import static android.telephony.PhoneStateListener.LISTEN_CELL_LOCATION;
import static android.telephony.TelephonyManager.CALL_STATE_IDLE;
import static android.telephony.TelephonyManager.CALL_STATE_OFFHOOK;
import static android.telephony.TelephonyManager.CALL_STATE_RINGING;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.robolectric.RuntimeEnvironment.application;
import static org.robolectric.Shadows.shadowOf;

import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.PersistableBundle;
import android.telecom.PhoneAccountHandle;
import android.telephony.CellInfo;
import android.telephony.CellLocation;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.Collections;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

@RunWith(AndroidJUnit4.class)
public class ShadowTelephonyManagerTest {

  private TelephonyManager telephonyManager;
  private ShadowTelephonyManager shadowTelephonyManager;

  @Before
  public void setUp() throws Exception {
    telephonyManager = (TelephonyManager) application.getSystemService(TELEPHONY_SERVICE);
    shadowTelephonyManager = shadowOf(telephonyManager);
  }

  @Test
  public void testListenInit() {
    PhoneStateListener listener = mock(PhoneStateListener.class);
    telephonyManager.listen(listener, LISTEN_CALL_STATE | LISTEN_CELL_INFO | LISTEN_CELL_LOCATION);

    verify(listener).onCallStateChanged(CALL_STATE_IDLE, null);
    verify(listener).onCellLocationChanged(null);
    if (VERSION.SDK_INT >= JELLY_BEAN_MR1) {
      verify(listener).onCellInfoChanged(Collections.emptyList());
    }
  }

  @Test
  public void shouldGiveDeviceId() {
    String testId = "TESTING123";
    shadowTelephonyManager.setDeviceId(testId);
    assertEquals(testId, telephonyManager.getDeviceId());
  }

  @Test
  @Config(minSdk = M)
  public void shouldGiveDeviceIdForSlot() {
    shadowTelephonyManager.setDeviceId(1, "device in slot 1");
    shadowTelephonyManager.setDeviceId(2, "device in slot 2");

    assertEquals("device in slot 1", telephonyManager.getDeviceId(1));
    assertEquals("device in slot 2", telephonyManager.getDeviceId(2));
  }

  @Test
  @Config(minSdk = O)
  public void getImei() {
    String testImei = "4test imei";
    shadowTelephonyManager.setImei(testImei);
    assertEquals(testImei, telephonyManager.getImei());
  }

  @Test
  @Config(minSdk = O)
  public void getMeid() {
    String testMeid = "4test meid";
    shadowTelephonyManager.setMeid(testMeid);
    assertEquals(testMeid, telephonyManager.getMeid());
  }

  @Test
  public void shouldGiveNetworkOperatorName() {
    shadowTelephonyManager.setNetworkOperatorName("SomeOperatorName");
    assertEquals("SomeOperatorName", telephonyManager.getNetworkOperatorName());
  }

  @Test
  public void shouldGiveSimOperatorName() {
    shadowTelephonyManager.setSimOperatorName("SomeSimOperatorName");
    assertEquals("SomeSimOperatorName", telephonyManager.getSimOperatorName());
  }

  @Test(expected = SecurityException.class)
  public void getSimSerialNumber_shouldThrowSecurityExceptionWhenReadPhoneStatePermissionNotGranted()
      throws Exception {
    shadowTelephonyManager.setReadPhoneStatePermission(false);
    telephonyManager.getSimSerialNumber();
  }

  @Test
  public void shouldGetSimSerialNumber() {
    shadowTelephonyManager.setSimSerialNumber("SomeSerialNumber");
    assertEquals("SomeSerialNumber", telephonyManager.getSimSerialNumber());
  }

  @Test
  public void shouldGiveNetworkType() {
    shadowTelephonyManager.setNetworkType(TelephonyManager.NETWORK_TYPE_CDMA);
    assertEquals(TelephonyManager.NETWORK_TYPE_CDMA, telephonyManager.getNetworkType());
  }

  @Test
  @Config(minSdk = N)
  public void shouldGiveVoiceNetworkType() {
    shadowTelephonyManager.setVoiceNetworkType(TelephonyManager.NETWORK_TYPE_CDMA);
    assertThat(telephonyManager.getVoiceNetworkType())
        .isEqualTo(TelephonyManager.NETWORK_TYPE_CDMA);
  }

  @Test
  @Config(minSdk = JELLY_BEAN_MR1)
  public void shouldGiveAllCellInfo() {
    PhoneStateListener listener = mock(PhoneStateListener.class);
    telephonyManager.listen(listener, LISTEN_CELL_INFO);

    List<CellInfo> allCellInfo = Collections.singletonList(mock(CellInfo.class));
    shadowTelephonyManager.setAllCellInfo(allCellInfo);
    assertEquals(allCellInfo, telephonyManager.getAllCellInfo());
    verify(listener).onCellInfoChanged(allCellInfo);
  }

  @Test
  public void shouldGiveNetworkCountryIso() {
    shadowTelephonyManager.setNetworkCountryIso("SomeIso");
    assertEquals("SomeIso", telephonyManager.getNetworkCountryIso());
  }

  @Test
  public void shouldGiveNetworkOperator() {
    shadowTelephonyManager.setNetworkOperator("SomeOperator");
    assertEquals("SomeOperator", telephonyManager.getNetworkOperator());
  }

  @Test
  public void shouldGiveLine1Number() {
    shadowTelephonyManager.setLine1Number("123-244-2222");
    assertEquals("123-244-2222", telephonyManager.getLine1Number());
  }

  @Test
  @Config(minSdk = JELLY_BEAN_MR2)
  public void shouldGiveGroupIdLevel1() {
    shadowTelephonyManager.setGroupIdLevel1("SomeGroupId");
    assertEquals("SomeGroupId", telephonyManager.getGroupIdLevel1());
  }

  @Test(expected = SecurityException.class)
  public void getDeviceId_shouldThrowSecurityExceptionWhenReadPhoneStatePermissionNotGranted()
      throws Exception {
    shadowTelephonyManager.setReadPhoneStatePermission(false);
    telephonyManager.getDeviceId();
  }

  @Test
  public void shouldGivePhoneType() {
    shadowTelephonyManager.setPhoneType(TelephonyManager.PHONE_TYPE_CDMA);
    assertEquals(TelephonyManager.PHONE_TYPE_CDMA, telephonyManager.getPhoneType());
    shadowTelephonyManager.setPhoneType(TelephonyManager.PHONE_TYPE_GSM);
    assertEquals(TelephonyManager.PHONE_TYPE_GSM, telephonyManager.getPhoneType());
  }

  @Test
  public void shouldGiveCellLocation() {
    PhoneStateListener listener = mock(PhoneStateListener.class);
    telephonyManager.listen(listener, LISTEN_CELL_LOCATION);

    CellLocation mockCellLocation = mock(CellLocation.class);
    shadowOf(telephonyManager).setCellLocation(mockCellLocation);
    assertEquals(mockCellLocation, telephonyManager.getCellLocation());
    verify(listener).onCellLocationChanged(mockCellLocation);
  }

  @Test
  public void shouldGiveCallState() {
    PhoneStateListener listener = mock(PhoneStateListener.class);
    telephonyManager.listen(listener, LISTEN_CALL_STATE);

    shadowOf(telephonyManager).setCallState(CALL_STATE_RINGING, "911");
    assertEquals(CALL_STATE_RINGING, telephonyManager.getCallState());
    verify(listener).onCallStateChanged(CALL_STATE_RINGING, "911");

    shadowOf(telephonyManager).setCallState(CALL_STATE_OFFHOOK, "911");
    assertEquals(CALL_STATE_OFFHOOK, telephonyManager.getCallState());
    verify(listener).onCallStateChanged(CALL_STATE_OFFHOOK, null);
  }

  @Test
  public void isSmsCapable() {
    assertThat(telephonyManager.isSmsCapable()).isTrue();
    shadowTelephonyManager.setIsSmsCapable(false);
    assertThat(telephonyManager.isSmsCapable()).isFalse();
  }

  @Test
  @Config(minSdk = O)
  public void shouldGiveCarrierConfigIfSet() {
    PersistableBundle bundle = new PersistableBundle();
    bundle.putInt("foo", 42);
    shadowTelephonyManager.setCarrierConfig(bundle);

    assertEquals(bundle, telephonyManager.getCarrierConfig());
  }

  @Test
  @Config(minSdk = O)
  public void shouldGiveNonNullCarrierConfigIfNotSet() {
    assertNotNull(telephonyManager.getCarrierConfig());
  }

  @Test
  public void shouldGiveVoiceMailNumber() {
    shadowTelephonyManager.setVoiceMailNumber("123");

    assertEquals("123", telephonyManager.getVoiceMailNumber());
  }

  @Test
  public void shouldGiveVoiceMailAlphaTag() {
    shadowTelephonyManager.setVoiceMailAlphaTag("tag");

    assertEquals("tag", telephonyManager.getVoiceMailAlphaTag());
  }

  @Test
  @Config(minSdk = M)
  public void shouldGivePhoneCount() {
    shadowTelephonyManager.setPhoneCount(42);

    assertEquals(42, telephonyManager.getPhoneCount());
  }

  @Test
  @Config(minSdk = N)
  public void shouldGiveVoiceVibrationEnabled() {
    PhoneAccountHandle phoneAccountHandle =
        new PhoneAccountHandle(
            new ComponentName(ApplicationProvider.getApplicationContext(), Object.class), "handle");

    shadowTelephonyManager.setVoicemailVibrationEnabled(phoneAccountHandle, true);

    assertTrue(telephonyManager.isVoicemailVibrationEnabled(phoneAccountHandle));
  }

  @Test
  @Config(minSdk = N)
  public void shouldGiveVoicemailRingtoneUri() {
    PhoneAccountHandle phoneAccountHandle =
        new PhoneAccountHandle(
            new ComponentName(ApplicationProvider.getApplicationContext(), Object.class), "handle");
    Uri ringtoneUri = Uri.fromParts("file", "ringtone.mp3", /* fragment = */ null);

    shadowTelephonyManager.setVoicemailRingtoneUri(phoneAccountHandle, ringtoneUri);

    assertEquals(ringtoneUri, telephonyManager.getVoicemailRingtoneUri(phoneAccountHandle));
  }

  @Test
  @Config(minSdk = O) // The setter on the real manager was added in O
  public void shouldSetVoicemailRingtoneUri() {
    PhoneAccountHandle phoneAccountHandle =
        new PhoneAccountHandle(
            new ComponentName(ApplicationProvider.getApplicationContext(), Object.class), "handle");
    Uri ringtoneUri = Uri.fromParts("file", "ringtone.mp3", /* fragment = */ null);

    // Note: Using the real manager to set, instead of the shadow.
    telephonyManager.setVoicemailRingtoneUri(phoneAccountHandle, ringtoneUri);

    assertEquals(ringtoneUri, telephonyManager.getVoicemailRingtoneUri(phoneAccountHandle));
  }

  @Test
  @Config(minSdk = O)
  public void shouldCreateForPhoneAccountHandle() {
    PhoneAccountHandle phoneAccountHandle =
        new PhoneAccountHandle(
            new ComponentName(ApplicationProvider.getApplicationContext(), Object.class), "handle");
    TelephonyManager mockTelephonyManager = mock(TelephonyManager.class);

    shadowTelephonyManager.setTelephonyManagerForHandle(phoneAccountHandle, mockTelephonyManager);

    assertEquals(
        mockTelephonyManager, telephonyManager.createForPhoneAccountHandle(phoneAccountHandle));
  }

  @Test
  @Config(minSdk = N)
  public void shouldCreateForSubscriptionId() {
    int subscriptionId = 42;
    TelephonyManager mockTelephonyManager = mock(TelephonyManager.class);

    shadowTelephonyManager.setTelephonyManagerForSubscriptionId(
        subscriptionId, mockTelephonyManager);

    assertEquals(mockTelephonyManager, telephonyManager.createForSubscriptionId(subscriptionId));
  }

  @Test
  @Config(minSdk = O)
  public void shouldSetServiceState() {
    ServiceState serviceState = new ServiceState();
    serviceState.setState(ServiceState.STATE_OUT_OF_SERVICE);

    shadowTelephonyManager.setServiceState(serviceState);

    assertEquals(serviceState, telephonyManager.getServiceState());
  }

  @Test
  public void shouldSetIsNetworkRoaming() {
    shadowTelephonyManager.setIsNetworkRoaming(true);

    assertTrue(telephonyManager.isNetworkRoaming());
  }

  @Test
  public void shouldGetSimState() {
    assertThat(telephonyManager.getSimState()).isEqualTo(TelephonyManager.SIM_STATE_READY);
  }

  @Test
  @Config(minSdk = O)
  public void shouldGetSimStateUsingSlotNumber() {
    int expectedSimState = TelephonyManager.SIM_STATE_ABSENT;
    int slotNumber = 3;
    shadowTelephonyManager.setSimState(slotNumber, expectedSimState);

    assertThat(telephonyManager.getSimState(slotNumber)).isEqualTo(expectedSimState);
  }

  @Test
  public void shouldGetSimIso() {
    assertThat(telephonyManager.getSimCountryIso()).isEmpty();
  }

  @Test
  @Config(minSdk = N)
  public void shouldGetSimIosWhenSetUsingSlotNumber() {
    String expectedSimIso = "usa";
    int subId = 2;
    shadowTelephonyManager.setSimCountryIso(subId, expectedSimIso);

    assertThat(telephonyManager.getSimCountryIso(subId)).isEqualTo(expectedSimIso);
  }

  @Test
  @Config(minSdk = P)
  public void shouldGetSimCarrierId() {
    int expectedCarrierId = 132;
    shadowTelephonyManager.setSimCarrierId(expectedCarrierId);

    assertThat(telephonyManager.getSimCarrierId()).isEqualTo(expectedCarrierId);
  }

  @Test
  @Config(minSdk = M)
  public void shouldGetCurrentPhoneTypeGivenSubId() {
    int subId = 1;
    int expectedPhoneType = TelephonyManager.PHONE_TYPE_GSM;
    shadowTelephonyManager.setCurrentPhoneType(subId, expectedPhoneType);

    assertThat(telephonyManager.getCurrentPhoneType(subId)).isEqualTo(expectedPhoneType);
  }

  @Test
  @Config(minSdk = M)
  public void shouldGetCarrierPackageNamesForIntentAndPhone() {
    List<String> packages = Collections.singletonList("package1");
    int phoneId = 123;
    shadowTelephonyManager.setCarrierPackageNamesForPhone(phoneId, packages);

    assertThat(telephonyManager.getCarrierPackageNamesForIntentAndPhone(new Intent(), phoneId))
        .isEqualTo(packages);
  }

  @Test
  @Config(minSdk = M)
  public void shouldGetCarrierPackageNamesForIntent() {
    List<String> packages = Collections.singletonList("package1");
    shadowTelephonyManager.setCarrierPackageNamesForPhone(
        SubscriptionManager.DEFAULT_SUBSCRIPTION_ID, packages);

    assertThat(telephonyManager.getCarrierPackageNamesForIntent(new Intent())).isEqualTo(packages);
  }

  @Test
  public void resetSimStates_shouldRetainDefaultState() {
    shadowTelephonyManager.resetSimStates();

    assertThat(telephonyManager.getSimState()).isEqualTo(TelephonyManager.SIM_STATE_READY);
  }

  @Test
  @Config(minSdk = N)
  public void resetSimCountryIsos_shouldRetainDefaultState() {
    shadowTelephonyManager.resetSimCountryIsos();

    assertThat(shadowTelephonyManager.getSimCountryIso()).isEmpty();
  }

  @Test
  public void shouldSetSubscriberId() {
    String subscriberId = "123451234512345";
    shadowTelephonyManager.setSubscriberId(subscriberId);

    assertThat(shadowTelephonyManager.getSubscriberId()).isEqualTo(subscriberId);
  }
}
