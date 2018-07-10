package xxx;

import android.graphics.drawable.Drawable;
import org.robolectric.annotation.Implements;

/** Fake shadow for testing {@link org.robolectric.errorprone.bugpatterns.ShadowUsageCheck}. */
@Implements(Drawable.class)
public class XShadowDrawable {
  public int getCreatedFromResId() {
    return 1234;
  }
}
