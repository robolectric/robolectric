package xxx;

import android.net.ConnectivityManager;
import org.robolectric.annotation.Implements;

/**
 * Fake shadow for testing {@link org.robolectric.errorprone.bugpatterns.DeprecatedMethodsCheck}.
 */
@Implements(ConnectivityManager.class)
public class XShadowNetworkInfo {
  public void setConnectionType(int connectionType) {
  }
}
