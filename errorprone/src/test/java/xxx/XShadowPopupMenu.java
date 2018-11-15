package xxx;

import android.widget.PopupMenu;
import org.robolectric.annotation.Implements;

/**
 * Fake shadow for testing {@link org.robolectric.errorprone.bugpatterns.DeprecatedMethodsCheck}.
 */
@Implements(PopupMenu.class)
public class XShadowPopupMenu {
  public static PopupMenu getLatestPopupMenu() {
    return null;
  }
}
