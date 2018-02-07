package org.robolectric.shadows.gms;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import android.accounts.Account;
import android.content.Intent;
import com.google.android.gms.auth.AccountChangeEvent;
import com.google.android.gms.auth.GoogleAuthUtil;
import java.util.List;
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
import org.robolectric.shadows.gms.ShadowGoogleAuthUtil.GoogleAuthUtilImpl;

/**
 * Unit test for {@link ShadowGoogleAuthUtil}.
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE, shadows = {ShadowGoogleAuthUtil.class})
public class ShadowGoogleAuthUtilTest {

  @Mock private GoogleAuthUtilImpl mockGoogleAuthUtil;

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Before
  public void setup() {
    MockitoAnnotations.initMocks(this);
    ShadowGoogleAuthUtil.reset();
  }

  @Test
  public void getImplementation_defaultNotNull() {
    assertNotNull(ShadowGoogleAuthUtil.getImpl());
  }

  @Test
  public void provideImplementation_nullValueNotAllowed() {
    thrown.expect(NullPointerException.class);
    ShadowGoogleAuthUtil.provideImpl(null);
  }

  @Test
  public void getImplementation_shouldGetSetted() {
    ShadowGoogleAuthUtil.provideImpl(mockGoogleAuthUtil);
    GoogleAuthUtilImpl googleAuthUtil = ShadowGoogleAuthUtil.getImpl();
    assertSame(googleAuthUtil, mockGoogleAuthUtil);
  }

  @Test
  public void canRedirectStaticMethodToImplementation() throws Exception {
    ShadowGoogleAuthUtil.provideImpl(mockGoogleAuthUtil);
    GoogleAuthUtil.clearToken(RuntimeEnvironment.application, "token");
    verify(mockGoogleAuthUtil, times(1)).clearToken(RuntimeEnvironment.application, "token");
  }

  @Test
  public void getAccountChangeEvents_defaultReturnEmptyList() throws Exception {
    List<AccountChangeEvent> list = GoogleAuthUtil.getAccountChangeEvents(
        RuntimeEnvironment.application, 0, "name");
    assertNotNull(list);
    assertEquals(0, list.size());
  }

  @Test
  public void getAccountId_defaultNotNull() throws Exception {
    assertNotNull(GoogleAuthUtil.getAccountId(RuntimeEnvironment.application, "name"));
  }

  @Test
  public void getToken_defaultNotNull() throws Exception {
    assertNotNull(GoogleAuthUtil.getToken(RuntimeEnvironment.application, "name", "scope"));
    assertNotNull(GoogleAuthUtil.getToken(RuntimeEnvironment.application, "name", "scope",
        null));
    assertNotNull(GoogleAuthUtil.getToken(RuntimeEnvironment.application, new Account("name",
        "robo"), "scope"));
    assertNotNull(GoogleAuthUtil.getToken(RuntimeEnvironment.application, new Account("name",
        "robo"), "scope", null));
    assertNotNull(GoogleAuthUtil.getTokenWithNotification(RuntimeEnvironment.application,
        "name", "scope", null));
    assertNotNull(GoogleAuthUtil.getTokenWithNotification(RuntimeEnvironment.application,
        "name", "scope", null, new Intent()));
    assertNotNull(GoogleAuthUtil.getTokenWithNotification(RuntimeEnvironment.application,
        "name", "scope", null, "authority", null));
    assertNotNull(GoogleAuthUtil.getTokenWithNotification(RuntimeEnvironment.application,
        new Account("name", "robo"), "scope", null));
    assertNotNull(GoogleAuthUtil.getTokenWithNotification(RuntimeEnvironment.application,
        new Account("name", "robo"), "scope", null, new Intent()));
    assertNotNull(GoogleAuthUtil.getTokenWithNotification(RuntimeEnvironment.application,
        new Account("name", "robo"), "scope", null, "authority", null));
  }

  @Test
  public void getTokenWithNotification_nullCallBackThrowIllegalArgumentException()
      throws Exception {
    thrown.expect(IllegalArgumentException.class);
    GoogleAuthUtil.getTokenWithNotification(RuntimeEnvironment.application, "name", "scope",
        null, null);
  }

  @Test
  public void getTokenWithNotification_nullAuthorityThrowIllegalArgumentException()
      throws Exception {
    thrown.expect(IllegalArgumentException.class);
    assertNotNull(GoogleAuthUtil.getTokenWithNotification(RuntimeEnvironment.application,
        "name", "scope", null, null, null));
  }
}
