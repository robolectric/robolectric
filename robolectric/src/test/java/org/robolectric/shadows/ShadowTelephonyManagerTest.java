package org.robolectric.shadows;

import android.telephony.CellLocation;
import android.telephony.CellInfo;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.TestRunners;

import static android.content.Context.TELEPHONY_SERVICE;
import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR1;
import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR2;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.robolectric.RuntimeEnvironment.*;
import static org.robolectric.Shadows.shadowOf;
import static org.robolectric.internal.Shadow.newInstanceOf;

@RunWith(TestRunners.MultiApiWithDefaults.class)
public class ShadowTelephonyManagerTest {

  private TelephonyManager manager;
  private ShadowTelephonyManager shadowManager;
  private MyPhoneStateListener listener;

  @Before
  public void setUp() throws Exception {
    manager = newInstanceOf(TelephonyManager.class);
    shadowManager = shadowOf(manager);

    listener = new MyPhoneStateListener();
  }

  @Test
  public void testListen() {
    manager.listen(listener, PhoneStateListener.LISTEN_CALL_STATE);
    assertThat(shadowManager.getListener()).isNotNull();
    assertThat((MyPhoneStateListener) shadowManager.getListener()).isSameAs(listener);
    assertThat(shadowManager.getEventFlags()).isEqualTo(PhoneStateListener.LISTEN_CALL_STATE);
  }

  @Test
  public void shouldGiveDeviceId() {
    String testId = "TESTING123";
    TelephonyManager telephonyManager = (TelephonyManager) application.getSystemService(TELEPHONY_SERVICE);
    shadowOf(telephonyManager).setDeviceId(testId);
    assertEquals(testId, telephonyManager.getDeviceId());
  }

  @Test
  public void shouldGiveNetworkOperatorName() {
    TelephonyManager telephonyManager = (TelephonyManager) application.getSystemService(TELEPHONY_SERVICE);
    ShadowTelephonyManager shadowTelephonyManager = shadowOf(telephonyManager);
    shadowTelephonyManager.setNetworkOperatorName("SomeOperatorName");
    assertEquals("SomeOperatorName", telephonyManager.getNetworkOperatorName());
  }

  @Test
  public void shouldGiveSimOperatorName() {
    TelephonyManager telephonyManager = (TelephonyManager) application.getSystemService(TELEPHONY_SERVICE);
    ShadowTelephonyManager shadowTelephonyManager = shadowOf(telephonyManager);
    shadowTelephonyManager.setSimOperatorName("SomeSimOperatorName");
    assertEquals("SomeSimOperatorName", telephonyManager.getSimOperatorName());
  }

  @Test
  public void shouldGiveNetworkType() {
    TelephonyManager telephonyManager = (TelephonyManager) application.getSystemService(TELEPHONY_SERVICE);
    ShadowTelephonyManager shadowTelephonyManager = shadowOf(telephonyManager);
    shadowTelephonyManager.setNetworkType(TelephonyManager.NETWORK_TYPE_CDMA);
    assertEquals(TelephonyManager.NETWORK_TYPE_CDMA, telephonyManager.getNetworkType());
  }

  @Test @Config(minSdk = JELLY_BEAN_MR1)
  public void shouldGiveAllCellInfo() {
    TelephonyManager telephonyManager = (TelephonyManager) application.getSystemService(TELEPHONY_SERVICE);
    ShadowTelephonyManager shadowTelephonyManager = shadowOf(telephonyManager);
    ArrayList<CellInfo> allCellInfo = new ArrayList<CellInfo>();
    shadowTelephonyManager.setAllCellInfo(allCellInfo);
    assertEquals(allCellInfo, telephonyManager.getAllCellInfo());
  }

  @Test
  public void shouldGiveNetworkCountryIso() {
    TelephonyManager telephonyManager = (TelephonyManager) application.getSystemService(TELEPHONY_SERVICE);
    ShadowTelephonyManager shadowTelephonyManager = shadowOf(telephonyManager);
    shadowTelephonyManager.setNetworkCountryIso("SomeIso");
    assertEquals("SomeIso", telephonyManager.getNetworkCountryIso());
  }

  @Test
  public void shouldGiveNetworkOperator() {
    TelephonyManager telephonyManager = (TelephonyManager) application.getSystemService(TELEPHONY_SERVICE);
    ShadowTelephonyManager shadowTelephonyManager = shadowOf(telephonyManager);
    shadowTelephonyManager.setNetworkOperator("SomeOperator");
    assertEquals("SomeOperator", telephonyManager.getNetworkOperator());
  }

  @Test
  public void shouldGiveLine1Number() {
    TelephonyManager telephonyManager = (TelephonyManager) application.getSystemService(TELEPHONY_SERVICE);
    ShadowTelephonyManager shadowTelephonyManager = shadowOf(telephonyManager);
    shadowTelephonyManager.setLine1Number("123-244-2222");
    assertEquals("123-244-2222", telephonyManager.getLine1Number());
  }

  @Test @Config(minSdk = JELLY_BEAN_MR2)
  public void shouldGiveGroupIdLevel1() {
    TelephonyManager telephonyManager = (TelephonyManager) application.getSystemService(TELEPHONY_SERVICE);
    ShadowTelephonyManager shadowTelephonyManager = shadowOf(telephonyManager);
    shadowTelephonyManager.setGroupIdLevel1("SomeGroupId");
    assertEquals("SomeGroupId", telephonyManager.getGroupIdLevel1());
  }

  @Test(expected = SecurityException.class)
  public void getDeviceId_shouldThrowSecurityExceptionWhenReadPhoneStatePermissionNotGranted() throws Exception {
    shadowManager.setReadPhoneStatePermission(false);
    manager.getDeviceId();
  }

  @Test
  public void shouldGivePhoneType() {
    TelephonyManager telephonyManager = (TelephonyManager) application.getSystemService(TELEPHONY_SERVICE);
    ShadowTelephonyManager shadowTelephonyManager = shadowOf(telephonyManager);
    shadowTelephonyManager.setPhoneType( TelephonyManager.PHONE_TYPE_CDMA );
    assertEquals(TelephonyManager.PHONE_TYPE_CDMA, telephonyManager.getPhoneType());
    shadowTelephonyManager.setPhoneType( TelephonyManager.PHONE_TYPE_GSM );
    assertEquals(TelephonyManager.PHONE_TYPE_GSM, telephonyManager.getPhoneType());
  }

  @Test
  public void shouldGiveCellLocation() {
    TelephonyManager telephonyManager = (TelephonyManager) application.getSystemService(TELEPHONY_SERVICE);
    assertThat(telephonyManager.getCellLocation()).isNull();
    CellLocation mockCellLocation = mock(CellLocation.class);
    shadowOf(telephonyManager).setCellLocation(mockCellLocation);
    assertThat(telephonyManager.getCellLocation()).isEqualTo(mockCellLocation);
  }

  private class MyPhoneStateListener extends PhoneStateListener {

  }
}
