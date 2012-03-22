package com.xtremelabs.robolectric.shadows;

import android.app.ListActivity;
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
public class ListActivityTest {

    @Test
    public void shouldSupportSettingAndGettingListAdapter(){
        ListActivity listActivity = new ListActivity();
        ListAdapter adapter = new CountingAdapter(5);
        listActivity.setListAdapter(adapter);

        assertThat(listActivity.getListAdapter(), is(notNullValue()));
    }

    @Test
    public void shouldSupportOnItemClick() throws Exception {
        final boolean[] clicked = new boolean[1];
        ListActivity listActivity = new ListActivity() {
            @Override
            protected void onListItemClick(ListView l, View v, int position, long id) {
                clicked[0] = true;
            }
        };
        listActivity.setContentView(new ListView(null));
        listActivity.setListAdapter(new CountingAdapter(5));
        Robolectric.shadowOf(listActivity.getListView()).performItemClick(0);
        assertTrue(clicked[0]);
    }

    @Test
    public void shouldSetAdapterOnListView() throws Exception {
        ListActivity listActivity = new ListActivity();
        ListAdapter adapter = new CountingAdapter(5);
        final ListView listView = new ListView(null);
        listActivity.setContentView(listView);
        listActivity.setListAdapter(adapter);
        assertThat(listView.getAdapter(), sameInstance(adapter));
    }
}
