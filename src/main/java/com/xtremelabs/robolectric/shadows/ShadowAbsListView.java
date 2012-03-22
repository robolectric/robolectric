package com.xtremelabs.robolectric.shadows;

import android.widget.AbsListView;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

@Implements(AbsListView.class)
public class ShadowAbsListView extends ShadowAdapterView {
    private AbsListView.OnScrollListener onScrollListener;
    private int smoothScrolledPosition;

    @Implementation
    public void setOnScrollListener(AbsListView.OnScrollListener l) {
        onScrollListener = l;
    }
    
    @Implementation
    public void smoothScrollToPosition(int position) {
        smoothScrolledPosition = position;
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
}
