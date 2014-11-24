package org.robolectric.shadows;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.internal.Shadow;

@Implements(className = ShadowDisplayManagerGlobal.REAL_CLASS_NAME)
public class ShadowDisplayManagerGlobal {
  public static final String REAL_CLASS_NAME = "android.hardware.display.DisplayManagerGlobal";


  @Implementation
  public static Object getInstance() {
    return Shadow.newInstanceOf(REAL_CLASS_NAME);
  }

  @Implementation
  public Object getDisplayInfo(int displayId) {
    return Shadow.newInstanceOf("android.view.DisplayInfo");
  }
}
