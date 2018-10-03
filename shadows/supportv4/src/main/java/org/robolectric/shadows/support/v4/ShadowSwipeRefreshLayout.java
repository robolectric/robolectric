package org.robolectric.shadows.support.v4;

import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.shadows.ShadowViewGroup;

@Implements(SwipeRefreshLayout.class)
public class ShadowSwipeRefreshLayout extends ShadowViewGroup {
  @RealObject SwipeRefreshLayout realObject;
  private OnRefreshListener listener;

  @Implementation
  protected void setOnRefreshListener(OnRefreshListener listener) {
    this.listener = listener;
    Shadow.directlyOn(realObject, SwipeRefreshLayout.class).setOnRefreshListener(listener);
  }

  /**
   * @return OnRefreshListener that was previously set.
   */
  public OnRefreshListener getOnRefreshListener() {
    return listener;
  }
}