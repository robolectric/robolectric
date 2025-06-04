package org.robolectric.shadows;


import android.app.UiModeManager;
import java.util.Set;

/**
 * Placeholder shadow for {@link UiModeManager} that is spelled correctly.
 *
 * <p>This is designed to be used in place of ShadowUIModeManager. It contains public API method
 * signatures from that class.
 */
public abstract class ShadowUiModeManager {

  public abstract boolean isNightModeOn();

  public abstract Set<Integer> getActiveProjectionTypes();

  public abstract int getApplicationNightMode();

  public abstract void setFailOnProjectionToggle(boolean failOnProjectionToggle);

  public abstract void setCurrentModeType(int modeType);

  public abstract int getLastCarModePriority();

  public abstract int getLastFlags();

  public abstract void setContrast(float contrast);
}
