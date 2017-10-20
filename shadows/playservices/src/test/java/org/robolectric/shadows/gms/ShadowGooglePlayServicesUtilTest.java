package org.robolectric.shadows.gms;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import android.app.Activity;
import android.content.Context;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.gms.ShadowGooglePlayServicesUtil.GooglePlayServicesUtilImpl;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE, shadows = {ShadowGooglePlayServicesUtil.class})
public class ShadowGooglePlayServicesUtilTest {

  @Mock
  private GooglePlayServicesUtilImpl mockGooglePlayServicesUtil;

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Before
  public void setup() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void getImplementation_defaultNotNull() {
    assertNotNull(ShadowGooglePlayServicesUtil.getImpl());
  }

  @Test
  public void provideImplementation_nullValueNotAllowed() {
    thrown.expect(NullPointerException.class);
    ShadowGooglePlayServicesUtil.provideImpl(null);
  }

  @Test
  public void getImplementation_shouldGetSetted() {
    ShadowGooglePlayServicesUtil.provideImpl(mockGooglePlayServicesUtil);
    ShadowGooglePlayServicesUtil.GooglePlayServicesUtilImpl googlePlayServicesUtil =
        ShadowGooglePlayServicesUtil.getImpl();
    assertSame(googlePlayServicesUtil, mockGooglePlayServicesUtil);
  }

  @Test
  public void canRedirectStaticMethodToImplementation() {
    ShadowGooglePlayServicesUtil.provideImpl(mockGooglePlayServicesUtil);
    when(mockGooglePlayServicesUtil.isGooglePlayServicesAvailable(
        any(Context.class))).thenReturn(ConnectionResult.INTERNAL_ERROR);
    assertEquals(ConnectionResult.INTERNAL_ERROR,
        GooglePlayServicesUtil.isGooglePlayServicesAvailable(RuntimeEnvironment.application));
  }

  @Test
  public void getErrorString_goesToRealImpl() {
    assertEquals("SUCCESS", GooglePlayServicesUtil.getErrorString(ConnectionResult.SUCCESS));
    assertEquals("SERVICE_MISSING", GooglePlayServicesUtil
        .getErrorString(ConnectionResult.SERVICE_MISSING));
  }

  @Test
  public void getRemoteContext_defaultNotNull() {
    assertNotNull(GooglePlayServicesUtil.getRemoteContext(RuntimeEnvironment.application));
  }

  @Test
  public void getRemoteResource_defaultNotNull() {
    assertNotNull(GooglePlayServicesUtil.getRemoteResource(RuntimeEnvironment.application));
  }

  @Test
  public void getErrorDialog() {
    assertNotNull(GooglePlayServicesUtil.getErrorDialog(
        ConnectionResult.SERVICE_MISSING, new Activity(), 0));
    assertNull(GooglePlayServicesUtil.getErrorDialog(
        ConnectionResult.SUCCESS, new Activity(), 0));
    assertNotNull(GooglePlayServicesUtil.getErrorDialog(
        ConnectionResult.SERVICE_MISSING, new Activity(), 0, null));
    assertNull(GooglePlayServicesUtil.getErrorDialog(
        ConnectionResult.SUCCESS, new Activity(), 0, null));
  }

  @Test
  public void getErrorPendingIntent() {
    assertNotNull(GooglePlayServicesUtil.getErrorPendingIntent(
        ConnectionResult.SERVICE_MISSING, RuntimeEnvironment.application, 0));
    assertNull(GooglePlayServicesUtil.getErrorPendingIntent(
        ConnectionResult.SUCCESS, RuntimeEnvironment.application, 0));
  }

  @Test
  public void getOpenSourceSoftwareLicenseInfo_defaultNotNull() {
    assertNotNull(GooglePlayServicesUtil.getOpenSourceSoftwareLicenseInfo(
        RuntimeEnvironment.application));
  }

  @Test
  public void isGooglePlayServicesAvailable_defaultServiceMissing() {
    assertEquals(ConnectionResult.SERVICE_MISSING,
        GooglePlayServicesUtil.isGooglePlayServicesAvailable(RuntimeEnvironment.application));
  }
}
