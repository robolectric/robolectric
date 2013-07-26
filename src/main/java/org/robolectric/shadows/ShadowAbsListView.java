package org.robolectric.shadows;

import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.AbsListView;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;

import static org.robolectric.Robolectric.directlyOn;

@Implements(AbsListView.class)
public class ShadowAbsListView extends ShadowAdapterView {
  @RealObject private AbsListView realAbsListView;
  private AbsListView.OnScrollListener onScrollListener;
  private int smoothScrolledPosition;
  private int lastSmoothScrollByDistance;
  private int lastSmoothScrollByDuration;

  @Implementation
  public void setOnScrollListener(AbsListView.OnScrollListener l) {
    onScrollListener = l;
  }

  @Implementation
  public void smoothScrollToPosition(int position) {
    smoothScrolledPosition = position;
  }

  @Implementation
  public void smoothScrollBy(int distance, int duration) {
    this.lastSmoothScrollByDistance = distance;
    this.lastSmoothScrollByDuration = duration;
  }

  @Implementation
  public boolean performItemClick(View view, int position, long id) {
    return ((Boolean) directlyOn(realAbsListView, AbsListView.class, "performItemClick", View.class, int.class, long.class).invoke(view, position, id));
  }

  @Implementation
  public int getCheckedItemPosition() {
    return ((Integer) directlyOn(realAbsListView, AbsListView.class, "getCheckedItemPosition").invoke());
  }

  @Implementation
  public int getCheckedItemCount() {
    return ((Integer) directlyOn(realAbsListView, AbsListView.class, "getCheckedItemCount").invoke());
  }

  @Implementation
  public void setItemChecked(int position, boolean value) {
    directlyOn(realAbsListView, AbsListView.class, "setItemChecked", int.class, boolean.class).invoke(position, value);
  }

  @Implementation
  public int getChoiceMode() {
    return (Integer) directlyOn(realAbsListView, AbsListView.class, "getChoiceMode").invoke();
  }

  @Implementation
  public void setChoiceMode(int choiceMode) {
    directlyOn(realAbsListView, AbsListView.class, "setChoiceMode", int.class).invoke(choiceMode);
  }

  @Implementation
  public SparseBooleanArray getCheckedItemPositions() {
    return (SparseBooleanArray) directlyOn(realAbsListView, AbsListView.class, "getCheckedItemPositions").invoke();
  }

  @Implementation
  public long[] getCheckedItemIds() {
    return (long[]) directlyOn(realAbsListView, AbsListView.class, "getCheckedItemIds").invoke();
  }

  /**
   * Robolectric accessor for the onScrollListener
   *
   * @return AbsListView.OnScrollListener
   */
  public AbsListView.OnScrollListener getOnScrollListener() {
    return onScrollListener;
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
}
