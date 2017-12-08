package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR1;

import android.hardware.display.DisplayManager;
import android.hardware.display.DisplayManagerGlobal;
import android.os.Build;
import android.view.Display;
import android.view.DisplayInfo;
import java.util.function.Consumer;
import org.robolectric.annotation.Implements;
import org.robolectric.shadow.api.Shadow;

/**
 * For tests, display properties may be changed and devices may be added or removed
 * programmatically.
 */
@Implements(value = DisplayManager.class, minSdk = JELLY_BEAN_MR1)
public class ShadowDisplayManager {

  /**
   * Adds a simulated display.
   *
   * @param displayInfo properties for the new display
   * @return the new display's ID
   */
  public static int addDisplay(DisplayInfo displayInfo) {
    return getShadowDisplayManagerGlobal().addDisplay(displayInfo);
  }

  /**
   * Changes properties of a simulated display.
   *
   * @param displayId the display id to change
   * @param displayInfo new properties for the display
   */
  public static void changeDisplay(int displayId, DisplayInfo displayInfo) {
    getShadowDisplayManagerGlobal().changeDisplay(displayId, displayInfo);
  }

  /**
   * Changes properties of a simulated display. The original properties will be passed to the
   * `consumer`, which may modify them in place. The display will be updated with the new
   * properties.
   *
   * @param displayId the display id to change
   * @param consumer a function which modifies the display properties
   */
  public static void changeDisplay(int displayId, Consumer<DisplayInfo> consumer) {
    DisplayInfo displayInfo =
        new DisplayInfo(DisplayManagerGlobal.getInstance().getDisplayInfo(displayId));
    consumer.accept(displayInfo);
    getShadowDisplayManagerGlobal().changeDisplay(displayId, displayInfo);
  }

  /**
   * Changes properties of the default display. The original properties will be passed to the
   * `consumer`, which may modify them in place. The display will be updated with the new
   * properties.
   *
   * @param consumer a function which modifies the display properties
   */
  public static void changeDefaultDisplay(Consumer<DisplayInfo> consumer) {
    int displayId = Display.DEFAULT_DISPLAY;
    changeDisplay(displayId, consumer);
  }

  /**
   * Removes a simulated display.
   *
   * @param displayId the display id to remove
   */
  public static void removeDisplay(int displayId) {
    getShadowDisplayManagerGlobal().removeDisplay(displayId);
  }

  private static ShadowDisplayManagerGlobal getShadowDisplayManagerGlobal() {
    if (Build.VERSION.SDK_INT < JELLY_BEAN_MR1) {
      throw new UnsupportedOperationException("multiple displays not supported in Jelly Bean");
    }

    return Shadow.extract(DisplayManagerGlobal.getInstance());
  }
}
