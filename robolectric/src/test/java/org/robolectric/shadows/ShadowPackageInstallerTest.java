package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.robolectric.Shadows.shadowOf;

import android.content.IIntentSender;
import android.content.IntentSender;
import android.content.pm.PackageInstaller;
import android.os.Handler;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.io.OutputStream;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.util.ReflectionHelpers;

@RunWith(AndroidJUnit4.class)
@Config(minSdk = LOLLIPOP)
public class ShadowPackageInstallerTest {

  private static final String TEST_PACKAGE_NAME = "com.some.other.package";
  private static final String TEST_PACKAGE_LABEL = "My Little App";

  private PackageInstaller packageInstaller;

  @Before
  public void setUp() {
    packageInstaller =
        ApplicationProvider.getApplicationContext().getPackageManager().getPackageInstaller();
  }

  @Test
  public void shouldBeNoInProcessSessionsOnRobolectricStartup() {
    assertThat(packageInstaller.getAllSessions()).isEmpty();
  }

  @Test
  public void packageInstallerCreateAndAbandonSession() throws Exception {
    PackageInstaller.SessionCallback mockCallback = mock(PackageInstaller.SessionCallback.class);
    packageInstaller.registerSessionCallback(mockCallback, new Handler());

    int sessionId = packageInstaller.createSession(createSessionParams("packageName"));

    PackageInstaller.SessionInfo sessionInfo = packageInstaller.getSessionInfo(sessionId);
    assertThat(sessionInfo.isActive()).isTrue();

    assertThat(sessionInfo.appPackageName).isEqualTo("packageName");

    packageInstaller.abandonSession(sessionId);

    assertThat(packageInstaller.getSessionInfo(sessionId)).isNull();
    assertThat(packageInstaller.getAllSessions()).isEmpty();

    verify(mockCallback).onCreated(sessionId);
    verify(mockCallback).onFinished(sessionId, false);
  }

  @Test
  public void packageInstallerOpenSession() throws Exception {
    int sessionId = packageInstaller.createSession(createSessionParams("packageName"));

    PackageInstaller.Session session = packageInstaller.openSession(sessionId);

    assertThat(session).isNotNull();
  }

  @Test(expected = SecurityException.class)
  public void packageInstallerOpenSession_nonExistantSessionThrowsException() throws Exception {
    PackageInstaller.Session session = packageInstaller.openSession(-99);
  }

  @Test // TODO: Initial implementation has a no-op OutputStream - complete this implementation.
  public void sessionOpenWriteDoesNotThrowException() throws Exception {
    int sessionId = packageInstaller.createSession(createSessionParams("packageName"));
    PackageInstaller.Session session = packageInstaller.openSession(sessionId);

    OutputStream filename = session.openWrite("filename", 0, 0);
    filename.write(10);
  }

  @Test
  public void sessionCommitSession_streamProperlyClosed() throws Exception {
    int sessionId = packageInstaller.createSession(createSessionParams("packageName"));
    PackageInstaller.Session session = packageInstaller.openSession(sessionId);

    OutputStream outputStream = session.openWrite("filename", 0, 0);
    outputStream.close();

    session.commit(new IntentSender(ReflectionHelpers.createNullProxy(IIntentSender.class)));
  }

  @Test(expected = SecurityException.class)
  public void sessionCommitSession_streamStillOpen() throws Exception {
    int sessionId = packageInstaller.createSession(createSessionParams("packageName"));
    PackageInstaller.Session session = packageInstaller.openSession(sessionId);

    session.openWrite("filename", 0, 0);

    session.commit(new IntentSender(ReflectionHelpers.createNullProxy(IIntentSender.class)));
  }

  @Test
  public void registerSessionCallback_sessionFails() throws Exception {
    PackageInstaller.SessionCallback mockCallback = mock(PackageInstaller.SessionCallback.class);
    packageInstaller.registerSessionCallback(mockCallback, new Handler());
    int sessionId = packageInstaller.createSession(createSessionParams("packageName"));
    verify(mockCallback).onCreated(sessionId);

    PackageInstaller.Session session = packageInstaller.openSession(sessionId);

    OutputStream outputStream = session.openWrite("filename", 0, 0);
    outputStream.close();

    session.abandon();

    assertThat(packageInstaller.getAllSessions()).isEmpty();

    verify(mockCallback).onFinished(sessionId, false);
  }

  @Test
  public void registerSessionCallback_sessionSucceeds() throws Exception {
    PackageInstaller.SessionCallback mockCallback = mock(PackageInstaller.SessionCallback.class);
    packageInstaller.registerSessionCallback(mockCallback, new Handler());
    int sessionId = packageInstaller.createSession(createSessionParams("packageName"));
    verify(mockCallback).onCreated(sessionId);

    PackageInstaller.Session session = packageInstaller.openSession(sessionId);

    OutputStream outputStream = session.openWrite("filename", 0, 0);
    outputStream.close();

    session.commit(new IntentSender(ReflectionHelpers.createNullProxy(IIntentSender.class)));

    shadowOf(packageInstaller).setSessionProgress(sessionId, 50.0f);
    verify(mockCallback).onProgressChanged(sessionId, 50.0f);

    verify(mockCallback).onFinished(sessionId, true);
  }

  private static PackageInstaller.SessionParams createSessionParams(String appPackageName) {
    PackageInstaller.SessionParams params = new PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL);
    params.setAppPackageName(appPackageName);
    return params;
  }
}
