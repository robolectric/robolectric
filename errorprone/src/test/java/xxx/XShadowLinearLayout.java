package xxx;

import android.widget.LinearLayout;
import org.robolectric.annotation.Implements;

/**
 * Fake shadow for testing {@link org.robolectric.errorprone.bugpatterns.RobolectricBestPractices}.
 */
@Implements(LinearLayout.class)
public class XShadowLinearLayout extends XShadowViewGroup {
  public int getGravity() {
    return 0;
  }
}
