package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.LOLLIPOP;

import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.AccessibilityWindowInfo;
import java.util.ArrayList;
import java.util.List;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/**
 * Shadow of AccessibilityService that tracks global actions and provides a mechanism to simulate
 * the window list.
 */
@Implements(AccessibilityService.class)
public class ShadowAccessibilityService extends ShadowService {

  private final List<Integer> globalActionsPerformed = new ArrayList<>();
  private final List<AccessibilityWindowInfo> windows = new ArrayList<>();

  @Implementation
  protected final boolean performGlobalAction(int action) {
    globalActionsPerformed.add(action);
    return true;
  }

  public List<Integer> getGlobalActionsPerformed() {
    return globalActionsPerformed;
  }

  /**
   * Returns a representation of interactive windows shown on the device screen. Mirrors the values
   * provided to {@link #setWindows(List<AccessibilityWindowInfo>)}. Returns an empty List if not
   * set.
   */
  @Implementation(minSdk = LOLLIPOP)
  protected List<AccessibilityWindowInfo> getWindows() {
    return new ArrayList<>(windows);
  }

  /**
   * Sets the list of interactive windows shown on the device screen as reported by {@link
   * #getWindows()}
   */
  public void setWindows(List<AccessibilityWindowInfo> windowList) {
    windows.clear();
    if (windowList != null) {
      windows.addAll(windowList);
    }
  }
}
