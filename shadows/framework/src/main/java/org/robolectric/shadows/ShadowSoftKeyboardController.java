package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.N;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityService.SoftKeyboardController;
import android.os.Handler;
import android.os.Looper;
import java.util.HashMap;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;

/** Shadow of SoftKeyboardController. */
@Implements(value = SoftKeyboardController.class, minSdk = N)
public class ShadowSoftKeyboardController {

  @RealObject private SoftKeyboardController realObject;

  private final HashMap<SoftKeyboardController.OnShowModeChangedListener, Handler> listeners =
      new HashMap<>();
  private int showMode = AccessibilityService.SHOW_MODE_AUTO;

  @Implementation
  protected void addOnShowModeChangedListener(
      SoftKeyboardController.OnShowModeChangedListener listener, Handler handler) {
    listeners.put(listener, handler);
  }

  @Implementation
  protected void addOnShowModeChangedListener(
      SoftKeyboardController.OnShowModeChangedListener listener) {
    listeners.put(listener, new Handler(Looper.getMainLooper()));
  }

  @Implementation
  protected int getShowMode() {
    return showMode;
  }

  @Implementation
  protected boolean setShowMode(int showMode) {
    this.showMode = showMode;
    notifyOnShowModeChangedListeners();
    return true;
  }

  @Implementation
  protected boolean removeOnShowModeChangedListener(
      SoftKeyboardController.OnShowModeChangedListener listener) {
    if (!listeners.containsKey(listener)) {
      return false;
    }
    listeners.remove(listener);
    return true;
  }

  private void notifyOnShowModeChangedListeners() {
    for (SoftKeyboardController.OnShowModeChangedListener listener : listeners.keySet()) {
      Handler handler = listeners.get(listener);
      handler.post(() -> listener.onShowModeChanged(realObject, showMode));
    }
  }
}
