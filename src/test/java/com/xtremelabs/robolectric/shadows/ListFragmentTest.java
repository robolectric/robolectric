package com.xtremelabs.robolectric.shadows;

import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.assertTrue;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(WithTestDefaultsRunner.class)
public class ListFragmentTest {

    @Test
    public void shouldSupportSettingAndGettingListAdapter(){
        ListFragment listFragment = new ListFragment();
        ListAdapter adapter = new CountingAdapter(5);
        listFragment.setListAdapter(adapter);

        assertThat(listFragment.getListAdapter(), is(notNullValue()));
    }

    @Test
    public void shouldSupportOnItemClick() throws Exception {
        final boolean[] clicked = new boolean[1];
        ListFragment listFragment = new ListFragment() {
            @Override
            public void onListItemClick(ListView l, View v, int position, long id) {
                clicked[0] = true;
            }
        };
        Robolectric.shadowOf(listFragment).setView(new ListView(null));
        listFragment.setListAdapter(new CountingAdapter(5));
        Robolectric.shadowOf(listFragment.getListView()).performItemClick(0);
        assertTrue(clicked[0]);
    }

    @Test
    public void shouldSetAdapterOnListView() throws Exception {
        ListFragment listFragment = new ListFragment();
        ListAdapter adapter = new CountingAdapter(5);
        final ListView listView = new ListView(null);
        Robolectric.shadowOf(listFragment).setView(listView);
        listFragment.setListAdapter(adapter);
        assertThat(listView.getAdapter(), sameInstance(adapter));
    }
}
