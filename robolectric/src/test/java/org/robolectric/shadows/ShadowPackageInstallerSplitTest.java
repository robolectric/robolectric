package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;

import android.content.pm.PackageInstaller;
import android.content.pm.PackageInstaller.SessionParams;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.io.OutputStream;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.shadows.ShadowPackageInstaller.ShadowSession;

/** Tests for split APK support in {@link ShadowPackageInstaller}. */
@RunWith(AndroidJUnit4.class)
public class ShadowPackageInstallerSplitTest {

  private PackageInstaller packageInstaller;

  @Before
  public void setUp() {
    packageInstaller =
        ApplicationProvider.getApplicationContext().getPackageManager().getPackageInstaller();
  }

  @Test
  public void openWrite_tracksSplitNames() throws Exception {
    int sessionId = packageInstaller.createSession(createSessionParams("com.example.app"));
    PackageInstaller.Session session = packageInstaller.openSession(sessionId);

    OutputStream baseStream = session.openWrite("base.apk", 0, -1);
    baseStream.close();
    OutputStream splitStream = session.openWrite("split_config.hdpi.apk", 0, -1);
    splitStream.close();
    OutputStream splitStream2 = session.openWrite("split_config.en.apk", 0, -1);
    splitStream2.close();

    ShadowSession shadowSession = Shadow.extract(session);
    assertThat(shadowSession.getWrittenSplitNames())
        .containsExactly("base.apk", "split_config.hdpi.apk", "split_config.en.apk")
        .inOrder();
  }

  @Test
  public void openWrite_emptySession_noSplitNames() throws Exception {
    int sessionId = packageInstaller.createSession(createSessionParams("com.example.app"));
    PackageInstaller.Session session = packageInstaller.openSession(sessionId);

    ShadowSession shadowSession = Shadow.extract(session);
    assertThat(shadowSession.getWrittenSplitNames()).isEmpty();
  }

  @Test
  public void openWrite_singleBaseApk_tracksName() throws Exception {
    int sessionId = packageInstaller.createSession(createSessionParams("com.example.app"));
    PackageInstaller.Session session = packageInstaller.openSession(sessionId);

    OutputStream stream = session.openWrite("base.apk", 0, -1);
    stream.close();

    ShadowSession shadowSession = Shadow.extract(session);
    assertThat(shadowSession.getWrittenSplitNames()).containsExactly("base.apk");
  }

  @Test
  public void openWrite_dynamicFeatureSplits_tracksNames() throws Exception {
    int sessionId = packageInstaller.createSession(createSessionParams("com.example.app"));
    PackageInstaller.Session session = packageInstaller.openSession(sessionId);

    OutputStream base = session.openWrite("base.apk", 0, -1);
    base.close();
    OutputStream feature1 = session.openWrite("split_feature_camera.apk", 0, -1);
    feature1.close();
    OutputStream feature2 = session.openWrite("split_feature_maps.apk", 0, -1);
    feature2.close();

    ShadowSession shadowSession = Shadow.extract(session);
    assertThat(shadowSession.getWrittenSplitNames())
        .containsExactly("base.apk", "split_feature_camera.apk", "split_feature_maps.apk")
        .inOrder();
  }

  @Test
  public void openWrite_multipleConfigSplits_typicalAabPattern() throws Exception {
    // Simulates a typical AAB install with base + config splits
    int sessionId = packageInstaller.createSession(createSessionParams("com.example.app"));
    PackageInstaller.Session session = packageInstaller.openSession(sessionId);

    String[] splits = {
      "base.apk", "split_config.arm64_v8a.apk", "split_config.en.apk", "split_config.xxhdpi.apk"
    };

    for (String split : splits) {
      OutputStream stream = session.openWrite(split, 0, -1);
      stream.close();
    }

    ShadowSession shadowSession = Shadow.extract(session);
    assertThat(shadowSession.getWrittenSplitNames()).hasSize(4);
    assertThat(shadowSession.getWrittenSplitNames())
        .containsExactly(
            "base.apk",
            "split_config.arm64_v8a.apk",
            "split_config.en.apk",
            "split_config.xxhdpi.apk")
        .inOrder();
  }

  private static SessionParams createSessionParams(String packageName) {
    SessionParams params = new SessionParams(SessionParams.MODE_FULL_INSTALL);
    params.setAppPackageName(packageName);
    return params;
  }
}
