package org.robolectric;

import static android.os.Build.VERSION_CODES.KITKAT;
import static org.junit.Assume.assumeThat;
import static org.robolectric.Shadows.shadowOf;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.os.Build;
import android.view.Display;
import java.io.File;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowDisplay;
import org.robolectric.util.ReflectionHelpers;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = Config.ALL_SDKS)
public class LoadWeirdClassesTest {

  @Test
  @Config(sdk = KITKAT)
  public void shouldLoadDisplay() {
    ReflectionHelpers.callInstanceMethod(
        Display.class, ShadowDisplay.getDefaultDisplay(), "getDisplayAdjustments");
  }

  @Test
  public void reset_shouldWorkEvenIfSdkIntIsOverridden() {
    ReflectionHelpers.setStaticField(Build.VERSION.class, "SDK_INT", 23);
  }

  @Test
  public void shadowOf_shouldCompile() {
    assumeThat("Windows is an affront to decency.",
        File.separator, Matchers.equalTo("/"));

    shadowOf(Robolectric.setupActivity(Activity.class));
  }

  @Test
  public void packageManager() {
    PackageInfo packageInfo = new PackageInfo();
    packageInfo.packageName = "test.package";
    shadowOf(RuntimeEnvironment.application.getPackageManager()).addPackage(packageInfo);
  }
}
