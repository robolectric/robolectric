package org.robolectric;

import static org.assertj.core.api.Assertions.assertThat;

import android.app.Application;
import android.content.pm.ApplicationInfo;
import com.google.common.base.Charsets;
import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.manifest.AndroidManifest;
import org.robolectric.res.Fs;

@RunWith(RobolectricTestRunner.class)
public class DefaultTestLifecycleTest {

  @Rule public TemporaryFolder temporaryFolder = new TemporaryFolder();
  private DefaultTestLifecycle defaultTestLifecycle = new DefaultTestLifecycle();

  @Test(expected = RuntimeException.class)
  public void shouldThrowWhenManifestContainsBadApplicationClassName() throws Exception {
    ApplicationInfo applicationInfo = new ApplicationInfo();
    applicationInfo.packageName = "org.robolectric";
    applicationInfo.className = "org.robolectric.BogusTestApplication";

    defaultTestLifecycle.createApplication(null, applicationInfo, null);
  }

  @Test
  public void shouldReturnDefaultAndroidApplicationWhenManifestDeclaresNoAppName() throws Exception {
    assertThat(defaultTestLifecycle.createApplication(null, new ApplicationInfo(), null))
        .isExactlyInstanceOf(Application.class);
  }

  @Test
  public void shouldReturnSpecifiedApplicationWhenManifestDeclaresAppName() throws Exception {
    ApplicationInfo applicationInfo = new ApplicationInfo();
    applicationInfo.packageName = "xxx";
    applicationInfo.className = "org.robolectric.TestApplication";
    assertThat(defaultTestLifecycle.createApplication(null, applicationInfo, null))
        .isExactlyInstanceOf(TestApplication.class);
  }

  @Test public void shouldAssignThePackageNameFromTheManifest() throws Exception {
    Application application = RuntimeEnvironment.application;

    assertThat(application.getPackageName()).isEqualTo("org.robolectric");
    assertThat(application).isExactlyInstanceOf(TestApplication.class);
  }

  @Test public void shouldDoTestApplicationNameTransform() throws Exception {
    assertThat(defaultTestLifecycle.getTestApplicationName(".Applicationz")).isEqualTo(".TestApplicationz");
    assertThat(defaultTestLifecycle.getTestApplicationName("Applicationz")).isEqualTo("TestApplicationz");
    assertThat(defaultTestLifecycle.getTestApplicationName("com.foo.Applicationz")).isEqualTo("com.foo.TestApplicationz");
  }

  @Test public void shouldLoadConfigApplicationIfSpecified() throws Exception {
    ApplicationInfo applicationInfo = new ApplicationInfo();
    applicationInfo.packageName = "org.robolectric";
    applicationInfo.className = "ClassNameToIgnore";
    Application application = defaultTestLifecycle.createApplication(null, applicationInfo,
        new Config.Builder().setApplication(TestFakeApp.class).build());
    assertThat(application).isExactlyInstanceOf(TestFakeApp.class);
  }

  @Test public void shouldLoadConfigInnerClassApplication() throws Exception {
    ApplicationInfo applicationInfo = new ApplicationInfo();
    applicationInfo.packageName = "org.robolectric";
    applicationInfo.className = "ClassNameToIgnore";
    Application application = defaultTestLifecycle.createApplication(null, applicationInfo,
        new Config.Builder().setApplication(TestFakeAppInner.class).build());
    assertThat(application).isExactlyInstanceOf(TestFakeAppInner.class);
  }

  @Test public void shouldLoadTestApplicationIfClassIsPresent() throws Exception {
    ApplicationInfo applicationInfo = new ApplicationInfo();
    applicationInfo.packageName = "org.robolectric";
    applicationInfo.className = FakeApp.class.getName();
    Application application = defaultTestLifecycle.createApplication(null, applicationInfo, null);
    assertThat(application).isExactlyInstanceOf(TestFakeApp.class);
  }

  @Test public void whenNoAppManifestPresent_shouldCreateGenericApplication() throws Exception {
    assertThat(defaultTestLifecycle.createApplication(null, (ApplicationInfo) null, null)).isExactlyInstanceOf(Application.class);
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
