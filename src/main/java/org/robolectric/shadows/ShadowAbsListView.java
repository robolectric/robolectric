package org.robolectric.shadows;

import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;
import org.robolectric.internal.Implementation;
import org.robolectric.internal.Implements;

@Implements(AbsListView.class)
public class ShadowAbsListView extends ShadowAdapterView {
    private AbsListView.OnScrollListener onScrollListener;
    private int smoothScrolledPosition;
    private int lastSmoothScrollByDistance;
    private int lastSmoothScrollByDuration;
    private int choiceMode;
    private SparseBooleanArray checkedItemPositions = new SparseBooleanArray();

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
    @Override
    public boolean performItemClick(View view, int position, long id) {
        boolean handled = false;
        if (choiceMode != ListView.CHOICE_MODE_NONE) {
            handled = true;

            if (choiceMode == ListView.CHOICE_MODE_MULTIPLE) {
                boolean newValue = !checkedItemPositions.get(position, false);
                checkedItemPositions.put(position, newValue);
            } else {
                boolean newValue = !checkedItemPositions.get(position, false);
                if (newValue) {
                    checkedItemPositions.clear();
                    checkedItemPositions.put(position, true);
                }
            }
        }

        handled |= super.performItemClick(view, position, id);
        return handled;
    }

    @Implementation
    public int getCheckedItemPosition() {
        if (choiceMode != ListView.CHOICE_MODE_SINGLE || checkedItemPositions.size() != 1)
            return ListView.INVALID_POSITION;

        return checkedItemPositions.keyAt(0);
    }

    @Implementation
    public void setItemChecked(int position, boolean value) {
        if (choiceMode == ListView.CHOICE_MODE_SINGLE) {
            checkedItemPositions.clear();
            checkedItemPositions.put(position, value);
        } else if (choiceMode == ListView.CHOICE_MODE_MULTIPLE) {
            checkedItemPositions.put(position, value);
        }
    }


    @Implementation
    public int getChoiceMode() {
        return choiceMode;
    }

    @Implementation
    public void setChoiceMode(int choiceMode) {
        this.choiceMode = choiceMode;
    }


    @Implementation
    public SparseBooleanArray getCheckedItemPositions() {
        if (choiceMode == ListView.CHOICE_MODE_NONE)
            return null;

        return checkedItemPositions;
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
