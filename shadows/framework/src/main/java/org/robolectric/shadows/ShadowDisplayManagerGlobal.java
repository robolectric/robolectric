package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR1;
import static android.os.Build.VERSION_CODES.M;
import static org.robolectric.RuntimeEnvironment.getApiLevel;

import android.hardware.display.DisplayManagerGlobal;
import android.hardware.display.IDisplayManager;
import android.view.Display;
import android.view.DisplayInfo;
import java.util.ArrayList;
import java.util.List;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;

@Implements(value = DisplayManagerGlobal.class, isInAndroidSdk = false, minSdk = JELLY_BEAN_MR1)
public class ShadowDisplayManagerGlobal {
  private static final IDisplayManager displayManager = ReflectionHelpers.createNullProxy(IDisplayManager.class);
  private static DisplayManagerGlobal instance;

  @Resetter
  public void reset() {
    instance = null;
  }

  @Implementation
  synchronized public static DisplayManagerGlobal getInstance() {
    if (instance == null) {
      instance = ReflectionHelpers.callConstructor(DisplayManagerGlobal.class,
          ClassParameter.from(IDisplayManager.class, displayManager));
    }
    return instance;
  }

  public static ShadowDisplayManagerGlobal getShadowInstance() {
    return Shadow.extract(DisplayManagerGlobal.getInstance());
  }

  private final List<DisplayInfo> displayInfos = new ArrayList<>();

  synchronized public int addDisplay(DisplayInfo displayInfo) {
    displayInfos.add(displayInfo);
    return displayInfos.size() - 1;
  }

  @Implementation
  synchronized public DisplayInfo getDisplayInfo(int displayId) {
    return displayInfos.get(displayId);
  }
}