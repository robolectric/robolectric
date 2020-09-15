package org.robolectric.shadows;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.OnScrollListener;
import java.util.List;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.util.ReflectionHelpers;

/** Shadow for {@link RecyclerView}. */
@Implements(RecyclerView.class)
public class ShadowRecyclerView extends ShadowViewGroup {
  @RealObject RecyclerView recyclerView;

  /**
   * Triggers {@link RecyclerView.OnScrollListener#onScrollStateChanged(RecyclerView, int)} for all
   * listeners.
   */
  public void triggerOnScrollStateChanged(int newState) {
    if (!(newState == RecyclerView.SCROLL_STATE_DRAGGING
        || newState == RecyclerView.SCROLL_STATE_IDLE
        || newState == RecyclerView.SCROLL_STATE_SETTLING)) {
      throw new IllegalArgumentException(
          String.format("Invalid scroll state for RecyclerView: %s", newState));
    }

    List<OnScrollListener> onScrollListeners =
        ReflectionHelpers.getField(recyclerView, "mScrollListeners");

    if (onScrollListeners == null) {
      return;
    }

    for (OnScrollListener onScrollListener : onScrollListeners) {
      onScrollListener.onScrollStateChanged(recyclerView, newState);
    }
  }
}
