package org.robolectric;

import static android.os.Build.VERSION_CODES.KITKAT;
import static org.robolectric.Shadows.shadowOf;

import android.app.Activity;
import android.os.Build;
import android.view.Display;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowDisplay;
import org.robolectric.util.ReflectionHelpers;

@RunWith(RobolectricTestRunner.class) @Config(sdk = Config.ALL_SDKS)
public class LoadWeirdClassesTest {
  @Test @Config(sdk = KITKAT)
  public void shouldLoadDisplay() throws Exception {
    ReflectionHelpers.callInstanceMethod(Display.class, ShadowDisplay.getDefaultDisplay(), "getDisplayAdjustments");
  }

  @Test
  public void reset_shouldWorkEvenIfSdkIntIsOverridden() throws Exception {
    ReflectionHelpers.setStaticField(Build.VERSION.class, "SDK_INT", 23);
  }

  @Test
  public void shadowOf_shouldCompile() throws Exception {
    shadowOf(Robolectric.setupActivity(Activity.class));
  }
}