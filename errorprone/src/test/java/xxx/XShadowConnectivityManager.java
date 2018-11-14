package xxx;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/**
 * Fake shadow for testing {@link org.robolectric.errorprone.bugpatterns.DeprecatedMethodsCheck}.
 */
@Implements(ConnectivityManager.class)
public class XShadowConnectivityManager {
  @Implementation
  public NetworkInfo getActiveNetworkInfo() {
    return null;
  }
}
