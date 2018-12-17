package org.robolectric.android.internal;

import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.Shadows.shadowOf;
import static org.robolectric.android.internal.ParallelUniverse.registerBroadcastReceivers;

import android.app.Application;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.google.common.base.Charsets;
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
import org.robolectric.res.Fs;
import org.robolectric.shadows.ShadowApplication;
import org.robolectric.shadows.testing.TestApplication;

@RunWith(AndroidJUnit4.class)
public class ParallelUniverseCreateApplicationTest {

  @Rule public TemporaryFolder temporaryFolder = new TemporaryFolder();

  @Test(expected = RuntimeException.class)
  public void shouldThrowWhenManifestContainsBadApplicationClassName() throws Exception {
    ParallelUniverse.createApplication(
        newConfigWith("<application android:name=\"org.robolectric.BogusTestApplication\"/>)"), null);
  }

  @Test
  public void shouldReturnDefaultAndroidApplicationWhenManifestDeclaresNoAppName() throws Exception {
    assertThat(ParallelUniverse.createApplication(newConfigWith(""), null))
        .isInstanceOf(Application.class);
  }

  @Test
  public void shouldReturnSpecifiedApplicationWhenManifestDeclaresAppName() throws Exception {
    assertThat(ParallelUniverse.createApplication(
        newConfigWith("<application android:name=\"org.robolectric.shadows.testing.TestApplication\"/>"), null))
        .isInstanceOf(TestApplication.class);
  }

  @Test public void shouldAssignThePackageNameFromTheManifest() throws Exception {
    Application application = ApplicationProvider.getApplicationContext();

    assertThat(application.getPackageName()).isEqualTo("org.robolectric");
    assertThat(application).isInstanceOf(TestApplication.class);
  }

  @Test
  public void shouldRegisterReceiversFromTheManifest() throws Exception {
    // gross:
    shadowOf((Application) ApplicationProvider.getApplicationContext())
        .getRegisteredReceivers()
        .clear();

    AndroidManifest appManifest = newConfigWith(
        "<application>"
            + "    <receiver android:name=\"org.robolectric.fakes.ConfigTestReceiver\">"
            + "      <intent-filter>\n"
            + "        <action android:name=\"org.robolectric.ACTION_SUPERSET_PACKAGE\"/>\n"
            + "      </intent-filter>"
            + "    </receiver>"
            + "</application>");
    Application application = ParallelUniverse.createApplication(appManifest, null);
    shadowOf(application).callAttach(RuntimeEnvironment.systemContext);
    registerBroadcastReceivers(application, appManifest);

    List<ShadowApplication.Wrapper> receivers = shadowOf(application).getRegisteredReceivers();
    assertThat(receivers).hasSize(1);
    assertThat(receivers.get(0).intentFilter.matchAction("org.robolectric.ACTION_SUPERSET_PACKAGE")).isTrue();
  }

  @Test public void shouldDoTestApplicationNameTransform() throws Exception {
    assertThat(ParallelUniverse.getTestApplicationName(".Applicationz")).isEqualTo(".TestApplicationz");
    assertThat(ParallelUniverse.getTestApplicationName("Applicationz")).isEqualTo("TestApplicationz");
    assertThat(ParallelUniverse.getTestApplicationName("com.foo.Applicationz")).isEqualTo("com.foo.TestApplicationz");
  }

  @Test public void shouldLoadConfigApplicationIfSpecified() throws Exception {
    Application application = ParallelUniverse.createApplication(
        newConfigWith("<application android:name=\"" + "ClassNameToIgnore" + "\"/>"),
        new Config.Builder().setApplication(TestFakeApp.class).build());
    assertThat(application).isInstanceOf(TestFakeApp.class);
  }

  @Test public void shouldLoadConfigInnerClassApplication() throws Exception {
    Application application = ParallelUniverse.createApplication(
        newConfigWith("<application android:name=\"" + "ClassNameToIgnore" + "\"/>"),
        new Config.Builder().setApplication(TestFakeAppInner.class).build());
    assertThat(application).isInstanceOf(TestFakeAppInner.class);
  }

  @Test public void shouldLoadTestApplicationIfClassIsPresent() throws Exception {
    Application application = ParallelUniverse.createApplication(
        newConfigWith("<application android:name=\"" + FakeApp.class.getName() + "\"/>"), null);
    assertThat(application).isInstanceOf(TestFakeApp.class);
  }

  @Test public void whenNoAppManifestPresent_shouldCreateGenericApplication() throws Exception {
    assertThat(ParallelUniverse.createApplication(null, null)).isInstanceOf(Application.class);
  }

  /////////////////////////////

  public AndroidManifest newConfigWith(String contents) throws IOException {
    return newConfigWith("org.robolectric", contents);
  }

  private AndroidManifest newConfigWith(String packageName, String contents) throws IOException {
    String fileContents = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
        "<manifest xmlns:android=\"http://schemas.android.com/apk/res/android\"\n" +
        "          package=\"" + packageName + "\">\n" +
        "    " + contents + "\n" +
        "</manifest>\n";
    File f = temporaryFolder.newFile("whatever.xml");

    Files.asCharSink(f, Charsets.UTF_8).write(fileContents);
    return new AndroidManifest(Fs.newFile(f), null, null);
  }

  public static class TestFakeAppInner extends Application { }
}
