package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR1;
import static android.os.Build.VERSION_CODES.M;
import static org.robolectric.RuntimeEnvironment.getApiLevel;

import android.hardware.display.DisplayManagerGlobal;
import android.hardware.display.IDisplayManager;
import android.view.Display;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;

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
    if (getApiLevel() >= M) {
      ReflectionHelpers.setField(result, "supportedModes", new Display.Mode[]{new Display.Mode(0, 0, 0, 0.0f)});
    }
    return result;
  }
}