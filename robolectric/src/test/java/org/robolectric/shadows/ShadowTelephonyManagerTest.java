package org.robolectric.shadows;

import static android.content.Context.TELEPHONY_SERVICE;
import static android.os.Build.VERSION_CODES.LOLLIPOP_MR1;
import static android.os.Build.VERSION_CODES.M;
import static android.os.Build.VERSION_CODES.N;
import static android.os.Build.VERSION_CODES.O;
import static android.os.Build.VERSION_CODES.P;
import static android.os.Build.VERSION_CODES.Q;
import static android.os.Build.VERSION_CODES.R;
import static android.os.Build.VERSION_CODES.S;
import static android.telephony.PhoneStateListener.LISTEN_CALL_STATE;
import static android.telephony.PhoneStateListener.LISTEN_CELL_INFO;
import static android.telephony.PhoneStateListener.LISTEN_CELL_LOCATION;
import static android.telephony.PhoneStateListener.LISTEN_DISPLAY_INFO_CHANGED;
import static android.telephony.PhoneStateListener.LISTEN_SERVICE_STATE;
import static android.telephony.PhoneStateListener.LISTEN_SIGNAL_STRENGTHS;
import static android.telephony.TelephonyDisplayInfo.OVERRIDE_NETWORK_TYPE_LTE_CA;
import static android.telephony.TelephonyDisplayInfo.OVERRIDE_NETWORK_TYPE_NONE;
import static android.telephony.TelephonyDisplayInfo.OVERRIDE_NETWORK_TYPE_NR_NSA;
import static android.telephony.TelephonyManager.CALL_COMPOSER_STATUS_ON;
import static android.telephony.TelephonyManager.CALL_STATE_IDLE;
import static android.telephony.TelephonyManager.CALL_STATE_OFFHOOK;
import static android.telephony.TelephonyManager.CALL_STATE_RINGING;
import static android.telephony.TelephonyManager.NETWORK_TYPE_EVDO_0;
import static android.telephony.TelephonyManager.NETWORK_TYPE_LTE;
import static android.telephony.emergency.EmergencyNumber.EMERGENCY_NUMBER_SOURCE_DATABASE;
import static android.telephony.emergency.EmergencyNumber.EMERGENCY_SERVICE_CATEGORY_POLICE;
import static com.google.common.truth.Truth.assertThat;
import static com.google.common.util.concurrent.MoreExecutors.directExecutor;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.withSettings;
import static org.robolectric.RuntimeEnvironment.getApplication;
import static org.robolectric.Shadows.shadowOf;
import static org.robolectric.shadows.ShadowTelephonyManager.createTelephonyDisplayInfo;

import android.Manifest.permission;
import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.PersistableBundle;
import android.telecom.PhoneAccountHandle;
import android.telephony.CarrierRestrictionRules;
import android.telephony.CellInfo;
import android.telephony.CellLocation;
import android.telephony.PhoneCapability;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyCallback;
import android.telephony.TelephonyCallback.CallStateListener;
import android.telephony.TelephonyCallback.CellInfoListener;
import android.telephony.TelephonyCallback.CellLocationListener;
import android.telephony.TelephonyCallback.DisplayInfoListener;
import android.telephony.TelephonyCallback.ServiceStateListener;
import android.telephony.TelephonyCallback.SignalStrengthsListener;
import android.telephony.TelephonyDisplayInfo;
import android.telephony.TelephonyManager;
import android.telephony.TelephonyManager.BootstrapAuthenticationCallback;
import android.telephony.TelephonyManager.CellInfoCallback;
import android.telephony.UiccSlotInfo;
import android.telephony.VisualVoicemailSmsFilterSettings;
import android.telephony.emergency.EmergencyNumber;
import android.telephony.gba.UaSecurityProtocolIdentifier;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;

@RunWith(AndroidJUnit4.class)
public class ShadowTelephonyManagerTest {

  private TelephonyManager telephonyManager;
  private ShadowTelephonyManager shadowTelephonyManager;
  private TelephonyManager tmForSub5;

  @Before
  public void setUp() throws Exception {
    telephonyManager = (TelephonyManager) getApplication().getSystemService(TELEPHONY_SERVICE);
    shadowTelephonyManager = Shadow.extract(telephonyManager);
    shadowOf((Application) ApplicationProvider.getApplicationContext())
        .grantPermissions(permission.READ_PRIVILEGED_PHONE_STATE);
    tmForSub5 = newTelephonyManager(5);
  }

  private TelephonyManager newTelephonyManager(Integer subId) {
    Class<?>[] parameters;
    Object[] arguments;
    if (subId == null || Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
      parameters = new Class<?>[] {Context.class};
      arguments = new Object[] {ApplicationProvider.getApplicationContext()};
    } else {
      parameters = new Class<?>[] {Context.class, int.class};
      arguments = new Object[] {ApplicationProvider.getApplicationContext(), subId};
    }
    TelephonyManager newInstance =
        Shadow.newInstance(TelephonyManager.class, parameters, arguments);
    if (subId != null) {
      shadowTelephonyManager.setTelephonyManagerForSubscriptionId(subId, newInstance);
    }
    return newInstance;
  }

  @Test
  public void testListenInit() {
    PhoneStateListener listener = mock(PhoneStateListener.class);
    telephonyManager.listen(listener, LISTEN_CALL_STATE | LISTEN_CELL_INFO | LISTEN_CELL_LOCATION);

    verify(listener).onCallStateChanged(CALL_STATE_IDLE, null);
    verify(listener).onCellLocationChanged(null);
    verify(listener).onCellInfoChanged(Collections.emptyList());
  }

  @Test
  @Config(minSdk = S)
  public void registerTelephonyCallback_shouldInitCallback() {
    TelephonyCallback callback =
        mock(
            TelephonyCallback.class,
            withSettings()
                .extraInterfaces(
                    CallStateListener.class, CellInfoListener.class, CellLocationListener.class));
    telephonyManager.registerTelephonyCallback(directExecutor(), callback);

    assertThat(shadowTelephonyManager.getLastTelephonyCallback()).isEqualTo(callback);
    verify((CallStateListener) callback).onCallStateChanged(CALL_STATE_IDLE);
    verify((CellInfoListener) callback).onCellInfoChanged(ImmutableList.of());
    verify((CellLocationListener) callback).onCellLocationChanged(null);
  }

  @Test
  @Config(minSdk = S)
  public void unregisterTelephonyCallback_shouldRemoveCallback() {
    TelephonyCallback callback =
        mock(TelephonyCallback.class, withSettings().extraInterfaces(CallStateListener.class));
    telephonyManager.registerTelephonyCallback(directExecutor(), callback);
    reset(callback);
    telephonyManager.unregisterTelephonyCallback(callback);

    shadowOf(telephonyManager).setCallState(CALL_STATE_RINGING, "123");

    verifyNoMoreInteractions(callback);
  }

  @Test
  public void shouldGiveDeviceId() {
    String testId = "TESTING123";
    shadowOf(telephonyManager).setDeviceId(testId);
    assertEquals(testId, telephonyManager.getDeviceId());
  }

  @Test
  @Config(minSdk = M)
  public void setDeviceId_withSlot_doesNotAffectCallingInstance() {
    String testId = "TESTING123";
    shadowOf(telephonyManager).setDeviceId(123, testId);
    assertThat(telephonyManager.getDeviceId()).isNull();
    assertThat(telephonyManager.getDeviceId(123)).isEqualTo(testId);
  }

  @Test
  @Config(minSdk = M)
  public void shouldGiveDeviceIdForSlot() {
    shadowOf(telephonyManager).setDeviceId(1, "device in slot 1");
    shadowOf(telephonyManager).setDeviceId(2, "device in slot 2");

    assertEquals("device in slot 1", telephonyManager.getDeviceId(1));
    assertEquals("device in slot 2", telephonyManager.getDeviceId(2));
  }

  @Test
  public void shouldGiveDeviceSoftwareVersion() {
    String testSoftwareVersion = "getDeviceSoftwareVersion";
    shadowOf(telephonyManager).setDeviceSoftwareVersion(testSoftwareVersion);
    assertEquals(testSoftwareVersion, telephonyManager.getDeviceSoftwareVersion());
  }

  @Test
  @Config(minSdk = O)
  public void getImei() {
    String testImei = "4test imei";
    shadowOf(telephonyManager).setImei(testImei);
    assertEquals(testImei, telephonyManager.getImei());
  }

  @Test
  @Config(minSdk = O)
  public void getImeiForSlot() {
    shadowOf(telephonyManager).setImei("defaultImei");
    shadowOf(telephonyManager).setImei(0, "imei0");
    shadowOf(telephonyManager).setImei(1, "imei1");
    assertEquals("imei0", telephonyManager.getImei(0));
    assertEquals("imei1", telephonyManager.getImei(1));
  }

  @Test
  @Config(minSdk = O)
  public void setImei_withSlotId_acceptsNull() {
    shadowOf(telephonyManager).setImei(0, "imei0");
    shadowOf(telephonyManager).setImei(0, null);
    assertEquals(null, telephonyManager.getImei(0));
  }

  @Test
  @Config(minSdk = O)
  public void getMeid() {
    String testMeid = "4test meid";
    shadowOf(telephonyManager).setMeid(testMeid);
    assertEquals(testMeid, telephonyManager.getMeid());
  }

  @Test
  @Config(minSdk = O)
  public void getMeidForSlot() {
    shadowOf(telephonyManager).setMeid("defaultMeid");
    shadowOf(telephonyManager).setMeid(0, "meid0");
    shadowOf(telephonyManager).setMeid(1, "meid1");
    assertEquals("meid0", telephonyManager.getMeid(0));
    assertEquals("meid1", telephonyManager.getMeid(1));
    assertEquals("defaultMeid", telephonyManager.getMeid());
  }

  @Test
  public void shouldGiveNetworkOperatorName() {
    shadowOf(telephonyManager).setNetworkOperatorName("SomeOperatorName");
    assertEquals("SomeOperatorName", telephonyManager.getNetworkOperatorName());
  }

  @Test
  public void shouldGiveSimOperatorName() {
    shadowOf(telephonyManager).setSimOperatorName("SomeSimOperatorName");
    assertEquals("SomeSimOperatorName", telephonyManager.getSimOperatorName());
  }

  @Test(expected = SecurityException.class)
  public void
      getSimSerialNumber_shouldThrowSecurityExceptionWhenReadPhoneStatePermissionNotGranted()
          throws Exception {
    shadowOf(telephonyManager).setReadPhoneStatePermission(false);
    telephonyManager.getSimSerialNumber();
  }

  @Test
  public void shouldGetSimSerialNumber() {
    shadowOf(telephonyManager).setSimSerialNumber("SomeSerialNumber");
    assertEquals("SomeSerialNumber", telephonyManager.getSimSerialNumber());
  }

  @Test
  public void shouldGiveNetworkType() {
    shadowOf(telephonyManager).setNetworkType(TelephonyManager.NETWORK_TYPE_CDMA);
    assertEquals(TelephonyManager.NETWORK_TYPE_CDMA, telephonyManager.getNetworkType());
  }

  @Test
  @Config(minSdk = N)
  public void shouldGiveDataNetworkType() {
    shadowOf(telephonyManager).setDataNetworkType(TelephonyManager.NETWORK_TYPE_CDMA);
    assertEquals(TelephonyManager.NETWORK_TYPE_CDMA, telephonyManager.getDataNetworkType());
  }

  @Test
  @Config(minSdk = N)
  public void shouldGiveVoiceNetworkType() {
    shadowOf(telephonyManager).setVoiceNetworkType(TelephonyManager.NETWORK_TYPE_CDMA);
    assertThat(telephonyManager.getVoiceNetworkType())
        .isEqualTo(TelephonyManager.NETWORK_TYPE_CDMA);
  }

  @Test
  public void shouldGiveAllCellInfo() {
    PhoneStateListener listener = mock(PhoneStateListener.class);
    telephonyManager.listen(listener, LISTEN_CELL_INFO);

    List<CellInfo> allCellInfo = Collections.singletonList(mock(CellInfo.class));
    shadowOf(telephonyManager).setAllCellInfo(allCellInfo);
    assertEquals(allCellInfo, telephonyManager.getAllCellInfo());
    verify(listener).onCellInfoChanged(allCellInfo);
  }

  @Test
  @Config(minSdk = S)
  public void shouldGiveAllCellInfo_toCallback() {
    TelephonyCallback callback =
        mock(TelephonyCallback.class, withSettings().extraInterfaces(CellInfoListener.class));
    telephonyManager.registerTelephonyCallback(directExecutor(), callback);

    List<CellInfo> allCellInfo = Collections.singletonList(mock(CellInfo.class));
    shadowOf(telephonyManager).setAllCellInfo(allCellInfo);
    assertEquals(allCellInfo, telephonyManager.getAllCellInfo());
    verify((CellInfoListener) callback).onCellInfoChanged(allCellInfo);
  }

  @Test
  @Config(minSdk = Q)
  public void shouldGiveCellInfoUpdate() throws Exception {
    List<CellInfo> callbackCellInfo = Collections.singletonList(mock(CellInfo.class));
    shadowOf(telephonyManager).setCallbackCellInfos(callbackCellInfo);
    assertNotEquals(callbackCellInfo, telephonyManager.getAllCellInfo());

    CountDownLatch callbackLatch = new CountDownLatch(1);
    shadowOf(telephonyManager)
        .requestCellInfoUpdate(
            new Executor() {
              @Override
              public void execute(Runnable r) {
                r.run();
              }
            },
            new CellInfoCallback() {
              @Override
              public void onCellInfo(List<CellInfo> list) {
                assertEquals(callbackCellInfo, list);
                callbackLatch.countDown();
              }
            });

    assertTrue(callbackLatch.await(5000, TimeUnit.MILLISECONDS));
  }

  @Test
  public void shouldGiveNetworkCountryIsoInLowercase() {
    shadowOf(telephonyManager).setNetworkCountryIso("SomeIso");
    assertEquals("someiso", telephonyManager.getNetworkCountryIso());
  }

  @Test
  @Config(minSdk = Q)
  public void shouldGiveSimLocale() {
    shadowOf(telephonyManager).setSimLocale(Locale.FRANCE);
    assertEquals(Locale.FRANCE, telephonyManager.getSimLocale());
  }

  @Test
  public void shouldGiveNetworkOperator() {
    shadowOf(telephonyManager).setNetworkOperator("SomeOperator");
    assertEquals("SomeOperator", telephonyManager.getNetworkOperator());
  }

  @Test
  @Config(minSdk = O)
  public void shouldGiveNetworkSpecifier() {
    shadowOf(telephonyManager).setNetworkSpecifier("SomeSpecifier");
    assertEquals("SomeSpecifier", telephonyManager.getNetworkSpecifier());
  }

  @Test
  public void shouldGiveLine1Number() {
    shadowOf(telephonyManager).setLine1Number("123-244-2222");
    assertEquals("123-244-2222", telephonyManager.getLine1Number());
  }

  @Test
  public void shouldGiveGroupIdLevel1() {
    shadowOf(telephonyManager).setGroupIdLevel1("SomeGroupId");
    assertEquals("SomeGroupId", telephonyManager.getGroupIdLevel1());
  }

  @Test(expected = SecurityException.class)
  public void getDeviceId_shouldThrowSecurityExceptionWhenReadPhoneStatePermissionNotGranted()
      throws Exception {
    shadowOf(telephonyManager).setReadPhoneStatePermission(false);
    telephonyManager.getDeviceId();
  }

  @Test
  @Config(minSdk = M)
  public void
      getDeviceIdForSlot_shouldThrowSecurityExceptionWhenReadPhoneStatePermissionNotGranted()
          throws Exception {
    shadowOf(telephonyManager).setReadPhoneStatePermission(false);
    assertThrows(SecurityException.class, () -> telephonyManager.getDeviceId(1));
  }

  @Test
  public void
      getDeviceSoftwareVersion_shouldThrowSecurityExceptionWhenReadPhoneStatePermissionNotGranted()
          throws Exception {
    shadowOf(telephonyManager).setReadPhoneStatePermission(false);
    assertThrows(SecurityException.class, () -> telephonyManager.getDeviceSoftwareVersion());
  }

  @Test
  public void shouldGivePhoneType() {
    shadowOf(telephonyManager).setPhoneType(TelephonyManager.PHONE_TYPE_CDMA);
    assertEquals(TelephonyManager.PHONE_TYPE_CDMA, telephonyManager.getPhoneType());
    shadowOf(telephonyManager).setPhoneType(TelephonyManager.PHONE_TYPE_GSM);
    assertEquals(TelephonyManager.PHONE_TYPE_GSM, telephonyManager.getPhoneType());
  }

  @Test
  @Config(minSdk = M)
  public void setCurrentPhoneType_fromPhoneId_canBeReadFromAnyInstance() {
    shadowOf(telephonyManager).setCurrentPhoneType(123, TelephonyManager.PHONE_TYPE_CDMA);

    assertEquals(TelephonyManager.PHONE_TYPE_CDMA, tmForSub5.getCurrentPhoneType(123));
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
  @Config(minSdk = S)
  public void shouldGiveCellLocation_toCallback() {
    TelephonyCallback callback =
        mock(TelephonyCallback.class, withSettings().extraInterfaces(CellLocationListener.class));
    telephonyManager.registerTelephonyCallback(directExecutor(), callback);

    CellLocation mockCellLocation = mock(CellLocation.class);
    shadowOf(telephonyManager).setCellLocation(mockCellLocation);
    assertEquals(mockCellLocation, telephonyManager.getCellLocation());
    verify((CellLocationListener) callback).onCellLocationChanged(mockCellLocation);
  }

  @Test
  @Config(minSdk = S)
  public void shouldGiveCallStateForSubscription() {
    PhoneStateListener listener = mock(PhoneStateListener.class);
    telephonyManager.listen(listener, LISTEN_CALL_STATE);

    shadowOf(telephonyManager).setCallState(CALL_STATE_RINGING, "911");
    assertEquals(CALL_STATE_RINGING, telephonyManager.getCallStateForSubscription());
    verify(listener).onCallStateChanged(CALL_STATE_RINGING, "911");

    shadowOf(telephonyManager).setCallState(CALL_STATE_OFFHOOK, "911");
    assertEquals(CALL_STATE_OFFHOOK, telephonyManager.getCallStateForSubscription());
    verify(listener).onCallStateChanged(CALL_STATE_OFFHOOK, null);
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
  @Config(minSdk = S)
  public void shouldGiveCallState_toCallback() {
    TelephonyCallback callback =
        mock(TelephonyCallback.class, withSettings().extraInterfaces(CallStateListener.class));
    telephonyManager.registerTelephonyCallback(directExecutor(), callback);

    shadowOf(telephonyManager).setCallState(CALL_STATE_RINGING, "911");
    assertEquals(CALL_STATE_RINGING, telephonyManager.getCallState());
    verify((CallStateListener) callback).onCallStateChanged(CALL_STATE_RINGING);

    shadowOf(telephonyManager).setCallState(CALL_STATE_OFFHOOK, "911");
    assertEquals(CALL_STATE_OFFHOOK, telephonyManager.getCallState());
    verify((CallStateListener) callback).onCallStateChanged(CALL_STATE_OFFHOOK);
  }

  @Test
  public void isSmsCapable() {
    assertThat(telephonyManager.isSmsCapable()).isTrue();
    shadowOf(telephonyManager).setIsSmsCapable(false);
    assertThat(telephonyManager.isSmsCapable()).isFalse();
  }

  @Test
  @Config(minSdk = R)
  public void setSmsCapable_modifiesAllInstances() {
    shadowOf(telephonyManager).setIsSmsCapable(false);
    newTelephonyManager(123);
    assertThat(telephonyManager.createForSubscriptionId(123).isSmsCapable()).isFalse();
  }

  @Test
  @Config(minSdk = O)
  public void shouldGiveCarrierConfigIfSet() {
    PersistableBundle bundle = new PersistableBundle();
    bundle.putInt("foo", 42);
    shadowOf(telephonyManager).setCarrierConfig(bundle);

    assertEquals(bundle, telephonyManager.getCarrierConfig());
  }

  @Test
  @Config(minSdk = O)
  public void shouldGiveNonNullCarrierConfigIfNotSet() {
    assertNotNull(telephonyManager.getCarrierConfig());
  }

  @Test
  public void shouldGiveVoiceMailNumber() {
    shadowOf(telephonyManager).setVoiceMailNumber("123");

    assertEquals("123", telephonyManager.getVoiceMailNumber());
  }

  @Test
  public void shouldGiveVoiceMailAlphaTag() {
    shadowOf(telephonyManager).setVoiceMailAlphaTag("tag");

    assertEquals("tag", telephonyManager.getVoiceMailAlphaTag());
  }

  @Test
  @Config(minSdk = M)
  public void shouldGivePhoneCount() {
    shadowOf(telephonyManager).setPhoneCount(42);

    assertEquals(42, telephonyManager.getPhoneCount());
  }

  @Test
  @Config(minSdk = R)
  public void shouldGiveDefaultActiveModemCount() {
    assertThat(telephonyManager.getActiveModemCount()).isEqualTo(1);
  }

  @Test
  @Config(minSdk = R)
  public void shouldGiveActiveModemCount() {
    shadowOf(telephonyManager).setActiveModemCount(42);

    assertThat(telephonyManager.getActiveModemCount()).isEqualTo(42);
  }

  @Test
  @Config(minSdk = LOLLIPOP_MR1)
  public void shouldGiveVoiceCapableTrue() {
    shadowOf(telephonyManager).setVoiceCapable(true);

    assertTrue(telephonyManager.isVoiceCapable());
  }

  @Test
  @Config(minSdk = LOLLIPOP_MR1)
  public void shouldGiveVoiceCapableFalse() {
    shadowOf(telephonyManager).setVoiceCapable(false);

    assertFalse(telephonyManager.isVoiceCapable());
  }

  @Test
  @Config(minSdk = N)
  public void shouldGiveVoiceVibrationEnabled() {
    PhoneAccountHandle phoneAccountHandle =
        new PhoneAccountHandle(
            new ComponentName(ApplicationProvider.getApplicationContext(), Object.class), "handle");

    shadowOf(telephonyManager).setVoicemailVibrationEnabled(phoneAccountHandle, true);

    assertTrue(telephonyManager.isVoicemailVibrationEnabled(phoneAccountHandle));
  }

  @Test
  @Config(minSdk = N)
  public void setVoicemailVibrationEnabled_accessibleFromAllTelephonyManagers() {
    PhoneAccountHandle phoneAccountHandle =
        new PhoneAccountHandle(
            new ComponentName(ApplicationProvider.getApplicationContext(), Object.class), "handle");

    shadowOf(telephonyManager).setVoicemailVibrationEnabled(phoneAccountHandle, true);

    assertTrue(telephonyManager.isVoicemailVibrationEnabled(phoneAccountHandle));
  }

  @Test
  @Config(minSdk = N)
  public void shouldGiveVoicemailRingtoneUri() {
    PhoneAccountHandle phoneAccountHandle =
        new PhoneAccountHandle(
            new ComponentName(ApplicationProvider.getApplicationContext(), Object.class), "handle");
    Uri ringtoneUri = Uri.fromParts("file", "ringtone.mp3", /* fragment= */ null);

    shadowOf(telephonyManager).setVoicemailRingtoneUri(phoneAccountHandle, ringtoneUri);

    assertEquals(ringtoneUri, telephonyManager.getVoicemailRingtoneUri(phoneAccountHandle));
  }

  @Test
  @Config(minSdk = O) // The setter on the real manager was added in O
  public void shouldSetVoicemailRingtoneUri() {
    PhoneAccountHandle phoneAccountHandle =
        new PhoneAccountHandle(
            new ComponentName(ApplicationProvider.getApplicationContext(), Object.class), "handle");
    Uri ringtoneUri = Uri.fromParts("file", "ringtone.mp3", /* fragment= */ null);

    // Note: Using the real manager to set, instead of the shadow.
    telephonyManager.setVoicemailRingtoneUri(phoneAccountHandle, ringtoneUri);

    assertEquals(ringtoneUri, telephonyManager.getVoicemailRingtoneUri(phoneAccountHandle));
  }

  @Test
  @Config(minSdk = N)
  public void setVoicemailRingtoneUri_accessibleFromAllTelephonyManagers() {
    PhoneAccountHandle phoneAccountHandle =
        new PhoneAccountHandle(
            new ComponentName(ApplicationProvider.getApplicationContext(), Object.class), "handle");
    Uri ringtoneUri = Uri.fromParts("file", "ringtone.mp3", /* fragment= */ null);

    shadowOf(telephonyManager).setVoicemailRingtoneUri(phoneAccountHandle, ringtoneUri);

    assertEquals(ringtoneUri, telephonyManager.getVoicemailRingtoneUri(phoneAccountHandle));
  }

  @Test
  @Config(minSdk = O)
  public void shouldCreateForPhoneAccountHandle() {
    PhoneAccountHandle phoneAccountHandle =
        new PhoneAccountHandle(
            new ComponentName(ApplicationProvider.getApplicationContext(), Object.class), "handle");
    TelephonyManager mockTelephonyManager = mock(TelephonyManager.class);

    shadowOf(telephonyManager)
        .setTelephonyManagerForHandle(phoneAccountHandle, mockTelephonyManager);

    assertEquals(
        mockTelephonyManager, telephonyManager.createForPhoneAccountHandle(phoneAccountHandle));
  }

  @Test
  @Config(minSdk = O)
  public void shouldCreateForPhoneAccountHandle_fromAllInstances() {
    PhoneAccountHandle phoneAccountHandle =
        new PhoneAccountHandle(
            new ComponentName(ApplicationProvider.getApplicationContext(), Object.class), "handle");
    TelephonyManager mockTelephonyManager = mock(TelephonyManager.class);

    shadowOf(telephonyManager)
        .setTelephonyManagerForHandle(phoneAccountHandle, mockTelephonyManager);

    assertEquals(mockTelephonyManager, tmForSub5.createForPhoneAccountHandle(phoneAccountHandle));
  }

  @Test
  @Config(minSdk = N)
  public void shouldCreateForSubscriptionId() {
    int subscriptionId = 42;
    TelephonyManager mockTelephonyManager = mock(TelephonyManager.class);

    shadowOf(telephonyManager)
        .setTelephonyManagerForSubscriptionId(subscriptionId, mockTelephonyManager);

    assertEquals(mockTelephonyManager, telephonyManager.createForSubscriptionId(subscriptionId));
  }

  @Test
  @Config(minSdk = N)
  public void shouldCreateForSubscriptionId_fromAllInstances() {
    int subscriptionId = 42;
    TelephonyManager mockTelephonyManager = mock(TelephonyManager.class);

    shadowOf(telephonyManager)
        .setTelephonyManagerForSubscriptionId(subscriptionId, mockTelephonyManager);

    assertEquals(mockTelephonyManager, tmForSub5.createForSubscriptionId(subscriptionId));
  }

  @Test
  @Config(minSdk = O)
  public void shouldSetServiceState() {
    PhoneStateListener listener = mock(PhoneStateListener.class);
    telephonyManager.listen(listener, LISTEN_SERVICE_STATE);
    ServiceState serviceState = new ServiceState();
    serviceState.setState(ServiceState.STATE_OUT_OF_SERVICE);

    shadowOf(telephonyManager).setServiceState(serviceState);

    assertEquals(serviceState, telephonyManager.getServiceState());
    verify(listener).onServiceStateChanged(serviceState);
  }

  @Test
  @Config(minSdk = S)
  public void shouldSetServiceState_toCallback() {
    TelephonyCallback callback =
        mock(TelephonyCallback.class, withSettings().extraInterfaces(ServiceStateListener.class));
    telephonyManager.registerTelephonyCallback(directExecutor(), callback);
    ServiceState serviceState = new ServiceState();
    serviceState.setState(ServiceState.STATE_OUT_OF_SERVICE);

    shadowOf(telephonyManager).setServiceState(serviceState);

    assertEquals(serviceState, telephonyManager.getServiceState());
    verify((ServiceStateListener) callback).onServiceStateChanged(serviceState);
  }

  @Test
  @Config(minSdk = O)
  public void listen_doesNotNotifyListenerOfCurrentServiceStateIfUninitialized() {
    PhoneStateListener listener = mock(PhoneStateListener.class);

    telephonyManager.listen(listener, LISTEN_SERVICE_STATE);

    verifyNoMoreInteractions(listener);
  }

  @Test
  @Config(minSdk = S)
  public void register_doesNotNotifyCallbackOfCurrentServiceStateIfUninitialized() {
    TelephonyCallback callback =
        mock(TelephonyCallback.class, withSettings().extraInterfaces(ServiceStateListener.class));

    telephonyManager.registerTelephonyCallback(directExecutor(), callback);

    verifyNoMoreInteractions(callback);
  }

  @Test
  @Config(minSdk = O)
  public void listen_notifiesListenerOfCurrentServiceStateIfInitialized() {
    PhoneStateListener listener = mock(PhoneStateListener.class);
    ServiceState serviceState = new ServiceState();
    serviceState.setState(ServiceState.STATE_OUT_OF_SERVICE);
    shadowOf(telephonyManager).setServiceState(serviceState);

    telephonyManager.listen(listener, LISTEN_SERVICE_STATE);

    verify(listener, times(1)).onServiceStateChanged(serviceState);
  }

  @Test
  @Config(minSdk = S)
  public void register_notifiesCallbackOfCurrentServiceStateIfInitialized() {
    TelephonyCallback callback =
        mock(TelephonyCallback.class, withSettings().extraInterfaces(ServiceStateListener.class));
    ServiceState serviceState = new ServiceState();
    serviceState.setState(ServiceState.STATE_OUT_OF_SERVICE);
    shadowOf(telephonyManager).setServiceState(serviceState);

    telephonyManager.registerTelephonyCallback(directExecutor(), callback);

    verify((ServiceStateListener) callback, times(1)).onServiceStateChanged(serviceState);
  }

  @Test
  public void shouldSetIsNetworkRoaming() {
    shadowOf(telephonyManager).setIsNetworkRoaming(true);

    assertTrue(telephonyManager.isNetworkRoaming());
  }

  @Test
  public void shouldGetSimState() {
    assertThat(telephonyManager.getSimState()).isEqualTo(TelephonyManager.SIM_STATE_READY);
  }

  @Test
  @Config(minSdk = O)
  public void getSimState_defaultForZeroSpecial() {
    assertThat(telephonyManager.getSimState(1)).isEqualTo(TelephonyManager.SIM_STATE_UNKNOWN);
    assertThat(telephonyManager.getSimState(0)).isEqualTo(TelephonyManager.SIM_STATE_READY);
  }

  @Test
  @Config(minSdk = O)
  public void shouldGetSimStateUsingSlotNumber() {
    int expectedSimState = TelephonyManager.SIM_STATE_ABSENT;
    int slotNumber = 3;
    shadowOf(telephonyManager).setSimState(slotNumber, expectedSimState);

    assertThat(telephonyManager.getSimState(slotNumber)).isEqualTo(expectedSimState);
  }

  @Test
  @Config(minSdk = O)
  public void setSimState_withSlotParameter_doesNotAffectCaller() {
    int expectedSimState = TelephonyManager.SIM_STATE_ABSENT;
    int slotNumber = 3;
    shadowOf(telephonyManager).setSimState(slotNumber, expectedSimState);

    assertThat(telephonyManager.getSimState()).isEqualTo(TelephonyManager.SIM_STATE_READY);
  }

  @Test
  public void shouldGetSimIso() {
    assertThat(telephonyManager.getSimCountryIso()).isEmpty();
  }

  @Test
  @Config(minSdk = N, maxSdk = Q)
  public void shouldGetSimIso_resetsZeroSpecial() {
    assertThat(callGetSimCountryIso(telephonyManager, 1)).isNull();
    assertThat(callGetSimCountryIso(telephonyManager, 0)).isEmpty();
  }

  private String callGetSimCountryIso(TelephonyManager telephonyManager, int subId) {
    return (String)
        ReflectionHelpers.callInstanceMethod(
            telephonyManager, "getSimCountryIso", ClassParameter.from(int.class, subId));
  }

  @Test
  @Config(minSdk = N, maxSdk = Q)
  public void shouldGetSimIsoWhenSetUsingSlotNumber() {
    String expectedSimIso = "usa";
    int subId = 2;
    shadowOf(telephonyManager).setSimCountryIso(subId, expectedSimIso);

    assertThat(callGetSimCountryIso(telephonyManager, subId)).isEqualTo(expectedSimIso);
  }

  @Test
  @Config(minSdk = N, maxSdk = Q)
  public void setSimIso_withSlotParameter_doesNotAffectCaller() {
    String expectedSimIso = "usa";
    int subId = 2;
    shadowOf(telephonyManager).setSimCountryIso(subId, expectedSimIso);

    assertThat(telephonyManager.getSimCountryIso()).isEqualTo("");
  }

  @Test
  @Config(minSdk = N, maxSdk = Q)
  public void setSimIso_withSlotParameter_acceptsNull() {
    String expectedSimIso = "usa";
    int subId = 2;
    shadowOf(telephonyManager).setSimCountryIso(subId, expectedSimIso);
    shadowOf(telephonyManager).setSimCountryIso(subId, null);

    assertThat(callGetSimCountryIso(telephonyManager, subId)).isEqualTo(null);
  }

  @Test
  @Config(minSdk = P)
  public void shouldGetSimCarrierId() {
    int expectedCarrierId = 132;
    shadowOf(telephonyManager).setSimCarrierId(expectedCarrierId);

    assertThat(telephonyManager.getSimCarrierId()).isEqualTo(expectedCarrierId);
  }

  @Test
  @Config(minSdk = Q)
  public void shouldGetSimSpecificCarrierId() {
    int expectedCarrierId = 132;
    shadowOf(telephonyManager).setSimSpecificCarrierId(expectedCarrierId);

    assertThat(telephonyManager.getSimSpecificCarrierId()).isEqualTo(expectedCarrierId);
  }

  @Test
  @Config(minSdk = P)
  public void shouldGetSimCarrierIdName() {
    String expectedCarrierIdName = "Fi";
    shadowOf(telephonyManager).setSimCarrierIdName(expectedCarrierIdName);

    assertThat(telephonyManager.getSimCarrierIdName().toString()).isEqualTo(expectedCarrierIdName);
  }

  @Test
  @Config(minSdk = Q)
  public void shouldGetCarrierIdFromSimMccMnc() {
    int expectedCarrierId = 419;
    shadowOf(telephonyManager).setCarrierIdFromSimMccMnc(expectedCarrierId);

    assertThat(telephonyManager.getCarrierIdFromSimMccMnc()).isEqualTo(expectedCarrierId);
  }

  @Test
  @Config(minSdk = M)
  public void shouldGetCurrentPhoneTypeGivenSubId() {
    int subId = 1;
    int expectedPhoneType = TelephonyManager.PHONE_TYPE_GSM;
    shadowOf(telephonyManager).setCurrentPhoneType(subId, expectedPhoneType);

    assertThat(telephonyManager.getCurrentPhoneType(subId)).isEqualTo(expectedPhoneType);
  }

  @Test
  @Config(minSdk = M)
  public void shouldGetCurrentPhoneTypeGivenSubId_fromAllInstances() {
    int subId = 1;
    int expectedPhoneType = TelephonyManager.PHONE_TYPE_GSM;
    shadowOf(telephonyManager).setCurrentPhoneType(subId, expectedPhoneType);

    assertThat(tmForSub5.getCurrentPhoneType(subId)).isEqualTo(expectedPhoneType);
  }

  @Test
  @Config(minSdk = M)
  public void clearPhoneTypes_setsStartingState() {
    ShadowTelephonyManager.clearPhoneTypes();
    assertEquals(TelephonyManager.PHONE_TYPE_NONE, telephonyManager.getCurrentPhoneType(0));
    assertEquals(TelephonyManager.PHONE_TYPE_NONE, telephonyManager.getCurrentPhoneType(1));
  }

  @Test
  @Config(minSdk = M)
  public void shouldGetCarrierPackageNamesForIntentAndPhone() {
    List<String> packages = Collections.singletonList("package1");
    int phoneId = 123;
    shadowOf(telephonyManager).setCarrierPackageNamesForPhone(phoneId, packages);

    assertThat(telephonyManager.getCarrierPackageNamesForIntentAndPhone(new Intent(), phoneId))
        .isEqualTo(packages);
  }

  @Test
  @Config(minSdk = M)
  public void setCarrierPackageNamesForPhone_acceptsNull() {
    List<String> packages = Collections.singletonList("package1");
    int phoneId = 123;
    shadowOf(telephonyManager).setCarrierPackageNamesForPhone(phoneId, packages);
    shadowOf(telephonyManager).setCarrierPackageNamesForPhone(phoneId, null);

    assertThat(telephonyManager.getCarrierPackageNamesForIntentAndPhone(new Intent(), phoneId))
        .isEqualTo(null);
  }

  @Test
  @Config(minSdk = M)
  public void shouldGetCarrierPackageNamesForIntentAndPhone_fromAllInstances() {
    List<String> packages = Collections.singletonList("package1");
    int phoneId = 123;
    shadowOf(telephonyManager).setCarrierPackageNamesForPhone(phoneId, packages);

    assertThat(tmForSub5.getCarrierPackageNamesForIntentAndPhone(new Intent(), phoneId))
        .isEqualTo(packages);
  }

  @Test
  @Config(minSdk = M)
  public void shouldGetCarrierPackageNamesForIntentAndPhone_doesNotAffectCaller() {
    List<String> packages = Collections.singletonList("package1");
    int phoneId = 123;
    shadowOf(telephonyManager).setCarrierPackageNamesForPhone(phoneId, packages);

    assertThat(telephonyManager.getCarrierPackageNamesForIntent(new Intent())).isNull();
  }

  @Test
  @Config(minSdk = M)
  public void shouldGetCarrierPackageNamesForIntent() {
    List<String> packages = Collections.singletonList("package1");
    shadowOf(telephonyManager)
        .setCarrierPackageNamesForPhone(SubscriptionManager.DEFAULT_SUBSCRIPTION_ID, packages);

    assertThat(telephonyManager.getCarrierPackageNamesForIntent(new Intent())).isEqualTo(packages);
  }

  @Test
  @Config(minSdk = O)
  public void resetSimStates_shouldRetainDefaultState() {
    shadowOf(telephonyManager).resetSimStates();

    assertThat(telephonyManager.getSimState()).isEqualTo(TelephonyManager.SIM_STATE_READY);
    assertThat(telephonyManager.getSimState(1)).isEqualTo(TelephonyManager.SIM_STATE_UNKNOWN);
    assertThat(telephonyManager.getSimState(0)).isEqualTo(TelephonyManager.SIM_STATE_READY);
  }

  @Test
  @Config(minSdk = N)
  public void resetSimCountryIsos_shouldRetainDefaultState() {
    shadowOf(telephonyManager).resetSimCountryIsos();

    assertThat(telephonyManager.getSimCountryIso()).isEmpty();
  }

  @Test
  @Config(minSdk = N, maxSdk = Q)
  public void resetSimCountryIsos_resetZeroSpecial() {
    shadowOf(telephonyManager).resetSimCountryIsos();
    assertThat(callGetSimCountryIso(telephonyManager, 1)).isNull();
    assertThat(callGetSimCountryIso(telephonyManager, 0)).isEmpty();
  }

  @Test
  public void shouldSetSubscriberId() {
    String subscriberId = "123451234512345";
    shadowOf(telephonyManager).setSubscriberId(subscriberId);

    assertThat(telephonyManager.getSubscriberId()).isEqualTo(subscriberId);
  }

  @Test(expected = SecurityException.class)
  public void getSubscriberId_shouldThrowSecurityExceptionWhenReadPhoneStatePermissionNotGranted() {
    shadowOf(telephonyManager).setReadPhoneStatePermission(false);
    telephonyManager.getSubscriberId();
  }

  @Test
  @Config(minSdk = P)
  public void getUiccSlotsInfo() {
    UiccSlotInfo slotInfo1 = new UiccSlotInfo(true, true, null, 0, 0, true);
    UiccSlotInfo slotInfo2 = new UiccSlotInfo(true, true, null, 0, 1, true);
    UiccSlotInfo[] slotInfos = new UiccSlotInfo[] {slotInfo1, slotInfo2};
    shadowOf(telephonyManager).setUiccSlotsInfo(slotInfos);

    assertThat(shadowOf(telephonyManager).getUiccSlotsInfo()).isEqualTo(slotInfos);
  }

  @Test
  @Config(minSdk = Q)
  public void getUiccCardsInfo() {
    Object /*UiccCardInfo*/ cardsInfo1 = "new UiccCardInfo(true, true, null, 0, 0, true)";
    Object /*UiccCardInfo*/ cardsInfo2 = "new UiccCardInfo(true, true, null, 0, 1, true)";
    List<Object /*UiccCardInfo*/> cardInfos = ImmutableList.of(cardsInfo1, cardsInfo2);
    shadowOf(telephonyManager).setUiccCardsInfo(cardInfos);

    assertThat(telephonyManager.getUiccCardsInfo()).isEqualTo(cardInfos);
  }

  @Test
  @Config(minSdk = Q)
  public void getUiccCardsInfo_returnsListAfterReset() {
    assertThat(telephonyManager.getUiccCardsInfo()).isEmpty();
  }

  @Test
  @Config(minSdk = O)
  public void shouldSetVisualVoicemailPackage() {
    shadowOf(telephonyManager).setVisualVoicemailPackageName("org.foo");

    assertThat(telephonyManager.getVisualVoicemailPackageName()).isEqualTo("org.foo");
  }

  @Test
  @Config(minSdk = P)
  public void canSetAndGetSignalStrength() {
    SignalStrength ss = Shadow.newInstanceOf(SignalStrength.class);
    shadowOf(telephonyManager).setSignalStrength(ss);
    assertThat(telephonyManager.getSignalStrength()).isEqualTo(ss);
  }

  @Test
  @Config(minSdk = P)
  public void shouldGiveSignalStrength() {
    PhoneStateListener listener = mock(PhoneStateListener.class);
    telephonyManager.listen(listener, LISTEN_SIGNAL_STRENGTHS);
    SignalStrength ss = Shadow.newInstanceOf(SignalStrength.class);

    shadowOf(telephonyManager).setSignalStrength(ss);

    verify(listener).onSignalStrengthsChanged(ss);
  }

  @Test
  @Config(minSdk = S)
  public void shouldGiveSignalStrength_toCallback() {
    TelephonyCallback callback =
        mock(
            TelephonyCallback.class, withSettings().extraInterfaces(SignalStrengthsListener.class));
    telephonyManager.registerTelephonyCallback(directExecutor(), callback);
    SignalStrength ss = Shadow.newInstanceOf(SignalStrength.class);

    shadowOf(telephonyManager).setSignalStrength(ss);

    verify((SignalStrengthsListener) callback).onSignalStrengthsChanged(ss);
  }

  @Test
  @Config(minSdk = O)
  public void setDataEnabledChangesIsDataEnabled() {
    shadowOf(telephonyManager).setDataEnabled(false);
    assertThat(telephonyManager.isDataEnabled()).isFalse();
    shadowOf(telephonyManager).setDataEnabled(true);
    assertThat(telephonyManager.isDataEnabled()).isTrue();
  }

  @Test
  @Config(minSdk = S)
  public void setDataEnabledForReasonChangesIsDataEnabledForReason() {
    int correctReason = TelephonyManager.DATA_ENABLED_REASON_POLICY;
    int incorrectReason = TelephonyManager.DATA_ENABLED_REASON_USER;

    assertThat(telephonyManager.isDataEnabledForReason(correctReason)).isTrue();
    assertThat(telephonyManager.isDataEnabledForReason(incorrectReason)).isTrue();

    telephonyManager.setDataEnabledForReason(correctReason, false);
    assertThat(telephonyManager.isDataEnabledForReason(correctReason)).isFalse();
    assertThat(telephonyManager.isDataEnabledForReason(incorrectReason)).isTrue();

    telephonyManager.setDataEnabledForReason(correctReason, true);
    assertThat(telephonyManager.isDataEnabledForReason(correctReason)).isTrue();
    assertThat(telephonyManager.isDataEnabledForReason(incorrectReason)).isTrue();
  }

  @Test
  public void setDataStateChangesDataState() {
    assertThat(telephonyManager.getDataState()).isEqualTo(TelephonyManager.DATA_DISCONNECTED);
    shadowOf(telephonyManager).setDataState(TelephonyManager.DATA_CONNECTING);
    assertThat(telephonyManager.getDataState()).isEqualTo(TelephonyManager.DATA_CONNECTING);
    shadowOf(telephonyManager).setDataState(TelephonyManager.DATA_CONNECTED);
    assertThat(telephonyManager.getDataState()).isEqualTo(TelephonyManager.DATA_CONNECTED);
  }

  @Test
  public void setDataActivityChangesDataActivity() {
    assertThat(telephonyManager.getDataActivity()).isEqualTo(TelephonyManager.DATA_ACTIVITY_NONE);
    shadowOf(telephonyManager).setDataActivity(TelephonyManager.DATA_ACTIVITY_IN);
    assertThat(telephonyManager.getDataActivity()).isEqualTo(TelephonyManager.DATA_ACTIVITY_IN);
    shadowOf(telephonyManager).setDataActivity(TelephonyManager.DATA_ACTIVITY_OUT);
    assertThat(telephonyManager.getDataActivity()).isEqualTo(TelephonyManager.DATA_ACTIVITY_OUT);
  }

  @Test
  @Config(minSdk = Q)
  public void setRttSupportedChangesIsRttSupported() {
    shadowOf(telephonyManager).setRttSupported(false);
    assertThat(telephonyManager.isRttSupported()).isFalse();
    shadowOf(telephonyManager).setRttSupported(true);
    assertThat(telephonyManager.isRttSupported()).isTrue();
  }

  @Test
  @Config(minSdk = M)
  public void setTtyModeSupportedChangesIsTtyModeSupported() {
    shadowOf(telephonyManager).setTtyModeSupported(false);
    assertThat(telephonyManager.isTtyModeSupported()).isFalse();
    shadowOf(telephonyManager).setTtyModeSupported(true);
    assertThat(telephonyManager.isTtyModeSupported()).isTrue();
  }

  @Test
  @Config(minSdk = M)
  public void
      isTtyModeSupported_shouldThrowSecurityExceptionWhenReadPhoneStatePermissionNotGranted()
          throws Exception {
    shadowOf(telephonyManager).setReadPhoneStatePermission(false);
    assertThrows(SecurityException.class, () -> telephonyManager.isTtyModeSupported());
  }

  @Test
  @Config(minSdk = N)
  public void hasCarrierPrivilegesWithSubId() {
    int subId = 3;
    assertThat(telephonyManager.hasCarrierPrivileges(subId)).isFalse();
    shadowOf(telephonyManager).setHasCarrierPrivileges(subId, true);
    assertThat(telephonyManager.hasCarrierPrivileges(subId)).isTrue();
  }

  @Test
  @Config(minSdk = O)
  public void sendDialerSpecialCode() {
    shadowOf(telephonyManager).sendDialerSpecialCode("1234");
    shadowOf(telephonyManager).sendDialerSpecialCode("123456");
    shadowOf(telephonyManager).sendDialerSpecialCode("1234");

    assertThat(shadowOf(telephonyManager).getSentDialerSpecialCodes())
        .containsExactly("1234", "123456", "1234")
        .inOrder();
  }

  @Test
  @Config(minSdk = M)
  public void setHearingAidCompatibilitySupportedChangesisHearingAidCompatibilitySupported() {
    shadowOf(telephonyManager).setHearingAidCompatibilitySupported(false);
    assertThat(telephonyManager.isHearingAidCompatibilitySupported()).isFalse();
    shadowOf(telephonyManager).setHearingAidCompatibilitySupported(true);
    assertThat(telephonyManager.isHearingAidCompatibilitySupported()).isTrue();
  }

  @Test
  @Config(minSdk = R)
  public void createTelephonyDisplayInfo_correctlyCreatesDisplayInfo() {
    TelephonyDisplayInfo displayInfo =
        (TelephonyDisplayInfo)
            createTelephonyDisplayInfo(NETWORK_TYPE_LTE, OVERRIDE_NETWORK_TYPE_LTE_CA);

    assertThat(displayInfo.getNetworkType()).isEqualTo(NETWORK_TYPE_LTE);
    assertThat(displayInfo.getOverrideNetworkType()).isEqualTo(OVERRIDE_NETWORK_TYPE_LTE_CA);
  }

  @Test
  @Config(minSdk = R)
  public void listen_doesNotNotifyListenerOfCurrentTelephonyDisplayInfoIfUninitialized() {
    PhoneStateListener listener = mock(PhoneStateListener.class);

    telephonyManager.listen(listener, LISTEN_DISPLAY_INFO_CHANGED);

    verifyNoMoreInteractions(listener);
  }

  @Test
  @Config(minSdk = S)
  public void register_doesNotNotifyCallbackOfCurrentTelephonyDisplayInfoIfUninitialized() {
    TelephonyCallback callback =
        mock(TelephonyCallback.class, withSettings().extraInterfaces(DisplayInfoListener.class));

    telephonyManager.registerTelephonyCallback(directExecutor(), callback);

    verifyNoMoreInteractions(callback);
  }

  @Test
  @Config(minSdk = R)
  public void listen_notifiesListenerOfCurrentTelephonyDisplayInfoIfInitialized() {
    PhoneStateListener listener = mock(PhoneStateListener.class);
    TelephonyDisplayInfo displayInfo =
        (TelephonyDisplayInfo)
            createTelephonyDisplayInfo(NETWORK_TYPE_EVDO_0, OVERRIDE_NETWORK_TYPE_NONE);
    shadowTelephonyManager.setTelephonyDisplayInfo(displayInfo);

    telephonyManager.listen(listener, LISTEN_DISPLAY_INFO_CHANGED);

    verify(listener, times(1)).onDisplayInfoChanged(displayInfo);
  }

  @Test
  @Config(minSdk = S)
  public void register_notifiesCallbackOfCurrentTelephonyDisplayInfoIfInitialized() {
    TelephonyCallback callback =
        mock(TelephonyCallback.class, withSettings().extraInterfaces(DisplayInfoListener.class));
    TelephonyDisplayInfo displayInfo =
        (TelephonyDisplayInfo)
            createTelephonyDisplayInfo(NETWORK_TYPE_EVDO_0, OVERRIDE_NETWORK_TYPE_NONE);
    shadowTelephonyManager.setTelephonyDisplayInfo(displayInfo);

    telephonyManager.registerTelephonyCallback(directExecutor(), callback);

    verify((DisplayInfoListener) callback, times(1)).onDisplayInfoChanged(displayInfo);
  }

  @Test
  @Config(minSdk = R)
  public void setTelephonyDisplayInfo_notifiesListeners() {
    PhoneStateListener listener = mock(PhoneStateListener.class);
    TelephonyDisplayInfo displayInfo =
        (TelephonyDisplayInfo)
            createTelephonyDisplayInfo(NETWORK_TYPE_LTE, OVERRIDE_NETWORK_TYPE_NR_NSA);
    telephonyManager.listen(listener, LISTEN_DISPLAY_INFO_CHANGED);

    shadowTelephonyManager.setTelephonyDisplayInfo(displayInfo);

    verify(listener, times(1)).onDisplayInfoChanged(displayInfo);
  }

  @Test
  @Config(minSdk = S)
  public void setTelephonyDisplayInfo_notifiesCallback() {
    TelephonyCallback callback =
        mock(TelephonyCallback.class, withSettings().extraInterfaces(DisplayInfoListener.class));
    TelephonyDisplayInfo displayInfo =
        (TelephonyDisplayInfo)
            createTelephonyDisplayInfo(NETWORK_TYPE_LTE, OVERRIDE_NETWORK_TYPE_NR_NSA);
    shadowTelephonyManager.setTelephonyDisplayInfo(displayInfo);

    telephonyManager.registerTelephonyCallback(directExecutor(), callback);

    verify((DisplayInfoListener) callback, times(1)).onDisplayInfoChanged(displayInfo);
  }

  @Test(expected = NullPointerException.class)
  @Config(minSdk = R)
  public void setTelephonyDisplayInfo_throwsNullPointerException() {
    shadowTelephonyManager.setTelephonyDisplayInfo(null);
  }

  @Test
  @Config(minSdk = R)
  public void isDataConnectionAllowed_returnsFalseByDefault() {
    assertThat(shadowTelephonyManager.isDataConnectionAllowed()).isFalse();
  }

  @Test
  @Config(minSdk = R)
  public void isDataConnectionAllowed_returnsFalseWhenSetToFalse() {
    shadowTelephonyManager.setIsDataConnectionAllowed(false);

    assertThat(shadowTelephonyManager.isDataConnectionAllowed()).isFalse();
  }

  @Test
  @Config(minSdk = R)
  public void isDataConnectionAllowed_returnsTrueWhenSetToTrue() {
    shadowTelephonyManager.setIsDataConnectionAllowed(true);

    assertThat(shadowTelephonyManager.isDataConnectionAllowed()).isTrue();
  }

  @Test
  @Config(minSdk = S)
  public void getCallComposerStatus_default() {
    assertThat(telephonyManager.getCallComposerStatus()).isEqualTo(0);
  }

  @Test
  @Config(minSdk = S)
  public void setCallComposerStatus() {
    telephonyManager.setCallComposerStatus(CALL_COMPOSER_STATUS_ON);

    assertThat(telephonyManager.getCallComposerStatus()).isEqualTo(CALL_COMPOSER_STATUS_ON);
  }

  @Test
  @Config(minSdk = S)
  public void getBootstrapAuthenticationCallback() {
    BootstrapAuthenticationCallback callback = mock(BootstrapAuthenticationCallback.class);

    telephonyManager.bootstrapAuthenticationRequest(
        TelephonyManager.APPTYPE_ISIM,
        Uri.parse("tel:test-uri"),
        new UaSecurityProtocolIdentifier.Builder().build(),
        true,
        directExecutor(),
        callback);

    assertThat(shadowTelephonyManager.getBootstrapAuthenticationCallback()).isEqualTo(callback);
  }

  @Test
  @Config(minSdk = S)
  public void setPhoneCapability_returnsPhoneCapability() {
    PhoneCapability phoneCapability = PhoneCapabilityFactory.create(2, 1, false, new int[0]);

    shadowTelephonyManager.setPhoneCapability(phoneCapability);

    assertThat(telephonyManager.getPhoneCapability()).isEqualTo(phoneCapability);
  }

  @Test
  @Config(minSdk = O)
  public void sendVisualVoicemailSms_shouldStoreLastSendSmsParameters() {
    telephonyManager.sendVisualVoicemailSms("destAddress", 0, "message", null);

    ShadowTelephonyManager.VisualVoicemailSmsParams params =
        shadowOf(telephonyManager).getLastSentVisualVoicemailSmsParams();

    assertThat(params.getDestinationAddress()).isEqualTo("destAddress");
    assertThat(params.getPort()).isEqualTo(0);
    assertThat(params.getText()).isEqualTo("message");
    assertThat(params.getSentIntent()).isNull();
  }

  @Test
  @Config(minSdk = O)
  public void setVisualVoicemailSmsFilterSettings_shouldStoreSettings() {
    VisualVoicemailSmsFilterSettings settings =
        new VisualVoicemailSmsFilterSettings.Builder()
            .setClientPrefix("clientPrefix")
            .setDestinationPort(100)
            .build();

    telephonyManager.setVisualVoicemailSmsFilterSettings(settings);

    assertThat(shadowOf(telephonyManager).getVisualVoicemailSmsFilterSettings())
        .isEqualTo(settings);
  }

  @Test
  @Config(minSdk = O)
  public void setVisualVoicemailSmsFilterSettings_setNullSettings_clearsSettings() {
    VisualVoicemailSmsFilterSettings settings =
        new VisualVoicemailSmsFilterSettings.Builder().build();

    telephonyManager.setVisualVoicemailSmsFilterSettings(settings);
    telephonyManager.setVisualVoicemailSmsFilterSettings(null);

    assertThat(shadowOf(telephonyManager).getVisualVoicemailSmsFilterSettings()).isNull();
  }

  @Test
  @Config(minSdk = Q)
  public void isEmergencyNumber_telephonyServiceUnavailable_throwsIllegalStateException() {
    ShadowServiceManager.setServiceAvailability(Context.TELEPHONY_SERVICE, false);

    assertThrows(IllegalStateException.class, () -> telephonyManager.isEmergencyNumber("911"));
  }

  @Test
  @Config(minSdk = O)
  public void
      getEmergencyCallbackMode_noReadPrivilegedPhoneStatePermission_throwsSecurityException() {
    shadowOf((Application) ApplicationProvider.getApplicationContext())
        .denyPermissions(permission.READ_PRIVILEGED_PHONE_STATE);

    assertThrows(SecurityException.class, () -> telephonyManager.getEmergencyCallbackMode());
  }

  @Test
  @Config(minSdk = O)
  public void getEmergencyCallback_wasSetToTrue_returnsTrue() {
    shadowTelephonyManager.setEmergencyCallbackMode(true);

    assertThat(telephonyManager.getEmergencyCallbackMode()).isTrue();
  }

  @Test
  @Config(minSdk = O)
  public void getEmergencyCallback_notSet_returnsFalse() {
    assertThat(telephonyManager.getEmergencyCallbackMode()).isFalse();
  }

  @Test
  @Config(minSdk = R)
  public void getEmergencyNumbersList_notSet_returnsEmptyList() {
    assertThat(telephonyManager.getEmergencyNumberList()).isEmpty();
  }

  @Test
  @Config(minSdk = R)
  public void getEmergencyNumbersList_wasSet_returnsCorrectList() throws Exception {
    EmergencyNumber emergencyNumber =
        EmergencyNumber.class
            .getConstructor(
                String.class,
                String.class,
                String.class,
                int.class,
                List.class,
                int.class,
                int.class)
            .newInstance(
                "911",
                "us",
                "30",
                EMERGENCY_NUMBER_SOURCE_DATABASE,
                ImmutableList.of(),
                EMERGENCY_SERVICE_CATEGORY_POLICE,
                EmergencyNumber.EMERGENCY_CALL_ROUTING_NORMAL);
    ShadowTelephonyManager.setEmergencyNumberList(
        ImmutableMap.of(0, ImmutableList.of(emergencyNumber)));
    assertThat(telephonyManager.getEmergencyNumberList().get(0)).containsExactly(emergencyNumber);
  }

  @Test
  @Config(minSdk = R)
  public void getSubscriptionIdForPhoneAccountHandle() {
    int subscriptionId = 123;
    PhoneAccountHandle phoneAccountHandle =
        new PhoneAccountHandle(
            new ComponentName(ApplicationProvider.getApplicationContext(), Object.class), "handle");
    shadowOf(telephonyManager)
        .setPhoneAccountHandleSubscriptionId(phoneAccountHandle, subscriptionId);
    assertEquals(subscriptionId, telephonyManager.getSubscriptionId(phoneAccountHandle));
  }

  @Test
  @Config(minSdk = R)
  public void getSubscriptionIdForPhoneAccountHandle_affectsAllInstances() {
    int subscriptionId = 123;
    PhoneAccountHandle phoneAccountHandle =
        new PhoneAccountHandle(
            new ComponentName(ApplicationProvider.getApplicationContext(), Object.class), "handle");
    shadowOf(telephonyManager)
        .setPhoneAccountHandleSubscriptionId(phoneAccountHandle, subscriptionId);
    assertEquals(subscriptionId, tmForSub5.getSubscriptionId(phoneAccountHandle));
  }

  @Test
  @Config(minSdk = R)
  public void getSubscriptionIdForPhoneAccountHandle_doesNotModifyCaller() {
    int subscriptionId = 123;
    PhoneAccountHandle phoneAccountHandle =
        new PhoneAccountHandle(
            new ComponentName(ApplicationProvider.getApplicationContext(), Object.class), "handle");
    shadowOf(telephonyManager)
        .setPhoneAccountHandleSubscriptionId(phoneAccountHandle, subscriptionId);
    assertEquals(5, tmForSub5.getSubscriptionId());
  }

  @Test
  @Config(minSdk = R)
  public void newInstance_alreadyKnowsSubId() {
    Context context = ApplicationProvider.getApplicationContext();
    Class<?>[] parameters = new Class<?>[] {Context.class, int.class};
    Object[] arguments = new Object[] {context, 123};
    TelephonyManager tm = Shadow.newInstance(TelephonyManager.class, parameters, arguments);

    assertThat(tm.getSubscriptionId()).isEqualTo(123);
  }

  @Test
  @Config(minSdk = Q)
  public void setDataRoamingEnabledChangesIsDataRoamingEnabled() {
    shadowOf(telephonyManager).setDataRoamingEnabled(false);
    assertThat(telephonyManager.isDataRoamingEnabled()).isFalse();
    shadowOf(telephonyManager).setDataRoamingEnabled(true);
    assertThat(telephonyManager.isDataRoamingEnabled()).isTrue();
  }

  @Test
  @Config(minSdk = Q)
  public void setCarrierRestrictionRules_changesCarrierRestrictionRules() {
    CarrierRestrictionRules carrierRestrictionRules = CarrierRestrictionRules.newBuilder().build();
    shadowOf(telephonyManager).setCarrierRestrictionRules(carrierRestrictionRules);

    assertThat(telephonyManager.getCarrierRestrictionRules()).isEqualTo(carrierRestrictionRules);
  }

  @Test()
  @Config(minSdk = Q)
  public void setCarrierRestrictionRules_throwsIllegalStateException() {
    assertThrows(
        IllegalStateException.class,
        () -> shadowTelephonyManager.setCarrierRestrictionRules(new Object()));
  }

  @Test
  @Config(minSdk = Q)
  public void rebootModem_rebootsModem() {
    shadowOf((Application) ApplicationProvider.getApplicationContext())
        .grantPermissions(permission.MODIFY_PHONE_STATE);

    shadowTelephonyManager.rebootModem();

    assertThat(shadowTelephonyManager.getModemRebootCount()).isEqualTo(1);
  }

  @Test()
  @Config(minSdk = Q)
  public void rebootModem_noModifyPhoneStatePermission_throwsSecurityException() {
    assertThrows(SecurityException.class, () -> shadowTelephonyManager.rebootModem());
  }
}
