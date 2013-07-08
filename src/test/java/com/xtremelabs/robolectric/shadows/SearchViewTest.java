package com.xtremelabs.robolectric.shadows;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.SearchView;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.hamcrest.core.IsSame;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(WithTestDefaultsRunner.class)
public class SearchViewTest {

    @Test
    public void canGetAndSetSuggestionsAdapter() throws Exception {
        Activity context = new Activity();
        SearchView searchView = new SearchView(context);
        CursorAdapter adapter = new CursorAdapter(context, null, false) {
            @Override
            public View newView(Context context, Cursor cursor, ViewGroup parent) {
                return null;
            }

            @Override
            public void bindView(View view, Context context, Cursor cursor) {
            }
        };
        searchView.setSuggestionsAdapter(adapter);
        assertThat(searchView.getSuggestionsAdapter(), sameInstance(adapter));
    }
}
