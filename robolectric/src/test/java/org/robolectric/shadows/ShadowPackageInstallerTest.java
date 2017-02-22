package org.robolectric.shadows;

import android.content.IIntentSender;
import android.content.IntentSender;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageInstaller;
import android.os.Build;
import android.os.Handler;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.TestRunners;
import org.robolectric.annotation.Config;
import org.robolectric.util.ReflectionHelpers;

import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;

import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.robolectric.Shadows.shadowOf;

@RunWith(TestRunners.MultiApiSelfTest.class)
@Config(minSdk = LOLLIPOP)
public class ShadowPackageInstallerTest {

  private static final String TEST_PACKAGE_NAME = "com.some.other.package";
  private static final String TEST_PACKAGE_LABEL = "My Little App";

  private PackageInstaller packageInstaller;

  @Before
  public void setUp() {
    packageInstaller = RuntimeEnvironment.application.getPackageManager().getPackageInstaller();
  }

  @Test
  public void getPackageInstaller() {
    PackageInfo packageInfo = new PackageInfo();
    packageInfo.packageName = TEST_PACKAGE_NAME;
    packageInfo.applicationInfo = new ApplicationInfo();
    packageInfo.applicationInfo.packageName = TEST_PACKAGE_NAME;
    packageInfo.applicationInfo.name = TEST_PACKAGE_LABEL;
    RuntimeEnvironment.getRobolectricPackageManager().addPackage(packageInfo);

    List<PackageInstaller.SessionInfo> allSessions = packageInstaller.getAllSessions();

    List<String> allPackageNames = new LinkedList<>();
    for (PackageInstaller.SessionInfo session : allSessions) {
      allPackageNames.add(session.appPackageName);
    }

    assertThat(allPackageNames).contains(TEST_PACKAGE_NAME);
  }

  @Test
  public void packageInstallerAndGetInstalledPackagesAreConsistent() {
    PackageInfo packageInfo = new PackageInfo();
    packageInfo.packageName = TEST_PACKAGE_NAME;
    packageInfo.applicationInfo = new ApplicationInfo();
    packageInfo.applicationInfo.packageName = TEST_PACKAGE_NAME;
    packageInfo.applicationInfo.name = TEST_PACKAGE_LABEL;
    RuntimeEnvironment.getRobolectricPackageManager().addPackage(packageInfo);

    List<PackageInstaller.SessionInfo> allSessions = packageInstaller.getAllSessions();

    assertThat(allSessions).hasSameSizeAs(RuntimeEnvironment.application.getPackageManager().getInstalledPackages(0));
  }

  @Test
  public void packageInstallerCreateSession() throws Exception {
    int sessionId = packageInstaller.createSession(createSessionParams("packageName"));

    PackageInstaller.SessionInfo sessionInfo = packageInstaller.getSessionInfo(sessionId);

    assertThat(sessionInfo.appPackageName).isEqualTo("packageName");

    packageInstaller.abandonSession(sessionId);

    assertThat(packageInstaller.getSessionInfo(sessionId)).isNull();
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

    session.commit(new IntentSender(ReflectionHelpers.createNullProxy(IIntentSender.class)));

    shadowOf(packageInstaller).setSessionFails(sessionId);
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

    shadowOf(packageInstaller).setSessionSucceeds(sessionId);
    verify(mockCallback).onFinished(sessionId, true);
  }

  private static PackageInstaller.SessionParams createSessionParams(String appPackageName) {
    PackageInstaller.SessionParams params = new PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL);
    params.setAppPackageName(appPackageName);
    return params;
  }
}
