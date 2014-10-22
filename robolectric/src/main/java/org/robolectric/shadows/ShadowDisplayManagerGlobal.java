package org.robolectric.shadows;

import org.robolectric.Robolectric;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

import static org.robolectric.Robolectric.newInstanceOf;

@Implements(value = Robolectric.Anything.class, className = ShadowDisplayManagerGlobal.REAL_CLASS_NAME)
public class ShadowDisplayManagerGlobal {
  public static final String REAL_CLASS_NAME = "android.hardware.display.DisplayManagerGlobal";


  @Implementation
  public static Object getInstance() {
    return newInstanceOf(REAL_CLASS_NAME);
  }

  @Implementation
  public Object getDisplayInfo(int displayId) {
    return newInstanceOf("android.view.DisplayInfo");
  }
}
