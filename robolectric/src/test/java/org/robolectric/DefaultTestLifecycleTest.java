package org.robolectric;

import android.app.Application;
import android.content.Intent;
import android.content.pm.PackageManager;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.manifest.AndroidManifest;
import org.robolectric.res.Fs;
import org.robolectric.shadows.ShadowApplication;
import org.robolectric.test.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class DefaultTestLifecycleTest {

  @Rule public TemporaryFolder temporaryFolder = new TemporaryFolder();
  private DefaultTestLifecycle defaultTestLifecycle = new DefaultTestLifecycle();

  @Test(expected = RuntimeException.class)
  public void shouldThrowWhenManifestContainsBadApplicationClassName() throws Exception {
    defaultTestLifecycle.getApplicationClass(null,
        newConfigWith("<application android:name=\"org.robolectric.BogusTestApplication\"/>)"), null);
  }

  @Test
  public void shouldReturnDefaultAndroidApplicationWhenManifestDeclaresNoAppName() throws Exception {
    assertThat(defaultTestLifecycle.getApplicationClass(null, newConfigWith(""), null))
        .isEqualTo(Application.class);
  }

  @Test
  public void shouldReturnSpecifiedApplicationWhenManifestDeclaresAppName() throws Exception {
    assertThat(defaultTestLifecycle.getApplicationClass(null,
        newConfigWith("<application android:name=\"org.robolectric.TestApplication\"/>"), null))
        .isEqualTo(TestApplication.class);
  }

  @Test
  @Config(manifest = "src/test/resources/TestAndroidManifestWithReceiversCustomPackage.xml")
  public void shouldAssignThePackageNameFromTheManifest() throws Exception {
    assertThat(RuntimeEnvironment.application.getPackageName()).isEqualTo("org.robolectric.mypackage");
    assertThat(RuntimeEnvironment.application).isExactlyInstanceOf(Application.class);
  }

  @Test
  @Config(manifest = "src/test/resources/TestAndroidManifestWithReceivers.xml")
  public void shouldRegisterReceiversFromTheManifest() throws Exception {
    List<ShadowApplication.Wrapper> receivers = ShadowApplication.getInstance().getRegisteredReceivers();
    assertThat(receivers.size()).isEqualTo(5);
    assertTrue(receivers.get(0).intentFilter.matchAction("org.robolectric.ACTION1"));
  }

  @Test
  @Config(manifest = "src/test/resources/TestAndroidManifestForActivities.xml")
  public void shouldRegisterActivitiesFromManifestInPackageManager() throws Exception {

    PackageManager packageManager = RuntimeEnvironment.application.getPackageManager();
    assertThat(packageManager.resolveActivity(new Intent("org.robolectric.shadows.TestActivity"), -1)).isNotNull();
    assertThat(packageManager.resolveActivity(new Intent("org.robolectric.shadows.TestActivity2"), -1)).isNotNull();
  }

  @Test public void shouldDoTestApplicationNameTransform() throws Exception {
    assertThat(defaultTestLifecycle.getTestApplicationName(".Applicationz")).isEqualTo(".TestApplicationz");
    assertThat(defaultTestLifecycle.getTestApplicationName("Applicationz")).isEqualTo("TestApplicationz");
    assertThat(defaultTestLifecycle.getTestApplicationName("com.foo.Applicationz")).isEqualTo("com.foo.TestApplicationz");
  }

  @Test public void shouldLoadConfigApplicationIfSpecified() throws Exception {
    Class application = defaultTestLifecycle.getApplicationClass(null,
        newConfigWith("<application android:name=\"" + "ClassNameToIgnore" + "\"/>"), new Config.Implementation(new int[0], "", "", "", "", "", new Class[0], new String[0], TestFakeApp.class, new String[0], null));
    assertThat(application).isEqualTo(TestFakeApp.class);
  }

  @Test public void shouldLoadConfigInnerClassApplication() throws Exception {
    Class application = defaultTestLifecycle.getApplicationClass(null,
        newConfigWith("<application android:name=\"" + "ClassNameToIgnore" + "\"/>"), new Config.Implementation(new int[0], "", "", "", "", "", new Class[0], new String[0], TestFakeAppInner.class, new String[0], null));
    assertThat(application).isEqualTo(TestFakeAppInner.class);
  }

  @Test public void shouldLoadTestApplicationIfClassIsPresent() throws Exception {
    Class application = defaultTestLifecycle.getApplicationClass(null,
        newConfigWith("<application android:name=\"" + FakeApp.class.getName() + "\"/>"), null);
    assertThat(application).isEqualTo(TestFakeApp.class);
  }

  @Test public void whenNoAppManifestPresent_shouldCreateGenericApplication() throws Exception {
    assertThat(defaultTestLifecycle.getApplicationClass(null, null, null)).isEqualTo(Application.class);
  }

  /////////////////////////////

  public AndroidManifest newConfigWith(String contents) throws IOException {
    return newConfigWith("org.robolectric", contents);
  }

  private AndroidManifest newConfigWith(String packageName, String contents) throws IOException {
    File f = temporaryFolder.newFile("whatever.xml",
        "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
            "<manifest xmlns:android=\"http://schemas.android.com/apk/res/android\"\n" +
            "          package=\"" + packageName + "\">\n" +
            "    " + contents + "\n" +
            "</manifest>\n");
    return new AndroidManifest(Fs.newFile(f), null, null);
  }

  public static class TestFakeAppInner extends Application { }
}
