package org.robolectric.shadows;

import static android.content.Context.TELEPHONY_SERVICE;
import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR1;
import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR2;
import static android.telephony.PhoneStateListener.LISTEN_CALL_STATE;
import static android.telephony.PhoneStateListener.LISTEN_CELL_INFO;
import static android.telephony.PhoneStateListener.LISTEN_CELL_LOCATION;
import static android.telephony.TelephonyManager.CALL_STATE_IDLE;
import static android.telephony.TelephonyManager.CALL_STATE_OFFHOOK;
import static android.telephony.TelephonyManager.CALL_STATE_RINGING;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.robolectric.RuntimeEnvironment.application;
import static org.robolectric.Shadows.shadowOf;

import android.os.Build.VERSION;
import android.telephony.CellInfo;
import android.telephony.CellLocation;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import java.util.Collections;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
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
  public void shouldGiveNetworkOperatorName() {
    shadowTelephonyManager.setNetworkOperatorName("SomeOperatorName");
    assertEquals("SomeOperatorName", telephonyManager.getNetworkOperatorName());
  }

  @Test
  public void shouldGiveSimOperatorName() {
    shadowTelephonyManager.setSimOperatorName("SomeSimOperatorName");
    assertEquals("SomeSimOperatorName", telephonyManager.getSimOperatorName());
  }

  @Test
  public void shouldGiveNetworkType() {
    shadowTelephonyManager.setNetworkType(TelephonyManager.NETWORK_TYPE_CDMA);
    assertEquals(TelephonyManager.NETWORK_TYPE_CDMA, telephonyManager.getNetworkType());
  }

  @Test @Config(minSdk = JELLY_BEAN_MR1)
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

  @Test @Config(minSdk = JELLY_BEAN_MR2)
  public void shouldGiveGroupIdLevel1() {
    shadowTelephonyManager.setGroupIdLevel1("SomeGroupId");
    assertEquals("SomeGroupId", telephonyManager.getGroupIdLevel1());
  }

  @Test(expected = SecurityException.class)
  public void getDeviceId_shouldThrowSecurityExceptionWhenReadPhoneStatePermissionNotGranted() throws Exception {
    shadowTelephonyManager.setReadPhoneStatePermission(false);
    telephonyManager.getDeviceId();
  }

  @Test
  public void shouldGivePhoneType() {
    shadowTelephonyManager.setPhoneType( TelephonyManager.PHONE_TYPE_CDMA );
    assertEquals(TelephonyManager.PHONE_TYPE_CDMA, telephonyManager.getPhoneType());
    shadowTelephonyManager.setPhoneType( TelephonyManager.PHONE_TYPE_GSM );
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
}
