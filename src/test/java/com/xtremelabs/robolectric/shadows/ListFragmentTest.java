package com.xtremelabs.robolectric.shadows;

import android.support.v4.app.ListFragment;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Tests for ShadowListFragment..
 */

@RunWith(WithTestDefaultsRunner.class)
public class ListFragmentTest
{
    @Test
    public void _setListAdapterIsRetrievedByGetListAdapter()
    {
        ListFragment fragment = new ListFragment();
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(null, 0);
        fragment.setListAdapter(adapter);

        assertThat(fragment.getListAdapter(), sameInstance((ListAdapter) adapter));
    }
}
