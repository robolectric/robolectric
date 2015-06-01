package org.robolectric.shadows.support.v4;

import org.robolectric.internal.Shadow;
import org.robolectric.annotation.RealObject;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Implementation;
import org.robolectric.shadows.ShadowViewGroup;

import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;

/**
 * Shadow for {@link android.support.v4.widget.SwipeRefreshLayout}
 */
@Implements(SwipeRefreshLayout.class)
public class ShadowSwipeRefreshLayout extends ShadowViewGroup {
  @RealObject SwipeRefreshLayout realObject;
  private OnRefreshListener listener;

  @Implementation
  public void setOnRefreshListener(OnRefreshListener listener) {
    this.listener = listener;
    Shadow.directlyOn(realObject, SwipeRefreshLayout.class).setOnRefreshListener(listener);
  }

  /**
   * Non-Android accessor.
   *
   * @return OnRefreshListener that was previously set.
   */
  public OnRefreshListener getOnRefreshListener() {
    return listener;
  }
}