package org.robolectric.shadows;

import static android.app.UiAutomation.ROTATION_FREEZE_0;
import static android.app.UiAutomation.ROTATION_FREEZE_180;
import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR1;
import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR2;
import static org.robolectric.Shadows.shadowOf;

import android.app.UiAutomation;
import android.content.ContentResolver;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.provider.Settings;
import android.view.Display;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/** Shadow for {@link UiAutomation}. */
@Implements(value = UiAutomation.class, minSdk = JELLY_BEAN_MR2)
public class ShadowUiAutomation {

  /**
   * Sets the animation scale, see {@link UiAutomation#setAnimationScale(float)}. Provides backwards
   * compatible access to SDKs < T.
   */
  public static void setAnimationScaleCompat(float scale) {
    ContentResolver cr = RuntimeEnvironment.getApplication().getContentResolver();
    if (RuntimeEnvironment.getApiLevel() >= JELLY_BEAN_MR1) {
      Settings.Global.putFloat(cr, Settings.Global.ANIMATOR_DURATION_SCALE, scale);
      Settings.Global.putFloat(cr, Settings.Global.TRANSITION_ANIMATION_SCALE, scale);
      Settings.Global.putFloat(cr, Settings.Global.WINDOW_ANIMATION_SCALE, scale);
    } else {
      Settings.System.putFloat(cr, Settings.System.ANIMATOR_DURATION_SCALE, scale);
      Settings.System.putFloat(cr, Settings.System.TRANSITION_ANIMATION_SCALE, scale);
      Settings.System.putFloat(cr, Settings.System.WINDOW_ANIMATION_SCALE, scale);
    }
  }

  // TODO: Add implementation once T support is added to Robolectric
  // @Implementation(minSdk = TIRAMISU)
  protected void setAnimationScale(float scale) {
    setAnimationScaleCompat(scale);
  }

  @Implementation
  protected boolean setRotation(int rotation) {
    if (rotation == UiAutomation.ROTATION_FREEZE_CURRENT
        || rotation == UiAutomation.ROTATION_UNFREEZE) {
      return true;
    }
    Display display = ShadowDisplay.getDefaultDisplay();
    int currentRotation = display.getRotation();
    boolean isRotated =
        (rotation == ROTATION_FREEZE_0 || rotation == ROTATION_FREEZE_180)
            != (currentRotation == ROTATION_FREEZE_0 || currentRotation == ROTATION_FREEZE_180);
    shadowOf(display).setRotation(rotation);
    if (isRotated) {
      int currentOrientation = Resources.getSystem().getConfiguration().orientation;
      String rotationQualifier =
          "+" + (currentOrientation == Configuration.ORIENTATION_PORTRAIT ? "land" : "port");
      ShadowDisplayManager.changeDisplay(display.getDisplayId(), rotationQualifier);
      RuntimeEnvironment.setQualifiers(rotationQualifier);
    }
    return true;
  }

  @Implementation
  protected void throwIfNotConnectedLocked() {}
}
