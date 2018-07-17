package org.robolectric.shadows;

import android.view.accessibility.CaptioningManager;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/** Shadow of {@link android.view.accessibility.CaptioningManager}. */
@Implements(CaptioningManager.class)
public class ShadowCaptioningManager {
  private float fontScale = 1;
  private boolean isEnabled = false;

  /** Returns 1.0 as default or the most recent value passed to {@link #setFontScale()} */
  @Implementation(minSdk = 19)
  protected float getFontScale() {
    return fontScale;
  }

  /** Sets the value to be returned by {@link CaptioningManager#getFontScale()} */
  public void setFontScale(float fontScale) {
    this.fontScale = fontScale;
  }

  /** Returns false or the most recent value passed to {@link #setEnabled(boolean)} */
  @Implementation(minSdk = 19)
  protected boolean isEnabled() {
    return isEnabled;
  }

  /** Sets the value to be returned by {@link CaptioningManager#isEnabled()} */
  public void setEnabled(boolean isEnabled) {
    this.isEnabled = isEnabled;
  }
}
