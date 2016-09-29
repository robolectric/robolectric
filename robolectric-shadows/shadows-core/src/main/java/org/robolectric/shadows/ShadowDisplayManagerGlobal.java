package org.robolectric.shadows;

import android.hardware.display.DisplayManagerGlobal;
import android.hardware.display.IDisplayManager;
import android.os.Build;
import android.view.Display;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.internal.Shadow;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR1;

/**
 * Shadow for {@link android.hardware.display.DisplayManagerGlobal}.
 */
@Implements(value = DisplayManagerGlobal.class, isInAndroidSdk = false, minSdk = JELLY_BEAN_MR1)
public class ShadowDisplayManagerGlobal {
  private static final IDisplayManager displayManager = ReflectionHelpers.createNullProxy(IDisplayManager.class);

  @Implementation
  public static Object getInstance() {
    return ReflectionHelpers.callConstructor(DisplayManagerGlobal.class,
        ClassParameter.from(IDisplayManager.class, displayManager));
  }

  @Implementation
  public Object getDisplayInfo(int displayId) {
    Object result = Shadow.newInstanceOf("android.view.DisplayInfo");
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      ReflectionHelpers.setField(result, "supportedModes", new Display.Mode[]{new Display.Mode(0, 0, 0, 0.0f)});
    }
    return result;
  }
}