package xxx;

import android.app.Dialog;
import org.robolectric.annotation.Implements;

/**
 * Fake shadow for testing {@link org.robolectric.errorprone.bugpatterns.DeprecatedMethodsCheck}.
 */
@Implements(Dialog.class)
public class XShadowDialog {
  public static Dialog getLatestDialog() {
    return null;
  }
}
