package org.robolectric;

import android.content.Context;
import android.os.Build;
import android.view.Display;
import android.view.WindowManager;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowRenderNodeAnimator;
import org.robolectric.util.ReflectionHelpers;

import static android.os.Build.VERSION_CODES.KITKAT;
import static android.os.Build.VERSION_CODES.LOLLIPOP;

@RunWith(MultiApiRobolectricTestRunner.class)
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
}