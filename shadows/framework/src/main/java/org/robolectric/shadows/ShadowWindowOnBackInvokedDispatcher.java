package org.robolectric.shadows;

import android.os.Build;
import android.window.WindowOnBackInvokedDispatcher;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;
import org.robolectric.util.ReflectionHelpers;

/** Shadow for {@link WindowOnBackInvokedDispatcher}. */
@Implements(
    value = WindowOnBackInvokedDispatcher.class,
    minSdk = Build.VERSION_CODES.UPSIDE_DOWN_CAKE,
    isInAndroidSdk = false)
public class ShadowWindowOnBackInvokedDispatcher {
  private static final boolean ENABLE_PREDICTIVE_BACK_DEFAULT =
      RuntimeEnvironment.getApiLevel() >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE
          ? ReflectionHelpers.getStaticField(
              WindowOnBackInvokedDispatcher.class, "ENABLE_PREDICTIVE_BACK")
          : false;

  static void setEnablePredictiveBack(boolean isEnabled) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
      ReflectionHelpers.setStaticField(
          WindowOnBackInvokedDispatcher.class, "ENABLE_PREDICTIVE_BACK", isEnabled);
    }
  }

  @Resetter
  public static void reset() {
    ReflectionHelpers.setStaticField(
        WindowOnBackInvokedDispatcher.class,
        "ENABLE_PREDICTIVE_BACK",
        ENABLE_PREDICTIVE_BACK_DEFAULT);
  }
}
