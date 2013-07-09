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
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.xtremelabs.robolectric.RobolectricShadowOfLevel16.shadowOf;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

@RunWith(WithTestDefaultsRunner.class)
public class SearchViewTest {
    private Activity context;
    private SearchView searchView;

    @Before
    public void setup() throws Exception {
        context = new Activity();
        searchView = new SearchView(context);
    }

    @Test
    public void canGetAndSetSuggestionsAdapter() throws Exception {
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
    
    @Test
    public void canSetQueryTextListener() throws Exception {
        SearchView.OnQueryTextListener queryTextListener = mock(SearchView.OnQueryTextListener.class);
        searchView.setOnQueryTextListener(queryTextListener);
        assertThat(shadowOf(searchView).getOnQueryTextListener(), sameInstance(queryTextListener));
    }
}
