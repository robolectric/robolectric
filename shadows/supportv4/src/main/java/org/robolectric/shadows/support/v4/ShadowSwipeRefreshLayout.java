package org.robolectric.shadows.support.v4;

import static org.robolectric.util.reflector.Reflector.reflector;

import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.shadows.ShadowViewGroup;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.ForType;

/**
 * Deprecated. Use SwipeRefreshLayout#setRefreshing to trigger a OnRefreshListener.onRefresh call
 * instead.
 */
@Implements(SwipeRefreshLayout.class)
@Deprecated
public class ShadowSwipeRefreshLayout extends ShadowViewGroup {
  @RealObject SwipeRefreshLayout realObject;

  /**
   * @return OnRefreshListener that was previously set.
   */
  public OnRefreshListener getOnRefreshListener() {
    return reflector(SwipeRefreshLayoutReflector.class, realObject).getOnRefreshListener();
  }

  @ForType(SwipeRefreshLayout.class)
  interface SwipeRefreshLayoutReflector {
    @Accessor("mListener")
    OnRefreshListener getOnRefreshListener();
  }
}