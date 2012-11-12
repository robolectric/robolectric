package com.xtremelabs.robolectric.shadows;

import android.support.v4.app.ListFragment;
import android.widget.AbsListView;
import android.widget.ListAdapter;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

/**
 * Mimicks ListFragment, despite the fact that the v4 Fragment classes are effortlessly available
 * to developers anyway. Too bad they weren't excluded.
 */

@Implements(ListFragment.class)
public class ShadowListFragment
{
    private ListAdapter mAdapter;

    @Implementation
    public void setListAdapter(android.widget.ListAdapter adapter)
    {
        mAdapter = adapter;
    }

    @Implementation
    public ListAdapter getListAdapter()
    {
        return mAdapter;
    }
}
