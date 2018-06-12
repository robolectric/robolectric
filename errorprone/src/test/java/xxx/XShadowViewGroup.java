package xxx;

import android.view.ViewGroup;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.LayoutAnimationController;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/** Fake shadow for testing {@link org.robolectric.errorprone.bugpatterns.ShadowUsageCheck}. */
@Implements(ViewGroup.class)
public class XShadowViewGroup {
  @Implementation
  public void setLayoutAnimationListener(AnimationListener listener) {}

  @Implementation
  public AnimationListener getLayoutAnimationListener() {
    return null;
  }

  @Implementation
  public void setLayoutAnimation(LayoutAnimationController layoutAnim) {}

  @Implementation
  public LayoutAnimationController getLayoutAnimation() {
    return null;
  }
}
