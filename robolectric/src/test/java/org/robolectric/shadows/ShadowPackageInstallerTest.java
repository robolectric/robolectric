package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.O;
import static android.os.Build.VERSION_CODES.P;
import static android.os.Build.VERSION_CODES.S;
import static android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE;
import static com.google.common.collect.Iterables.getOnlyElement;
import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.robolectric.Shadows.shadowOf;
import static org.robolectric.shadows.ShadowLooper.shadowMainLooper;

import android.app.Application;
import android.app.PendingIntent;
import android.content.IIntentSender;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageInstaller;
import android.content.pm.PackageInstaller.PreapprovalDetails;
import android.content.pm.PackageInstaller.SessionParams;
import android.content.pm.PackageManager;
import android.content.pm.VersionedPackage;
import android.graphics.Bitmap;
import android.icu.util.ULocale;
import android.os.Handler;
import android.os.PersistableBundle;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.io.OutputStream;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.util.ReflectionHelpers;

@RunWith(AndroidJUnit4.class)
public class ShadowPackageInstallerTest {

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
  public void packageInstallerCreateSession() throws Exception {
    int sessionId = packageInstaller.createSession(createSessionParams("packageName"));

    assertThat(sessionId).isNotEqualTo(0);
  }

  @Config(sdk = P)
  @Test
  public void packageInstallerCreateSession_sessionInfoHasCorrectInstallerName() throws Exception {
    SessionParams params = createSessionParams("packageName");
    params.setInstallerPackageName("installerPackageName");
    int sessionId = packageInstaller.createSession(params);

    assertThat(packageInstaller.getSessionInfo(sessionId).installerPackageName)
        .isEqualTo("installerPackageName");
  }

  @Test
  public void packageInstallerCreateAndGetSession() throws Exception {
    // Act.
    int sessionId = packageInstaller.createSession(createSessionParams("packageName"));

    // Assert.
    List<PackageInstaller.SessionInfo> sessions;
    sessions = packageInstaller.getMySessions();
    assertThat(sessions).hasSize(1);
    assertThat(sessions.get(0).getSessionId()).isEqualTo(sessionId);

    sessions = packageInstaller.getAllSessions();
    assertThat(sessions).hasSize(1);
    assertThat(sessions.get(0).getSessionId()).isEqualTo(sessionId);

    assertThat(packageInstaller.getSessionInfo(sessionId)).isNotNull();
  }

  @Test
  public void packageInstallerCreateAndAbandonSession() throws Exception {
    PackageInstaller.SessionCallback mockCallback = mock(PackageInstaller.SessionCallback.class);
    packageInstaller.registerSessionCallback(mockCallback, new Handler());

    int sessionId = packageInstaller.createSession(createSessionParams("packageName"));
    shadowMainLooper().idle();

    PackageInstaller.SessionInfo sessionInfo = packageInstaller.getSessionInfo(sessionId);
    assertThat(sessionInfo.isActive()).isTrue();

    assertThat(sessionInfo.appPackageName).isEqualTo("packageName");

    packageInstaller.abandonSession(sessionId);
    shadowMainLooper().idle();

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

  @Test
  public void packageInstallerOpenSessionTwice_existingSessionReturned() throws Exception {
    int sessionId = packageInstaller.createSession(createSessionParams("packageName"));

    PackageInstaller.Session originalSession = packageInstaller.openSession(sessionId);

    assertThat(packageInstaller.openSession(sessionId)).isSameInstanceAs(originalSession);
  }

  @Test
  public void shouldBeNoSessionCallbacksOnRobolectricStartup() {
    assertThat(shadowOf(packageInstaller).getAllSessionCallbacks()).isEmpty();
  }

  @Test
  public void shouldBeSessionCallbacksWhenRegistered() {
    PackageInstaller.SessionCallback mockCallback = mock(PackageInstaller.SessionCallback.class);

    packageInstaller.registerSessionCallback(mockCallback);
    shadowMainLooper().idle();

    assertThat(shadowOf(packageInstaller).getAllSessionCallbacks()).containsExactly(mockCallback);
  }

  @Test(expected = SecurityException.class)
  public void packageInstallerOpenSession_nonExistantSessionThrowsException() throws Exception {
    packageInstaller.openSession(-99);
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
    shadowMainLooper().idle();
    verify(mockCallback).onCreated(sessionId);

    PackageInstaller.Session session = packageInstaller.openSession(sessionId);

    OutputStream outputStream = session.openWrite("filename", 0, 0);
    outputStream.close();

    session.abandon();
    shadowMainLooper().idle();

    assertThat(packageInstaller.getAllSessions()).isEmpty();

    verify(mockCallback).onFinished(sessionId, false);
  }

  @Test
  public void registerSessionCallback_sessionSucceeds() throws Exception {
    PackageInstaller.SessionCallback mockCallback = mock(PackageInstaller.SessionCallback.class);
    packageInstaller.registerSessionCallback(mockCallback, new Handler());
    int sessionId = packageInstaller.createSession(createSessionParams("packageName"));
    shadowMainLooper().idle();
    verify(mockCallback).onCreated(sessionId);

    PackageInstaller.Session session = packageInstaller.openSession(sessionId);

    OutputStream outputStream = session.openWrite("filename", 0, 0);
    outputStream.close();

    session.commit(new IntentSender(ReflectionHelpers.createNullProxy(IIntentSender.class)));

    shadowOf(packageInstaller).setSessionProgress(sessionId, 50.0f);
    shadowMainLooper().idle();
    verify(mockCallback).onProgressChanged(sessionId, 50.0f);

    verify(mockCallback).onFinished(sessionId, true);
  }

  @Test
  public void sessionActiveStateChanged_receivingOnActiveChangedCallback() throws Exception {
    PackageInstaller.SessionCallback mockCallback = mock(PackageInstaller.SessionCallback.class);
    packageInstaller.registerSessionCallback(mockCallback);
    int sessionId = packageInstaller.createSession(createSessionParams("packageName"));
    shadowMainLooper().idle();

    shadowOf(packageInstaller).setSessionActiveState(sessionId, false);
    shadowMainLooper().idle();

    verify(mockCallback).onActiveChanged(sessionId, false);
  }

  @Test
  public void unregisterSessionCallback() throws Exception {
    PackageInstaller.SessionCallback mockCallback = mock(PackageInstaller.SessionCallback.class);
    packageInstaller.registerSessionCallback(mockCallback, new Handler());
    packageInstaller.unregisterSessionCallback(mockCallback);

    int sessionId = packageInstaller.createSession(createSessionParams("packageName"));
    shadowMainLooper().idle();
    verify(mockCallback, never()).onCreated(sessionId);
  }

  @Config(sdk = UPSIDE_DOWN_CAKE)
  @Test
  public void sessionRequestUserPreapproval_noException() throws Exception {
    int sessionId = packageInstaller.createSession(createSessionParams("packageName"));
    PackageInstaller.Session session = packageInstaller.openSession(sessionId);
    PreapprovalDetails preapprovalDetails = createPreapprovalDetails();
    IntentSender intentSender =
        new IntentSender(ReflectionHelpers.createNullProxy(IIntentSender.class));

    session.requestUserPreapproval(preapprovalDetails, intentSender);
  }

  @Config(sdk = UPSIDE_DOWN_CAKE)
  @Test
  public void setPreapprovalDialogApproved() throws Exception {
    int sessionId = packageInstaller.createSession(createSessionParams("packageName"));
    PackageInstaller.Session session = packageInstaller.openSession(sessionId);
    session.requestUserPreapproval(createPreapprovalDetails(), createStatusReceiver());

    shadowOf(packageInstaller).setPreapprovalDialogApproved(sessionId);

    Intent intent = getOnlyBroadcastIntent();
    assertThat(intent.getExtra(PackageInstaller.EXTRA_STATUS))
        .isEqualTo(PackageInstaller.STATUS_SUCCESS);
    assertThat(intent.getExtra(PackageInstaller.EXTRA_PRE_APPROVAL)).isEqualTo(true);
    assertThat(intent.getExtra(PackageInstaller.EXTRA_SESSION_ID)).isEqualTo(sessionId);
  }

  @Config(sdk = UPSIDE_DOWN_CAKE)
  @Test
  public void setPreapprovalDialogDismissed() throws Exception {
    int sessionId = packageInstaller.createSession(createSessionParams("packageName"));
    PackageInstaller.Session session = packageInstaller.openSession(sessionId);
    session.requestUserPreapproval(createPreapprovalDetails(), createStatusReceiver());

    shadowOf(packageInstaller).setPreapprovalDialogDismissed(sessionId);

    Intent intent = getOnlyBroadcastIntent();
    assertThat(intent.getExtra(PackageInstaller.EXTRA_STATUS))
        .isEqualTo(PackageInstaller.STATUS_FAILURE_ABORTED);
    assertThat(intent.getExtra(PackageInstaller.EXTRA_PRE_APPROVAL)).isEqualTo(true);
    assertThat(intent.getExtra(PackageInstaller.EXTRA_SESSION_ID)).isEqualTo(sessionId);
  }

  @Config(sdk = UPSIDE_DOWN_CAKE)
  @Test
  public void setPreapprovalDialogDenied() throws Exception {
    int sessionId = packageInstaller.createSession(createSessionParams("packageName"));
    PackageInstaller.Session session = packageInstaller.openSession(sessionId);
    session.requestUserPreapproval(createPreapprovalDetails(), createStatusReceiver());

    shadowOf(packageInstaller).setPreapprovalDialogDenied(sessionId);

    Intent intent = getOnlyBroadcastIntent();
    assertThat(intent.getExtra(PackageInstaller.EXTRA_STATUS))
        .isEqualTo(PackageInstaller.STATUS_FAILURE);
    assertThat(intent.getExtra(PackageInstaller.EXTRA_PRE_APPROVAL)).isEqualTo(true);
    assertThat(intent.getExtra(PackageInstaller.EXTRA_SESSION_ID)).isEqualTo(sessionId);
  }

  @Config(sdk = UPSIDE_DOWN_CAKE)
  @Test
  public void sessionWriteAndReadMetadata() throws Exception {
    int sessionId = packageInstaller.createSession(createSessionParams("packageName"));
    PackageInstaller.Session session = packageInstaller.openSession(sessionId);
    PersistableBundle bundle = new PersistableBundle();
    bundle.putBoolean("Test key", true);

    session.setAppMetadata(bundle);

    assertThat(session.getAppMetadata()).isEqualTo(bundle);
  }

  @Test
  public void uninstallMaxVersion() throws Exception {
    packageInstaller.uninstall("packageName", /* statusReceiver */ null);

    assertThat(shadowOf(packageInstaller).getLastUninstalledVersion("packageName"))
        .isEqualTo(PackageManager.VERSION_CODE_HIGHEST);
    assertThat(shadowOf(packageInstaller).getLastUninstalledStatusReceiver("packageName")).isNull();
  }

  @Test
  public void uninstallMaxVersionWithStatusReceiver() throws Exception {
    IntentSender intentSender = createStatusReceiver();
    packageInstaller.uninstall("packageName", intentSender);

    assertThat(shadowOf(packageInstaller).getLastUninstalledVersion("packageName"))
        .isEqualTo(PackageManager.VERSION_CODE_HIGHEST);
    assertThat(shadowOf(packageInstaller).getLastUninstalledStatusReceiver("packageName"))
        .isEqualTo(intentSender);
  }

  @Config(sdk = O)
  @Test
  public void uninstallVersion() throws Exception {
    packageInstaller.uninstall(new VersionedPackage("packageName", 1), /* statusReceiver */ null);

    assertThat(shadowOf(packageInstaller).getLastUninstalledVersion("packageName")).isEqualTo(1);
    assertThat(shadowOf(packageInstaller).getLastUninstalledStatusReceiver("packageName")).isNull();
  }

  @Config(sdk = UPSIDE_DOWN_CAKE)
  @Test
  public void uninstallVersionWithFlags() throws Exception {
    packageInstaller.uninstall(
        new VersionedPackage("packageName", 1), /* flags= */ 0, /* statusReceiver= */ null);

    assertThat(shadowOf(packageInstaller).getLastUninstalledVersion("packageName")).isEqualTo(1);
    assertThat(shadowOf(packageInstaller).getLastUninstalledStatusReceiver("packageName")).isNull();
  }

  @Config(sdk = S)
  @Test
  public void uninstallExtistingPackage() throws Exception {
    packageInstaller.uninstallExistingPackage("packageName", /* IntentSender */ null);

    assertThat(shadowOf(packageInstaller).getLastUninstalledVersion("packageName"))
        .isEqualTo(PackageManager.VERSION_CODE_HIGHEST);
    assertThat(shadowOf(packageInstaller).getLastUninstalledStatusReceiver("packageName")).isNull();
  }

  @Test
  public void nothingUninstalled() throws Exception {
    assertThat(shadowOf(packageInstaller).getLastUninstalledVersion("packageName")).isNull();
    assertThat(shadowOf(packageInstaller).getLastUninstalledStatusReceiver("packageName")).isNull();
  }

  private static Intent getOnlyBroadcastIntent() {
    return getOnlyElement(
        shadowOf((Application) ApplicationProvider.getApplicationContext()).getBroadcastIntents());
  }

  private static IntentSender createStatusReceiver() {
    Intent broadcastIntent = new Intent("my.action");
    PendingIntent pendingIntent =
        PendingIntent.getBroadcast(
            ApplicationProvider.getApplicationContext(), 0, broadcastIntent, 0);
    return pendingIntent.getIntentSender();
  }

  private static PreapprovalDetails createPreapprovalDetails() {
    return new PreapprovalDetails.Builder()
        .setPackageName("packageName")
        .setIcon(Bitmap.createBitmap(48, 48, Bitmap.Config.ARGB_8888))
        .setLocale(ULocale.CANADA)
        .setLabel("app label")
        .build();
  }

  private static PackageInstaller.SessionParams createSessionParams(String appPackageName) {
    PackageInstaller.SessionParams params =
        new PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL);
    params.setAppPackageName(appPackageName);
    return params;
  }
}
