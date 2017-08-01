package org.robolectric;

import static android.os.Build.VERSION_CODES.KITKAT;
import static org.robolectric.Shadows.shadowOf;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.view.Display;
import android.view.WindowManager;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.util.ReflectionHelpers;

@RunWith(RobolectricTestRunner.class) @Config(sdk = Config.ALL_SDKS)
public class LoadWeirdClassesTest {
  @Test @Config(sdk = KITKAT)
  public void shouldLoadDisplay() throws Exception {
    WindowManager windowManager = (WindowManager) RuntimeEnvironment.application.getSystemService(Context.WINDOW_SERVICE);
    ReflectionHelpers.callInstanceMethod(Display.class, windowManager.getDefaultDisplay(), "getDisplayAdjustments");
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