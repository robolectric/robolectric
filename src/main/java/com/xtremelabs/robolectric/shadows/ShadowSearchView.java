package com.xtremelabs.robolectric.shadows;

import android.widget.CursorAdapter;
import android.widget.SearchView;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

@Implements(SearchView.class)
public class ShadowSearchView extends ShadowLinearLayout {
    private CursorAdapter suggestionsAdapter;
    private SearchView.OnQueryTextListener onQueryTextListener;
    private SearchView.OnSuggestionListener onSuggestionListener;

    @Implementation
    public CursorAdapter getSuggestionsAdapter() {
        return suggestionsAdapter;
    }

    @Implementation
    public void setSuggestionsAdapter(CursorAdapter suggestionsAdapter) {
        this.suggestionsAdapter = suggestionsAdapter;
    }

    public SearchView.OnQueryTextListener getOnQueryTextListener() {
        return onQueryTextListener;
    }

    @Implementation
    public void setOnQueryTextListener(SearchView.OnQueryTextListener onQueryTextListener) {
        this.onQueryTextListener = onQueryTextListener;
    }

    public SearchView.OnSuggestionListener getOnSuggestionListener() {
        return onSuggestionListener;
    }

    @Implementation
    public void setOnSuggestionListener(SearchView.OnSuggestionListener onSuggestionListener) {
        this.onSuggestionListener = onSuggestionListener;
    }
}
