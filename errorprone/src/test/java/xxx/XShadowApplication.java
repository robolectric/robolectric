package xxx;

import android.app.Application;
import android.content.Context;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/**
 * Fake shadow for testing {@link org.robolectric.errorprone.bugpatterns.RobolectricBestPractices}.
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

  public void runBackgroundTasks() {}
}
