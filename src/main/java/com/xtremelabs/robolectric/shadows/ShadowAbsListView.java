package com.xtremelabs.robolectric.shadows;

import android.widget.AbsListView;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

@Implements(AbsListView.class)
public class ShadowAbsListView extends ShadowAdapterView {
    private AbsListView.OnScrollListener onScrollListener;

    @Implementation
    public void setOnScrollListener(AbsListView.OnScrollListener l) {
        onScrollListener = l;
    }

    /**
     * Robolectric accessor for the onScrollListener
     *
     * @return AbsListView.OnScrollListener
     */
    public AbsListView.OnScrollListener getOnScrollListener() {
        return onScrollListener;
    }
}
