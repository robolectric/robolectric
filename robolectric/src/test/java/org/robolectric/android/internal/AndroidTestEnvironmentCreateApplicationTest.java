package org.robolectric.android.internal;

import static com.google.common.truth.Truth.assertThat;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.fail;
import static org.robolectric.Shadows.shadowOf;
import static org.robolectric.android.internal.AndroidTestEnvironment.registerBroadcastReceivers;

import android.app.Application;
import android.content.pm.ApplicationInfo;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import java.util.List;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.robolectric.FakeApp;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.TestFakeApp;
import org.robolectric.annotation.Config;
import org.robolectric.manifest.AndroidManifest;
import org.robolectric.shadows.ShadowApplication;
import org.robolectric.shadows.testing.TestApplication;

@RunWith(AndroidJUnit4.class)
public class AndroidTestEnvironmentCreateApplicationTest {

  @Rule public TemporaryFolder temporaryFolder = new TemporaryFolder();

  @Test(expected = RuntimeException.class)
  public void shouldThrowWhenManifestContainsBadApplicationClassName() throws Exception {
    AndroidTestEnvironment.createApplication(
        newConfigWith("<application android:name=\"org.robolectric.BogusTestApplication\"/>)"),
        null, null);
  }

  @Test
  public void shouldReturnDefaultAndroidApplicationWhenManifestDeclaresNoAppName()
      throws Exception {
    Application application = AndroidTestEnvironment.createApplication(newConfigWith(""), null,
        new ApplicationInfo());
    assertThat(application.getClass()).isEqualTo(Application.class);
  }

  @Test
  public void shouldReturnSpecifiedApplicationWhenManifestDeclaresAppName() throws Exception {
    Application application =
        AndroidTestEnvironment.createApplication(
            newConfigWith(
                "<application android:name=\"org.robolectric.shadows.testing.TestApplication\"/>"),
            null, null);
    assertThat(application.getClass()).isEqualTo(TestApplication.class);
  }

  @Test
  public void shouldAssignThePackageNameFromTheManifest() {
    Application application = ApplicationProvider.getApplicationContext();

    assertThat(application.getPackageName()).isEqualTo("org.robolectric");
    assertThat(application.getClass()).isEqualTo(TestApplication.class);
  }

  @Test
  public void shouldRegisterReceiversFromTheManifest() throws Exception {
    // gross:
    shadowOf((Application) ApplicationProvider.getApplicationContext()).clearRegisteredReceivers();

    AndroidManifest appManifest =
        newConfigWith(
            "<application>"
                + "    <receiver android:name=\"org.robolectric.fakes.ConfigTestReceiver\">"
                + "      <intent-filter>\n"
                + "        <action android:name=\"org.robolectric.ACTION_SUPERSET_PACKAGE\"/>\n"
                + "      </intent-filter>"
                + "    </receiver>"
                + "</application>");
    Application application = AndroidTestEnvironment.createApplication(appManifest, null,
        new ApplicationInfo());
    shadowOf(application).callAttach(RuntimeEnvironment.systemContext);
    registerBroadcastReceivers(application, appManifest);

    List<ShadowApplication.Wrapper> receivers = shadowOf(application).getRegisteredReceivers();
    assertThat(receivers).hasSize(1);
    assertThat(receivers.get(0).intentFilter.matchAction("org.robolectric.ACTION_SUPERSET_PACKAGE"))
        .isTrue();
  }

  @Test
  public void shouldDoTestApplicationNameTransform() {
    assertThat(AndroidTestEnvironment.getTestApplicationName(".Applicationz"))
        .isEqualTo(".TestApplicationz");
    assertThat(AndroidTestEnvironment.getTestApplicationName("Applicationz"))
        .isEqualTo("TestApplicationz");
    assertThat(AndroidTestEnvironment.getTestApplicationName("com.foo.Applicationz"))
        .isEqualTo("com.foo.TestApplicationz");
  }

  @Test
  public void shouldLoadConfigApplicationIfSpecified() throws Exception {
    Application application =
        AndroidTestEnvironment.createApplication(
            newConfigWith("<application android:name=\"" + "ClassNameToIgnore" + "\"/>"),
            new Config.Builder().setApplication(TestFakeApp.class).build(), null);
    assertThat(application.getClass()).isEqualTo(TestFakeApp.class);
  }

  @Test
  public void shouldLoadConfigInnerClassApplication() throws Exception {
    Application application =
        AndroidTestEnvironment.createApplication(
            newConfigWith("<application android:name=\"" + "ClassNameToIgnore" + "\"/>"),
            new Config.Builder().setApplication(TestFakeAppInner.class).build(), null);
    assertThat(application.getClass()).isEqualTo(TestFakeAppInner.class);
  }

  @Test
  public void shouldLoadTestApplicationIfClassIsPresent() throws Exception {
    Application application =
        AndroidTestEnvironment.createApplication(
            newConfigWith("<application android:name=\"" + FakeApp.class.getName() + "\"/>"),
            null, null);
    assertThat(application.getClass()).isEqualTo(TestFakeApp.class);
  }

  @Test
  public void shouldLoadPackageApplicationIfClassIsPresent() {
    final ApplicationInfo applicationInfo = new ApplicationInfo();
    applicationInfo.className = TestApplication.class.getCanonicalName();
    Application application = AndroidTestEnvironment.createApplication(null, null, applicationInfo);
    assertThat(application.getClass()).isEqualTo(TestApplication.class);
  }

  @Test
  public void shouldLoadTestPackageApplicationIfClassIsPresent() {
    final ApplicationInfo applicationInfo = new ApplicationInfo();
    applicationInfo.className = FakeApp.class.getCanonicalName();
    Application application = AndroidTestEnvironment.createApplication(null, null, applicationInfo);
    assertThat(application.getClass()).isEqualTo(TestFakeApp.class);
  }

  @Test
  public void shouldThrowWhenPackageContainsBadApplicationClassName() {
    try {
      final ApplicationInfo applicationInfo = new ApplicationInfo();
      applicationInfo.className = "org.robolectric.BogusTestApplication";
      AndroidTestEnvironment.createApplication(null, null, applicationInfo);
      fail();
    } catch (RuntimeException expected) { }
  }

  @Test
  public void whenNoAppManifestPresent_shouldCreateGenericApplication() {
    Application application = AndroidTestEnvironment.createApplication(null, null,
        new ApplicationInfo());
    assertThat(application.getClass()).isEqualTo(Application.class);
  }

  /////////////////////////////

  public AndroidManifest newConfigWith(String contents) throws IOException {
    return newConfigWith("org.robolectric", contents);
  }

  private AndroidManifest newConfigWith(String packageName, String contents) throws IOException {
    String fileContents =
        "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
            + "<manifest xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
            + "          package=\""
            + packageName
            + "\">\n"
            + "    "
            + contents
            + "\n"
            + "</manifest>\n";
    File f = temporaryFolder.newFile("whatever.xml");

    Files.asCharSink(f, UTF_8).write(fileContents);
    return new AndroidManifest(f.toPath(), null, null);
  }

  public static class TestFakeAppInner extends Application {}
}
