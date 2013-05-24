package org.robolectric.shadows;

import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.TestRunners;
import org.robolectric.annotation.Config;

import static org.fest.assertions.api.Assertions.assertThat;

@RunWith(TestRunners.WithDefaults.class) @Config(manifest = Config.NONE)
public class NonAppLibraryTest {
  @Test public void shouldStillCreateAnApplication() throws Exception {
    assertThat(Robolectric.application).isExactlyInstanceOf(Application.class);
  }

  @Test public void applicationShouldHaveSomeReasonableConfig() throws Exception {
    assertThat(Robolectric.application.getPackageName()).isEqualTo("org.robolectric.default");
  }

  @Test public void shouldHaveDefaultPackageInfo() throws Exception {
    PackageInfo packageInfo = Robolectric.packageManager.getPackageInfo("org.robolectric.default", 0);
    assertThat(packageInfo).isNotNull();

    ApplicationInfo applicationInfo = packageInfo.applicationInfo;
    assertThat(applicationInfo).isNotNull();
    assertThat(applicationInfo.packageName).isEqualTo("org.robolectric.default");
  }
  
  @Test public void shouldCreatePackageContext() throws Exception {
    Context packageContext = Robolectric.application.createPackageContext("org.robolectric.default", 0);
    assertThat(packageContext).isNotNull();
  }
}
