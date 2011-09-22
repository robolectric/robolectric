package com.xtremelabs.robolectric.shadows;

import android.app.ListActivity;
import android.widget.ListAdapter;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
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
}
