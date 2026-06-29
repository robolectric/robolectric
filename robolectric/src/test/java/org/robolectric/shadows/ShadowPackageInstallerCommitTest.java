package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;

import android.content.IIntentSender;
import android.content.IntentSender;
import android.content.pm.PackageInfo;
import android.content.pm.PackageInstaller;
import android.content.pm.PackageInstaller.SessionParams;
import android.os.Build.VERSION_CODES;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.io.OutputStream;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.shadows.ShadowPackageInstaller.ShadowSession;
import org.robolectric.util.ReflectionHelpers;

/**
 * Tests for PackageInstaller Session commit integration with {@link ShadowPackageManager} in {@link
 * ShadowPackageInstaller}.
 */
@RunWith(AndroidJUnit4.class)
@Config(minSdk = VERSION_CODES.O)
public class ShadowPackageInstallerCommitTest {

  private static final String PACKAGE_NAME = "com.example.splitapp";

  private PackageInstaller packageInstaller;
  private android.content.pm.PackageManager packageManager;

  @Before
  public void setUp() {
    packageManager = ApplicationProvider.getApplicationContext().getPackageManager();
    packageInstaller = packageManager.getPackageInstaller();
  }

  @Test
  public void commit_setsIsCommittedTrue() throws Exception {
    int sessionId = packageInstaller.createSession(createSessionParams(PACKAGE_NAME));
    PackageInstaller.Session session = packageInstaller.openSession(sessionId);
    ShadowSession shadowSession = Shadow.extract(session);

    assertThat(shadowSession.isCommitted()).isFalse();
    session.commit(nullSender());

    assertThat(shadowSession.isCommitted()).isTrue();
  }

  @Test
  public void commit_withBaseAndSplitApks_installsPackageWithSplits() throws Exception {
    int sessionId = packageInstaller.createSession(createSessionParams(PACKAGE_NAME));
    PackageInstaller.Session session = packageInstaller.openSession(sessionId);

    // Write base APK (should not become a split name).
    OutputStream base = session.openWrite("base.apk", 0, -1);
    base.close();
    // Write two feature splits.
    OutputStream feature = session.openWrite("split_feature_camera.apk", 0, -1);
    feature.close();
    OutputStream config = session.openWrite("split_config.xxhdpi.apk", 0, -1);
    config.close();

    session.commit(nullSender());

    PackageInfo info = packageManager.getPackageInfo(PACKAGE_NAME, 0);
    assertThat(info).isNotNull();
    assertThat(info.splitNames)
        .asList()
        .containsAtLeast("split_feature_camera", "split_config.xxhdpi");
  }

  @Test
  public void commit_withBaseApkOnly_installsPackageWithNoSplits() throws Exception {
    int sessionId = packageInstaller.createSession(createSessionParams(PACKAGE_NAME));
    PackageInstaller.Session session = packageInstaller.openSession(sessionId);

    OutputStream base = session.openWrite("base.apk", 0, -1);
    base.close();

    session.commit(nullSender());

    PackageInfo info = packageManager.getPackageInfo(PACKAGE_NAME, 0);
    assertThat(info).isNotNull();
    // base.apk is not a split; splitNames should be null or empty.
    if (info.splitNames != null) {
      assertThat(info.splitNames).isEmpty();
    }
  }

  @Test
  public void commit_existingPackage_addsSplitsToPackage() throws Exception {
    // Pre-install the package without splits.
    PackageInfo existing = new PackageInfo();
    existing.packageName = PACKAGE_NAME;
    existing.applicationInfo = new android.content.pm.ApplicationInfo();
    existing.applicationInfo.packageName = PACKAGE_NAME;
    shadowOf(packageManager).installPackage(existing);

    // Now commit a session that adds a split.
    int sessionId = packageInstaller.createSession(createSessionParams(PACKAGE_NAME));
    PackageInstaller.Session session = packageInstaller.openSession(sessionId);

    OutputStream base = session.openWrite("base.apk", 0, -1);
    base.close();
    OutputStream feature = session.openWrite("split_feature_maps.apk", 0, -1);
    feature.close();

    session.commit(nullSender());

    PackageInfo info = packageManager.getPackageInfo(PACKAGE_NAME, 0);
    assertThat(info.splitNames).asList().contains("split_feature_maps");
  }

  @Test
  public void commit_emptySession_doesNotInstallPackage() throws Exception {
    // A session with no written APKs should not create a new package entry.
    int sessionId = packageInstaller.createSession(createSessionParams(PACKAGE_NAME));
    PackageInstaller.Session session = packageInstaller.openSession(sessionId);

    session.commit(nullSender());

    try {
      packageManager.getPackageInfo(PACKAGE_NAME, 0);
      // If it didn't throw, the package was installed unexpectedly.
      throw new AssertionError("Expected NameNotFoundException");
    } catch (android.content.pm.PackageManager.NameNotFoundException expected) {
      // correct: an empty session should not register the package.
    }
  }

  @Test
  public void commit_withBaseMasterApk_treatedAsBaseNotSplit() throws Exception {
    int sessionId = packageInstaller.createSession(createSessionParams(PACKAGE_NAME));
    PackageInstaller.Session session = packageInstaller.openSession(sessionId);

    OutputStream base = session.openWrite("base-master.apk", 0, -1);
    base.close();
    OutputStream feature = session.openWrite("feature_kotlin-master.apk", 0, -1);
    feature.close();

    session.commit(nullSender());

    PackageInfo info = packageManager.getPackageInfo(PACKAGE_NAME, 0);
    assertThat(info).isNotNull();
    assertThat(info.splitNames).asList().contains("feature_kotlin-master");
    assertThat(info.splitNames).asList().doesNotContain("base-master");
  }

  @Test
  public void writtenSplitNames_preservedAfterCommit() throws Exception {
    int sessionId = packageInstaller.createSession(createSessionParams(PACKAGE_NAME));
    PackageInstaller.Session session = packageInstaller.openSession(sessionId);
    ShadowSession shadowSession = Shadow.extract(session);

    OutputStream out = session.openWrite("base.apk", 0, -1);
    out.close();
    OutputStream split = session.openWrite("split_feature_camera.apk", 0, -1);
    split.close();

    session.commit(nullSender());

    assertThat(shadowSession.getWrittenSplitNames())
        .containsExactly("base.apk", "split_feature_camera.apk")
        .inOrder();
  }

  private static SessionParams createSessionParams(String packageName) {
    SessionParams params = new SessionParams(SessionParams.MODE_FULL_INSTALL);
    params.setAppPackageName(packageName);
    return params;
  }

  private static IntentSender nullSender() {
    return new IntentSender(ReflectionHelpers.createNullProxy(IIntentSender.class));
  }

  private static ShadowPackageManager shadowOf(android.content.pm.PackageManager pm) {
    return (ShadowPackageManager) Shadow.extract(pm);
  }
}
