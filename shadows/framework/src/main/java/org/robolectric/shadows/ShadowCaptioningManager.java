package org.robolectric.shadows;

import android.annotation.NonNull;
import android.util.ArraySet;
import android.view.accessibility.CaptioningManager;
import android.view.accessibility.CaptioningManager.CaptioningChangeListener;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/** Shadow of {@link android.view.accessibility.CaptioningManager}. */
@Implements(CaptioningManager.class)
public class ShadowCaptioningManager {
  private float fontScale = 1;
  private boolean isEnabled = false;

  private final ArraySet<CaptioningChangeListener> listeners = new ArraySet<>();

  /** Returns 1.0 as default or the most recent value passed to {@link #setFontScale()} */
  @Implementation(minSdk = 19)
  protected float getFontScale() {
    return fontScale;
  }

  /** Sets the value to be returned by {@link CaptioningManager#getFontScale()} */
  public void setFontScale(float fontScale) {
    this.fontScale = fontScale;

    for (CaptioningChangeListener captioningChangeListener : listeners) {
      captioningChangeListener.onFontScaleChanged(fontScale);
    }
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

  @Implementation(minSdk = 19)
  protected void addCaptioningChangeListener(@NonNull CaptioningChangeListener listener) {
    listeners.add(listener);
  }

  @Implementation(minSdk = 19)
  protected void removeCaptioningChangeListener(@NonNull CaptioningChangeListener listener) {
    listeners.remove(listener);
  }
}
