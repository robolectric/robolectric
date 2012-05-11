package com.xtremelabs.robolectric.shadows;

import android.database.DataSetObserver;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;
import com.xtremelabs.robolectric.internal.RealObject;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;

@Implements(ViewPager.class)
public class ShadowViewPager extends ShadowViewGroup {
    @RealObject
    private ViewPager realViewPager;

    private PagerAdapter adapter;

    @Implementation
    public void setAdapter(PagerAdapter adapter) {
        this.adapter = adapter;
        shadowOf(adapter).registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                dataSetChanged();
            }
        });
        dataSetChanged();
    }

    @Implementation
    public PagerAdapter getAdapter() {
        return adapter;
    }

    @Implementation
    public void dataSetChanged() {
        int count = adapter.getCount();
        if (count > 0) {
            adapter.startUpdate(realViewPager);
            Object item = adapter.instantiateItem(realViewPager, 0);
            adapter.setPrimaryItem(realViewPager, 0, item);
            adapter.finishUpdate(realViewPager);
        }
    }
}