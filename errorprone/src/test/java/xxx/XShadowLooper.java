package xxx;

import android.os.Looper;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/**
 * Fake shadow for testing {@link org.robolectric.errorprone.bugpatterns.DeprecatedMethodsCheck}.
 */
@Implements(Looper.class)
public class XShadowLooper {
  @Implementation
  public static Looper getMainLooper() {
    return null;
  }

  public String getSchedule() {
    return null;
  }

  public void runToEndOfTasks() {
  }
}
