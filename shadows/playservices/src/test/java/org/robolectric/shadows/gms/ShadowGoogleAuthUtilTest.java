package org.robolectric.shadows.gms;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import android.accounts.Account;
import android.content.Intent;
import com.google.android.gms.auth.AccountChangeEvent;
import com.google.android.gms.auth.GoogleAuthUtil;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.gms.ShadowGoogleAuthUtil.GoogleAuthUtilImpl;

/** Unit test for {@link ShadowGoogleAuthUtil}. */
@RunWith(RobolectricTestRunner.class)
@Config(shadows = {ShadowGoogleAuthUtil.class})
public class ShadowGoogleAuthUtilTest {

  private AutoCloseable mock;

  @Mock private GoogleAuthUtilImpl mockGoogleAuthUtil;

  @Before
  public void setup() {
    mock = MockitoAnnotations.openMocks(this);
    ShadowGoogleAuthUtil.reset();
  }

  @After
  public void tearDown() throws Exception {
    mock.close();
  }

  @Test
  public void getImplementation_defaultNotNull() {
    assertNotNull(ShadowGoogleAuthUtil.getImpl());
  }

  @Test
  public void provideImplementation_nullValueNotAllowed() {
    assertThrows(NullPointerException.class, () -> ShadowGoogleAuthUtil.provideImpl(null));
  }

  @Test
  public void getImplementation_shouldGetSet() {
    ShadowGoogleAuthUtil.provideImpl(mockGoogleAuthUtil);
    GoogleAuthUtilImpl googleAuthUtil = ShadowGoogleAuthUtil.getImpl();
    assertSame(googleAuthUtil, mockGoogleAuthUtil);
  }

  @Test
  public void canRedirectStaticMethodToImplementation() throws Exception {
    ShadowGoogleAuthUtil.provideImpl(mockGoogleAuthUtil);
    GoogleAuthUtil.clearToken(RuntimeEnvironment.getApplication(), "token");
    verify(mockGoogleAuthUtil, times(1)).clearToken(RuntimeEnvironment.getApplication(), "token");
  }

  @Test
  public void getAccountChangeEvents_defaultReturnEmptyList() throws Exception {
    List<AccountChangeEvent> list =
        GoogleAuthUtil.getAccountChangeEvents(RuntimeEnvironment.getApplication(), 0, "name");
    assertNotNull(list);
    assertEquals(0, list.size());
  }

  @Test
  public void getAccountId_defaultNotNull() throws Exception {
    assertNotNull(GoogleAuthUtil.getAccountId(RuntimeEnvironment.getApplication(), "name"));
  }

  @Test
  public void getToken_defaultNotNull() throws Exception {
    assertNotNull(GoogleAuthUtil.getToken(RuntimeEnvironment.getApplication(), "name", "scope"));
    assertNotNull(
        GoogleAuthUtil.getToken(RuntimeEnvironment.getApplication(), "name", "scope", null));
    assertNotNull(
        GoogleAuthUtil.getToken(
            RuntimeEnvironment.getApplication(), new Account("name", "robo"), "scope"));
    assertNotNull(
        GoogleAuthUtil.getToken(
            RuntimeEnvironment.getApplication(), new Account("name", "robo"), "scope", null));
    assertNotNull(
        GoogleAuthUtil.getTokenWithNotification(
            RuntimeEnvironment.getApplication(), "name", "scope", null));
    assertNotNull(
        GoogleAuthUtil.getTokenWithNotification(
            RuntimeEnvironment.getApplication(), "name", "scope", null, new Intent()));
    assertNotNull(
        GoogleAuthUtil.getTokenWithNotification(
            RuntimeEnvironment.getApplication(), "name", "scope", null, "authority", null));
    assertNotNull(
        GoogleAuthUtil.getTokenWithNotification(
            RuntimeEnvironment.getApplication(), new Account("name", "robo"), "scope", null));
    assertNotNull(
        GoogleAuthUtil.getTokenWithNotification(
            RuntimeEnvironment.getApplication(),
            new Account("name", "robo"),
            "scope",
            null,
            new Intent()));
    assertNotNull(
        GoogleAuthUtil.getTokenWithNotification(
            RuntimeEnvironment.getApplication(),
            new Account("name", "robo"),
            "scope",
            null,
            "authority",
            null));
  }

  @Test
  public void getTokenWithNotification_nullCallBackThrowIllegalArgumentException() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            GoogleAuthUtil.getTokenWithNotification(
                RuntimeEnvironment.getApplication(), "name", "scope", null, null));
  }

  @Test
  public void getTokenWithNotification_nullAuthorityThrowIllegalArgumentException() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            GoogleAuthUtil.getTokenWithNotification(
                RuntimeEnvironment.getApplication(), "name", "scope", null, null, null));
  }
}
