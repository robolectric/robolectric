package org.robolectric;

import static org.robolectric.Shadows.shadowOf;

import android.content.pm.PackageInfo;
import android.os.Build;
import android.view.Display;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowDisplay;
import org.robolectric.util.ReflectionHelpers;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = Config.ALL_SDKS)
public class LoadWeirdClassesTest {

  @Test
  @Config(sdk = Config.OLDEST_SDK)
  public void shouldLoadDisplay() {
    ReflectionHelpers.callInstanceMethod(
        Display.class, ShadowDisplay.getDefaultDisplay(), "getDisplayAdjustments");
  }

  @Test
  public void reset_shouldWorkEvenIfSdkIntIsOverridden() {
    ReflectionHelpers.setStaticField(Build.VERSION.class, "SDK_INT", 23);
  }

  @Test
  public void packageManager() {
    PackageInfo packageInfo = new PackageInfo();
    packageInfo.packageName = "test.package";
    shadowOf(RuntimeEnvironment.application.getPackageManager()).addPackage(packageInfo);
  }
}
