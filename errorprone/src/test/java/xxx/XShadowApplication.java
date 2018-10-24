package xxx;

import android.app.Application;
import android.content.Context;
import android.os.Looper;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/**
 * Fake shadow for testing {@link org.robolectric.errorprone.bugpatterns.DeprecatedMethodsCheck}.
 */
@Implements(Application.class)
public class XShadowApplication {
  public static XShadowApplication getInstance() {
    return null;
  }

  @Implementation
  public Context getApplicationContext() {
    return null;
  }

  public XShadowAlertDialog getLatestAlertDialog() {
    return null;
  }

  public XShadowDialog getLatestDialog() {
    return null;
  }

  public XShadowPopupMenu getLatestPopupMenu() {
    return null;
  }

  @Implementation
  public Looper getMainLooper() {
    return null;
  }

  public void runBackgroundTasks() {}
}
