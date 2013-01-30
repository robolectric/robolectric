package com.xtremelabs.robolectric.shadows;

import android.widget.AbsListView;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

@Implements(AbsListView.class)
public class ShadowAbsListView extends ShadowAdapterView {
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
