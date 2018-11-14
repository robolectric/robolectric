package xxx;

import android.app.AlertDialog;
import org.robolectric.annotation.Implements;

/**
 * Fake shadow for testing {@link org.robolectric.errorprone.bugpatterns.DeprecatedMethodsCheck}.
 */
@Implements(AlertDialog.class)
public class XShadowAlertDialog {
  public static AlertDialog getLatestAlertDialog() {
    return null;
  }
}
