package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR1;
import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR2;

import android.app.UiAutomation;
import android.content.ContentResolver;
import android.provider.Settings;
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
  protected void throwIfNotConnectedLocked() {}
}
