package org.robolectric.shadows;

import static org.assertj.core.api.Assertions.assertThat;

import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.TestRunners;
import org.robolectric.annotation.Config;

@Config(manifest = Config.NONE)
@RunWith(TestRunners.MultiApiSelfTest.class)
public class ShadowNonAppLibraryTest {
  @Test public void shouldStillCreateAnApplication() throws Exception {
    assertThat(RuntimeEnvironment.application).isExactlyInstanceOf(Application.class);
  }

  @Test public void applicationShouldHaveSomeReasonableConfig() throws Exception {
    assertThat(RuntimeEnvironment.application.getPackageName()).isEqualTo("org.robolectric.default");
  }

  @Test public void shouldHaveDefaultPackageInfo() throws Exception {
    PackageInfo packageInfo = RuntimeEnvironment.application.getPackageManager().getPackageInfo("org.robolectric.default", 0);
    assertThat(packageInfo).isNotNull();

    ApplicationInfo applicationInfo = packageInfo.applicationInfo;
    assertThat(applicationInfo).isNotNull();
    assertThat(applicationInfo.packageName).isEqualTo("org.robolectric.default");
  }

  @Test public void shouldCreatePackageContext() throws Exception {
    Context packageContext = RuntimeEnvironment.application.createPackageContext("org.robolectric.default", 0);
    assertThat(packageContext).isNotNull();
  }
}
