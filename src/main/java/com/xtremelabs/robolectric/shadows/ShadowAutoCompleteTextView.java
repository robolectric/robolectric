package com.xtremelabs.robolectric.shadows;

import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.Filterable;
import android.widget.ListAdapter;

/**
 * A shadow for AutoCompleteTextView
 */
@Implements(AutoCompleteTextView.class)
public class ShadowAutoCompleteTextView extends ShadowEditText {

    private ListAdapter adapter;
    private int threshold = 2;
    private OnItemClickListener onItemClickListener;

    @Implementation
    public ListAdapter getAdapter() {
        return adapter;
    }

    @Implementation
    public <T extends ListAdapter & Filterable> void setAdapter(T adapter) {
        this.adapter = adapter;
    }

    @Implementation
    public int getThreshold() {
        return threshold;
    }

    @Implementation
    public void setThreshold(int threshold) {
        if (threshold <= 0) {
            threshold = 1;
        }
        this.threshold = threshold;
    }

    @Implementation
    public AdapterView.OnItemClickListener getOnItemClickListener() {
        return onItemClickListener;
    }

    @Implementation
    public void setOnItemClickListener(AdapterView.OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }
}