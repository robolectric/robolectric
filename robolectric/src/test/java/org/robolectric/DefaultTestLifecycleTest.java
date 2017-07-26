package org.robolectric;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;
import static org.robolectric.Shadows.shadowOf;
import static org.robolectric.util.TestUtil.newConfig;

import android.app.Application;
import android.content.Intent;
import android.content.pm.PackageManager;
import com.google.common.base.Charsets;
import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import java.util.List;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.manifest.AndroidManifest;
import org.robolectric.res.Fs;
import org.robolectric.shadows.ShadowApplication;

@RunWith(TestRunners.SelfTest.class)
public class DefaultTestLifecycleTest {

  @Rule public TemporaryFolder temporaryFolder = new TemporaryFolder();
  private DefaultTestLifecycle defaultTestLifecycle = new DefaultTestLifecycle();

  @Test(expected = RuntimeException.class)
  public void shouldThrowWhenManifestContainsBadApplicationClassName() throws Exception {
    defaultTestLifecycle.createApplication(null,
        newConfigWith("<application android:name=\"org.robolectric.BogusTestApplication\"/>)"), null);
  }

  @Test
  public void shouldReturnDefaultAndroidApplicationWhenManifestDeclaresNoAppName() throws Exception {
    assertThat(defaultTestLifecycle.createApplication(null, newConfigWith(""), null))
        .isExactlyInstanceOf(Application.class);
  }

  @Test
  public void shouldReturnSpecifiedApplicationWhenManifestDeclaresAppName() throws Exception {
    assertThat(defaultTestLifecycle.createApplication(null,
        newConfigWith("<application android:name=\"org.robolectric.TestApplication\"/>"), null))
        .isExactlyInstanceOf(TestApplication.class);
  }

  @Config(manifest = "TestAndroidManifestWithReceiversCustomPackage.xml")
  @Test public void shouldAssignThePackageNameFromTheManifest() throws Exception {
    Application application = RuntimeEnvironment.application;

    assertThat(application.getPackageName()).isEqualTo("org.robolectric.mypackage");
    assertThat(application).isExactlyInstanceOf(Application.class);
  }

  @Test
  public void shouldRegisterReceiversFromTheManifest() throws Exception {
    AndroidManifest appManifest = newConfig("TestAndroidManifestWithReceivers.xml");
    Application application = defaultTestLifecycle.createApplication(null, appManifest, null);
    shadowOf(application).bind(appManifest);

    List<ShadowApplication.Wrapper> receivers = shadowOf(application).getRegisteredReceivers();
    assertThat(receivers.size()).isEqualTo(5);
    assertTrue(receivers.get(0).intentFilter.matchAction("org.robolectric.ACTION1"));
  }

  @Config(manifest = "TestAndroidManifestForActivities.xml")
  @Test public void shouldRegisterActivitiesFromManifestInPackageManager() throws Exception {
    Application application = RuntimeEnvironment.application;

    PackageManager packageManager = application.getPackageManager();
    assertThat(packageManager.resolveActivity(new Intent("org.robolectric.shadows.TestActivity"), -1)).isNotNull();
    assertThat(packageManager.resolveActivity(new Intent("org.robolectric.shadows.TestActivity2"), -1)).isNotNull();
  }

  @Test public void shouldDoTestApplicationNameTransform() throws Exception {
    assertThat(defaultTestLifecycle.getTestApplicationName(".Applicationz")).isEqualTo(".TestApplicationz");
    assertThat(defaultTestLifecycle.getTestApplicationName("Applicationz")).isEqualTo("TestApplicationz");
    assertThat(defaultTestLifecycle.getTestApplicationName("com.foo.Applicationz")).isEqualTo("com.foo.TestApplicationz");
  }

  @Test public void shouldLoadConfigApplicationIfSpecified() throws Exception {
    Application application = defaultTestLifecycle.createApplication(null,
        newConfigWith("<application android:name=\"" + "ClassNameToIgnore" + "\"/>"), new Config.Builder().setApplication(TestFakeApp.class).build());
    assertThat(application).isExactlyInstanceOf(TestFakeApp.class);
  }

  @Test public void shouldLoadConfigInnerClassApplication() throws Exception {
    Application application = defaultTestLifecycle.createApplication(null,
        newConfigWith("<application android:name=\"" + "ClassNameToIgnore" + "\"/>"), new Config.Builder().setApplication(TestFakeAppInner.class).build());
    assertThat(application).isExactlyInstanceOf(TestFakeAppInner.class);
  }

  @Test public void shouldLoadTestApplicationIfClassIsPresent() throws Exception {
    Application application = defaultTestLifecycle.createApplication(null,
        newConfigWith("<application android:name=\"" + FakeApp.class.getName() + "\"/>"), null);
    assertThat(application).isExactlyInstanceOf(TestFakeApp.class);
  }

  @Test public void whenNoAppManifestPresent_shouldCreateGenericApplication() throws Exception {
    assertThat(defaultTestLifecycle.createApplication(null, null, null)).isExactlyInstanceOf(Application.class);
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

    Files.write(fileContents, f, Charsets.UTF_8);
    return new AndroidManifest(Fs.newFile(f), null, null);
  }

  public static class TestFakeAppInner extends Application { }
}
