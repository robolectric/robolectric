package org.robolectric;

import android.app.Application;
import android.content.Intent;
import android.content.pm.PackageManager;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.res.Fs;
import org.robolectric.shadows.ShadowApplication;
import org.robolectric.test.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;
import static org.robolectric.Robolectric.shadowOf;
import static org.robolectric.util.TestUtil.newConfig;

@RunWith(TestRunners.WithDefaults.class)
public class DefaultTestLifecycleTest {

  @Rule public TemporaryFolder temporaryFolder = new TemporaryFolder();
  private DefaultTestLifecycle defaultTestLifecycle = new DefaultTestLifecycle();

  @Test(expected = RuntimeException.class)
  public void shouldThrowWhenManifestContainsBadApplicationClassName() throws Exception {
    defaultTestLifecycle.createApplication(null,
        newConfigWith("<application android:name=\"org.robolectric.BogusTestApplication\"/>)"));
  }

  @Test
  public void shouldReturnDefaultAndroidApplicationWhenManifestDeclaresNoAppName() throws Exception {
    assertThat(defaultTestLifecycle.createApplication(null, newConfigWith("")))
        .isExactlyInstanceOf(Application.class);
  }

  @Test
  public void shouldReturnSpecifiedApplicationWhenManifestDeclaresAppName() throws Exception {
    assertThat(defaultTestLifecycle.createApplication(null,
        newConfigWith("<application android:name=\"org.robolectric.TestApplication\"/>")))
        .isExactlyInstanceOf(TestApplication.class);
  }

  @Test
  public void shouldAssignThePackageNameFromTheManifest() throws Exception {
    AndroidManifest appManifest = newConfigWith("com.wacka.wa", "");
    Application application = defaultTestLifecycle.createApplication(null, appManifest);
    shadowOf(application).bind(appManifest, null);

    assertThat(application.getPackageName()).isEqualTo("com.wacka.wa");
    assertThat(application).isExactlyInstanceOf(Application.class);
  }

  @Test
  public void shouldRegisterReceiversFromTheManifest() throws Exception {
    AndroidManifest appManifest = newConfig("TestAndroidManifestWithReceivers.xml");
    Application application = defaultTestLifecycle.createApplication(null, appManifest);
    shadowOf(application).bind(appManifest, null);

    List<ShadowApplication.Wrapper> receivers = shadowOf(application).getRegisteredReceivers();
    assertThat(receivers.size()).isEqualTo(6);
    assertTrue(receivers.get(0).intentFilter.matchAction("org.robolectric.ACTION1"));
  }

  @Test
  public void shouldRegisterActivitiesFromManifestInPackageManager() throws Exception {
    AndroidManifest appManifest = newConfig("TestAndroidManifestForActivities.xml");
    Application application = defaultTestLifecycle.createApplication(null, appManifest);

    PackageManager packageManager = application.getPackageManager();
    assertThat(packageManager.resolveActivity(new Intent("org.robolectric.shadows.TestActivity"), -1)).isNotNull();
    assertThat(packageManager.resolveActivity(new Intent("org.robolectric.shadows.TestActivity2"), -1)).isNotNull();

  }

  @Test public void shouldDoTestApplicationNameTransform() throws Exception {
    assertThat(defaultTestLifecycle.getTestApplicationName(".Applicationz")).isEqualTo(".TestApplicationz");
    assertThat(defaultTestLifecycle.getTestApplicationName("Applicationz")).isEqualTo("TestApplicationz");
    assertThat(defaultTestLifecycle.getTestApplicationName("com.foo.Applicationz")).isEqualTo("com.foo.TestApplicationz");
  }

  @Test public void shouldLoadTestApplicationIfClassIsPresent() throws Exception {
    Application application = defaultTestLifecycle.createApplication(null,
        newConfigWith("<application android:name=\"" + FakeApp.class.getName() + "\"/>"));
    assertThat(application).isExactlyInstanceOf(TestFakeApp.class);
  }

  @Test public void whenNoAppManifestPresent_shouldCreateGenericApplication() throws Exception {
    assertThat(defaultTestLifecycle.createApplication(null, null)).isExactlyInstanceOf(Application.class);
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
}
