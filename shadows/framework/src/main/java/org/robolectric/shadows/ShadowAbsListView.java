package org.robolectric.shadows;

import static org.robolectric.util.reflector.Reflector.reflector;

import android.widget.AbsListView;
import org.robolectric.annotation.Filter;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.ForType;

@Implements(AbsListView.class)
public class ShadowAbsListView extends ShadowAdapterView {
  @RealObject private AbsListView realAbsListView;

  private int smoothScrolledPosition;
  private int lastSmoothScrollByDistance;
  private int lastSmoothScrollByDuration;

  @Filter
  protected void smoothScrollToPosition(int position) {
    smoothScrolledPosition = position;
  }

  @Filter
  protected void smoothScrollBy(int distance, int duration) {
    this.lastSmoothScrollByDistance = distance;
    this.lastSmoothScrollByDuration = duration;
  }

  /**
   * Robolectric accessor for the onScrollListener
   *
   * @return AbsListView.OnScrollListener
   */
  public AbsListView.OnScrollListener getOnScrollListener() {
    return reflector(AbsListViewReflector.class, realAbsListView).getOnScrollListener();
  }

  /**
   * Robolectric accessor for the last smoothScrolledPosition
   *
   * @return int position
   */
  public int getSmoothScrolledPosition() {
    return smoothScrolledPosition;
  }

  /**
   * Robolectric accessor for the last smoothScrollBy distance
   *
   * @return int distance
   */
  public int getLastSmoothScrollByDistance() {
    return lastSmoothScrollByDistance;
  }

  /**
   * Robolectric accessor for the last smoothScrollBy duration
   *
   * @return int duration
   */
  public int getLastSmoothScrollByDuration() {
    return lastSmoothScrollByDuration;
  }

  @ForType(AbsListView.class)
  interface AbsListViewReflector {
    @Accessor("mOnScrollListener")
    AbsListView.OnScrollListener getOnScrollListener();
  }
}
