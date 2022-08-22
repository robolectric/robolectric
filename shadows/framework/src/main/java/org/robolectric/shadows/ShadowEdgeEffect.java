package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.S;

import android.view.MotionEvent;
import android.widget.EdgeEffect;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/** Shadow for {@link android.widget.EdgeEffect} */
@Implements(EdgeEffect.class)
public class ShadowEdgeEffect {

  /**
   * Disable edge effects for Android S and above. The problem with edge effects in S+ is that
   * ScrollView will intercept/swallow all touch events while edge effects are still running (in
   * {@link android.widget.ScrollView#onInterceptTouchEvent(MotionEvent)}. {@link EdgeEffect}
   * completion depends on a free-running clock and draw traversals being continuously performed. So
   * for Robolectric to ensure that edge effects are complete, it has to bump the uptime and then
   * re-run draw traversals any time an edge effect starts.
   *
   * <p>Because edge effects are not critical for unit testing, it is simpler to disable them.
   */
  @Implementation(minSdk = S)
  protected int getCurrentEdgeEffectBehavior() {
    return -1; // EdgeEffect.TYPE_NONE (disables edge effects)
  }
}
