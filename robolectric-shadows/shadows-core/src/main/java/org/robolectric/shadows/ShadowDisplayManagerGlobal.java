package org.robolectric.shadows;

import android.hardware.display.DisplayManagerGlobal;
import android.hardware.display.IDisplayManager;
import android.hardware.display.RoboDisplayManager;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.internal.Shadow;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;

@Implements(className = ShadowDisplayManagerGlobal.REAL_CLASS_NAME)
public class ShadowDisplayManagerGlobal {
  private static final RoboDisplayManager displayManager = new RoboDisplayManager();
  public static final String REAL_CLASS_NAME = "android.hardware.display.DisplayManagerGlobal";

  @Implementation
  public static Object getInstance() {
    return ReflectionHelpers.callConstructorReflectively(DisplayManagerGlobal.class, new ClassParameter(IDisplayManager.class, displayManager));
  }

  @Implementation
  public Object getDisplayInfo(int displayId) {
    return Shadow.newInstanceOf("android.view.DisplayInfo");
  }
}
